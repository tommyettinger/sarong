package sarong;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.support.util.IntAppender;
import org.huldra.math.BigInt;
import org.junit.Assert;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.longlong.Roaring64NavigableMap;
import sarong.util.CrossHash;
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
     * -------------------------------------------------------------------------------
     * Verification code for key 0x3695: 0xE66CB543CDCE41C7L
     * Verification code for key 0x6AC5: 0xF8788B90C756073BL
     */
    @Test
    public void testSquaresTruncated16Bit()
    {
        short[] counts = new short[256];
        short key = 0x6AC5;
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
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Verification code for key 0x%04X: 0x%016XL\n", key, CrossHash.Water.hash64(counts));
    }

    /**
     * Creates 1680 different keys from unique, distinct combinations of odd hex digits, and uses all of them as keys
     * for the 16-bit state, 8-bit output shrunk-down version of Squares. The text here is the number of times each
     * 8-bit output occurred over the full periods for each key.
     * <br>
     * <pre>
     * APPEARANCE COUNTS (hex):
     *  6b739  68b75  68f02  68af8  693b8  69035  6927b  68817  6998e  6886a  691f4  688a7  69485  68722  68e2a  68f04
     *  6a321  68842  68baf  68917  696e8  68b1c  690b1  680a0  69595  69060  68feb  689d8  6932f  687b1  695ef  68438
     *  6a6db  68951  6911e  685e9  69f9b  68a0f  68e48  68852  69a66  68605  69360  68810  699d4  687fd  694ad  693ad
     *  6a055  685d1  68f30  6887c  69e13  68816  68fb2  686b9  69a36  687b0  68f2a  68c8a  69315  68b16  68f88  6885d
     *  6b689  68ba6  68e9d  6856e  69a4c  68d59  6898e  691dc  69b40  68a59  68f23  68b2f  69236  686db  69330  68b3c
     *  6a31a  68782  69187  68ed5  69ab9  6882e  68909  68860  6a09a  68f77  693e1  686f6  696ab  6826c  696f5  68b8f
     *  6a799  68ce1  690ac  68a10  695d6  67f1e  68e50  68d6b  69b4c  68b95  69112  68918  6976b  68a7f  68db9  68681
     *  6a507  68b22  68fe6  68980  69bd4  68a20  69055  68248  69beb  687f1  69381  68cc8  696f0  68431  68d46  68790
     *  6b439  68a1f  68ba2  68462  69762  68b56  69124  6898b  695fe  685a6  69325  686e2  69b35  6882d  68f67  68ba5
     *  6a282  68e0e  68d9b  68b89  6a172  68631  689fa  68e03  69c8d  686b0  6926c  68cae  696b6  69029  692ed  686ab
     *  6a8d5  68647  69319  68723  6958a  68e72  68ceb  68893  69ee7  6860e  692e9  6832c  697d1  68c24  69255  688cb
     *  6a320  68792  690db  6861b  69a06  68542  68f6e  68737  69dac  687f4  68f3f  688f9  69603  685b6  69330  68706
     *  6bf56  68e90  68dbd  68cb9  697e7  6869e  6960f  68b89  69bb3  687cb  690c1  68bbf  694e6  687b9  69169  6866e
     *  6a62e  68471  69092  6885f  69bbf  68b9b  692e7  68c54  69c0a  68ce2  68c6f  6820a  69724  68b64  69555  68872
     *  6a958  689d4  6887d  68ade  6980f  68a66  68b18  6899d  69879  68ee5  6958f  688b6  6972a  69031  692d3  68b53
     *  69f4d  688d1  68b54  68667  69484  68747  688ae  6880f  69c91  68db3  691bb  68c91  69732  68c39  6948f  6850a
     * Total number of missing results: 0/256
     * Lowest appearance count : 425758 (in hex, 0x00067f1e)
     * Highest appearance count: 442198 (in hex, 0x0006bf56)
     * </pre>
     * There's some fairly clear column biases here, where some columns (each of which corresponds to a different last
     * hex digit) reliably occur more (or less) frequently than others. The first column, where the last hex digit is 0,
     * never has an entry occur less often than 0x69f4d times, and at most occurs 0x6bf56 times. The second column,
     * where the last hex digit is 1, has an entry occur 0x68471 times and at most occurs 0x68e90 times. That's a
     * difference of over 2.55, averaged over all keys, between the least frequent 0 digit and the most frequent 1
     * digit, which is almost exactly 1% of the expected total for an equidistributed generator. If this was a roulette
     * wheel roll simulator, choosing 0xC0 (192 in decimal) would be an unusually good guess, and choosing 0x65 (101 in
     * decimal) would be an unusually poor one.
     */
    @Test
    public void testSquaresTruncated16BitGreatSum()
    {
        int[] counts = new int[256];
        for (int d0 = 1; d0 < 16; d0+=2) {
            for (int d1 = 1; d1 < 16; d1 += 2) {
                if (d1 == d0) continue;
                for (int d2 = 1; d2 < 16; d2 += 2) {
                    if (d2 == d0 || d2 == d1) continue;
                    for (int d3 = 1; d3 < 16; d3 += 2) {
                        if (d3 == d0 || d3 == d1 || d3 == d2) continue;
                        int key = d0 | d1 << 4 | d2 << 8 | d3 << 12;
                        for (int a = 0; a < 0x10000; a++) {
                            int t, x, y, z;
                            y = x = a * key & 0xFFFF;
                            z = y + key & 0xFFFF;
                            x = x * x + y & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            x = x * x + z & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            x = x * x + y & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            t = ((x * x + y & 0xFF00) >>> 8);
                            counts[t]++;
                        }
                    }
                }
            }
        }
        System.out.println("APPEARANCE COUNTS (hex):");
        int missing = 0, lowest = Integer.MAX_VALUE, highest = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%6x ", counts[i]);
                if(counts[i] == 0) missing++;
                lowest = Math.min(lowest, counts[i]);
                highest = Math.max(highest, counts[i]);
                i++;
            }
            System.out.println();
        }
        System.out.printf("Total number of missing results: %d/256\n", missing);
        System.out.printf("Lowest appearance count : %d (in hex, 0x%08x)\n", lowest, lowest);
        System.out.printf("Highest appearance count: %d (in hex, 0x%08x)\n", highest, highest);
    }

    /**
     * Testing all 17472 keys with an odd last hex digit and 4 distinct hex digits, not permitting 0... These are the
     * same rules Squares uses for its keys, more or less, except that it has an extra restriction that prevents the
     * first and last 8 hex digits of a 64-bit key from being the same. We effectively do this anyway because we only
     * have four digits, and they are all unique.
     * <pre>
     * APPEARANCE COUNTS (hex):
     * 459150 43f6b9 4448c1 440005 4471e3 43fe60 443cd7 43eedb 44c6bd 440816 4445d2 440020 449c61 43e1d9 44330e 4402f1
     * 45132e 43fde4 444e12 43ff1c 447765 440132 4436c8 43ef24 44bff8 440c48 444072 43f476 447d5a 43f8f2 444a23 43f805
     * 4549ef 43f82e 443a67 4403fd 44899a 4409db 444547 4401b0 44bea7 43f29c 44451b 43fb99 447fc3 43ed07 4447da 440527
     * 44fa33 43fe68 4455d1 43f6b1 449146 43ed7d 443a41 440099 44c1c4 440c0f 444726 43fd11 447de2 43f12a 4430a7 43fc65
     * 45e356 43f50d 444388 43ffcc 4490f2 43f723 4440e1 440702 44d640 43f627 444298 44067f 44768f 43fd3c 4440a5 43fe32
     * 4511e6 43f220 4441c4 440a13 448129 43fdde 443f93 43fa08 44cbd6 43f0b4 4445a5 43f9e7 4483d0 4408b5 443fad 440116
     * 4556f6 43ff81 44311f 43e9cc 449166 43e550 444c52 43f6af 44ccfe 440661 444176 4403d3 44803a 44036b 444ee1 4401f2
     * 452d00 43fc46 44413e 43fd4d 449243 43f505 443b0c 43fb16 44be54 440067 4437ba 441f55 448042 43ecf4 442686 43f5c8
     * 45895a 440371 443d70 43f96c 448aa9 43e9e3 44481f 43fcd8 44ca70 4406db 444695 43faf5 4479b9 43ff74 444002 43f58d
     * 44ef9c 43f834 4442b3 43f1d7 4486e9 43f19f 4434d7 43fe8e 44bd43 43fec7 443340 440f37 448abb 43f283 4438fd 43f2b4
     * 455d12 43f3db 444e07 440b6e 447a38 43f885 442f37 44108a 44dd9f 43f244 443db9 43fcac 4488c7 4407fb 4437fc 440a0d
     * 4511ea 43e9ac 444261 43eb7b 447a04 4402e9 443dab 44010d 44c1ce 4403fa 443f1e 440796 448412 43f5cf 44401f 44065e
     * 463b6b 43f092 4438d7 43f743 447d44 4401b5 444aa4 43ebe2 44c612 43ecf2 444045 440ae3 448280 43f2c7 44434c 44003f
     * 44fbc5 43fb8b 4437ed 44006a 448a30 43fac7 445a5f 4402a7 44d484 43ef80 44500c 43e813 447f67 4408e6 44416f 43fb0e
     * 455b51 43fc5b 442dca 43fb04 448055 43fe25 4447bc 43eb8d 44bf97 440a27 4449e4 4407ea 449297 4401e3 443efd 4403e2
     * 450e6d 43e0ee 444152 4405c3 448fbe 43f2e9 4443e9 43fa48 44ca42 43fb01 444d9f 43f017 446b99 43fb82 4445fc 43fbf1
     * Total number of missing results: 0/256
     * Total number of distinct keys tried: 17472
     * Lowest appearance count : 4448494 (in hex, 0x0043e0ee)
     * Highest appearance count: 4602731 (in hex, 0x00463b6b)
     * </pre>
     * Guessing 0xC0, or 192, is still an unusually good guess!
     */
    @Test
    public void testSquaresTruncated16BitGreatestSum()
    {
        int[] counts = new int[256];
        int totalKeys = 0;
        for (int d0 = 1; d0 < 16; d0+=2) {
            for (int d1 = 1; d1 < 16; d1++) {
                if (d1 == d0) continue;
                for (int d2 = 1; d2 < 16; d2++) {
                    if (d2 == d0 || d2 == d1) continue;
                    for (int d3 = 1; d3 < 16; d3++) {
                        if (d3 == d0 || d3 == d1 || d3 == d2) continue;
                        totalKeys++;
                        int key = d0 | d1 << 4 | d2 << 8 | d3 << 12;
                        for (int a = 0; a < 0x10000; a++) {
                            int t, x, y, z;
                            y = x = a * key & 0xFFFF;
                            z = y + key & 0xFFFF;
                            x = x * x + y & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            x = x * x + z & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            x = x * x + y & 0xFFFF;
                            x = (x >>> 8 | x << 8) & 0xFFFF;
                            t = ((x * x + y & 0xFF00) >>> 8);
                            counts[t]++;
                        }
                    }
                }
            }
        }
        System.out.println("APPEARANCE COUNTS (hex):");
        int missing = 0, lowest = Integer.MAX_VALUE, highest = -1;
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%6x ", counts[i]);
                if(counts[i] == 0) missing++;
                lowest = Math.min(lowest, counts[i]);
                highest = Math.max(highest, counts[i]);
                i++;
            }
            System.out.println();
        }
        System.out.printf("Total number of missing results: %d/256\n", missing);
        System.out.printf("Total number of distinct keys tried: %d\n", totalKeys);
        System.out.printf("Lowest appearance count : %d (in hex, 0x%08x)\n", lowest, lowest);
        System.out.printf("Highest appearance count: %d (in hex, 0x%08x)\n", highest, highest);
    }

    /**
     * Total number of missing results: 0/65536
     * <br>
     * "Sin clave" means "without key" in Spanish.
     */
    @Test
    public void testSinclave16Bit()
    {
        short[] counts = new short[65536];
        short key = 5555;
        for (int a = 0; a < 0x10000; a++) {
            int x, y;
            x = (a ^ 55) * key & 0xFFFF; y = x | 1;
            x = x*x + y & 0xFFFF;
            x = (x>>>8 | x<<8) & 0xFFFF;
            y = x | 1; // without this line: Total number of missing results: 25920/65536
            x = x*x + y & 0xFFFF;
            x ^= x >>> 7;
            counts[x]++;
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
     * Total number of missing results: 0/256
     * Total number of distinct keys tried: 17472
     * Lowest appearance count : 17472 (in hex, 0x00004440)
     * Highest appearance count: 17472 (in hex, 0x00004440)
     * <br>
     * Very similar to a stripped-down Squares, but importantly this is equidistributed and needs fewer "rounds" to
     * produce adequate randomness. It XORs the input by 55 before multiplying it by the key, which helps resist the
     * issue where the input could be incremented by the modular multiplicative inverse of the key. 55 isn't a valid
     * constant for an XLCG, so it seems less likely that a XOR by 55 could be encountered accidentally. This does not
     * make the mistake Squares makes that breaks its equidistribution; Squares reuses "x" (the input times the key)
     * as "y", and as "z" with the full key added again. The y and z aren't ever updated in later rounds, which turns
     * out to break the chance of reaching equidistribution. Here, we only have y, and it is just {@code x | 1}, which
     * is recalculated before it is used in the squaring-based operation. This closes with a xor-shift right to mix the
     * low bits a little better; multiplications earlier likely didn't improve any low bits very much.
     */
    @Test
    public void testEnclave16BitGreatestSum()
    {
        int[] counts = new int[65536];
        int totalKeys = 0;
        for (int d0 = 1; d0 < 16; d0+=2) {
            for (int d1 = 1; d1 < 16; d1++) {
                if (d1 == d0) continue;
                for (int d2 = 1; d2 < 16; d2++) {
                    if (d2 == d0 || d2 == d1) continue;
                    for (int d3 = 1; d3 < 16; d3++) {
                        if (d3 == d0 || d3 == d1 || d3 == d2) continue;
                        totalKeys++;
                        int key = d0 | d1 << 4 | d2 << 8 | d3 << 12;
                        for (int a = 0; a < 0x10000; a++) {
                            int x, y;
                            x = (a ^ 55) * key & 0xFFFF; y = x | 1;
                            x = x * x + y & 0xFFFF;
                            x = (x>>>8 | x<<8) & 0xFFFF;
                            y = x | 1;
                            x = x * x + y & 0xFFFF;
                            x ^= x >>> 7;
                            counts[x]++;
                        }
                    }
                }
            }
        }
        System.out.println("APPEARANCE COUNTS (hex):");
        int missing = 0, lowest = Integer.MAX_VALUE, highest = -1;
        for (int y = 0, i = 0; y < 4096; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.printf("%6x ", counts[i]);
                if(counts[i] == 0) missing++;
                lowest = Math.min(lowest, counts[i]);
                highest = Math.max(highest, counts[i]);
                i++;
            }
            System.out.println();
        }
        System.out.printf("Total number of missing results: %d/65536\n", missing);
        System.out.printf("Total number of distinct keys tried: %d\n", totalKeys);
        System.out.printf("Lowest appearance count : %d (in hex, 0x%08x)\n", lowest, lowest);
        System.out.printf("Highest appearance count: %d (in hex, 0x%08x)\n", highest, highest);
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
     * a previously-unproven variant on it. Add-Square-Or also works!
     */
    @Test
    public void test16BitXorSquareOr()
    {
        short result, xor = 0, s = 0;
        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        for (int i = 0; i < 0x10000; i++) {
//            result = (short) (i ^ (i * i | 1)); // works, no fixed-points
//            s ^= (s * s | 1);
//            result = ++s; //Repeats after 16384 iterations
//            result = (short) (i ^ rotl16(i, 3) ^ rotl16(i, 7) ^ (i * i | 7)); // low cardinality, 28376/65536
//            result = (short) (i ^ rotl16(i, 3) ^ (i * i | 7)); // 29608/65536
//            result = (short) (i ^ i >>> 1 ^ (i * i | 7)); // 32768/65536
//            result = (short) (i + (i * i | 1)); // 65536/65536
//            result = (short) (i + (i * i | 2)); // 32768/65536
//            result = (short) (i + (i * i | 3)); // 65536/65536
//            result = (short) (i + (i * i | 4)); // 32768/65536
//            result = (short) (i + (i * i | 5)); // 65536/65536
//            result = (short) (i + (i * i | 25)); // 65536/65536
//            result = (short) (i + (i * i + 1 | 1)); // 65536/65536
//            result = (short) (i + (i * i + 2 | 1)); // 65536/65536 // adding an even "key" is the same as one odd key
//            result = (short) (i + (i * i + 3 | 7)); // 65536/65536
//            result = (short) (i + (i * i + 25 | 7)); // 65536/65536
//            result = (short) (i + (i * i + 0xDE4D | 7)); // 65536/65536
            result = (short) (i + (i * i + 0xDE4D | 25)); // 65536/65536
//            result = (short) (i + (i * i + i | 1)); // 10924/65536
//            result = (short) (i + (i * i + i | 7)); // 21164/65536
//            result = (short) (i + rotl16(i, 3) ^ (i * i | 7)); // 29502/65536
//            result = (short) (i * i + i ^ i); // 6978/65536
//            result = (short) ((i | 1) * ((i + 1 & 0xFFFF) >>> 1 & 0xFFFF)); // 65535/65536
//            result = (short) ((i | 1) * (i >> 1) + (i|1)); // 32768/65536
//            result = (short) (i * i + i >>> 1); // 65536/65536, does math in a larger modular field
//            result = (short) ((short)(i * i + i) >>> 1); // 32768/65536
//            result = (short) ((i * i + i & 0xFFFF) >>> 1); // 32768/65536
//            result = (short) ((short)(i * i + i) >> 1); // 32768/65536
//            result = (short) (rotl16(i * i + i & 0xFFFF, 1)); // 32768/65536
//            result = (short) (((i * i + i & 0xFFFF) >>> 1) ^ (i&0x0100) << 7); // 65536/65536
//            result = (short) (((i * i + i & 0xFFFF) >>> 1) ^ (i << 7 & 0x8000)); // 65536/65536
//            result = (short) (((i * i + i & 0xFFFF)) + (i >>> 8 & 1)); // 65536/65536
//            result = (short) (((i * i + i & 0xFFFF)) + 1 + (i >>> 8 & 1)); // 65536/65536
//            result = (short) (((i * i + i & 0xFFFF)) + 0xDEAD + (i >>> 8 & 1)); // 65536/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ (i >>> 8)); // 41168/65536
//            result = (short) (((i * i + i & 0xFFFF)) + ((short)i >> 8)); // 41344/65536
//            result = (short) (((i * i & 0xFFFF)) + ((short)i >> 8)); // 42007/65536
//            result = (short) (((i * i & 0xFFFF)) + (i >>> 8)); // 42007/65536
//            result = (short) (((i * i & 0xFFFF)) + (i >>> 8 & 1)); // 21164/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ ((short)i >> 8)); // 41748/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ rotl16(i, 15)); // 49084/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ rotl16(i, 1)); // 49724/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ rotl16(i, 8)); // 42424/65536
//            result = (short) (((i * i + i & 0xFFFF)) + rotl16(i, 8)); // 41919/65536
//            result = (short) (((i * i + i & 0xFFFF)) + i); // 10924/65536
//            result = (short) (((i * i + i & 0xFFFF)) + (i & 1)); // 65536/65536, NOTHING DOWNWARD!
//            result = (short) (((i * i + i & 0xFFFF)) + 1 - (i & 1)); // 65536/65536, NOTHING DOWNWARD!
//            result = (short) (((i * i + (i|1) & 0xFFFF))); // 65536/65536, XQO is related to AMP!
//            result = (short) (((i * i + (i|2) & 0xFFFF))); // 32768/65536 :(
//            result = (short) (((i * i + (i|3) & 0xFFFF))); // 32768/65536 :(
//            result = (short) (((i * i + (i|5) & 0xFFFF))); // 32768/65536 :(
//            result = (short) (((i * i + (i|7) & 0xFFFF))); // 24576/65536 :(
//            result = (short) (((i * i + (i^7) & 0xFFFF))); // 32768/65536 :(
//            result = (short) (((i * i + i & 0xFFFF)) + (i & 3)); // 32768/65536
//            result = (short) (((i * i + i & 0xFFFF)) ^ (i & 5)); // 32768/65536
//            result = (short) (((i * i + i & 0xFFFF)) + (i & 5)); // 32768/65536
//            result = (short) (((i * i + i & 0xFFFF)) + 5 + (i & 1)); // 65536/65536
//
//            s ^= (short) (((s * s + s & 0xFFFF)) + 1 + (s >>> 8 & 1));
//            if(s == 0) {
//                System.out.println("Repeats after " + (i+1) + " iterations");
//                break;
//            }
            xor ^= result;
            sum.add(result);
            all.add(result & 0xFFFF);
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
            result = (short) (i ^ (i * i | 7)); // it isn't clear yet how much improvement could be expected by non-1 operands...
//            result = (short) (i ^ (i * i | 9)); // but that this does work is surprising and interesting.
//            result = (short) (i ^ (i * i | 2)); // however, even operands don't work.
//            result = (state ^= (state * state | ((m += 0x666))));
//            state ^= (state * state | 1);
//            result += state ^= (state * state | 1); //Repeats after 131072 iterations
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
//                state = (short) (j-(state ^ (state * state | 5))); // for any even j, this is full-period also.
//                state = (short) -(state ^ (state * state | j)); // if (j & 7) is 5 or 7, this is full-period.
//                state = (short) -(state + (state * state | j)); // nothing full-period
                state = (short) (state - (state * state | j)); // if (j & 7) is 5 or 7, this is full-period.
//                state = (short) ((state * state + (state|j))); // nothing full-period
//                state = (short) -((state * state + (state|j))); // nothing full-period
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

    /**
     * Surprisingly, this shrunk-down RomuTrio has more than one "disaster" subcycle -- 0,0,0 was known already and can
     * be manually dealt with, but if there is any additional extremely-short subcycle in the full RomuTrio, that would
     * be a problem... Especially if only bad actors know which states have exploitably short periods.
     * <br>
     * Subcycle #0 has length: 1
     * SHORT CYCLE: 00000000
     * Subcycle #1 has length: 12222165
     * Subcycle #2 has length: 3021848
     * Subcycle #3 has length: 147066
     * Subcycle #4 has length: 673492
     * Subcycle #5 has length: 403883
     * Subcycle #6 has length: 31266
     * Subcycle #7 has length: 34567
     * Subcycle #8 has length: 138786
     * Subcycle #9 has length: 88438
     * Subcycle #10 has length: 15336
     * Subcycle #11 has length: 293
     * Subcycle #12 has length: 45
     * SHORT CYCLE: 0000201C, 001C1000, 008140F4, 0028315B, 00FE5B38, 00748C4A, 001D093C, 008237CF, 0069A136, 0019ADD3, 008D6B63, 0044209F, 0084062C, 00CF6BEC, 008CFD15, 00F1A3C4, 00C97F2B, 004951F3, 001F7973, 00D41885, 00974E5C, 0029CB2D, 00CB7A13, 002A9DA9, 00B1D3EE, 00DB976B, 0088B059, 001B5D58, 00D71419, 0078EFED, 003108A8, 002581EB, 00945AA7, 0047CE9C, 002FC8BD, 00EC2C35, 0018DFE4, 0027EF88, 00079D5D, 004D01FD, 008910DF, 002FC433, 006D4635, 00E4443F, 0014140C
     * Subcycle #13 has length: 25
     * SHORT CYCLE: 0001D628, 0065BADB, 00757F67, 00DE6017, 00CF25EA, 0055EC15, 002D5FB7, 00D9A27F, 00E68CA3, 004BA7C2, 00949729, 00BFB99C, 00C07465, 00893C40, 00A9F333, 00D60393, 007AC112, 0037BE5E, 002F810D, 00D5D135, 00807237, 00C1EC80, 00BAB11B, 00215A1E, 00F8F03B
     * Subcycle #14 has length: 4
     * SHORT CYCLE: 00407CDF, 009876C0, 0044DA08, 004D4B2C
     * Subcycle #15 has length: 1
     * SHORT CYCLE: 00EE779A
     * Longest cycle has length: 12222165
     */
    @Test
    public void testRomuTrioBytes() {
        final int bytes3 = 1 << 24;
        IntIntMap periods = new IntIntMap(bytes3);
        IntList seq = new IntList(bytes3), distinctCycles = new IntList(256);
        int longestCycle = -1;
        for (int i = 0; i < bytes3; i++) {
            if(periods.containsKey(i)) continue;
            seq.clear();
            int period = 0, state = i;
            do {
                seq.add(state);
                int stateA = state & 255;
                int fa = stateA;
                int stateB = state >>> 8 & 255;
                int stateC = state >>> 16 & 255;
                stateA = 0xDB * stateC & 255;
                stateC = stateC - stateB & 255;
                stateB = stateB - fa & 255;
                stateB = rotate8(stateB, 2);
                stateC = rotate8(stateC, 5);
                state = stateA | stateB << 8 | stateC << 16;
                period++;
            } while (state != i);
            for (int j = 0, n = seq.size(); j < n; j++) {
                periods.put(seq.get(j), period);
            }
            distinctCycles.add(period);
            longestCycle = Math.max(longestCycle, period);
            if(period < 50){
                System.out.println("SHORT CYCLE: " + seq.toString(", ", false, Base.BASE16::appendUnsigned));
            }
        }
        distinctCycles.sort();
        System.out.println("Cycles by length: " + distinctCycles);
    }

    /**
     * These cycles don't ever include the all-0 state, because the constructor already knows about it and prevents it.
     * The first number, such as "3 has..." is the multiplier used for that attempt.
     * Some multipliers are much better than others here!
     * Most have a length-1 cycle in addition to the all-0 state, but not all have such a bad case.
     * <br>
     * 3 has cycles by length: [1, 2, 6, 41, 120, 468, 3411, 3833, 4751, 120505, 169490, 285023, 1018848, 4017930, 11152786]
     * 5 has cycles by length: [1, 5, 19, 27, 86, 3547, 3881, 12657, 23045, 66191, 66799, 73755, 1587883, 4410068, 10529251]
     * 7 has cycles by length: [1, 2, 2, 2, 4, 4, 4, 8, 23, 60, 103, 577, 1108, 3043, 5981, 9217, 61084, 464780, 515575, 536208, 841014, 2560677, 2709291, 3396706, 5671741]
     * 9 has cycles by length: [1, 5, 7, 19, 29, 209, 399, 858, 1731, 1853, 3778, 45973, 127301, 202999, 352014, 553472, 1157430, 1664166, 3252432, 3959084, 5453455]
     * 11 has cycles by length: [1, 1, 27, 34, 77, 212, 3736, 5208, 11313, 47498, 181423, 826082, 1360180, 6866674, 7474749]
     * 13 has cycles by length: [3, 6, 8, 10, 35, 35, 189, 731, 3666, 4370, 9864, 16325, 22976, 30005, 30550, 73157, 145680, 160360, 653505, 1118198, 14507542]
     * 15 has cycles by length: [1, 36, 47, 58, 124, 398, 774, 1242, 2123, 19168, 98149, 190827, 283509, 1056736, 15124023]
     * 17 has cycles by length: [5, 121, 177, 582, 1276, 670220, 714133, 1242411, 1482239, 4075698, 8590353]
     * 19 has cycles by length: [2, 2, 2, 2, 4, 4, 4, 4, 10, 14, 19, 33, 58, 61, 79, 7782, 15472, 26995, 53252, 291697, 455603, 911352, 15014764]
     * 21 has cycles by length: [1, 3, 3, 6, 95, 141, 449, 1101, 1341, 3297, 48556, 102070, 614827, 6745606, 9259719]
     * 23 has cycles by length: [1, 5, 11, 25, 384, 7034, 17555, 34082, 63535, 83038, 140089, 163533, 570125, 651659, 653704, 721764, 843740, 1171184, 1584827, 2315703, 7755217]
     * 25 has cycles by length: [1, 2, 4, 6, 15, 29, 30, 106, 159, 161, 337, 20491, 22701, 45407, 58986, 102245, 2293705, 2560550, 11672280]
     * 27 has cycles by length: [1, 1, 48, 127, 140, 388, 1568, 2176, 5278, 10266, 10424, 16956, 39292, 541326, 2366918, 2773196, 11009110]
     * 29 has cycles by length: [1, 2, 6, 6, 14, 17, 33, 55, 104, 1519, 3435, 8714, 27162, 37289, 61343, 88522, 128047, 1121611, 1672922, 6016823, 7609590]
     * 31 has cycles by length: [1, 5, 11, 30, 38, 39, 225, 1103, 1229, 3907, 4277, 9887, 25563, 27259, 186106, 396407, 1287754, 2178382, 12654992]
     * 33 has cycles by length: [1, 2, 4, 7, 37, 760, 5141, 23183, 46328, 47106, 62542, 71595, 127766, 141704, 3551634, 4565763, 8133642]
     * 35 has cycles by length: [2, 7, 88, 100, 108, 437, 837, 972, 2492, 8761, 10957, 48283, 435294, 665581, 944624, 4644210, 10014462]
     * 37 has cycles by length: [1, 7, 12, 53, 431, 3095, 5836, 9429, 15955, 65559, 162418, 195040, 495771, 947445, 1019929, 1182323, 1443546, 4615813, 6614552]
     * 39 has cycles by length: [1, 1, 4, 17, 974, 1668, 1854, 2420, 2603, 3519, 11709, 90263, 220829, 1811237, 2434386, 3863162, 8332568]
     * 41 has cycles by length: [1, 1, 1, 2, 23, 109, 111, 1331, 1934, 5029, 13310, 32133, 251084, 385734, 1863661, 2439667, 11783084]
     * 43 has cycles by length: [1, 15, 21, 29, 193, 742, 1330, 3071, 3573, 7877, 27736, 651034, 16081593]
     * 45 has cycles by length: [2, 2, 5, 11, 18, 21, 486, 631, 1418, 1621, 1952, 2876, 6873, 38538, 198727, 452584, 457608, 1035087, 1659205, 3874896, 9044654]
     * 47 has cycles by length: [1, 2, 9, 10, 23, 213, 744, 1420, 9612, 79479, 128418, 334180, 16223104]
     * 49 has cycles by length: [1, 8, 15, 34, 188, 696, 1159, 10097, 331653, 3371132, 13062232]
     * 51 has cycles by length: [1, 2, 2, 3, 4, 28, 31, 56, 72, 92, 309, 383, 1691, 2818, 3864, 4221, 7918, 22114, 48165, 893586, 1184046, 3904756, 10703053]
     * 53 has cycles by length: [1, 1, 1, 6, 11, 13, 15, 843, 1283, 2580, 14430, 15972, 56176, 61966, 99285, 650588, 1093476, 2543610, 12236958]
     * 55 has cycles by length: [1, 1, 2, 2, 5, 11, 172, 278, 2103, 3795, 3912, 4159, 8901, 9778, 11173, 21692, 25782, 36687, 152493, 346777, 349850, 572396, 2979593, 4842075, 7405577]
     * 57 has cycles by length: [1, 15, 18, 1009, 19995, 23478, 48747, 81482, 87743, 208217, 1100732, 1829290, 13376488]
     * 59 has cycles by length: [63, 181, 1048, 6357, 7934, 14255, 19690, 110698, 1857930, 2486951, 12272108]
     * 61 has cycles by length: [1, 3, 3, 6, 10, 25, 404, 1376, 10170, 10170, 11336, 18924, 329167, 811944, 3136030, 4047618, 8400028]
     * 63 has cycles by length: [1, 1, 7, 8, 49, 88, 415, 748, 10818, 16787, 24702, 124779, 498566, 1977832, 2439655, 5554466, 6128293]
     * 65 has cycles by length: [1, 84, 442, 670, 782, 990, 1252, 1371, 3319, 6302, 27342, 32868, 47256, 90597, 343084, 382625, 15838230]
     * 67 has cycles by length: [2, 8, 8, 38, 43, 91, 191, 1161, 1352, 3698, 7493, 84373, 97336, 102638, 120983, 510523, 1375147, 1535573, 4041906, 4310280, 4584371]
     * 69 has cycles by length: [2, 3, 4, 5, 6, 8, 47, 57, 162, 200, 1196, 1471, 2888, 4274, 5764, 6033, 14080, 15570, 19621, 39076, 117622, 463579, 602857, 1181636, 2607018, 4411119, 7282917]
     * 71 has cycles by length: [1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 6, 6, 14, 16, 65, 130, 569, 762, 1693, 9548, 19613, 25600, 65933, 93377, 3548113, 13011742]
     * 73 has cycles by length: [13, 34, 236, 462, 621, 2008, 2446, 2866, 38070, 51936, 137137, 428776, 980409, 1024523, 1679774, 5697545, 6730359]
     * 75 has cycles by length: [1, 1, 3, 4, 9, 23, 86, 123, 218, 1486, 2399, 3665, 4317, 24554, 53062, 63209, 66064, 88538, 166371, 257444, 339527, 405084, 540032, 592706, 700669, 829946, 2653210, 3276811, 6707653]
     * 77 has cycles by length: [7, 20, 65, 90, 200, 1788, 4167, 8741, 10403, 10461, 13730, 14734, 15248, 72752, 277513, 640745, 912482, 4733632, 10060437]
     * 79 has cycles by length: [3, 9, 44, 112, 386, 22725, 260621, 432264, 528779, 758344, 1314126, 1793086, 11666716]
     * 81 has cycles by length: [22, 32, 44, 111, 119, 450, 6301, 8333, 12996, 191985, 265799, 1223844, 2559642, 2672239, 9835298]
     * 83 has cycles by length: [2, 166, 2375, 33060, 45833, 67754, 1464006, 6907271, 8256748]
     * 85 has cycles by length: [1, 7, 7, 12, 25, 322, 335, 706, 1996, 4349, 11487, 21067, 47514, 78655, 274610, 307005, 16029117]
     * 87 has cycles by length: [10, 11, 12, 13, 21, 29, 805, 829, 6928, 14141, 17009, 18365, 51205, 7769997, 8897840]
     * 89 has cycles by length: [1, 11, 94, 104, 137, 329, 556, 982, 16928, 297099, 339145, 2162804, 2211572, 2288321, 9459132]
     * 91 has cycles by length: [1, 1, 6, 14, 24, 139, 4698, 6975, 12670, 21207, 204308, 337582, 16189590]
     * 93 has cycles by length: [1, 3, 4, 59, 76, 118, 196, 1098, 2054, 3413, 8361, 9105, 43828, 158892, 190302, 325514, 417617, 1427975, 14188599]
     * 95 has cycles by length: [1, 40, 77, 1669, 2049, 11431, 29082, 49951, 68939, 114816, 483852, 703002, 997842, 1409043, 2433889, 2721387, 7750145]
     * 97 has cycles by length: [1, 1, 2, 3, 10, 105, 1436, 6758, 8702, 10244, 10805, 15663, 155538, 187309, 489626, 518890, 2379616, 2551173, 2628948, 2724800, 5087585]
     * 99 has cycles by length: [1, 1, 2, 3, 8, 13, 19, 234, 285, 872, 1606, 2503, 4416, 6834, 99437, 112212, 226951, 576649, 726124, 1348811, 3187215, 4105933, 6377086]
     * 101 has cycles by length: [1, 1, 2, 9, 13, 18, 166, 473, 588, 3585, 6689, 8745, 14500, 17920, 29195, 67515, 320857, 5649936, 10657002]
     * 103 has cycles by length: [1, 2, 2, 2, 2, 55, 149, 225, 544, 588, 1450, 1657, 15338, 39221, 182573, 3598441, 12936965]
     * 105 has cycles by length: [1, 5, 5, 6, 7, 8, 13, 15, 24, 134, 148, 172, 391, 3133, 5138, 6302, 6611, 45388, 77342, 87622, 119416, 1008204, 3441158, 4375446, 7600526]
     * 107 has cycles by length: [2, 3, 20, 374, 1822, 9417, 13375, 620248, 16131954]
     * 109 has cycles by length: [2, 9, 10, 27, 248, 602, 2627, 10971, 11683, 25212, 128621, 260465, 2875602, 3136339, 10324797]
     * 111 has cycles by length: [6, 15, 18, 20, 289, 866, 9293, 9499, 26658, 49670, 117999, 2550793, 14012089]
     * 113 has cycles by length: [1, 2, 7, 45, 113, 210, 400, 446, 1191, 2487, 39872, 8359436, 8373005]
     * 115 has cycles by length: [1, 2, 8, 40, 96, 104, 228, 297, 325, 2764, 9672, 74294, 141975, 227172, 1548059, 2234868, 12537310]
     * 117 has cycles by length: [14, 35, 96, 301, 2183, 6050, 26121, 50019, 93439, 322143, 364803, 586514, 15325497]
     * 119 has cycles by length: [11, 28, 226, 58522, 316063, 369973, 3973392, 5999230, 6059770]
     * 121 has cycles by length: [2, 7, 14, 14, 221, 374, 517, 976, 1126, 2961, 5301, 13809, 17941, 28247, 505512, 1389944, 14810249]
     * 123 has cycles by length: [1, 1, 2, 3, 5, 51, 87, 821, 1212, 2843, 3649, 37384, 192430, 473996, 1284533, 3112521, 11667676]
     * 125 has cycles by length: [1, 1, 2, 3, 7, 7, 9, 22, 22, 37, 54, 200, 458, 1676, 3389, 96568, 121131, 257638, 803180, 2954970, 12537840]
     * 127 has cycles by length: [1, 3, 31, 87, 123, 956, 2225, 3599, 8282, 11367, 14811, 21283, 44940, 74580, 339574, 352801, 2927227, 5266364, 7708961]
     * 129 has cycles by length: [3, 3, 3, 4, 5, 43, 52, 188, 513, 5627, 8461, 49845, 135601, 291865, 656500, 4119857, 11508645]
     * 131 has cycles by length: [1, 2, 3, 4, 5, 7, 39, 138, 166, 201, 228, 513, 837, 1535, 2596, 2796, 3817, 8292, 167585, 331423, 540003, 5712231, 10004793]
     * 133 has cycles by length: [1, 1, 1, 1, 2, 5, 19, 127, 144, 191, 197, 889, 87500, 194078, 448936, 1182434, 1251309, 4338870, 9272510]
     * 135 has cycles by length: [1, 1, 2, 2, 2, 2, 3, 6, 195, 608, 1711, 12129, 32681, 90562, 155674, 566336, 3568381, 4689486, 7659433]
     * 137 has cycles by length: [1, 4, 4, 28, 623, 907, 1152, 1950, 8848, 10225, 55200, 159516, 263643, 1681964, 1949846, 4498860, 8144444]
     * 139 has cycles by length: [1, 1, 5, 6, 20, 30, 94, 131, 4111, 4175, 7904, 12238, 26513, 68244, 93708, 95890, 104262, 133548, 337420, 5014906, 10874008]
     * 141 has cycles by length: [38, 299, 403, 432, 3845, 7524, 128798, 552078, 1750635, 3078170, 11254993]
     * 143 has cycles by length: [1, 4, 8, 14, 15, 56, 94, 143, 938, 1587, 1737, 77428, 142046, 6923452, 9629692]
     * 145 has cycles by length: [1, 4, 6, 59, 545, 878, 2033, 2732, 3282, 3695, 13750, 22800, 83470, 468641, 716372, 4906988, 10551959]
     * 147 has cycles by length: [2, 2, 2, 4, 9, 12, 15, 23, 42, 61, 80, 806, 16409, 32553, 45288, 111744, 144806, 229929, 930254, 2172503, 2185956, 4929432, 5977283]
     * 149 has cycles by length: [166, 384, 1184, 1932, 10656, 31877, 50709, 765802, 15914505]
     * 151 has cycles by length: [7, 15, 31, 74, 117, 119, 140, 578, 12448, 29812, 43312, 224598, 586335, 1411239, 2712171, 5824780, 5931439]
     * 153 has cycles by length: [1, 1, 3, 4, 4, 6, 17226, 47718, 199201, 337100, 2414210, 3256083, 10505658]
     * 155 has cycles by length: [1, 2, 6, 7, 18, 31, 32, 66, 882, 1059, 1537, 3588, 9936, 16909, 21215, 32667, 85360, 105216, 817740, 1987083, 13693860]
     * 157 has cycles by length: [3, 33, 160, 226, 788, 1583, 3350, 9102, 790892, 3564511, 12406567]
     * 159 has cycles by length: [1, 7, 7, 12, 23, 26, 28, 29, 52, 142, 150, 329, 672, 1915, 3515, 3888, 21588, 53377, 58730, 70270, 268115, 704423, 720138, 6283326, 8586452]
     * 161 has cycles by length: [1, 1, 2, 11, 19, 22, 142, 550, 1424, 3075, 7062, 11883, 59596, 75975, 95598, 2582918, 13938936]
     * 163 has cycles by length: [2, 3, 3, 3, 4, 40, 51, 63, 530, 662, 769, 1127, 2115, 3539, 30928, 59628, 60111, 214711, 231122, 239733, 501896, 552796, 2332287, 2918585, 9626507]
     * 165 has cycles by length: [1, 7, 8, 30, 42, 43, 113, 161, 169, 286, 346, 970, 1451, 3591, 5627, 6228, 12799, 25295, 33426, 84080, 210631, 994853, 1333387, 1980822, 12082849]
     * 167 has cycles by length: [1, 4, 5, 6, 8, 9, 10, 23, 63, 324, 1334, 3061, 3579, 49632, 85621, 146862, 536440, 771005, 4087912, 4552652, 6538664]
     * 169 has cycles by length: [1, 1, 13, 126, 155, 262, 341, 612, 1140, 2011, 2725, 2881, 4558, 6563, 17307, 1263158, 15475361]
     * 171 has cycles by length: [1, 9, 151, 221, 1791, 3922, 5519, 6361, 8598, 10423, 10923, 736688, 1169202, 2796180, 3676135, 3739167, 4611924]
     * 173 has cycles by length: [5, 9, 14, 728, 925, 2032, 104178, 232553, 1296108, 6317157, 8823506]
     * 175 has cycles by length: [2, 3, 5, 5, 1252, 2635, 3376, 5182, 9308, 11918, 60463, 270572, 276464, 458057, 1763752, 1963222, 2084598, 4328395, 5538006]
     * 177 has cycles by length: [2, 11, 11, 45, 88, 88, 98, 102, 110, 835, 1330, 9483, 14414, 39237, 45260, 59386, 2879944, 6532826, 7193945]
     * 179 has cycles by length: [2, 2, 3, 5, 21, 33, 58, 8253, 24085, 28471, 200274, 257306, 1161199, 5796490, 9301013]
     * 181 has cycles by length: [1, 1, 2, 2, 2, 2, 3, 5, 6, 12, 14, 97, 125, 514, 643, 923, 1167, 5893, 159551, 2424535, 14183717]
     * 183 has cycles by length: [1, 1, 1, 2, 10, 13, 22, 54, 106, 170, 205, 339, 634, 2883, 4169, 2616985, 14151620]
     * 185 has cycles by length: [1, 1, 4, 13, 27, 129, 352, 352, 1707, 1747, 1948, 5082, 5360, 5846, 6109, 42169, 47228, 150829, 324374, 399912, 602972, 6427264, 8753789]
     * 187 has cycles by length: [3, 3, 37, 166, 179, 508, 1361, 1604, 6827, 20069, 36561, 376427, 2640892, 2868492, 10824086]
     * 189 has cycles by length: [1, 21, 21, 22, 25, 385, 1262, 3017, 4060, 10405, 16490, 38238, 850997, 989927, 14862344]
     * 191 has cycles by length: [1, 1, 3, 3, 27, 35, 61, 89, 1056, 2445, 3285, 4466, 6528, 12107, 55371, 189358, 261984, 394958, 2190927, 2906238, 10748272]
     * 193 has cycles by length: [1, 7, 12, 201, 249, 366, 944, 5682, 8552, 90916, 105542, 221175, 370550, 718460, 3167176, 5718904, 6368478]
     * 195 has cycles by length: [2, 2, 21, 224, 293, 375, 824, 1100, 2393, 24910, 29037, 91610, 161109, 186063, 227623, 336414, 525661, 775893, 817559, 1489989, 12106113]
     * 197 has cycles by length: [2, 3, 3, 44, 44, 161, 203, 496, 538, 2203, 7743, 22009, 47106, 256012, 334791, 357413, 2014316, 6593357, 7140771]
     * 199 has cycles by length: [1, 2, 2, 2, 4, 5, 10, 12, 14, 20, 28, 53, 127, 133, 606, 1185, 2729, 2767, 10738, 11877, 18099, 56332, 59460, 62953, 131991, 216680, 4170999, 4482909, 7547477]
     * 201 has cycles by length: [1, 4, 88, 312, 356, 545, 587, 3243, 4846, 7780, 28466, 42770, 49003, 69221, 1094497, 5739854, 9735642]
     * 203 has cycles by length: [1, 1, 4, 11, 81, 314, 1164, 1896, 11310, 13856, 22539, 73273, 110584, 410531, 602188, 2471505, 13057957]
     * 205 has cycles by length: [22, 34, 60, 434, 712, 1085, 6873, 16057, 24293, 28327, 71928, 177545, 981276, 1117690, 1644871, 2634284, 10071724]
     * 207 has cycles by length: [3, 171, 716, 1001, 1066, 1477, 2903, 5180, 5605, 25337, 28091, 55782, 66849, 109553, 111825, 2269625, 14092031]
     * 209 has cycles by length: [1, 5, 34, 136, 350, 564, 1867, 2257, 8365, 10741, 16161, 28097, 38845, 77364, 1053519, 2010005, 2668464, 3726128, 7134312]
     * 211 has cycles by length: [2, 3, 7, 7, 11, 21, 32, 140, 812, 2332, 10013, 26217, 281334, 2219826, 14236458]
     * 213 has cycles by length: [6, 10, 111, 123, 478, 4005, 9344, 86738, 393245, 695021, 15588134]
     * 215 has cycles by length: [1, 4, 8, 35, 72, 115, 807, 2976, 5205, 6431, 15075, 20274, 72220, 80045, 134559, 167922, 391929, 970404, 1040071, 4638940, 9230122]
     * 217 has cycles by length: [1, 3, 142, 1073, 4942, 11182, 12205, 14290, 14548, 182586, 318702, 340391, 1732460, 1738006, 2226661, 5031291, 5148732]
     * 219 has cycles by length: [1, 4, 25, 45, 293, 15336, 31266, 34567, 88438, 138786, 147066, 403883, 673492, 3021848, 12222165]
     * 221 has cycles by length: [1, 4, 43, 48, 100, 104, 385, 5594, 35032, 128812, 2520198, 5540451, 8546443]
     * 223 has cycles by length: [1, 8, 91, 416, 1058, 5166, 18537, 184263, 198496, 205459, 265689, 358163, 750048, 1649215, 13140605]
     * 225 has cycles by length: [1, 3, 3, 4, 5, 40, 70, 149, 397, 661, 1462, 13435, 36255, 39119, 113207, 211657, 411654, 491802, 2759546, 4650495, 8047250]
     * 227 has cycles by length: [1, 1, 1, 2, 4, 4, 5, 6, 10, 12, 56, 323, 340, 356, 893, 1199, 1337, 2176, 4188, 7197, 10124, 26011, 52913, 333586, 1119619, 1387047, 3059284, 4109033, 6661487]
     * 229 has cycles by length: [1, 7, 40, 89, 2029, 21620, 107762, 713633, 2365942, 5889918, 7676174]
     * 231 has cycles by length: [1, 1, 2, 2, 2, 4, 4, 5, 21, 68, 72, 475, 9070, 5198014, 11569474]
     * 233 has cycles by length: [1, 4, 6, 64, 126, 185, 260, 422, 1715, 2360, 5273, 17682, 91984, 111166, 116903, 190432, 204343, 633671, 5068775, 5129049, 5202794]
     * 235 has cycles by length: [6, 8, 9, 17, 329, 2292, 4451, 53399, 71077, 115432, 257625, 371023, 698272, 882856, 979581, 1089741, 1793780, 4161887, 6295430]
     * 237 has cycles by length: [2, 228, 544, 2823, 5093, 11137, 45763, 49188, 142403, 485965, 16034069]
     * 239 has cycles by length: [3, 25, 65, 275, 325, 21053, 159290, 459902, 489757, 838702, 2616412, 4694188, 7497218]
     * 241 has cycles by length: [1, 1, 7, 10, 15, 22, 9574, 24317, 41731, 526683, 879682, 908614, 3720690, 4309847, 6356021]
     * 243 has cycles by length: [1, 2, 5, 6, 9, 11, 467, 574, 3505, 18027, 30554, 42365, 137668, 505527, 517282, 1925432, 3156763, 5005720, 5433297]
     * 245 has cycles by length: [38, 105, 150, 189, 228, 1000, 3356, 3597, 14420, 16231, 37504, 74998, 100205, 212190, 258502, 3079207, 12975295]
     * 247 has cycles by length: [3, 357, 430, 3221, 3701, 16303, 20616, 39396, 122387, 246997, 479753, 5965955, 9878096]
     * 249 has cycles by length: [2, 2, 2, 4, 4, 56, 124, 235, 615, 706, 823, 3914, 9413, 9849, 20047, 25690, 1174313, 4101961, 11429455]
     * 251 has cycles by length: [1, 1, 2, 26, 1341, 1688, 4854, 6631, 11854, 13550, 47939, 75469, 196936, 6606566, 9810357]
     * 253 has cycles by length: [4, 7, 8, 83, 165, 900, 1271, 1533, 1909, 3420, 4009, 4861, 7519, 15792, 193009, 359805, 382837, 767649, 15032434]
     * 255 has cycles by length: [1, 1, 1, 47, 52, 93, 113, 28893, 256985, 339006, 566887, 1413769, 14171367]
     * <br>
     * Multiplying stateC by the listed multiplier and adding 1, making an LCG-like change:
     * <br>
     * 3 has cycles by length: [1, 1, 6, 8, 10, 20, 62, 112, 2500, 2532, 2967, 4838, 22238, 359281, 392789, 1031854, 1421424, 2009176, 2666779, 8860618]
     * 5 has cycles by length: [4, 19, 36, 67, 82, 109, 127, 196, 203, 2208, 3486, 11751, 20666, 78336, 79755, 16580171]
     * 7 has cycles by length: [1, 4, 4, 4, 4, 5, 5, 6, 16, 30, 41, 74, 76, 90, 184, 449, 786, 948, 1042, 16588, 84536, 115690, 2706295, 13850338]
     * 9 has cycles by length: [5, 44, 68, 116, 438, 16776545]
     * 11 has cycles by length: [24, 49, 99, 439, 582, 1786, 11853, 63604, 1738842, 2177710, 3278191, 9504037]
     * 13 has cycles by length: [1, 1, 9, 23, 32, 222, 315, 467, 611, 847, 988, 1086, 8061, 14418, 29570, 29807, 34568, 36689, 49403, 52702, 114630, 508726, 941531, 962512, 2322375, 11667622]
     * 15 has cycles by length: [1, 1, 4, 19, 28, 41, 295, 1182, 15456, 30101, 49668, 109110, 119230, 133723, 175848, 216759, 314715, 1113646, 1214537, 13282852]
     * 17 has cycles by length: [1, 1, 26, 91, 103, 766, 6556, 11553, 13879, 19712, 67839, 93892, 104645, 366966, 848731, 892707, 1504390, 12845358]
     * 19 has cycles by length: [1, 1, 1, 1, 6, 33, 69, 79, 178, 229, 307, 511, 686, 3362, 3742, 3755, 11303, 40750, 41036, 73884, 3108343, 13488939]
     * 21 has cycles by length: [7, 9, 11, 30, 93, 100, 112, 167, 2674, 6928, 73018, 496283, 919991, 1070392, 3484422, 10722979]
     * 23 has cycles by length: [3, 6, 6, 9, 9, 41, 54, 228, 295, 328, 394, 591, 3399, 5651, 15326, 30755, 63675, 146684, 273393, 440330, 1086274, 1104257, 3976241, 9629267]
     * 25 has cycles by length: [2, 5, 6, 7, 275, 1538, 1679, 1939, 2510, 2637, 3140, 10521, 16207, 26137, 28537, 66794, 288274, 16327008]
     * 27 has cycles by length: [1, 7, 14, 37, 1088, 5238, 12899, 20400, 490299, 2151265, 5253311, 8842657]
     * 29 has cycles by length: [1, 1, 2, 2, 2, 5, 6, 6, 6, 6, 6, 7, 13, 479, 8357, 105499, 351848, 641444, 858806, 1638326, 6280123, 6892271]
     * 31 has cycles by length: [1, 2, 12, 36, 704, 4774, 26259, 79720, 82899, 108958, 882972, 1396893, 5425342, 8768644]
     * 33 has cycles by length: [4, 4, 10, 16, 62, 71, 303, 12343, 102005, 906011, 6610501, 9145886]
     * 35 has cycles by length: [3, 73, 256, 526, 2404, 4256, 9723, 19746, 144238, 235369, 438530, 648422, 5376297, 9897373]
     * 37 has cycles by length: [7, 12, 14, 18, 142, 321, 598, 1922, 3651, 8423, 15629, 86431, 103082, 137183, 715601, 3313927, 6016485, 6373770]
     * 39 has cycles by length: [1, 4, 23, 60, 88, 120, 603, 6356, 16307, 97577, 528238, 707769, 3167746, 12252324]
     * 41 has cycles by length: [1, 1, 1, 11, 38, 42, 43, 67, 97, 289, 447, 2518, 5788, 11444, 16991, 137972, 559967, 813789, 1031344, 1041966, 6313222, 6841178]
     * 43 has cycles by length: [1, 3, 3, 4, 6, 45, 82, 87, 89, 186, 619, 729, 1021, 3654, 8372, 9074, 13684, 22176, 92560, 16624821]
     * 45 has cycles by length: [1, 3, 7, 7, 24, 27, 60, 465, 1512, 1875, 2103, 2335, 9094, 13359, 71137, 404790, 1146956, 15123461]
     * 47 has cycles by length: [1, 2, 48, 91, 216, 525, 1119, 1374, 17937, 57496, 617411, 1045074, 1189524, 13846398]
     * 49 has cycles by length: [1, 2, 3, 4, 11, 33, 57, 60, 147, 152, 203, 302, 885, 2123, 4824, 32053, 52419, 3861917, 4981618, 7840402]
     * 51 has cycles by length: [1, 3, 199, 950, 24280, 16751783]
     * 53 has cycles by length: [1, 1, 4, 26, 33, 43, 44, 68, 99, 316, 682, 1342, 2624, 4753, 28408, 78458, 94349, 209740, 442279, 946102, 1016746, 4269129, 4506161, 5175808]
     * 55 has cycles by length: [1, 1, 9, 18, 48, 57, 90, 211, 434, 446, 1465, 9998, 12778, 23073, 35356, 44017, 49426, 513539, 591013, 1327258, 7042802, 7125176]
     * 57 has cycles by length: [3, 60, 312, 470, 1249, 3003, 27062, 35213, 42899, 74179, 106916, 255103, 382218, 738374, 1203708, 1244926, 2983658, 9677863]
     * 59 has cycles by length: [49, 114, 657, 1938, 3006, 3534, 6373, 7472, 12683, 113443, 256461, 771314, 1145338, 2873138, 5378973, 6202723]
     * 61 has cycles by length: [3, 3, 3, 4, 5, 5, 230, 5691, 6010, 50585, 64704, 175890, 342995, 1266641, 1684263, 13180184]
     * 63 has cycles by length: [1, 21, 28, 34, 64, 97, 130, 304, 69594, 548535, 607610, 2606706, 2882684, 10061408]
     * 65 has cycles by length: [1, 1, 2, 3, 6, 6, 12, 225, 348, 622, 2992, 11620, 44861, 59425, 1068445, 15588647]
     * 67 has cycles by length: [1, 2, 2, 2, 8, 29, 79, 304, 411, 1147, 2960, 5968, 25368, 16740935]
     * 69 has cycles by length: [1, 1, 2, 2, 5, 6, 79, 201, 318, 410, 811, 6064, 43864, 60641, 86468, 167368, 336106, 544368, 1466353, 2209176, 5637059, 6217913]
     * 71 has cycles by length: [2, 2, 2, 2, 10, 11, 23, 25, 28, 41, 67, 86, 90, 103, 385, 15915, 81289, 104337, 114255, 132254, 173909, 658783, 2340896, 13154701]
     * 73 has cycles by length: [11, 13, 46, 219, 300, 1273, 1511, 32574, 241635, 585714, 4422232, 11491688]
     * 75 has cycles by length: [5, 7, 10, 12, 19, 20, 284, 704, 3837, 57531, 180122, 257350, 268499, 16008816]
     * 77 has cycles by length: [1, 1, 5, 72, 215, 286, 827, 1448, 2112, 5123, 5557, 35604, 70720, 190946, 780677, 15683622]
     * 79 has cycles by length: [1, 1, 1, 4, 40, 43, 69, 331, 843, 8080, 24324, 39249, 55689, 73612, 269496, 1339728, 7317985, 7647720]
     * 81 has cycles by length: [2, 6, 10, 18, 181, 682, 1696, 1701, 4652, 31017, 121691, 125899, 150183, 718256, 855100, 1736179, 2219364, 10810579]
     * 83 has cycles by length: [1, 1, 2, 4, 9, 10, 10, 12, 55, 160, 417, 544, 885, 885, 894, 1479, 1674, 2440, 5058, 5207, 5589, 9962, 14774, 330281, 1132534, 2294528, 3780487, 9189314]
     * 85 has cycles by length: [86, 537, 3055, 3254, 17947, 21918, 68931, 129121, 143647, 394595, 1233020, 14761105]
     * 87 has cycles by length: [1, 9, 15, 22, 22, 112, 273, 1681, 2867, 3074, 4894, 13869, 18205, 30071, 76159, 90123, 101048, 16434771]
     * 89 has cycles by length: [1, 3, 3, 4, 5, 8, 72, 87, 120, 6278, 10938, 12364, 13559, 49469, 209851, 214248, 224208, 308087, 857203, 14870708]
     * 91 has cycles by length: [3, 6, 10, 27, 59, 20746, 21656, 46232, 61608, 146292, 643947, 958902, 1057933, 1105469, 1649530, 11064796]
     * 93 has cycles by length: [1, 2, 21, 132, 269, 351, 441, 744, 1012, 1032, 2468, 2492, 3153, 12358, 93625, 93757, 143956, 186146, 1552237, 14683019]
     * 95 has cycles by length: [12, 17, 44, 383, 466, 1043, 1383, 11527, 42298, 231532, 749141, 868188, 1859311, 2074553, 3584280, 7353038]
     * 97 has cycles by length: [2, 10, 82, 144, 461, 559, 580, 2451, 64332, 100364, 4491353, 12116878]
     * 99 has cycles by length: [3, 50, 153, 7326, 20668, 171709, 1488095, 1673471, 5719861, 7695880]
     * 101 has cycles by length: [1, 5, 11, 30, 36, 56, 22887, 86810, 109358, 465399, 3055208, 3516741, 4103082, 5417592]
     * 103 has cycles by length: [1, 2, 3, 3, 9, 9, 22, 115, 196, 1214, 2090, 8166, 11963, 13290, 80696, 275550, 292981, 1842385, 2176752, 12071769]
     * 105 has cycles by length: [1, 21, 54, 56, 190, 364, 1965, 2039, 2703, 4002, 15529, 22403, 207278, 275202, 305480, 323071, 926917, 2655559, 3592479, 8441903]
     * 107 has cycles by length: [2, 36, 263, 1028, 2088, 2645, 3419, 9076, 11515, 20233, 48997, 63146, 76686, 408741, 6361317, 9768024]
     * 109 has cycles by length: [1, 3, 5, 54, 612, 1213, 68275, 84984, 311399, 1671363, 2769203, 11870104]
     * 111 has cycles by length: [1, 2, 3, 10, 82, 158, 3343, 45769, 121525, 167185, 263843, 1447280, 3239735, 11488280]
     * 113 has cycles by length: [1, 3, 3, 5, 8, 13, 14, 18, 70, 82, 360, 444, 517, 919, 7411, 221818, 239972, 415138, 2712499, 13177921]
     * 115 has cycles by length: [1, 1, 3, 5, 6, 15, 67, 69, 121, 212, 2113993, 14662723]
     * 117 has cycles by length: [10, 24, 52, 114, 255, 3308, 4403, 22170, 328050, 817727, 846129, 2674293, 3025157, 9055524]
     * 119 has cycles by length: [6, 8, 11, 15, 19, 20, 59, 122, 177, 633, 716, 1203, 1470, 7890, 13275, 24539, 39309, 112585, 187370, 362696, 446068, 4513097, 4636689, 6429239]
     * 121 has cycles by length: [2, 2, 9, 225, 2215, 6391, 30457, 69357, 110879, 185538, 185764, 212944, 396335, 548718, 842820, 14185560]
     * 123 has cycles by length: [1, 4, 30, 30, 177, 555, 721, 1105, 1396, 2633, 8462, 143748, 197289, 474715, 776920, 2280552, 4759152, 8129726]
     * 125 has cycles by length: [5, 6, 59, 304, 2579, 11132, 16123, 46714, 59713, 76917, 1172486, 2573094, 6307067, 6511017]
     * 127 has cycles by length: [5, 14, 22, 74, 178, 4065, 7390, 7944, 8717, 313127, 1046370, 2074800, 2097274, 3244019, 3986268, 3986949]
     * 129 has cycles by length: [1, 1, 2, 2, 3, 4, 9, 11, 618, 658, 2578, 4416, 26344, 241763, 1958126, 14542680]
     * 131 has cycles by length: [1, 3, 16, 20, 147, 1292, 3420, 4305, 115703, 16652309]
     * 133 has cycles by length: [186, 194, 241, 824, 14589, 90463, 995101, 15675618]
     * 135 has cycles by length: [1, 1, 2, 2, 2, 2, 3, 6, 13, 16, 18, 20, 312, 448, 1962, 4646, 34019, 243780, 1683176, 14808787]
     * 137 has cycles by length: [1, 2, 12, 367, 913, 1068, 9964, 35002, 162442, 833988, 6902235, 8831222]
     * 139 has cycles by length: [2, 2, 20, 39, 292, 307, 307, 1776, 4644, 21206, 72545, 106452, 346816, 608073, 2709768, 12904967]
     * 141 has cycles by length: [1, 11, 36, 40, 282, 481, 1858, 1904, 30672, 63957, 71863, 1833846, 3606374, 11165891]
     * 143 has cycles by length: [1, 9, 18, 58, 126, 147, 974, 2619, 7806, 21399, 26910, 2110081, 5741707, 8865361]
     * 145 has cycles by length: [10, 11, 16, 65, 540, 2193, 4000, 4117, 4337, 21629, 388048, 826887, 1836712, 1870094, 2227932, 2393481, 3340489, 3856655]
     * 147 has cycles by length: [1, 1, 3, 4, 6, 6, 9, 9, 9, 24, 29, 77, 630, 699, 3730, 8038, 30301, 370942, 610757, 623752, 964259, 14163930]
     * 149 has cycles by length: [2, 4, 5, 9, 17, 41, 616, 1113, 14282, 25737, 43576, 607148, 805014, 15279652]
     * 151 has cycles by length: [1, 3, 4, 30, 62, 94, 262, 1682, 2206, 4706, 12143, 13497, 17104, 20979, 165751, 2164857, 3364224, 11009611]
     * 153 has cycles by length: [1, 3, 41, 45, 104, 276, 552, 2102, 7237, 10030, 13521, 14915, 24679, 38574, 1211285, 15453851]
     * 155 has cycles by length: [1, 1, 4, 4, 6, 15, 48, 229, 302, 935, 2222, 3189, 7901, 24418, 131611, 165299, 297966, 316729, 487753, 3387873, 5782618, 6168092]
     * 157 has cycles by length: [1, 1, 2, 3, 3, 9, 11, 14, 42, 140, 10163, 13765, 30103, 32661, 45533, 75217, 239676, 352433, 469114, 1483452, 2375563, 11649310]
     * 159 has cycles by length: [1, 8, 12, 561, 2344, 2528, 37985, 390632, 1600904, 3309225, 4605492, 6827524]
     * 161 has cycles by length: [2, 2, 582, 1055, 1668, 1816, 2414, 104401, 174874, 213680, 545814, 1432919, 1895881, 2819551, 3694678, 5887879]
     * 163 has cycles by length: [1, 1, 4, 8, 10, 24, 618, 1954, 2971, 13376, 81290, 156776, 239136, 3239569, 3429905, 9611573]
     * 165 has cycles by length: [1, 19, 23, 25, 401, 709, 1035, 1554, 1744, 4478, 4994, 6320, 6772, 21038, 86053, 97463, 107388, 143923, 156651, 160450, 2180785, 2247190, 3019292, 8528908]
     * 167 has cycles by length: [1, 4, 5, 33, 8248, 12226, 42698, 196354, 383984, 1464593, 4396906, 10272164]
     * 169 has cycles by length: [1, 3, 6, 10, 27, 59, 62, 192, 652, 981, 1099, 1511, 7179, 7713, 8930, 9467, 13896, 47312, 63637, 322688, 481363, 791849, 4682806, 10335773]
     * 171 has cycles by length: [1, 2, 3, 15, 78, 1062, 512740, 5102105, 5132240, 6028970]
     * 173 has cycles by length: [1, 22, 102, 126, 162, 2523, 2709, 85205, 110362, 160891, 216245, 555429, 6485652, 9157787]
     * 175 has cycles by length: [1, 3, 4, 50, 56, 171, 677, 954, 1278, 3534, 8319, 51373, 57339, 158981, 224616, 630504, 661428, 703454, 1572176, 12702298]
     * 177 has cycles by length: [12, 61, 119, 121, 449, 929, 4251, 12181, 46152, 77499, 3092779, 13542663]
     * 179 has cycles by length: [1, 23, 69, 664, 1431, 340529, 1218383, 3038843, 4845904, 7331369]
     * 181 has cycles by length: [1, 2, 3, 12, 14, 51, 67, 106, 122, 7198, 10327, 151275, 412002, 447808, 858672, 901585, 4262709, 9725262]
     * 183 has cycles by length: [1, 10, 153, 182, 498, 562, 2383, 2792, 5000, 89001, 96689, 3751592, 5754608, 7073745]
     * 185 has cycles by length: [16, 133, 243, 253, 6005, 6584, 91425, 147011, 272206, 323418, 355796, 473615, 1584523, 13515988]
     * 187 has cycles by length: [3, 56, 6157, 15402, 107435, 192170, 297188, 2845487, 5264881, 8048437]
     * 189 has cycles by length: [1, 9, 22, 244, 328, 678, 1785, 7960, 26257, 274651, 1926126, 14539155]
     * 191 has cycles by length: [17, 19, 28, 49, 53, 122, 164, 678, 706, 5487, 20392, 20820, 108106, 481225, 1189996, 1655973, 2351635, 10941746]
     * 193 has cycles by length: [1, 1, 13, 21, 49, 61, 72, 775, 2092, 62802, 103369, 983755, 1895798, 2339593, 3310340, 8078474]
     * 195 has cycles by length: [1, 1, 2, 3, 6, 46, 95, 136, 146, 1587, 28352, 89065, 143296, 3302910, 6156263, 7055307]
     * 197 has cycles by length: [2, 2, 4, 7, 8, 37, 169, 202, 208, 505, 524, 1437, 16085, 610205, 641976, 1053358, 1746431, 3618928, 4021989, 5065139]
     * 199 has cycles by length: [2, 3, 4, 7, 8, 10, 11, 20, 79, 187, 303, 315, 1424, 4833, 6855, 14679, 29427, 32715, 47545, 68350, 286403, 16284036]
     * 201 has cycles by length: [2, 3, 6, 7, 173, 677, 757, 1071, 1232, 1330, 13130, 14792, 15239, 25118, 26403, 428901, 769413, 4091817, 5158307, 6228838]
     * 203 has cycles by length: [1, 1, 3, 4, 12, 12, 16, 217, 4314, 12459, 20209, 24995, 198430, 698954, 1037596, 1343886, 4957930, 8478177]
     * 205 has cycles by length: [1, 9, 13, 35, 69, 135, 327, 1771, 6384, 11676, 22387, 35492, 572855, 660506, 2457612, 13007944]
     * 207 has cycles by length: [1, 1, 1, 2, 4, 10, 24, 27, 94, 102, 281, 331, 384, 2115, 40538, 54440, 74453, 126986, 215710, 491165, 837759, 874038, 1770614, 2146597, 4091696, 6049843]
     * 209 has cycles by length: [4, 4, 15, 17, 26, 185, 329, 1481, 2031, 2480, 7413, 48904, 83973, 86171, 93094, 204450, 231597, 429765, 460445, 546322, 968490, 13610020]
     * 211 has cycles by length: [1, 1, 18, 2371, 6099, 61671, 176736, 249937, 433430, 1747087, 1871123, 12228742]
     * 213 has cycles by length: [1, 2, 7, 11, 13, 13, 31, 43, 48, 120, 160, 476, 1918, 6872, 187281, 223178, 282183, 490322, 2315579, 13268958]
     * 215 has cycles by length: [1, 11, 23, 26, 62, 457, 845, 1804, 3360, 5767, 49937, 91350, 195612, 408320, 427456, 433376, 552299, 678820, 1396796, 1502299, 4936917, 6091678]
     * 217 has cycles by length: [3, 104, 477, 540, 8066, 68891, 380177, 3121568, 6210167, 6987223]
     * 219 has cycles by length: [1, 18, 45, 93, 2184, 2621, 3659, 4451, 67697, 16696447]
     * 221 has cycles by length: [1, 1, 1, 3, 7, 8, 19, 34, 606, 5499, 5885, 6293, 951270, 1304478, 1550729, 12952382]
     * 223 has cycles by length: [1, 6, 12, 72, 88, 118, 235, 3890, 3962, 5280, 174152, 195770, 288294, 1286729, 5125247, 9693360]
     * 225 has cycles by length: [1, 1, 2, 52, 4662, 100839, 197139, 382597, 554767, 561813, 654443, 4144944, 4245432, 5930524]
     * 227 has cycles by length: [2, 5, 6, 12, 268, 3245, 5770, 31904, 55946, 120405, 153607, 180901, 206455, 225159, 283487, 795469, 1101087, 13613488]
     * 229 has cycles by length: [1, 1, 8, 13, 24, 40, 58, 1474, 2062, 3315, 15910, 24899, 30639, 83032, 475005, 670689, 745449, 14724597]
     * 231 has cycles by length: [2, 2, 60, 108, 244, 325, 358, 1128, 13023, 139219, 2964427, 13658320]
     * 233 has cycles by length: [46, 59, 63, 161, 912, 3234, 8428, 16445, 24812, 27434, 1056674, 2343556, 4475428, 8819964]
     * 235 has cycles by length: [1, 1, 544, 2298, 11138, 29329, 144014, 331189, 791070, 15467632]
     * 237 has cycles by length: [1, 3, 7, 7, 19, 39, 151, 10311, 29509, 16737169]
     * 239 has cycles by length: [2, 2, 4, 6, 7, 7, 37, 42, 46, 556, 1924, 1967, 4084, 26433, 38528, 473459, 1492396, 14737716]
     * 241 has cycles by length: [1, 1, 1, 3, 7, 18, 114, 967, 1042, 1189, 50175, 98142, 250341, 538059, 2132001, 13705155]
     * 243 has cycles by length: [1, 1, 4, 4, 4, 4, 4, 7, 7, 8, 14, 14, 31, 85, 98, 132, 141, 413, 486, 2959, 7107, 16251, 27891, 572604, 1372592, 2701469, 5954614, 6120271]
     * 245 has cycles by length: [4, 5, 17, 131, 641, 1677, 3360, 21003, 25229, 130656, 409964, 691706, 5946256, 9546567]
     * 247 has cycles by length: [35, 38, 72, 485, 805, 2487, 7106, 92800, 156743, 2103446, 6824550, 7588649]
     * 249 has cycles by length: [10, 109, 301, 469, 930, 1436, 4881, 5861, 5985, 12006, 19735, 29634, 85171, 150314, 298201, 16162173]
     * 251 has cycles by length: [1, 3, 7, 48, 60, 347, 454, 528, 869, 11393, 12432, 73435, 1668390, 1694891, 1885665, 2486877, 3408286, 5533530]
     * 253 has cycles by length: [1, 2, 33, 1834, 2347, 7593, 41693, 98664, 255343, 963297, 1056046, 1574666, 2058085, 2840036, 3820931, 4056645]
     * 255 has cycles by length: [3, 32, 37, 264, 565, 1007, 5902, 8380, 23295, 26980, 45144, 50923, 115461, 662828, 1080274, 2147177, 2912372, 9696572]
     * <br>
     * Adding the constant ("mul") to stateC, instead of multiplying stateC by it:
     * <br>
     * 3 has cycles by length: [3, 8, 14, 32, 80, 110, 140, 178, 97103, 120951, 160949, 405084, 3505570, 12486994]
     * 5 has cycles by length: [2, 3, 8, 25, 40, 41, 82, 117, 826, 20491, 148030, 476630, 598609, 1673146, 6639640, 7219526]
     * 7 has cycles by length: [1, 1, 16, 35, 69, 414, 517, 1897, 21915, 35021, 94535, 107027, 4893553, 11622215]
     * 9 has cycles by length: [8, 67, 74, 127, 140, 929, 1208, 1608, 1641, 8461, 140901, 230427, 351804, 563860, 5051996, 10423965]
     * 11 has cycles by length: [1, 1, 1, 5, 99, 476, 591, 931, 5364, 16339, 65280, 97034, 182784, 266611, 323934, 504197, 3226680, 12086888]
     * 13 has cycles by length: [3, 5, 6, 7, 48, 841, 3369, 5687, 12663, 20367, 24030, 25543, 35128, 56645, 112283, 270807, 609501, 617721, 1100327, 1163545, 1665962, 11052728]
     * 15 has cycles by length: [2, 3, 81, 151, 244, 2945, 43202, 105031, 120476, 190820, 2431285, 3534885, 4388995, 5959096]
     * 17 has cycles by length: [1, 1, 2, 8, 12, 63, 359, 1029, 1521, 1658, 1783, 9709, 13862, 25762, 101357, 16620089]
     * 19 has cycles by length: [5, 14, 15, 32, 32, 46, 662, 12168, 21723, 30039, 59109, 61349, 1685442, 3893032, 4286915, 6726633]
     * 21 has cycles by length: [1, 1, 1, 3, 27, 130, 530, 1526, 1979, 2263, 2396, 4856, 10103, 157944, 168018, 359145, 406139, 601349, 1166635, 13894170]
     * 23 has cycles by length: [1, 2, 3, 10, 15, 33, 35, 36, 41, 51, 68, 451, 2659, 2811, 10158, 53547, 173397, 264599, 851013, 1122006, 1298850, 2496905, 3136910, 7363615]
     * 25 has cycles by length: [4, 7, 163, 1333, 5173, 9463, 20642, 146208, 629594, 886644, 956056, 14121929]
     * 27 has cycles by length: [1, 1, 2, 8, 9, 15, 22, 37, 50, 423, 797, 850, 1544, 2866, 3537, 3639, 4659, 39790, 49035, 87131, 91798, 201008, 220353, 2129655, 4982057, 8957929]
     * 29 has cycles by length: [3, 18, 40, 55, 101, 248, 249, 297, 521, 1389, 128128, 153897, 496776, 1022239, 1451686, 13521569]
     * 31 has cycles by length: [1, 1, 1, 2, 181, 1012, 1029, 1793, 6767, 16523, 22564, 37572, 37818, 124768, 724341, 1118344, 1686162, 3390286, 3604534, 6003517]
     * 33 has cycles by length: [4, 4, 11, 1493, 2226, 5021, 24864, 29238, 716639, 2114046, 4854494, 9029176]
     * 35 has cycles by length: [3, 111, 279, 3789, 5709, 9002, 18862, 50460, 84636, 198602, 325928, 1812270, 5268192, 8999373]
     * 37 has cycles by length: [1, 25, 28, 99, 153, 480, 1436, 2941, 55783, 363637, 7790497, 8562136]
     * 39 has cycles by length: [3, 9, 89, 188, 195, 3826, 6038, 7319, 71674, 80939, 90695, 164725, 307107, 326692, 902618, 14815099]
     * 41 has cycles by length: [1, 1, 1, 8, 15, 65, 1006, 6985, 8726, 14253, 23468, 26984, 92616, 95252, 205718, 401577, 809172, 3350235, 4452103, 7289030]
     * 43 has cycles by length: [1, 3, 10, 14, 113, 154, 9000, 41963, 6550981, 10174977]
     * 45 has cycles by length: [5, 11, 13, 20, 130, 191, 347, 408, 508, 779, 1246, 2374, 9551, 105476, 107814, 1142461, 4083912, 11321970]
     * 47 has cycles by length: [1, 1, 2, 30, 43, 77, 182, 829, 922, 2982, 4100, 8157, 21016, 22050, 292093, 3467212, 6020864, 6936655]
     * 49 has cycles by length: [3, 8, 21, 27, 73, 624, 904, 12525, 41331, 153881, 180826, 344406, 616012, 1224110, 2759596, 3520001, 3807583, 4115285]
     * 51 has cycles by length: [1, 1, 1, 3, 20, 92, 360, 1467, 1808, 5580, 9790, 57181, 166988, 262285, 305077, 1015418, 1330242, 13620902]
     * 53 has cycles by length: [1, 4, 17, 19, 56, 65, 71, 226, 252, 971, 1267, 2060, 7429, 831681, 1158051, 1452065, 5693737, 7629244]
     * 55 has cycles by length: [2, 3, 9, 11, 40, 41, 82, 173, 2261, 3587, 62521, 64242, 269812, 1773441, 6046917, 8554074]
     * 57 has cycles by length: [1, 2, 3, 3, 3, 13, 35, 52, 244, 312, 815, 983, 1521, 17717, 29736, 16725776]
     * 59 has cycles by length: [3, 5, 8, 23, 38, 50, 56, 298, 375, 404, 3047, 12526, 15921, 20421, 195173, 1165759, 1489428, 1588032, 5781407, 6504242]
     * 61 has cycles by length: [1, 1, 1, 3, 6, 20, 25, 58, 72, 89, 90, 1653, 4162, 14325, 25760, 53158, 154431, 163272, 466577, 674142, 1030743, 2070467, 4878139, 7240021]
     * 63 has cycles by length: [1, 1, 2, 3, 6, 55, 70, 138, 198, 351, 940, 22325, 92694, 128060, 955362, 2312618, 3658475, 9605917]
     * 65 has cycles by length: [3, 3, 3, 14, 27, 58, 72, 178, 779, 1405, 1861, 2279, 3825, 5496, 6491, 6881, 29966, 77983, 103831, 1236375, 1569972, 13729714]
     * 67 has cycles by length: [1, 2, 3, 3, 4, 13, 22, 25, 275, 524, 664, 669, 2009, 11206, 93717, 159787, 188797, 2335451, 5676506, 8307538]
     * 69 has cycles by length: [2, 3, 5, 13, 44, 382, 424, 1008, 3216, 3955, 4416, 7308, 49375, 140666, 620258, 4027105, 4522650, 7396386]
     * 71 has cycles by length: [1, 1, 7, 7, 71, 875, 1702, 11170, 56181, 143260, 445672, 548761, 954370, 1209569, 6609927, 6795642]
     * 73 has cycles by length: [1, 3, 7, 15, 1069, 20749, 40277, 50834, 87870, 543318, 4786308, 11246765]
     * 75 has cycles by length: [2, 3, 3, 3, 9, 16, 51, 71, 94, 308, 1813, 4004, 4009, 4360, 13715, 17964, 85400, 254577, 352488, 16038326]
     * 77 has cycles by length: [1, 19, 22, 422, 467, 1194, 3232, 9232, 9596, 101408, 174964, 180580, 497182, 675002, 1643168, 1778716, 5007988, 6694023]
     * 79 has cycles by length: [3, 35, 53, 138, 149, 461, 2518, 4198, 23906, 34025, 78116, 114959, 164529, 178172, 328359, 508379, 1101303, 14237913]
     * 81 has cycles by length: [1, 1, 1, 3, 44, 1464, 1794, 3179, 4457, 6536, 236278, 1026850, 2081596, 13415012]
     * 83 has cycles by length: [1, 1, 3, 3, 17, 27, 45, 48, 96, 136, 278, 408, 5089, 1219200, 1880247, 13671617]
     * 85 has cycles by length: [2, 3, 248, 980, 2656, 8114, 11052, 64028, 121273, 561788, 809400, 15197672]
     * 87 has cycles by length: [1, 2, 8, 20, 969, 975, 1910, 8441, 204739, 873900, 1701716, 2063883, 3756081, 8164571]
     * 89 has cycles by length: [3, 15, 65, 1197, 8609, 11440, 30385, 42028, 68890, 801756, 3013832, 12798996]
     * 91 has cycles by length: [1, 1, 3, 4, 35, 43, 541, 776, 855, 2374, 6902, 14162, 47857, 334270, 758615, 1528803, 3617213, 10464761]
     * 93 has cycles by length: [1, 1, 3, 24, 44, 118, 1217, 1302, 1316, 2475, 5344, 12879, 547081, 683876, 5019642, 10501893]
     * 95 has cycles by length: [25, 27, 323, 487, 2464, 2570, 4545, 4779, 11883, 57219, 78502, 102863, 112801, 291379, 666684, 15440665]
     * 97 has cycles by length: [1, 1, 2, 2, 3, 28, 50, 80, 84, 126, 362, 956, 2997, 4670, 5196, 5903, 7314, 9990, 12710, 33286, 51402, 431752, 516629, 765773, 1218816, 1630499, 2704820, 9373764]
     * 99 has cycles by length: [3, 3, 3, 3, 5, 9, 46, 977, 2577, 4630, 9160, 25056, 41988, 72181, 85627, 203787, 272445, 544390, 7210961, 8303365]
     * 101 has cycles by length: [1, 1, 1, 3, 5, 15, 32, 652, 725, 1816, 1868, 10249, 11494, 15607, 17003, 20507, 63403, 277851, 579329, 15776654]
     * 103 has cycles by length: [1, 4, 20, 52, 1305, 1929, 3189, 4952, 5278, 25854, 33788, 100997, 102415, 513896, 757556, 1656869, 6299781, 7269330]
     * 105 has cycles by length: [3, 9, 311, 761, 916, 6705, 10356, 10773, 12884, 14431, 22243, 1468777, 5352345, 9876702]
     * 107 has cycles by length: [2, 3, 3, 3, 3, 3, 3, 4, 209, 228, 241, 497, 2084, 4372, 6135, 12198, 17830, 36587, 137206, 1554344, 2312090, 12693171]
     * 109 has cycles by length: [2, 3, 5, 12, 16, 30, 42, 96, 137, 1697, 4549, 8089, 33863, 82113, 295277, 417684, 2885791, 13047810]
     * 111 has cycles by length: [1, 1, 10, 12, 110, 836, 1000, 7865, 63232, 64529, 66397, 76463, 95568, 189243, 200582, 774230, 2008183, 13228954]
     * 113 has cycles by length: [1, 1, 10, 27, 695, 6348, 28393, 229555, 802373, 15709813]
     * 115 has cycles by length: [2, 3, 3, 3, 3, 18, 40, 106, 363, 1926, 2221, 4701, 10405, 80558, 1044710, 3101112, 3907119, 8623923]
     * 117 has cycles by length: [1, 2, 3, 3, 3, 59, 134, 209, 327, 460, 601, 613, 667, 6520, 15756, 42103, 107601, 116915, 5731567, 10753672]
     * 119 has cycles by length: [2, 3, 8, 9, 19, 442, 545, 8676, 23457, 95623, 150166, 1262376, 1405793, 2989996, 3094592, 7745509]
     * 121 has cycles by length: [1, 1, 3, 28, 1028, 6949, 17705, 33237, 163453, 572842, 6676505, 9305464]
     * 123 has cycles by length: [1, 1, 3, 4, 19, 47, 49, 207, 889, 1001, 9575, 14900, 61807, 104323, 938341, 1689387, 3646903, 10309759]
     * 125 has cycles by length: [2, 3, 3, 3, 3, 15, 16, 32, 938, 1478, 2619, 9796, 25050, 25190, 74771, 392632, 1093996, 1133243, 3258404, 10759022]
     * 127 has cycles by length: [1, 2, 9, 31, 75, 3617, 4494, 7445, 10000, 20986, 50567, 138884, 161067, 415331, 760599, 2210515, 3075681, 9917912]
     * 129 has cycles by length: [2, 3, 4, 131, 267, 586, 1374, 27328, 33861, 1445758, 1742400, 13525502]
     * 131 has cycles by length: [1, 1, 1, 3, 3, 5, 6, 73, 233, 655, 1733, 2787, 5117, 5670, 94528, 640006, 1570463, 14455931]
     * 133 has cycles by length: [1, 1, 1, 3, 3, 5, 6, 46, 90, 278, 1297, 1708, 3090, 4207, 4606, 9547, 112587, 1129593, 2654356, 4075411, 4157131, 4623249]
     * 135 has cycles by length: [3, 4, 14, 20, 56, 73, 249, 924, 1153, 1686, 10685, 22719, 23604, 38913, 92461, 157021, 305903, 2364503, 5427921, 8329304]
     * 137 has cycles by length: [1, 2, 8, 34, 66, 85, 98, 176, 394, 545, 2357, 6385, 16995, 82593, 119181, 618793, 790104, 1862230, 4398019, 8879150]
     * 139 has cycles by length: [2, 3, 3, 3, 3, 11, 68, 201, 2711, 4036, 4268, 5321, 33421, 34197, 53958, 126072, 279999, 16232939]
     * 141 has cycles by length: [1, 1, 3, 4, 9, 1076, 1093, 1962, 2542, 7617, 10342, 20743, 25972, 26667, 138562, 197361, 2719279, 13623982]
     * 143 has cycles by length: [1, 1, 3, 17, 49, 79, 200, 234, 353, 2584, 3782, 12770, 14698, 72321, 94545, 350617, 455830, 2337704, 2674159, 10757269]
     * 145 has cycles by length: [2, 3, 8, 9, 17, 10725, 11091, 25533, 557574, 16172254]
     * 147 has cycles by length: [1, 2, 3, 3, 3, 4, 39, 63, 675, 2178, 3526, 6257, 11617, 17209, 17994, 18183, 788128, 15911331]
     * 149 has cycles by length: [3, 3, 3, 3, 11, 14, 18, 89, 328, 593, 918, 8215, 17016, 67614, 904887, 1872163, 2623061, 11282277]
     * 151 has cycles by length: [1, 1, 13, 32, 136, 940, 1146, 1243, 4931, 19007, 21291, 28045, 54623, 222989, 635357, 15787461]
     * 153 has cycles by length: [1, 1, 12, 26, 423, 588, 607, 8957, 13945, 14709, 51350, 149923, 381747, 502404, 646032, 1840393, 3005236, 10160862]
     * 155 has cycles by length: [3, 5, 16, 34, 232, 314, 346, 9750, 14787, 27179, 47673, 93503, 94122, 133658, 454399, 621482, 1439633, 3997185, 4328482, 5514413]
     * 157 has cycles by length: [2, 3, 3, 3, 3, 3, 3, 4, 33, 47, 399, 1120, 2939, 4696, 6147, 13513, 14302, 75735, 97472, 108937, 637571, 873180, 3161887, 11779214]
     * 159 has cycles by length: [9, 38, 1792, 1862, 5356, 18237, 27281, 120433, 161855, 365830, 6773711, 9300812]
     * 161 has cycles by length: [1, 4, 80, 175, 194, 674, 1097, 34490, 78859, 2106178, 2585782, 2866647, 3469792, 5633243]
     * 163 has cycles by length: [1, 1, 1, 3, 5, 15, 91, 466, 543, 1579, 5547, 17410, 32535, 41049, 109541, 232872, 417729, 463086, 1198821, 2771477, 5331075, 6153369]
     * 165 has cycles by length: [3, 3, 3, 3, 5, 35, 58, 511, 7898, 13862, 91991, 137818, 152109, 297959, 343351, 2572516, 5098963, 8060128]
     * 167 has cycles by length: [1, 1, 2, 2, 3, 9, 11, 72, 197, 2335, 2555, 2977, 3102, 5095, 5219, 7643, 32920, 41448, 44106, 53137, 63382, 226512, 4033941, 12252546]
     * 169 has cycles by length: [2, 37, 44, 103, 164, 5792, 516730, 688800, 1098856, 2259142, 4328781, 7878765]
     * 171 has cycles by length: [1, 1, 3, 34, 57, 73, 372, 390, 463, 20238, 63693, 142164, 352936, 534637, 834444, 1319791, 1531851, 11976068]
     * 173 has cycles by length: [1, 1, 1, 3, 4, 7, 22, 185, 1859, 2898, 2957, 17232, 28645, 40017, 48298, 1663512, 6615112, 8356462]
     * 175 has cycles by length: [3, 151, 201, 435, 760, 4115, 6312, 7659, 15027, 33005, 91362, 101476, 191395, 217276, 471493, 543906, 712595, 1032173, 1772432, 2803733, 4227021, 4544686]
     * 177 has cycles by length: [1, 2, 6, 8, 14, 55, 63, 85, 148, 1676, 1968, 11313, 55508, 82595, 731056, 952332, 1777759, 13162627]
     * 179 has cycles by length: [2, 3, 9, 18, 143, 518, 850, 1556, 5553, 10519, 41681, 242793, 2405186, 14068385]
     * 181 has cycles by length: [1, 3, 3, 4, 17, 27, 35, 254, 10840, 44423, 240292, 874000, 1685975, 1792401, 1972724, 2679618, 2763129, 4713470]
     * 183 has cycles by length: [1, 1, 1, 3, 7, 11, 35, 48, 209, 1907, 25552, 53365, 122498, 994737, 2260953, 2299450, 5074470, 5943968]
     * 185 has cycles by length: [3, 20, 27, 72, 133, 365, 493, 3163, 4194, 4631, 4920, 118035, 148787, 183266, 432672, 15876435]
     * 187 has cycles by length: [1, 8, 19, 12781, 28702, 356021, 480075, 1329739, 1957177, 12612693]
     * 189 has cycles by length: [2, 3, 3, 3, 38, 48, 51, 63, 362, 965, 1131, 9028, 72888, 75365, 107173, 861278, 1849759, 2686232, 5016608, 6096216]
     * 191 has cycles by length: [1, 3, 7, 33, 329, 496, 605, 5465, 7049, 33661, 258979, 507909, 746284, 1290591, 1430530, 1457144, 4418717, 6619413]
     * 193 has cycles by length: [1, 1, 7, 30, 184, 215, 305, 477, 1948, 11530, 16149, 84614, 409532, 802185, 1058389, 2242940, 4918471, 7230238]
     * 195 has cycles by length: [3, 5, 98, 339, 536, 2856, 24840, 38120, 109008, 111679, 328218, 534201, 567013, 15060300]
     * 197 has cycles by length: [1, 2, 3, 3, 4, 11, 12, 13, 13, 39, 133, 135, 223, 392, 1107, 1524, 9411, 21770, 48903, 105089, 137792, 2774754, 2970693, 10705189]
     * 199 has cycles by length: [3, 3, 5, 27, 483, 589, 1334, 1430, 1940, 6623, 10609, 28012, 32377, 43430, 77437, 137855, 149815, 183069, 514882, 954146, 6384072, 8249075]
     * 201 has cycles by length: [1, 2, 3, 21, 296, 3349, 5555, 9253, 15592, 31245, 35291, 126369, 159133, 2685921, 4280214, 9424971]
     * 203 has cycles by length: [1, 1, 3, 5, 34, 42, 31853, 45985, 945009, 2007885, 5383473, 8362925]
     * 205 has cycles by length: [3, 5, 8, 33, 35, 56, 264, 291, 405, 10077, 16058, 23573, 2992470, 13733938]
     * 207 has cycles by length: [1, 2, 3, 13, 99, 135, 243, 1265, 3796, 5342, 6575, 12347, 44572, 80750, 153765, 183644, 189407, 217126, 281373, 338811, 789620, 1006010, 1180048, 1471838, 1480168, 9330263]
     * 209 has cycles by length: [2, 3, 5, 40, 75, 298, 1745, 2188, 21578, 23545, 380497, 652590, 5255820, 10438830]
     * 211 has cycles by length: [1, 4, 5, 118, 147, 343, 908, 4818, 7028, 29436, 311704, 16422704]
     * 213 has cycles by length: [1, 1, 3, 41, 572, 1462, 9423, 14760, 21684, 25112, 320665, 360077, 470551, 15552864]
     * 215 has cycles by length: [14, 21, 21, 70, 3156, 11233, 12034, 16030, 33488, 45172, 47442, 175715, 197057, 215131, 291348, 694993, 1147228, 1869892, 4267173, 7749998]
     * 217 has cycles by length: [1, 1, 2, 30, 37, 40, 82, 175, 245, 734, 1700, 5783, 12513, 57893, 75304, 316124, 1044687, 2439495, 2570317, 10252053]
     * 219 has cycles by length: [11, 152, 509, 910, 3897, 5839, 52381, 269690, 360825, 552382, 6763617, 8767003]
     * 221 has cycles by length: [1, 10, 14, 31, 39, 734, 860, 1910, 4228, 4546, 5244, 5593, 8552, 9364, 31365, 33406, 360654, 1237354, 1517246, 1604033, 2119495, 2181237, 3742025, 3909275]
     * 223 has cycles by length: [1, 1, 15, 34, 67, 96, 128, 192, 447, 1630, 10788, 11751, 13155, 27677, 126140, 152162, 198170, 297427, 418663, 1762160, 2034139, 11722373]
     * 225 has cycles by length: [3, 9, 10, 11, 13, 182, 901, 1043, 4667, 6033, 58606, 93210, 316693, 563389, 866592, 1632759, 1813213, 11419882]
     * 227 has cycles by length: [1, 5, 17, 25, 39, 162, 202, 337, 1035, 1184, 1896, 4369, 7706, 38387, 42578, 189849, 319520, 1242454, 2042058, 2643691, 4716632, 5525069]
     * 229 has cycles by length: [5, 6, 22, 69, 84, 147, 482, 1992, 5106, 6089, 12290, 28216, 39444, 52569, 79626, 105631, 335503, 351028, 396282, 945191, 4564437, 9852997]
     * 231 has cycles by length: [4, 4, 8, 11, 17, 1044, 1476, 7425, 11381, 84227, 105194, 112428, 202095, 1016507, 1040398, 14194997]
     * 233 has cycles by length: [1, 1, 2, 57, 59, 176, 1483, 1654, 9714, 140156, 160502, 399279, 1098811, 1777119, 3732289, 9455913]
     * 235 has cycles by length: [3, 18, 31, 45, 651, 966, 3811, 29101, 73693, 133202, 1443402, 1580350, 4572639, 8939304]
     * 237 has cycles by length: [1, 1, 2, 8, 9, 21, 48, 106, 164, 850, 2828, 3585, 4314, 7947, 57243, 1192346, 5419056, 10088687]
     * 239 has cycles by length: [4, 5, 67, 84, 2605, 4064, 7022, 32397, 70860, 76967, 266514, 2013836, 2626038, 11676753]
     * 241 has cycles by length: [1, 2, 3, 5, 7, 10, 10, 38, 293, 408, 1348, 2570, 3087, 18704, 37018, 57999, 195508, 358855, 445864, 462950, 708238, 1290990, 1415330, 2082818, 4456899, 5238261]
     * 243 has cycles by length: [1, 1, 7, 122, 139, 730, 1930, 3197, 4512, 489576, 7747705, 8529296]
     * 245 has cycles by length: [5, 141, 450, 713, 807, 1727, 5561, 12555, 13205, 106032, 167341, 376357, 761368, 15330954]
     * 247 has cycles by length: [1, 1, 2, 12, 87, 146, 692, 896, 19624, 66726, 342202, 783469, 1266771, 3265954, 5014758, 6015875]
     * 249 has cycles by length: [2, 3, 20, 22, 77, 331, 441, 463, 2258, 10857, 15301, 17233, 18686, 19859, 261048, 1031322, 1929645, 4100755, 4431066, 4937827]
     * 251 has cycles by length: [3, 5, 7, 17, 47, 96, 311, 457, 3924, 9843, 29576, 32239, 39925, 68476, 86334, 87901, 203117, 461496, 629058, 15124384]
     * 253 has cycles by length: [1, 1, 40, 112, 4583, 210005, 1622503, 14939971]
     * 255 has cycles by length: [5, 12, 136, 2657, 3386, 10952, 58827, 165025, 223235, 1370357, 1572532, 3319513, 5013093, 5037486]
     * <br>
     * Using just additions and subtractions on rotations, no multiplications, but using a counter to guarantee a minimum period:
     * (This is TECHNIQUE 1.)
     * <br>
     * 3 has cycles by length: [1024, 1024, 1024, 1024, 2560, 3584, 4096, 5632, 6912, 6912, 6912, 6912, 7168, 7168, 10240, 10752, 11264, 13824, 14336, 17152, 17920, 17920, 20736, 22528, 25088, 25600, 46592, 79360, 83712, 85760, 100352, 130560, 137216, 191488, 193536, 223232, 247296, 414720, 449280, 518400, 546048, 605696, 613888, 669696, 704000, 763392, 1286400, 1841664, 2864384, 3711232]
     * 5 has cycles by length: [1536, 1536, 1536, 1536, 1536, 3072, 4608, 10752, 13312, 16384, 36864, 42496, 42496, 42496, 66560, 101376, 143360, 165888, 228864, 382464, 467456, 849920, 1487360, 1617920, 1720320, 1761280, 3187200, 4377088]
     * 7 has cycles by length: [15616, 31232, 49920, 49920, 49920, 124928, 399360, 593408, 599040, 3232512, 5041920, 6589440]
     * 9 has cycles by length: [256, 256, 256, 512, 512, 1024, 1536, 3584, 5632, 6400, 6400, 11264, 12544, 14080, 17920, 19200, 19200, 22016, 25088, 30976, 30976, 33792, 35840, 39168, 62720, 64512, 100352, 107520, 112896, 125440, 319488, 594176, 688128, 718848, 1158144, 1587200, 2772224, 8027136]
     * 11 has cycles by length: [256, 256, 768, 768, 1024, 1280, 1536, 1792, 2048, 2048, 2304, 3072, 3328, 4096, 4096, 4096, 4352, 5376, 6144, 6144, 6144, 7168, 8192, 9216, 9216, 13568, 15360, 16640, 17664, 19456, 22272, 24576, 24576, 29952, 33792, 35328, 43008, 43008, 43008, 52992, 61440, 66816, 67584, 86016, 86016, 98304, 119296, 129024, 172032, 202752, 208896, 244224, 257792, 344064, 405504, 488448, 632832, 903168, 1591296, 2469376, 2580480, 5031936]
     * 13 has cycles by length: [256, 256, 1024, 1536, 2304, 5632, 5632, 10240, 11264, 20992, 22528, 22528, 38656, 55808, 55808, 61952, 83968, 146944, 204800, 209920, 219648, 259072, 272896, 281600, 332288, 356864, 356864, 362752, 399360, 424960, 461824, 545792, 546304, 641792, 697600, 1175552, 1408000, 1702144, 1742336, 3627520]
     * 15 has cycles by length: [512, 768, 1280, 2560, 5120, 7168, 7680, 8192, 9216, 26112, 28160, 43008, 46848, 48640, 53760, 64512, 74240, 78336, 93184, 99840, 107520, 112640, 121856, 184320, 184320, 235008, 321280, 1323520, 1397760, 3107328, 3237888, 5744640]
     * 17 has cycles by length: [256, 256, 512, 768, 768, 1536, 2048, 3072, 3584, 3584, 4608, 6144, 10496, 13824, 14336, 21504, 21504, 25088, 29184, 29184, 37632, 58368, 69120, 99840, 129024, 138240, 209920, 248832, 262400, 314880, 334848, 490752, 682240, 734720, 808704, 3463680, 3673600, 4828160]
     * 19 has cycles by length: [256, 768, 1536, 2048, 2816, 2816, 26112, 26880, 30976, 34048, 52224, 78336, 98560, 107520, 281600, 304128, 698880, 2071552, 3279360, 9676800]
     * 21 has cycles by length: [256, 256, 256, 256, 768, 768, 768, 1280, 1536, 1536, 2816, 3072, 4096, 4096, 4352, 4608, 8448, 9728, 10240, 10240, 10752, 12288, 12288, 13824, 14336, 15360, 19968, 22528, 32256, 59904, 60928, 108288, 354816, 438272, 1274112, 3033600, 4429056, 6795264]
     * 23 has cycles by length: [1024, 1280, 2304, 2304, 3840, 3840, 4608, 7680, 13056, 16640, 18688, 19200, 19968, 25600, 39936, 44544, 45056, 59904, 66816, 71424, 72960, 119040, 135168, 161280, 166400, 168960, 195840, 216576, 290304, 326400, 364032, 380160, 796416, 1161984, 1175040, 1198080, 3793920, 5586944]
     * 25 has cycles by length: [256, 768, 2048, 2048, 2048, 3328, 3840, 4096, 4864, 9728, 9984, 14336, 20480, 20992, 24320, 29184, 29184, 29184, 53248, 58368, 116736, 116736, 291840, 428032, 515584, 627456, 2042880, 2918400, 3443712, 5953536]
     * 27 has cycles by length: [3072, 4352, 12288, 12288, 13056, 14336, 14336, 14336, 43008, 43008, 56576, 73728, 91392, 129024, 157696, 162816, 186368, 243712, 479232, 948736, 1103872, 1763328, 4727808, 6478848]
     * 29 has cycles by length: [512, 768, 768, 1024, 1536, 3072, 3584, 5376, 5376, 6144, 10752, 11264, 12288, 14592, 15872, 16384, 17664, 17920, 20736, 25600, 29952, 40960, 49152, 60672, 72960, 86016, 122880, 163840, 382464, 450560, 1024000, 1269760, 2171904, 3247104, 3604480, 3809280]
     * 31 has cycles by length: [2048, 2048, 3072, 11264, 17408, 29440, 69120, 81408, 97280, 132096, 134144, 167680, 201216, 206080, 239616, 441600, 1030400, 1542656, 5829120, 6539520]
     * 33 has cycles by length: [768, 768, 1024, 1024, 1536, 3072, 4608, 4864, 4864, 4864, 4864, 5376, 5376, 6144, 6912, 8448, 9728, 14592, 14592, 14592, 14592, 14592, 47104, 49152, 53248, 53248, 102144, 106496, 136192, 138240, 141056, 168960, 206848, 248064, 321024, 705280, 734464, 851968, 2715648, 9850880]
     * 35 has cycles by length: [256, 1024, 1536, 3072, 3072, 3328, 3584, 3840, 6656, 6656, 7168, 7168, 8192, 8192, 14080, 16896, 21504, 21504, 23296, 42496, 61440, 64512, 93184, 108032, 127488, 127488, 169984, 178176, 197120, 215040, 236544, 253440, 492800, 637440, 732160, 1189888, 1914880, 9774080]
     * 37 has cycles by length: [4096, 16896, 17664, 24320, 24320, 30976, 36864, 49152, 73728, 121600, 458752, 462848, 680960, 1167360, 1179648, 1240320, 2967040, 8220672]
     * 39 has cycles by length: [1792, 2048, 2816, 3072, 3072, 4096, 5120, 5120, 6144, 6144, 8192, 8192, 8704, 9984, 9984, 9984, 10240, 11520, 13312, 16640, 16640, 17920, 17920, 18432, 20736, 21504, 22528, 24576, 26112, 26624, 40192, 48128, 51200, 53248, 53760, 69120, 70656, 71680, 73216, 92160, 95232, 107520, 110592, 128000, 149760, 183040, 208896, 295936, 322560, 384000, 394240, 435200, 443904, 444416, 449280, 518400, 668160, 692224, 748800, 760320, 915200, 1018368, 2396160, 3924480]
     * 41 has cycles by length: [512, 768, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 3072, 3584, 4608, 6144, 6144, 7168, 7936, 9984, 14336, 15872, 17664, 20480, 22528, 22528, 23552, 26112, 27648, 30720, 37120, 37120, 48384, 50688, 52992, 61440, 80640, 108032, 111360, 147456, 185600, 186368, 235520, 306432, 593920, 668160, 2023680, 3693312, 7869440]
     * 43 has cycles by length: [256, 256, 256, 512, 512, 512, 768, 1024, 1536, 1792, 2048, 2048, 3328, 3840, 4096, 5632, 5888, 6400, 12288, 14848, 16896, 17152, 17152, 23552, 25600, 26112, 29440, 38400, 41728, 44032, 57344, 59904, 61440, 62464, 65280, 68608, 82432, 102400, 154368, 240128, 396800, 446464, 678912, 1366016, 1423616, 1775616, 2010624, 2193408, 2469888, 2713600]
     * 45 has cycles by length: [256, 256, 256, 768, 768, 768, 768, 768, 1024, 1024, 1536, 1536, 1536, 2048, 2304, 2816, 3072, 5376, 5376, 6400, 7680, 8704, 8704, 14848, 15360, 18944, 20224, 34816, 46336, 52224, 59136, 64512, 64512, 69632, 84480, 86016, 99840, 130560, 145920, 261120, 287232, 299520, 430080, 600576, 622080, 879104, 940032, 1290240, 1634304, 2429952, 2924544, 3107328]
     * 47 has cycles by length: [256, 1024, 1536, 1536, 1536, 1536, 1792, 4608, 6144, 12544, 13312, 13312, 13824, 16896, 19968, 27648, 35072, 50432, 50432, 53248, 100864, 299520, 1424384, 1916928, 2118144, 10590720]
     * 49 has cycles by length: [256, 256, 512, 512, 512, 512, 512, 768, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 2560, 3584, 4096, 4096, 8192, 11264, 23040, 24576, 32000, 47616, 49152, 51456, 52992, 66816, 81408, 107520, 552960, 977664, 1044480, 1474560, 2675712, 3293184, 6174720]
     * 51 has cycles by length: [256, 256, 1536, 1536, 2048, 2048, 2560, 3328, 3584, 3840, 4096, 4096, 4352, 4352, 4864, 6144, 8192, 10240, 10752, 14336, 14336, 15360, 16384, 16896, 18944, 25088, 31488, 32256, 33280, 38400, 49152, 49152, 51200, 59392, 59392, 82688, 82688, 104960, 129024, 132096, 142848, 147968, 157696, 169728, 176640, 202752, 222208, 225280, 245760, 249856, 282624, 508928, 622336, 1179648, 1277952, 1327104, 1671168, 6832128]
     * 53 has cycles by length: [256, 1280, 1792, 1792, 1792, 1792, 1792, 2048, 2048, 2304, 3584, 3840, 4864, 4864, 5120, 5376, 7168, 10752, 10752, 13824, 15360, 16896, 20480, 22016, 22272, 24064, 30464, 30464, 38912, 46080, 48128, 49152, 60928, 65280, 69120, 75264, 87040, 91392, 94976, 116736, 118272, 144384, 150528, 220160, 307200, 505344, 670208, 852992, 938496, 1079808, 1157632, 1901056, 2622976, 4996096]
     * 55 has cycles by length: [1792, 5376, 17920, 19712, 191232, 413952, 573696, 1274880, 6310656, 7968000]
     * 57 has cycles by length: [256, 2816, 3584, 4096, 5120, 5632, 6144, 13824, 16384, 17920, 17920, 26624, 35840, 35840, 47360, 50176, 61952, 88064, 96768, 107520, 143360, 143616, 157696, 176128, 179200, 239360, 267520, 330240, 358400, 638464, 781312, 836608, 1003520, 1012736, 1056768, 1075200, 1497088, 6236160]
     * 59 has cycles by length: [1792, 1792, 3072, 3584, 5376, 6144, 8192, 8960, 9216, 9216, 11264, 16384, 18432, 21504, 21504, 28672, 43520, 45056, 46080, 77056, 92928, 104448, 110592, 114688, 116736, 130560, 153600, 162816, 180224, 196608, 261120, 261120, 286720, 310016, 534528, 571648, 1062912, 1294336, 1479680, 8965120]
     * 61 has cycles by length: [256, 512, 768, 768, 1280, 1280, 1280, 2048, 4096, 4096, 5120, 5888, 6400, 6400, 7168, 7680, 11520, 12288, 24320, 36096, 41984, 53760, 65280, 74240, 85760, 140800, 175104, 207360, 233472, 323840, 702720, 933888, 1809408, 2451456, 3443712, 5895168]
     * 63 has cycles by length: [512, 1792, 1792, 1792, 2816, 16128, 16128, 17920, 19712, 32256, 32256, 42496, 96768, 145152, 179200, 189440, 256256, 306432, 378880, 1306368, 1610240, 2177280, 3173120, 6772480]
     * 65 has cycles by length: [256, 256, 2048, 2048, 5120, 5888, 10240, 10240, 10496, 18432, 23296, 26112, 30720, 30720, 32768, 32768, 34816, 38912, 51200, 61440, 88320, 92160, 92160, 147456, 155648, 311296, 344064, 348160, 706560, 835584, 1179648, 1413120, 1751040, 2150400, 2949120, 3784704]
     * 67 has cycles by length: [10752, 10752, 13568, 24576, 28928, 28928, 28928, 49152, 81408, 122112, 129024, 135680, 172032, 307200, 368640, 376064, 540672, 666624, 811008, 860160, 872448, 1075200, 1248256, 1872384, 1967104, 4975616]
     * 69 has cycles by length: [512, 512, 512, 512, 1024, 1536, 2560, 4096, 4864, 4864, 7168, 9216, 9728, 10752, 17920, 30720, 37376, 72192, 299520, 321024, 479232, 904704, 1737216, 2396160, 3354624, 7068672]
     * 71 has cycles by length: [256, 256, 256, 256, 512, 512, 512, 1024, 1536, 3072, 5376, 6144, 6144, 13312, 13568, 24576, 28672, 29184, 30720, 32768, 36864, 117760, 245760, 471040, 479232, 743424, 1413120, 13071360]
     * 73 has cycles by length: [256, 256, 256, 256, 512, 768, 768, 1024, 1280, 1536, 1536, 2304, 2816, 3072, 3072, 3584, 4608, 6144, 6656, 6912, 7168, 8192, 10752, 14336, 14336, 15360, 17408, 17920, 20480, 25600, 26112, 26112, 26112, 34560, 36864, 40960, 49152, 51712, 52224, 64512, 65024, 74240, 76800, 78848, 112640, 168448, 235520, 254464, 287232, 306176, 308480, 353280, 365568, 376832, 440320, 522240, 522240, 652800, 824320, 861696, 894976, 962560, 1070592, 1177600, 1860608, 3316224]
     * 75 has cycles by length: [512, 768, 1280, 1536, 9216, 12800, 15104, 16896, 30720, 35072, 35840, 92160, 139520, 242688, 502272, 537600, 609280, 1218560, 1227776, 1227776, 4046080, 6773760]
     * 77 has cycles by length: [2048, 3584, 3584, 5376, 8448, 14336, 17920, 25344, 28672, 28672, 34304, 34304, 34304, 39424, 40960, 65536, 92160, 101376, 152320, 157696, 168960, 180224, 207872, 405504, 466944, 480256, 675840, 1047552, 1269248, 1440768, 1537536, 2517504, 2572800, 2915840]
     * 79 has cycles by length: [256, 256, 512, 512, 768, 2048, 3072, 3584, 5120, 5120, 5120, 7680, 7936, 7936, 9216, 12288, 18944, 23040, 25600, 29696, 30464, 31744, 31744, 40960, 46080, 46080, 46080, 61952, 92160, 108032, 138240, 157440, 168960, 203520, 222208, 230400, 312320, 368640, 545280, 660480, 1059840, 1730048, 4423680, 5852160]
     * 81 has cycles by length: [256, 256, 256, 512, 512, 512, 512, 512, 512, 1024, 2304, 2560, 4608, 5888, 6656, 9728, 21504, 43776, 59392, 64512, 64512, 100864, 645120, 3612672, 4967424, 7160832]
     * 83 has cycles by length: [256, 256, 768, 1280, 1280, 1536, 3328, 3840, 4096, 6400, 6912, 7680, 7680, 7680, 7680, 8704, 10752, 10752, 12800, 13312, 14592, 15616, 23040, 27648, 31488, 38400, 39936, 49920, 53248, 56064, 69888, 79872, 79872, 79872, 92928, 102400, 140800, 161280, 168960, 192000, 199680, 199680, 204800, 279552, 320000, 468480, 489216, 620800, 678912, 1113600, 1741824, 8825856]
     * 85 has cycles by length: [256, 256, 512, 512, 768, 1024, 1280, 1792, 1792, 7168, 12544, 13312, 13312, 13312, 18688, 19968, 19968, 19968, 21504, 23552, 26880, 30720, 40704, 53248, 56064, 56064, 86016, 91392, 93184, 112128, 123392, 168192, 216832, 336384, 532480, 938496, 1121280, 1457664, 1681920, 9362688]
     * 87 has cycles by length: [256, 256, 512, 512, 768, 1024, 1536, 1536, 3072, 3584, 5632, 14080, 15872, 23040, 25088, 27648, 28672, 32000, 32768, 38400, 39424, 40448, 41216, 41216, 50176, 53760, 59904, 61440, 76800, 82432, 88064, 92160, 123648, 123904, 179200, 239360, 247296, 262656, 298496, 329728, 453376, 494592, 552960, 602112, 700672, 1030400, 1244160, 1360128, 1904640, 5646592]
     * 89 has cycles by length: [2560, 7680, 15360, 19712, 24832, 39424, 46080, 49664, 59136, 99328, 120320, 258048, 372480, 463360, 571392, 718848, 943616, 1163008, 1163008, 2601984, 3170304, 4867072]
     * 91 has cycles by length: [256, 256, 512, 512, 512, 1024, 1024, 1280, 1280, 1280, 1536, 1536, 1536, 1536, 1536, 2048, 2304, 2560, 3328, 4608, 4608, 5120, 5120, 6144, 11520, 18688, 23808, 23808, 26880, 34816, 36352, 36352, 39936, 47616, 51968, 52992, 71424, 72704, 72704, 73728, 76288, 93184, 109056, 125952, 145408, 188928, 218112, 253440, 254464, 276480, 285696, 1309440, 4333056, 8360960]
     * 93 has cycles by length: [1024, 1280, 1280, 1536, 1792, 2048, 3072, 3072, 3584, 4096, 4608, 5120, 5120, 6144, 7168, 7424, 8960, 8960, 9728, 9728, 11264, 11776, 14336, 19968, 21504, 21504, 22528, 24576, 31232, 39936, 45056, 48640, 62464, 93696, 105472, 112640, 124928, 238080, 263680, 281088, 321024, 374784, 677376, 981504, 1841152, 1880064, 1984000, 7027200]
     * 95 has cycles by length: [8448, 18432, 18432, 33792, 36864, 43520, 50688, 118272, 135168, 152064, 217600, 350208, 442368, 479232, 557568, 777216, 976896, 1115136, 1419264, 2120448, 2396160, 5309440]
     * 97 has cycles by length: [256, 512, 768, 768, 768, 1024, 1024, 1280, 1536, 3072, 3584, 5120, 5376, 5632, 5888, 6144, 6656, 6912, 9216, 10240, 10752, 14336, 14592, 14848, 15360, 15360, 15360, 20736, 26624, 26624, 37120, 39936, 40704, 61440, 73216, 93184, 99840, 99840, 104448, 112640, 122880, 218112, 261120, 425984, 562176, 888832, 931840, 1079296, 1182720, 1218560, 2257920, 6615040]
     * 99 has cycles by length: [256, 256, 256, 512, 512, 1024, 1024, 1024, 2560, 3072, 3328, 5120, 5888, 5888, 6912, 7680, 10752, 11776, 11776, 12800, 14848, 26624, 30976, 34816, 36352, 46080, 47104, 53248, 58112, 81408, 135424, 141312, 294400, 365056, 500480, 755456, 1336576, 2731264, 2963712, 7031552]
     * 101 has cycles by length: [512, 512, 512, 768, 1024, 1024, 1024, 1024, 1024, 1280, 2048, 3072, 3072, 4864, 5632, 5632, 7680, 10240, 12288, 14336, 15360, 16384, 16640, 19456, 20736, 21504, 22272, 24576, 25088, 25856, 26880, 26880, 27136, 78848, 91136, 98304, 107520, 121344, 164864, 172032, 186624, 186624, 233472, 248832, 295680, 534016, 746496, 808704, 847872, 1612800, 1747200, 1953792, 3064320, 3110400]
     * 103 has cycles by length: [512, 512, 1024, 1024, 1280, 1280, 1280, 1536, 9216, 9216, 17920, 18432, 18432, 27136, 33280, 53760, 54528, 55040, 73728, 81408, 82944, 92160, 109056, 109056, 138240, 181760, 387072, 599808, 817920, 1529856, 1690368, 2235648, 2562816, 5779968]
     * 105 has cycles by length: [1024, 1280, 1280, 3840, 4352, 4352, 5120, 6400, 7168, 7680, 8704, 12288, 22528, 25600, 26112, 33280, 34560, 60416, 116480, 133120, 165120, 195840, 195840, 248064, 254720, 256000, 330240, 394240, 626688, 660480, 1376000, 1816320, 4458240, 5283840]
     * 107 has cycles by length: [768, 768, 1280, 2560, 3584, 3584, 3840, 3840, 4608, 4608, 7936, 7936, 8704, 8704, 10496, 10496, 10752, 12032, 17408, 23808, 24576, 25088, 29440, 31488, 36864, 39680, 41984, 41984, 41984, 55552, 69632, 75264, 78848, 95232, 113664, 120320, 178432, 180480, 204544, 206336, 214272, 217600, 240128, 277760, 286720, 428544, 591360, 674560, 724224, 792064, 878336, 1279488, 1605632, 1605888, 1684480, 3437056]
     * 109 has cycles by length: [1024, 1024, 6144, 6144, 6144, 6144, 6144, 12288, 12288, 12288, 12288, 25600, 30720, 36864, 46080, 47104, 86016, 129024, 132096, 138240, 168960, 184320, 215040, 393216, 399360, 860160, 933888, 3148800, 4128768, 5591040]
     * 111 has cycles by length: [512, 512, 512, 1024, 1536, 2560, 2560, 4096, 4608, 6912, 7168, 8704, 8960, 10240, 12288, 20736, 27648, 32256, 39424, 48128, 48128, 60416, 88064, 116736, 138240, 165888, 172800, 192512, 241920, 532224, 577536, 705024, 913920, 1058816, 1128960, 1491968, 2791424, 6112256]
     * 113 has cycles by length: [768, 4608, 64768, 191232, 194304, 1101056, 6606336, 8614144]
     * 115 has cycles by length: [256, 512, 512, 512, 512, 768, 1024, 1536, 1536, 1536, 2048, 2048, 2304, 4096, 4096, 4096, 5376, 6144, 8192, 10752, 13312, 17664, 17920, 22528, 22528, 28672, 37632, 44800, 53760, 53760, 55552, 55552, 77824, 123648, 124928, 125952, 245760, 277760, 331520, 388864, 499968, 555520, 1111040, 1155840, 1999872, 9277184]
     * 117 has cycles by length: [768, 768, 1280, 1536, 2048, 2048, 2304, 2304, 2304, 3840, 4864, 5376, 6144, 14336, 14336, 15360, 16128, 16128, 19200, 20480, 26624, 30720, 30720, 34560, 38400, 39168, 45056, 45056, 46080, 52480, 59904, 79872, 99840, 104960, 125440, 126720, 137984, 173824, 387072, 419328, 434176, 787200, 810240, 1259520, 1259520, 1259520, 2204160, 6507520]
     * 119 has cycles by length: [512, 512, 5120, 7168, 7168, 8192, 13312, 13312, 39936, 39936, 51200, 79872, 108544, 124928, 279552, 1331200, 1689600, 2941952, 3788800, 6246400]
     * 121 has cycles by length: [256, 256, 256, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1280, 1536, 1536, 1536, 3072, 3584, 4096, 4096, 4608, 6144, 6400, 9216, 11264, 12800, 14848, 20480, 23040, 24064, 45824, 46080, 46592, 63232, 63744, 90624, 96000, 107520, 185856, 208640, 305920, 3915776, 11441408]
     * 123 has cycles by length: [256, 512, 768, 768, 1536, 1792, 2304, 5376, 5376, 7168, 8448, 9216, 13568, 15872, 18176, 19200, 29184, 34304, 34304, 36864, 39936, 39936, 79360, 83712, 137216, 148224, 240128, 240128, 274432, 308736, 420608, 610560, 666624, 754688, 793600, 2428672, 2507776, 6757888]
     * 125 has cycles by length: [7936, 9216, 9216, 15872, 16896, 16896, 23808, 23808, 23808, 63488, 66560, 71424, 85760, 95232, 163328, 175360, 184320, 208384, 230144, 357120, 433152, 442368, 452352, 582912, 1036288, 1166592, 1238016, 1281024, 1357056, 1766400, 2172672, 2999808]
     * 127 has cycles by length: [256, 1024, 2304, 3328, 6400, 7168, 9984, 9984, 11776, 11776, 23552, 35072, 43520, 43520, 43520, 47104, 58880, 58880, 58880, 82432, 87040, 99840, 99840, 117760, 229632, 259072, 269568, 359424, 1000960, 1487616, 2284544, 9922560]
     * 129 has cycles by length: [1024, 1024, 1280, 1280, 11776, 24576, 36352, 39936, 58880, 119808, 189696, 211968, 341504, 348160, 391680, 435200, 878592, 1175040, 1327872, 1566720, 1610240, 2263040, 2390528, 3351040]
     * 131 has cycles by length: [7936, 8960, 15360, 15872, 15872, 17664, 17664, 18432, 18432, 27648, 35328, 36864, 52992, 52992, 56320, 60160, 70656, 71424, 84480, 88320, 95232, 110592, 123648, 123904, 129536, 141312, 186880, 194304, 238080, 331776, 353280, 371712, 372992, 460288, 608256, 732160, 1024512, 1158656, 1207296, 1833216, 2349312, 3856896]
     * 133 has cycles by length: [256, 768, 1024, 1536, 1536, 2048, 2304, 2560, 3072, 3840, 3840, 3840, 4608, 5376, 13056, 16896, 29696, 32256, 34304, 79360, 94976, 111104, 126976, 157440, 171520, 172800, 174592, 269824, 271360, 488448, 651776, 698368, 746240, 759808, 1112576, 1543680, 2603008, 6380544]
     * 135 has cycles by length: [256, 256, 256, 256, 256, 512, 1024, 1024, 1280, 1536, 3328, 3328, 3840, 3840, 4096, 4096, 4096, 7680, 9216, 9984, 12288, 13824, 14080, 17920, 19200, 21504, 26624, 28928, 28928, 31232, 32000, 41472, 41984, 45056, 61184, 69120, 74240, 87040, 122880, 183552, 325632, 367104, 2875648, 3487488, 3487488, 5200640]
     * 137 has cycles by length: [512, 512, 512, 1536, 2560, 2560, 3584, 3584, 7168, 9728, 12288, 22528, 26624, 26624, 33792, 43008, 44032, 74240, 226304, 252928, 307200, 386048, 1198080, 1280000, 1291264, 2457600, 3788800, 5273600]
     * 139 has cycles by length: [512, 1024, 1280, 1792, 2048, 2048, 2048, 3840, 5376, 6144, 6144, 7680, 8192, 9472, 9984, 9984, 10240, 12288, 14336, 14336, 18432, 23296, 29952, 30720, 48384, 49920, 61440, 69120, 91392, 116480, 120064, 152064, 204800, 215040, 245760, 368640, 507904, 524800, 860160, 2728960, 3148800, 7032320]
     * 141 has cycles by length: [512, 512, 2048, 2048, 2048, 2560, 3584, 4096, 6144, 7168, 8192, 9984, 19968, 20992, 24576, 32256, 35840, 39424, 40960, 48640, 53760, 53760, 55552, 55552, 55552, 59392, 73472, 80640, 87808, 88064, 107520, 107520, 134400, 277760, 288768, 611072, 888832, 1053696, 5444096, 6888448]
     * 143 has cycles by length: [768, 768, 6144, 6144, 12288, 23040, 32256, 52224, 62976, 64768, 64768, 259072, 1943040, 14248960]
     * 145 has cycles by length: [512, 1024, 1024, 2560, 3072, 5632, 5632, 6144, 8704, 8960, 8960, 17920, 39424, 41472, 44032, 44800, 62208, 69632, 90112, 115712, 116480, 134400, 241920, 268800, 481280, 528640, 564480, 600320, 673792, 1423872, 3898368, 7267328]
     * 147 has cycles by length: [1024, 4096, 6144, 6144, 6144, 15360, 15360, 16384, 43008, 67584, 67584, 129024, 230400, 240640, 387072, 399360, 688128, 1044480, 1419264, 2227200, 3870720, 5892096]
     * 149 has cycles by length: [768, 6144, 7168, 8704, 8960, 10496, 12032, 20992, 23808, 30464, 36096, 41984, 69120, 72192, 82688, 103936, 120576, 125952, 136448, 156416, 156416, 211456, 230912, 247296, 304384, 304384, 318720, 347648, 426496, 484096, 577024, 928256, 992256, 1511424, 1523712, 1555456, 2647040, 2935296]
     * 151 has cycles by length: [1024, 1280, 1280, 1280, 2048, 3840, 4352, 4352, 6400, 7680, 7680, 7680, 8704, 8704, 10240, 12800, 15360, 17408, 20480, 21760, 33792, 33792, 34816, 38400, 50176, 55040, 55040, 56576, 67840, 80896, 115200, 126208, 166400, 220160, 225280, 230656, 235520, 274176, 361216, 391680, 2036480, 2421760, 3027200, 6274560]
     * 153 has cycles by length: [512, 512, 3840, 3840, 6400, 7168, 12800, 30208, 38400, 79872, 109056, 109056, 275200, 534528, 1308672, 1363200, 1581312, 1824768, 4144128, 5343744]
     * 155 has cycles by length: [512, 512, 1024, 2048, 2048, 2048, 5120, 7168, 7168, 7168, 7424, 10752, 11008, 19456, 20480, 21504, 21504, 23040, 25600, 26880, 26880, 26880, 33792, 34560, 41472, 54528, 59904, 66560, 71680, 80640, 98304, 104448, 145152, 161280, 269568, 294912, 373248, 435456, 684288, 820736, 1747200, 2752512, 3359232, 4811520]
     * 157 has cycles by length: [256, 512, 512, 1024, 1024, 1024, 1792, 2048, 2048, 2560, 3584, 3584, 4096, 4096, 4608, 5888, 5888, 5888, 7168, 7424, 11264, 11520, 12288, 21248, 26112, 26624, 29440, 47616, 48384, 58112, 67584, 73216, 111872, 290560, 400384, 523008, 929792, 947968, 3138048, 9937152]
     * 159 has cycles by length: [256, 256, 512, 1536, 2048, 2816, 3072, 5376, 6144, 6656, 6656, 6656, 7680, 8448, 11520, 12288, 13312, 14336, 15360, 15360, 21504, 21504, 21504, 43008, 46080, 46592, 53248, 145920, 179712, 208896, 212992, 226304, 227328, 417792, 417792, 445440, 488448, 591872, 612352, 619008, 903168, 1183744, 1413120, 1996800, 2611200, 3481600]
     * 161 has cycles by length: [8448, 16896, 18432, 21760, 25344, 33792, 42240, 50688, 59136, 59136, 65280, 87040, 92160, 221184, 473088, 516096, 696320, 1824768, 1917696, 2045952, 3801600, 4700160]
     * 163 has cycles by length: [256, 512, 1024, 1536, 2048, 2560, 3072, 3840, 5120, 6400, 7680, 7680, 9216, 11264, 12288, 13824, 15872, 15872, 15872, 19200, 19200, 19200, 22528, 33024, 47616, 50688, 61440, 63488, 77824, 93696, 108032, 126976, 142848, 155648, 218624, 253952, 258048, 280320, 374784, 396800, 428544, 483840, 562176, 936960, 1030656, 1238016, 1285632, 1327104, 1745920, 4778496]
     * 165 has cycles by length: [256, 256, 512, 512, 768, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 2048, 2304, 2560, 2560, 3328, 3840, 5120, 6144, 9216, 12800, 13824, 16896, 18432, 23808, 24576, 25600, 36352, 36352, 36864, 37376, 52224, 55296, 70656, 71424, 81408, 110592, 142848, 261888, 273408, 476160, 497664, 508928, 654336, 1285632, 3833088, 8070144]
     * 167 has cycles by length: [10240, 18432, 19712, 36864, 39424, 55296, 58880, 87040, 110080, 124160, 161280, 198656, 221184, 227840, 405504, 985600, 1614080, 3981312, 4001536, 4420096]
     * 169 has cycles by length: [256, 256, 512, 512, 512, 512, 768, 1024, 1536, 1536, 2304, 2816, 3072, 4608, 7424, 7680, 9216, 10752, 10752, 14336, 15360, 15360, 16384, 18432, 27648, 30720, 30720, 30720, 32256, 44544, 45824, 46080, 46080, 50176, 56832, 57344, 61952, 70400, 76800, 82432, 93184, 109568, 123648, 123648, 182784, 184320, 211968, 240128, 308224, 585728, 3456000, 10221568]
     * 171 has cycles by length: [512, 512, 1792, 2816, 3072, 3584, 4096, 5120, 5120, 6400, 7168, 7680, 8192, 8960, 9728, 13312, 14336, 17664, 26112, 41984, 45824, 62720, 66560, 84224, 286720, 672768, 832000, 858624, 1009152, 1233408, 2915328, 8521728]
     * 173 has cycles by length: [256, 256, 512, 512, 768, 2304, 3072, 3328, 4096, 6400, 6656, 6656, 6912, 12544, 13824, 20736, 20736, 30720, 37632, 39936, 49920, 59136, 60672, 79872, 96768, 119808, 132096, 140800, 172800, 304128, 307200, 326400, 332800, 352000, 737280, 785408, 798720, 1198080, 1317888, 1320192, 3234816, 4632576]
     * 175 has cycles by length: [512, 512, 512, 1024, 1024, 1280, 3072, 4608, 5120, 5120, 7424, 11008, 16128, 18944, 22272, 23040, 24832, 64512, 115712, 451584, 903168, 1677312, 4064256, 9354240]
     * 177 has cycles by length: [256, 256, 512, 512, 768, 1024, 1024, 1024, 1280, 1536, 2048, 2560, 3328, 4096, 4096, 4352, 4608, 5120, 5120, 5632, 7680, 7680, 9216, 11520, 12800, 14336, 15872, 23552, 23808, 31744, 46080, 51456, 88320, 98304, 130560, 158208, 219648, 245760, 317440, 424960, 460800, 634880, 737280, 1290240, 1666560, 1843200, 2534400, 5621760]
     * 179 has cycles by length: [1792, 2048, 2048, 2048, 2048, 4096, 5376, 7168, 14336, 14336, 18432, 18432, 20480, 25344, 30720, 33792, 34304, 34304, 34304, 36864, 43008, 50688, 57344, 57344, 67584, 84480, 106496, 155648, 176128, 185856, 228096, 286720, 444416, 651776, 827904, 1406464, 1436160, 1638912, 1909248, 6620672]
     * 181 has cycles by length: [1280, 1536, 1792, 2560, 3072, 6144, 17664, 27648, 35840, 39168, 139520, 357888, 680960, 865024, 6138880, 8458240]
     * 183 has cycles by length: [256, 256, 512, 512, 512, 1024, 1280, 1536, 1536, 1792, 2048, 2560, 2560, 3072, 5120, 5376, 5888, 6144, 6400, 6400, 6656, 7680, 8192, 8960, 10240, 10240, 14080, 17920, 18944, 19200, 20480, 20480, 20480, 23552, 24064, 24320, 24832, 26112, 26112, 26624, 29440, 30720, 30720, 35840, 47360, 52224, 55296, 57600, 63488, 69632, 70656, 71680, 78336, 107520, 112640, 121856, 122880, 133120, 133120, 136192, 184320, 208896, 225792, 287232, 329728, 348160, 365568, 399360, 417792, 440320, 443904, 635904, 706560, 768000, 4262912, 4778496]
     * 185 has cycles by length: [256, 256, 256, 256, 256, 512, 512, 512, 768, 768, 768, 1792, 10496, 12032, 12032, 23808, 24576, 27136, 38656, 117760, 239616, 473088, 835584, 1884160, 4062720, 9008640]
     * 187 has cycles by length: [256, 2560, 3072, 4864, 4864, 9728, 29952, 32768, 41472, 43776, 48640, 86528, 170240, 963072, 2276352, 2456064, 4852224, 5750784]
     * 189 has cycles by length: [13568, 64512, 67840, 86016, 110592, 150528, 150528, 162816, 172032, 196608, 215040, 301056, 529152, 549632, 578560, 651264, 814080, 817152, 835584, 954624, 967680, 1179648, 1885952, 5322752]
     * 191 has cycles by length: [256, 2048, 2560, 4096, 4864, 5632, 5888, 5888, 6144, 22272, 23552, 29952, 47104, 49152, 52992, 55296, 59392, 73728, 88064, 92160, 98304, 98304, 124928, 135424, 172032, 212992, 347392, 356352, 399360, 614400, 936192, 1744896, 4055040, 6850560]
     * 193 has cycles by length: [256, 512, 2304, 3584, 5120, 9728, 12032, 12032, 16128, 16128, 16128, 23552, 112896, 125440, 157696, 172032, 189440, 241920, 290304, 387072, 467712, 805120, 1657600, 2580480, 3125760, 6346240]
     * 195 has cycles by length: [256, 512, 512, 512, 768, 1024, 1280, 2560, 3840, 3840, 3840, 5120, 5120, 8960, 10496, 11776, 16384, 19200, 36096, 43520, 58368, 58368, 58368, 62720, 75520, 115712, 136960, 138240, 170240, 525312, 960000, 1108992, 1575936, 11556864]
     * 197 has cycles by length: [2816, 3072, 3072, 3072, 5632, 6144, 7168, 8192, 9216, 10752, 18432, 18432, 24576, 43520, 43520, 43520, 48384, 57344, 57344, 110592, 174080, 189952, 202496, 229376, 301312, 411136, 581632, 768000, 1138688, 1419264, 1914880, 8921600]
     * 199 has cycles by length: [768, 1024, 1024, 2048, 2048, 2560, 3072, 3072, 3328, 3584, 4096, 6144, 6144, 6144, 8192, 22016, 22016, 35840, 35840, 44032, 48640, 71680, 86016, 107520, 150528, 179200, 185856, 220160, 229376, 243712, 322560, 433664, 465920, 535040, 2007040, 2157568, 3170304, 5949440]
     * 201 has cycles by length: [1792, 3584, 10752, 114688, 327936, 509952, 509952, 764928, 892416, 1211136, 1338624, 11091456]
     * 203 has cycles by length: [768, 768, 768, 1536, 6144, 7424, 7680, 8448, 14592, 21504, 23296, 30464, 30464, 38912, 43776, 48128, 51968, 63232, 82176, 91392, 97280, 104448, 107008, 135936, 140800, 182784, 216576, 227840, 264704, 279040, 360960, 413952, 938752, 1340416, 2589440, 2622976, 2647040, 3533824]
     * 205 has cycles by length: [256, 1024, 2048, 2048, 2560, 2560, 2560, 2560, 3072, 3328, 3584, 3584, 3584, 3840, 4352, 4608, 4864, 5120, 6144, 7424, 13056, 14336, 20480, 21504, 22272, 22528, 22528, 30464, 32768, 39168, 53248, 56576, 64000, 75264, 79872, 89600, 93696, 110592, 161792, 195840, 196608, 199680, 211968, 266240, 325632, 331776, 774656, 795648, 835584, 1179648, 1720320, 8650752]
     * 207 has cycles by length: [256, 512, 768, 768, 768, 1024, 1024, 1024, 1536, 3840, 6912, 8192, 8704, 9216, 13568, 14848, 16896, 16896, 19968, 24576, 26624, 36864, 38912, 40192, 40448, 51456, 86016, 184320, 185856, 257280, 454656, 463104, 463104, 463104, 823296, 958464, 1400832, 3241728, 3344640, 4065024]
     * 209 has cycles by length: [768, 1024, 3072, 3072, 5376, 7680, 12288, 13312, 13312, 13312, 36864, 38400, 39936, 46080, 93184, 100864, 100864, 100864, 119808, 133120, 304128, 479232, 1008640, 1078272, 1424384, 11599360]
     * 211 has cycles by length: [256, 256, 512, 512, 1280, 1280, 1536, 1536, 2048, 2560, 2816, 3072, 3072, 5376, 6144, 6144, 7168, 7424, 8192, 8704, 8704, 8704, 9984, 11008, 13056, 23040, 23808, 26112, 31488, 34560, 34816, 43008, 56576, 64512, 64512, 78336, 86016, 104448, 119808, 235008, 235008, 279552, 287232, 299520, 304640, 399360, 443904, 487424, 678912, 757248, 1139712, 1267200, 3827712, 5222400]
     * 213 has cycles by length: [256, 256, 256, 256, 256, 512, 512, 512, 512, 512, 512, 768, 768, 768, 768, 1536, 1792, 2048, 2048, 2304, 2560, 2560, 3584, 4096, 5888, 6144, 6912, 13312, 13568, 17152, 26112, 29440, 34304, 36096, 37888, 41472, 50176, 50944, 56320, 60672, 76800, 78336, 130560, 176640, 182784, 382720, 514048, 912640, 1166336, 1303552, 1523200, 1676800, 1869568, 6266880]
     * 215 has cycles by length: [512, 512, 1024, 1024, 1024, 2048, 3072, 4096, 4096, 4096, 7168, 7936, 11520, 15872, 16384, 17408, 17408, 18432, 20480, 20992, 29184, 29696, 35840, 36864, 37120, 37120, 37888, 38400, 46080, 80640, 82944, 109056, 119040, 124928, 132096, 148480, 179200, 193536, 259840, 269824, 523776, 668160, 1095168, 1299200, 3935232, 7052800]
     * 217 has cycles by length: [1024, 1024, 1024, 1024, 1280, 1536, 2048, 2048, 2048, 2048, 3072, 3072, 3328, 6144, 8448, 8704, 8704, 8704, 8704, 11520, 11520, 13056, 13312, 16640, 16640, 21504, 23040, 26624, 28672, 33280, 40192, 55296, 61440, 72704, 89600, 92160, 92160, 116480, 126720, 166400, 186368, 186368, 209664, 251904, 264192, 377856, 412672, 491520, 635392, 817920, 1558016, 1597440, 1774080, 2007040, 2312960, 2490880]
     * 219 has cycles by length: [1536, 4096, 4096, 8192, 8192, 20480, 36864, 36864, 36864, 57344, 59904, 90112, 145920, 218880, 462080, 656640, 847872, 860160, 875520, 958464, 1921280, 1945600, 2985984, 4534272]
     * 221 has cycles by length: [256, 512, 1024, 1024, 1024, 1536, 2048, 2048, 2048, 7168, 7168, 7168, 7168, 11520, 14080, 14336, 14336, 14336, 28160, 28160, 46080, 49664, 53248, 65536, 136192, 197120, 214016, 297472, 308224, 352000, 380160, 509952, 549120, 659456, 666624, 1062400, 1232384, 1317376, 2055680, 6459392]
     * 223 has cycles by length: [3072, 4608, 4608, 4864, 4864, 5376, 7680, 7680, 7680, 9728, 11264, 12288, 14336, 16896, 19456, 19456, 22272, 27648, 29184, 53248, 53248, 53248, 53504, 58368, 113664, 165888, 170240, 172032, 266240, 291840, 389120, 425984, 462080, 486400, 549632, 1331200, 2715648, 8732672]
     * 225 has cycles by length: [1024, 1024, 1536, 3072, 3072, 4096, 19968, 22528, 27648, 33536, 97280, 100608, 136192, 337920, 1073152, 1307904, 1383680, 1766400, 4386560, 6070016]
     * 227 has cycles by length: [256, 256, 512, 512, 768, 768, 1536, 1536, 3072, 3072, 3840, 4608, 4608, 4608, 4608, 6144, 9216, 12800, 13056, 13824, 18432, 18432, 20480, 21504, 26368, 26880, 33280, 38400, 38912, 43008, 45568, 78336, 81408, 81920, 119808, 150528, 286720, 532480, 580608, 1526784, 1802240, 2826240, 3333120, 4956160]
     * 229 has cycles by length: [12288, 14336, 18432, 43008, 57344, 57344, 58368, 113664, 139264, 152320, 218880, 218880, 262656, 262656, 272384, 583680, 822528, 875520, 3225600, 9368064]
     * 231 has cycles by length: [256, 512, 2048, 3072, 3840, 4608, 4864, 20480, 26624, 26624, 53248, 57344, 58368, 58368, 110592, 175104, 280576, 432896, 466944, 642048, 758784, 807424, 817152, 11965440]
     * 233 has cycles by length: [256, 1280, 1280, 1280, 1536, 2304, 2304, 2304, 3840, 4352, 6400, 6912, 8704, 11520, 11520, 13056, 13056, 16128, 20992, 22016, 22528, 22528, 22528, 23040, 32000, 32256, 39936, 48640, 59904, 59904, 62208, 72960, 92160, 92160, 102400, 105984, 112896, 142080, 156672, 158976, 159744, 165120, 179712, 218880, 219648, 279552, 326400, 360448, 403200, 450560, 483072, 585728, 743424, 998400, 1441792, 2117632, 2676480, 3354624]
     * 235 has cycles by length: [256, 256, 256, 512, 512, 512, 512, 768, 768, 768, 768, 768, 1536, 1536, 1536, 2048, 2048, 2816, 5632, 6144, 16384, 21248, 23808, 29184, 34304, 38912, 41472, 51456, 60672, 60672, 76800, 110592, 121344, 133632, 139776, 161792, 182016, 335872, 788736, 14318592]
     * 237 has cycles by length: [256, 512, 512, 2816, 8448, 8448, 9984, 14080, 17408, 20992, 26112, 33280, 34816, 161280, 174080, 253440, 268800, 426496, 433664, 537600, 1290240, 1549312, 1935360, 9569280]
     * 239 has cycles by length: [768, 1536, 3072, 3840, 6144, 6912, 6912, 7168, 8192, 17920, 20736, 22016, 25088, 34560, 35328, 52480, 56832, 56832, 59904, 97536, 99840, 117504, 150528, 168960, 197120, 304128, 324864, 470016, 518400, 519680, 629760, 1416960, 1469440, 2046720, 2046720, 5772800]
     * 241 has cycles by length: [1280, 1280, 2304, 2560, 2560, 2560, 3328, 5888, 6400, 7168, 7680, 13824, 14336, 15360, 19456, 26112, 28160, 33280, 34560, 53760, 56320, 56320, 57344, 78848, 84480, 93184, 148480, 247040, 313344, 432640, 809472, 816640, 1598464, 2585088, 2950656, 6167040]
     * 243 has cycles by length: [256, 512, 1024, 5120, 5632, 7936, 20992, 23040, 27904, 32768, 41984, 50688, 55808, 62976, 76800, 83712, 135168, 139520, 153600, 167424, 184320, 306944, 435200, 455680, 502272, 557568, 743424, 892928, 1391104, 2036224, 3211776, 4966912]
     * 245 has cycles by length: [256, 256, 512, 512, 768, 768, 768, 768, 768, 1536, 2048, 2048, 2048, 2560, 2816, 3072, 3072, 3072, 3328, 3840, 4864, 5120, 5632, 5632, 6144, 7936, 7936, 8192, 8192, 9216, 10496, 10752, 11264, 13568, 14336, 14848, 15360, 17152, 18432, 19456, 25088, 26112, 27136, 30720, 31488, 31744, 32256, 35840, 36864, 40704, 54528, 60672, 92160, 122880, 211968, 239616, 1026048, 1248256, 2143744, 11010048]
     * 247 has cycles by length: [1024, 2816, 2816, 2816, 3328, 3584, 3584, 3584, 3584, 3584, 5120, 5120, 7168, 12544, 12544, 17920, 25088, 25088, 28672, 39424, 50176, 50944, 51200, 70400, 71680, 75264, 76800, 78848, 79872, 152064, 152064, 329472, 358400, 376320, 448000, 479232, 678912, 992000, 1198080, 1956864, 3010560, 5830656]
     * 249 has cycles by length: [15616, 31232, 49920, 49920, 49920, 49920, 62464, 99840, 199680, 249856, 827648, 968192, 1842688, 1996800, 2496000, 7787520]
     * 251 has cycles by length: [2048, 2048, 3072, 6144, 27648, 40960, 42496, 42496, 42496, 81920, 82944, 84992, 101376, 156672, 169984, 273408, 637440, 716800, 764928, 3102208, 4403200, 5991936]
     * 253 has cycles by length: [1024, 2560, 2560, 2560, 3584, 3584, 3584, 3584, 4096, 4096, 5632, 6912, 6912, 7168, 8448, 11264, 12288, 19712, 21504, 26624, 27648, 27904, 38400, 46592, 46592, 46592, 55808, 55808, 55808, 55808, 56320, 57344, 93184, 96768, 100352, 102912, 137216, 143360, 157696, 171520, 222976, 251136, 463104, 465920, 497408, 562688, 675840, 741888, 1728000, 2795776, 3041536, 3599616]
     * 255 has cycles by length: [256, 256, 512, 1024, 3072, 3328, 3584, 3584, 3840, 4864, 7168, 7168, 14080, 17408, 23552, 25600, 38912, 59904, 61184, 75264, 122368, 175616, 489472, 645120, 2875648, 12114432]
     * <br>
     * TECHNIQUE 2:
     * <br>
     * 3 has cycles by length: [256, 512, 512, 768, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 2560, 2560, 2816, 3840, 14080, 15360, 15360, 15360, 15360, 15360, 15360, 24064, 24064, 24064, 24064, 24064, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 44032, 72192, 120320, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 220160, 264704, 439296, 721920, 721920, 1717248, 1717248, 1717248, 1717248, 2069504, 2069504]
     * 5 has cycles by length: [2048, 2048, 2048, 2048, 2048, 6144, 7680, 7680, 7680, 7680, 10240, 10240, 10240, 10240, 14848, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 29696, 29696, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 40960, 44544, 122880, 148480, 148480, 157696, 309248, 591360, 1143296, 1159680, 2242048, 3153920, 6184960]
     * 7 has cycles by length: [256, 256, 512, 512, 512, 512, 512, 1792, 3584, 6912, 8448, 8704, 8704, 8704, 13824, 16896, 29440, 29440, 35328, 35328, 35328, 35328, 38656, 58880, 77312, 206080, 247296, 317952, 317952, 317952, 388608, 388608, 388608, 600576, 600576, 794880, 971520, 1000960, 4445440, 5334528]
     * 9 has cycles by length: [1536, 1536, 4096, 4096, 6656, 35328, 35328, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 76544, 76544, 94208, 94208, 113152, 159744, 159744, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 905216, 1301248, 1301248]
     * 11 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2560, 2560, 5120, 5120, 8704, 8704, 12032, 17408, 17408, 24064, 24064, 24064, 48128, 51968, 102912, 103936, 103936, 103936, 120320, 205824, 207872, 409088, 519680, 1766912, 2418432, 10445568]
     * 13 has cycles by length: [1024, 1024, 1024, 1280, 1280, 1280, 3072, 3072, 3072, 3840, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 14336, 14336, 14336, 14336, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 43008, 43008, 43008, 43008, 55552, 55552, 55552, 71680, 166656, 215040, 215040, 215040, 215040, 215040, 215040, 215040, 215040, 215040, 333312, 444416, 444416, 444416, 444416, 444416, 444416, 444416, 1111040, 1333248, 1333248, 1333248, 1333248, 1333248, 1333248, 1333248]
     * 15 has cycles by length: [256, 256, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 20224, 20224, 43776, 43776, 63744, 101120, 218880, 318720, 3633408, 3633408, 3633408, 5035776]
     * 17 has cycles by length: [256, 512, 512, 512, 512, 768, 1536, 5888, 11776, 14848, 14848, 14848, 25600, 25600, 25600, 39168, 39168, 39168, 39168, 43264, 76800, 78336, 86528, 588800, 742400, 742400, 900864, 2271744, 4326400, 6619392]
     * 19 has cycles by length: [42240, 42240, 42240, 42240, 42240, 116480, 126720, 126720, 126720, 186368, 186368, 186368, 186368, 186368, 186368, 186368, 209664, 442624, 802560, 1490944, 2365440, 2399488, 2703360, 4350720]
     * 21 has cycles by length: [256, 256, 512, 512, 768, 768, 2304, 2304, 2304, 2304, 3072, 3072, 3840, 3840, 3840, 3840, 4608, 5632, 5632, 5632, 7680, 7936, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9984, 9984, 11264, 11264, 11264, 12288, 12288, 15360, 15360, 15360, 15872, 16896, 23808, 29440, 29440, 29952, 29952, 29952, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 34048, 36864, 36864, 36864, 46080, 46080, 46080, 49920, 49920, 49920, 61440, 61440, 61440, 68096, 88320, 88320, 88320, 88320, 88320, 95232, 101376, 101376, 101376, 101376, 101376, 101376, 102144, 135168, 135168, 135168, 135168, 135168, 135168, 219648, 264960, 285696, 309504, 380928, 408576, 439296, 647680, 912640, 1225728, 1295360, 1327872, 1634304, 3915520]
     * 23 has cycles by length: [1024, 3328, 3328, 7168, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 11264, 19456, 19456, 23296, 23296, 32256, 32256, 32256, 32256, 32256, 32256, 36608, 36608, 37888, 59904, 59904, 64512, 101376, 118272, 126464, 126464, 175104, 175104, 185344, 204288, 204288, 265216, 340992, 340992, 416768, 602368, 602368, 719872, 719872, 1668096, 1946112, 6857728]
     * 25 has cycles by length: [9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 9472, 23040, 23040, 23040, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 46080, 46080, 46080, 105984, 142080, 170496, 211968, 217856, 340992, 834048, 971520, 1562880, 1668096, 1714432, 7645440]
     * 27 has cycles by length: [512, 512, 768, 768, 768, 768, 768, 768, 768, 1536, 1536, 1536, 1536, 2048, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 6144, 6144, 8704, 8704, 10240, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 13056, 30720, 30720, 34816, 36864, 36864, 36864, 39168, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 52224, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 141312, 141312, 141312, 141312, 141312, 174080, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 235520, 235520, 235520, 235520, 235520, 235520, 235520, 235520, 423936, 783360, 2119680, 2119680, 2119680, 2119680]
     * 29 has cycles by length: [256, 256, 256, 256, 256, 256, 512, 512, 512, 768, 768, 768, 768, 768, 768, 768, 768, 1024, 1024, 1024, 1536, 2816, 2816, 3072, 5120, 5120, 5120, 5120, 5120, 5120, 5632, 5888, 5888, 5888, 8448, 8704, 8704, 8704, 8704, 11264, 15360, 15360, 15616, 15616, 15616, 17408, 17408, 17664, 18688, 18688, 26112, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 30976, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 33792, 37376, 46848, 56064, 56320, 56320, 64768, 74752, 87040, 87040, 87040, 87040, 92928, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 171776, 200192, 371712, 371712, 371712, 371712, 371712, 371712, 371712, 371712, 371712, 371712, 371712, 373760, 373760, 429824, 530944, 777216, 1053184, 1139968, 2061312, 2261248]
     * 31 has cycles by length: [512, 512, 768, 768, 1024, 1024, 1280, 1280, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2816, 2816, 3072, 5120, 6144, 10240, 10752, 10752, 10752, 10752, 10752, 11264, 22528, 43008, 43008, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 53760, 58112, 58112, 118272, 153600, 232448, 464896, 563200, 2440704, 11622400]
     * 33 has cycles by length: [2560, 3840, 3840, 3840, 3840, 3840, 5120, 15872, 18944, 24064, 27136, 31744, 37888, 42496, 48128, 49920, 54272, 84992, 98560, 119040, 142080, 152320, 180480, 203520, 309504, 318720, 369408, 469248, 529152, 611072, 729344, 828672, 926464, 944384, 1044736, 1127168, 1431808, 1614592, 1636096, 2528512]
     * 35 has cycles by length: [256, 512, 1024, 1024, 1024, 1792, 3840, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 4352, 7168, 8704, 10752, 15360, 17408, 19456, 19456, 19456, 19456, 19456, 21504, 21504, 24576, 24576, 24576, 24576, 24576, 30464, 59904, 59904, 59904, 65280, 182784, 299520, 299520, 299520, 330752, 417792, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 958464, 958464, 958464, 958464, 958464, 958464, 1018368, 2276352, 2276352]
     * 37 has cycles by length: [256, 512, 1024, 5120, 17152, 41472, 65280, 130560, 261120, 261120, 261120, 261120, 261120, 261120, 3525120, 3525120, 3525120, 4373760]
     * 39 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 3072, 3072, 3840, 3840, 3840, 3840, 5632, 5632, 6656, 6656, 6656, 6656, 8192, 8192, 8192, 8192, 14848, 14848, 14848, 14848, 15360, 16896, 19968, 19968, 24576, 24576, 27392, 27392, 27392, 27392, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 42240, 44544, 44544, 61952, 61952, 99840, 109568, 122880, 185856, 222720, 253952, 253952, 253952, 253952, 301312, 349184, 412672, 412672, 464640, 712192, 876544, 920576, 920576, 1588736, 3314432, 3841024]
     * 41 has cycles by length: [512, 512, 512, 1536, 1536, 1536, 2048, 2048, 2560, 2560, 2560, 6144, 6144, 10240, 10240, 12800, 12800, 12800, 20992, 20992, 20992, 20992, 20992, 20992, 35840, 35840, 35840, 35840, 35840, 35840, 51200, 51200, 59904, 59904, 59904, 59904, 71680, 71680, 71680, 71680, 71680, 71680, 71680, 71680, 83968, 83968, 83968, 83968, 299520, 1497600, 2456064, 2456064, 4193280, 4193280]
     * 43 has cycles by length: [1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 9984, 11520, 12544, 12544, 12544, 12544, 12544, 12544, 12544, 19712, 37632, 37632, 37632, 37632, 37632, 37632, 37632, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 42240, 69888, 73472, 80640, 109824, 126720, 295680, 409344, 472320, 489216, 489216, 489216, 489216, 564480, 564480, 564480, 564480, 1731840, 2069760, 2069760, 2069760, 2069760]
     * 45 has cycles by length: [256, 256, 256, 256, 512, 768, 768, 768, 768, 1024, 1536, 2560, 2560, 2560, 2560, 2560, 2560, 3072, 5120, 5120, 9728, 9728, 9728, 9728, 9728, 9728, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 19456, 19456, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 47616, 47616, 47616, 47616, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 199680, 238080, 238080, 291840, 291840, 380928, 380928, 380928, 380928, 380928, 380928, 904704, 904704, 1238016, 1238016, 1238016, 1238016, 1238016, 1238016]
     * 47 has cycles by length: [512, 512, 512, 1536, 1536, 9728, 35328, 65024, 65024, 65024, 81408, 195072, 195072, 1235456, 4486656, 10338816]
     * 49 has cycles by length: [3584, 5888, 18944, 37120, 39424, 64768, 208384, 304640, 408320, 500480, 569856, 631040, 631040, 631040, 631040, 631040, 936192, 1610240, 3012096, 5902080]
     * 51 has cycles by length: [512, 512, 512, 512, 768, 768, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 2560, 2560, 3840, 3840, 3840, 3840, 3840, 3840, 3840, 3840, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 22528, 22528, 22528, 22528, 46592, 46592, 53760, 53760, 53760, 53760, 53760, 53760, 58624, 58624, 67584, 69888, 112640, 116480, 293120, 337920, 349440, 4103680, 5158912, 5334784]
     * 53 has cycles by length: [5120, 5120, 5120, 5120, 5120, 46080, 80640, 175360, 1285120, 2313216, 4048128, 8803072]
     * 55 has cycles by length: [512, 1536, 8192, 17664, 17664, 17664, 17664, 24576, 30720, 30720, 33792, 33792, 39168, 39168, 39168, 39168, 122880, 122880, 122880, 122880, 122880, 122880, 122880, 122880, 270336, 270336, 270336, 270336, 706560, 706560, 706560, 777216, 777216, 777216, 1566720, 1566720, 1566720, 1723392, 1723392, 1723392]
     * 57 has cycles by length: [1536, 1536, 1536, 2560, 2560, 2560, 4864, 6656, 6656, 6656, 8704, 8704, 8704, 9216, 9216, 9216, 9728, 15872, 15872, 15872, 16128, 32256, 44544, 74240, 141056, 172032, 172032, 193024, 252416, 267264, 286720, 286720, 460288, 467712, 516096, 516096, 516096, 516096, 516096, 516096, 516096, 516096, 516096, 516096, 516096, 745472, 745472, 974848, 974848, 1089536, 1777664, 1777664]
     * 59 has cycles by length: [768, 768, 1536, 1536, 1536, 1536, 1536, 1536, 3584, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 9984, 9984, 10752, 10752, 14336, 14336, 19968, 19968, 20736, 20736, 20736, 20736, 20736, 20736, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 53760, 53760, 60416, 69888, 69888, 120832, 120832, 120832, 120832, 181248, 181248, 290304, 785408, 785408, 785408, 906240, 906240, 4893696, 5497856]
     * 61 has cycles by length: [256, 256, 256, 256, 256, 256, 256, 256, 768, 768, 768, 768, 1280, 1280, 1280, 1280, 2048, 2048, 2048, 2048, 6144, 6144, 6144, 6144, 13568, 13568, 20992, 20992, 20992, 20992, 33792, 33792, 33792, 33792, 40704, 50944, 50944, 67840, 108544, 152832, 254720, 325632, 407552, 1112576, 1222656, 1790976, 4177408, 6724608]
     * 63 has cycles by length: [256, 256, 256, 256, 3072, 3072, 4352, 4352, 6656, 6656, 26368, 26368, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 39936, 39936, 52224, 61952, 61952, 316416, 805376, 805376, 1053184, 3345408, 3345408, 6381056]
     * 65 has cycles by length: [256, 256, 256, 256, 768, 768, 1280, 1280, 3840, 3840, 3840, 3840, 3840, 3840, 3840, 3840, 4864, 4864, 5376, 5376, 5632, 5632, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 15104, 15104, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 24320, 26880, 28160, 63744, 63744, 63744, 63744, 63744, 75520, 318720, 318720, 318720, 446208, 446208, 446208, 956160, 956160, 956160, 1211136, 1402368, 3760896, 4462080]
     * 67 has cycles by length: [512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2816, 2816, 2816, 3328, 3328, 3328, 4608, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 16384, 16384, 23040, 24320, 24320, 24320, 25344, 28160, 29952, 30208, 30208, 32000, 32000, 32000, 33280, 48640, 48640, 48640, 48640, 48640, 64000, 64000, 64000, 64000, 64000, 66560, 66560, 81920, 81920, 146432, 151040, 151040, 180224, 212992, 218880, 288000, 332288, 392704, 1264640, 1556480, 1664000, 2048000, 2869760, 3776000]
     * 69 has cycles by length: [256, 512, 768, 768, 768, 768, 768, 1280, 1536, 1536, 1536, 1536, 1536, 2560, 3840, 7680, 24064, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 63232, 72192, 120320, 126464, 189696, 379392, 5943808, 9484800]
     * 71 has cycles by length: [512, 512, 512, 4352, 4352, 4352, 6912, 6912, 6912, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 9728, 9728, 13056, 13056, 13056, 13568, 13568, 13568, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 27136, 27136, 27136, 92672, 165376, 235008, 262656, 461312, 461312, 461312, 496128, 515584, 515584, 515584, 787712, 1251072, 2363136, 2455808, 4911616]
     * 73 has cycles by length: [256, 512, 1024, 1280, 1536, 1536, 1536, 2560, 3072, 3072, 3328, 3584, 5120, 5888, 6656, 10752, 10752, 13312, 17920, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 29440, 35328, 40704, 46592, 54272, 76544, 81408, 129024, 129024, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 162816, 271360, 423936, 569856, 705536, 936192, 976896, 976896, 976896, 976896]
     * 75 has cycles by length: [256, 256, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1792, 2048, 2048, 2048, 2304, 2560, 2560, 2560, 3584, 3840, 3840, 4352, 4352, 4608, 4608, 4608, 4608, 6144, 6144, 6400, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 8704, 8704, 9728, 9728, 9728, 9728, 9728, 9728, 10240, 10240, 10240, 10752, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 12800, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 19200, 19200, 19200, 19200, 19200, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 25856, 25856, 26112, 26880, 29184, 29184, 29184, 29184, 30464, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 34816, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 38400, 39168, 43520, 51712, 51712, 52992, 52992, 52992, 57600, 57600, 57600, 57600, 57600, 80640, 88320, 88320, 88320, 92160, 92160, 92160, 92160, 92160, 92160, 108800, 123648, 141312, 145920, 145920, 155136, 165376, 165376, 174080, 176640, 180992, 206848, 232704, 258560, 264960, 264960, 264960, 300288, 437760, 437760, 441600, 646400, 671232, 671232, 706560, 982528, 982528, 1034240, 1784064]
     * 77 has cycles by length: [768, 768, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 1536, 1536, 1536, 1536, 1536, 1536, 3328, 3328, 3840, 6144, 6912, 6912, 6912, 7680, 7680, 10240, 16640, 19968, 19968, 26624, 31488, 33024, 34560, 52480, 55040, 60160, 60160, 60160, 60160, 60160, 60160, 60160, 89856, 90624, 136448, 143104, 151040, 360960, 360960, 392704, 481280, 1624320, 2466560, 2586880, 7098880]
     * 79 has cycles by length: [256, 1280, 1536, 2048, 10752, 49664, 65280, 65280, 65280, 65280, 65280, 65280, 130560, 130560, 130560, 522240, 913920, 913920, 913920, 12664320]
     * 81 has cycles by length: [3584, 3584, 6656, 6656, 20992, 20992, 34304, 34304, 53760, 89600, 99840, 166400, 314880, 383488, 383488, 514560, 524800, 712192, 712192, 857600, 2246144, 2246144, 3670528, 3670528]
     * 83 has cycles by length: [256, 256, 512, 512, 512, 512, 9216, 9216, 9216, 9216, 9216, 13056, 16384, 16384, 16384, 16384, 16384, 17408, 17408, 18944, 18944, 26112, 26112, 26368, 27904, 27904, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52224, 52736, 52736, 156672, 156672, 156672, 156672, 278528, 278528, 278528, 278528, 340992, 340992, 606208, 606208, 966144, 1004544, 1423104, 1785856, 1793024, 1951232, 2874112]
     * 85 has cycles by length: [256, 256, 512, 512, 512, 512, 768, 768, 1024, 1024, 1024, 1024, 1024, 1280, 1280, 2560, 2560, 2560, 2560, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3840, 3840, 22528, 22528, 22528, 22528, 22528, 28416, 28416, 35072, 35072, 35840, 35840, 35840, 35840, 35840, 67584, 107520, 113664, 113664, 113664, 113664, 140288, 142080, 142080, 175360, 175360, 420864, 2500608, 3086336, 3978240, 4910080]
     * 87 has cycles by length: [1792, 1792, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 6144, 6144, 6144, 6144, 6144, 6144, 8960, 9472, 9472, 10240, 10752, 10752, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18944, 18944, 19712, 19712, 19712, 19712, 19712, 19712, 19712, 21504, 21504, 21504, 21504, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23296, 23808, 23808, 23808, 23808, 23808, 23808, 23808, 23808, 26624, 26880, 28416, 28416, 46080, 46080, 46080, 46080, 46080, 46080, 47360, 47616, 47616, 59136, 59136, 59136, 59136, 59136, 59136, 59136, 69888, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 92160, 95232, 95232, 95232, 113664, 116480, 119040, 123136, 129024, 129024, 129024, 129024, 133120, 157696, 299520, 299520, 299520, 299520, 299520, 299520, 309504, 349440, 571392, 571392, 571392, 615680, 681984, 729344, 1547520, 1774080, 1833216]
     * 89 has cycles by length: [512, 512, 1024, 1024, 1536, 1536, 3072, 3072, 3584, 7168, 10752, 10752, 10752, 10752, 10752, 11264, 11264, 11264, 11264, 11264, 11264, 17152, 17152, 17920, 21504, 21504, 21504, 21504, 21504, 25088, 25088, 25088, 33792, 33792, 33792, 33792, 33792, 33792, 35840, 42240, 42240, 50176, 50176, 50176, 53760, 75264, 75264, 75264, 107520, 120064, 150528, 150528, 150528, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 168960, 295680, 295680, 295680, 295680, 295680, 295680, 295680, 295680, 295680, 360192, 600320, 754688, 840448, 1680896, 2069760, 4139520]
     * 91 has cycles by length: [256, 256, 512, 512, 768, 768, 4352, 4352, 20224, 20224, 28928, 36096, 36096, 36096, 36096, 39424, 39424, 57856, 72192, 86784, 491776, 613632, 2285312, 2851584, 4454912, 5558784]
     * 93 has cycles by length: [768, 6144, 10752, 19968, 26880, 64768, 132096, 518144, 906752, 1683968, 2266880, 11140096]
     * 95 has cycles by length: [768, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 3072, 3072, 8192, 8192, 8192, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 16128, 23552, 23552, 24576, 24576, 26880, 27136, 27136, 27136, 32256, 32256, 32256, 32256, 35328, 35840, 80640, 80640, 80640, 80640, 80640, 80640, 80640, 81408, 81408, 84992, 84992, 127488, 188416, 188416, 282624, 282624, 286720, 430080, 624128, 624128, 679936, 679936, 741888, 949760, 1019904, 1019904, 2252288, 2252288, 2677248]
     * 97 has cycles by length: [768, 768, 768, 768, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 7680, 7680, 7680, 8448, 8448, 8448, 8448, 9984, 9984, 13056, 13312, 13312, 13312, 15360, 15360, 15360, 15360, 16896, 16896, 16896, 17408, 17408, 19968, 33792, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 50944, 56576, 84480, 84480, 84480, 93184, 93184, 93184, 93184, 99840, 109824, 139776, 143616, 152832, 203776, 305664, 662272, 866048, 1528320, 1537536, 9271808]
     * 99 has cycles by length: [256, 256, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 3072, 3072, 3328, 3328, 5120, 5120, 5120, 5120, 5120, 10496, 10496, 10752, 13312, 13312, 13312, 19968, 20480, 20480, 20480, 20480, 20480, 21504, 21504, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 24064, 26880, 26880, 41984, 41984, 41984, 48128, 48128, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 62976, 66560, 70656, 70656, 107520, 107520, 107520, 107520, 107520, 107520, 107520, 107520, 117760, 117760, 117760, 117760, 139776, 209920, 266240, 312832, 430080, 430080, 430080, 430080, 430080, 440832, 471040, 471040, 471040, 471040, 494592, 494592, 839680, 986624, 1106944, 1106944, 2526720]
     * 101 has cycles by length: [512, 1536, 1536, 3840, 4352, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 11008, 23040, 23040, 23040, 23040, 23040, 23040, 26112, 41216, 66048, 112128, 130560, 247296, 280320, 280320, 280320, 330240, 336384, 336384, 336384, 953088, 1236480, 2410752, 9026304]
     * 103 has cycles by length: [256, 256, 512, 512, 5376, 8704, 8704, 8704, 8704, 8704, 8704, 21248, 21248, 35328, 35328, 35328, 35328, 35328, 35328, 42496, 42496, 58624, 182784, 247296, 247296, 247296, 446208, 1993216, 4865792, 8090112]
     * 105 has cycles by length: [7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 14336, 14336, 14336, 14336, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 78848, 88064, 96256, 96256, 121088, 154112, 168448, 168448, 186368, 256256, 264704, 1598464, 2454784, 5195008, 5366272]
     * 107 has cycles by length: [1792, 1792, 1792, 1792, 5376, 8960, 9472, 9472, 9472, 9472, 14336, 20992, 20992, 20992, 20992, 28416, 33280, 33280, 33280, 33280, 33280, 33280, 33280, 33280, 33280, 47360, 62976, 75776, 83968, 83968, 99840, 104960, 133120, 133120, 422912, 2235392, 2477056, 2477056, 3927040, 3927040]
     * 109 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1280, 1280, 1280, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 3072, 3072, 5120, 5120, 7168, 7168, 7680, 7680, 7680, 8960, 8960, 8960, 8960, 8960, 14592, 14592, 14592, 17152, 17152, 17152, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 28928, 29184, 29184, 29184, 29184, 34304, 35840, 57856, 74752, 93440, 102912, 115712, 144640, 145920, 171520, 173568, 186880, 204288, 240128, 289280, 289280, 404992, 510720, 600320, 1012480, 1065216, 1252096, 1648896, 1938176, 2111744]
     * 111 has cycles by length: [2560, 3840, 7680, 7680, 7680, 7680, 7680, 18432, 18432, 18432, 18432, 18432, 21760, 27136, 27136, 29440, 29440, 29440, 29440, 29440, 35840, 37120, 37376, 40960, 56064, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 81408, 92160, 92160, 92160, 92160, 92160, 92160, 129024, 129024, 129024, 129024, 313344, 317696, 379904, 379904, 407040, 407040, 434176, 434176, 461312, 523264, 534528, 541952, 560640, 598016, 786944, 2119680, 2149120, 3120640]
     * 113 has cycles by length: [1280, 1280, 1280, 3584, 3584, 3584, 8960, 8960, 8960, 8960, 8960, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 17408, 17408, 17408, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 26880, 43264, 43264, 43264, 252160, 365568, 609280, 706048, 908544, 1514240, 3429376, 8523008]
     * 115 has cycles by length: [256, 256, 512, 1536, 1536, 1536, 1536, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4608, 4608, 4608, 4608, 6656, 7168, 9216, 9216, 9216, 9728, 12032, 14080, 14080, 17920, 17920, 17920, 17920, 19968, 19968, 21504, 21504, 23552, 28160, 29184, 29184, 29440, 29440, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 58880, 59904, 64512, 70656, 70656, 72192, 87552, 107520, 107520, 108288, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 117760, 126720, 161280, 168960, 211968, 232960, 232960, 264960, 340480, 340480, 353280, 366080, 394240, 535040, 661760, 765440, 824320, 824320, 824320, 842240, 1118720, 1295360, 1383680]
     * 117 has cycles by length: [512, 1536, 1792, 3584, 5376, 10752, 11264, 11264, 13312, 13312, 17408, 17408, 22528, 22528, 59648, 78848, 78848, 78848, 93184, 93184, 93184, 121856, 121856, 121856, 157696, 157696, 157696, 178944, 2624512, 3101696, 4056064, 5249024]
     * 119 has cycles by length: [4352, 5376, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 8704, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 11520, 23040, 23040, 23040, 23040, 26112, 30464, 43008, 43008, 53760, 53760, 60928, 60928, 60928, 60928, 60928, 60928, 60928, 60928, 60928, 69888, 69888, 69888, 69888, 69888, 69888, 69888, 161280, 182784, 396032, 396032, 396032, 396032, 396032, 396032, 396032, 396032, 436224, 436224, 545280, 545280, 559104, 617984, 698880, 763392, 1048320, 1635840, 4325888]
     * 121 has cycles by length: [256, 256, 512, 512, 768, 768, 768, 768, 768, 768, 1536, 1792, 1792, 3328, 5376, 6400, 6400, 6656, 7424, 7424, 9984, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 12032, 14848, 14848, 19200, 21504, 21504, 21504, 21504, 21504, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 22272, 23296, 24064, 34048, 34048, 34048, 34048, 34048, 34048, 34048, 34048, 36096, 36096, 51968, 51968, 68096, 83200, 84224, 102144, 156416, 185600, 185600, 279552, 289536, 300800, 348928, 348928, 408576, 408576, 408576, 408576, 408576, 408576, 408576, 623616, 623616, 851200, 1010688, 1046784, 1600256, 2962176]
     * 123 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 6144, 9984, 9984, 9984, 9984, 11776, 11776, 11776, 11776, 11776, 12288, 12288, 18176, 19968, 19968, 22016, 22016, 22016, 22016, 22016, 35328, 36352, 36352, 54528, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 66048, 94208, 94208, 159744, 176128, 176128, 178176, 178176, 290816, 1366016, 1366016, 2316288, 2553856, 2553856, 4216832]
     * 125 has cycles by length: [256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 512, 512, 512, 768, 768, 768, 768, 1280, 1280, 1280, 1280, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2560, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 30720, 30720, 51712, 51712, 51712, 59136, 59136, 59136, 59136, 118272, 155136, 155136, 155136, 206848, 206848, 258560, 473088, 473088, 473088, 473088, 473088, 473088, 11945472]
     * 127 has cycles by length: [512, 512, 512, 512, 1536, 2816, 2816, 5632, 8448, 8704, 8704, 8960, 8960, 15872, 17920, 26880, 34304, 53248, 53248, 53248, 53248, 59904, 87296, 95744, 159744, 188672, 277760, 304640, 329472, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 600320, 905216, 905216, 1048320, 1650688, 3567616]
     * 129 has cycles by length: [1024, 1792, 2304, 2304, 2304, 2304, 3072, 5120, 5120, 5120, 5120, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5376, 5888, 16128, 16128, 16128, 16128, 16128, 16128, 17664, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 21504, 21504, 34048, 35840, 46080, 61440, 97280, 97280, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 102144, 117760, 123648, 123648, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 340480, 389120, 389120, 389120, 389120, 389120, 389120, 389120, 389120, 389120, 389120, 430080, 430080, 437760, 680960, 1118720]
     * 131 has cycles by length: [1024, 3584, 4096, 4096, 4096, 4096, 5888, 7680, 13824, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 28672, 28672, 33536, 44032, 44032, 46080, 48128, 61440, 61440, 62464, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 94208, 110592, 110592, 154112, 154112, 161280, 168448, 218624, 264960, 276736, 330240, 330240, 359168, 360960, 468480, 506368, 536576, 594432, 594432, 649728, 843264, 1509120, 1576192, 2045696, 2884096]
     * 133 has cycles by length: [256, 256, 512, 512, 512, 512, 512, 768, 768, 768, 768, 768, 768, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 3072, 3072, 3072, 3328, 3328, 3584, 3584, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5376, 5376, 5376, 6656, 7168, 7168, 7424, 7424, 8448, 8448, 11776, 11776, 11776, 11776, 13056, 13056, 13056, 13056, 13056, 13056, 13056, 13056, 13056, 13056, 13312, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14848, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 16896, 23296, 23552, 23552, 26112, 27648, 27648, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 29696, 33792, 35840, 43008, 43008, 43008, 51968, 52224, 59136, 66560, 82432, 91392, 96768, 107520, 117760, 117760, 143360, 143360, 143360, 143360, 143616, 143616, 143616, 148480, 152064, 152064, 152064, 168960, 168960, 168960, 168960, 169728, 179712, 186368, 199680, 317952, 317952, 329728, 329728, 353280, 353280, 378624, 400896, 415744, 430080, 430080, 430080, 430080, 445440, 473088, 600576, 774144, 774144, 1462272]
     * 135 has cycles by length: [512, 512, 9728, 9728, 14080, 14080, 16384, 16384, 18944, 18944, 23040, 36352, 41216, 41216, 126720, 126720, 126720, 126720, 126720, 311296, 311296, 359936, 359936, 437760, 690688, 901120, 999680, 1041920, 1854720, 2637824, 2926336, 3049984]
     * 137 has cycles by length: [1792, 1792, 1792, 1792, 2048, 2048, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 8448, 10240, 10240, 17152, 17152, 17920, 17920, 19712, 19712, 22528, 34304, 34304, 84480, 133120, 133120, 171520, 171520, 171520, 184576, 184576, 188672, 210944, 232960, 232960, 377344, 870144, 1098240, 1766656, 2229760, 2229760, 2229760, 3533312]
     * 139 has cycles by length: [256, 256, 256, 256, 256, 256, 1024, 1024, 1024, 1536, 1536, 1536, 4352, 4352, 4352, 5120, 5120, 5120, 11264, 11264, 11264, 41728, 41728, 41728, 64768, 64768, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 259072, 388608, 1101056, 1295360, 10557184]
     * 141 has cycles by length: [512, 768, 768, 1536, 2304, 2304, 2304, 2304, 2304, 2304, 2560, 2816, 3840, 3840, 4608, 4608, 4608, 4608, 7168, 7168, 7680, 9472, 14080, 14848, 17664, 21504, 21504, 21504, 21504, 22272, 22272, 25344, 32000, 32000, 32000, 32000, 32000, 32000, 44544, 47104, 47104, 47360, 52992, 52992, 52992, 78848, 81664, 85248, 88320, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 141312, 265216, 274688, 288000, 494592, 512256, 518144, 896000, 928000, 1742848, 5888000]
     * 143 has cycles by length: [256, 768, 1792, 2048, 2048, 2560, 3840, 3840, 3840, 3840, 4608, 5120, 5376, 7680, 7680, 7680, 7680, 7680, 7936, 14336, 14336, 15360, 15360, 15360, 15360, 15360, 17920, 18432, 21760, 23040, 23040, 23040, 30720, 30720, 32256, 35840, 55552, 59648, 65280, 65280, 65280, 65280, 65280, 92160, 92160, 92160, 119040, 129024, 152320, 178944, 477184, 477184, 596480, 1073664, 1192960, 1849088, 4294656, 5070080]
     * 145 has cycles by length: [1280, 1280, 1280, 1280, 1280, 1280, 1792, 1792, 1792, 1792, 2560, 2560, 2560, 3584, 3584, 12032, 12032, 14080, 14080, 14080, 18944, 18944, 18944, 18944, 19712, 19712, 24064, 27136, 27136, 27136, 27136, 28160, 28160, 28160, 39424, 39424, 56320, 56320, 56320, 66560, 66560, 66560, 78848, 78848, 93184, 93184, 132352, 157440, 157440, 157440, 208384, 208384, 208384, 220416, 220416, 264704, 298496, 298496, 298496, 416768, 416768, 492544, 492544, 529408, 596992, 596992, 625664, 705536, 705536, 1479936, 2330112, 3337728]
     * 147 has cycles by length: [256, 256, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 5888, 7168, 7168, 10496, 12800, 15872, 15872, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 23552, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25600, 25600, 31744, 31744, 33024, 41216, 41984, 50176, 50176, 73472, 89600, 111104, 132096, 231168, 365056, 396800, 396800, 494592, 537600, 537600, 577024, 627200, 627200, 650752, 881664, 924672, 924672, 924672, 1028608, 2047488, 3236352]
     * 149 has cycles by length: [1280, 1280, 1280, 1280, 1280, 2560, 2560, 2560, 2560, 2560, 3072, 3072, 5120, 7680, 7680, 7680, 8960, 10752, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 13312, 13312, 25600, 25600, 33280, 33280, 33280, 46592, 52224, 60416, 60416, 62976, 62976, 62976, 65280, 89600, 91392, 130560, 136960, 151040, 151040, 151040, 157440, 164352, 211456, 535296, 535296, 535296, 712192, 818688, 1369600, 1396992, 1574400, 3232256, 3715584]
     * 151 has cycles by length: [256, 256, 256, 512, 512, 1280, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 14080, 14080, 14080, 14080, 14080, 14080, 14080, 14080, 17920, 20480, 28160, 28160, 44544, 47104, 47104, 47104, 47104, 47104, 47104, 47104, 143360, 143360, 197120, 197120, 197120, 197120, 197120, 235520, 356352, 356352, 1648640, 1648640, 2449920, 4098048, 4098048]
     * 153 has cycles by length: [256, 256, 256, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1792, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5888, 7168, 7168, 7168, 7168, 7168, 10496, 11776, 11776, 15104, 15104, 15104, 15872, 15872, 15872, 15872, 15872, 20992, 20992, 23552, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 30208, 31744, 31744, 31744, 31744, 41984, 44032, 44032, 44032, 44032, 44032, 44032, 44032, 44032, 44032, 60416, 60416, 82432, 105728, 111104, 146944, 308224, 308224, 347392, 365056, 619264, 650752, 659456, 1175552, 1232896, 1232896, 1232896, 1232896, 1364992, 1364992, 2597888]
     * 155 has cycles by length: [512, 512, 512, 1024, 1024, 1024, 3584, 3584, 3584, 3840, 7680, 7680, 7680, 7680, 7936, 8704, 8704, 13824, 15872, 16640, 17408, 17408, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 20992, 24320, 26880, 26880, 26880, 26880, 26880, 27648, 29696, 29696, 29696, 29696, 29696, 29696, 33280, 34560, 34560, 34560, 35840, 41984, 48640, 53760, 53760, 53760, 53760, 53760, 60928, 60928, 65280, 65280, 69120, 69120, 69120, 96768, 116480, 116480, 116480, 116480, 116480, 130560, 130560, 134912, 134912, 146944, 157440, 170240, 170240, 170240, 170240, 170240, 207872, 207872, 214272, 277760, 282880, 282880, 314880, 325376, 413440, 413440, 445440, 445440, 445440, 449280, 656640, 682240, 920576, 997120, 1930240, 2821120]
     * 157 has cycles by length: [512, 512, 512, 1024, 1024, 1792, 2816, 3584, 3584, 3584, 3584, 5376, 5632, 7168, 7168, 7168, 10752, 11264, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 21504, 24832, 49664, 99328, 127488, 446208, 446208, 446208, 446208, 701184, 892416, 1019904, 1019904, 1019904, 3569664, 6183168]
     * 159 has cycles by length: [512, 512, 1024, 1024, 1280, 1280, 5120, 13568, 13568, 50176, 50176, 50176, 50176, 50176, 50176, 54272, 64000, 64000, 64000, 64000, 64000, 64000, 64000, 3392000, 6272000, 6272000]
     * 161 has cycles by length: [12032, 53504, 180480, 802560, 878336, 2009344, 3905792, 8935168]
     * 163 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 7680, 56320, 56320, 62976, 62976, 62976, 62976, 125440, 314880, 314880, 381440, 1385472, 1385472, 3085824, 9383424]
     * 165 has cycles by length: [1536, 5120, 5120, 5120, 5120, 5120, 5120, 5376, 6144, 6144, 7680, 8448, 10240, 10240, 10240, 10240, 12032, 26112, 26112, 26112, 26880, 33024, 42240, 43008, 60160, 67584, 90624, 90624, 91392, 91392, 91392, 96256, 112128, 143616, 143616, 143616, 165120, 261120, 264192, 302080, 302080, 373760, 392448, 561408, 561408, 561408, 613632, 616704, 634368, 878336, 996864, 1419776, 2410752, 3896832]
     * 167 has cycles by length: [256, 1024, 1280, 1536, 1792, 3072, 3072, 5120, 8960, 10752, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 187392, 187392, 312320, 437248]
     * 169 has cycles by length: [512, 512, 512, 512, 512, 1536, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 1792, 2560, 2560, 2560, 2560, 2560, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3328, 3584, 3584, 3584, 5376, 6656, 6656, 6656, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7680, 9984, 17920, 23296, 23296, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 33280, 35840, 35840, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 50176, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 64512, 75264, 93184, 326144, 358400, 419328, 665600, 2508800, 2508800, 3225600, 3225600]
     * 171 has cycles by length: [1536, 1536, 1536, 1536, 2304, 2304, 2304, 2304, 3072, 3072, 3840, 3840, 3840, 3840, 8192, 8192, 8192, 8192, 8192, 9216, 9984, 9984, 9984, 9984, 12288, 12288, 12288, 12288, 12288, 12288, 15360, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 35328, 35328, 35328, 36864, 36864, 36864, 39680, 39936, 52992, 52992, 52992, 61440, 61440, 61440, 88320, 88320, 88320, 119040, 158720, 159744, 159744, 159744, 201216, 229632, 229632, 229632, 301824, 503040, 565248, 1073152, 1307904, 1904640, 2737920, 5198080]
     * 173 has cycles by length: [256, 512, 768, 768, 1024, 1024, 1024, 1536, 1536, 2816, 4864, 5632, 14592, 14592, 19456, 53504, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 119808, 119808, 658944, 1138176]
     * 175 has cycles by length: [512, 512, 512, 512, 2304, 2304, 2560, 2560, 2816, 2816, 6656, 6656, 11520, 14080, 14848, 14848, 29952, 36608, 40448, 40448, 44544, 44544, 74240, 80384, 80384, 182016, 193024, 222464, 222720, 361728, 442112, 579072, 1172992, 2331136, 3518976, 6993408]
     * 177 has cycles by length: [512, 768, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 6144, 6144, 6144, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 15104, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 46848, 46848, 46848, 93696, 120832, 120832, 120832, 135168, 135168, 135168, 135168, 135168, 135168, 135168, 135168, 374784, 724992, 1499136, 1499136, 1499136, 2764032, 4122624]
     * 179 has cycles by length: [256, 256, 768, 3328, 4352, 4608, 4608, 4608, 4608, 4608, 6144, 6400, 6400, 15616, 15616, 15872, 15872, 18432, 18432, 18432, 18432, 18432, 18432, 19200, 22784, 22784, 23552, 26880, 46848, 47616, 59904, 68352, 78336, 83200, 108800, 134400, 134400, 134400, 134400, 134400, 153600, 161280, 161280, 161280, 190464, 190464, 203008, 206336, 211968, 211968, 265472, 269824, 296192, 374784, 387328, 546816, 588800, 730112, 730112, 1436672, 1639680, 1666560, 2096128, 2392320]
     * 181 has cycles by length: [256, 256, 256, 512, 1280, 1280, 1280, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2816, 5888, 12544, 12544, 12544, 14080, 25088, 29440, 51456, 51456, 51456, 52992, 102912, 125440, 137984, 264960, 288512, 514560, 566016, 1183488, 2596608, 3550464, 3550464, 3550464]
     * 183 has cycles by length: [1792, 5376, 7168, 63744, 63744, 63744, 63744, 254976, 444416, 15808512]
     * 185 has cycles by length: [512, 512, 1536, 1536, 2048, 2048, 2048, 2048, 2560, 2560, 10496, 31232, 31232, 31488, 45568, 45568, 45568, 45568, 52480, 54016, 83968, 162048, 270080, 432128, 640256, 1868288, 3294976, 9614848]
     * 187 has cycles by length: [512, 512, 512, 512, 3072, 3072, 3072, 3072, 3584, 6656, 6656, 6656, 6656, 9728, 21504, 30208, 46592, 55296, 55296, 55296, 55296, 58368, 85504, 126464, 181248, 387072, 392704, 513024, 1050624, 1111552, 3262464, 9234432]
     * 189 has cycles by length: [2304, 2816, 4608, 5632, 9216, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 26624, 26624, 26624, 26624, 26624, 26624, 26624, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 36864, 51200, 51200, 51200, 51200, 51200, 57600, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 70400, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 184320, 225280, 225280, 225280, 225280, 225280, 266240, 266240, 266240, 266240, 266240, 266240, 266240, 266240, 281600, 307200, 405504, 405504, 405504, 405504, 405504, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 479232, 665600]
     * 191 has cycles by length: [256, 256, 768, 768, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 1280, 3072, 3840, 3840, 5888, 5888, 6656, 6656, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 13568, 15360, 17664, 17664, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 26880, 26880, 29440, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 32256, 33280, 33280, 67840, 70656, 92160, 107520, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 129024, 153088, 153088, 161280, 161280, 161280, 161280, 161280, 161280, 161280, 161280, 232960, 232960, 239616, 239616, 239616, 239616, 312064, 399360, 399360, 399360, 399360, 474880, 645120, 645120, 645120, 645120, 645120, 645120, 741888, 976896, 1628160]
     * 193 has cycles by length: [512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 2560, 2560, 5632, 5632, 7680, 7680, 8192, 8192, 8192, 8192, 8192, 8192, 12032, 12032, 12800, 14848, 14848, 14848, 14848, 14848, 14848, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 16896, 16896, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 24064, 24064, 28160, 28416, 28416, 38400, 40960, 40960, 56832, 56832, 74240, 74240, 84480, 90112, 90112, 120320, 122880, 122880, 122880, 122880, 163328, 163328, 204800, 264704, 284160, 300800, 319488, 319488, 371200, 445440, 445440, 450560, 568320, 568320, 568320, 579072, 579072, 625152, 661760, 710400, 721920, 738816, 738816, 738816, 816640, 938496, 1562880]
     * 195 has cycles by length: [768, 3072, 9216, 9216, 9216, 9216, 9216, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 12288, 21504, 21504, 33024, 36864, 36864, 36864, 36864, 44800, 64512, 64512, 86016, 86016, 89600, 89600, 89600, 89600, 89600, 89600, 89600, 136704, 179200, 396288, 462336, 716800, 820224, 820224, 956928, 956928, 1926400, 7974400]
     * 197 has cycles by length: [56832, 74240, 397824, 519680, 738816, 965120, 6081024, 7943680]
     * 199 has cycles by length: [256, 256, 512, 768, 768, 1536, 3328, 4864, 5888, 5888, 8960, 9984, 11776, 11776, 11776, 11776, 11776, 14592, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17664, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 26880, 28928, 28928, 29696, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 35328, 57856, 76544, 89088, 111872, 153088, 206080, 223744, 232960, 340480, 376064, 412160, 549632, 683008, 683008, 683008, 1012480, 1039360, 1039360, 1236480, 1996032, 3355648]
     * 201 has cycles by length: [2816, 2816, 17152, 17152, 19712, 22528, 22528, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 84480, 120064, 157696, 161280, 290048, 321024, 337920, 337920, 437760, 437760, 437760, 437760, 437760, 437760, 514560, 1284096, 1284096, 1766656, 1955328, 2320384, 2373120]
     * 203 has cycles by length: [256, 256, 256, 256, 256, 256, 256, 256, 512, 512, 512, 512, 512, 512, 512, 512, 768, 768, 768, 768, 768, 768, 768, 768, 768, 1536, 1536, 3328, 3328, 3328, 3328, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 9984, 11520, 11520, 11520, 11520, 11520, 11520, 11520, 12544, 12544, 13568, 13568, 13568, 13568, 16384, 16384, 16384, 16384, 16384, 16384, 25088, 25088, 30208, 30208, 30208, 30208, 34816, 34816, 34816, 34816, 34816, 34816, 37632, 40704, 49152, 90624, 104448, 147456, 147456, 163072, 212992, 225792, 313344, 313344, 452608, 564480, 664832, 737280, 868352, 966656, 966656, 1480192, 1566720, 1845248, 2054144, 2054144]
     * 205 has cycles by length: [256, 256, 512, 768, 768, 768, 768, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2304, 2304, 2304, 2304, 2304, 2304, 2304, 2816, 2816, 5632, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 15104, 15104, 15104, 15104, 16896, 18432, 18432, 18432, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 25344, 30208, 30208, 60672, 60672, 60672, 60672, 60672, 60672, 60672, 73728, 73728, 73728, 90624, 90624, 135936, 135936, 485376, 485376, 485376, 667392, 1941504, 1941504, 1941504, 3579648, 3579648]
     * 207 has cycles by length: [256, 512, 1792, 2304, 2304, 3584, 4096, 4096, 4096, 4352, 4608, 4608, 8704, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 17664, 32768, 32768, 32768, 35328, 36864, 36864, 36864, 36864, 36864, 36864, 52480, 86016, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 98304, 208896, 282624, 282624, 282624, 367360, 472320, 472320, 839680, 892160, 3621120, 6717440]
     * 209 has cycles by length: [2048, 2048, 2048, 3840, 3840, 3840, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 11008, 11008, 11008, 48640, 48640, 48640, 61440, 63488, 119040, 176128, 210944, 210944, 341248, 389120, 389120, 791040, 1507840, 2267648, 5009920, 5009920]
     * 211 has cycles by length: [768, 768, 768, 768, 768, 1280, 1280, 1536, 2560, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3840, 5120, 5120, 5120, 5120, 5120, 5376, 5376, 6144, 6144, 6144, 8960, 8960, 10752, 12288, 15360, 16128, 16128, 16128, 19968, 19968, 19968, 20480, 26112, 28672, 28672, 30720, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 35840, 35840, 43008, 43008, 43520, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 60928, 60928, 80640, 99840, 139776, 139776, 239616, 239616, 239616, 239616, 239616, 239616, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 419328, 479232, 479232, 599040, 599040, 1018368, 1018368]
     * 213 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 7168, 22016, 22016, 41472, 43520, 44032, 44032, 56320, 56320, 82944, 87040, 88064, 88064, 128000, 145152, 152320, 154112, 165888, 174080, 478720, 478720, 478720, 478720, 478720, 544000, 544000, 544000, 544000, 544000, 1210880, 1210880, 2280960, 2592000, 2752000]
     * 215 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 1792, 1792, 2048, 2048, 2048, 2048, 3584, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 5632, 5632, 5632, 5632, 5632, 5632, 6912, 6912, 13824, 19712, 22528, 22528, 22528, 28672, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 38400, 38400, 39424, 53760, 53760, 53760, 53760, 76032, 110592, 134400, 152064, 153600, 172800, 172800, 172800, 229376, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 268800, 430080, 430080, 591360, 591360, 591360, 884736, 3440640, 3440640]
     * 217 has cycles by length: [512, 512, 1024, 1024, 1024, 1024, 1536, 1536, 1792, 1792, 1792, 1792, 2560, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 5120, 5120, 5376, 5376, 5376, 5376, 8960, 8960, 14336, 14336, 20992, 20992, 21504, 21504, 23040, 24064, 24064, 43008, 43008, 43008, 43008, 43008, 43008, 46080, 46080, 47616, 47616, 62976, 62976, 71680, 72192, 72192, 80640, 80640, 95232, 95232, 95232, 95232, 104960, 120320, 125952, 125952, 144384, 144384, 333312, 333312, 645120, 944640, 1082880, 1333248, 1333248, 1952256, 1952256, 2237952, 2237952]
     * 219 has cycles by length: [256, 512, 768, 1024, 1024, 1024, 1536, 5632, 5632, 5632, 7936, 13312, 13312, 13312, 17408, 17408, 17408, 17408, 17408, 19456, 19456, 19456, 19456, 19456, 19968, 19968, 19968, 19968, 23808, 31744, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 39936, 44544, 44544, 44544, 52224, 58368, 174592, 191488, 191488, 214016, 214016, 219648, 219648, 226304, 226304, 226304, 226304, 252928, 252928, 252928, 252928, 412672, 579072, 579072, 579072, 579072, 579072, 579072, 1380864, 1514496, 1514496, 1692672, 1692672]
     * 221 has cycles by length: [1024, 1024, 1024, 1280, 1280, 1280, 1280, 1280, 1280, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 3072, 3072, 3072, 3072, 3072, 3840, 3840, 5120, 5120, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 8960, 8960, 8960, 9216, 9216, 15360, 15360, 17408, 17408, 17408, 17408, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 21504, 21504, 21504, 21504, 21504, 23040, 23040, 26880, 32768, 32768, 32768, 32768, 35840, 35840, 45824, 45824, 45824, 64512, 64512, 87040, 87040, 107520, 121856, 121856, 121856, 121856, 137472, 161280, 163840, 163840, 229376, 229376, 229376, 229376, 458240, 549888, 609280, 641536, 824832, 1146880, 3116032, 5865472]
     * 223 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2304, 2304, 2304, 3072, 3072, 3072, 3072, 3072, 4608, 6144, 6144, 6144, 6144, 6144, 8192, 8192, 8192, 8192, 8192, 8192, 9216, 9216, 9216, 9216, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 12288, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 24576, 24576, 24576, 24576, 24576, 44032, 44032, 44032, 44032, 64512, 79360, 88064, 88064, 88064, 88064, 119040, 129024, 158720, 258048, 258048, 258048, 352256, 352256, 352256, 352256, 396288, 476160, 516096, 528384, 528384, 528384, 528384, 2499840, 6824960]
     * 225 has cycles by length: [27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 138240, 189440, 525312, 525312, 691200, 691200, 691200, 719872, 719872, 1797120, 1797120, 2462720, 2462720, 2841600]
     * 227 has cycles by length: [24064, 24064, 24064, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 41472, 96256, 96256, 96256, 96256, 96256, 96256, 120320, 165888, 165888, 165888, 165888, 165888, 165888, 207360, 216576, 697856, 697856, 1202688, 1202688, 3778048, 6511104]
     * 229 has cycles by length: [768, 768, 768, 768, 768, 768, 768, 768, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 12544, 12544, 37632, 37632, 42240, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 148224, 337920, 506880, 506880, 506880, 506880, 506880, 689920, 1185792, 2420992, 8893440]
     * 231 has cycles by length: [5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 15360, 23808, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 31744, 32256, 32256, 32256, 60672, 64512, 64512, 66560, 79360, 80896, 103168, 161280, 161280, 202240, 225280, 225280, 225280, 225280, 262912, 396800, 419328, 806400, 806400, 1011200, 1396736, 2838528, 2838528, 3559424]
     * 233 has cycles by length: [512, 512, 1024, 1024, 1024, 1024, 1536, 1536, 5632, 5632, 7680, 7680, 39424, 56832, 65024, 65024, 130048, 130048, 130048, 130048, 195072, 195072, 715264, 715264, 975360, 975360, 5006848, 7217664]
     * 235 has cycles by length: [9728, 9728, 9728, 29184, 55808, 55808, 55808, 87552, 136192, 136192, 167424, 223744, 223744, 301568, 502272, 661504, 661504, 781312, 781312, 1283584, 1283584, 1730048, 3794944, 3794944]
     * 237 has cycles by length: [1536, 1536, 1536, 1536, 1536, 4352, 4352, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 8960, 10752, 10752, 10752, 13056, 26880, 26880, 26880, 26880, 26880, 26880, 26880, 26880, 50688, 50688, 50688, 50688, 50688, 53760, 72192, 90624, 91392, 136704, 152320, 204544, 256768, 354816, 354816, 354816, 387328, 421120, 528640, 797440, 1774080, 2382336, 2990592, 4511232]
     * 239 has cycles by length: [54784, 76288, 794368, 1106176, 1287424, 1792768, 4875776, 6789632]
     * 241 has cycles by length: [768, 768, 768, 768, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 6144, 7168, 7168, 8192, 8192, 8192, 8192, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 19456, 19456, 24576, 26880, 26880, 26880, 29184, 42240, 43008, 43008, 52992, 52992, 52992, 52992, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 56320, 107520, 172032, 172032, 204288, 204288, 423936, 591360, 741888, 1695744, 1854720, 1854720, 1854720, 2013696, 2914560]
     * 243 has cycles by length: [768, 768, 1536, 1536, 1536, 1536, 1536, 1536, 2816, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 10752, 10752, 16896, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 36096, 36096, 39424, 39936, 39936, 39936, 39936, 39936, 39936, 43008, 43008, 47360, 53760, 53760, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 90112, 122880, 122880, 132352, 199680, 199680, 199680, 199680, 199680, 199680, 284160, 288768, 360960, 439296, 663040, 1515520, 2225920, 7388160]
     * 245 has cycles by length: [256, 512, 1792, 2304, 2560, 3584, 3840, 5120, 11264, 13568, 16128, 17920, 26880, 27136, 35840, 39680, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 49920, 78848, 99840, 99840, 99840, 99840, 99840, 99840, 122112, 135680, 149760, 149760, 149760, 199680, 199680, 199680, 199680, 199680, 203520, 271360, 277760, 596992, 1547520, 1547520, 1547520, 1547520, 1547520, 2103040, 2196480]
     * 247 has cycles by length: [7168, 7168, 7168, 7168, 7168, 7168, 21504, 21504, 21504, 21504, 28672, 28672, 28672, 28672, 35840, 35840, 35840, 35840, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 58368, 64512, 175104, 175104, 175104, 233472, 233472, 233472, 233472, 291840, 291840, 291840, 291840, 1383424, 11265024]
     * 249 has cycles by length: [512, 512, 768, 768, 768, 768, 768, 1536, 5120, 5120, 9216, 9216, 9216, 9216, 9216, 15360, 16384, 16384, 25344, 25344, 25344, 28160, 33536, 33536, 42240, 49152, 49664, 50688, 56320, 56320, 56320, 56320, 56320, 74496, 100608, 101376, 101376, 101376, 101376, 101376, 101376, 101376, 101376, 101376, 496640, 506880, 506880, 893952, 901120, 1589248, 1622016, 1844480, 3252992, 3320064]
     * 251 has cycles by length: [1280, 2560, 2560, 3840, 4352, 8704, 8704, 13056, 17152, 34304, 34304, 40960, 42752, 51456, 85504, 85504, 128256, 139264, 276480, 548864, 940032, 1368064, 3704832, 9234432]
     * 253 has cycles by length: [256, 512, 768, 1280, 1536, 1536, 1536, 1536, 2560, 3072, 3072, 3072, 4096, 4096, 4096, 12032, 20736, 20736, 20736, 20736, 24064, 41472, 41472, 41472, 42496, 42496, 42496, 44032, 82944, 82944, 82944, 103680, 132096, 132096, 132096, 132096, 132096, 132096, 132096, 176128, 176128, 176128, 176128, 220160, 331776, 974592, 2069504, 3442176, 3654656, 3654656]
     * 255 has cycles by length: [256, 768, 2304, 2304, 2304, 2304, 3328, 3584, 3840, 6400, 9984, 11520, 11520, 11520, 11776, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 16128, 22784, 32256, 46592, 49920, 57600, 59648, 83200, 105984, 153088, 178944, 205056, 209664, 296192, 835072, 894720, 1491200, 2743808, 3757824, 5308672]
     * <br>
     * TECHNIQUE 3:
     * <br>
     * 3 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728]
     * 5 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760]
     * 7 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296]
     * 9 has cycles by length: [17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080]
     * 11 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848]
     * 13 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952]
     * 15 has cycles by length: [2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856]
     * 17 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832]
     * 19 has cycles by length: [5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440]
     * 21 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224]
     * 23 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712]
     * 25 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800]
     * 27 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456]
     * 29 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568]
     * 31 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856]
     * 33 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560]
     * 35 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856]
     * 37 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640]
     * 39 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248]
     * 41 has cycles by length: [3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000]
     * 43 has cycles by length: [3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712]
     * 45 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384]
     * 47 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104]
     * 49 has cycles by length: [10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832]
     * 51 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120]
     * 53 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400]
     * 55 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208]
     * 57 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640]
     * 59 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744]
     * 61 has cycles by length: [2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616]
     * 63 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800]
     * 65 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896]
     * 67 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008]
     * 69 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488]
     * 71 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840]
     * 73 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808]
     * 75 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792]
     * 77 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720]
     * 79 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648]
     * 81 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080]
     * 83 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976]
     * 85 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080]
     * 87 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464]
     * 89 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776]
     * 91 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072]
     * 93 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984]
     * 95 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904]
     * 97 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392]
     * 99 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072]
     * 101 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640]
     * 103 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696]
     * 105 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152]
     * 107 has cycles by length: [8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608]
     * 109 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528]
     * 111 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632]
     * 113 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616]
     * 115 has cycles by length: [37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184]
     * 117 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168]
     * 119 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544]
     * 121 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696]
     * 123 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976]
     * 125 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376]
     * 127 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016]
     * 129 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 24064, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016, 86016]
     * 131 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376, 37376]
     * 133 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976, 126976]
     * 135 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696, 93696]
     * 137 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544, 108544]
     * 139 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 24576, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168, 71168]
     * 141 has cycles by length: [37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 37888, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184, 93184]
     * 143 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 42496, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616, 47616]
     * 145 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 20992, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 25600, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632, 69632]
     * 147 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528, 86528]
     * 149 has cycles by length: [8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608, 68608]
     * 151 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 14336, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152, 113152]
     * 153 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 18432, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29184, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696, 29696]
     * 155 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640, 112640]
     * 157 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 60416, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072]
     * 159 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392]
     * 161 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 38400, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904, 59904]
     * 163 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984, 105984]
     * 165 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 45568, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072, 67072]
     * 167 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776]
     * 169 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 6144, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 28672, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464, 62464]
     * 171 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080]
     * 173 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976, 62976]
     * 175 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080, 110080]
     * 177 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 31232, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648, 91648]
     * 179 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720, 94720]
     * 181 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792, 97792]
     * 183 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808, 119808]
     * 185 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 13824, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840, 99840]
     * 187 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 59392, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488]
     * 189 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008, 107008]
     * 191 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896, 80896]
     * 193 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 10752, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 32768, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800]
     * 195 has cycles by length: [2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 7680, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616, 111616]
     * 197 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744, 95744]
     * 199 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 11264, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 12800, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 23040, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 31744, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640]
     * 201 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 14848, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208, 94208]
     * 203 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 20480, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400, 102400]
     * 205 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 19456, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 30720, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120, 69120]
     * 207 has cycles by length: [10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832]
     * 209 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104, 111104]
     * 211 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 11776, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 30208, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384, 80384]
     * 213 has cycles by length: [3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712, 115712]
     * 215 has cycles by length: [3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000, 128000]
     * 217 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 9216, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248, 117248]
     * 219 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 21504, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640, 48640]
     * 221 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856, 121856]
     * 223 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 49664, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560, 66560]
     * 225 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 22528, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856]
     * 227 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 3584, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568, 109568]
     * 229 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 15872, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456, 83456]
     * 231 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 9728, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 35840, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800, 76800]
     * 233 has cycles by length: [1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 3072, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 23552, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 27648, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712, 51712]
     * 235 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 4608, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 8704, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224, 116224]
     * 237 has cycles by length: [5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 46592, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440, 61440]
     * 239 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 5120, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832]
     * 241 has cycles by length: [2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 2048, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 17408, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 25088, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856, 57856]
     * 243 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952, 125952]
     * 245 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 5632, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848, 78848]
     * 247 has cycles by length: [17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 17920, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 32256, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 34816, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080, 46080]
     * 249 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 22016, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 53248, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296, 55296]
     * 251 has cycles by length: [2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 2560, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 7168, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 13312, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 15360, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 38912, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760, 53760]
     * 253 has cycles by length: [512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 512, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 19968, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 26112, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728, 73728]
     * 255 has cycles by length: [1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 1536, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 6656, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 10240, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 16896, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 26368, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008, 43008]
     */
    @Test
    public void testRomuTrioBytesAllMultipliers() {
        final int bytes3 = 1 << 24;
        IntIntMap periods = new IntIntMap(bytes3);
        IntList seq = new IntList(bytes3), distinctCycles = new IntList(256);
        for (int mul = 3; mul < 256; mul += 2) {
            periods.clear();
            seq.clear();
            distinctCycles.clear();
            int longestCycle = -1;
            // omits the all 0 state, which we already know has period 1, and already forbid
            for (int i = 1; i < bytes3; i++) {
                if (periods.containsKey(i)) continue;
                seq.clear();
                int period = 0, state = i;
                do {
                    seq.add(state);
                    int stateA = state & 255;
                    int stateB = state >>> 8 & 255;
                    int stateC = state >>> 16 & 255;

                    // RomuTrio, shrunk down to 8-bit words
//                    int fa = stateA;
//                    stateA = mul * stateC  & 255;
//                    stateC = stateC - stateB & 255;
//                    stateB = stateB - fa & 255;
//                    stateB = rotate8(stateB, 2);
//                    stateC = rotate8(stateC, 5);

                    // TECHNIQUE 1
//                    int fa = stateA;
//                    int fb = stateB;
//                    int fc = stateC;
//                    stateA = fa + mul & 255;
//                    stateC = rotate8(fc, 5) - fa & 255;
//                    stateB = rotate8(fb, 2) + fc & 255;

                    // TECHNIQUE 2
//                    int fa = stateA;
//                    int fb = stateB;
//                    int fc = stateC;
//                    stateA = fa + mul & 255;
//                    stateC = rotate8(fb, 5) + fa & 255;
//                    stateB = rotate8(fa, 2) + fc & 255;

                    // TECHNIQUE 3
                    int fa = stateA;
                    int fb = stateB;
                    int fc = stateC;
                    stateA = fa + mul & 255;
                    stateC = (rotate8(fc, 5) ^ fb) & 255;
                    stateB = (rotate8(fb, 2) + fa) & 255;

                    state = stateA | stateB << 8 | stateC << 16;
                    period++;
                } while (state != i);
                for (int j = 0, n = seq.size(); j < n; j++) {
                    periods.put(seq.get(j), period);
                }
                distinctCycles.add(period);
                longestCycle = Math.max(longestCycle, period);
//                if (period == 1)
//                    System.out.println(mul + " HAS SINGULAR CYCLE: " + seq.toString(", ", false, Base.BASE16::appendUnsigned));
//                else if(period < 50)
//                    System.out.println(mul + " HAS SHORT CYCLE: " + seq.toString(", ", false, Base.BASE16::appendUnsigned));
            }
            distinctCycles.sort();
            System.out.println(mul + " has cycles by length: " + distinctCycles);
//            System.out.println();
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
            y = (stateB = (byte)(stateB + (clz8(x))));
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


    @Test
    public void testRotationAddVariants()
    {
        int[] counts = new int[65536];
        int m = 0, n = 0;
        for (int a = 1; a <= 65536; a++) {
//          Completed 63232 iterations before repeating
//            m = m + 7 & 0xFF;
//            n = (m + rotate8(n, 1)) & 0xFF;
//          Completed 8192 iterations before repeating
//            m = m + 1 & 0xFF;
//            n = (m + rotate8(n, 1)) & 0xFF;
//          Completed 512 iterations before repeating
//            m = m + 1 & 0xFF;
//            n = (n + rotate8(m, 1)) & 0xFF;
//          Completed 256 iterations before repeating
//            m = m + 1 & 0xFF;
//            n = (clz8(n) + rotate8(m, 1)) & 0xFF;
//          Completed 65536 iterations before repeating
//          Each state occurs exactly once.
//          If you reduce the tested duration to half (32768 iterations), low results are drastically less common.
//            m = m + 1 & 0xFF;
//            n = (n + clz8(m)) & 0xFF;
//          Completed 65536 iterations before repeating
//          Each state occurs exactly once.
//          If you reduce the tested duration to half (32768 iterations), the first quarter of results are always
//          more common than what should be the average... None are "bad bets."
//            m = m + 0x95 & 0xFF;
//            n = (n + clz8(m)) & 0xFF;
//          Completed 65536 iterations before repeating, equidistributed
//          Each state occurs exactly once.
//          If you reduce the tested duration to half (32768 iterations), frequent and infrequent results are mixed.
//            m = m + 1 & 0xFF;
//            n = (rotate8(n, 1) + clz8(m)) & 0xFF;
//          Completed 8192 iterations before repeating
//            m = m + 1 & 0xFF;
//            n = (rotate8(n, 4) + clz8(m)) & 0xFF;
//          Completed 65536 iterations before repeating, equidistributed
//          Each state occurs exactly once.
//            m = m + 1 & 0xFF;
//            n = (rotate8(n, 7) + clz8(m)) & 0xFF;
//          Completed 32768 iterations before repeating, not even close to equidistributed
            // no state occurs more than once (reversible state transition implies this also).
//            m = m + 1 & 0xFF;
//            n = (rotate8(n, 2) + clz8(m)) & 0xFF;
//          Completed 45568 iterations before repeating, not even close to equidistributed
//            m = m + 1 & 0xFF;
//            n = (rotate8(n, 6) + clz8(m)) & 0xFF;
//          Not a bijection, and wildly varies between not producing some numbers and producing others a lot
//            m = m + 1 & 0xFF;
//            n = n + (rotate8(n, 1) | m) & 0xFF;
//          Completed 1024 iterations before repeating
//          Actually can be inverted, but not full-period; seems equidistributed though.
//            m = m + 5 & 0xFF;
//            n = n + (rotate8(m, 7) | m) & 0xFF;
//          Completed 1024 iterations before repeating
//          Actually can be inverted, but not full-period; seems equidistributed though.
//            m = m + 5 & 0xFF;
//            n = n + (rotate8(m, 7) & m) & 0xFF;
//          Completed 512 iterations before repeating
//          Actually can be inverted, but not full-period; NOT equidistributed.
//            m = m + 5 & 0xFF;
//            n = n + (rotate8(m, 7) ^ m) & 0xFF;
//          Completed 512 iterations before repeating
//          Actually can be inverted, but not full-period; NOT equidistributed.
//            m = m + 5 & 0xFF;
//            n = n + (rotate8(m, 1) ^ m) & 0xFF;
//          Completed 1024 iterations before repeating
//          Actually can be inverted, but not full-period; seems equidistributed though.
//          Yes, that's mixing two state changes to n, one equidistributed and one not.
//            m = m + 5 & 0xFF;
//            n = n + (rotate8(m, 7) & m) & 0xFF;
//            n = n + (rotate8(m, 7) ^ m) & 0xFF;
//          Completed 256 iterations before repeating
//          Not at all equidistributed.
            m = m + 5 & 0xFF;
            n = n + (rotate8(m, 7) + m) & 0xFF;

//            int p = m << 8 | n;
//            counts[p]++;
            counts[n]++;
            if (m == 0 && n == 0) {
                System.out.println("Completed " + a + " iterations before repeating");
                break;
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

}
