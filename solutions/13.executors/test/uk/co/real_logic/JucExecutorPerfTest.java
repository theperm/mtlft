/*
 * Copyright 2012 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor;

public class JucExecutorPerfTest
{
    private static final int REPETITIONS = 20 * 1000 * 1000;
    private static final int NUM_PRODUCERS = 2;
    private static final int THREAD_POOL_SIZE = 2;
    private static final int QUEUE_CAPACITY = 64 * 1024;

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

    public static void main(final String[] args)
        throws Exception
    {
        JucExecutorPerfTest test = new JucExecutorPerfTest();
        test.shouldPerformanceTestOfferAndPoll();
    }

    @Test
    public void shouldPerformanceTestOfferAndPoll()
        throws Exception
    {
        final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler()
        {
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor)
            {
                r.run();
            }
        };

        final ExecutorService executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
                                                                0L, TimeUnit.MILLISECONDS,
                                                                queue, rejectedExecutionHandler);
        for (int i = 0; i < 5; i++)
        {
            System.gc();
            Thread.sleep(1000L);
            perfRun(i, executor);
        }

        executor.shutdownNow();
    }

    private void perfRun(final int runNum, final Executor executor)
        throws Exception
    {
        final Thread[] producerThreads = new Thread[NUM_PRODUCERS];
        final CyclicBarrier barrier = new CyclicBarrier(NUM_PRODUCERS + 1);
        for (int i = 0; i < NUM_PRODUCERS; i++)
        {
            producerThreads[i] = new Thread(new Producer(executor, barrier));
            producerThreads[i].start();
        }

        barrier.await();

        final long start = System.nanoTime();

        for (final Thread t : producerThreads)
        {
            t.join();
        }

        while (!queue.isEmpty())
        {
            Thread.sleep(1);
        }

        long duration = System.nanoTime() - start;
        long opsPerSec = (REPETITIONS * NUM_PRODUCERS * 1000L * 1000L * 1000L) / duration;
        System.out.printf("%d - %d producers %d workers: %,d ops/sec - %s\n",
                          Integer.valueOf(runNum),
                          Integer.valueOf(NUM_PRODUCERS),
                          Integer.valueOf(THREAD_POOL_SIZE),
                          Long.valueOf(opsPerSec),
                          this.getClass().getSimpleName());
    }

    private static class NoOpTask implements Runnable
    {
        public void run()
        {
            // do nothing
        }
    }

    private static class Producer implements Runnable
    {
        private final Executor executor;
        private final CyclicBarrier barrier;

        private Producer(final Executor executor, final CyclicBarrier barrier)
        {
            this.executor = executor;
            this.barrier = barrier;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (Exception ignore)
            {
            }

            final Runnable dummyTask = new NoOpTask();
            int i = REPETITIONS + 1;
            while (0 != --i)
            {
                executor.execute(dummyTask);
            }
        }
    }
}
