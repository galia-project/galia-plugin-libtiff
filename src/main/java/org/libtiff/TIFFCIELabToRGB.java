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
 * struct {
 *     int range;
 *     float rstep;
 *     float gstep;
 *     float bstep;
 *     float X0;
 *     float Y0;
 *     float Z0;
 *     TIFFDisplay display;
 *     float Yr2r[1501];
 *     float Yg2g[1501];
 *     float Yb2b[1501];
 * }
 * }
 */
public class TIFFCIELabToRGB {

    TIFFCIELabToRGB() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        tiffio_h.C_INT.withName("range"),
        tiffio_h.C_FLOAT.withName("rstep"),
        tiffio_h.C_FLOAT.withName("gstep"),
        tiffio_h.C_FLOAT.withName("bstep"),
        tiffio_h.C_FLOAT.withName("X0"),
        tiffio_h.C_FLOAT.withName("Y0"),
        tiffio_h.C_FLOAT.withName("Z0"),
        TIFFDisplay.layout().withName("display"),
        MemoryLayout.sequenceLayout(1501, tiffio_h.C_FLOAT).withName("Yr2r"),
        MemoryLayout.sequenceLayout(1501, tiffio_h.C_FLOAT).withName("Yg2g"),
        MemoryLayout.sequenceLayout(1501, tiffio_h.C_FLOAT).withName("Yb2b")
    ).withName("$anon$168:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt range$LAYOUT = (OfInt)$LAYOUT.select(groupElement("range"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int range
     * }
     */
    public static final OfInt range$layout() {
        return range$LAYOUT;
    }

    private static final long range$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int range
     * }
     */
    public static final long range$offset() {
        return range$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int range
     * }
     */
    public static int range(MemorySegment struct) {
        return struct.get(range$LAYOUT, range$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int range
     * }
     */
    public static void range(MemorySegment struct, int fieldValue) {
        struct.set(range$LAYOUT, range$OFFSET, fieldValue);
    }

    private static final OfFloat rstep$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("rstep"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float rstep
     * }
     */
    public static final OfFloat rstep$layout() {
        return rstep$LAYOUT;
    }

    private static final long rstep$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float rstep
     * }
     */
    public static final long rstep$offset() {
        return rstep$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float rstep
     * }
     */
    public static float rstep(MemorySegment struct) {
        return struct.get(rstep$LAYOUT, rstep$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float rstep
     * }
     */
    public static void rstep(MemorySegment struct, float fieldValue) {
        struct.set(rstep$LAYOUT, rstep$OFFSET, fieldValue);
    }

    private static final OfFloat gstep$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("gstep"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float gstep
     * }
     */
    public static final OfFloat gstep$layout() {
        return gstep$LAYOUT;
    }

    private static final long gstep$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float gstep
     * }
     */
    public static final long gstep$offset() {
        return gstep$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float gstep
     * }
     */
    public static float gstep(MemorySegment struct) {
        return struct.get(gstep$LAYOUT, gstep$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float gstep
     * }
     */
    public static void gstep(MemorySegment struct, float fieldValue) {
        struct.set(gstep$LAYOUT, gstep$OFFSET, fieldValue);
    }

    private static final OfFloat bstep$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("bstep"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float bstep
     * }
     */
    public static final OfFloat bstep$layout() {
        return bstep$LAYOUT;
    }

    private static final long bstep$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float bstep
     * }
     */
    public static final long bstep$offset() {
        return bstep$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float bstep
     * }
     */
    public static float bstep(MemorySegment struct) {
        return struct.get(bstep$LAYOUT, bstep$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float bstep
     * }
     */
    public static void bstep(MemorySegment struct, float fieldValue) {
        struct.set(bstep$LAYOUT, bstep$OFFSET, fieldValue);
    }

    private static final OfFloat X0$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("X0"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float X0
     * }
     */
    public static final OfFloat X0$layout() {
        return X0$LAYOUT;
    }

    private static final long X0$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float X0
     * }
     */
    public static final long X0$offset() {
        return X0$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float X0
     * }
     */
    public static float X0(MemorySegment struct) {
        return struct.get(X0$LAYOUT, X0$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float X0
     * }
     */
    public static void X0(MemorySegment struct, float fieldValue) {
        struct.set(X0$LAYOUT, X0$OFFSET, fieldValue);
    }

    private static final OfFloat Y0$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("Y0"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float Y0
     * }
     */
    public static final OfFloat Y0$layout() {
        return Y0$LAYOUT;
    }

    private static final long Y0$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float Y0
     * }
     */
    public static final long Y0$offset() {
        return Y0$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float Y0
     * }
     */
    public static float Y0(MemorySegment struct) {
        return struct.get(Y0$LAYOUT, Y0$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float Y0
     * }
     */
    public static void Y0(MemorySegment struct, float fieldValue) {
        struct.set(Y0$LAYOUT, Y0$OFFSET, fieldValue);
    }

    private static final OfFloat Z0$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("Z0"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float Z0
     * }
     */
    public static final OfFloat Z0$layout() {
        return Z0$LAYOUT;
    }

    private static final long Z0$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float Z0
     * }
     */
    public static final long Z0$offset() {
        return Z0$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float Z0
     * }
     */
    public static float Z0(MemorySegment struct) {
        return struct.get(Z0$LAYOUT, Z0$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float Z0
     * }
     */
    public static void Z0(MemorySegment struct, float fieldValue) {
        struct.set(Z0$LAYOUT, Z0$OFFSET, fieldValue);
    }

    private static final GroupLayout display$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("display"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * TIFFDisplay display
     * }
     */
    public static final GroupLayout display$layout() {
        return display$LAYOUT;
    }

    private static final long display$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * TIFFDisplay display
     * }
     */
    public static final long display$offset() {
        return display$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * TIFFDisplay display
     * }
     */
    public static MemorySegment display(MemorySegment struct) {
        return struct.asSlice(display$OFFSET, display$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * TIFFDisplay display
     * }
     */
    public static void display(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, display$OFFSET, display$LAYOUT.byteSize());
    }

    private static final SequenceLayout Yr2r$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("Yr2r"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static final SequenceLayout Yr2r$layout() {
        return Yr2r$LAYOUT;
    }

    private static final long Yr2r$OFFSET = 112;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static final long Yr2r$offset() {
        return Yr2r$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static MemorySegment Yr2r(MemorySegment struct) {
        return struct.asSlice(Yr2r$OFFSET, Yr2r$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static void Yr2r(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, Yr2r$OFFSET, Yr2r$LAYOUT.byteSize());
    }

    private static long[] Yr2r$DIMS = { 1501 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static long[] Yr2r$dimensions() {
        return Yr2r$DIMS;
    }
    private static final VarHandle Yr2r$ELEM_HANDLE = Yr2r$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static float Yr2r(MemorySegment struct, long index0) {
        return (float)Yr2r$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * float Yr2r[1501]
     * }
     */
    public static void Yr2r(MemorySegment struct, long index0, float fieldValue) {
        Yr2r$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final SequenceLayout Yg2g$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("Yg2g"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static final SequenceLayout Yg2g$layout() {
        return Yg2g$LAYOUT;
    }

    private static final long Yg2g$OFFSET = 6116;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static final long Yg2g$offset() {
        return Yg2g$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static MemorySegment Yg2g(MemorySegment struct) {
        return struct.asSlice(Yg2g$OFFSET, Yg2g$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static void Yg2g(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, Yg2g$OFFSET, Yg2g$LAYOUT.byteSize());
    }

    private static long[] Yg2g$DIMS = { 1501 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static long[] Yg2g$dimensions() {
        return Yg2g$DIMS;
    }
    private static final VarHandle Yg2g$ELEM_HANDLE = Yg2g$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static float Yg2g(MemorySegment struct, long index0) {
        return (float)Yg2g$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * float Yg2g[1501]
     * }
     */
    public static void Yg2g(MemorySegment struct, long index0, float fieldValue) {
        Yg2g$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final SequenceLayout Yb2b$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("Yb2b"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static final SequenceLayout Yb2b$layout() {
        return Yb2b$LAYOUT;
    }

    private static final long Yb2b$OFFSET = 12120;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static final long Yb2b$offset() {
        return Yb2b$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static MemorySegment Yb2b(MemorySegment struct) {
        return struct.asSlice(Yb2b$OFFSET, Yb2b$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static void Yb2b(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, Yb2b$OFFSET, Yb2b$LAYOUT.byteSize());
    }

    private static long[] Yb2b$DIMS = { 1501 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static long[] Yb2b$dimensions() {
        return Yb2b$DIMS;
    }
    private static final VarHandle Yb2b$ELEM_HANDLE = Yb2b$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static float Yb2b(MemorySegment struct, long index0) {
        return (float)Yb2b$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * float Yb2b[1501]
     * }
     */
    public static void Yb2b(MemorySegment struct, long index0, float fieldValue) {
        Yb2b$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

