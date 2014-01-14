package uk.co.real_logic;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Util
{
    private static final Unsafe unsafe;

    static
    {
        try
        {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static Unsafe getUnsafe()
    {
        return unsafe;
    }

    public static int min(final int a, final int b)
    {
        return b + ((a - b) & (a - b) >> 31);
    }
}
