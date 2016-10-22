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
        // TODO
        if (getCurrentHolderThreadId() == Thread.currentThread().getId())
            throw new IllegalStateException();

        if (getCurrentHolderThreadId() != NO_HOLDER)
            return false;

        return currentHolder.compareAndSet(getCurrentHolderThreadId(),
                Thread.currentThread().getId());
    }

    public void lock()
    {

        // TODO
        while(!tryLock())
        {
            Thread.yield();
        }
    }

    public void unlock()
    {
        // TODO
        if (getCurrentHolderThreadId() != Thread.currentThread().getId())
            throw new IllegalStateException();

        currentHolder.compareAndSet(getCurrentHolderThreadId(),
                NO_HOLDER);
    }

    public long getCurrentHolderThreadId()
    {
        return currentHolder.get();
    }
}
