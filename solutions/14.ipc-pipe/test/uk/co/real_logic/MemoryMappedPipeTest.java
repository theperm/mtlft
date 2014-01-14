package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MemoryMappedPipeTest
{
    @Test
    public void shouldExchangeOneMessage() throws Exception
    {
        final String testMsg = "This is a test message to exchange";
        final byte[] testBytes = testMsg.getBytes();

        final File file = File.createTempFile("pipe-", ".dat");
        file.deleteOnExit();

        final int pipeCapacity = 1024;
        final MemoryMappedPipeWriter pipeWriter = new MemoryMappedPipeWriter(file, pipeCapacity);
        final MemoryMappedPipeReader pipeReader = new MemoryMappedPipeReader(file, pipeCapacity);

        pipeWriter.write(testBytes, 0, testBytes.length);

        final byte[] buffer = new byte[1024];
        final int bytes = pipeReader.read(buffer, 0, buffer.length);

        Assert.assertEquals(new String(buffer, 0, bytes), testMsg);
    }

    @Test
    public void shouldExchangeMessageLargerThanBuffer() throws Exception
    {
        final String testMsg = "This is a test message to exchange";
        final byte[] testBytes = testMsg.getBytes();

        final int bufferSize = 1024;
        final byte[] buffer = new byte[bufferSize];
        final int[] bytesRead = new int[1];

        final File file = File.createTempFile("pipe-", ".dat");
        file.deleteOnExit();

        final int pipeCapacity = 32;
        final MemoryMappedPipeWriter pipeWriter = new MemoryMappedPipeWriter(file, pipeCapacity);
        final MemoryMappedPipeReader pipeReader = new MemoryMappedPipeReader(file, pipeCapacity);

        final Thread reader = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    int bytes;
                    bytesRead[0] = 0;
                    while (-1 != (bytes = pipeReader.read(buffer, bytesRead[0], bufferSize - bytesRead[0])))
                    {
                        bytesRead[0] += bytes;
                    }
                }
                catch (final IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        reader.start();

        int remaining = testBytes.length;
        do
        {
            remaining -= pipeWriter.write(testBytes, testBytes.length - remaining, remaining);
        }
        while (remaining > 0);

        pipeWriter.close();

        reader.join();

        final String exchangedMsg = new String(buffer, 0, bytesRead[0]);
        Assert.assertEquals(testBytes.length, bytesRead[0]);
        Assert.assertEquals(exchangedMsg, testMsg);
    }

    @Test
    public void shouldExchangeSecondMessageWithWrap() throws Exception
    {
        final String testMsg = "This is a test message to exchange";
        final byte[] testBytes = testMsg.getBytes();

        final int bufferSize = 1024;
        final byte[] buffer = new byte[bufferSize];
        final int[] bytesRead = new int[1];

        final File file = File.createTempFile("pipe-", ".dat");
        file.deleteOnExit();

        final int pipeCapacity = 64;
        final MemoryMappedPipeWriter pipeWriter = new MemoryMappedPipeWriter(file, pipeCapacity);
        final MemoryMappedPipeReader pipeReader = new MemoryMappedPipeReader(file, pipeCapacity);

        final Thread reader = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    int bytes;
                    bytesRead[0] = 0;
                    while (-1 != (bytes = pipeReader.read(buffer, bytesRead[0], bufferSize - bytesRead[0])))
                    {
                        bytesRead[0] += bytes;
                    }
                }
                catch (final IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        reader.start();

        for (int i = 0; i < 2; i++)
        {
            int remaining = testBytes.length;
            do
            {
                remaining -= pipeWriter.write(testBytes, testBytes.length - remaining, remaining);
            }
            while (remaining > 0);
        }

        pipeWriter.close();

        reader.join();

        final String exchangedMsg = new String(buffer, 0, bytesRead[0]);
        Assert.assertEquals(testBytes.length * 2, bytesRead[0]);
        Assert.assertEquals(exchangedMsg, testMsg + testMsg);
    }
}