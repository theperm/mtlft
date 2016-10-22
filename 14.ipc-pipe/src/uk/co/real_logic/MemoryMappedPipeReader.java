package uk.co.real_logic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.LockSupport;

import static uk.co.real_logic.PipeDescriptor.*;

public class MemoryMappedPipeReader
    implements Closeable
{
    private final int mask;
    private final int capacity;
    private final int readCounterIndex;
    private final int writeCounterIndex;
    private final int eofIndex;

    private final FileChannel channel;
    private final AtomicBuffer atomicBuffer;

    public MemoryMappedPipeReader(final File file, final int capacity)
        throws IOException
    {
        if (Integer.bitCount(capacity) != 1)
        {
            String msg = "capacity must be a power of 2 in size: capacity=" + capacity;
            throw new IllegalArgumentException(msg);
        }

        if (!file.exists())
        {
            throw new IllegalStateException("file does not exist: " + file);
        }

        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        final int fileSize = capacity + TRAILER_SIZE;
        while (randomAccessFile.length() != fileSize)
        {
            LockSupport.parkNanos(1);
        }

        this.capacity = capacity;
        channel = randomAccessFile.getChannel();
        final MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

        mask = capacity - 1;
        readCounterIndex = capacity + READ_COUNTER_TRAILER_OFFSET;
        writeCounterIndex = capacity + WRITE_COUNTER_TRAILER_OFFSET;
        eofIndex = capacity + EOF_FLAG_TRAILER_OFFSET;
        final int readerConnectedIndex = capacity + READER_CONNECTED_FLAG_TRAILER_OFFSET;

        atomicBuffer = new AtomicBuffer(mappedBuffer);
        atomicBuffer.putLongVolatile(readerConnectedIndex, 1);
    }

    public void close()
        throws IOException
    {
        channel.close();
    }

    public boolean isEof()
    {
        return TRUE == atomicBuffer.getLongVolatile(eofIndex);
    }

    public int read(final byte[] dst, final int offset, final int count)
        throws IOException
    {
        // TODO

        int start = (int)atomicBuffer.getLongVolatile(readCounterIndex);
        int readCount = Math.min(capacity-start, count);

        atomicBuffer.putBytes(start, src, offset, writeCount);

        atomicBuffer.putOrderedLong(readCounterIndex, start + readCount);

        return count - readCount;
    }
}
