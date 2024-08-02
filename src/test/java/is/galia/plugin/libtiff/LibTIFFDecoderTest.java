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
import is.galia.codec.DecoderHint;
import is.galia.codec.SourceFormatException;
import is.galia.image.Size;
import is.galia.image.Format;
import is.galia.image.Metadata;
import is.galia.image.Region;
import is.galia.image.ReductionFactor;
import is.galia.plugin.libtiff.test.TestUtils;
import is.galia.stream.PathImageInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LibTIFFDecoderTest {

    private static final double DELTA = 0.00000001;
    private static final boolean SAVE_IMAGES = true;

    private final Arena arena = Arena.ofConfined();
    private LibTIFFDecoder instance;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(Application.ENVIRONMENT_VM_ARGUMENT,
                Application.TEST_ENVIRONMENT);
        try (LibTIFFDecoder decoder = new LibTIFFDecoder()) {
            decoder.onApplicationStart();
        }
    }

    @BeforeEach
    void setUp() {
        instance = new LibTIFFDecoder();
        instance.setArena(arena);
        instance.initializePlugin();
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-uncompressed.tif"));
    }

    @AfterEach
    void tearDown() {
        instance.close();
        arena.close();
    }

    //region Plugin methods

    @Test
    void getPluginConfigKeys() {
        Set<String> keys = instance.getPluginConfigKeys();
        assertTrue(keys.isEmpty());
    }

    @Test
    void getPluginName() {
        assertEquals(LibTIFFDecoder.class.getSimpleName(),
                instance.getPluginName());
    }

    //endregion
    //region Decoder methods

    /* detectFormat() */

    @Test
    public void detectFormatWithIncompatibleImage() throws Exception {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertEquals(Format.UNKNOWN, instance.detectFormat());
    }

    @Test
    void detectFormat() throws Exception {
        assertEquals(LibTIFFDecoder.FORMAT, instance.detectFormat());
    }

    /* getNumImages(int) */

    @Test
    void getNumImagesWithIncompatibleImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class,
                () -> instance.getNumImages());
    }

    @Test
    void getNumImagesWithSingleImage() throws Exception {
        assertEquals(1, instance.getNumImages());
    }

    @Test
    void getNumImagesWithMultipleImages() throws Exception {
        instance.setSource(TestUtils.getFixture("multipage.tif"));
        assertEquals(9, instance.getNumImages());
    }

    @Test
    void getNumImagesWithPyramid() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        assertEquals(4, instance.getNumImages());
    }

    /* getNumResolutions() */

    @Test
    void getNumResolutionsWithIncompatibleImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class,
                () -> instance.getNumResolutions());
    }

    @Test
    void getNumResolutionsWithSingleResolution() throws Exception {
        assertEquals(1, instance.getNumResolutions());
    }

    @Test
    void getNumResolutionsWithMultiplePages() throws Exception {
        instance.setSource(TestUtils.getFixture("multipage.tif"));
        assertEquals(1, instance.getNumResolutions());
    }

    @Test
    void getNumResolutionsWithPyramid() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        assertEquals(4, instance.getNumResolutions());
    }

    /* getSize(int) */

    @Test
    void getSizeWithIncompatibleImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class, () -> instance.getSize(0));
    }

    @Test
    void getSizeWithIllegalImageIndex() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> instance.getSize(1));
    }

    @Test
    void getSize() throws Exception {
        Size size = instance.getSize(0);
        assertEquals(64, size.intWidth());
        assertEquals(56, size.intHeight());
    }

    @Test
    void getSizeWithNonzeroImageIndex() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        Size size = instance.getSize(1);
        assertEquals(744, size.intWidth());
        assertEquals(495, size.intHeight());
    }

    /* getTileSize(int) */

    @Test
    void getTileSizeWithIncompatibleImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class,
                () -> instance.getTileSize(0));
    }

    @Test
    void getTileSizeWithIllegalImageIndex() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> instance.getTileSize(1));
    }

    @Test
    void getTileSizeWithNonTiledImage() throws Exception {
        assertEquals(instance.getSize(0), instance.getTileSize(0));
    }

    @Test
    void getTileSizeWithTiledImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-uncompressed.tif"));
        Size size = instance.getTileSize(0);
        assertEquals(16, size.intWidth());
        assertEquals(16, size.intHeight());
    }

    @Test
    void getTileSizeWithNonzeroImageIndex() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        Size size = instance.getTileSize(1);
        assertEquals(32, size.intWidth());
        assertEquals(32, size.intHeight());
    }

    /* read(int) */

    @Test
    void decode1WithIncompatibleImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class, () -> instance.decode(0));
    }

    @Test
    void decode1WithIllegalImageIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> instance.decode(1));
    }

    @Test
    void decode1FromFile() throws Exception {
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1FromStream() throws Exception {
        Path fixture = TestUtils.getFixture("tiled-rgb-8bit-jpeg.tif");
        instance.setSource(new PathImageInputStream(fixture));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithBigTIFFImage() throws Exception {
        instance.setSource(TestUtils.getFixture("bigtiff.tf8"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1With2BitImage() throws Exception {
        instance.setSource(TestUtils.getFixture("2bit.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(4, image.getSampleModel().getNumBands());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithCMYKImage() throws Exception {
        instance.setSource(TestUtils.getFixture("cmyk.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(4, image.getSampleModel().getNumBands());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB8BitJPEGCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-jpeg.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB8BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB8BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB8BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB8BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-8bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB16BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-16bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB16BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-16bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB16BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-16bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGB16BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgb-16bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA8BitJPEGCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-8bit-jpeg.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA8BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-8bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA8BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-8bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA8BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-8bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA8BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-8bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA16BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-16bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA16BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-16bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA16BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-16bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithStripedRGBA16BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-rgba-16bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTileSizeLargerThanImageSize() throws Exception {
        instance.setSource(TestUtils.getFixture("tile-larger-than-image.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(69, image.getWidth());
        assertEquals(54, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB8BitJPEGCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-jpeg.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB8BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB8BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB8BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB8BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB16BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-16bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB16BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-16bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB16BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-16bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGB16BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-16bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA8BitJPEGCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-8bit-jpeg.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA8BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-8bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA8BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-8bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA8BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-8bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA8BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-8bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA16BitLZWCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-16bit-lzw.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA16BitPackBitsCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-16bit-packbits.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA16BitUncompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-16bit-uncompressed.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode1WithTiledRGBA16BitZipCompressedImage() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgba-16bit-zip.tif"));
        BufferedImage image = instance.decode(0);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    /* read(int, ...) */

    @Test
    void decode2WithIllegalImageIndex() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                instance.decode(1, null, null, null, null, null));
    }

    @Test
    void decode2WithStripedImageWithOrientation0() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-0.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithOrientation90() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-90.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithOrientation180() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-180.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithOrientation270() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-270.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithFullRegion() throws Exception {
        Region region                   = new Region(0, 0, 100, 88);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(64, image.getWidth());
        assertEquals(56, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithRegionWithOrientation0() throws Exception {
        Region region                   = new Region(38, 41, 6, 9); // bottom center lego man
        double[] scales                 = { 1, 1 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(6, image.getWidth());
        assertEquals(9, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithRegionWithOrientation90() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-90.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithRegionWithOrientation180() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-180.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithStripedImageWithRegionWithOrientation270() throws Exception {
        instance.setSource(TestUtils.getFixture("striped-orientation-270.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithOrientation0() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-0.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithOrientation90() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-90.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithOrientation180() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-180.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithOrientation270() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-270.tif"));
        Region region                   = new Region(0, 0, 150, 200, true);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(150, image.getWidth());
        assertEquals(200, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTileSizeLargerThanImageSize() throws Exception {
        instance.setSource(TestUtils.getFixture("tile-larger-than-image.tif"));
        Region region                   = new Region(0, 0, 69, 54);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(69, image.getWidth());
        assertEquals(54, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithRegionWithOrientation0() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-rgb-8bit-uncompressed.tif"));
        Region region                   = new Region(38, 41, 6, 9); // bottom center lego man
        double[] scales                 = { 1, 1 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(6, image.getWidth());
        assertEquals(9, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithRegionWithOrientation90() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-90.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithRegionWithOrientation180() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-180.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithTiledImageWithRegionWithOrientation270() throws Exception {
        instance.setSource(TestUtils.getFixture("tiled-orientation-270.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithNonSquareTiledImageWithRegionWithOrientation0() throws Exception {
        instance.setSource(TestUtils.getFixture("nonsquare-tiles-orientation-0.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithNonSquareTiledImageWithRegionWithOrientation90() throws Exception {
        instance.setSource(TestUtils.getFixture("nonsquare-tiles-orientation-90.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithNonSquareTiledImageWithRegionWithOrientation180() throws Exception {
        instance.setSource(TestUtils.getFixture("nonsquare-tiles-orientation-180.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithNonSquareTiledImageWithRegionWithOrientation270() throws Exception {
        instance.setSource(TestUtils.getFixture("nonsquare-tiles-orientation-270.tif"));
        Region region                   = new Region(90, 116, 18, 30, false);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(18, image.getWidth());
        assertEquals(30, image.getHeight());
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithPyramidalImageWithRegion() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        Region region                   = new Region(0, 0, 800, 600);
        double[] scales                 = { 0.2, 0.2 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(200, image.getWidth());
        assertEquals(150, image.getHeight());
        assertEquals(2, reductionFactor.factor);
        assertEquals(0.8, diffScales[0], DELTA);
        assertEquals(0.8, diffScales[1], DELTA);
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2WithPyramidalImageWithScale() throws Exception {
        instance.setSource(TestUtils.getFixture("pyramid.tif"));
        Region region                   = new Region(0, 0, 1488, 991);
        double[] scales                 = { 0.2, 0.2 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        BufferedImage image = instance.decode(0, region, scales, reductionFactor,
                diffScales, decoderHints);
        assertEquals(372, image.getWidth());
        assertEquals(247, image.getHeight());
        assertEquals(2, reductionFactor.factor);
        assertEquals(0.8, diffScales[0], DELTA);
        assertEquals(0.8, diffScales[1], DELTA);
        if (SAVE_IMAGES) TestUtils.save(image);
    }

    @Test
    void decode2PopulatesDecoderHints() throws Exception {
        Region region                   = new Region(0, 0, 32, 28);
        double[] scales                 = { 1, 1 };
        double[] diffScales             = new double[] { 1, 1 };
        ReductionFactor reductionFactor = new ReductionFactor();
        Set<DecoderHint> decoderHints   = EnumSet.noneOf(DecoderHint.class);

        instance.decode(0, region, scales, reductionFactor, diffScales,
                decoderHints);
        assertTrue(decoderHints.contains(DecoderHint.ALREADY_ORIENTED));
        assertFalse(decoderHints.contains(DecoderHint.IGNORED_REGION));
        assertFalse(decoderHints.contains(DecoderHint.IGNORED_SCALE));
    }

    /* readMetadata() */

    @Test
    void decodeMetadataWithWithInvalidImage() {
        instance.setSource(TestUtils.getFixture("alpha.png"));
        assertThrows(SourceFormatException.class,
                () -> instance.readMetadata(0));
    }

    @Test
    void decodeMetadataWithIllegalImageIndex() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> instance.readMetadata(9999));
    }

    @Test
    void decodeMetadataWithEXIF() throws Exception {
        instance.setSource(TestUtils.getFixture("exif.tif"));
        Metadata metadata = instance.readMetadata(0);
        assertFalse(metadata.getEXIF().isEmpty());
    }

    @Test
    void decodeMetadataWithIPTC() throws Exception {
        instance.setSource(TestUtils.getFixture("iptc.tif"));
        Metadata metadata = instance.readMetadata(0);
        assertFalse(metadata.getIPTC().isEmpty());
    }

    @Test
    void decodeMetadataWithXMP() throws Exception {
        instance.setSource(TestUtils.getFixture("xmp.tif"));
        Metadata metadata = instance.readMetadata(0);
        assertTrue(metadata.getXMP().isPresent());
    }

    /* readSequence() */

    @Test
    void decodeSequence() {
        assertThrows(UnsupportedOperationException.class,
                () -> instance.decodeSequence());
    }

}
