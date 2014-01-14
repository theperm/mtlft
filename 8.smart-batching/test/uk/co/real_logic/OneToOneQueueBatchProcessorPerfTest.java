package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;

public class OneToOneQueueBatchProcessorPerfTest
{
    private static final int REPETITIONS = 100 * 1000 * 1000;
    private static final int QUEUE_SIZE = 64 * 1024;
    private static final int TEST_ELEMENT = 777;

    private static final OneToOneConcurrentArrayQueue<Integer> queue = new OneToOneConcurrentArrayQueue<Integer>(QUEUE_SIZE);

    public static void main(final String[] args)
        throws Exception
    {
        OneToOneQueueBatchProcessorPerfTest test = new OneToOneQueueBatchProcessorPerfTest();
        test.shouldLoopPerformanceTest();
    }

    @Test
    public void shouldLoopPerformanceTest()
        throws Exception
    {
        for (int i  = 0; i < 5; i++)
        {
            System.gc();
            Thread.sleep(1000);
            perfRun(i);
        }
    }

    private void perfRun(int runNumber)
        throws Exception
    {
        final ProducerEventCountingElementHandler eventHandler = new ProducerEventCountingElementHandler();

        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        final QueueProducer producer = new QueueProducer(queue, TEST_ELEMENT, cyclicBarrier, REPETITIONS);

        Thread producerThread = new Thread(producer);
        producerThread.start();

        final DrainingConsumer consumer = new DrainingConsumer(queue, eventHandler);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        cyclicBarrier.await();

        final long start = System.nanoTime();

        producerThread.join();

        while (!queue.isEmpty())
        {
            Thread.yield();
        }

        final long end = System.nanoTime();
        final long duration = end - start;

        consumer.halt();

        Assert.assertEquals(REPETITIONS, eventHandler.getCounter());
        final long ops = (REPETITIONS * 1000L * 1000L * 1000L) / duration;
        System.out.printf("%d - 1 to 1: %,d ops/sec\n", Integer.valueOf(runNumber), Long.valueOf(ops));
    }

    private static class ProducerEventCountingElementHandler implements ElementHandler<Integer>
    {
        private long counter;

        public void onElement(final Integer element)
        {
            ++counter;
        }

        public long getCounter()
        {
            return counter;
        }
    }
}

