package uk.co.real_logic;


import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;

public class OverflowExecutorTest
{
    public static final int QUEUE_CAPACITY = 1024;
    public static final int THREAD_POOL_SIZE = 1;

    @Test(expected = RejectedExecutionException.class)
    public void shouldRejectAfterShutdown() throws InterruptedException
    {
        final OverflowExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, QUEUE_CAPACITY);

        executor.shutdownNow();

        executor.execute(new TestTask());
    }

    private static class TestTask implements Runnable
    {
        public void run()
        {
            // Noop
        }
    }
}
