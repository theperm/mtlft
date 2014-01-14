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

import sun.misc.Unsafe;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * <p>
 * A bounded concurrent {@link Queue} using a lock-free and wait-free algorithm for exchanging
 * elements between multiple producers and multiple consumers.
 * </p>
 * <p>
 * This is the most general purpose of the queues in this package and should be the default selection
 * when the number of producers or consumers cannot be predicted.  Performance of this {@link Queue} is
 * on par or greater than {@link java.util.concurrent.ConcurrentLinkedQueue} with the additional benefit of generating no garbage
 * and providing bounded semantics on size which would have to be wrapped around {@link java.util.concurrent.ConcurrentLinkedQueue} thus
 * reducing its performance to significantly less than this queue.
 * </p>
 *
 * @param <E> type of the element to be held in this collection.
 */
public class ManyToManyConcurrentArrayQueue<E>
    implements SequencedContainerQueue<E>
{
    private static final Unsafe unsafe;
    private static final int arrayBase;
    private static final int shiftForScale;

    static
    {
        try
        {
            unsafe = Util.getUnsafe();
            arrayBase = unsafe.arrayBaseOffset(Object[].class);
            shiftForScale = Util.calculateShiftForScale(unsafe.arrayIndexScale(Object[].class));
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private final E[] buffer;
    private final int mask;
    private final int capacity;

    private final AtomicCounter tail = new AtomicCounter(0);
    private final AtomicCounter head = new AtomicCounter(0);

    private final AtomicCounter tailCache = new AtomicCounter(0);
    private final AtomicCounter headCache = new AtomicCounter(0);

    @SuppressWarnings("unchecked")
    public ManyToManyConcurrentArrayQueue(final int capacity)
    {
        final int actualCapacity = Util.findNextPositivePowerOfTwo(capacity);
        mask = actualCapacity - 1;
        this.capacity = actualCapacity;
        buffer = (E[])new Object[actualCapacity];
    }

    public boolean add(final E e)
    {
        if (offer(e))
        {
            return true;
        }

        throw new IllegalStateException("Queue is full");
    }

    public boolean offer(final E e)
    {
        if (null == e)
        {
            throw new NullPointerException("Null is not a valid element");
        }

        long currentTail;
        long currentHead = headCache.get();
        long bufferLimit = currentHead + capacity;
        do
        {
            currentTail = tail.get();
            if (currentTail >= bufferLimit)
            {
                currentHead = head.get();
                bufferLimit = currentHead + capacity;
                if (currentTail >= bufferLimit)
                {
                    return false;
                }

                headCache.setOrdered(currentHead);
            }
        }
        while (!tail.compareAndSet(currentTail, currentTail + 1));

        final long elementOffset = calculateOffset((int)currentTail & mask);
        while (!unsafe.compareAndSwapObject(buffer,  elementOffset, null, e))
        {
            // busy spin
        }

        return true;
    }

    public E poll()
    {
        long currentTail = tailCache.get();
        long currentHead;
        do
        {
            currentHead = head.get();
            if (currentHead >= currentTail)
            {
                currentTail = tail.get();
                if (currentHead >= currentTail)
                {
                    return null;
                }
                tailCache.setOrdered(currentTail);
            }
        }
        while (!head.compareAndSet(currentHead, currentHead + 1));

        return removeSequence(calculateOffset((int)currentHead & mask));
    }

    public int drain(final ElementHandler<E> elementHandler)
    {
        int drainCount = 0;
        E e;

        while (null != (e = poll()))
        {
            ++drainCount;
            elementHandler.onElement(e);
        }

        return drainCount;
    }

    public E remove()
    {
        final E e = poll();
        if (null == e)
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return e;
    }

    public E element()
    {
        final E e = peek();
        if (null == e)
        {
            throw new NoSuchElementException("Queue is empty");
        }

        return e;
    }

    public E peek()
    {
        final int index = (int)head.get() & mask;
        return getElementVolatile(index);
    }

    public int size()
    {
        int size;
        do
        {
            final long currentHead = head.get();
            final long currentTail = tail.get();
            size = (int)(currentTail - currentHead);
        }
        while (size > capacity);

        return size;
    }

    public boolean isEmpty()
    {
        return tail.get() == head.get();
    }

    public boolean contains(final Object o)
    {
        if (null == o)
        {
            return false;
        }

        for (long i = head.get(), limit = tail.get(); i < limit; i++)
        {
            final E e = getElementVolatile((int)i & mask);
            if (o.equals(e))
            {
                return true;
            }
        }

        return false;
    }

    public Iterator<E> iterator()
    {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(final T[] a)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> c)
    {
        for (final Object o : c)
        {
            if (!contains(o))
            {
                return false;
            }
        }

        return true;
    }

    public boolean addAll(final Collection<? extends E> c)
    {
        for (final E e : c)
        {
            add(e);
        }

        return true;
    }

    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        Object value;
        do
        {
            value = poll();
        }
        while (null != value);
    }

    public long getAddedCount()
    {
        return tail.get();
    }

    public long getRemovedCount()
    {
        return head.get();
    }

    public int getCapacity()
    {
        return capacity;
    }

    public int getRemainingCapacity()
    {
        return getCapacity() - size();
    }

    private static long calculateOffset(final int index)
    {
        return arrayBase + ((long)index << shiftForScale);
    }

    @SuppressWarnings("unchecked")
    private E getElementVolatile(final int index)
    {
        return (E)unsafe.getObjectVolatile(buffer, calculateOffset(index));
    }

    @SuppressWarnings("unchecked")
    private E removeSequence(final long elementOffset)
    {
        Object e;
        do
        {
            e = unsafe.getObjectVolatile(buffer, elementOffset);
        }
        while (null == e);

        unsafe.putOrderedObject(buffer, elementOffset, null);

        return (E)e;
    }
}

