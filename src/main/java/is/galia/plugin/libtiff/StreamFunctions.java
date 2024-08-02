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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static org.libtiff.tiff_h_1.C_INT;
import static org.libtiff.tiff_h_1.C_LONG_LONG;
import static org.libtiff.tiff_h_1.C_POINTER;
import static org.libtiff.tiffio_h_1.SEEK_CUR;
import static org.libtiff.tiffio_h_1.SEEK_END;
import static org.libtiff.tiffio_h_1.SEEK_SET;

/**
 * Contains static I/O functions to be used with {@link
 * org.libtiff.tiffio_h.TIFFClientOpenExt}.
 */
final class StreamFunctions {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StreamFunctions.class);

    static MethodHandle CLOSE_FUNCTION, READ_FUNCTION, SEEK_FUNCTION,
            SIZE_FUNCTION, WRITE_FUNCTION;
    static final FunctionDescriptor CLOSE_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_INT, C_POINTER);
    static final FunctionDescriptor READ_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG_LONG);
    static final FunctionDescriptor SEEK_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_LONG_LONG, C_INT);
    static final FunctionDescriptor SIZE_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_LONG_LONG, C_POINTER);
    static final FunctionDescriptor WRITE_FUNCTION_DESCRIPTOR =
            FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG_LONG);

    private static int SEEK_SET;
    private static int SEEK_CUR;
    private static int SEEK_END;

    static void initializeClass() {
        SEEK_SET = SEEK_SET();
        SEEK_CUR = SEEK_CUR();
        SEEK_END = SEEK_END();
        try {
            CLOSE_FUNCTION = MethodHandles.lookup().findStatic(
                    StreamFunctions.class, "close",
                    CLOSE_FUNCTION_DESCRIPTOR.toMethodType());
            READ_FUNCTION = MethodHandles.lookup().findStatic(
                    StreamFunctions.class, "read",
                    READ_FUNCTION_DESCRIPTOR.toMethodType());
            SEEK_FUNCTION = MethodHandles.lookup().findStatic(
                    StreamFunctions.class, "seek",
                    SEEK_FUNCTION_DESCRIPTOR.toMethodType());
            SIZE_FUNCTION = MethodHandles.lookup().findStatic(
                    StreamFunctions.class, "size",
                    SIZE_FUNCTION_DESCRIPTOR.toMethodType());
            WRITE_FUNCTION = MethodHandles.lookup().findStatic(
                    StreamFunctions.class, "write",
                    WRITE_FUNCTION_DESCRIPTOR.toMethodType());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
    }

    static int read(MemorySegment handle, MemorySegment buffer, long size) {
        ImageInputStream is = fetchInputStream(handle);
        byte[] bytes = new byte[(int) size];
        try {
            is.readFully(bytes);
            for (int i = 0; i < bytes.length; i++) {
                buffer.set(ValueLayout.JAVA_BYTE, i, bytes[i]);
            }
            return bytes.length;
        } catch (IOException e) {
            LOGGER.error("read(): {}", e.getMessage());
            return -1;
        }
    }

    static int write(MemorySegment handle, MemorySegment buffer, long size) {
        return 0;
    }

    static int close(MemorySegment handle) {
        return 0;
    }

    static long seek(MemorySegment handle, long pos, int whence) {
        ImageInputStream inputStream = fetchInputStream(handle);
        try {
            final long currentPos = inputStream.getStreamPosition();
            final long length     = inputStream.length();
            if (length < 0) {
                throw new IOException("Seeking requires an " +
                        ImageInputStream.class.getSimpleName() +
                        " implementation whose length() method returns a " +
                        "positive value.");
            }
            long newPos;
            if (whence == SEEK_SET) {  // seek from the beginning
                newPos = pos;
            } else if (whence == SEEK_CUR) { // seek from the current position
                newPos = currentPos + pos;
            } else if (whence == SEEK_END) { // seek from the end
                newPos = length - pos;
            } else {
                LOGGER.warn("seek(): unexpected whence value: {}", whence);
                return -1;
            }
            inputStream.seek(newPos);
            return newPos;
        } catch (IOException e) {
            LOGGER.error("seek(): {}", e.getMessage());
            return -1;
        }
    }

    static long size(MemorySegment handle) {
        ImageInputStream is = fetchInputStream(handle);
        try {
            return is.length();
        } catch (IOException e) {
            LOGGER.error("size(): {}", e.getMessage());
            return -1;
        }
    }

    private static ImageInputStream fetchInputStream(MemorySegment userData) {
        final long threadID    = userData.get(ValueLayout.JAVA_LONG, 0);
        LibTIFFDecoder decoder = LibTIFFDecoder.LIVE_INSTANCES.get(threadID);
        return decoder.getInputStream();
    }

    private StreamFunctions() {}

}
