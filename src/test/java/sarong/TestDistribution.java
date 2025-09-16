package sarong;

import org.huldra.math.BigInt;
import org.junit.Assert;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.longlong.Roaring64NavigableMap;
import sarong.util.StringKit;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/31/2018.
 */
public class TestDistribution {
    @Test
    public void test8Bit()
    {
        byte stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 0, t;
        int result, xor = 0;
        BigInt sum = new BigInt(0);
        //long[] counts = new long[256];
        for (int j = 0; j < 256; j++) {
            for (int i = 0x80000000; i < 0x7FFFFFFF; i++) {
//            result = (byte)(stateB+0x6D);
                result = ((stateB << 5 | (stateB & 0xFF) >>> 3) + stateE++ & 0xFF);
                t = (byte) (stateB << 3);
                stateC ^= stateA;
                stateD ^= stateB;
                stateB ^= stateC;
                stateA ^= stateD;
                stateC ^= t;
                stateD = (byte) (stateD << 1 | (stateD & 0xFF) >>> 7);
                result |= ((stateB << 5 | (stateB & 0xFF) >>> 3) + stateE++ & 0xFF) << 8;
                t = (byte) (stateB << 3);
                stateC ^= stateA;
                stateD ^= stateB;
                stateB ^= stateC;
                stateA ^= stateD;
                stateC ^= t;
                stateD = (byte) (stateD << 1 | (stateD & 0xFF) >>> 7);
                result |= ((stateB << 5 | (stateB & 0xFF) >>> 3) + stateE++ & 0xFF) << 16;
                t = (byte) (stateB << 3);
                stateC ^= stateA;
                stateD ^= stateB;
                stateB ^= stateC;
                stateA ^= stateD;
                stateC ^= t;
                stateD = (byte) (stateD << 1 | (stateD & 0xFF) >>> 7);
                result |= ((stateB << 5 | (stateB & 0xFF) >>> 3) + stateE++ & 0xFF) << 24;
                t = (byte) (stateB << 3);
                stateC ^= stateA;
                stateD ^= stateB;
                stateB ^= stateC;
                stateA ^= stateD;
                stateC ^= t;
                stateD = (byte) (stateD << 1 | (stateD & 0xFF) >>> 7);
                xor ^= result;
                sum.add(result);
                //counts[result]++;

            }
        }
        System.out.println(sum.toBinaryString() + ", should be " + Long.toBinaryString(0x80000000L * 0xFFFFFFFFL));
        System.out.println(sum.toString() + ", should be " + (0x80000000L * 0xFFFFFFFFL));
        System.out.println(Integer.toBinaryString(xor) + " " + xor);
//        int b = -1;
//        for (int i = 0; i < 32; i++) {
//            System.out.printf("%03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X\n",
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b],
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b]);
//        }
    }
    
    //<< 5 , rotl 3
    //<< 6 , rotl 4
    //<< 9 , rotl 5
    //<< 9 , rotl 7
    //<< 10, rotl 6
    //<< 11, rotl 5
    //<< 11, rotl 6
    //<< 11, rotl 9
    //<< 12, rotl 6
    //<< 12, rotl 8
    //<< 12, rotl 10
    //<< 15, rotl 11
    //<< 17, rotl 13
    
    @Test
    public void test16BitLanyard()
    {
        int r, a = 10, b = 21;
        RoaringBitmap all = new RoaringBitmap();
        int i = -0x80000;
        for (; i <= 0x7FFFF; i++) { 
            r = ((a << 7 | a >>> 9) + b & 0xFFFF);
            final long t = a >>> 12;
            r = (r << t | r >>> 16 - t) & 0xFFFF;
//            r = r * 0x251D & 0xFFFF;
            a = a * 0x2C55 + r & 0xFFFF;
            b = b * 0x3685 + 0xABCD & 0xFFFF;
            all.add(r ^ r >>> 8);
        }
        System.out.println(all.getCardinality());
    }
    private static int hiXorLo(final int a, final int b){
        final long r = (a & 0xFFFFFFFFL) * (b & 0xFFFFFFFFL);
        return (int)(r ^ r >>> 32);
    }
    @Test
    public void test32Bit()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000;
        //// testing this:
//  const P0 = 0xa0761d6478bd642f'u64
//  const P1 = 0xe7037ed1a0b428db'u64
//  const P5x8 = 0xeb44accab455d1e5'u64
//  Hash(hiXorLo(hiXorLo(P0, uint64(x) xor P1), P5x8))
        //// 2019921967/4294967296 outputs were present.
        //// 52.97002682927996% of outputs were missing.

        //// When run with P0 and P1 switched,
        //// 2011632872/4294967296 outputs were present.
        //// 53.16302236169577% of outputs were missing.

        for (; i < 0; i++) {
            all.add(hiXorLo(hiXorLo(0x78bd642f, i ^ 0xa0b428db), 0xb455d1e5));
        }
        for (; i >= 0; i++) {
            all.add(hiXorLo(hiXorLo(0x78bd642f, i ^ 0xa0b428db), 0xb455d1e5));
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x1p-32 * 100.0 + "% of outputs were missing.");
    }
    @Test
    public void test32BitSimpler()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000;
        //// 2722710391/4294967296 outputs were present.
        //// 36.606958718039095% of outputs were missing.
        for (; i < 0; i++) {
            all.add(hiXorLo(0x78bd642f, i ^ 0xa0b428db));
        }
        for (; i >= 0; i++) {
            all.add(hiXorLo(0x78bd642f, i ^ 0xa0b428db));
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x1p-32 * 100.0 + "% of outputs were missing.");
    }

    /**
     * Testing constant found by <a href="https://github.com/hayguen/mlpolygen">mlpolygen</a>. It works!
     */
    @Test
    public void test32BitLFSR()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000, state = -1;
        for (; i < 0; i++) {
            all.add(state = state >>> 1 ^ (-(state & 1) & 0xA9FA3215));
            if(state == -1) break;
        }
        if(state != -1) {
            for (; i >= 0; i++) {
                all.add(state = state >>> 1 ^ (-(state & 1) & 0xA9FA3215));
                if(state == -1) break;
            }
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }

    /**
     * Just one less output (0) than all possible ints, which is correct for an LFSR.
     */
    @Test
    public void test32BitLFSRReverse()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000, state = -1;
        for (; i < 0; i++) {
            all.add(state = state << 1 ^ (state >> 31 & 0xA84C5F95));
            if(state == -1) break;
        }
        if(state != -1) {
            for (; i >= 0; i++) {
                all.add(state = state << 1 ^ (state >> 31 & 0xA84C5F95));
                if(state == -1) break;
            }
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }

    @Test
    public void test32BitSplitMixFixedPoint()
    {
        int i = 0x80000000, state = -1;
        for (; i < 0; i++) {
            int y = state; // state coming into this call
            int z = (state += 0x9E3779B9); // state changes, y is unchanged.
            z ^= z >>> 16;
            z *= 0x21f0aaad;
            z ^= z >>> 15;
            z *= 0x735a2d97;
            z ^= z >>> 15; // z would normally be the output here
            if(y == z) // if the incoming state is equal to the output, print the fixed point
                System.out.printf("0x%08X, ", z);
        }
        for (; i >= 0; i++) {
            int y = state;
            int z = (state += 0x9E3779B9);
            z ^= z >>> 16;
            z *= 0x21f0aaad;
            z ^= z >>> 15;
            z *= 0x735a2d97;
            z ^= z >>> 15;
            if(y == z)
                System.out.printf("0x%08X, ", z);
        }
    }


    // h == state, state before: 0x61C88647, 0x51B6B222
    // h == state, state after:  0x00000000, 0xEFEE2BDB,
    // h == y: 0x1784893B, 0x19A8593E
    @Test
    public void test32BitFMixFixedPoint()
    {
        int i = 0x80000000, state = -1;
        for (; i < 0; i++) {
            int y = state;
            int h = (state += 0x9E3779B9);
            h ^= h >>> 16;
            h *= 0x85ebca6b;
            h ^= h >>> 13;
            h *= 0xc2b2ae35;
            h ^= h >>> 16;
            if(h == state)
            {
                System.out.printf("0x%08X, ", h);
            }
        }
        for (; i >= 0; i++) {
            int y = state;
            int h = (state += 0x9E3779B9);
            h ^= h >>> 16;
            h *= 0x85ebca6b;
            h ^= h >>> 13;
            h *= 0xc2b2ae35;
            h ^= h >>> 16;
            if(h == state)
            {
                System.out.printf("0x%08X, ", h);
            }
        }
    }
    @Test
    public void test32BitRightLFSR()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int ctr = 0, state = -1;
        ALL:
        for (int i = 0; i < 0x10000; i++) {
            for (int j = 0; j < 0x10000; j++) {
                all.add(state = state >>> 1 ^ (-(state & 1) & 0xA9FA3215));
                if (state == -1) break ALL;
                ++ctr;
            }
        }
        System.out.println("Period: " + ctr);
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }
    @Test
    public void test32BitRightAlternateLFSR()
    {
        int sum = 0;
        int state = -1;
        long ctr = 0L;
        final int TAPS = 0xAA335600;
        ALL:
        for (int i = 0; i < 0x10000; i++) {
            for (int j = 0; j < 0x10000; j++) {
                sum += (state = state >>> 1 ^ (-(state & 1) & TAPS));
                ++ctr;
                if (state == -1) {
                    System.out.println("Successfully cycled with period: " + ctr + "; maximum is " + 0x100000000L);
                    break ALL;
                }
            }
        }
        System.out.println("Sum: " + sum + "; should be " + 0x80000000);
    }
    @Test
    public void test32BitLeftAlternateLFSR()
    {
        int sum = 0;
        int state = -1;
        long ctr = 0L;
        final int TAPS = 0xA84C5F95;
//        System.out.printf("0x%08X\n", Integer.reverse(0xA9FA3215));
        ALL:
        for (int i = 0; i < 0x10000; i++) {
            for (int j = 0; j < 0x10000; j++) {
                sum += (state = state << 1 ^ (state >> 31 & TAPS));
//                sum += (state = state << 1 ^ (state >> 63 & 0xB5E1107E81BC107BL)); // 64-bit left Galois LFSR
                ++ctr;
                if (state == -1) {
                    System.out.println("Successfully cycled with period: " + ctr + "; maximum is " + 0x100000000L);
                    break ALL;
                }
            }
        }
        System.out.println("Sum: " + sum + "; should be " + 0x80000000);
    }

    public static void main(String[] args)
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000;
        //// testing this:
//  const P0 = 0xa0761d6478bd642f'u64
//  const P1 = 0xe7037ed1a0b428db'u64
//  const P5x8 = 0xeb44accab455d1e5'u64
//  Hash(hiXorLo(hiXorLo(P0, uint64(x) xor P1), P5x8))
//// (using the lower 31 bits of the 32-bit output)
        //// 1539997854/2147483648 outputs were present.
        //// 28.28826168552041% of outputs were missing.
        for (; i < 0; i++) {
            all.add(hiXorLo(hiXorLo(0xa0b428db, i ^ 0x78bd642f), 0xb455d1e5) & 0x7FFFFFFF);
        }
        for (; i >= 0; i++) {
            all.add(hiXorLo(hiXorLo(0xa0b428db, i ^ 0x78bd642f), 0xb455d1e5) & 0x7FFFFFFF);
        }
        System.out.println(all.getLongCardinality() + "/" + 0x80000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-31 + "% of outputs were missing.");
    }

    @Test
    public void test16Bit()
    {
        short t, result, xor = 0;
        BigInt sum = new BigInt(0);
        //long[] counts = new long[256];
        RoaringBitmap all = new RoaringBitmap();
        for (int i = 0; i < 0x10000; i++) {
            //t = (short)(i + 0x9E37);
            //result = (short) ((t << 9 | (t & 0xFFFF) >>> 7) + 0xADE5);
            result = (short) ((i ^ (i & 0xFFFF) >>> 6) + (i << 13));
            xor ^= result;
            sum.add(result);
            all.flip(result & 0xFFFF);
        }
        System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x8000L));
        System.out.println(sum.toString() + ", should be -" + (0x8000L));
        System.out.println(Integer.toBinaryString(xor) + " " + xor);
        System.out.println(all.getLongCardinality());
