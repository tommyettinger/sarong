package net.adoptopenjdk.bumblebench.examples;

public final class LargeArrayGenerator {
    public static long[] generate(long state, final long[] data)
    {
        state = (state & 0x1FFFFFFL) + 1L;
        for (int i = 0; i < data.length; i++) {
            data[i] = (state = 0xC6BC279692B5CC8BL - (state << 35 | state >>> 29));
        }
        return data;
    }
}
