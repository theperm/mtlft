package uk.co.real_logic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SpinLock implements Lock
{
    public static final int NO_HOLDER = -1;

    private final AtomicCounter currentHolder = new AtomicCounter(NO_HOLDER);

    public void lockInterruptibly() throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    public Condition newCondition()
    {
        throw new UnsupportedOperationException();
    }

    public boolean tryLock()
    {
        final long threadId = Thread.currentThread().getId();
        if (threadId == currentHolder.get())
        {
            throw new IllegalStateException("Previous lock has not been unlocked");
        }

        return currentHolder.compareAndSet(NO_HOLDER, threadId);
    }

    public void lock()
    {
        final long threadId = Thread.currentThread().getId();
        if (threadId == currentHolder.get())
        {
            throw new IllegalStateException("Previous lock has not been unlocked");
        }

        while (!currentHolder.compareAndSet(NO_HOLDER, threadId))
        {
            Thread.yield();
        }
    }

    public void unlock()
    {
        final long threadId = Thread.currentThread().getId();
        if (threadId != currentHolder.get())
        {
            throw new IllegalStateException("Unlocked called without holding the lock");
        }

        currentHolder.setOrdered(NO_HOLDER);
    }

    public long getCurrentHolderThreadId()
    {
        return currentHolder.get();
    }
}
