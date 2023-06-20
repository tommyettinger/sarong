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
    public void test32BitPointHash()
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

    private int rotate8(int v, int amt) {
        return (v << (amt & 7) & 255) | ((v & 255) >>> (8 - amt & 7));
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
            b = b + (byte)((byte)a + ((a & 255) >>> 1) >>> 31) & 255;
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
        for (int j = 1; j < 0x10000; j += 2) { // when (j & 7) equals 5 or 7, this is full-period.
            sum.assign(0);
            all.clear();
            xor = state = 0;
            for (int i = 0; i < 0x10000; i++) {
                // This (multiplier & 3) must equal 3.
                // This includes all powers of two minus 1 that are greater than 2, and many LEA constants.
                state ^= (state * state | j);
                state *= -1;
                //state = (state ^ (state * state | o5o7)) * m3; // (o5o7 & 7) must equal 5 or 7, (m3 & 3) must equal 3.
                result = state;
                xor ^= result;
                sum.add(result);
                all.add(result & 0xFFFF);
                if (state == 0) {
                    if(i == 65535)
//                    if(i > 16383)
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
}
