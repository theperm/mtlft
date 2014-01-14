package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

public final class AtomicCounterTest
{
    public static final long INITIAL_VALUE = 7L;

    @Test
    public void shouldGetValue()
    {
        AtomicCounter counter = new AtomicCounter();
        Assert.assertEquals(0L, counter.get());
    }

    @Test
    public void shouldGetInitialisedValue()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        Assert.assertEquals(INITIAL_VALUE, counter.get());
    }

    @Test
    public void shouldSetAndGet()
    {
        AtomicCounter counter = new AtomicCounter();
        final long testValue = 9L;
        counter.setOrdered(testValue);
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldDoVolatileSet()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        counter.setVolatile(testValue);
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldDoOrderedAdd()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long delta = 9L;
        counter.addOrdered(delta);
        Assert.assertEquals(delta + INITIAL_VALUE, counter.get());
    }

    @Test
    public void shouldCasNewValue()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        Assert.assertTrue(counter.compareAndSet(INITIAL_VALUE, testValue));
        Assert.assertEquals(testValue, counter.get());
    }

    @Test
    public void shouldIncrementAndGet()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        long newValue = counter.incrementAndGet();
        Assert.assertEquals(INITIAL_VALUE + 1L, newValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, counter.get());
    }

    @Test
    public void shouldGetAndIncrement()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        long existingValue = counter.getAndIncrement();
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, counter.get());
    }

    @Test
    public void shouldAddAndGet()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long newValue = counter.addAndGet(increment);
        Assert.assertEquals(INITIAL_VALUE + increment, newValue);
        Assert.assertEquals(INITIAL_VALUE + increment, counter.get());
    }

    @Test
    public void shouldGetAndAdd()
    {
        AtomicCounter counter = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long existingValue = counter.getAndAdd(increment);
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + increment, counter.get());
    }
}


