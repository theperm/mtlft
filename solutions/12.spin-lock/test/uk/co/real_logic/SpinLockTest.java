package uk.co.real_logic;

import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpinLockTest
{
    @Test
    public void shouldLockAndUnlock()
    {
        final long threadId = Thread.currentThread().getId();

        final SpinLock lock = new SpinLock();
        Assert.assertEquals(SpinLock.NO_HOLDER, lock.getCurrentHolderThreadId());

        lock.lock();
        Assert.assertEquals(threadId, lock.getCurrentHolderThreadId());

        lock.unlock();
        Assert.assertEquals(SpinLock.NO_HOLDER, lock.getCurrentHolderThreadId());
    }

    @Test
    public void shouldSuccessfullyTryLockAndUnlock()
    {
        final long threadId = Thread.currentThread().getId();

        final SpinLock lock = new SpinLock();
        Assert.assertEquals(SpinLock.NO_HOLDER, lock.getCurrentHolderThreadId());

        Assert.assertTrue("Should succeed", lock.tryLock());
        Assert.assertEquals(threadId, lock.getCurrentHolderThreadId());

        lock.unlock();
        Assert.assertEquals(SpinLock.NO_HOLDER, lock.getCurrentHolderThreadId());
    }

    @Test
    public void shouldLockAcrossThreads()
        throws Exception
    {
        final long threadId = Thread.currentThread().getId();
        final AtomicBoolean hasLock = new AtomicBoolean(false);
        final CountDownLatch inLock = new CountDownLatch(1);

        final SpinLock lock = new SpinLock();

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                lock.lock();
                try
                {
                    inLock.countDown();

                    for (int i = 0; i < 1000; i++)
                    {
                        Assert.assertFalse("Should not have got lock", hasLock.get());
                    }
                }
                finally
                {
                    lock.unlock();
                }
            }
        });

        t.start();
        inLock.await();

        lock.lock();
        hasLock.set(true);

        Assert.assertEquals(threadId, lock.getCurrentHolderThreadId());
    }

    @Test
    public void shouldTryLockAcrossThreads()
        throws Exception
    {
        final CountDownLatch inLock = new CountDownLatch(1);
        final CountDownLatch leaveLock = new CountDownLatch(1);

        final SpinLock lock = new SpinLock();

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                lock.lock();
                try
                {
                    inLock.countDown();

                    leaveLock.await();
                }
                catch (InterruptedException ex)
                {
                    // not needed
                }
                finally
                {
                    lock.unlock();
                }
            }
        });

        t.start();
        inLock.await();
        Assert.assertFalse("Should not succeed", lock.tryLock());

        leaveLock.countDown();
        t.join();
        Assert.assertTrue("Should succeed", lock.tryLock());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfUnlockFromAnotherThread()
        throws Exception
    {
        final SpinLock lock = new SpinLock();
        final CountDownLatch inLock = new CountDownLatch(1);

        new Thread(new Runnable()
        {
            public void run()
            {
                lock.lock();
                inLock.countDown();
            }
        }).start();

        inLock.await();

        lock.unlock();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfLockTreatedAsReentrant()
    {
        final SpinLock lock = new SpinLock();

        lock.lock();
        lock.lock();
    }
}
