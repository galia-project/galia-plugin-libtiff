// Generated by jextract

package org.libtiff;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * typedef void (*tileSeparateRoutine)(TIFFRGBAImage *, uint32_t *, uint32_t, uint32_t, uint32_t, uint32_t, int32_t, int32_t, unsigned char *, unsigned char *, unsigned char *, unsigned char *)
 * }
 */
public class tileSeparateRoutine {

    tileSeparateRoutine() {
        // Should not be called directly
    }

    /**
     * The function pointer signature, expressed as a functional interface
     */
    public interface Function {
        void apply(MemorySegment _x0, MemorySegment _x1, int _x2, int _x3, int _x4, int _x5, int _x6, int _x7, MemorySegment _x8, MemorySegment _x9, MemorySegment _x10, MemorySegment _x11);
    }

    private static final FunctionDescriptor $DESC = FunctionDescriptor.ofVoid(
        tiffio_h.C_POINTER,
        tiffio_h.C_POINTER,
        tiffio_h.C_INT,
        tiffio_h.C_INT,
        tiffio_h.C_INT,
        tiffio_h.C_INT,
        tiffio_h.C_INT,
        tiffio_h.C_INT,
        tiffio_h.C_POINTER,
        tiffio_h.C_POINTER,
        tiffio_h.C_POINTER,
        tiffio_h.C_POINTER
    );

    /**
     * The descriptor of this function pointer
     */
    public static FunctionDescriptor descriptor() {
        return $DESC;
    }

    private static final MethodHandle UP$MH = tiffio_h.upcallHandle(tileSeparateRoutine.Function.class, "apply", $DESC);

    /**
     * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
     * The lifetime of the returned segment is managed by {@code arena}
     */
    public static MemorySegment allocate(tileSeparateRoutine.Function fi, Arena arena) {
        return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
    }

    private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

    /**
     * Invoke the upcall stub {@code funcPtr}, with given parameters
     */
    public static void invoke(MemorySegment funcPtr,MemorySegment _x0, MemorySegment _x1, int _x2, int _x3, int _x4, int _x5, int _x6, int _x7, MemorySegment _x8, MemorySegment _x9, MemorySegment _x10, MemorySegment _x11) {
        try {
             DOWN$MH.invokeExact(funcPtr, _x0, _x1, _x2, _x3, _x4, _x5, _x6, _x7, _x8, _x9, _x10, _x11);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}

