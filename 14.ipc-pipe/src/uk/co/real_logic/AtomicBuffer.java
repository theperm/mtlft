package uk.co.real_logic;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;

public class AtomicBuffer
{
    private static final Unsafe unsafe = Util.getUnsafe();
    private static final long BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);

    private final long address;
    private final ByteBuffer buffer; // required to ensure buffer is not garbage collected

    public AtomicBuffer(final ByteBuffer buffer)
    {
        if (!buffer.isDirect())
        {
            throw new IllegalArgumentException("buffer must be direct");
        }

        address = ((sun.nio.ch.DirectBuffer)buffer).address();
        this.buffer = buffer;
    }

    public void getBytes(final int index, final byte[] buffer, final int offset, final int length)
    {
        unsafe.copyMemory(null, address + index, buffer, BYTE_ARRAY_OFFSET + offset, length);
    }

    public void putBytes(final int index, final byte[] buffer, final int offset, final int length)
    {
        unsafe.copyMemory(buffer, BYTE_ARRAY_OFFSET + offset, null, address + index, length);
    }

    public long getLongVolatile(final int index)
    {
        return unsafe.getLongVolatile(null, address + index);
    }

    public void putLongVolatile(final int index, final long value)
    {
        unsafe.putLongVolatile(null, address + index, value);
    }

    public void putOrderedLong(final int index, final long value)
    {
        unsafe.putOrderedLong(null, address + index, value);
    }
}
