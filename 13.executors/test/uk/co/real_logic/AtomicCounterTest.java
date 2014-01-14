package uk.co.real_logic;

import org.junit.Assert;
import org.junit.Test;

public class AtomicCounterTest
{
    public static final long INITIAL_VALUE = 7L;

    @Test
    public void shouldGetValue()
    {
        AtomicCounter sequence = new AtomicCounter();
        Assert.assertEquals(0L, sequence.get());
    }

    @Test
    public void shouldGetInitialisedValue()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        Assert.assertEquals(INITIAL_VALUE, sequence.get());
    }

    @Test
    public void shouldSetAndGet()
    {
        AtomicCounter sequence = new AtomicCounter();
        final long testValue = 9L;
        sequence.setOrdered(testValue);
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldDoVolatileSet()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        sequence.setVolatile(testValue);
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldDoOrderedAdd()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long delta = 9L;
        sequence.addOrdered(delta);
        Assert.assertEquals(delta + INITIAL_VALUE, sequence.get());
    }

    @Test
    public void shouldCasNewValue()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long testValue = 9L;
        Assert.assertTrue(sequence.compareAndSet(INITIAL_VALUE, testValue));
        Assert.assertEquals(testValue, sequence.get());
    }

    @Test
    public void shouldIncrementAndGet()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        long newValue = sequence.incrementAndGet();
        Assert.assertEquals(INITIAL_VALUE + 1L, newValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, sequence.get());
    }

    @Test
    public void shouldGetAndIncrement()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        long existingValue = sequence.getAndIncrement();
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + 1L, sequence.get());
    }

    @Test
    public void shouldAddAndGet()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long newValue = sequence.addAndGet(increment);
        Assert.assertEquals(INITIAL_VALUE + increment, newValue);
        Assert.assertEquals(INITIAL_VALUE + increment, sequence.get());
    }

    @Test
    public void shouldGetAndAdd()
    {
        AtomicCounter sequence = new AtomicCounter(INITIAL_VALUE);
        final long increment = 5L;
        long existingValue = sequence.getAndAdd(increment);
        Assert.assertEquals(INITIAL_VALUE, existingValue);
        Assert.assertEquals(INITIAL_VALUE + increment, sequence.get());
    }
}


