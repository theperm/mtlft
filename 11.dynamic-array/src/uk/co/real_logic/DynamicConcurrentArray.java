package uk.co.real_logic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class DynamicConcurrentArray<T> implements Collection<T>
{
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final AtomicReference<Object[]> arrayRef = new AtomicReference<Object[]>(EMPTY_ARRAY);

    public boolean add(final T item)
    {
        // TODO
        if (item == null)
            throw new NullPointerException();

        Object[] dest, src;
        do
        {
            src = arrayRef.get();
            dest = new Object[src.length+1];

            System.arraycopy(src, 0,dest, 0, src.length);

            dest[src.length] = item;
        }
        while (!arrayRef.compareAndSet(src, dest));

        return true;
    }

    public boolean remove(final Object item)
    {
        // TODO
        if (item == null)
            throw new NullPointerException();

        Object[] src, dest;

        do
        {
            src = arrayRef.get();
            int index = find(src,item);
            if (index == -1)
                return false;
            dest = new Object[src.length-1];

            System.arraycopy(src, 0, dest, 0, index);
            System.arraycopy(src, index+1,dest, index, dest.length - index);
        }
        while (!arrayRef.compareAndSet(src, dest));

        return true;
    }

    public int size()
    {
        return arrayRef.get().length;
    }

    public Iterator<T> iterator()
    {
        return new ArrayIterator<T>(arrayRef.get());
    }

    private final static class ArrayIterator<T> implements Iterator<T>
    {
        private final Object[] array;
        private int index;

        private ArrayIterator(final Object[] array)
        {
            this.array = array;
        }

        public boolean hasNext()
        {
            return index < array.length;
        }

        @SuppressWarnings("unchecked")
        public T next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }

            return (T)array[index++];
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isEmpty()
    {
        return arrayRef.get().length == 0;
    }

    public boolean contains(final Object o)
    {
        return -1 != find(arrayRef.get(), o);
    }

    public Object[] toArray()
    {
        final Object[] theArray = arrayRef.get();
        return Arrays.copyOf(theArray, theArray.length);
    }

    public <T> T[] toArray(final T[] a)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(final Collection<? extends T> c)
    {
        for (final T item : c)
        {
            add(item);
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
        arrayRef.set(EMPTY_ARRAY);
    }

    private int find(final Object[] array, final Object item)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (item.equals(array[i]))
            {
                return i;
            }
        }

        return -1;
    }
}
