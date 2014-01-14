package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

public class AtomicCounterTest
{
    public static final long INITIAL_VALUE = 7L;

    @Test
    public void shouldGetValue()
    {
        final AtomicCounter sequence = new AtomicCounter();
        Assert.assertEquals(0L, sequence.get());
    }

    @Test
    public void shouldGetInitialisedValue()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        Assert.assertEquals(INITIAL_VALUE, sequence.get());
    }

    @Test
    public void shouldSetAndGet()
    {
        final AtomicCounter sequence = new AtomicCounter();
        final long testValue = 9L;
        sequence.setOrdered(testValue);
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldDoVolatileSet()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        sequence.setVolatile(testValue);
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldDoOrderedAdd()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long delta = 9L;
        sequence.addOrdered(delta);
        Assert.assertEquals(delta + INITIAL_VALUE, sequence.get());
    }

    @Test
    public void shouldCasNewValue()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        Assert.assertTrue(sequence.compareAndSet(INITIAL_VALUE, testValue));
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldIncrementAndGet()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        long newValue = sequence.incrementAndGet();
        Assert.assertEquals(INITIAL_VALUE + 1L, newValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, sequence.get());
    }

    @Test
    public void shouldGetAndIncrement()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        long existingValue = sequence.getAndIncrement();
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, sequence.get());
    }

    @Test
    public void shouldAddAndGet()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long newValue = sequence.addAndGet(increment);
        Assert.assertEquals(INITIAL_VALUE + increment, newValue);
        Assert.assertEquals(INITIAL_VALUE + increment, sequence.get());
    }

    @Test
    public void shouldGetAndAdd()
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long existingValue = sequence.getAndAdd(increment);
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + increment, sequence.get());
    }

    @Test
    public void shouldGetAndAddConcurrently() throws Exception
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final int reps = 25 * 1000 * 1000;

        final Thread t1 = new Thread(new GetAndAddRunner(sequence, reps));
        final Thread t2 = new Thread(new GetAndAddRunner(sequence, reps));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertEquals(INITIAL_VALUE + (reps * 2), sequence.get());
    }

    @Test
    public void shouldAddAndGetConcurrently() throws Exception
    {
        final AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final int reps = 25 * 1000 * 1000;

        final Thread t1 = new Thread(new AddAndGetRunner(sequence, reps));
        final Thread t2 = new Thread(new AddAndGetRunner(sequence, reps));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertEquals(INITIAL_VALUE + (reps * 2), sequence.get());
    }

    private static class GetAndAddRunner implements Runnable
    {
        private final AtomicCounter atomicCounter;
        private final int reps;

        private GetAndAddRunner(final AtomicCounter atomicCounter, final int reps)
        {
            this.atomicCounter = atomicCounter;
            this.reps = reps;
        }

        public void run()
        {
            for (int i = 0, size = reps; i < size; i++)
            {
                atomicCounter.getAndAdd(1);
            }
        }
    }

    private static class AddAndGetRunner implements Runnable
    {
        private final AtomicCounter atomicCounter;
        private final int reps;

        private AddAndGetRunner(final AtomicCounter atomicCounter, final int reps)
        {
            this.atomicCounter = atomicCounter;
            this.reps = reps;
        }

        public void run()
        {
            for (int i = 0, size = reps; i < size; i++)
            {
                atomicCounter.addAndGet(1);
            }
        }
    }
}


