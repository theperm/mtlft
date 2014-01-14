package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

public class AtomicCounterTest
{
    public static final long INITIAL_VALUE = 7L;

    @Test
    public void shouldGetValue()
    {
        final AtomicCounter counter = new AtomicCounter();
        Assert.assertEquals(0L, counter.get());
    }

    @Test
    public void shouldGetInitialisedValue()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        Assert.assertEquals(INITIAL_VALUE, counter.get());
    }

    @Test
    public void shouldSetAndGet()
    {
        final AtomicCounter counter = new AtomicCounter();
        final long testValue = 9L;
        counter.setOrdered(testValue);
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldDoVolatileSet()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        counter.setVolatile(testValue);
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldDoOrderedAdd()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long delta = 9L;
        counter.addOrdered(delta);
        Assert.assertEquals(delta + INITIAL_VALUE, counter.get());
    }

    @Test
    public void shouldCasNewValue()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        Assert.assertTrue(counter.compareAndSet(INITIAL_VALUE, testValue));
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldIncrementAndGet()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        long newValue = counter.incrementAndGet();
        Assert.assertEquals(INITIAL_VALUE + 1L, newValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, counter.get());
    }

    @Test
    public void shouldGetAndIncrement()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        long existingValue = counter.getAndIncrement();
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, counter.get());
    }

    @Test
    public void shouldAddAndGet()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long newValue = counter.addAndGet(increment);
        Assert.assertEquals(INITIAL_VALUE + increment, newValue);
        Assert.assertEquals(INITIAL_VALUE + increment, counter.get());
    }

    @Test
    public void shouldGetAndAdd()
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long existingValue = counter.getAndAdd(increment);
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + increment, counter.get());
    }

    @Test
    public void shouldGetAndAddConcurrently() throws Exception
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final int reps = 25 * 1000 * 1000;

        final Thread t1 = new Thread(new GetAndAddRunner(counter, reps));
        final Thread t2 = new Thread(new GetAndAddRunner(counter, reps));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertEquals(INITIAL_VALUE + (reps * 2), counter.get());
    }

    @Test
    public void shouldAddAndGetConcurrently() throws Exception
    {
        final AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final int reps = 25 * 1000 * 1000;

        final Thread t1 = new Thread(new AddAndGetRunner(counter, reps));
        final Thread t2 = new Thread(new AddAndGetRunner(counter, reps));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertEquals(INITIAL_VALUE + (reps * 2), counter.get());
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


