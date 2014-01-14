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
        return false;
    }

    public void lock()
    {
        // TODO
    }

    public void unlock()
    {
        // TODO
    }

    public long getCurrentHolderThreadId()
    {
        return currentHolder.get();
    }
}