//        int b = -1;
//        for (int i = 0; i < 32; i++) {
//            System.out.printf("%03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X\n",
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b],
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b]);
//        }
    }
    //<< 12, rotl 10
    //<< 23, rotl 19
    @Test
    public void test64Bit()
    {
        Roaring64NavigableMap all = new Roaring64NavigableMap();
        long s = -0x200000000L, t;
        for (int j = 0; j < 512; j++) {
            for (int i = 0x80000000; i < 0; i++) {
                t = (s << 15) - (s << 19 | s >>> 45);
                if((t & 0xFFFFFFFF00000000L) == 0L)
                {
                    if(all.contains(t))
                    {
                        System.out.println(s + 0x200000000L);
                        return;
                    }
                    all.addLong(t);
                }
                s++;
            }
            System.out.print((j & 7));
        }
        System.out.println("No 32-bit collisions in 2 to the 40 generated longs");
    }
    
    @Test
    public void testOrbit8Bit()
    {
        byte stateA = 0, stateB = 1;
        short[] counts = new short[256];
        for (int i = 0; i < 0x8000; i++) {
            int s = (stateA += 0xCD) & 0xFF;
            if(s >= 11) stateB += 0x96;
            s = (s ^ s >>> 3) * (stateB & 0xFF) & 0xFF;
            counts[s ^ s >>> 3]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testTangly8Bit()
    {
        byte stateA = 0, stateB = 1;
        short[] counts = new short[256];
        for (int i = 0; i < 0x100; i++) {
            byte s = (stateA += 0xCD);
//            if(s == 0) stateB += 0x96;
            s *= (stateB += 0x96);
            s ^= (s & 255) >>> 3;
            s *= stateB;
            counts[(s ^ (s & 255) >>> 3) & 255]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testGrayLCG8Bit()
    {
        int stateA = 0;
        short[] counts = new short[256];
        for (int i = 0; i < 0x100; i++) {
            stateA = ((stateA ^ (stateA & 255)>>>1) + 5 + 11) & 255;
            counts[stateA]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }
    // There is not much rhyme or reason to which ones are full-period.
/*
gray * 1 + 153
gray * 3 + 90
gray * 7 + 80
gray * 9 + 103
gray * 13 + 111
gray * 13 + 243
gray * 19 + 62
gray * 19 + 70
gray * 19 + 234
gray * 23 + 90
gray * 23 + 128
gray * 25 + 227
gray * 27 + 84
gray * 29 + 233
gray * 35 + 186
gray * 37 + 45
gray * 37 + 161
gray * 41 + 3
gray * 41 + 53
gray * 41 + 95
gray * 41 + 219
gray * 43 + 70
gray * 43 + 104
gray * 43 + 152
gray * 43 + 164
gray * 43 + 168
gray * 47 + 40
gray * 47 + 84
gray * 47 + 128
gray * 51 + 38
gray * 51 + 138
gray * 51 + 140
gray * 51 + 152
gray * 55 + 200
gray * 55 + 210
gray * 61 + 35
gray * 61 + 83
gray * 63 + 106
gray * 67 + 120
gray * 67 + 212
gray * 69 + 19
gray * 69 + 171
gray * 71 + 218
gray * 73 + 35
gray * 75 + 4
gray * 75 + 126
gray * 81 + 175
gray * 85 + 161
gray * 85 + 169
gray * 87 + 46
gray * 91 + 10
gray * 93 + 167
gray * 101 + 125
gray * 101 + 243
gray * 103 + 34
gray * 103 + 122
gray * 107 + 216
gray * 115 + 4
gray * 115 + 74
gray * 115 + 158
gray * 115 + 168
gray * 117 + 153
gray * 119 + 78
gray * 123 + 96
gray * 125 + 143
gray * 125 + 161
gray * 131 + 222
gray * 131 + 240
gray * 133 + 31
gray * 137 + 49
gray * 139 + 230
gray * 141 + 53
gray * 141 + 123
gray * 141 + 215
gray * 141 + 225
gray * 149 + 167
gray * 153 + 5
gray * 153 + 93
gray * 155 + 2
gray * 155 + 140
gray * 163 + 216
gray * 165 + 117
gray * 169 + 81
gray * 171 + 214
gray * 171 + 222
gray * 175 + 208
gray * 181 + 1
gray * 181 + 123
gray * 183 + 92
gray * 185 + 165
gray * 187 + 108
gray * 187 + 212
gray * 189 + 7
gray * 189 + 171
gray * 193 + 21
gray * 195 + 44
gray * 195 + 92
gray * 201 + 173
gray * 201 + 183
gray * 205 + 89
gray * 205 + 231
gray * 205 + 243
gray * 205 + 245
gray * 209 + 43
gray * 209 + 87
gray * 209 + 255
gray * 213 + 23
gray * 213 + 57
gray * 213 + 215
gray * 213 + 219
gray * 213 + 231
gray * 215 + 32
gray * 215 + 74
gray * 215 + 124
gray * 215 + 164
gray * 219 + 82
gray * 219 + 222
gray * 221 + 197
gray * 227 + 150
gray * 229 + 43
gray * 231 + 156
gray * 233 + 37
gray * 233 + 255
gray * 237 + 57
gray * 237 + 65
gray * 237 + 149
gray * 243 + 16
gray * 243 + 140
gray * 247 + 24
gray * 249 + 47
gray * 253 + 37
gray * 255 + 230
 */
    @Test
    public void exhaustGrayLCG8Bit()
    {
        int stateA = 0;
        short[] counts = new short[256];
        ArrayList<Integer> pairs = new ArrayList<>(64);
        for (int mul = 1; mul < 256; mul++) {
            PER:
            for (int add = 0; add < 256; add++) {
                Arrays.fill(counts, (short) 0);
                for (int i = 0; i < 0x100; i++) {
                    stateA = ((stateA ^ (stateA & 255) >>> 1) * mul + add) & 255;
                    if(++counts[stateA] > 1) continue PER;
                }
                pairs.add(mul);
                pairs.add(add);
            }
        }
        for (int i = 0, n = pairs.size() - 1; i < n; i++) {
            System.out.println("gray * " + pairs.get(i) + " + " + pairs.get(++i));
        }
    }

    public static int rotate8(int v, int amt) {
        return (v << (amt & 7) & 255) | ((v & 255) >>> (8 - amt & 7));
    }

    public static int clz8(int v) {
        return Integer.numberOfLeadingZeros(v & 255) - 24;
    }

    @Test
    public void testWrangly8Bit()
    {
        short[] counts = new short[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int y = a, z = b;
                z = z + (y ^ rotate8(y, 11) ^ rotate8(y, 50)) & 255;
                y = y + (z ^ rotate8(z, 46) ^ rotate8(z, 21)) & 255;
                z = z + (y ^ rotate8(y,  5) ^ rotate8(y, 14)) & 255;
                y = y + (z ^ rotate8(z, 25) ^ rotate8(z, 41)) & 255;
                z = z + (y ^ rotate8(y, 53) ^ rotate8(y,  3)) & 255;
                y = y + (z ^ rotate8(z, 31) ^ rotate8(z, 37)) & 255;
                counts[y ^ z]++;
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }
    @Test
    public void testWriggly8Bit()
    {
        short[] counts = new short[256];
        int a = 1, b = 1;
        do {
            a = a + 157 & 255;
            b = b + (a + (a >>> 1) >>> 7 & 1) & 255;
            // the above line should increment b 85 out of 256 times.
            // It's a nice branch-free way to guarantee a full period of 2 to the 16 for two 8-bit states.
            // int sum = 0; for(int a = 0; a < 256; a++) {sum += (a + (a >>> 1) & 255) >>> 7;}
            // // 85
            // int sum = 0; for(int a = 0; a < 65536; a++) {sum += (a + (a >>> 1) & 65535) >>> 15;}
            // // 21845
            int y = a, z = b;
            z = z + (y ^ rotate8(y, 11) ^ rotate8(y, 50)) & 255;
            y = y + (z ^ rotate8(z, 46) ^ rotate8(z, 21)) & 255;
            z = z + (y ^ rotate8(y, 5) ^ rotate8(y, 14)) & 255;
            y = y + (z ^ rotate8(z, 27) ^ rotate8(z, 39)) & 255; // changed because of 8-bit stuff
            z = z + (y ^ rotate8(y, 53) ^ rotate8(y, 3)) & 255;
            y = y + (z ^ rotate8(z, 31) ^ rotate8(z, 37)) & 255;
            counts[y ^ z]++;
//            int y = a, z = b;
//            z = z + (y ^ rotate8(y, 11) ^ rotate8(y, 50)) & 255;
//            y = y + (z ^ rotate8(z, 46) ^ rotate8(z, 21)) & 255;
//            z = z + (y ^ rotate8(y, 5) ^ rotate8(y, 14)) & 255;
//            y = y + (z ^ rotate8(z, 25) ^ rotate8(z, 41)) & 255;
//            z = z + (y ^ rotate8(y, 53) ^ rotate8(y, 3)) & 255;
//            y = y + (z ^ rotate8(z, 31) ^ rotate8(z, 37)) & 255;
//            counts[y ^ z]++;
        } while (!(a == 1 && b == 1));
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Tests XEX on Mormur-style changes, adding one constant (b) that is XORed in with the state before and after
     * Moremur is run on the changing state.
     */
    @Test
    public void testXmurx8Bit()
    {
        short[] counts = new short[256];
        int a = 1, b = 16;
        do {
            int y = a ^ b;
            a = a + 151 & 255;

//            y = (y ^ y >>> 4) * 0xB3 & 255;
            y = (y ^ y >>> 3) * 0x75 & 255;
            y = (y ^ y >>> 4) * 0xB3 & 255;
            y = (y ^ y >>> 3);
            counts[y ^ b]++;
        } while (a != 1);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Both Xor-Encrypt-Xor (XEX) and Add-Encrypt-Add work here with a changing "orbit" state.
     */
    @Test
    public void testXmorbx8Bit()
    {
        short[] counts = new short[256];
        int a = 1, b = 1;
        do {
            int x = a;
            int y = b;
//            int y = a + x & 255;
            a = x + 151 & 255;
//            b = b + clz8(a) & 255;
            b = y + (x + (x >>> 1) >>> 7 & 1) & 255;
            // b += x + (x >>> 1) >>> 63; // use this in 64-bit generators

//            y = x ^ y;
//            y = (y ^ y >>> 4) * 0xB3 & 255;
            y = (y ^ y >>> 3 ^ x) * 0x75 + x & 255;
            y = (y ^ y >>> 4) * 0xB3 & 255;
            y = (y ^ y >>> 3);
            counts[y]++;
//            counts[y + x & 255]++;
        } while (a != 1 || b != 1);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testWrangly8BitSingles()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int y = a + b, z = b * 3;
                z = z + (y ^ rotate8(y, 11) ^ rotate8(y, 50)) & 255;
                y = y + (z ^ rotate8(z, 46) ^ rotate8(z, 21)) & 255;
//                z = z + (y ^ rotate8(y,  3) ^ rotate8(y, 13)) & 255;
//                y = y + (z ^ rotate8(z, 28) ^ rotate8(z, 41)) & 255;
                z = z + (y ^ rotate8(y, 53) ^ rotate8(y,  3)) & 255;
                y = y + (z ^ rotate8(z, 31) ^ rotate8(z, 37)) & 255;
                counts[y ^ z]++;
                sums[a] += y ^ z;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testJade8BitSingles()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int y = a + b * 0x8F & 255, z = b * 0x1D & 255;
//                z = z * ((y ^ 0x23) | 1) & 255;
//                y = y * ((z ^ 0x95) | 1) & 255;
                z = z * ((y ^ rotate8(y, 11) ^ rotate8(y, 50)) | 1) & 255;
                y = y * ((z ^ rotate8(z, 31) ^ rotate8(z, 37)) | 1) & 255;
                counts[y ^ y >>> 3]++;
                sums[a] += y ^ y >>> 3;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testJacinth8BitOrbital()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int r = m = m + 0xDB & 0xFF;
//                int q = n = n + 0x91 & 0xFF;
//                int q = n = n + ((m*0x9D&0xFF)>>>4) & 0xFF;
                int q = n = n + ((m|0xAE-m) >>> 31) + 0x91 & 0xFF;
//                int q = n = n + ((m|0xAE-m) >> 31 & 0x91) & 0xFF;
//                int y = a + b * 0x8F & 255, z = b * 0x1D & 255;
//                q = q + (r ^ rotate8(r, 11) ^ rotate8(r, 50)) & 255;
                r = r + (q ^ rotate8(q, 46) ^ rotate8(q, 21)) & 255;
                q = q + (r ^ rotate8(r, 53) ^ rotate8(r,  3)) & 255;
                r = r + (q ^ rotate8(q, 31) ^ rotate8(q, 37)) & 255;
                counts[r]++;
                sums[a] += r;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testJacinth16BitOrbital()
    {
        short[] counts = new short[65536];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int r = m = m + 0xDB & 0xFF;
                int q = n = n + 0x91 - ((m|0xAE-m) >> 31) & 0xFF;
//                int q = n = n + 0x91 & 0xFF;
//                int q = n = n + ((m*0x9D&0xFF)>>>4) & 0xFF;
//                int q = n = n + ((m|0xAE-m) >> 31 & 0x91) & 0xFF;
//                int y = a + b * 0x8F & 255, z = b * 0x1D & 255;
//                q = q + (r ^ rotate8(r, 11) ^ rotate8(r, 50)) & 255;
                r = r + (q ^ rotate8(q, 46) ^ rotate8(q, 21)) & 255;
                q = q + (r ^ rotate8(r, 53) ^ rotate8(r,  3)) & 255;
                r = r + (q ^ rotate8(q, 31) ^ rotate8(q, 37)) & 255;
                int p = r << 8;
                r = m = m + 0xDB & 0xFF;
                q = n = n + 0x91 - ((m|0xAE-m) >> 31) & 0xFF;
                r = r + (q ^ rotate8(q, 46) ^ rotate8(q, 21)) & 255;
                q = q + (r ^ rotate8(r, 53) ^ rotate8(r,  3)) & 255;
                r = r + (q ^ rotate8(q, 31) ^ rotate8(q, 37)) & 255;
                p |= r;
                counts[p]++;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 256; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }


    /**
     * Adapts <a href="https://arxiv.org/abs/2004.06278">this paper's 64-bit Squares RNG</a> to 16-bit.
     * Total number of missing results: 24080/65536
     */
    @Test
    public void testSquares16Bit()
    {
        short[] counts = new short[65536];
        short key = 0x3695;
        for (int a = 0; a < 0x10000; a++) {
            int t, x, y, z;
            y = x = a * key & 0xFFFF; z = y + key & 0xFFFF;
            x = x*x + y & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            x = x*x + z & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            x = x*x + y & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            t = x = x*x + z & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            t ^= ((x*x + y & 0xFF00) >>> 8);
            counts[t]++;
        }
        System.out.println("APPEARANCE COUNTS:");
        int missing = 0;
        for (int y = 0, i = 0; y < 4096; y++) {
            for (int x = 0; x < 16; x++) {
                // remove both "if(y < 128)" conditions if you want to spam your terminal with all output
                if(y < 128)
                    System.out.printf("%04X ", counts[i]);
                if(counts[i] == 0) missing++;
                i++;
            }
            // this one too
            if(y < 128)
                System.out.println();
        }
        System.out.printf("Total number of missing results: %d/65536\n", missing);
    }

    /**
     * Adapts <a href="https://arxiv.org/abs/2004.06278">this paper's 32-bit output Squares RNG</a> to 8-bit output
     * with a 16-bit state.
     * <br>
     * APPEARANCE COUNTS (decimal):
     *  250  265  251  224  269  244  265  263  279  279  289  238  267  266  272  240
     *  237  244  273  266  248  248  242  257  275  242  241  250  274  257  245  242
     *  275  245  253  283  256  240  236  274  257  279  212  247  267  235  232  250
     *  272  244  242  258  248  232  249  270  234  242  276  256  281  245  270  266
     *  287  233  252  231  238  280  245  235  270  256  252  244  257  265  270  239
     *  260  277  255  296  264  248  255  262  236  246  267  255  267  266  261  234
     *  230  234  233  275  257  250  259  271  282  265  264  254  284  282  225  272
     *  243  273  279  249  265  234  239  252  254  273  270  269  265  270  239  244
     *  273  283  257  261  250  269  261  277  269  286  273  226  244  269  267  281
     *  267  282  246  284  256  254  256  262  242  271  245  245  243  234  259  261
     *  254  231  281  273  265  301  252  231  223  249  260  247  245  227  233  257
     *  252  272  253  250  267  267  267  247  267  236  247  259  236  232  230  260
     *  273  273  271  241  248  249  237  235  245  279  243  249  242  261  280  237
     *  249  255  240  237  242  246  278  259  256  257  271  235  266  261  270  258
     *  278  279  250  260  278  225  242  279  239  251  249  267  277  257  252  246
     *  254  232  249  253  270  251  260  241  271  261  255  253  265  250  252  237
     * Total number of missing results: 0/256
     * Lowest appearance count : 212
     * Highest appearance count: 301
     */
    @Test
    public void testSquaresTruncated16Bit()
    {
        short[] counts = new short[256];
        short key = 0x3695;
        for (int a = 0; a < 0x10000; a++) {
            int t, x, y, z;
            y = x = a * key & 0xFFFF; z = y + key & 0xFFFF;
            x = x*x + y & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            x = x*x + z & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            x = x*x + y & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            t = ((x*x + y & 0xFF00) >>> 8);
            counts[t]++;
        }
        System.out.println("APPEARANCE COUNTS (decimal):");
        int missing = 0, lowest = 65536, highest = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%4d ", counts[i]);
                if(counts[i] == 0) missing++;
                lowest = Math.min(lowest, counts[i]);
                highest = Math.max(highest, counts[i]);
                i++;
            }
            System.out.println();
        }
        System.out.printf("Total number of missing results: %d/256\n", missing);
        System.out.printf("Lowest appearance count : %d\n", lowest);
        System.out.printf("Highest appearance count: %d\n", highest);
    }

    /**
     * Surprisingly, every byte appears equally often.
     * This doesn't depend on the rotation amount for q, again surprisingly.
     * This is only equidistributed over the full period of 2 to the 16.
     */
    @Test
    public void testCitrine8BitOrbital()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int r = m = m + 0xDB & 0xFF;
                int q = n = n + ((m|0xAE-m) >>> 31) + 0x91 & 0xFF;
                r ^= r >>> 3;
                r = r * (q | 1) & 255;
                r ^= r >>> 3;
                r = r * (rotate8(q, 2) | 1) & 255;
                r ^= r >>> 3;
                counts[r]++;
                sums[a] += r;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * PERIOD IS 512
     * Very chaotic distribution; some results appear once, or never, or 3x as often as they should.
     */
    @Test
    public void testTwinsies()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int s0 = 0, s1 = 0, iteration = 0;
        // just running once for the maximum possible period first
        for (int b = 0; b < 0x10000; b++) {
            int r = (rotate8(s0, 5) ^ s1) & 255;
            r ^= r >>> (r >>> 7) + 3 ^ r >>> 7;
            s0 = s0 * 0x35 + r & 255;
            s1 = s1 * 0xD5 + 0x95 & 255;
        }

        int check0 = s0, check1 = s1;

        ALL:
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++, iteration++) {
                int r = (rotate8(s0, 5) ^ s1) & 255;
                r ^= r >>> (r >>> 7) + 3 ^ r >>> 7;
                s0 = s0 * 0x35 + r & 255;
                s1 = s1 * 0xD5 + 0x95 & 255;
                counts[r]++;
                sums[a] += r;
                if(s0 == check0 && s1 == check1) {
                    ++iteration;
                    break ALL;
                }
            }
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * PERIOD IS 65536
     * 1D-equidistributed!
     * Hex numbers starting with 7 or lower and ending with an odd digit, of the same length as a word, work well.
     * 0x79, 0x7979, 0x79797979, 0x7979797979797979
     */
    @Test
    public void testMonstruo()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int s0 = 0, s1 = 0, iteration = 0;
        // just running once for the maximum possible period first
        for (int b = 0; b < 0x10000; b++) {
//            int r = (rotate8(s0, 5) ^ s1) & 255;
//            r ^= r >>> (r >>> 7) + 3 ^ r >>> 7;
            s0 = s0 * 0x35 + 0x95 & 255;
            s1 = s1 * 0xD5 + ((byte)s0 % 0x79) & 255;
        }

        int check0 = s0, check1 = s1;

        ALL:
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++, iteration++) {
                int r = (rotate8(s0, 5) ^ s1) & 255;
                r ^= r >>> (r >>> 7) + 3 ^ r >>> 7;
                s0 = s0 * 0x35 + 0x95 & 255;
                // % 0x97 is too high!
                s1 = s1 * 0xD5 + ((byte)s0 % 0x79) & 255;
                counts[r]++;
                sums[a] += r;
                if(s0 == check0 && s1 == check1) {
                    ++iteration;
                    break ALL;
                }
            }
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testPact()
    {
        short[] counts = new short[256];
        int[] sums = new int[256];
        int s0 = 0, s1 = 0, iteration = 0;
        // just running once for the maximum possible period first
        for (int b = 0; b < 0x10000; b++) {
//            int r = (rotate8(s0, 5) ^ s1) & 255;
//            r ^= r >>> (r >>> 7) + 3 ^ r >>> 7;
            s1 = s1 + clz8(s0) & 255;
            s0 = s0 + 0x95 & 255;
        }

        int check0 = s0, check1 = s1;

        ALL:
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++, iteration++) {
                int r = s0 ^ s1;
                r = (r ^ r >>> (r >>> 6) + 2) * 0xC5 & 0xFF;
                r ^= r >>> 5;

                s1 = s1 + clz8(s0) & 255;
                s0 = s0 + 0x95 & 255;
                counts[r]++;
                sums[a] += r;
                if(s0 == check0 && s1 == check1) {
                    ++iteration;
                    break ALL;
                }
            }
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%04X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("SUMS:");
        long total = 0L;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                total += sums[i] & 0xFFFF;
                System.out.printf("%04X ", sums[i++]);
            }
            System.out.println();
        }
        System.out.printf("Total of all sums is %016X\n", total);
        System.out.printf("Expected total is    %016X\n", 256 * 256 * 255 / 2);
    }

    @Test
    public void testImpact()
    {
        short[] counts = new short[256];
        int[] sums = new int[256];
        int s0 = 0, s1 = 0, iteration = 0;
        // just running once for the maximum possible period first
        for (int b = 0; b < 0x10000; b++) {
            s1 = s1 + clz8(s0) & 255;
            s0 = s0 + 0x95 & 255;
        }

        int check0 = s0, check1 = s1;

        ALL:
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++, iteration++) {
                // this is equidistributed.
//                int r = ((rotate8(s0, 1) ^ rotate8(s1, 6) + s0)) & 0xFF;
                // this is equidistributed.
//                int r = ((s0 ^ rotate8(s0, 5) ^ rotate8(s0, 1)) + (s1 ^ rotate8(s1, 7) ^ rotate8(s1, 3))) & 0xFF;
                int r = ((rotate8(s0, 2) ^ rotate8(s1, 2) * (s0|1))) & 0xFF;
                s1 = s1 + clz8(s0) & 255;
                s0 = s0 + 0x95 & 255;
                counts[r]++;
                sums[a] += r;
                if(s0 == check0 && s1 == check1) {
                    ++iteration;
                    break ALL;
                }
            }
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%04X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println("PERIOD IS " + (iteration));
        System.out.println("SUMS:");
        long total = 0L;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                total += sums[i] & 0xFFFF;
                System.out.printf("%04X ", sums[i++]);
            }
            System.out.println();
        }
        System.out.printf("Total of all sums is %016X\n", total);
        System.out.printf("Expected total is    %016X\n", 256 * 256 * 255 / 2);
    }

    @Test
    public void testLeader8BitOrbital()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int r = m = m + 0xDB & 0xFF;
                int q = n = n + (Long.numberOfLeadingZeros(m)) & 0xFF; // well that's... new...

                // this is one round of the Speck cipher's inner "mix" system, with the "key" being 47.
