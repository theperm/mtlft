/*
 * Copyright 2012 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

/**
 * A {@link java.util.concurrent.Executor} that has a pool of threads for executing asynchronous commands.
 */
public class ThreadPoolExecutor
    implements OverflowExecutor
{
    private final AtomicCounter overflowCounter = new AtomicCounter(AtomicCounter.INITIAL_VALUE);
    private final IdleStrategy idleStrategy = new ProgressiveBackoffStrategy();
    private final SequencedContainerQueue<Runnable> queue;
    private final ExceptionHandler exceptionHandler;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicReferenceArray<Thread> parkedWorkers;
    private final AtomicInteger activeWorkerCount;
    private final Thread[] workerThreads;
    private final Thread monitorThread;

    /**
     * Create an {@link java.util.concurrent.Executor} with bounded internal queue size.
     *
     * This executor will use the default {@link NoOpExceptionHandler}.
     *
     * @param threadPoolSize number of threads in the execution pool
     * @param queueCapacity for the bounded internal queue
     */
    public ThreadPoolExecutor(final int threadPoolSize, final int queueCapacity)
    {
        this(threadPoolSize, queueCapacity, new NoOpExceptionHandler());
    }

    /**
     * Create new {@link java.util.concurrent.Executor} with a bounded internal queue size.
     *
     * @param threadPoolSize number of threads in the execution pool
     * @param queueCapacity for the bounded internal queue
     * @param exceptionHandler to be called back when an exception occurs while executing a command
     */
    public ThreadPoolExecutor(final int threadPoolSize,
                              final int queueCapacity,
                              final ExceptionHandler exceptionHandler)
    {
        this(threadPoolSize, new ManyToManyConcurrentArrayQueue<Runnable>(queueCapacity), exceptionHandler);
    }

    /**
     * Create new {@link java.util.concurrent.Executor} with a bounded internal queue size.
     *
     * @param threadPoolSize number of threads in the execution pool
     * @param queue for scheduling commands for execution.
     * @param exceptionHandler to be called back when an exception occurs while executing a command
     */
    public ThreadPoolExecutor(final int threadPoolSize,
                              final SequencedContainerQueue<Runnable> queue,
                              final ExceptionHandler exceptionHandler)
    {
        if (threadPoolSize < 1)
        {
            throw new IllegalArgumentException("Thread pool size must be at least 1: threadPoolSize=" + threadPoolSize);
        }

        if (null == queue)
        {
            throw new NullPointerException("queue cannot be null");
        }

        if (null == exceptionHandler)
        {
            throw new NullPointerException("a null ExceptionHandler is not permitted");
        }

        this.exceptionHandler = exceptionHandler;
        this.queue = queue;

        activeWorkerCount = new AtomicInteger(threadPoolSize);
        parkedWorkers = new AtomicReferenceArray<Thread>(threadPoolSize);
        workerThreads = new Thread[threadPoolSize];

        for (int i = 0; i < threadPoolSize; i++)
        {
            workerThreads[i] = new Thread(new CommandWorker(i));
            workerThreads[i].setDaemon(true);
            workerThreads[i].setName(getClass().getSimpleName() + "-thread-" + i);
            workerThreads[i].start();
        }

        monitorThread = new Thread(new MonitorRunner());
        monitorThread.setDaemon(true);
        monitorThread.setName("thread-pool-monitor");
        monitorThread.start();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final Runnable command)
    {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public long getAddedCount()
    {
        return queue.getAddedCount() + overflowCounter.get();
    }

    /**
     * {@inheritDoc}
     */
    public long getRemovedCount()
    {
        return queue.getRemovedCount() + overflowCounter.get();
    }

    /**
     * {@inheritDoc}
     */
    public int getCapacity()
    {
        return queue.getCapacity();
    }

    /**
     * {@inheritDoc}
     */
    public int getRemainingCapacity()
    {
        return queue.getRemainingCapacity();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void shutdownNow() throws InterruptedException
    {
        if (running.compareAndSet(true, false))
        {
            monitorThread.interrupt();
            monitorThread.join();

            for (final Thread t : workerThreads)
            {
                t.interrupt();
            }

            for (final Thread t : workerThreads)
            {
                t.join();
            }

            queue.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActive()
    {
        return 0 != activeWorkerCount.get();
    }

    /**
     * {@inheritDoc}
     */
    public long getOverflowCounter()
    {
        return overflowCounter.get();
    }

    private void executeOverflow(final Runnable command)
    {
        try
        {
            command.run();
        }
        catch (Throwable t)
        {
            exceptionHandler.handle(t, command);
        }
        finally
        {
            overflowCounter.incrementAndGet();
        }
    }

    private class CommandWorker implements Runnable
    {
        private final int id;
        private Thread thread;

        private CommandWorker(final int id)
        {
            this.id = id;
        }

        public void run()
        {
            thread = Thread.currentThread();

            while (running.get())
            {
                executeNextCommand();
            }

            activeWorkerCount.decrementAndGet();
        }

        private void executeNextCommand()
        {
            Runnable command = null;
            try
            {
                while (null == (command = queue.poll()))
                {
                    if (!running.get())
                    {
                        return;
                    }

                    parkedWorkers.set(id, thread);
                    LockSupport.park();
                }

                command.run();
            }
            catch (final Throwable t)
            {
                exceptionHandler.handle(t, command);
            }
        }
    }


    private class MonitorRunner implements Runnable
    {
        public void run()
        {
            int idleCounter = 0;
            while (running.get())
            {
                final long backlogSize = queue.size();
                if (backlogSize > 0)
                {
                    wakeWorkers(backlogSize);
                    idleCounter = 0;
                }
                else
                {
                    idleStrategy.idle(++idleCounter);
                }
            }
        }

        private void wakeWorkers(long backlogSize)
        {
            // TODO
        }
    }
}
