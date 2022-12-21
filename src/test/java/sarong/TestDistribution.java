package sarong;

import org.huldra.math.BigInt;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.longlong.Roaring64NavigableMap;
import sarong.util.StringKit;

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
        //// 2011632872/4294967296 outputs were present.
        //// 53.16302236169577% of outputs were missing.
        for (; i < 0; i++) {
            all.add(hiXorLo(hiXorLo(0xa0b428db, i ^ 0x78bd642f), 0xb455d1e5));
        }
        for (; i >= 0; i++) {
            all.add(hiXorLo(hiXorLo(0xa0b428db, i ^ 0x78bd642f), 0xb455d1e5));
        }
        System.out.println(all.getLongCardinality() + "/" + 0x100000000L + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-32 + "% of outputs were missing.");
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
    public void testMWC8Bit()
    {
        short state = 1;
        short[] counts = new short[256];
        for (int i = 0; i < 0x7C7F; i++) {
            int s = (state = (short) (249 * (state & 0xFF) + (state >>> 8 & 0xFF))) & 0xFF;
            counts[s]++;
        }
        for (int y = 0, i = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                System.out.print(StringKit.hex(counts[i++]) + " ");
            }
            System.out.println();
        }
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
                result = (short)iphCoord(x, y, 123);
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
        //// testing this:
//  const P0 = 0xa0761d6478bd642f'u64
//  const P1 = 0xe7037ed1a0b428db'u64
//  const P5x8 = 0xeb44accab455d1e5'u64
//  Hash(hiXorLo(hiXorLo(P0, uint64(x) xor P1), P5x8))
        //// 2011632872/4294967296 outputs were present.
        //// 53.16302236169577% of outputs were missing.
        for (; i < 0; i++) {
            all.add(i ^ (i * i | 1));
        }
        for (; i >= 0; i++) {
            all.add(i ^ (i * i | 1));
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
}
