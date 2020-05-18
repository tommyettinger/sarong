package net.adoptopenjdk.bumblebench.examples;

import sarong.DiverRNG;

/**
 * Will almost certainly fail stringent tests. For int array input, it's faster
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
        final long s = seed + len;
        long a = 0xC13FA9A902A6328FL * ~(s << 1);
        long r = s ^ a;
        for (int i = 0; i < len; i++) {
            r += a ^= data[i] * 0x9E3779B97F4A7C15L;//(a += 0x9E3779B97F4A7C16L);
        }
        return (int) (r ^ r >>> 25 ^ r >>> 37);
    }
    
    public int unrolledHash(int[] data) {
        if (data == null)
            return 0;
        //// seed can be any long.
        //// the final step of the hash should handle avalanche decently-well.
        long result = seed;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        // similar to LinnormRNG and its determine() method
        return (int) ((result = ((result = (((result * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ result >>> 23 ^ result >>> 47) * 0xAEF17502108EF2D9L) ^ result >>> 25);
    }

    public int hash(long[] data) {
        if(data == null) return 0;
        final int len = data.length;
        final long s = seed + len;
        long a = 0xC13FA9A902A6328FL * ~(s << 1);
        long r = s ^ a;
        long d;
        for (int i = 0; i < len; i++) {
            d = data[i];
            r += a ^= (d ^ d >>> 32) * 0x9E3779B97F4A7C15L;//(a += 0x9E3779B97F4A7C16L);
        }
        return (int) (r ^ r >>> 25 ^ r >>> 37);
    }
}
