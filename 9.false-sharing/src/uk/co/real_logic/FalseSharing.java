package uk.co.real_logic;

import java.util.concurrent.atomic.AtomicLong;

public class FalseSharing
    implements Runnable
{
    private final static int NUM_THREADS = 4; // TODO change
    private final static int ITERATIONS = 1000 * 1000 * 1000;

    private final int arrayIndex;

    private static final AtomicLong[] longs = new AtomicLong[NUM_THREADS];
    static
    {
        for (int i = 0; i < longs.length; i++)
        {
            //longs[i] = new PaddedAtomicLong();
            longs[i] = new AtomicLong(); // TODO change
        }
    }

    public FalseSharing(final int arrayIndex)
    {
        this.arrayIndex = arrayIndex;
    }

    public static void main(final String[] args) throws Exception
    {
        final long start = System.nanoTime();

        runTest();

        final long duration = System.nanoTime() - start;
        System.out.printf("%,d duration(ns)\n", duration);
    }

    private static void runTest() throws InterruptedException
    {
        final Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new FalseSharing(i));
        }

        for (final Thread t : threads)
        {
            t.start();
        }

        for (final Thread t : threads)
        {
            t.join();
        }
    }

    public void run()
    {
        final AtomicLong atomicLong = longs[arrayIndex];
        int i = ITERATIONS + 1;
        while (0 != --i)
        {
            atomicLong.set(i);
        }
    }

    public static long sumPaddingToPreventOptimisation(final int index)
    {
        PaddedAtomicLong v = (PaddedAtomicLong)longs[index];
        return v.p1 + v.p2 + v.p3 + v.p4 + v.p5 + v.p6;
    }

    public static class PaddedAtomicLong extends AtomicLong
    {
        public volatile long p1, p2, p3, p4, p5, p6 = 7L;
    }
}