package net.adoptopenjdk.bumblebench.examples;

import sarong.DiverRNG;

/**
 * Will almost certainly fail stringent tests. For int array input, it's a little faster
 * than Yolk, and allows a 64-bit seed like Yolk. And it's simple, at least.
 * <br>
 * Created by Tommy Ettinger on 5/11/2020.
 */
public final class DumbHash {
    public final long seed;

    public DumbHash(long seed) {
        this.seed = DiverRNG.randomize(seed);
    }

    public int hash(int[] data) {
        if(data == null) return 0;
        final int len = data.length;
        long s = seed + len, a = 0xC13FA9A902A6328FL * ~(s << 1), r = s ^ a;
        for (int i = 0; i < len; i++) {
            r += (data[i] + i) * a;//(a += 0x9E3779B97F4A7C16L);
        }
        return (int) (r ^ r >>> 25 ^ r >>> 37);
    }

    public int hash(long[] data) {
        if(data == null) return 0;
        final int len = data.length;
        long s = seed + len, a = 0xC13FA9A902A6328FL * ~(s << 1), r = s ^ a, d;
        for (int i = 0; i < len; i++) {
            d = data[i] + i;
            r += (d ^ d >>> 32) * a;//(a += 0x9E3779B97F4A7C16L);
        }
        return (int) (r ^ r >>> 25 ^ r >>> 37);
    }
}
