/*
 * Copyright © 2024 Baird Creek Software LLC
 *
 * Licensed under the PolyForm Noncommercial License, version 1.0.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://polyformproject.org/licenses/noncommercial/1.0.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package is.galia.plugin.libtiff;

import is.galia.Application;
import is.galia.codec.AbstractDecoder;
import is.galia.codec.Decoder;
import is.galia.codec.DecoderHint;
import is.galia.codec.SourceFormatException;
import is.galia.codec.tiff.Directory;
import is.galia.codec.tiff.DirectoryReader;
import is.galia.codec.tiff.EXIFBaselineTIFFTagSet;
import is.galia.codec.tiff.EXIFGPSTagSet;
import is.galia.codec.tiff.EXIFInteroperabilityTagSet;
import is.galia.codec.tiff.EXIFTagSet;
import is.galia.codec.tiff.TagSet;
import is.galia.image.Size;
import is.galia.image.Format;
import is.galia.image.MediaType;
import is.galia.image.Metadata;
import is.galia.image.Orientation;
import is.galia.image.Region;
import is.galia.image.ReductionFactor;
import is.galia.plugin.Plugin;
import is.galia.stream.PathImageInputStream;
import is.galia.util.IOUtils;
import is.galia.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.libtiff.tiff_h_1.C_INT;
import static org.libtiff.tiff_h_1.C_POINTER;
import static org.libtiff.tiff_h_1.TIFFTAG_ORIENTATION;
import static org.libtiff.tiff_h_1.TIFFTAG_ROWSPERSTRIP;
import static org.libtiff.tiff_h_1.TIFFTAG_SAMPLESPERPIXEL;
import static org.libtiff.tiff_h_1.TIFFTAG_TILELENGTH;
import static org.libtiff.tiff_h_1.TIFFTAG_TILEWIDTH;
import static org.libtiff.tiffio_h.TIFFClientOpenExt;
import static org.libtiff.tiffio_h.TIFFClose;
import static org.libtiff.tiffio_h.TIFFGetField;
import static org.libtiff.tiffio_h.TIFFGetVersion;
import static org.libtiff.tiffio_h.TIFFOpenExt;
import static org.libtiff.tiffio_h.TIFFOpenOptionsAlloc;
import static org.libtiff.tiffio_h.TIFFOpenOptionsFree;
import static org.libtiff.tiffio_h.TIFFOpenOptionsSetErrorHandlerExtR;
import static org.libtiff.tiffio_h.TIFFOpenOptionsSetWarningHandlerExtR;
import static org.libtiff.tiffio_h.TIFFReadDirectory;
import static org.libtiff.tiffio_h.TIFFReadRGBAStrip;
import static org.libtiff.tiffio_h.TIFFReadRGBATile;
import static org.libtiff.tiffio_h.TIFFSetDirectory;
import static org.libtiff.tiffio_h.vsnprintf;
import static org.libtiff.tiffio_h_1.TIFFTAG_IMAGELENGTH;
import static org.libtiff.tiffio_h_1.TIFFTAG_IMAGEWIDTH;

/**
 * <p>Implementation using the Java Foreign Function & Memory API to call into
 * the libtiff library.</p>
 *
 * @see <a href="http://www.libtiff.org/functions.html">TIFF Functions
 *     Overview</a>
 */
