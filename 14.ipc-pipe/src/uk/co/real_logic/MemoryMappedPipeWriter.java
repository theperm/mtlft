package uk.co.real_logic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.LockSupport;

import static uk.co.real_logic.PipeDescriptor.*;

public class MemoryMappedPipeWriter
    implements Closeable
{
    private final int capacity;
    private final int mask;
    private final int readCounterIndex;
    private final int writeCounterIndex;
    private final int eofIndex;
    private final int readerConnectedIndex;

    private long readCounterCache = INITIAL_COUNT;
    private final FileChannel channel;
    private final AtomicBuffer atomicBuffer;

    public MemoryMappedPipeWriter(final File file, final int capacity)
        throws IOException
    {
        if (Integer.bitCount(capacity) != 1)
        {
            String msg = "capacity must be a power of 2 in size: capacity=" + capacity;
            throw new IllegalArgumentException(msg);
        }

        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        final int fileSize = capacity + TRAILER_SIZE;
        for (int i = 0; i < fileSize; i++)
        {
            randomAccessFile.writeByte(0);
        }

        channel = randomAccessFile.getChannel();
        final MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

        this.capacity = capacity;
        mask = capacity - 1;
        readCounterIndex = capacity + READ_COUNTER_TRAILER_OFFSET;
        writeCounterIndex = capacity + WRITE_COUNTER_TRAILER_OFFSET;
        eofIndex = capacity + EOF_FLAG_TRAILER_OFFSET;
        readerConnectedIndex = capacity + READER_CONNECTED_FLAG_TRAILER_OFFSET;

        atomicBuffer = new AtomicBuffer(mappedBuffer);
    }

    public void waitForReaderToConnect()
    {
        while (0 == atomicBuffer.getLongVolatile(readerConnectedIndex))
        {
            LockSupport.parkNanos(1);
        }
    }

    public void close()
        throws IOException
    {
        atomicBuffer.putLongVolatile(eofIndex, 1);
        channel.close();
    }

    public int write(final byte[] src, final int offset, final int count)
        throws IOException
    {
        // TODO

        int start = (int)atomicBuffer.getLongVolatile(writeCounterIndex);
        int writeCount = Math.min(capacity-start, count);

        atomicBuffer.putBytes(start, src, offset, writeCount);

        atomicBuffer.putOrderedLong(writeCounterIndex, start + writeCount);

        return count - writeCount;
    }
}
