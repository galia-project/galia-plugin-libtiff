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
 *     ttag_t field_tag;
 *     short field_readcount;
 *     short field_writecount;
 *     TIFFDataType field_type;
 *     unsigned short field_bit;
 *     unsigned char field_oktochange;
 *     unsigned char field_passcount;
 *     char *field_name;
 * }
 * }
 */
public class TIFFFieldInfo {

    TIFFFieldInfo() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        tiffio_h.C_INT.withName("field_tag"),
        tiffio_h.C_SHORT.withName("field_readcount"),
        tiffio_h.C_SHORT.withName("field_writecount"),
        tiffio_h.C_INT.withName("field_type"),
        tiffio_h.C_SHORT.withName("field_bit"),
        tiffio_h.C_CHAR.withName("field_oktochange"),
        tiffio_h.C_CHAR.withName("field_passcount"),
        tiffio_h.C_POINTER.withName("field_name")
    ).withName("$anon$635:13");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt field_tag$LAYOUT = (OfInt)$LAYOUT.select(groupElement("field_tag"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * ttag_t field_tag
     * }
     */
    public static final OfInt field_tag$layout() {
        return field_tag$LAYOUT;
    }

    private static final long field_tag$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * ttag_t field_tag
     * }
     */
    public static final long field_tag$offset() {
        return field_tag$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * ttag_t field_tag
     * }
     */
    public static int field_tag(MemorySegment struct) {
        return struct.get(field_tag$LAYOUT, field_tag$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * ttag_t field_tag
     * }
     */
    public static void field_tag(MemorySegment struct, int fieldValue) {
        struct.set(field_tag$LAYOUT, field_tag$OFFSET, fieldValue);
    }

    private static final OfShort field_readcount$LAYOUT = (OfShort)$LAYOUT.select(groupElement("field_readcount"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * short field_readcount
     * }
     */
    public static final OfShort field_readcount$layout() {
        return field_readcount$LAYOUT;
    }

    private static final long field_readcount$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * short field_readcount
     * }
     */
    public static final long field_readcount$offset() {
        return field_readcount$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * short field_readcount
     * }
     */
    public static short field_readcount(MemorySegment struct) {
        return struct.get(field_readcount$LAYOUT, field_readcount$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * short field_readcount
     * }
     */
    public static void field_readcount(MemorySegment struct, short fieldValue) {
        struct.set(field_readcount$LAYOUT, field_readcount$OFFSET, fieldValue);
    }

    private static final OfShort field_writecount$LAYOUT = (OfShort)$LAYOUT.select(groupElement("field_writecount"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * short field_writecount
     * }
     */
    public static final OfShort field_writecount$layout() {
        return field_writecount$LAYOUT;
    }

    private static final long field_writecount$OFFSET = 6;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * short field_writecount
     * }
     */
    public static final long field_writecount$offset() {
        return field_writecount$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * short field_writecount
     * }
     */
    public static short field_writecount(MemorySegment struct) {
        return struct.get(field_writecount$LAYOUT, field_writecount$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * short field_writecount
     * }
     */
    public static void field_writecount(MemorySegment struct, short fieldValue) {
        struct.set(field_writecount$LAYOUT, field_writecount$OFFSET, fieldValue);
    }

    private static final OfInt field_type$LAYOUT = (OfInt)$LAYOUT.select(groupElement("field_type"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * TIFFDataType field_type
     * }
     */
    public static final OfInt field_type$layout() {
        return field_type$LAYOUT;
    }

    private static final long field_type$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * TIFFDataType field_type
     * }
     */
    public static final long field_type$offset() {
        return field_type$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * TIFFDataType field_type
     * }
     */
    public static int field_type(MemorySegment struct) {
        return struct.get(field_type$LAYOUT, field_type$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * TIFFDataType field_type
     * }
     */
    public static void field_type(MemorySegment struct, int fieldValue) {
        struct.set(field_type$LAYOUT, field_type$OFFSET, fieldValue);
    }

    private static final OfShort field_bit$LAYOUT = (OfShort)$LAYOUT.select(groupElement("field_bit"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned short field_bit
     * }
     */
    public static final OfShort field_bit$layout() {
        return field_bit$LAYOUT;
    }

    private static final long field_bit$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned short field_bit
     * }
     */
    public static final long field_bit$offset() {
        return field_bit$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned short field_bit
     * }
     */
    public static short field_bit(MemorySegment struct) {
        return struct.get(field_bit$LAYOUT, field_bit$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned short field_bit
     * }
     */
    public static void field_bit(MemorySegment struct, short fieldValue) {
        struct.set(field_bit$LAYOUT, field_bit$OFFSET, fieldValue);
    }

    private static final OfByte field_oktochange$LAYOUT = (OfByte)$LAYOUT.select(groupElement("field_oktochange"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char field_oktochange
     * }
     */
    public static final OfByte field_oktochange$layout() {
        return field_oktochange$LAYOUT;
    }

    private static final long field_oktochange$OFFSET = 14;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char field_oktochange
     * }
     */
    public static final long field_oktochange$offset() {
        return field_oktochange$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char field_oktochange
     * }
     */
    public static byte field_oktochange(MemorySegment struct) {
        return struct.get(field_oktochange$LAYOUT, field_oktochange$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char field_oktochange
     * }
     */
    public static void field_oktochange(MemorySegment struct, byte fieldValue) {
        struct.set(field_oktochange$LAYOUT, field_oktochange$OFFSET, fieldValue);
    }

    private static final OfByte field_passcount$LAYOUT = (OfByte)$LAYOUT.select(groupElement("field_passcount"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char field_passcount
     * }
     */
    public static final OfByte field_passcount$layout() {
        return field_passcount$LAYOUT;
    }

    private static final long field_passcount$OFFSET = 15;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char field_passcount
     * }
     */
    public static final long field_passcount$offset() {
        return field_passcount$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char field_passcount
     * }
     */
    public static byte field_passcount(MemorySegment struct) {
        return struct.get(field_passcount$LAYOUT, field_passcount$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char field_passcount
     * }
     */
    public static void field_passcount(MemorySegment struct, byte fieldValue) {
        struct.set(field_passcount$LAYOUT, field_passcount$OFFSET, fieldValue);
    }

    private static final AddressLayout field_name$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("field_name"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * char *field_name
     * }
     */
    public static final AddressLayout field_name$layout() {
        return field_name$LAYOUT;
    }

    private static final long field_name$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * char *field_name
     * }
     */
    public static final long field_name$offset() {
        return field_name$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * char *field_name
     * }
     */
    public static MemorySegment field_name(MemorySegment struct) {
        return struct.get(field_name$LAYOUT, field_name$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * char *field_name
     * }
     */
    public static void field_name(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(field_name$LAYOUT, field_name$OFFSET, fieldValue);
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

