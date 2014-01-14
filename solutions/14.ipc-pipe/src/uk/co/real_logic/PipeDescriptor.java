package uk.co.real_logic;

public class PipeDescriptor
{
    public static final long INITIAL_COUNT = 0;
    public static final int CACHE_LINE_SIZE = 64;
    public static final int TRUE = 1;
    public static final int EOF = -1;

    public static final int READ_COUNTER_TRAILER_OFFSET = CACHE_LINE_SIZE;
    public static final int WRITE_COUNTER_TRAILER_OFFSET = READ_COUNTER_TRAILER_OFFSET + CACHE_LINE_SIZE;
    public static final int EOF_FLAG_TRAILER_OFFSET = WRITE_COUNTER_TRAILER_OFFSET + CACHE_LINE_SIZE;
    public static final int READER_CONNECTED_FLAG_TRAILER_OFFSET = EOF_FLAG_TRAILER_OFFSET + 8;

    public static final int TRAILER_SIZE = EOF_FLAG_TRAILER_OFFSET + CACHE_LINE_SIZE;
}
