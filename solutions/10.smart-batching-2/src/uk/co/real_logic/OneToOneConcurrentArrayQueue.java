package uk.co.real_logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class OneToOneConcurrentArrayQueue<E> implements Queue<E>
{
    private final int capacity;
    private final int mask;
    private final E[] buffer;

    private final AtomicCounter head = new AtomicCounter(0);
    private final AtomicCounter tail = new AtomicCounter(0);

    private final AtomicCounter headCache = new AtomicCounter(0);
    private final AtomicCounter tailCache = new AtomicCounter(0);

    @SuppressWarnings("unchecked")
    public OneToOneConcurrentArrayQueue(final int capacity)
    {
        this.capacity = Util.findNextPositivePowerOfTwo(capacity);
        mask = this.capacity - 1;
        buffer = (E[])new Object[this.capacity];
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

        long currentHead = headCache.get();
        long bufferLimit = currentHead + capacity;
        final long currentTail = tail.get();
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

        buffer[(int)currentTail & mask] = e;
        tail.setOrdered(currentTail + 1);

        return true;
    }

    public E poll()
    {
        final long currentHead = head.get();
        if (currentHead >= tailCache.get())
        {
            long currentTail = tail.get();
            if (currentHead >= currentTail)
            {
                return null;
            }

            tailCache.setOrdered(currentTail);
        }

        final int index = (int)currentHead & mask;
        final E e = buffer[index];
        buffer[index] = null;
        head.setOrdered(currentHead + 1);

        return e;
    }

    public E remove()
    {
        final E e = poll();
        if (null == e)
        {
            throw new IllegalStateException("Queue is empty");
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
        return buffer[(int)head.get() & mask];
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
            E e = buffer[(int)i & mask];
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
        for (final E o : c)
        {
            add(o);
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
        throw new UnsupportedOperationException();
    }
}