//                q = (rotate8(q, 5) + r ^ 47) & 0xFF;
//                r = (rotate8(r, 2) ^ q);
                // either using 2 rounds of Speck's inner mix (here the key is 79)...
//                q = (rotate8(q, 5) + r ^ 79) & 0xFF;
//                r = (rotate8(r, 2) ^ q);
                // or just xorshifting by 3 is enough to adequately scramble the low-order bits.
//                r ^= r >>> 3;
                // with only one round, the last 3 digits of the sum of 256 items will cycle every 4096 items.
                // this appears in the printed sum data as each row of any one column ending in the same 3 bits.

                // If not using Speck, the below SplitMix-like construct may be stronger.
                r = (r ^ r >>> 5 ^ rotate8(q, 3)) * 0x9D & 0xFF;
                r = (r ^ r >>> 4 ^ rotate8(q, 5)) * 0xC3 & 0xFF;
                r ^= r >>> 3;

                // xor-rotate-rotate on the sum of r and q also could work.
                // it could need extra steps, certainly.
//                r = r + q & 0xFF;
//                r ^= rotate8(r, 5) ^ rotate8(r, 2);
                counts[r]++;
                sums[a] += r;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }


    @Test
    public void testBasic8BitOrbital()
    {
        short[] counts = new short[256];
        short[] sums = new short[256];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                int r = m = m + 0xDB & 0xFF;
                int q = n = n + (m | m >>> (m & 7)) & 0xFF;
                r ^= q;
                counts[r]++;
                sums[a] += r;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("SUMS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(sums[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testSplurge8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int x = a, y = b, z = c, w = d;
                        y = y + (x ^ rotate8(x, 46) ^ rotate8(x, 21)) & 255;
                        z = z + (y ^ rotate8(y,  5) ^ rotate8(y, 14)) & 255;
                        w = w + (z ^ rotate8(z, 25) ^ rotate8(z, 44)) & 255;
                        x = x + (w ^ rotate8(w, 53) ^ rotate8(w,  3)) & 255;
                        counts[x]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }
    @Test
    public void testSporty8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int x = a;
                        x = (x ^ x >>> 4) * (b | 1) & 255;
                        x = (x ^ x >>> 3) * (c | 1) & 255;
                        x = (x ^ x >>> 4) * (d | 1) & 255;
                        counts[x ^ x >>> 3]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testSporty8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = a;
            x = (x ^ x >>> 4) * (b | 1) & 255;
            x = (x ^ x >>> 3) * (c | 1) & 255;
            x = (x ^ x >>> 4) * (d | 1) & 255;
            counts[x ^ x >>> 3]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = a;
            x = (x ^ x >>> 4) * (b | 1) & 255;
            x = (x ^ x >>> 3) * (c | 1) & 255;
            x = (x ^ x >>> 4) * (d | 1) & 255;
            counts[x ^ x >>> 3]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    @Test
    public void testSpoon8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int x = a, y = b;
                        x = (x ^ x >>> 3) * (c | 1) & 255;
                        y = (y ^ y >>> 4) * (d | 1) & 255;
                        counts[x ^ x >>> 4 ^ y ^ y >>> 3]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testSpoon8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = a, y = b;
            x = (x ^ x >>> 3) * (c | 1) & 255;
            y = (y ^ y >>> 4) * (d | 1) & 255;
            counts[x ^ x >>> 4 ^ y ^ y >>> 3]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = a, y = b;
            x = (x ^ x >>> 3) * (c | 1) & 255;
            y = (y ^ y >>> 4) * (d | 1) & 255;
            counts[x ^ x >>> 4 ^ y ^ y >>> 3]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * Over the full state of 2 to the 32 values, this returns each byte result equally often. This is usually not used
     * with an approach that covers all possible states, though, so smaller sections should be investigated.
     */
    @Test
    public void testSpark8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int n = (a ^ c) * 0xF5 & 255;
                        int o = (b ^ d) * 0xD5 & 255;
                        int x = (o ^ rotate8(o, 2) ^ rotate8(o, 7)) + (n ^ rotate8(n, 3) ^ rotate8(n, 4)) & 255;
                        counts[x]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testSpark8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int n = (a ^ c) * 0xF5 & 255;
            int o = (b ^ d) * 0xD5 & 255;
            int x = (o ^ rotate8(o, 2) ^ rotate8(o, 7)) + (n ^ rotate8(n, 3) ^ rotate8(n, 4)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int n = (a ^ c) * 0xF5 & 255;
            int o = (b ^ d) * 0xD5 & 255;
            int x = (o ^ rotate8(o, 2) ^ rotate8(o, 7)) + (n ^ rotate8(n, 3) ^ rotate8(n, 4)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    @Test
    public void testSparkle8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                        int n = (a ^ c) * 0xF5 & 255;
                        int o = b ^ 0x4B;
                        int x = (o ^ rotate8(o, 2) ^ rotate8(o, 7)) + (n ^ rotate8(n, 3) ^ rotate8(n, 4)) & 255;
                        counts[x]++;
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testSparkle8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0;
        for (int i = 0; i < 256; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            int n = (a ^ c) * 0xF5 & 255;
            int o = b ^ 0x4B;
            int x = (o ^ rotate8(o, 2) ^ rotate8(o, 7)) + (n ^ rotate8(n, 3) ^ rotate8(n, 4)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X \n", a, b, c);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * Produces every byte equally often when it can use all combinations of 4 bytes of state.
     */
    @Test
    public void testSpritz8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int x = (a + rotate8(b, 2) + rotate8(c, 7) + rotate8(d, 3)) & 255;
                        counts[x]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * 41.015625% of possible outputs were not produced.
     */
    @Test
    public void testSpritz8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = (a + rotate8(b, 2) + rotate8(c, 7) + rotate8(d, 3)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = (a + rotate8(b, 2) + rotate8(c, 7) + rotate8(d, 3)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * On average, 40.17680287361145% of possible outputs were not produced.
     */
    @Test
    public void testSpritz8BitAverage() {
        int[] counts = new int[256];
        long zeroes = 0;
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 256; z++) {
                    int a = x, b = y, c = z, d = 0;
                    for (int i = 0; i < 256; i++) {
                        a = a + 0x85 & 255;
                        b = b + 0xAF & 255;
                        c = c + 0xB9 & 255;
                        d = d + 0xEB & 255;
                        int r = (a + rotate8(b, 2) + rotate8(c, 7) + rotate8(d, 3)) & 255;
                        counts[r]++;
                    }
                    for (int n = 0; n < 256; n++) {
                        if (counts[n] == 0) zeroes++;
                    }
                    Arrays.fill(counts, 0);
                }
            }
        }
        System.out.println("On average, " + (zeroes * 0x1p-32 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * Produces every byte equally often when it can use all combinations of 4 bytes of state.
     * (Spritz using XOR.)
     */
    @Test
    public void testSpritzer8Bit()
    {
        int[] counts = new int[256];
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                for (int c = 0; c < 0x100; c++) {
                    for (int d = 0; d < 0x100; d++) {
                        int x = (a ^ rotate8(b, 2) ^ rotate8(c, 7) ^ rotate8(d, 3)) & 255;
                        counts[x]++;
                    }
                }
            }
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * 32.03125% of possible outputs were not produced.
     * (Spritz using XOR.)
     */
    @Test
    public void testSpritzer8BitSmall()
    {
        int[] counts = new int[256];
        int a = 0, b = 0, c = 0, d = 0;
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = (a ^ rotate8(b, 2) ^ rotate8(c, 7) ^ rotate8(d, 3)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        for (int i = 0; i < 128; i++) {
            a = a + 0x85 & 255;
            b = b + 0xAF & 255;
            c = c + 0xB9 & 255;
            d = d + 0xEB & 255;
            int x = (a ^ rotate8(b, 2) ^ rotate8(c, 7) ^ rotate8(d, 3)) & 255;
            counts[x]++;
        }
        System.out.printf("a:%02X b:%02X c:%02X d:%02X \n", a, b, c ,d);
        int zeroes = 0;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%02X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-8 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * On average, 35.77956780791283% of possible outputs were not produced.
     */
    @Test
    public void testSpritzer8BitAverage() {
        int[] counts = new int[256];
        long zeroes = 0;
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 256; z++) {
                    int a = x, b = y, c = z, d = 0;
                    for (int i = 0; i < 256; i++) {
                        a = a + 0x85 & 255;
                        b = b + 0xAF & 255;
                        c = c + 0xB9 & 255;
                        d = d + 0xEB & 255;
                        int r = (a ^ rotate8(b, 2) ^ rotate8(c, 7) ^ rotate8(d, 3)) & 255;
                        counts[r]++;
                    }
                    for (int n = 0; n < 256; n++) {
                        if (counts[n] == 0) zeroes++;
                    }
                    Arrays.fill(counts, 0);
                }
            }
        }
        System.out.println("On average, " + (zeroes * 0x1p-32 * 100.0) + "% of possible outputs were not produced.");
    }

    @Test
    public void testSimpleTransition16Bit()
    {
        int[] counts = new int[65536];
        int m = 0, n = 0;
        for (int a = 0; a < 0x100; a++) {
            for (int b = 0; b < 0x100; b++) {
                m = m + (0x91 - ((n|0xAE-n) >> 31)) & 0xFF;
                n = n + 0xDB & 0xFF;
//                int p = m << 8;
//                m = m + (0x91 & ((n|-n) >> 31)) & 0xFF;
//                n = n + 0xDB & 0xFF;
//                p |= m;
                int p = m << 8 | n;
                counts[p]++;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 256; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Appearance counts are perfectly uniform.
     */
    @Test
    public void testSimpleFinalize8Bit()
    {
        int[] counts = new int[256];
        for (int m = 0; m < 0x100; m++) {
            for (int n = 0; n < 0x100; n++) {
                int x = m, y = n;
//                q = (rotate8(q, 5) + r ^ 47) & 0xFF;
//                r = (rotate8(r, 2) ^ q);
//                q = (rotate8(q, 6) + r ^ 79) & 0xFF;
//                r = (rotate8(r, 3) ^ q);
                y = rotate8(y,  3) ^ (x = rotate8(x, 24) + y ^ 2) + rotate8(x,  7);
                x = rotate8(x, 14) ^ (y = rotate8(y, 29) + x ^ 3) + rotate8(y, 11);
                y = rotate8(y, 19) ^ (x = rotate8(x,  5) + y ^ 5) + rotate8(x, 29);
                x = rotate8(x, 17) ^ (y = rotate8(y, 11) + x ^ 7) + rotate8(y, 23);
//                b = (b <<  3 | b >>> 32 -  3) ^ (a = (a << 24 | a >>> 32 - 24) + b ^ 2) + (a <<  7 | a >>> 32 -  7);
//                a = (a << 14 | a >>> 32 - 14) ^ (b = (b << 29 | b >>> 32 - 29) + a ^ 3) + (b << 11 | b >>> 32 - 11);
//                b = (b << 19 | b >>> 32 - 19) ^ (a = (a <<  5 | a >>> 32 -  5) + b ^ 5) + (a << 29 | a >>> 32 - 29);
//                a = (a << 17 | a >>> 32 - 17) ^ (b = (b << 11 | b >>> 32 - 11) + a ^ 7) + (b << 23 | b >>> 32 - 23);
                counts[x&255]++;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void testWyRand8Bit()
    {
        int count = 0x80;
        int[] counts = new int[256];
        for (int m = 0; m < count; m++) {
            int x = m, y = x ^ 0x89;
            x ^= x * y >>> 8 ^ (x * y & 255);
            counts[x&255]++;
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        for (int m = 0x80; m < count + 0x80; m++) {
            int x = m, y = x ^ 0x89;
            x ^= (x * y >>> 8) ^ (x * y & 255);
            counts[x&255]++;
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Not at all equidistributed. Using the high bits of a multiplication seems rather chaotic.
     */
    @Test
    public void testWyIsh8Bit()
    {
        int[] counts = new int[256];
        for (int m = 0; m < 0x100; m++) {
            for (int n = 0; n < 0x100; n++) {
                int x = m, y = n;
                x ^= (x * y >>> 8) ^ (x * y & 255);
                counts[x&255]++;
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }


    @Test
    public void testMWC8Bit()
    {
        short state = 1;
        short[] counts = new short[256];
        int i = 0;
        for (; i < 0x10000; i++) {
            int s = (state = (short) (249 * (state & 0xFF) + (state >>> 8 & 0xFF))) & 0xFF;
            counts[s]++;
            if(state == 1) break;
        }
        for (int y = 0, d = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[d++]) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.printf("Period was %04X\n", i);
    }

    public static int rotl16(int n, int amount) {
        return (n << (amount & 15) & 0xFFFF) | (n & 0xFFFF) >>> (16 - amount & 15);
    }
    @Test
    public void test16BitMulShift()
    {
        short result, xor = 0;
        BigInt sum = new BigInt(0), ZERO = new BigInt(0);
        //long[] counts = new long[256];
        RoaringBitmap all = new RoaringBitmap();
        for (int s = 1; s <= 16; s++) {
            sum.and(ZERO);
            all.clear();
            result = 0;
            xor = 0;
            int mask = (1 << s) - 1;
            for (int i = 0; i <= 0xFFFF; i++) {
                //t = (short)(i + 0x9E37);
                //result = (short) ((t << 9 | (t & 0xFFFF) >>> 7) + 0xADE5);
//                final long n = i * 0x9E3779B97F4A7C15L;
                final int n = (i * (0x4F1) & 0xFFFF);
//                result = (short) (n);
                result = (short) ((n ^ n >>> 5 ^ n >>> 11) & mask);
//                result = (short) ((n ^ rotl16(n, 5) ^ rotl16(n, 11))>>> 16 - s);
//                result = (short) (n >>> 64 - s);
                xor ^= result;
                sum.add(result);
                all.add(result & 0xFFFF);
            }
            System.out.println("\nWith " + s + " bits:");
            System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x8000L));
            System.out.println(sum + ", should be -" + (0x8000L));
            System.out.println(Integer.toBinaryString(xor) + " " + xor);
            System.out.println(all.getLongCardinality());
        }
    }
    public static int scratcherCoord(final int x, final int y) {
        final int n = 123;
        final int h = (((x * 0x1827F5) + (y * 0x123C21) - n ^ 0xD1B54A35) * 0x9E373 ^ 0x91E10DA5) * 0x125493;
        return h ^ (h & 0xFFFF) >>> 11 ^ (h & 0xFFFF) >>> 5;
    }
    public static int iphCoord(int x, int y, int s) {
        s ^= x * 0x1827F5 ^ y * 0x123C21;
        return (s = (s ^ (s << 9 | (s & 0xFFFF) >>> 7) ^ (s << 3 | (s & 0xFFFF) >>> 13) ^ 0xD1B54A35) * 0x125493) ^ (s & 0xFFFF) >>> 5;
    }

    @Test
    public void test16BitHashDist()
    {
        short result, xor = 0;
        BigInt sum = new BigInt(0);
        //long[] counts = new long[256];
        RoaringBitmap all = new RoaringBitmap();
        ALL:
        for (int x = 0; x < 0x10000; x++) {
            for (int y = 0; y < 0x10000; y++) {
//                // surprisingly, this isn't at all evenly distributed... It does produce all hash codes.
//                final long n = x * 0xC13FA9A9L + y * 0x91E10DA5L & 0xFFFFFFFFL;
//                result = (short) (n ^ n >>> 16);
//                // however, this one is evenly distributed, producing all shorts equally often.
//                final long n = (x ^ x >>> 8) * 0xC13FA9A9L + (y ^ y >>> 8) * 0x91E10DA5L & 0xFFFFFFFFL;
//                result = (short) (n ^ (n << 11 | (n & 0xFFFF) >>> 5) ^ (n << 3 | (n & 0xFFFF) >>> 13));
                //scratcherCoord was found and apparently forgotten about, despite colliding less in tables?
                // it is also evenly distributed when adapted for shorts.
//                result = (short)iphCoord(x, y, 123);
                // this is also evenly distributed.
                result = (short)((x ^ x >>> 8) * 0xC13FA9A9L + (y ^ y >>> 8) * 0x91E10DA5L);
                xor ^= result;
                sum.add(result);
                all.add(result & 0xFFFF);
//                if (all.cardinalityExceeds(0xFFFF))
//                    break ALL;
            }
        }
        System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x80000000L));
        System.out.println(sum + ", should be -" + (0x80000000L));
        System.out.println(Integer.toBinaryString(xor) + " " + xor);
        System.out.println(all.getLongCardinality());
//        int b = -1;
//        for (int i = 0; i < 32; i++) {
//            System.out.printf("%03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X\n",
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b],
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b]);
//        }
    }
    @Test
    public void test32BitXorSquareOr()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000;
        int state = 0;
        for (; i < 0; i++) {
//            all.add(i ^ (i * i | 1));
//            all.add(state = (state ^ (state * state | 5)) * 259);
            all.add(state = -(state ^ (state * state | 5)));
            //For each of the above three lines,
            //4294967296/4294967296 outputs were present.
            //0.0% of outputs were missing.
        }
        for (; i >= 0; i++) {
//            all.add(i ^ (i * i | 1));
//            all.add(state = (state ^ (state * state | 5)) * 259);
            all.add(state = -(state ^ (state * state | 5)));
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }

    /**
     * Testing <a href="https://github.com/skeeto/hash-prospector/issues/23">the xorsquare function</a>, or at least
     * a previously-unproven variant on it.
     */
    @Test
    public void test16BitXorSquareOr()
    {
        short result, xor = 0, state = 0;
        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        for (int i = 0; i < 0x10000; i++) {
//            result = (short) (i ^ (i * i | 1)); // works, no fixed-points
            state ^= (state * state | 1);
            result = ++state; //Repeats after 16384 iterations
            if(state == 0) {
                System.out.println("Repeats after " + (i+1) + " iterations");
                break;
            }
            xor ^= result;
            sum.add(result);
            all.flip(result & 0xFFFF);
        }
        System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x8000L));
        System.out.println(sum.toString() + ", should be -" + (0x8000L));
        System.out.println(Integer.toBinaryString(xor) + " " + xor);
        System.out.println(all.getLongCardinality());
    }

    /**
     * Testing <a href="https://github.com/skeeto/hash-prospector/issues/23">the xorsquare function</a>, or at least
     * a previously-unproven variant on it. This is called XQO in my fork of hash-prospector, and there it currently
     * only tests {@code x ^= x * x | 1;}, but this tries out other variants on xorsquare.
     */
    @Test
    public void test16BitXorSquareOrVariants()
    {
        short result = 0, xor = 0, state = 0, m = 1;
        int i = -0x80000000;
        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        for (; i < 0; i++) {
//            result = (short) (i ^ (i * i | 1) ^ i >>> 1); // using a shift of 1, and only 1, generates only half of all results
//            result = (short) (i ^ (i * i * i * i | 1)); // using the 4th power instead of the 2nd still works (generates all results)
//            result = (short) (i ^ (i * i | 3)); // not just 1 works for the OR operand!
//            result = (short) (i ^ (i * i | 5)); // any odd number appears to work
//            result = (short) (i ^ (i * i | 7)); // it isn't clear yet how much improvement could be expected by non-1 operands...
//            result = (short) (i ^ (i * i | 9)); // but that this does work is surprising and interesting.
//            result = (short) (i ^ (i * i | 2)); // however, even operands don't work.
//            result = (state ^= (state * state | ((m += 0x666))));
//            state ^= (state * state | 1);
            result += state ^= (state * state | 1); //Repeats after 131072 iterations
//            result = ++state; //Repeats after 16384 iterations
            if(state == 0 && result == 0) {
                System.out.println("Repeats after " + (i+0x80000001L) + " iterations");
                break;
            }
            xor ^= result;
            sum.add(result);
            all.add(result & 0xFFFF);
        }
        for (; i >= 0; i++) {
//            result = (state ^= (state * state | ((m += 0x666))));
//            state ^= (state * state | 1);
            result += state ^= (state * state | 1);
            if(state == 0 && result == 0) {
                System.out.println("Repeats after " + (i+0x80000001L) + " iterations");
                break;
            }
            xor ^= result;
            sum.add(result);
            all.add(result & 0xFFFF);
        }
        System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x8000L));
        System.out.println(sum + ", should be -" + (0x8000L));
        System.out.println(Integer.toBinaryString(xor) + " " + xor);
        System.out.println(all.getLongCardinality() + ", should be " + (0x10000L));
    }
    @Test
    public void test16BitXorSquareOrPeriods() {
        short result = 0, xor = 0, state = 0;
        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        for (int j = 0; j < 0x10000; j++) { // when (j & 7) equals 5 or 7, this is full-period.
            sum.assign(0);
            all.clear();
            xor = state = 0;
            for (int i = 0; i < 0x10000; i++) {
                // This (multiplier & 3) must equal 3.
                // This includes all powers of two minus 1 that are greater than 2, and many LEA constants.
                // Here, it is -1 .
//                state = (short) (j-(state ^ (state * state | 7))); // for any even j, this is full-period.
                state = (short) (j-(state ^ (state * state | 5))); // for any even j, this is full-period also.
//                state = (short) -(state ^ (state * state | j));
                //state = (state ^ (state * state | o5o7)) * m3; // (o5o7 & 7) must equal 5 or 7, (m3 & 3) must equal 3.
                result = state;
                xor ^= result;
                sum.add(result);
                all.add(result & 0xFFFF);
                if (state == 0) {
                    if(i == 65535)
//                    if(i > 16383)
                    {
                        System.out.println("Using j=" + j + " repeats after " + 65536 + " iterations");
                        System.out.println(sum + ", should be -" + (0x8000L));
                        System.out.println(Integer.toBinaryString(xor) + " " + xor);
                        System.out.println(all.getLongCardinality() + ", should be " + (0x10000L));
                    }
                    break;
                }
            }
        }
    }
    @Test
    public void test16BitMulPeriods() {
        short result = 0, xor = 0, state = 0;
        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        for (int j = 3; j < 0x100; j += 4) {
            sum.assign(0);
            all.clear();
            xor = 0;
            state = 1;
            for (int i = 0; i < 0x10000; i++) {
                state *= j;
                result = state;
                xor ^= result;
                sum.add(result);
                all.add(result & 0xFFFF);
                if (state == 1) {
//                    if(i == 65535)
//                    if(i > 1023)
                    {
                        System.out.println("Using j=" + j + " repeats after " + (i) + " iterations");
                        System.out.println(sum + ", should be -" + (0x8000L));
                        System.out.println(Integer.toBinaryString(xor) + " " + xor);
                        System.out.println(all.getLongCardinality() + ", should be " + (0x10000L));
                    }
                    break;
                }
            }
        }
    }

    @Test
    public void testSpeck1Round0KeySub() {
        final RoaringBitmap all = new RoaringBitmap();
        final int[] counts = new int[0x100];
        for (int k = 0; k < 101; k++) {
            for (int b = 0; b < 256; b++) {
                for (int c = 0; c < 256; c++) {
                    int b0 = b, b1 = c;
//                    b1 = ((b1 << 5 | b1 >>> 3) + b0 ^ k) & 0xFF;
//                    b0 = ((b0 << 2 | b0 >>> 6) ^ b1) & 0xFF;

//                b1 = ((b1 << 5 | b1 >>> 3) + b0 ^ k+1) & 0xFF;
//                b0 = ((b0 << 2 | b0 >>> 6) ^ b1) & 0xFF;

                    // surprisingly, both of the following are equidistributed
//                    b0 = ( (b0 << 2 | b0 >>> 6) ^ (b1 << 5 | b1 >>> 3) + b0 ^ k + b0) & 255;
//                    b0 = ( (b0 << 2 | b0 >>> 6) ^ (b1 << 5 | b1 >>> 3) + b0 ^ k ^ b0) & 255;
                    // neither of the following are
//                    b0 = ( (b0 << 2 | b0 >>> 6) ^ (b1 << 5 | b1 >>> 3) + b0 ^ k + b1) & 255;
//                    b0 = ( (b0 << 2 | b0 >>> 6) ^ (b1 << 5 | b1 >>> 3) + b0 ^ k ^ b1) & 255;
                    // both of the following parts are equidistributed
//                    b0 = ((b1 << 5 | b1 >>> 3) + b0 ^ k + b0) & 255;
//                    b0 = ((b1 << 5 | b1 >>> 3) + b0 ^ k ^ b0) & 255;
                    // the next two lines together are equidistributed
//                    b1 = ((b1 << 5 | b1 >>> 3) + b0 ^ k) & 255;
//                    b0 = (b0 + (b0 << 2 | b0 >>> 6) + b1) & 255;
                    // same here, equidistributed
//                    b1 = ((b1 << 5 | b1 >>> 3) + b0 ^ k) & 255;
//                    b0 = b0 + ((b0 << 2 | b0 >>> 6) ^ b1) & 255;

                    // including `b0 +=` or `b0 ^=` in the first block makes this not equidistributed; otherwise it is.
                    b1 = (rotate8(b1, 5) + b0 ^ k) & 255;
                    b0 = (rotate8(b0, 2) ^ b1) & 255;
                    b0 ^= rotate8(b0, 1) ^ rotate8(b0, 3);
                    b1 = (rotate8(b1, 5) + b0 ^ k) & 255;
                    b0 = (rotate8(b0, 2) ^ b1) & 255;

//                    b0 |= b1 << 8;
                    all.add(b0);
                    counts[b0]++;
                }
            }
        }
        System.out.println("APPEARANCE COUNTS:");
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }

        System.out.println(all.getCardinality() + "/" + 0x100L + " outputs were present.");
        System.out.println(100.0 - all.getCardinality() * 0x64p-8 + "% of outputs were missing.");
    }

    /**
     * Produces each byte with equal frequency, 256 times each.
     */
    @Test
    public void testFermatResidueLow()
    {
        short[] counts = new short[256];
        for (int a = 0; a < 0x10000; a++) {
                counts[a - (a >>> 8) & 255]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Only produces 0xFF once; all other bytes are produced 257 times each.
     */
    @Test
    public void testFermatResidueHigh()
    {
        short[] counts = new short[256];
        int sum = 0;
        for (int a = 0; a < 0x10000; a++) {
                counts[a - (a >>> 8) >>> 8 & 255]++;
                sum += a - (a >>> 8) >>> 8 & 255;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("Sum of all generated values: " + sum);
    }

    /**
     * Quite surprisingly, the lower 8 bits of this multiply-xor-rotate-multiply--xor-rotate-multiply thing are
     * equidistributed. The upper 8 bits are not.
     */
    @Test
    public void testXRRMulLo()
    {
        short[] counts = new short[256];
        int sum = 0;
        for (int a = 0; a < 0x10000; a++) {
//            int n = (a * 0xACE5 ^ rotl16(a, 3) * 0xBA55 ^ rotl16(a, 12) * 0xDE4D) & 255;
//            int n = (a * 0xDE4D ^ rotl16(a, 3) * 0xDE4D ^ rotl16(a, 12) * 0xDE4D) & 255;
            int n = (a * 0xACE5 ^ rotl16(a, 3) * 0xBA55 ^ rotl16(a, 12) * 0xDE4D) & 255;
//            int n = (a * 0xACE5 ^ rotl16(a, 3) ^ rotl16(a, 12)) & 255;
            counts[n]++;
            sum += n;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("Sum of all generated values: " + sum);
        System.out.println("Optimal sum                : " + (255 * 65536 / 2));
    }

    @Test
    public void testXRRMulHi()
    {
        short[] counts = new short[256];
        int sum = 0;
        for (int a = 0; a < 0x10000; a++) {
//            int n = (a * 0xDE4D ^ rotl16(a, 3) * 0xDE4D ^ rotl16(a, 12) * 0xDE4D) >>> 8 & 255;
//            int n = (a ^ rotl16(a * 0xBA55, 3) ^ rotl16(a * 0xDE4D, 12)) >>> 8 & 255;
            int n = (a * 0xACE5 ^ rotl16(a, 3) * 0xBA55 ^ rotl16(a, 12) * 0xDE4D) >>> 8 & 255;
//            int n = (a * 0xACE5 ^ rotl16(a, 3) ^ rotl16(a, 12)) >>> 8 & 255;
            counts[n]++;
            sum += n;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("Sum of all generated values: " + sum);
        System.out.println("Optimal sum                : " + (255 * 65536 / 2));
    }


    @Test
    public void testXRRMulSpin()
    {
        short[] counts = new short[256];
        int sum = 0;
        for (int a = 0; a < 0x100; a++) {
//            int n = (a * 0xDE4D ^ rotl16(a, 3) * 0xDE4D ^ rotl16(a, 12) * 0xDE4D) >>> 8 & 255;
//            int n = (a ^ rotl16(a * 0xBA55, 3) ^ rotl16(a * 0xDE4D, 12)) >>> 8 & 255;
//            int b = rotate8(a, 4);
//            int n = ((a * 41 ^ rotate8(a, 3) * 53 ^ rotate8(a, 6) * 67) ^ (b * 41 ^ rotate8(b, 3) * 53 ^ rotate8(b, 6) * 67)) & 0xFF;
            int n = ((a ^ rotate8(a, 3) ^ rotate8(a, 6)) * 67) & 0xFF;
//            int n = (a * 0xACE5 ^ rotl16(a, 3) ^ rotl16(a, 12)) >>> 8 & 255;
            counts[n]++;
            sum += n;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
        System.out.println("Sum of all generated values: " + sum);
        System.out.println("Optimal sum                : " + (255 * 65536 / 2));
    }

    /**
     * 60.73455810546875% of possible outputs were not produced.
     * Not a good sign for Wyrand.
     */
    @Test
    public void testWyrand16()
    {
        int[] counts = new int[65536];
        int a = 0, b, x;
        for (int i = 0; i < 65536; i++) {
            a = a + 0x642F & 0xFFFF;
            b = a ^ 0x28DB;
            x = a * b;
            x ^= x >>> 16;
            counts[x & 0xFFFF]++;
        }
        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%04X ", counts[i++]);
            }
            System.out.println();
        }
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible outputs were not produced.");
    }
    /**
     * 60.73455810546875% of possible outputs were not produced, exactly the same as non-safe.
     * Not a good sign for Wyrand.
     */
    @Test
    public void testWyrandSafe16()
    {
        int[] counts = new int[65536];
        int a = 0, b, x;
        for (int i = 0; i < 65536; i++) {
            a = a + 0x642F & 0xFFFF;
            b = a ^ 0x28DB;
            x = a * b;
            x ^= x >>> 16 ^ a ^ b;
            counts[x & 0xFFFF]++;
        }
        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
//                System.out.printf("%04X ", counts[i]);
            }
//            System.out.println();
        }
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible outputs were not produced.");
    }

    /**
     * OK, this is a surprise. So XORing a counter and an LFSR gives you a period that is (counter period) times (LFSR
     * period), which here is 256 times 255, or 0xFF00 . The strange part is that almost 1/5 possible subsequent pairs
     * of results can't be produced by the aforementioned sequence... Even though over the full period, each individual
     * byte result appears equally often, 255 times.
     * <br>
     * 18.218994140625% of pairs of possible outputs were not produced.
     */
    @Test
    public void testNemesis16()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 1;
        int x = 1;
        for (int i = 0; i < 0xFF00; i+=2) {
            a += (byte) 0x97;
            b = (byte) (b << 1 ^ (b >> 31 & 0x1D));
            smallCounts[((a ^ b) & 255)]++;
            counts[x = (x << 8 | ((a ^ b) & 255)) & 0xFFFF]++;
            a += (byte) 0x97;
            b = (byte) (b << 1 ^ (b >> 31 & 0x1D));
            smallCounts[((a ^ b) & 255)]++;
            counts[x = (x << 8 | ((a ^ b) & 255)) & 0xFFFF]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                System.out.printf("%04X ", counts[i]);
            }
            System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible outputs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testNomnom16()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 0;
        int x = 0;
        for (int i = 1; i <= 0x10000; i++) {
//            a = (byte)(a * 0x8D + 0x9B);
//            a = (byte) -(a ^ (a * a | 0x5));
//            b = (byte) (-(clz8(a) + b ^ (b * b | 0x5)));

            // good option; 50.3509521484375% of possible outputs were not produced.
//            a = (byte)(a * 0x9B ^ 0x8D);
//            b = (byte) (clz8(a)-(b ^ (b * b | 0x5)));

            // probably faster, also good option; 50.32958984375% of possible outputs were not produced.
//            a = (byte)(a * 0x9B ^ 0x9D);
//            b = (byte)(b * 0x8D + clz8(a));

            // best so far; 50.274658203125% of possible outputs were not produced.
            a = (byte)(a * 0x8D + 0xAB);
            b = (byte)((b + clz8(a)) * 0x9B ^ 0x9D);

            smallCounts[((rotate8(a, 2) ^ b) & 255)]++;
            counts[x = (x << 8 | ((rotate8(a, 2) ^ b) & 255)) & 0xFFFF]++;
            if((a|b) == 0){
                System.out.println("Cycled at " + i);
                break;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
//                System.out.printf("%04X ", counts[i]);
            }
//            System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible outputs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 36.33575439453125% of possible pairs were not produced.
     */
    @Test
    public void testSpun16()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 0;
        int accum = 0;
        for (int i = 1; i <= 0x10000; i++) {

            byte x = a, y = b;
            a = (byte)(x * 0x35 + 0xC3);
            b = (byte)(y * 0xD5 + clz8(x));
//            a = (byte)-(x ^ (x * x | 23));
//            b = (byte)(y + 0xD5 + clz8(x));

//            smallCounts[(x+y) & 255]++;
//            counts[accum = (accum << 8 | ((x+y) & 255)) & 0xFFFF]++;
            smallCounts[(rotate8(x, 2) + y ^ rotate8(y, 5)) & 255]++;
            counts[accum = (accum << 8 | ((rotate8(x, 2) + y ^ rotate8(y, 5)) & 255)) & 0xFFFF]++;
            if((a|b) == 0){
                System.out.println("Cycled at " + i);
                break;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
//                System.out.printf("%04X ", counts[i]);
            }
//            System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible pairs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 40.362548828125% of possible pairs were not produced.
     * Simpler transition between states, but a Cantor pairing function for output.
     * OR
     * 0.39215087890625% of possible pairs were not produced.
     * Using a similar simple transition between states, but just summing the states for output.
     */
    @Test
    public void testPoder16()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 0;
        int accum = 0;
        for (int i = 1; i <= 0x10000; i++) {

            byte x = a, y = b;
            a = (byte)(x + 0xC5);
            // the next one should work well on GWT. It does a XOR last, which makes sure b is in int range.
            b = (byte)(x ^ (y + clz8(x)));
//            b = (byte)(y + (x ^ clz8(x))); // seems best for pairs produced
//            b = (byte)(y + x + clz8(x));
            // could use this b instead; also full-period.
//            b = (byte)(y + clz8(x));

            // with (y+x+clz), 35.8184814453125% of possible pairs were not produced.
            // with y+(x^clz), 35.5804443359375% of possible pairs were not produced.
            // with x^(y+clz), 36.3739013671875% of possible pairs were not produced.
            // with (y+clz)  , 68.06182861328125% of possible pairs were not produced.
//            smallCounts[(rotate8(x, 2) + y ^ rotate8(y, 5)) & 255]++;
//            counts[accum = (accum << 8 | ((rotate8(x, 2) + y ^ rotate8(y, 5)) & 255)) & 0xFFFF]++;
            // with (y+x+clz), 8.59375% of possible pairs were not produced.
            // with y+(x^clz), 7.03125% of possible pairs were not produced.
            // with x^(y+clz), 31.66961669921875% of possible pairs were not produced.
            // with (y+clz)  , 95.70159912109375% of possible pairs were not produced.
//            smallCounts[(rotate8(x, 3) + y) & 255]++;
//            counts[accum = (accum << 8 | ((rotate8(x, 3) + y) & 255)) & 0xFFFF]++;
            // with (y+x+clz), 12.07122802734375% of possible pairs were not produced.
            // with y+(x^clz), 11.31439208984375% of possible pairs were not produced.
            // with x^(y+clz), 7.57293701171875% of possible pairs were not produced.
            // with (y+clz)  , 79.98809814453125% of possible pairs were not produced.
//            smallCounts[(x ^ y) & 255]++;
//            counts[accum = (accum << 8 | ((x ^ y) & 255)) & 0xFFFF]++;
            // with (y+x+clz), 3.12652587890625% of possible pairs were not produced.
            // with y+(x^clz), 0.39215087890625% of possible pairs were not produced.
            // with x^(y+clz), 9.62982177734375% of possible pairs were not produced.
            // with (y+clz)  , 96.48284912109375% of possible pairs were not produced.
            smallCounts[(x + y) & 255]++;
            counts[accum = (accum << 8 | ((x + y) & 255)) & 0xFFFF]++;
            // works
//            smallCounts[(a + ((b * (b + 1) & 255) >>> 3)) & 255]++;
//            counts[accum = (accum << 8 | ((a + ((b * (b + 1) & 255) >>> 3)) & 255)) & 0xFFFF]++;
            // also works
//            smallCounts[(a + ((b * (b + 1) & 255) >>> 1)) & 255]++;
//            counts[accum = (accum << 8 | ((a + ((b * (b + 1) & 255) >>> 1)) & 255)) & 0xFFFF]++;

            if((a|b) == 0){
                System.out.println("Cycled at " + i);
                break;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
//                System.out.printf("%04X ", counts[i]);
            }
//            System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible pairs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 36.30218505859375% of possible output pairs were not produced.
     * However, every (a,b) pair is encountered once, and every (q,r) potential output state is encountered once.
     */
    @Test
    public void testSpangledSmall16Pairing()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 1;
        int q, r;
        int x = 1;
        for (int i = 0; i <= 0xFFFF; i++) {
            q = (a += (byte) 0x97) & 255;
            if(a != 0) b += (byte)0xA5;
            r = b & 255;
//            r = (b += 0x65) & 255;

//            for (int j = 0, key = 40; j < 7; key = (key ^ key >>> 1) + key + ++j) {
//                r = (r + rotate8(q, 5)) & 255;
//                q = (rotate8(q, 6) + r ^ key) & 255;
//                r = (rotate8(r, 3) ^ q) & 255;
//            }

            for (int j = 0, key = 40; j < 9; j++, key++) {
                q = (rotate8(q, 6) + r ^ key) & 255;
                r = (rotate8(r, 3) ^ q) & 255;
            }

            smallCounts[(r)]++;
            counts[x = (x << 8 | (r)) & 0xFFFF]++;
            // every state is encountered, and that allows every q and r value to occur together once for each pair
//            counts[(q << 8 | (r)) & 0xFFFF]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible output pairs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 36.75689697265625% of possible output pairs were not produced.
     */
    @Test
    public void testBannerSmall16Pairing()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 1;
        int q, r;
        int x = 1;
        for (int i = 0; i <= 0xFFFF; i++) {
            q = (a += (byte) 0x99) & 255;
            if(a != 0) b += 0x75;
            r = b & 255;
//            r = (b += 0x65) & 255;
//            for (int j = 0; j < 2; ++j) {
//                q = (rotate8(q, 7) ^ r) & 255;
//                r = (rotate8(r, 2) + q) & 255;
//                q = (rotate8(q, 3) ^ r) & 255;
//            }

//            r = (rotate8(r, 2) ^ q) & 255;
//            q = (rotate8(q, 7) ^ r) & 255;
//            r = (rotate8(r, 1) + q) & 255;
//            q = (rotate8(q, 3) + r) & 255;
//            r = (rotate8(r, 5) ^ q) & 255;

//            q = (rotate8(r, 2) + q) & 255;
            r = (rotate8(q, 5) ^ r) & 255;
            q = (rotate8(r, 2) ^ q) & 255;
            r = (rotate8(q, 5) + r) & 255;
            q = (rotate8(r, 2) + q) & 255;
            r = (rotate8(q, 5) ^ r) & 255;




            smallCounts[(r)]++;
            counts[x = (x << 8 | (r)) & 0xFFFF]++;
            // every state is encountered, and that allows every q and r value to occur together once for each pair
//            counts[(q << 8 | (r)) & 0xFFFF]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible output pairs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 89.84375% of possible outputs were not produced.
     */
    @Test
    public void testOrbitty16Pairing()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 1;
        int x = 1;
        for (int i = 0; i <= 0xFFFF; i++) {
            a += (byte) 0x97;
            if(a == 0) b += 0x65;
            smallCounts[((a ^ b) & 255)]++;
            counts[x = (x << 8 | ((a ^ b) & 255)) & 0xFFFF]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible outputs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 0.0% of possible full states were not entered.
     */
    @Test
    public void testOrbitty16FullState()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 1;
        for (int i = 0; i <= 0xFFFF; i++) {
            a += (byte) 0x97;
            b += (byte)((a | 0x1A - a) >> 31 & 0x65);
//            if(a == 0) b += 0x65;
            smallCounts[((a ^ b) & 255)]++;
            counts[(a & 255) << 8 | (b & 255)]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible full states were not entered.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testFried16FullState()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte a = 0, b = 0;
        for (int i = 0; i <= 0xFFFF; i++) {
//            a += (byte) 0x97;
            // this will fail to enter 50% of all 2 to the 16 possible states.
//            b += (byte)(rotate8(a+1^a, 1));
            // this will fail to enter 0% of all 2 to the 16 possible states.
//            b += (byte)((a | 0x1A - a) >>> 31);
            // this will fail to enter 99.21875% of all 2 to the 16 possible states.
//            b += (byte)(a ^ rotate8(a, 3) ^ rotate8(a, 5));
            // this will fail to enter 99.609375% of all 2 to the 16 possible states.
//            b += 1;
            // this will fail to enter 0% of all 2 to the 16 possible states.
//            b += clz8(a);

            // this will fail to enter 0% of all 2 to the 16 possible states.
            a = (byte) (a * 0xC3 ^ 0x95);
            b = (byte) (clz8(a) - (b ^ (b * b | 7)));
            smallCounts[((a + rotate8(b, 2)) & 255)]++;
            counts[(a & 255) << 8 | (b & 255)]++;
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible full states were not entered.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testLilith24States() {
        final RoaringBitmap all = new RoaringBitmap();
        int a = 0, b = 0, c = 0, q, r, s;
        for (int i = 0; i < 0x1000000; i++) {
//            q = a = a * 0x1D + 0x75 & 255;
//            r = b = b * 0x35 + clz8(a) & 255;
//            s = c = c * 0xA5 + clz8(a&b) & 255;

            q = a = (a + 0x75) & 255;
            r = b = (a ^ b + clz8(a)) & 255;
            s = c = (b ^ c + clz8(a & b)) & 255;

//            r = r + (rotate8(q, 7) ^ s) & 255;
//            r = r + (rotate8(s, 3) ^ q) & 255;
//            s = s ^ (rotate8(q, 1) + r) & 255;
//            s = s ^ (rotate8(r, 5) + q) & 255;

            q = (rotate8(q, 6) + r ^ s) & 255;
            r = (rotate8(r, 3) ^ q) & 255;

//            q = (rotate8(q, 6) + r ^ (s = s + 3 & 255)) & 255;
//            r = (rotate8(r, 3) ^ q) & 255;

            all.add(q << 16 | r << 8 | s);
        }

        System.out.println(all.getCardinality() + "/" + 0x1000000L + " outputs were present.");
        System.out.println(100.0 - all.getCardinality() * 0x64p-24 + "% of outputs were missing.");
    }

    /**
     * 92.87109375% of possible full states were not entered.
     */
    @Test
    public void testBrycReducedCoverage()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte h1, h2;
        for (int a = 0; a <= 0xFF; a++) {
            for (int b = 0; b <= 0xFF; b++) {
                h1 = (byte) a;
                h2 = (byte) b;
                h1 ^= (h1 ^ ((h2 & 255) >>> 3)) * 0x77 & 255;
                h2 ^= (h2 ^ ((h1 & 255) >>> 3)) * 0xc9 & 255;
                h1 ^= (h2 & 255) >>> 4 & 255;
                h2 ^= (h1 & 255) >>> 4 & 255;

                smallCounts[(h2 & 255)]++;
                counts[(h1 & 255) << 8 | (h2 & 255)]++;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible full states were not entered.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 0.0% of possible full states were not entered.
     */
    @Test
    public void testBryc2ReducedCoverage()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte h1, h2;
        for (int a = 0; a <= 0xFF; a++) {
            for (int b = 0; b <= 0xFF; b++) {
                h1 = (byte) a;
                h2 = (byte) b;
                h1 ^= (h2 ^ ((h2 & 255) >>> 3)) * 0x77 & 255;
                h2 ^= (h1 ^ ((h1 & 255) >>> 3)) * 0xc9 & 255;
                h1 ^= (h2 & 255) >>> 4 & 255;
                h2 ^= (h1 & 255) >>> 4 & 255;

                smallCounts[(h2 & 255)]++;
                counts[(h1 & 255) << 8 | (h2 & 255)]++;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible full states were not entered.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * 0.0% of possible full states were not entered.
     */
    @Test
    public void testBryc2ReverseReducedCoverage()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte h1, h2;
        for (int a = 0; a <= 0xFF; a++) {
            for (int b = 0; b <= 0xFF; b++) {
                h1 = (byte) a;
                h2 = (byte) b;

                h1 ^= (h2 ^ ((h2 & 255) >>> 3)) * 0x77 & 255;
                h2 ^= (h1 ^ ((h1 & 255) >>> 3)) * 0xc9 & 255;
                h1 ^= (h2 & 255) >>> 4 & 255;
                h2 ^= (h1 & 255) >>> 4 & 255;


                h2 ^= (h1 & 255) >>> 4 & 255;
                h1 ^= (h2 & 255) >>> 4 & 255;
                h2 ^= (h1 ^ ((h1 & 255) >>> 3)) * 0xc9 & 255;
                h1 ^= (h2 ^ ((h2 & 255) >>> 3)) * 0x77 & 255;

                Assert.assertEquals("Failed with a="+a+", b="+b+", h1="+h1+", h2="+h2, a, h1&255);
                Assert.assertEquals("Failed with a="+a+", b="+b+", h1="+h1+", h2="+h2, b, h2&255);

                smallCounts[(h2 & 255)]++;
                counts[(h1 & 255) << 8 | (h2 & 255)]++;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible full states were not entered.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testAXRB()
    {
        int[] smallCounts = new int[256];
        int[] counts = new int[65536];
        byte h1 = 0, h2 = 0;
        int o = 0, pair = 0;
        for (int a = 0; a <= 0xFF; a++) {
            for (int b = 0; b <= 0xFF; b++) {
                h1 += 0x75;
                h2 += 0x9B + clz8(h1);
//                h2 += 0x9B;
//                if(h1 == 0) h2++;
                //81.9976806640625% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 3);
                //74.5819091796875% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 1);
                //79.107666015625% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 2);
                //81.9976806640625% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 3);
                //75.1556396484375% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 4);
                //77.923583984375% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 5);
                //80.755615234375% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 6);
                //78.509521484375% of possible pairs were not produced. (CLZ)
//                o = h1 ^ rotate8(h2, 7);

                //45.5841064453125% of possible pairs were not produced. (CLZ)
                o = h1 ^ h2 ^ rotate8(h2, 1) ^ rotate8(h2, 4);
                //56.71844482421875% of possible pairs were not produced.
//                o = h1 + (h2 ^ rotate8(h2, 1) ^ rotate8(h2, 4));

                //95.5413818359375% of possible pairs were not produced. (CLZ)
//                o = h1 + rotate8(h2, 3);
                //76.6448974609375% of possible pairs were not produced. (CLZ)
//                o = h1 ^ h2;
                //96.484375% of possible pairs were not produced. (CLZ)
//                o = h1 + h2;
                //62.493896484375% of possible pairs were not produced. (CLZ)
                //85.56365966796875% of possible pairs were not produced. (if)
//                o = (rotate8(h2, 3) ^ (rotate8(h1, 6) + h2 ^ 5)) & 255;

                smallCounts[(o & 255)]++;
                counts[pair = (pair & 255) << 8 | (o & 255)]++;
//                counts[(h1 & 255) << 8 | (h2 & 255)]++;
            }
        }

        int zeroes = 0;
        for (int y = 0, i = 0; y < 2048; y++) {
            for (int z = 0; z < 32; z++, i++) {
                if(counts[i] == 0)
                    zeroes++;
                if(y < 32)
                    System.out.printf("%04X ", counts[i]);
            }
            if(y < 32)
                System.out.println();
        }
        System.out.println();
        System.out.println((zeroes * 0x1p-16 * 100.0) + "% of possible pairs were not produced.");
        System.out.println();
        for (int y = 0, i = 0; y < 8; y++) {
            for (int z = 0; z < 32; z++, i++) {
                System.out.printf("%04X ", smallCounts[i]);
            }
            System.out.println();
        }

//        zeroes = 0;
//        for (int y = 0, i = 0; y < 8; y++) {
//            for (int z = 0; z < 32; z++, i++) {
//                if(smallCounts[i] == 0)
//                    zeroes++;
//                System.out.printf("%04X ", smallCounts[i]);
//            }
//            System.out.println();
//        }

    }


    @Test
    public void testTriState()
    {
        int[] smallCounts = new int[256];
        byte h1 = 0, h2 = 0, h3 = 0;
        int o = 0;
        for (int a = 0; a <= 0xFFFFFF; a++) {
                byte n = h1 += 0x65;
                n &= (h2 += (0x91) + clz8(h1));
                h3 += (0x7C) + clz8(n);
                o = (h3 ^ n);
                smallCounts[(o & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%06X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testVarState()
    {
        final int VAR_LEN = 5;

        long[] smallCounts = new long[256];
        byte[] h = new byte[VAR_LEN];
        byte n;
        final int lim = h.length - 1, pairLim = lim - 1;
        final long iterations = 1L << (VAR_LEN << 3);
        for (long a = 0; a < iterations; a++) {
            n = h[0] += 0x65;
            byte ctr = 0x35;
            int i = 1;
            for (; i < pairLim;) {
                n |= (h[i++] += (ctr += 0x3A) ^ Integer.numberOfLeadingZeros(n & 255) - 24);
                n &= (h[i++] += (ctr += 0x96) ^ Integer.numberOfLeadingZeros(n & 255) - 24);
            }
            if(i < lim)
                n |= (h[i] += (ctr += 0x3A) ^ Integer.numberOfLeadingZeros(n & 255) - 24);
            n ^= h[lim] += (ctr + 0xAE) ^ Integer.numberOfLeadingZeros(n & 255) - 24;
            // at this point, a random number generator should run n through a unary hash.
            smallCounts[(n & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This is full-period (2 to the 32 outputs, 32 bits of state).
     */
    @Test
    public void testGarbanzoDistribution()
    {

        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        final long iterations = 1L << 32;
        for (long a = 0; a < iterations; a++) {
            byte n, x, y, z, w;
            n = (byte)((x = (stateA += 0xBB)));
            n = (byte)((y = (stateB += 0xF5 ^ Integer.numberOfLeadingZeros((x     ) & 255))) + (n ^ rotate8(n, 3) ^ rotate8(n, 6)));
            n = (byte)((z = (stateC += 0x2D ^ Integer.numberOfLeadingZeros((x &= y) & 255))) + (n ^ rotate8(n, 2) ^ rotate8(n, 1)));
            n = (byte)((w = (stateD += 0xA5 ^ Integer.numberOfLeadingZeros((x &= z) & 255))) + (n ^ rotate8(n, 5) ^ rotate8(n, 4)));
            n = (byte)(x + (n ^ rotate8(n, 4) ^ rotate8(n, 7)));
            smallCounts[(n & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This is, somehow, also full-period (2 to the 32 outputs, 32 bits of state).
     */
    @Test
    public void testGabberDistribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        final long iterations = 1L << 32;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z, w;
            x = (stateA += (byte)(0xBB));
            y = (stateB += (byte)(x ^ rotate8(x, 3) ^ rotate8(x, 6) ^ Integer.numberOfLeadingZeros((x     ) & 255)));
            z = (stateC += (byte)(y ^ rotate8(y, 2) ^ rotate8(y, 1) ^ Integer.numberOfLeadingZeros((x &= y) & 255)));
            w = (stateD += (byte)(z ^ rotate8(z, 5) ^ rotate8(z, 4) ^ Integer.numberOfLeadingZeros((x &= z) & 255)));
            x ^= (byte)(w ^ rotate8(w, 4) ^ rotate8(w, 7));
            smallCounts[(x & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This is, somehow, also full-period (2 to the 32 outputs, 32 bits of state).
     */
    @Test
    public void testGoblinDistribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        final long iterations = 1L << 32;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z, w;
            x = (stateA += (byte)(0xBD));
            y = (stateB += (byte)(x + clz8(x     )));
            z = (stateC += (byte)(y + clz8(x &= y)));
            w = (stateD += (byte)(z + clz8(x &= z)));
            // equidistributed
            smallCounts[(x + w & 255)]++;
//            x = (stateA += (byte)(0xBD));
//            y = (stateB += (byte)(rotate8(x, 3) + 0x65 * clz8(x     )));
//            z = (stateC += (byte)(rotate8(y, 3) + 0x55 * clz8(x &= y)));
//            w = (stateD += (byte)(rotate8(z, 3) + 0x45 * clz8(x &= z)));
//            // equidistributed
//            smallCounts[(w & 255)]++;
//            x = (stateA += (byte)(0xBD));
//            y = (stateB += (byte)(rotate8(x, 3) * clz8(x     )));
//            z = (stateC += (byte)(rotate8(y, 3) * clz8(x &= y)));
//            w = (stateD += (byte)(rotate8(z, 3) * clz8(x &= z)));
//            // NOT equidistributed, but not by much
//            smallCounts[(x + w & 255)]++;
//            x = (stateA += (byte)(0xBD));
//            y = (stateB += (byte)(rotate8(x, 3) + clz8(x     )));
//            z = (stateC += (byte)(rotate8(y, 3) + clz8(x &= y)));
//            w = (stateD += (byte)(rotate8(z, 3) + clz8(x &= z)));
//            // equidistributed
//            smallCounts[(w & 255)]++;
            // equidistributed
//            x += (byte)(w ^ rotate8(w, 3) ^ rotate8(w, 7));
//            smallCounts[(x & 255)]++;
            // equidistributed
//            x ^= (byte)(rotate8(w, 3));
//            smallCounts[(x & 255)]++;
            // fails entirely, NOT equidistributed
//            y += x ^ rotate8(w, 3);
//            z += w ^ rotate8(y, 3);
            // equidistributed
//            y += x ^ rotate8(w, 3);
//            z += rotate8(y, 3);
//            smallCounts[(z & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This is, somehow, again, also full-period (2 to the 32 outputs, 32 bits of state).
     */
    @Test
    public void testBrisketDistribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        final long iterations = 1L << 32;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z, w;
            x = (stateA += (byte)(0xDB));
            y = (stateB += (byte)((x|1) * (Integer.numberOfLeadingZeros((x     ) & 255) + 0x33 & 255)));
            z = (stateC += (byte)((y|1) * (Integer.numberOfLeadingZeros((x &= y) & 255) + 0x75 & 255)));
            w = (stateD += (byte)((z|1) * (Integer.numberOfLeadingZeros((x &= z) & 255) + 0x85 & 255)));
            w ^= (byte)(rotate8(w, 5) ^ rotate8(w, 7));
            smallCounts[(w & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This is, somehow, again, also full-period (2 to the 32 outputs, 32 bits of state).
     */
    @Test
    public void testBlanketDistribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        final long iterations = 1L << 32;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z, w;
            x = (stateA += (byte)(0xDB));
            y = (stateB += (byte)((x) * (clz8(x     ) + 0x33 & 255)));
            z = (stateC += (byte)((y) * (clz8(x &= y) + 0x75 & 255)));
            w = (stateD += (byte)((z) * (clz8(x &= z) + 0x85 & 255)));
            smallCounts[(w & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * Equidistributed and full-period.
     */
    @Test
    public void testSimplishDistribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0, stateC = 0;
        final long iterations = 1L << 24;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z;
            x = (stateA = (byte)(stateA + 0xC5));
            y = (stateB = (byte)(stateB + x + (clz8(x    ))));
            z = (stateC = (byte)(stateC + y + (clz8(x & y))));
            smallCounts[((rotate8(z, 2) ^ rotate8(y, 5) + z ^ x) & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * Equidistributed and full-period.
     */
    @Test
    public void testSimplish16Distribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {
            byte x, y;
            x = (stateA = (byte)(stateA + 0xC5));
            y = (stateB = (byte)(stateB + (clz8(x    ))));
//            y = (stateB = (byte)(stateB + (x + 1 + rotate8(x, 1) & 255)));
            smallCounts[((rotate8(x, 2) ^ rotate8(y, 5) + x ^ 107) & 255)]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * This combination takes some trial and error if you adjust it, but it doesn't need clz or other "exotic" functions
     * to work.
     */
    @Test
    public void testEasierPairing16()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {
            int x, y, result;
//            x = (stateA = (byte)(stateA + 0xC5 ^ 0x96)) & 255;
            // I was surprised to see all the following three lines work, and are full-period!
//            y = (stateB = (byte)(stateB + clz8(x) ^ x * 0xBD)) & 255;
//            y = (stateB = (byte)(stateB + clz8(x) ^ x)) & 255;
//            y = (stateB = (byte)(stateB + clz8(x) + 0xBD ^ x)) & 255;

//            y = (y ^ rotate8(y, 3) ^ rotate8(y, 6)) + (x ^= rotate8(x, y) ^ rotate8(x, ~y)) & 255;
//            result = (x ^ rotate8(x, 2) ^ rotate8(x, 7)) + (y ^ rotate8(y, x) ^ rotate8(y, ~x)) & 255;
//            result = y + rotate8(x, 7 - y) & 255;
//            result = (result ^ result >>> 6) * 0xBD & 255;
//            result = (result ^ rotate8(result, 5) ^ rotate8(result, 2));

//            x = (stateA = (byte)(stateA + 0xC5 ^ 0x96)) & 255;
//            result = x & (0xDA - x & 255);
//            y = (stateB = (byte)(stateB + rotate8(result, 1) ^ 0xD6)) & 255;
            x = (stateA = (byte)(stateA + 0xC5 ^ 0x96)) & 255;
            result = x & (0xDA - x & 255);
            y = (stateB = (byte)(stateB + rotate8(result, 1) ^ x)) & 255;
            x = x + (y ^= rotate8(y, x) ^ rotate8(y, 7 - x)) & 255;
            y = y + (x ^= rotate8(x, y) ^ rotate8(x, 7 - y)) & 255; // works with x ^= or just x ^
            // both of the following work
            result = x ^ rotate8(y, 5);
//            result = y ^ y >>> 3;

//
//            y = y + rotate8(x, y) & 255;
//            y = (y ^ y >>> 6) * 0xBD & 255;
//            result = (y ^ rotate8(y, 5) ^ rotate8(y, 2));

//            result = y ^ (y >>> (y >>> 6) + 2);
//            result = y ^ rotate8(x, 3);

            smallCounts[result & 255]++;
            if(stateA == 0 && stateB == 0) break;

            if(a < 64){
                System.out.printf("x = 0x%02X , y = 0x%02X, result = 0x%02X\n", x, y, result);
            }
        }
        System.out.println();
        long minCount = Integer.MAX_VALUE, maxCount = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                long c = smallCounts[i];
                minCount = Math.min(minCount, c);
                maxCount = Math.max(maxCount, c);
                System.out.printf("%09X ", c);
            }
            System.out.println();
        }
        System.out.println("Min count was " + minCount);
        System.out.println("Max count was " + maxCount);
    }

    @Test
    public void testPartyRandom()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {


            int result;

//            stateB += 0xD3;
//            stateA += 0x65 + clz8(stateB);
//            int t = rotate8(stateA, 5) ^ stateB, s = rotate8(t, 6) + stateB;
//            t = (s ^ rotate8(stateB, 3)) & 255;
//            result = (rotate8(s, 6) + t & 255) ^ rotate8(t, 3);

            int t = rotate8(stateA, 5) ^ (stateB += 0xD3), s = rotate8(t, 6) + stateB;
            stateA = (byte)(s ^ rotate8(stateB, 3));
            result = (rotate8(s, 6) + stateA & 255) ^ rotate8(stateA, 3);

            smallCounts[result & 255]++;
            if(stateA == 0 && stateB == 0){
                System.out.printf("\nPeriod was cut short!\nPeriod is %d (0x%04X)\n", (a + 1), (a + 1));
                break;
            }

            if(a < 64){
                System.out.printf("x = 0x%02X , y = 0x%02X, result = 0x%02X\n", stateA, stateB, result);
            }
        }
        System.out.println();
        long minCount = Integer.MAX_VALUE, maxCount = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                long c = smallCounts[i];
                minCount = Math.min(minCount, c);
                maxCount = Math.max(maxCount, c);
                System.out.printf("%09X ", c);
            }
            System.out.println();
        }
        System.out.println("Min count was " + minCount);
        System.out.println("Max count was " + maxCount);
    }

    @Test
    public void testPartyRandom2()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                stateA = (byte) a;
                stateB = (byte) b;



                int result;

                int t = rotate8(stateA, 5) ^ (stateB += 0xD3), s = rotate8(t, 6) + stateB;
                stateA = (byte)(s ^ rotate8(stateB, 3));
                result = (rotate8(s, 6) + stateA & 255) ^ rotate8(stateA, 3);

                smallCounts[result & 255]++;

//                if(a < 64){
//                    System.out.printf("x = 0x%02X , y = 0x%02X, result = 0x%02X\n", stateA, stateB, result);
//                }

            }
        }
        System.out.println();
        long minCount = Integer.MAX_VALUE, maxCount = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                long c = smallCounts[i];
                minCount = Math.min(minCount, c);
                maxCount = Math.max(maxCount, c);
                System.out.printf("%09X ", c);
            }
            System.out.println();
        }
        System.out.println("Min count was " + minCount);
        System.out.println("Max count was " + maxCount);
    }

    public int taxon32(int stateA, int stateB){
        int x = stateA + 0x9E3779BD ^ 0xD1B54A32;
        int t = x & 0xDB4F0B96 - x;
        int y = stateB + (t << 1 | t >>> 31) ^ 0xAF723597;
        y += (x << y | x >>> 32 - y);
        y = (y ^ y >>> 22 ^ y << 5) * 0xB45ED;
        return y ^ y >>> 21;
    }

    /**
     * Nothing I do seems to get 2 to the 32 n values to get assigned to a pair of ints somehow and get 2 to the 32
     * distinct values returned by taxon32(). I suppose it's more random in that way, just not 2D equidistributed, which
     * I guess I already knew.
     */
    @Test
    public void testInitialSeedingTaxon32() {
        long total = 0L;
        for (int i = 0, n = 0x80000000; i < 0x10000; i++) {
            for (int j = 0; j < 0x10000; j++) {
                total += taxon32(n, ~n) & 0xFFFFFFFFL;
                ++n;
            }
        }
        System.out.printf("0x%016X should be 0x%016X\n", total, 0x80000000L);
    }


    @Test
    public void testMumVariants16()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {
            int x, y, result;
            x = (stateA = (byte)(stateA + 0xC5)) & 255;
            y = (stateB = (byte)(stateB + (clz8(x    )))) & 255;
            // equidistributed. 256/256
//            result = (rotate8(x, 2) ^ rotate8(y, 5) + x ^ 107);
            // very unbalanced
            // used in Yolk, which is what digital's Hasher class defaults to... 32/1280
//            result = (x ^ rotate8(y, 5)) * (y ^ rotate8(x, 5)) & 255;
//            result ^= result >>> 3;
            // kinda more unbalanced... 103/1280
//            result = (x - rotate8(y, 5)) * (y + rotate8(x, 5)) & 255;
//            result ^= result >>> 3;
            // 117/1120
//            result = (x - rotate8(y, 5)) * (y + rotate8(x, 3)) & 255;
//            result ^= result >>> 4;
            // 103/1280
//            result = (x - rotate8(y, 5)) * (y + rotate8(x, 5)) & 255;
//            result ^= result >>> 4;
            // equidistributed
            result = x - y;
            result = (result ^ (result * result | 1)) & 255;
            result ^= rotate8(result, 3) ^ rotate8(result, 6);

            // equidistributed... probably not enough mixing. 256/256
//            result = ((x ^ rotate8(x, 2) ^ rotate8(x, 6)) - (y ^ rotate8(y, 3) ^ rotate8(y, 5)));
            // what wyhash uses; rather unbalanced... 131/1485
//            result = x * y;
//            result ^= result >>> 8;
            smallCounts[result & 255]++;
        }
        System.out.println();
        long minCount = Integer.MAX_VALUE, maxCount = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                long c = smallCounts[i];
                minCount = Math.min(minCount, c);
                maxCount = Math.max(maxCount, c);
                System.out.printf("%09X ", c);
            }
            System.out.println();
        }
        System.out.println("Min count was " + minCount);
        System.out.println("Max count was " + maxCount);
    }

    public int roundFunction(int n) {
        n ^= rotate8(n, 2) ^ rotate8(n, 6);
        n = n + 179 & 255;
        return n ^ rotate8(n, 3) ^ rotate8(n, 5);
    }

    @Test
    public void testFeistel16Distribution()
    {
        long[] smallCounts = new long[256];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {
            byte x, y;
            x = (stateA = (byte)(stateA + 0xC5));
//            y = (stateB = (byte)(stateB + 0xBD));
            y = (stateB = (byte)(stateB + (clz8(x))));
            y = (byte) ((x) ^ roundFunction(x = y));
            x ^= roundFunction(y);

            smallCounts[x & 255]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testFeisty16Distribution()
    {
        long[] smallCounts = new long[65536];
        byte stateA = 0, stateB = 0;
        final long iterations = 1L << 16;
        for (long a = 0; a < iterations; a++) {
            byte x, y;
            // update style 1
            x = (stateA = (byte)(stateA + 0xC5 ^ 0xFA));
            y = (stateB = (byte)(stateB + clz8(x) ^ 0xBE));
            // update style 2
//            x = (stateA = (byte)(stateA + 0xC5));
//            y = (byte)(x + (stateB = (byte)(stateB + (clz8(x)))));
            // the change is in the next two lines. `x = y` can be changed to `x += y` or `x ^= y` without trouble.
            // It remains just as equidistributed.
//            y = (byte) ((x) ^ roundFunction(x += y));
//            x ^= roundFunction(x ^ y);

//            x = (byte)(y ^ ((x += (y ^ rotate8(y, 7) ^ rotate8(y, 6)) + 0x95) ^ rotate8(x, 3) ^ rotate8(x, 6)) + 0x95);

//            y = (byte) ((x) ^ (x = (byte)((y ^ rotate8(y, 54) ^ rotate8(y, 47)) + 0xB7)));

//            x = (byte) ((y) ^ (y += (byte)((x ^ rotate8(x, 25) ^ rotate8(x, 50)) + 0x95)));
//            y = (byte) ((x) ^ (x += (byte)((y ^ rotate8(y, 54) ^ rotate8(y, 47)) + 0xA1)));
//            x ^= (x + (y ^ rotate8(y, 25) ^ rotate8(y, 50)) + 0xDB);

            // OK, wow.
            // This desperate attempt to get FrostyRandom to be random enough from correlated initial states
            // actually turns out to be almost decorrelated enough to be unnoticeable (my test can't notice it,
            // but my eyes can). It also produces each x,y state pair, after all this processing, equally often.
            // This could return (x^y) or (x+y) without losing its 1D equidistribution. This could also be used
            // with a smaller word size (int) to produce larger words (long) as needed, but default to smaller.
//            y = (byte)((rotate8(y,  3) ^ (x = (byte)(rotate8(x, 56) + y ^ 0xBD))) + rotate8(x, 34));
//            x = (byte)((rotate8(x, 53) ^ (y = (byte)(rotate8(y, 26) + x ^ 0xDB))) + rotate8(y, 17));
//            y = (byte)((rotate8(y, 23) ^ (x = (byte)(rotate8(x, 46) + y ^ 0x95))) + rotate8(x, 20));
//            x = (byte)((rotate8(x, 13) ^ (y = (byte)(rotate8(y,  6) + x ^ 0xF5))) + rotate8(y, 57));

//            y = (byte)((rotate8(y,  3) + (x = (byte)(rotate8(x, 56) + y ^ 0xBD))) ^ rotate8(x, 34));
//            x = (byte)((rotate8(x, 53) + (y = (byte)(rotate8(y, 26) + x ^ 0xDB))) ^ rotate8(y, 17));
//            y = (byte)((rotate8(y, 23) + (x = (byte)(rotate8(x, 46) + y ^ 0x95))) ^ rotate8(x, 20));
//            x = (byte)((rotate8(x, 13) + (y = (byte)(rotate8(y,  6) + x ^ 0xF5))) ^ rotate8(y, 57));

//            y = (byte)((rotate8(y,  3) ^ (x = (byte)(rotate8(x, 56) + y ^ 0xBD))));
//            x = (byte)((rotate8(x, 53) ^ (y = (byte)(rotate8(y, 26) + x ^ 0xDB))));
//            y = (byte)((rotate8(y, 23) ^ (x = (byte)(rotate8(x, 46) + y ^ 0x95))));
//            x = (byte)((rotate8(x, 13) ^ (y = (byte)(rotate8(y,  6) + x ^ 0xF5))));
//
//            y += rotate8(x, 6);
//            x += rotate8(y, 3);
//            y ^= rotate8(x, 5);
//            x ^= rotate8(y, 2);
//            x ^= rotate8(x, y) ^ rotate8(x, rotate8(y, 3));
//            y ^= rotate8(y, x) ^ rotate8(y, rotate8(x, 3));

            // not a bijection!
//            y = (byte)(rotate8(x, 6) + rotate8(y, 3) ^ (x = (byte)(rotate8(x, 5) + y ^ 0xBD)));
//            x = (byte)(rotate8(y, 3) + rotate8(x, 5) ^ (y = (byte)(rotate8(y, 2) + x ^ 0xDB)));
//            y = (byte)(rotate8(x, 5) + rotate8(y, 2) ^ (x = (byte)(rotate8(x, 4) + y ^ 0x95)));
//            x = (byte)(rotate8(y, 2) + rotate8(x, 1) ^ (y = (byte)(rotate8(y, 6) + x ^ 0xF5)));

            // but rearrange it, and it's back to being bijective!
            // this style has XOR placed strategically so that it won't lose precision on GWT.
            y = (byte)(rotate8(y, 3) ^ (x = (byte)(rotate8(x, 5) + y ^ 0xBD)) + rotate8(x, 6));
            x = (byte)(rotate8(x, 5) ^ (y = (byte)(rotate8(y, 2) + x ^ 0xDB)) + rotate8(y, 3));
            y = (byte)(rotate8(y, 2) ^ (x = (byte)(rotate8(x, 4) + y ^ 0x95)) + rotate8(x, 5));
            x = (byte)(rotate8(x, 1) ^ (y = (byte)(rotate8(y, 6) + x ^ 0xF5)) + rotate8(y, 2));



            smallCounts[(y << 8 & 0xFF00)|(x & 0xFF)]++;
//            smallCounts[x & 255]++;
//            smallCounts[y & 255]++;
//            smallCounts[(x^y) & 255]++;
//            smallCounts[(x+y) & 255]++;

            // this one-liner mixing is equidistributed with either update style.
//            smallCounts[(y ^ roundFunction(y ^ (x ^ roundFunction(y)))) & 255]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testFeisty24Distribution()
    {
//        long[] smallCounts = new long[1 << 16];
        long[] smallCounts = new long[1 << 24];
        byte stateA = 0, stateB = 0, stateC = 0;
        final long iterations = 1L << 24;
        for (long a = 0; a < iterations; a++) {
            byte x, y, z;
            x = (stateA = (byte)(stateA + 0xC5 ^ 0xD6));
//            y = (stateB = (byte)(stateB + clz8(x) ^ 0x9A));
            int t = x | 0x5D + x; // look ma, no subtraction!
//            y = (stateB = (byte)(stateB + rotate8(t, 1) ^ 0xBA));
//            y = (stateB = (byte)(stateB + rotate8(t, 1) ^ 0xBA)); // this works and only uses the simplest ops!
            y = (stateB = (byte)(stateB + rotate8(t, 1) ^ 0xBA));
//            y = (stateB = (byte)(stateB + 0x91 + clz8(x)));
//            z = (byte) (stateC ^ x ^ y);
//            z = stateC;
//            z = (stateC = (byte)(stateC + 0xDB));
//            z = (stateC = (byte)(stateC + 0xDB + clz8(x&y)));
//            z = (stateC = (byte)(stateC + clz8(x&y) ^ 0xBE));
//            z = (stateC = (byte)(stateC + rotate8(t | y | -y, 1) ^ 0xAE));
            z = (stateC = (byte)(stateC + rotate8(t | y | 0x3B + y, 1) ^ 0xAE));
//            z = (stateC = (byte)(stateC + rotate8(t | y | t - y, 1) ^ 0xAE)); // surprisingly works!
//            z = (stateC = (byte)(stateC + rotate8((x&y) | -(x&y), 1)));

            // this style has XOR placed strategically so that it won't lose precision on GWT.
            y = (byte)(rotate8(y, 3) ^ (x = (byte)(rotate8(x, 5) + y ^ z)) + rotate8(x, 6));
            x = (byte)(rotate8(x, 5) ^ (y = (byte)(rotate8(y, 2) + x ^ z)) + rotate8(y, 3));
            y = (byte)(rotate8(y, 2) ^ (x = (byte)(rotate8(x, 4) + y ^ z)) + rotate8(x, 5));
            x = (byte)(rotate8(x, 1) ^ (y = (byte)(rotate8(y, 6) + x ^ z)) + rotate8(y, 2));

//            y = (byte)(rotate8(y, 2) ^ (x = (byte)(rotate8(x, 4) + y ^ z)) + rotate8(x, 5));
//            z = (byte)(rotate8(z, 1) ^ (y = (byte)(rotate8(y, 6) + z ^ x)) + rotate8(y, 2));
//            x = (byte)(rotate8(x, 5) ^ (z = (byte)(rotate8(z, 2) + x ^ y)) + rotate8(z, 3));

            final int index = (z << 16 & 0xFF0000)|(y << 8 & 0xFF00)|(x & 0xFF);
//            final int index = (y << 8 & 0xFF00)|(x & 0xFF);
            smallCounts[index]++;
//            smallCounts[x & 255]++;
//            smallCounts[y & 255]++;
//            smallCounts[(x^y) & 255]++;
//            smallCounts[(x+y) & 255]++;
        }
        System.out.println();
        for (int x = 0, i = 0; x < 256; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++, i++) {
                    System.out.printf("%09X ", smallCounts[i]);
                }
                System.out.println();
            }
            System.out.println('\n');
        }
    }

    /**
     * With the Coord.hashCode() version as-is:
     * 3621146300/4294967296 outputs were present.
     * 15.688617620617151% of outputs were missing.
     * <br>
     * With Rosenberg-Strong instead of Cantor:
     * 4294967296/4294967296 outputs were present.
     * 0.0% of outputs were missing.
     */
    @Test
    public void test32BitPointHash()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int state = 0;
        for (int x = -0x8000; x < 0x8000; x++) {
            for (int y = -0x8000; y < 0x8000; y++) {
                // the signs for x and y; each is either -1 or 0
                int xs = x >> 31, ys = y >> 31;
                // makes mx equivalent to -1 ^ this.x if this.x is negative
                int mx = x ^ xs;
                // same for my
                int my = y ^ ys;
                // Cantor pairing function, and XOR with every odd-index bit of xs and every even-index bit of ys
                // this makes negative x, negative y, positive both, and negative both all get different bits XORed or not
//                my = my + ((mx + my) * (mx + my + 1) >>> 1) ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555);
//                state = ((mx >= my ? mx * (mx + 2) - my : my * my + mx) ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555)) * 0x9E3779B9;
                // a specific combination of XOR and two rotations that doesn't produce duplicate hashes for our target range
//                state = (my ^ (my << 16 | my >>> 16) ^ (my << 8 | my >>> 24));

                final int max = Math.max(mx, my);
                state = ((max * max + max + mx - my) ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555)) * 0x9E3779B9;

                all.add(state);
            }
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }

    @Test
    public void test32BitMix()
    {
        final RoaringBitmap all = new RoaringBitmap();

        for (int x = 0; x < 0x10000; x++) {
            for (int y = 0; y < 0x10000; y++) {
                int state = x << 16 | y;
                state = (state ^ 0xEFAA28F1 ^ state >>> 16);
                if(!all.checkedAdd(state)) {
                    System.out.printf("Encountered a collision after %d/%d with state 0x%08X \n", all.getLongCardinality(), 0x100000000L, state);
                    return;
                }
            }
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
    }
   @Test
    public void test16BitPointHash()
    {
        final RoaringBitmap all = new RoaringBitmap();
        int state = 0;
        for (int x = -0x80; x < 0x80; x++) {
            for (int y = -0x80; y < 0x80; y++) {
                // the signs for x and y; each is either -1 or 0
                int xs = x >> 31, ys = y >> 31;
                // makes mx equivalent to -1 ^ this.x if this.x is negative
                int mx = x ^ xs;
                // same for my
                int my = y ^ ys;
                // Cantor pairing function, and XOR with every odd-index bit of xs and every even-index bit of ys
                // this makes negative x, negative y, positive both, and negative both all get different bits XORed or not
//                my = my + ((mx + my) * (mx + my + 1) >>> 1) ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555);
                my = (mx >= my ? mx * (mx + 2) - my : my * my + mx) ^ (xs & 0xAAAA) ^ (ys & 0x5555);
                state = my;
                // a specific combination of XOR and two rotations that doesn't produce duplicate hashes for our target range
//                state = (my ^ (my << 4 | my >>> 12) ^ (my << 8 | my >>> 8)) & 0xFFFF;

                all.add(state);
            }

        }
        System.out.println(all.getLongCardinality() + "/" + 0x10000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-16 + "% of outputs were missing.");
    }

    @Test
    public void testBeastDistribution()
    {
        long[] smallCounts = new long[256];
        byte initialA = 0, initialB = 0, initialC = 0, initialD = 0;
        byte stateA = initialA, stateB = initialB, stateC = initialC, stateD = initialD;
        final long iterations = 1L << 32;
        long a = 0;
        for (; a < iterations; a++) {
            byte x = stateA, y = stateB, z = stateC, w = stateD;
            stateA += (byte)0x99;
            stateB += clz8(x);
            stateC = (byte)(rotate8(w, 1) - x);
            stateD = (byte)(rotate8(z, 6) ^ y);
            if(stateA == initialA && stateB == initialB && stateC == initialC && stateD == initialD)
                break;
            x ^= rotate8(w, 7) + y;
            y += rotate8(z, 4) ^ x;

            smallCounts[(x - rotate8(y, 5) & 255)]++;
        }
        System.out.printf("Subcycle %d, %d, %d, %d has length %d, %10.8f%% of the maximum cycle.",
                initialA, initialB, initialC, initialD, a, a * 0x64p-32);
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testMattv8Distribution()
    {
        final RoaringBitmap all = new RoaringBitmap();
        long[] smallCounts = new long[256];
        int stateA = 0;
        final long iterations = 1L << 32;
        long a = 0;
        for (; a < iterations; a++) {
            int t = stateA += 0x99;
            t = (t ^ t >>> 15) * (t | 1);// XOR the current state with a right shift by 15 bits to add additional entropy
            t ^= t + (t ^ t >>> 7) * (t | 61);// Perform an integer multiplication to add additional entropy
            t ^= t >>> 14;// XOR the current state with a right shift by 14 bits to add additional, then bitwise AND with 0xFF to ensure an 8-bit value

            smallCounts[(t & 255)]++;
            all.add(t & 255);
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-8 + "% of outputs were missing.");
    }

    @Test
    public void testMattv8LongestSubcycle() {
        long a = 1;
        int initial = 166469239;
        int t = initial;
        for (; a <= 0x100000000L; a++) {
            t = (t ^ t >>> 15) * (t | 1);
            t ^= t + (t ^ t >>> 7) * (t | 61);

            if (initial == t) System.out.println("Failed after " + a + " iterations.");

        }
        Assert.assertEquals(initial, t);
    }

    @Test
    public void testMattv8StateDistribution()
    {
        final RoaringBitmap all = new RoaringBitmap();
        all.add(0, 1L << 32);
        int initialState = 0;
        int[] cycleCheck = new int[256];
        int cycleIndex = 0, concern = 0;
        long sum = 0, lastSum = -1;
        while (!all.isEmpty()) {
            int state = initialState;
//            System.out.println("Starting subcycle on " + state);
            long a = 1;
            int t = state;
            for (; a <= 0x100000000L; a++) {
                t = (t ^ t >>> 15) * (t | 1);// XOR the current state with a right shift by 15 bits to add additional entropy
                state = t ^= t + (t ^ t >>> 7) * (t | 61);// Perform an integer multiplication to add additional entropy
                //return (t ^ t >>> 14) & 255;// XOR the current state with a right shift by 14 bits to add additional, then bitwise AND with 0xFF to ensure an 8-bit value
                sum -= cycleCheck[cycleIndex & 255];
                cycleCheck[cycleIndex++ & 255] = state;
                sum += state;
                if(a > 256 && lastSum == sum) {
                    System.out.println("CONCERNED AT " + a);
                    if(++concern >= 5) break;
                }
                all.remove(state);
                if (state == initialState)
                    break;
//                if((a & 0x3FFFFF) == 0x3FFFFF)
//                    System.out.printf("I have run for %d steps on this cycle. The state is %d.\n", a, state);
            }
            System.out.printf("Subcycle %d has length %d, %10.8f%% of the maximum cycle.\n",
                    initialState, a, a * 0x64p-32);
            initialState = (int)all.nextValue(initialState);
        }
        System.out.println();
    }


    /**
     * <code>
     *                 stateA += (byte) 0x99;
     *                 stateB += clz8(x);
     *                 stateC = (byte) (rotate8(w, 1) - x);
     *                 stateD = (byte) (rotate8(z, 6) ^ y);
     * </code>
     * <br>
     * <pre>
     * Subcycle 0 has length 1201602560, 27.97698975% of the maximum cycle.
     * Subcycle 2 has length 581828608, 13.54675293% of the maximum cycle.
     * Subcycle 6 has length 809500672, 18.84765625% of the maximum cycle.
     * Subcycle 9 has length 455344128, 10.60180664% of the maximum cycle.
     * Subcycle 26 has length 113836032, 2.65045166% of the maximum cycle.
     * Subcycle 49 has length 12648448, 0.29449463% of the maximum cycle.
     * Subcycle 75 has length 37945344, 0.88348389% of the maximum cycle.
     * Subcycle 111 has length 25296896, 0.58898926% of the maximum cycle.
     * Subcycle 3072 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3073 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3074 has length 60293120, 1.40380859% of the maximum cycle.
     * Subcycle 3075 has length 60293120, 1.40380859% of the maximum cycle.
     * Subcycle 3077 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3078 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3079 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3080 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3081 has length 23592960, 0.54931641% of the maximum cycle.
     * Subcycle 3087 has length 23592960, 0.54931641% of the maximum cycle.
     * Subcycle 3092 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3093 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3094 has length 23592960, 0.54931641% of the maximum cycle.
     * Subcycle 3098 has length 23592960, 0.54931641% of the maximum cycle.
     * Subcycle 3101 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3115 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3117 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3121 has length 2621440, 0.06103516% of the maximum cycle.
     * Subcycle 3130 has length 23592960, 0.54931641% of the maximum cycle.
     * Subcycle 3136 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3147 has length 7864320, 0.18310547% of the maximum cycle.
     * Subcycle 3168 has length 20971520, 0.48828125% of the maximum cycle.
     * Subcycle 3183 has length 2621440, 0.06103516% of the maximum cycle.
     * Subcycle 3206 has length 2621440, 0.06103516% of the maximum cycle.
     * Subcycle 3840 has length 49807360, 1.15966797% of the maximum cycle.
     * Subcycle 3842 has length 12058624, 0.28076172% of the maximum cycle.
     * Subcycle 3843 has length 12058624, 0.28076172% of the maximum cycle.
     * Subcycle 3846 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3848 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3849 has length 4718592, 0.10986328% of the maximum cycle.
     * Subcycle 3855 has length 4718592, 0.10986328% of the maximum cycle.
     * Subcycle 3860 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3862 has length 4718592, 0.10986328% of the maximum cycle.
     * Subcycle 3866 has length 4718592, 0.10986328% of the maximum cycle.
     * Subcycle 3869 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3883 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3885 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3889 has length 524288, 0.01220703% of the maximum cycle.
     * Subcycle 3898 has length 4718592, 0.10986328% of the maximum cycle.
     * Subcycle 3904 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3915 has length 1572864, 0.03662109% of the maximum cycle.
     * Subcycle 3936 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 3951 has length 524288, 0.01220703% of the maximum cycle.
     * Subcycle 3974 has length 524288, 0.01220703% of the maximum cycle.
     * Subcycle 5376 has length 74711040, 1.73950195% of the maximum cycle.
     * Subcycle 5378 has length 18087936, 0.42114258% of the maximum cycle.
     * Subcycle 5379 has length 18087936, 0.42114258% of the maximum cycle.
     * Subcycle 5382 has length 12582912, 0.29296875% of the maximum cycle.
     * Subcycle 5384 has length 12582912, 0.29296875% of the maximum cycle.
     * Subcycle 5385 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5391 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5398 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5402 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5405 has length 12582912, 0.29296875% of the maximum cycle.
     * Subcycle 5410 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5412 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5415 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5425 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 5427 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5430 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5434 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5437 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5440 has length 12582912, 0.29296875% of the maximum cycle.
     * Subcycle 5451 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 5479 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 5480 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5487 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 5488 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5490 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5510 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 5571 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 5573 has length 786432, 0.01831055% of the maximum cycle.
     * Subcycle 11264 has length 6225920, 0.14495850% of the maximum cycle.
     * Subcycle 11266 has length 3014656, 0.07019043% of the maximum cycle.
     * Subcycle 11270 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 11273 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 11290 has length 589824, 0.01373291% of the maximum cycle.
     * Subcycle 11313 has length 65536, 0.00152588% of the maximum cycle.
     * Subcycle 11339 has length 196608, 0.00457764% of the maximum cycle.
     * Subcycle 11375 has length 131072, 0.00305176% of the maximum cycle.
     * Subcycle 34304 has length 12451840, 0.28991699% of the maximum cycle.
     * Subcycle 34306 has length 3014656, 0.07019043% of the maximum cycle.
     * Subcycle 34307 has length 3014656, 0.07019043% of the maximum cycle.
     * Subcycle 34310 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 34313 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 34326 has length 2359296, 0.05493164% of the maximum cycle.
     * Subcycle 34330 has length 1179648, 0.02746582% of the maximum cycle.
     * Subcycle 34333 has length 4194304, 0.09765625% of the maximum cycle.
     * Subcycle 34353 has length 131072, 0.00305176% of the maximum cycle.
     * Subcycle 34379 has length 393216, 0.00915527% of the maximum cycle.
     * Subcycle 34415 has length 131072, 0.00305176% of the maximum cycle.
     * Subcycle 34438 has length 131072, 0.00305176% of the maximum cycle.
     * </pre>
     */
    @Test
    public void testBeastStateDistribution()
    {
        final RoaringBitmap all = new RoaringBitmap();
        all.add(0, 1L << 32);
        int initialState = 0;
        while (!all.isEmpty()) {
            int state = initialState;
//            System.out.println("Starting subcycle on " + state);
            long a = 1;
            byte stateA = (byte) (state >>> 24);
            byte stateB = (byte) (state >>> 16);
            byte stateC = (byte) (state >>>  8);
            byte stateD = (byte) (state       );
            for (; a <= 0x100000000L; a++) {
                byte x = stateA, y = stateB, z = stateC, w = stateD;
                stateA += (byte) 0x99;
                stateB += clz8(x);
                stateC = (byte) (rotate8(w, 1) - x);
                stateD = (byte) (rotate8(z, 6) ^ y);
                state = (stateA << 24) | (stateB << 16 & 0xFF0000) | (stateC << 8 & 0xFF00) | (stateD & 0xFF);
                all.remove(state);
                if (state == initialState)
                    break;
//                if((a & 0x3FFFFF) == 0x3FFFFF)
//                    System.out.printf("I have run for %d steps on this cycle. The state is %d.\n", a, state);
            }
            System.out.printf("Subcycle %d has length %d, %10.8f%% of the maximum cycle.\n",
                    initialState, a, a * 0x64p-32);
            initialState = (int)all.nextValue(initialState);
        }
        System.out.println();
    }

    @Test
    public void testBeastFinalizerDistribution()
    {
        long[] smallCounts = new long[256];
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 256; z++) {
                    for (int w = 0; w < 256; w++) {
                        int a = (x ^ (rotate8(w, 7) + y)) & 255;
                        int b = (y + (rotate8(z, 4) ^ x)) & 255;
                        smallCounts[(a - rotate8(b, 5) & 255)]++;
                    }
                }
            }
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testBlamFinalizerDistribution()
    {
        long[] smallCounts = new long[256];
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                for (int c = 0; c < 256; c++) {
                    int x = a, y = b, z = c;
                    // Like the Speck cipher, 2 rounds; equidistributed for x and y
//                    x = (y ^ rotate8(x, 2) + z) & 255;
//                    y = (x + rotate8(y, 7) ^ z) & 255;
//                    x = (y ^ rotate8(x, 5) + z) & 255;
//                    y = (x + rotate8(y, 3) ^ z) & 255;
                    // appears equidistributed...
//                    z = (z ^ (y * x | 1)) & 255;
//                    x = (x ^ (z * y | 1)) & 255;
//                    y = (y ^ (x * z | 1)) & 255;
                    // also equidistributed
//                    z = (rotate8(z, 1) ^ (y * x | 1)) & 255;
//                    x = (rotate8(x, 6) ^ (z * y | 1)) & 255;
//                    y = (rotate8(y, 5) ^ (x * z | 1)) & 255;

                    x = (x ^ rotate8(z, 1) + rotate8(y, 6)) & 255;
                    y = (y ^ rotate8(x, 2) + rotate8(z, 4)) & 255;
                    z = (z ^ rotate8(y, 3) + rotate8(x, 7)) & 255;

//                    y = (z ^ rotate8(y, 3) + x) & 255;
//                    z = (y + rotate8(z, 6)) & 255;

//                    int a = (x ^ (rotate8(w, 7) + y)) & 255;
//                        int b = (y + (rotate8(z, 4) ^ x)) & 255;
//                        smallCounts[(a - rotate8(b, 5) & 255)]++;
//                        smallCounts[(x + y & 255)]++;
//                        smallCounts[xx ^ yy ^ zz]++;
//                        smallCounts[xx + yy + zz & 255]++;
                        smallCounts[(z)&255]++;
//                        smallCounts[y]++;
//                        smallCounts[x ^ y ^ z]++;
                }
            }
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testTriangularDistribution()
    {
        long[] smallCounts = new long[256];
        for (int a = 0; a < 256; a++) {
            int z = (((a+1>>>1) * (a | 1)) & 255);
//            int z = (((a+1>>>1) * (a | 1) ^ 7) & 255);
            smallCounts[(z)&255]++;
        }
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * For the modifier m, which is XORed with the nth triangular number to get the n+1-th element in the sequence, only
     * m-values 7, 13, 114, and 120 have a full period.
     */
    @Test
    public void testTriangularPeriod() {
        short[] counts = new short[256];
        for (int m = 0; m < 256; m++) {
            Arrays.fill(counts, (short) 0);
            int z = 0;
            for (int a = 0; a < 0x100; a++) {
                counts[z = ((z + 1 >>> 1) * (z | 1) ^ m) & 255]++;
            }
            int optimal = 0;
            for (int y = 0, i = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    if (counts[i] == 1) optimal++;
                    System.out.print(StringKit.hex(counts[i++]) + " ");
                }
                System.out.println();
            }
            System.out.println("For " + m + ", " + optimal + " results were output the optimal number of times.");
            if (optimal == counts.length)
                System.out.println("That's perfect!");
        }
    }

    /**
     * With 16-bit state size, no XORed values produce an optimal period of 2 to the 16. Some get close:
     * When m is 4131, 4697, 28070, or 28636, the period is 65532, just 4 shy of optimal.
     */
    @Test
    public void testTriangular16Period() {
        short[] counts = new short[0x10000];
        for (int m = 0; m < 65536; m++) {
            Arrays.fill(counts, (short) 0);
            int z = 0;
            for (int a = 0; a < 0x10000; a++) {
                counts[z = ((z + 1 >>> 1) * (z | 1) ^ m) & 0xFFFF]++;
            }
            int optimal = 0;
            for (int i = 0; i < 0x10000; i++) {
                if (counts[i] == 1) optimal++;
            }
//            for (int y = 0, i = 0; y < 16; y++) {
//                for (int x = 0; x < 16; x++) {
//                    System.out.print(StringKit.hex(counts[i++]) + " ");
//                }
//                System.out.println();
//            }
//            System.out.println("For " + m + ", " + optimal + " results were output the optimal number of times.");
            if (optimal > 64000) {
                System.out.println("For " + m + ", " + optimal + " results were output the optimal number of times.");
                if (optimal == counts.length)
                    System.out.println("That's perfect!");
            }
        }
    }

    @Test
    public void testBlamDistribution()
    {
        long[] smallCounts = new long[256];
        byte initialA = 0, initialB = 0, initialC = 0;
        byte stateA = initialA, stateB = initialB, stateC = initialC;
        final long iterations = (1L << 32) + 1024L;
//        final long iterations = (1L << 16);
        long a = 1;
        for (; a < iterations; a++) {
            byte x = stateA, y = stateB, z = stateC, n = x;
            // equidistributed, at least 1D.
            stateA = (byte)(0xC ^ x + 0x99);
            stateB = (byte)(x ^ y + clz8(n));
            stateC = (byte)(y ^ z + clz8(n&y));

            smallCounts[(x ^ y ^ z) & 255]++;
            if(stateA == initialA && stateB == initialB && stateC == initialC) {
                System.out.println("Completed a (sub) cycle!");
                break;
            }
        }
        System.out.printf("Subcycle %d, %d, %d has length %d, %10.8f%% of the maximum cycle.",
                initialA, initialB, initialC, a, a * 0x64p-24);
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    @Test
    public void testBoomDistribution()
    {
        long[] smallCounts = new long[65536];
        byte initialA = 0, initialB = 0, initialC = 0, initialD = 0;
        byte stateA = initialA, stateB = initialB, stateC = initialC, stateD = initialD;
        final long iterations = (1L << 32) + 1024L;
//        final long iterations = (1L << 16);
        long a = 1;
        for (; a < iterations; a++) {
            // 1D equidistributed for 16-bit outputs.
            // 2D equidistributed for 8-bit outputs, when returning either x or y.
//            byte x = stateA, y = stateB, z = stateC, w = stateD, n = x;
//            stateA += (byte)0x99;
//            stateB += clz8(n);
//            stateC += clz8(n|=y);
//            stateD += clz8(n&z);
//            x = (byte)(y ^ rotate8(x, 7) + z);
//            y = (byte)(x + rotate8(y, 4));
//            x = (byte)(y ^ rotate8(x, 7) + w);
//            y = (byte)(x + rotate8(y, 4));

            // distributed quite unevenly. the total number of occurrences of any single 16-bit result is always odd...?
//            byte x = stateA, y = stateB, z = stateC, w = stateD;
//            stateA += (byte)0x99;
//            stateB += clz8(x);
//            stateC += clz8(x|=y);
//            stateD += clz8(x&=z);
//            z = (byte)(z ^ rotate8(w, 7) + x);
//            w = (byte)(w + rotate8(z, 4));
//            z = (byte)(z ^ rotate8(w, 7) + y);
//            w = (byte)(w + rotate8(z, 4));

            // also 2D equidistributed, for z and w this time.
//            byte x = stateA, y = stateB, z = stateC, w = stateD, n = x;
//            stateA += (byte)0x99;
//            stateB += clz8(n);
//            stateC += clz8(n|=y);
//            stateD += clz8(n&z);
//            z = (byte)(z ^ rotate8(w, 7) + x);
//            w = (byte)(w + rotate8(z, 4));
//            z = (byte)(z ^ rotate8(w, 7) + y);
//            w = (byte)(w + rotate8(z, 4));

            // 2D equidistributed when returning 8-bit z or w.
//            byte x = stateA, y = stateB, z = stateC, w = stateD, n = x;
//            stateA += (byte)0x99;
//            stateB += clz8(n);
//            stateC += clz8(n|=y);
//            stateD += clz8(n&z);
//            x = (byte)(y ^ rotate8(x, 7) + w);
//            y = (byte)(x + rotate8(y, 4) ^ z);
//            z = (byte)(w ^ rotate8(z, 7) + y);
//            w = (byte)(z + rotate8(w, 4) ^ x);

            // 2D equidistributed when returning 8-bit y or w.
//            byte x = stateA, y = stateB, z = stateC, w = stateD, n = x;
//            stateA += (byte)0x99;
//            stateB += clz8(n);
//            stateC += clz8(n|=y);
//            stateD += clz8(n&z);
//            y = (byte)((y ^ rotate8(x, 7) + w) + rotate8(y, 4));
//            w = (byte)((w ^ rotate8(z, 2) + y) + rotate8(w, 5));

            // 2D equidistributed when returning 8-bit y or w.
            // This pattern passes 16GB of PractRand without anomalies,
            // but starts to have trouble after that (using 32-bit state variables).
            byte x = stateA, y = stateB, z = stateC, w = stateD, n = x;
            stateA += (byte)0x99;
            stateB += x ^ clz8(n);
            stateC += y ^ clz8(n|=y);
            stateD += z ^ clz8(n&z);
            y = (byte)((y ^ rotate8(x, 7) + w) + rotate8(y, 4));
            w = (byte)((w ^ rotate8(z, 2) + y) + rotate8(w, 5));

//            smallCounts[(x & 0xFF)]++;
//            smallCounts[(y & 0xFF)]++;
//            smallCounts[(x & 0xFF) | (y << 8 & 0xFF00)]++;
//            smallCounts[(z & 0xFF) | (w << 8 & 0xFF00)]++;
            smallCounts[(y & 0xFF) | (w << 8 & 0xFF00)]++;
            if(stateA == initialA && stateB == initialB && stateC == initialC && stateD == initialD) {
                System.out.println("Completed a (sub) cycle!");
                break;
            }
        }
        System.out.printf("Subcycle %d, %d, %d, %d has length %d, %10.8f%% of the maximum cycle.",
                initialA, initialB, initialC, initialD, a, a * 0x64p-32);
        System.out.println();
        for (int y = 0, i = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++, i++) {
                System.out.printf("%09X ", smallCounts[i]);
            }
            System.out.println();
        }
    }

    /**
     * Produces every output equally often; period is 2 to the 16.
     * The big thing here is that "a" can be randomly rotated by "b",
     * XORed with "b", and it keeps equidistribution.
     */
    @Test
    public void testOrbitPCG8Bit()
    {
        byte stateA = 0, stateB = 1;
        short[] counts = new short[256];
        for (int i = 0; i < 0x10000; i++) {
            int a = (stateA += 0xCD) & 0xFF;
            int b = (stateB += 0x96 + clz8(stateA)) & 0xFF;
            a = (a ^ a >>> 4) * 0xF5 & 0xFF;
            b = (b ^ b >>> 3) * 0xBD & 0xFF;
            // not at all well-distributed!
//            int s = (rotate8(b, a) ^ rotate8(a, b)) & 0xFF;
            // 1D equidistributed
            int s = (rotate8(b, 3) ^ rotate8(a, b)) & 0xFF;
            // 1D equidistributed
//            int s = (b ^ b >>> 5 ^ rotate8(a, b)) & 0xFF;
            // 1D equidistributed
//            int s = (b ^ (a >>> (b & 3))) * 0x65 & 0xFF;
            counts[s]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
    }


}
