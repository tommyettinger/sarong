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
    public static int[] generate(int stateA, int stateB, final int[] data)
    {
        for (int i = 0; i < data.length; i++) 
        {
            final int s = (stateA += 0xC1C64E6D);
            int x = (s ^ s >>> 17) * ((stateB += (s | -s) >> 31 & 0x9E3779BB) >>> 12 | 1);
            x = (x ^ x >>> 16) * 0xAC451;
            data[i] = (x ^ x >>> 15);
        }
        return data;
    }
}
