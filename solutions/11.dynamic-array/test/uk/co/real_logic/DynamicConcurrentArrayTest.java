package uk.co.real_logic;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


public class DynamicConcurrentArrayTest
{
    final DynamicConcurrentArray<Integer> array = new DynamicConcurrentArray<Integer>();

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAddingNull()
    {
        array.add(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnRemovingNull()
    {
        array.remove(null);
    }

    @Test
    public void shouldAddItemToArray()
    {
        assertEquals(0, array.size());
        array.add(Integer.valueOf(1));
        assertEquals(1, array.size());
    }

    @Test
    public void shouldRemoveItemFromArrayLeavingItEmpty()
    {
        array.add(Integer.valueOf(1));
        assertTrue("Should remove", array.remove(Integer.valueOf(1)));
        assertEquals(0, array.size());
    }

    @Test
    public void shouldIterateOverArray()
    {
        final int size = 19;
        final List<Integer> expected = newList(0, size - 1);
        array.addAll(expected);

        assertSame(array, expected);
    }

    @Test
    public void shouldRemoveItem()
    {
        final int size = 10;
        final Integer toBeRemoved = Integer.valueOf(7);
        final List<Integer> expected = newList(0, size - 1);
        array.addAll(expected);

        assertTrue(array.remove(toBeRemoved));
        expected.remove(toBeRemoved);

        assertSame(array, expected);
    }

    @Test
    public void shouldNotRemoveItem()
    {
        final int size = 10;
        final Integer notToBeRemoved = Integer.valueOf(17);
        final List<Integer> expected = newList(0, size - 1);
        array.addAll(expected);

        assertFalse(array.remove(notToBeRemoved));

        assertSame(array, expected);
    }

    @Test
    public void shouldHandleConcurrentMutation() throws Exception
    {
        final int numThreads = 4;
        final int keepSize = 100;
        final int repetitions = 10 * 1000 * 1000;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(new ArrayTester(array, i, numThreads, keepSize, repetitions, latch));
            threads[i].start();
        }

        for (final Thread t : threads)
        {
            t.join();
        }

        assertEquals(Integer.valueOf(array.size()), Integer.valueOf(keepSize));

        for (int i  = repetitions - keepSize; i < keepSize; i++)
        {
            assertTrue("must contain " + i, array.contains(Integer.valueOf(i)));
        }
    }

    private static List<Integer> newList(final int from, final int to)
    {
        final int size = to - from;
        final ArrayList<Integer> list = new ArrayList<Integer>(size);

        for (int i = from; i <= to; i++)
        {
            list.add(Integer.valueOf(i));
        }

        return list;
    }

    private static void assertSame(final DynamicConcurrentArray<Integer> tested, final List<Integer> expected)
    {
        assertEquals("Not the same size", tested.size(), expected.size());

        int i = 0;
        for (final Integer v : tested)
        {
            assertEquals("Does not match at index: " + i, v, expected.get(i++));
        }
    }

    public static class ArrayTester implements Runnable
    {
        private final DynamicConcurrentArray<Integer> array;
        private final int startIndex;
        private final int stepSize;
        private final int keepSize;
        private final int repetitions;
        private final CountDownLatch latch;

        public ArrayTester(final DynamicConcurrentArray<Integer> array,
                           final int startIndex,
                           final int stepSize,
                           final int keepSize,
                           final int repetitions,
                           final CountDownLatch latch)
        {
            this.array = array;
            this.startIndex = startIndex;
            this.stepSize = stepSize;
            this.keepSize = keepSize;
            this.repetitions = repetitions;
            this.latch = latch;
        }

        public void run()
        {
            latch.countDown();

            for (int i = startIndex; i < keepSize; i += stepSize)
            {
                array.add(Integer.valueOf(i));
            }

            for (int i = keepSize + startIndex; i < repetitions;  i += stepSize)
            {
                if (!array.add(Integer.valueOf(i)))
                {
                    throw new IllegalStateException("failed to add " + i);
                }

                if (!array.remove(Integer.valueOf(i - keepSize)))
                {
                    throw new IllegalStateException("failed to remove " + (i - keepSize));
                }
            }
        }
    }
}
