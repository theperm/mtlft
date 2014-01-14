package uk.co.real_logic;

import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

public class ManyToManyQueueValidatingPerfTest
{
    private static final int REPETITIONS = 20 * 1000 * 1000;
    private static final int NUM_PRODUCERS = 2;
    private static final int NUM_CONSUMERS = 2;
    private static final int QUEUE_CAPACITY = 64 * 1024;

    public static void main(final String[] args)
        throws Exception
    {
        ManyToManyQueueValidatingPerfTest test = new ManyToManyQueueValidatingPerfTest();
        test.shouldPerformanceTestOfferAndPoll();
    }

    @Test
    public void shouldPerformanceTestOfferAndPoll()
        throws Exception
    {
        final Queue<Integer> queue = new ManyToManyConcurrentArrayQueue<Integer>(QUEUE_CAPACITY);
        //final Queue<Integer> queue = new java.util.concurrent.ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);

        for (int i = 0; i < 5; i++)
        {
            System.gc();
            Thread.sleep(1000L);
            perfRun(i, queue);
        }
    }

    private void perfRun(final int runNum, final Queue<Integer> queue)
        throws Exception
    {
        final Thread[] consumerThreads = new Thread[NUM_CONSUMERS];
        final Consumer[] consumers = new Consumer[NUM_CONSUMERS];
        for (int i = 0; i < NUM_CONSUMERS; i++)
        {
            consumers[i] = new Consumer(queue);
            consumerThreads[i] = new Thread(consumers[i]);
            consumerThreads[i].start();
        }

        final CyclicBarrier barrier = new CyclicBarrier(NUM_PRODUCERS + 1);
        final Thread[] producerThreads = new Thread[NUM_PRODUCERS];
        for (int i = 0; i < NUM_PRODUCERS; i++)
        {
            producerThreads[i] = new Thread(new Producer(queue, i, barrier));
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
        System.out.printf("%d - %d producers %d consumers: %,d ops/sec - %s\n",
                          Integer.valueOf(runNum),
                          Integer.valueOf(NUM_PRODUCERS),
                          Integer.valueOf(NUM_CONSUMERS),
                          Long.valueOf(opsPerSec),
                          this.getClass().getSimpleName());

        for (final Consumer consumer : consumers)
        {
            consumer.halt();
        }

        for (final Thread t : consumerThreads)
        {
            t.join();
        }

        long eventTotal = 0;
        for (Consumer consumer : consumers)
        {
            for (int producerCount : consumer.getCounters())
            {
                eventTotal += producerCount;
            }
        }
        assertEventsExchanged(eventTotal);
    }


    private void assertEventsExchanged(long eventTotal)
    {
        final long expectedEventTotal = REPETITIONS * NUM_PRODUCERS;
        if (eventTotal != expectedEventTotal)
        {
            final String msg = String.format("Queue did not exchange expected number of events: expected=%,d got=%,d",
                                             Long.valueOf(expectedEventTotal), Long.valueOf(eventTotal));
            throw new IllegalStateException(msg);
        }
    }

    private static class Producer implements Runnable
    {
        private final Queue<Integer> queue;
        private final Integer id;
        private final CyclicBarrier barrier;

        private Producer(final Queue<Integer> queue, final int id, final CyclicBarrier barrier)
        {
            this.queue = queue;
            this.id = Integer.valueOf(id);
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

            int i = REPETITIONS + 1;
            while (0 != --i)
            {
                while (!queue.offer(id))
                {
                    Thread.yield();
                }
            }
        }
    }

    private static class Consumer implements Runnable
    {
        private final Queue<Integer> queue;
        private final int[] counters = new int[NUM_PRODUCERS];
        private volatile boolean halt = false;

        private Consumer(final Queue<Integer> queue)
        {
            this.queue = queue;
        }

        public void halt()
        {
            halt = true;
        }

        public int[] getCounters()
        {
            return counters;
        }

        public void run()
        {
            while (true)
            {
                Integer producerIndex;
                while (null == (producerIndex = queue.poll()))
                {
                    if (halt)
                    {
                        return;
                    }

                    Thread.yield();
                }

                ++counters[producerIndex.intValue()];
            }
        }
    }
}