public final class LibTIFFDecoder extends AbstractDecoder
        implements Decoder, Plugin {

    private static class Image {
        int width, length, orientation, rowsPerStrip, samplesPerPixel,
                tileWidth, tileLength;
        Directory ifd;

        Size getSize() {
            return new Size(width, length);
        }

        Size getTileSize() {
            int twidth  = (tileWidth > 0) ? tileWidth : width;
            int theight = (tileLength > 0) ? tileLength : length;
            return new Size(twidth, theight);
        }

        Orientation getOrientation() {
            return Orientation.forTIFFOrientation(orientation);
        }

        boolean isTiled() {
            return tileWidth > 0 && tileLength > 0;
        }
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LibTIFFDecoder.class);

    static final Format FORMAT = new Format(
            "tif",                                   // key
            "TIFF",                                  // name
            List.of(new MediaType("image", "tiff")), // media types
            List.of("tif", "tiff", "ptif", "tf8"),   // extensions
            true,                                    // isRaster
            false,                                   // isVideo
            true);                                   // supportsTransparency

    private static final AtomicBoolean IS_CLASS_INITIALIZED =
            new AtomicBoolean();

    static final Map<Long,LibTIFFDecoder> LIVE_INSTANCES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final FunctionDescriptor ERROR_HANDLER_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER,
                    C_POINTER, C_POINTER, C_POINTER);
    private static final FunctionDescriptor WARNING_HANDLER_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER,
                    C_POINTER, C_POINTER, C_POINTER);
    private static MethodHandle ERROR_HANDLER_FUNCTION, WARNING_HANDLER_FUNCTION;

    private MemorySegment tiff;
    private final List<Image> images = new ArrayList<>();
    /** If we set {@link #inputStream} ourselves, {@link #close()} will need
     * to close it. */
    private boolean ownsInputStream;

    ImageInputStream getInputStream() {
        return inputStream;
    }

    //endregion
    //region Plugin methods

    @Override
    public Set<String> getPluginConfigKeys() {
        return Set.of();
    }

    @Override
    public String getPluginName() {
        return LibTIFFDecoder.class.getSimpleName();
    }

    @Override
    public void onApplicationStart() {
        if (!IS_CLASS_INITIALIZED.getAndSet(true)) {
            System.loadLibrary("tiff");
            MemorySegment segment = TIFFGetVersion();
            String version = segment.getString(0);
            LOGGER.info("{}", version);
            StreamFunctions.initializeClass();
            try {
                ERROR_HANDLER_FUNCTION = MethodHandles.lookup().findStatic(
                        LibTIFFDecoder.class, "handleError",
                        ERROR_HANDLER_FUNCTION_DESCRIPTOR.toMethodType());
                WARNING_HANDLER_FUNCTION = MethodHandles.lookup().findStatic(
                        LibTIFFDecoder.class, "handleWarning",
                        WARNING_HANDLER_FUNCTION_DESCRIPTOR.toMethodType());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private static int handleError(MemorySegment tiff,
                                   MemorySegment userData,
                                   MemorySegment module,
                                   MemorySegment fmt,
                                   MemorySegment ap) {
        return handle(1, module, fmt, ap);
    }

    private static int handleWarning(MemorySegment tiff,
                                     MemorySegment userData,
                                     MemorySegment module,
                                     MemorySegment fmt,
                                     MemorySegment ap) {
        String message = fmt.getString(0);
        if (message.startsWith("Unknown field with tag")) {
            return 1;
        }
        return handle(2, module, fmt, ap);
    }

    private static int handle(int type,
                              MemorySegment module,
                              MemorySegment fmt,
                              MemorySegment ap) {
        try (Arena arena = Arena.ofConfined()) {
            // `fmt` is a printf string; `ap` is a va_list of varargs to be
            // injected into the string. We will use `vsnprintf()` to assemble
            // the string in a native buffer which we will bring back into the
            // JVM and log.
            long bufSize = 2048;
            MemorySegment buf = arena.allocate(bufSize);
            vsnprintf(buf, bufSize, fmt, ap);
            String message   = buf.getString(0);
            String moduleStr = module.getString(0);
            if (moduleStr != null) {
                message = moduleStr + ": " + message;
            }
            if (type == 2) {
                LOGGER.warn(message);
            } else {
                LOGGER.error(message);
            }
            if (Application.isTesting()) { // logging is disabled when testing
                System.err.println(((type == 1) ? "ERROR: " : "WARNING: ") + message);
            }
        }
        return 1;
    }

    @Override
    public void onApplicationStop() {
    }

    @Override
    public void initializePlugin() {
        LIVE_INSTANCES.put(Thread.currentThread().threadId(), this);
    }

    //endregion
    //region Decoder methods

    @Override
    public void close() {
        super.close();
        if (tiff != null) TIFFClose(tiff);
        if (ownsInputStream && inputStream != null) {
            IOUtils.closeQuietly(inputStream);
        }
        LIVE_INSTANCES.remove(Thread.currentThread().threadId());
    }

    @Override
    public Format detectFormat() throws IOException {
        try {
            openFile();
            inputStream.seek(0);
            byte[] magicBytes = new byte[4];
            int b, i = 0;
            while ((b = inputStream.read()) != -1 && i < magicBytes.length) {
                magicBytes[i] = (byte) b;
                i++;
            }
            inputStream.seek(0);
            if ((magicBytes[0] == 0x49 && magicBytes[1] == 0x49 &&
                    magicBytes[2] == 0x2a && magicBytes[3] == 0x00) ||
                    (magicBytes[0] == 0x4d && magicBytes[1] == 0x4d &&
                            magicBytes[2] == 0x00 && magicBytes[3] == 0x2a)) {
                return FORMAT;
            }
        } catch (SourceFormatException e) {
            return Format.UNKNOWN;
        }
        return Format.UNKNOWN;
    }

    @Override
    public int getNumImages() throws IOException {
        readIFDs();
        return images.size();
    }

    @Override
    public int getNumResolutions() throws IOException {
        return isPyramid() ? images.size() : 1;
    }

    /**
     * @return Full source image dimensions.
     */
    @Override
    public Size getSize(int imageIndex) throws IOException {
        validateImageIndex(imageIndex);
        return images.get(imageIndex).getSize();
    }

    @Override
    public Set<Format> getSupportedFormats() {
        return Set.of(FORMAT);
    }

    @Override
    public Size getTileSize(int imageIndex) throws IOException {
        validateImageIndex(imageIndex);
        return images.get(imageIndex).getTileSize();
    }

    /**
     * N.B.: {@code imageIndex} does not correspond to pyramid level! It is
     * used only for multi-page (not multi-resolution) images. It is actually
     * {@code scales} that selects a pyramid level.
     */
    @Override
    public BufferedImage decode(int imageIndex,
                                Region orientedRegion,
                                double[] scales,
                                ReductionFactor reductionFactor,
                                double[] diffScales,
                                Set<DecoderHint> decoderHints) throws IOException {
        Stopwatch watch = new Stopwatch();
        validateImageIndex(imageIndex);

        ReductionFactor xFactor = ReductionFactor.forScale(scales[0]);
        ReductionFactor yFactor = ReductionFactor.forScale(scales[1]);
        reductionFactor.factor  = Math.min(xFactor.factor, yFactor.factor);

        // When reading a pyramidal TIFF, imageIndex will be provided as 0
        // rather than the pyramid level. In that case, we want to adjust it to
        // the pyramid level.
        if (imageIndex == 0 && scales[0] <= 0.5 && scales[1] <= 0.5 &&
                isPyramid()) {
            imageIndex = reductionFactor.factor;
        }

        Image image           = images.get(imageIndex);
        Region physicalRegion = orientedRegion.oriented(
                image.getSize(), image.getOrientation());
        decoderHints.add(DecoderHint.ALREADY_ORIENTED);

        physicalRegion = physicalRegion.scaled(reductionFactor.getScale());
        orientedRegion = orientedRegion.scaled(reductionFactor.getScale());
        diffScales[0]  = reductionFactor.findDifferentialScale(scales[0]);
        diffScales[1]  = reductionFactor.findDifferentialScale(scales[1]);
        if (diffScales[0] != 1 || diffScales[1] != 1) {
            decoderHints.add(DecoderHint.NEEDS_DIFFERENTIAL_SCALE);
        }

        physicalRegion = physicalRegion.clippedTo(image.getSize());
        if (Orientation.ROTATE_90.equals(image.getOrientation()) || Orientation.ROTATE_270.equals(image.getOrientation())) {
            orientedRegion = orientedRegion.clippedTo(image.getSize().inverted());
        } else {
            orientedRegion = orientedRegion.clippedTo(image.getSize());
        }

        BufferedImage bufferedImage;
        if (image.isTiled()) {
            bufferedImage = readTiledRegion(
                    imageIndex, physicalRegion, orientedRegion);
        } else {
            bufferedImage = readStripedRegion(
                    imageIndex, physicalRegion, orientedRegion);
        }
        LOGGER.trace("Read region {},{}/{}x{} from IFD {} into {}x{} image ({}x reduction factor) in {}",
                orientedRegion.intX(), orientedRegion.intY(),
                orientedRegion.intWidth(), orientedRegion.intHeight(),
                imageIndex,
                bufferedImage.getWidth(), bufferedImage.getHeight(),
                reductionFactor, watch);
        return bufferedImage;
    }

    @Override
    public Metadata readMetadata(int imageIndex) throws IOException {
        validateImageIndex(imageIndex);
        Image image = images.get(imageIndex);
        if (image.ifd == null) {
            DirectoryReader reader = new DirectoryReader();
            reader.setSource(inputStream);
            TagSet tagSet = new EXIFBaselineTIFFTagSet();
            tagSet.addTag(TIFFMetadata.IPTC_POINTER_TAG);
            tagSet.addTag(TIFFMetadata.XMP_POINTER_TAG);
            reader.addTagSet(tagSet);
            reader.addTagSet(new EXIFTagSet());
            reader.addTagSet(new EXIFGPSTagSet());
            reader.addTagSet(new EXIFInteroperabilityTagSet());
            inputStream.seek(0);
            List<Directory> exifIFDs = reader.readAll();
            image.ifd = exifIFDs.get(imageIndex);
        }
        return new TIFFMetadata(image.ifd);
    }

    //endregion
    //region Private methods

    private void openFile() throws IOException {
        if (tiff != null) {
            return;
        }
        if (imageFile != null && inputStream == null) {
            inputStream     = new PathImageInputStream(imageFile);
            ownsInputStream = true;
        } else if (imageFile == null && inputStream == null) {
            throw new IOException("Source not set");
        }
        // r: read-only
        // m: no memory mapping (should speed up access)
        // O: on-demand strip/tile offset/byte-count loading
        MemorySegment mode               = arena.allocateFrom("rmO");
        MemorySegment options            = TIFFOpenOptionsAlloc();
        MemorySegment userData           = MemorySegment.NULL;
        MemorySegment errorHandlerFunc   = Linker.nativeLinker().upcallStub(
                ERROR_HANDLER_FUNCTION,
                ERROR_HANDLER_FUNCTION_DESCRIPTOR,
                arena);
        MemorySegment warningHandlerFunc = Linker.nativeLinker().upcallStub(
                WARNING_HANDLER_FUNCTION,
                WARNING_HANDLER_FUNCTION_DESCRIPTOR,
                arena);
        TIFFOpenOptionsSetErrorHandlerExtR(options, errorHandlerFunc, userData);
        TIFFOpenOptionsSetWarningHandlerExtR(options, warningHandlerFunc, userData);

        if (imageFile != null) {
            MemorySegment pathname = arena.allocateFrom(imageFile.toString());
            tiff = TIFFOpenExt(pathname, mode, options);
        } else {
            MemorySegment filename   = arena.allocateFrom("irrelevant.tif");
            MemorySegment clientData = arena.allocateFrom(
                    ValueLayout.JAVA_LONG, Thread.currentThread().threadId());
            MemorySegment readFunc = Linker.nativeLinker().upcallStub(
                    StreamFunctions.READ_FUNCTION,
                    StreamFunctions.READ_FUNCTION_DESCRIPTOR,
                    arena);
            MemorySegment writeFunc = Linker.nativeLinker().upcallStub(
                    StreamFunctions.WRITE_FUNCTION,
                    StreamFunctions.WRITE_FUNCTION_DESCRIPTOR,
                    arena);
            MemorySegment seekFunc = Linker.nativeLinker().upcallStub(
                    StreamFunctions.SEEK_FUNCTION,
                    StreamFunctions.SEEK_FUNCTION_DESCRIPTOR,
                    arena);
            MemorySegment closeFunc = Linker.nativeLinker().upcallStub(
                    StreamFunctions.CLOSE_FUNCTION,
                    StreamFunctions.CLOSE_FUNCTION_DESCRIPTOR,
                    arena);
            MemorySegment sizeFunc = Linker.nativeLinker().upcallStub(
                    StreamFunctions.SIZE_FUNCTION,
                    StreamFunctions.SIZE_FUNCTION_DESCRIPTOR,
                    arena);
            // TIFFClientOpen() seems to be OK with these being null, but not
            // any of the other ones.
            MemorySegment mapFileFunc   = MemorySegment.NULL;
            MemorySegment unmapFileFunc = MemorySegment.NULL;
            tiff = TIFFClientOpenExt(filename, mode, clientData, readFunc,
                    writeFunc, seekFunc, closeFunc, sizeFunc, mapFileFunc,
                    unmapFileFunc, options);
        }
        try {
            if (tiff.address() == 0) {
                throw new SourceFormatException();
            }
        } finally {
            TIFFOpenOptionsFree(options);
        }
    }

    /**
     * Iterates through all top-level IFDs and stores {@link Image}
     * representations of them in {@link #images}.
     */
    private void readIFDs() throws IOException {
        if (!images.isEmpty()) {
            return;
        }
        openFile();
        do {
            final Image image = new Image();
            images.add(image);
            { // Read width & length
                MemorySegment widthPtr  = arena.allocate(C_POINTER);
                MemorySegment lengthPtr = arena.allocate(C_POINTER);
                TIFFGetField.makeInvoker(C_POINTER)
                        .apply(tiff, TIFFTAG_IMAGEWIDTH(), widthPtr);
                TIFFGetField.makeInvoker(C_POINTER)
                        .apply(tiff, TIFFTAG_IMAGELENGTH(), lengthPtr);
                image.width  = widthPtr.get(ValueLayout.OfInt.JAVA_INT, 0);
                image.length = lengthPtr.get(ValueLayout.OfInt.JAVA_INT, 0);
            }
            { // Read tile width & length
                MemorySegment widthPtr  = arena.allocate(C_POINTER);
                MemorySegment lengthPtr = arena.allocate(C_POINTER);
                int status = TIFFGetField.makeInvoker(C_POINTER)
                        .apply(tiff, TIFFTAG_TILEWIDTH(), widthPtr);
                if (status == 1) {
                    TIFFGetField.makeInvoker(C_POINTER)
                            .apply(tiff, TIFFTAG_TILELENGTH(), lengthPtr);
                    image.tileWidth = widthPtr.get(ValueLayout.OfInt.JAVA_INT, 0);
                    image.tileLength = lengthPtr.get(ValueLayout.OfInt.JAVA_INT, 0);
                }
            }
            { // Read other stuff
                // Samples per pixel
                MemorySegment ptr = arena.allocate(C_POINTER);
                TIFFGetField.makeInvoker(C_POINTER)
                        .apply(tiff, TIFFTAG_SAMPLESPERPIXEL(), ptr);
                image.samplesPerPixel = ptr.get(ValueLayout.OfInt.JAVA_INT, 0);
                // Rows per strip
                if (!image.isTiled()) {
                    TIFFGetField.makeInvoker(C_POINTER)
                            .apply(tiff, TIFFTAG_ROWSPERSTRIP(), ptr);
                    image.rowsPerStrip = ptr.get(ValueLayout.OfInt.JAVA_INT, 0);
                }
                // Orientation
                TIFFGetField.makeInvoker(C_POINTER)
                        .apply(tiff, TIFFTAG_ORIENTATION(), ptr);
                image.orientation = ptr.get(ValueLayout.OfInt.JAVA_INT, 0);
            }
        } while (TIFFReadDirectory(tiff) == 1);
    }

    private boolean isPyramid() throws IOException {
        readIFDs();
        List<Size> sizes = images.stream().map(Image::getSize).toList();
        return Size.isPyramid(sizes);
    }

    private void validateImageIndex(int index) throws IOException {
        if (index < 0 || index >= getNumImages()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private BufferedImage readStripedRegion(int imageIndex,
                                            Region physicalRegion,
                                            Region orientedRegion) throws SourceFormatException {
        TIFFSetDirectory(tiff, imageIndex);
        final Image image                 = images.get(imageIndex);
        final BufferedImage bufferedImage = newBufferedImage(
                orientedRegion.intWidth(), orientedRegion.intHeight(), 4);
        final WritableRaster raster       = bufferedImage.getRaster();
        final int regionY                 = physicalRegion.intY();
        final int regionH                 = physicalRegion.intHeight();
        // Find the region on the strip grid that lies underneath the ROI in
        // order to avoid reading irrelevant strips.
        final int stripGridMinY           = Math.max(regionY - (regionY % image.rowsPerStrip), 0);
        final int stripGridMaxY           = Math.min(regionY + regionH, image.length);
        final int bufSize                 = image.width * image.rowsPerStrip * 4;

        for (int stripY = stripGridMinY; stripY < stripGridMaxY; stripY += image.rowsPerStrip) {
            MemorySegment stripBuf = arena.allocate(bufSize);
            int result = TIFFReadRGBAStrip(tiff, stripY, stripBuf);
            if (result != 1) {
                System.err.println("TIFFReadRGBAStrip() returned " + result);
            }
            byte[] stripBytes = stripBuf.asSlice(0, bufSize)
                    .toArray(ValueLayout.JAVA_BYTE);
            switch (image.getOrientation()) {
                case ROTATE_0 ->   fill0(image, physicalRegion, raster, stripY, stripBytes);
                case ROTATE_90 ->  fill90(image, physicalRegion, raster, stripY, stripBytes);
                case ROTATE_180 -> fill180(image, physicalRegion, raster, stripY, stripBytes);
                case ROTATE_270 -> fill270(image, physicalRegion, raster, stripY, stripBytes);
            }
        }
        return bufferedImage;
    }

    private static void fill0(Image image, Region region,
                              WritableRaster raster,
                              int stripY, byte[] stripBytes) {
        final int regionX = region.intX();
        final int regionY = region.intY();
        final int regionW = region.intWidth();
        final int regionH = region.intHeight();
        final int maxY    = Math.min(stripY + image.rowsPerStrip, image.length);
        int offset        = 0;
        for (int y = maxY - 1; y >= stripY; y--) {
            for (int x = 0; x < image.width; x++) {
                if (x >= regionX && x < regionX + regionW &&
                        y >= regionY && y < regionY + regionH) {
                    int rasterX = x - regionX;
                    int rasterY = y - regionY;
                    raster.setSample(rasterX, rasterY, 0, (stripBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (stripBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (stripBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (stripBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill90(Image image, Region physicalRegion,
                               WritableRaster raster,
                               int stripY, byte[] stripBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxY    = Math.min(stripY + image.rowsPerStrip, image.length);
        int offset        = 0;
        for (int y = maxY - 1; y >= stripY; y--) {
            for (int x = 0; x < image.width; x++) {
                int px = image.width - 1 - x;
                int py = y;
                if (px >= regionX && px < regionX + regionW &&
                        py >= regionY && py < regionY + regionH) {
                    int rasterX = regionH - 1 - (py - regionY);
                    int rasterY = px - regionX;
                    raster.setSample(rasterX, rasterY, 0, (stripBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (stripBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (stripBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (stripBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill180(Image image, Region physicalRegion,
                                WritableRaster raster,
                                int stripY, byte[] stripBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxY    = Math.min(stripY + image.rowsPerStrip, image.length);
        int offset        = 0;
        for (int y = stripY; y < maxY; y++) {
            for (int x = 0; x < image.width; x++) {
                int px = image.width - x - 1;
                int py = y;
                if (px >= regionX && px < regionX + regionW &&
                        py >= regionY && py < regionY + regionH) {
                    int rasterX = regionW - 1 - (px - regionX);
                    int rasterY = regionH - 1 - (py - regionY);
                    raster.setSample(rasterX, rasterY, 0, (stripBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (stripBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (stripBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (stripBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill270(Image image, Region physicalRegion,
                                WritableRaster raster,
                                int stripY, byte[] stripBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxY    = Math.min(stripY + image.rowsPerStrip, image.length);
        int offset        = 0;
        for (int y = stripY; y < maxY; y++) {
            for (int x = 0; x < image.width; x++) {
                if (x >= regionX && x < regionX + regionW &&
                        y >= regionY && y < regionY + regionH) {
                    int rasterX = y - regionY;
                    int rasterY = regionW - 1 - (x - regionX);
                    raster.setSample(rasterX, rasterY, 0, (stripBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (stripBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (stripBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (stripBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private BufferedImage readTiledRegion(int imageIndex,
                                          Region physicalRegion,
                                          Region orientedRegion) throws SourceFormatException {
        TIFFSetDirectory(tiff, imageIndex);
        final Image image                 = images.get(imageIndex);
        final BufferedImage bufferedImage = newBufferedImage(
                orientedRegion.intWidth(), orientedRegion.intHeight(), 4);
        final WritableRaster raster       = bufferedImage.getRaster();
        final int regionX                 = physicalRegion.intX();
        final int regionY                 = physicalRegion.intY();
        final int regionW                 = physicalRegion.intWidth();
        final int regionH                 = physicalRegion.intHeight();
        // We will be reading whole tiles, so we need to find the region on
        // the tile grid that lies underneath the ROI.
        final int tileGridMinX            = Math.max(regionX - (regionX % image.tileWidth), 0);
        final int tileGridMinY            = Math.max(regionY - (regionY % image.tileLength), 0);
        final int tileGridMaxX            = Math.min(regionX + regionW, image.width);
        final int tileGridMaxY            = Math.min(regionY + regionH, image.length);
        final int bufSize                 = image.tileWidth * image.tileLength * 4;
        // Loop through all tile origins.
        for (int tileY = tileGridMinY; tileY < tileGridMaxY; tileY += image.tileLength) {
            for (int tileX = tileGridMinX; tileX < tileGridMaxX; tileX += image.tileWidth) {
                MemorySegment tileBuf = arena.allocate(bufSize);
                int result = TIFFReadRGBATile(tiff, tileX, tileY, tileBuf);
                if (result != 1) {
                    System.err.println("TIFFReadRGBATile() returned " + result);
                }
                byte[] tileBytes = tileBuf.asSlice(0, bufSize)
                        .toArray(ValueLayout.JAVA_BYTE);
                switch (image.getOrientation()) {
                    case ROTATE_0 ->   fill0(image, physicalRegion, raster, tileX, tileY, tileBytes);
                    case ROTATE_90 ->  fill90(image, physicalRegion, raster, tileX, tileY, tileBytes);
                    case ROTATE_180 -> fill180(image, physicalRegion, raster, tileX, tileY, tileBytes);
                    case ROTATE_270 -> fill270(image, physicalRegion, raster, tileX, tileY, tileBytes);
                }
            }
        }
        return bufferedImage;
    }

    private static void fill0(Image image, Region physicalRegion,
                               WritableRaster raster,
                               int tileX, int tileY, byte[] tileBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxX    = tileX + image.tileWidth;
        final int maxY    = tileY + image.tileLength;
        int offset        = 0;
        for (int y = maxY - 1; y >= tileY; y--) {
            for (int x = tileX; x < maxX; x++) {
                if (x >= regionX && x < regionX + regionW &&
                        y >= regionY && y < regionY + regionH) {
                    int rasterX = x - regionX;
                    int rasterY = y - regionY;
                    raster.setSample(rasterX, rasterY, 0, (tileBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (tileBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (tileBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (tileBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill90(Image image, Region physicalRegion,
                               WritableRaster raster,
                               int tileX, int tileY, byte[] tileBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxX    = tileX + image.tileWidth;
        final int maxY    = tileY + image.tileLength;
        final int excessW = image.width % image.tileWidth;
        int offset        = 0;
        for (int y = maxY - 1; y >= tileY; y--) {
            for (int x = maxX - 1; x >= tileX; x--) {
                int px = x;
                int py = y;
                if (px >= image.width - excessW) {
                    px -= image.tileWidth - excessW;
                    if (tileBytes[offset + 3] == 0x00) {
                        offset += 4;
                        continue;
                    }
                }
                if (px >= regionX && px < regionX + regionW &&
                        py >= regionY && py < regionY + regionH) {
                    int rasterX = regionH - 1 - (py - regionY);
                    int rasterY = px - regionX;
                    raster.setSample(rasterX, rasterY, 0, (tileBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (tileBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (tileBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (tileBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill180(Image image, Region physicalRegion,
                                WritableRaster raster,
                                int tileX, int tileY, byte[] tileBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxX    = tileX + image.tileWidth;
        final int maxY    = tileY + image.tileLength;
        final int excessW = image.width % image.tileWidth;
        final int excessH = image.length % image.tileLength;
        int offset        = 0;
        for (int y = tileY; y < maxY; y++) {
            for (int x = maxX - 1; x >= tileX; x--) {
                int px = x;
                int py = y;
                if (px >= image.width - excessW) {
                    px -= image.tileWidth - excessW;
                    if (tileBytes[offset + 3] == 0x00) {
                        offset += 4;
                        continue;
                    }
                }
                if (py >= image.length - excessH) {
                    py -= image.tileLength - excessH;
                    if (tileBytes[offset + 3] == 0x00) {
                        offset += 4;
                        continue;
                    }
                }
                if (px >= regionX && px < regionX + regionW &&
                        py >= regionY && py < regionY + regionH) {
                    int rasterX = regionW - 1 - (px - regionX);
                    int rasterY = regionH - 1 - (py - regionY);
                    raster.setSample(rasterX, rasterY, 0, (tileBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (tileBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (tileBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (tileBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static void fill270(Image image, Region physicalRegion,
                                WritableRaster raster,
                                int tileX, int tileY, byte[] tileBytes) {
        final int regionX = physicalRegion.intX();
        final int regionY = physicalRegion.intY();
        final int regionW = physicalRegion.intWidth();
        final int regionH = physicalRegion.intHeight();
        final int maxX    = tileX + image.tileWidth;
        final int maxY    = tileY + image.tileLength;
        final int excessH = image.length % image.tileLength;
        int offset        = 0;
        for (int y = tileY; y < maxY; y++) {
            for (int x = tileX; x < maxX; x++) {
                int px = x;
                int py = y;
                if (py >= image.length - image.tileLength + excessH) {
                    py -= image.tileLength - excessH;
                }
                if (px >= regionX && px < regionX + regionW &&
                        py >= regionY && py < regionY + regionH) {
                    int rasterX = py - regionY;
                    int rasterY = regionW - 1 - (px - regionX);
                    raster.setSample(rasterX, rasterY, 0, (tileBytes[offset] & 0xff));
                    raster.setSample(rasterX, rasterY, 1, (tileBytes[offset + 1] & 0xff));
                    raster.setSample(rasterX, rasterY, 2, (tileBytes[offset + 2] & 0xff));
                    raster.setSample(rasterX, rasterY, 3, (tileBytes[offset + 3] & 0xff));
                }
                offset += 4;
            }
        }
    }

    private static BufferedImage newBufferedImage(int width,
                                                  int height,
                                                  int numBands) throws SourceFormatException {
        return switch (numBands) {
            case 1  -> new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            case 3  -> new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            case 4  -> new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            default -> throw new SourceFormatException("Image has " + numBands + " bands");
        };
    }

}