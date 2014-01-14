package uk.co.real_logic;

import java.io.File;
import java.io.IOException;

public class MemoryMappedPipePerfTest
{
    private static final int REPETITIONS = 200 * 1000 * 1000;
    private static final int PIPE_CAPACITY = 64 * 1024;

    private static final byte[] TEST_MSG = "This is a representative message size for a finance order".getBytes();

    private static long totalBytesRead = 0;

    public static void main(final String[] args)
        throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            final File file = File.createTempFile("pipe-", ".dat");
            file.deleteOnExit();

            final Thread writer = new Thread(new WriteRunner(new MemoryMappedPipeWriter(file, PIPE_CAPACITY)));
            writer.setName("writer");

            final Thread reader = new Thread(new ReadRunner(new MemoryMappedPipeReader(file, PIPE_CAPACITY)));
            reader.setName("reader");

            final long start = System.nanoTime();

            writer.start();
            reader.start();
            reader.join();

            final long duration = System.nanoTime() - start;

            System.out.printf(" %d: %,d msg/s - %,d bytes exchanged\n",
                              Integer.valueOf(i),
                              Long.valueOf((REPETITIONS * 1000000000L) / duration),
                              Long.valueOf(totalBytesRead));

            if (totalBytesRead != (REPETITIONS * (long)TEST_MSG.length))
            {
                String msg = String.format("Invalid number of bytes exchanged: expected %,d got %,d\n",
                                           Long.valueOf(totalBytesRead),
                                           Long.valueOf((REPETITIONS * (long)TEST_MSG.length)));

                throw new IllegalStateException(msg);
            }
        }
    }

    public static class WriteRunner implements Runnable
    {
        private final MemoryMappedPipeWriter writeEnd;

        public WriteRunner(final MemoryMappedPipeWriter writeEnd)
        {
            this.writeEnd = writeEnd;
        }

        public void run()
        {
            try
            {
                writeEnd.waitForReaderToConnect();

                final int messageSize = TEST_MSG.length;
                for (int i = 0; i < REPETITIONS; i++)
                {
                    int remaining = messageSize;
                    do
                    {
                        remaining -= writeEnd.write(TEST_MSG, messageSize - remaining, remaining);
                    }
                    while (remaining > 0);
                }

                writeEnd.close();
            }
            catch (final IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static class ReadRunner implements Runnable
    {
        private final MemoryMappedPipeReader readEnd;

        public ReadRunner(final MemoryMappedPipeReader readEnd)
        {
            this.readEnd = readEnd;
        }

        public void run()
        {
            try
            {
                final int BUFFER_SIZE = 1024;
                final byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;

                while (-1 != (bytesRead = readEnd.read(buffer, 0, BUFFER_SIZE)))
                {
                    totalBytesRead += bytesRead;
                }

                MemoryMappedPipePerfTest.totalBytesRead = totalBytesRead;
                readEnd.close();
            }
            catch (final IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
