package sarong;

import org.huldra.math.BigInt;
import org.junit.Ignore;
import org.junit.Test;
import sarong.util.StringKit;

import java.math.BigInteger;

/**
 * Created by Tommy Ettinger on 9/1/2016.
 */
public class StrengthTest {
    //@Test
    public void testThunder() {
        ThunderRNG random = new ThunderRNG(); //0xABC7890456123DEFL
        long partA = random.getStatePartA(), partB = random.getStatePartB();
        System.out.println(partA + "," + partB);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x1000000);
        }
    }
    //@Test
    public void testPermuted()
    {
        PermutedRNG random = new PermutedRNG(); //0xABC7890456123DEFL
        long state = random.getState();
        System.out.println(state);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x1000000);
        }
    }
    //@Test
    public void testLight()
    {
        LightRNG random = new LightRNG(); //0xABC7890456123DEFL
        long state = random.getState();
        System.out.println(state);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x1000000);
        }
    }

    @Test
    @Ignore
    public void testLap()
    {
        LapRNG random = new LapRNG(); //0xABC7890456123DEFL
        System.out.println("LapRNG (testing nextLong): " + StringKit.hex(random.getState0()) + StringKit.hex(random.getState1()));
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextLong());
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("LAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    //@Test
    public void testLapInt()
    {
        LapRNG random = new LapRNG(); //0xABC7890456123DEFL
        System.out.println("LapRNG (testing nextInt): " + StringKit.hex(random.getState0()) + StringKit.hex(random.getState1()));
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("LAP INT: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }


    @Test
    public void testThrustAlt() {
        for (long i = -20L; i <= 20L; i++) {
            System.out.println(i + ": " + ThrustAltRNG.determineFloat(i) + ", " + ThrustAltRNG.determineDouble(i));
        }
    }

    @Test
    public void testThrust()
    {
        ThrustRNG random = new ThrustRNG(0xABC7890456123DEFL);
        System.out.println("ThrustRNG (testing nextLong): " + random.toString());
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("THRUST: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) >= 0.01) ? "!!!" : "");
        }
    }

//    @Test
//    public void testVortexStreams()
//    {
//        VortexRNG streamA = new VortexRNG(0xABD7890456123DEFL, 0);
//        VortexRNG streamB = new VortexRNG(0xABD7890456123DEFL, 1);
//        VortexRNG streamC = new VortexRNG(0xABD7890456123DEFL, 2);
//        System.out.println("VortexRNG (testing stream correlation)");
//        long[] bitsAB = new long[64], bitsAC = new long[64], bitsBC = new long[64];
//        long currA = (streamA.nextLong()), currB = (streamB.nextLong()), currC = (streamC.nextLong()), a, b, c;
//        long xorA = currA, xorB = currB, xorC = currC;
//        BigInt totalA = new BigInt(currA), totalB = new BigInt(currB), totalC = new BigInt(currC);
//        int bi;
//        for (long i = 0; i < 0x1000000L; i++) {
//            a = currA ^ (currA = (streamA.nextLong()));
//            b = currB ^ (currB = (streamB.nextLong()));
//            c = currC ^ (currC = (streamC.nextLong()));
//            xorA ^= currA;
//            xorB ^= currB;
//            xorC ^= currC;
//            totalA.add(currA);
//            totalB.add(currB);
//            totalC.add(currC);
//            bi = 63;
//            for (long bit = 0x8000000000000000L; bit != 0; bit >>>= 1, bi--) {
//                bitsAB[bi] += (a & b & bit) >>> bi;
//                bitsAC[bi] += (a & c & bit) >>> bi;
//                bitsBC[bi] += (b & c & bit) >>> bi;
//            }
//        }
//        System.out.println("VORTEX: Out of 0x1000000 random numbers,");
//        System.out.println("Stream 0 stats: bit xor " + StringKit.bin(xorA) + " ; binary " + totalA.toReversedBinaryString() + " ; total " + totalA.toString());
//        System.out.println("Stream 1 stats: bit xor " + StringKit.bin(xorB) + " ; binary " + totalB.toReversedBinaryString() + " ; total " + totalB.toString());
//        System.out.println("Stream 2 stats: bit xor " + StringKit.bin(xorC) + " ; binary " + totalC.toReversedBinaryString() + " ; total " + totalC.toString());
//        System.out.println("each bit changes in both of two streams this often relative to 0.0 probability...");
//        for (int i = 0; i < 64; i++) {
//            System.out.printf("With stream 0 and stream 1: %02d : % .24f\n", i, bitsAB[i] / 0x1p24);
//        }
//        for (int i = 0; i < 64; i++) {
//            System.out.printf("With stream 0 and stream 2: %02d : % .24f\n", i, bitsAC[i] / 0x1p24);
//        }
//        for (int i = 0; i < 64; i++) {
//            System.out.printf("With stream 1 and stream 2: %02d : % .24f\n", i, bitsBC[i] / 0x1p24);
//        }
//    }

    @Test
    public void testVortexStreamsAll64()
    {
        testVortexStreamsLowN(64);
    }

    @Test
    public void testVortexStreamsLow32()
    {
        testVortexStreamsLowN(32);
    }

    @Test
    public void testVortexStreamsLow16()
    {
        testVortexStreamsLowN(16);
    }

    @Test
    public void testVortexStreamsLow8()
    {
        testVortexStreamsLowN(8);
    }

    public void testVortexStreamsLowN(final int n)
    {
        VortexRNG streamA = new VortexRNG(0xABD7890456123DEFL, 0);
        VortexRNG streamB = new VortexRNG(0xABD7890456123DEFL, 1);
        VortexRNG streamC = new VortexRNG(0xABD7890456123DEFL, 2);
        System.out.println("VortexRNG (testing stream correlation, low " + n + ")");
        long[] bitsAB = new long[n], bitsAC = new long[n], bitsBC = new long[n];
        long currA = (streamA.nextLong() & (-1L >>> n)), currB = (streamB.nextLong() & (-1L >>> n)), currC = (streamC.nextLong() & (-1L >>> n)), a, b, c;
        long xorA = currA, xorB = currB, xorC = currC;
        BigInt totalA = new BigInt(currA), totalB = new BigInt(currB), totalC = new BigInt(currC);
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            a = currA ^ (currA = (streamA.nextLong() & (-1L >>> n)));
            b = currB ^ (currB = (streamB.nextLong() & (-1L >>> n)));
            c = currC ^ (currC = (streamC.nextLong() & (-1L >>> n)));
            xorA ^= currA;
            xorB ^= currB;
            xorC ^= currC;
            totalA.add(currA);
            totalB.add(currB);
            totalC.add(currC);
            bi = n - 1;
            for (long bit = (1L << bi); bit != 0; bit >>>= 1, bi--) {
                bitsAB[bi] += (a & b & bit) >>> bi;
                bitsAC[bi] += (a & c & bit) >>> bi;
                bitsBC[bi] += (b & c & bit) >>> bi;
            }
        }
        System.out.println("VORTEX: Out of 0x1000000 random numbers,");
        System.out.println("Stream 0 stats: bit xor " + StringKit.bin(xorA).substring(64 - n) + " ; binary " + totalA.toReversedBinaryString() + " ; total " + totalA.toString());
        System.out.println("Stream 1 stats: bit xor " + StringKit.bin(xorB).substring(64 - n) + " ; binary " + totalB.toReversedBinaryString() + " ; total " + totalB.toString());
        System.out.println("Stream 2 stats: bit xor " + StringKit.bin(xorC).substring(64 - n) + " ; binary " + totalC.toReversedBinaryString() + " ; total " + totalC.toString());
        System.out.println("each bit changes in both of two streams this often relative to 0.0 probability...");
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 0 and stream 1: %02d : % .24f\n", i, bitsAB[i] / 0x1p24);
        }
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 0 and stream 2: %02d : % .24f\n", i, bitsAC[i] / 0x1p24);
        }
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 1 and stream 2: %02d : % .24f\n", i, bitsBC[i] / 0x1p24);
        }
    }

    @Test
    public void testMeshStreamsAll64()
    {
        testMeshStreamsLowN(64);
    }

    @Test
    public void testMeshStreamsLow32()
    {
        testMeshStreamsLowN(32);
    }

    @Test
    public void testMeshStreamsLow16()
    {
        testMeshStreamsLowN(16);
    }

    @Test
    public void testMeshStreamsLow8()
    {
        testMeshStreamsLowN(8);
    }

    public void testMeshStreamsLowN(final int n)
    {
        //9876543212345L
        MeshRNG streamA = new MeshRNG(0xABD7890456123DEFL, 0L, 0L);
        MeshRNG streamB = new MeshRNG(0xABD7890456123DEFL, 0L, 1L);
        MeshRNG streamC = new MeshRNG(0xABD7890456123DEFL, 45L, 0L);
        System.out.println("MeshRNG (testing stream correlation, low " + n + ")");
        long[] bitsAB = new long[n], bitsAC = new long[n], bitsBC = new long[n];
        long currA = (streamA.nextLong() & (-1L >>> n)), currB = (streamB.nextLong() & (-1L >>> n)), currC = (streamC.nextLong() & (-1L >>> n)), a, b, c;
        long xorA = currA, xorB = currB, xorC = currC;
        BigInt totalA = new BigInt(currA), totalB = new BigInt(currB), totalC = new BigInt(currC);
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            a = currA ^ (currA = (streamA.nextLong() & (-1L >>> n)));
            b = currB ^ (currB = (streamB.nextLong() & (-1L >>> n)));
            c = currC ^ (currC = (streamC.nextLong() & (-1L >>> n)));
            xorA ^= currA;
            xorB ^= currB;
            xorC ^= currC;
            totalA.add(currA);
            totalB.add(currB);
            totalC.add(currC);
            bi = n - 1;
            for (long bit = (1L << bi); bit != 0; bit >>>= 1, bi--) {
                bitsAB[bi] += (a & b & bit) >>> bi;
                bitsAC[bi] += (a & c & bit) >>> bi;
                bitsBC[bi] += (b & c & bit) >>> bi;
            }
        }
        System.out.println("MESH: Out of 0x1000000 random numbers,");
        System.out.println("Stream 0 stats: bit xor " + StringKit.bin(xorA).substring(64 - n) + " ; binary " + totalA.toReversedBinaryString() + " ; total " + totalA.toString());
        System.out.println("Stream 1 stats: bit xor " + StringKit.bin(xorB).substring(64 - n) + " ; binary " + totalB.toReversedBinaryString() + " ; total " + totalB.toString());
        System.out.println("Stream 2 stats: bit xor " + StringKit.bin(xorC).substring(64 - n) + " ; binary " + totalC.toReversedBinaryString() + " ; total " + totalC.toString());
        System.out.println("each bit changes in both of two streams this often relative to 0.0 probability...");
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 0 and stream 1: %02d : % .24f\n", i, bitsAB[i] / 0x1p24);
        }
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 0 and stream 2: %02d : % .24f\n", i, bitsAC[i] / 0x1p24);
        }
        for (int i = 0; i < n; i++) {
            System.out.printf("With stream 1 and stream 2: %02d : % .24f\n", i, bitsBC[i] / 0x1p24);
        }
    }


    //@Test
    public void numberTest()
    {
        int run = 0, total = 0;
        long grand = 0;
        for (; ;) {
            total += (total ^ (run += 0xC6BC278D));
            if(total == 0 && run == 0)
                break;
            else
                grand++;
        }
        System.out.printf("%08X in %X with run %08X", total, grand, run);
    }
    private static class Tracker
    {
        public int total = 0;
        public int alpha = 0, beta = 0x9E3779B9;//, gamma = 0, delta = 0;
        //public int inc = 0x9E3779B9, inc2 = 0x632BE5AB;
        public int next()
        {
            int z = (alpha += (alpha == 0) ? (beta += 0x632BE5AC) : beta);
            z = (z ^ (z >>> 16)) * 0x85EBCA6B;
            z = (z ^ (z >>> 13)) * 0xC2B2AE35;
            total += (z ^= (z >>> 16));
            return z;
        }
    }
    //0x85157AF5 0x632BE5AB 0x9296FE97  0x108EF2D9   all prime

    private static BigInteger lcm(long... values)
    {
        BigInteger result = BigInteger.valueOf(values[0]), tmp;
        for (int i = 1; i < values.length; i++) {
            tmp = BigInteger.valueOf(values[i]);
            result = tmp.divide(result.gcd(tmp)).multiply(result);
        }
        return result;
    }

    //@Test
    public void testBuckets()
    {
        final int[] primeStuff = {
                43, 5,
                47, 5,
                53, 5,
                59, 5,
                61, 5,
                67, 6,
                71, 6,
                73, 6,
                79, 6,
                83, 6,
                89, 6,
                97, 6,
                101, 6,
                103, 6,
                107, 6,
                109, 6,
                113, 6,
                127, 6,
                131, 7,
                137, 7,
                139, 7,
                149, 7,
                151, 7,
                157, 7,
                163, 7,
                167, 7,
                173, 7,
                179, 7,
                181, 7,
                191, 7,
                193, 7,
                197, 7,
                199, 7,
                211, 7,
                223, 7,
                227, 7,
                229, 7,
                233, 7,
                239, 7,
                241, 7,
                251, 7,
                257, 8,
                263, 8,
                269, 8,
                271, 8,
                277, 8,
                281, 8,
                283, 8,
                293, 8,
                307, 8,
                311, 8,
                313, 8,
                317, 8,
                331, 8,
                337, 8,
                347, 8,
                349, 8,
                353, 8,
                359, 8,
                367, 8,
                373, 8,
                379, 8,
                383, 8,
                389, 8,
                397, 8,
                401, 8,
                409, 8,
                419, 8,
                421, 8,
                431, 8,
                433, 8,
                439, 8,
                443, 8,
                449, 8,
                457, 8,
                461, 8,
                463, 8,
                467, 8,
                479, 8,
                487, 8,
                491, 8,
                499, 8,
                503, 8,
                509, 8,
                521, 9,
                523, 9,
                541, 9,
        };
        long[] buckets = new long[16];
        int ctr, optimalPrime = 43;
        BigInteger l, best = BigInteger.ZERO;
        for (int s = 0; s < 83; s++) {
            int p = primeStuff[s << 1];
            buckets[0] = 0L;
            buckets[1] = 0L;
            buckets[2] = 0L;
            buckets[3] = 0L;
            buckets[4] = 0L;
            buckets[5] = 0L;
            buckets[6] = 0L;
            buckets[7] = 0L;
            buckets[8] = 0L;
            buckets[9] = 0L;
            buckets[10] = 0L;
            buckets[11] = 0L;
            buckets[12] = 0L;
            buckets[13] = 0L;
            buckets[14] = 0L;
            buckets[15] = 0L;
            ctr = 0;
            for (long i = 0; i < 0x100000000L; i++) {
                buckets[(++ctr >>> 1) * p >>> 28]++;
            }
            best = best.max(l = lcm(buckets));
            if (best.equals(l)) {
                optimalPrime = p;
                System.out.printf("%d: %s (%d bits)\n", p, best.toString(16), best.bitLength());
                for (int i = 0; i < buckets.length; i++) {
                    System.out.printf("Bucket %03d has 0x%09X hits\n", i, buckets[i]);
                }

            }
        }
        String big = best.toString(16);
        int bits = best.bitLength();
        System.out.println("Optimal prime is " + optimalPrime);
        System.out.println("Highest LCM is " + bits + " bits long, " + big.length() + " hex digits long:\n" + big);
        System.out.println("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"
                + "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"
                + "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"
                + "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
    }

    //@Test
    public void dummyTest()
    {
        {
            Tracker track = new Tracker();
            //System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
//            int tetra = 0, tetra2 = 0;
//            //for (byte inc : new byte[]{3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79,
//            //        83, 89, 97, 101, 103, 107, 109, 113, 127, -107, -89, -83, -59, -29, -23, -17, -5}) {
//            for(byte inc = -127; inc < 127; inc+=2) {
//                //for (byte inc2 = -127; inc2 < 127; inc2+=2) {
//                    //for (byte inc : new byte[]{-128, 100, 23, 43}){
//                    track.inc = inc;
//                    //track.inc2 = inc2;
//                    track.total = 0;
//                    track.alpha = 0;
//                    track.beta = 0;
//                    track.gamma = 0;
//                    track.delta = 0;
//                    for (long i = 0; i < 0x10000L; i++) {
//                        track.next();
//                    }
//                System.out.println("Increment " + inc + " has total " + track.total);
//                    tetra = (track.next() & 0xFF) << 24 | (track.next() & 0xFF) << 16 | (track.next() & 0xFF) << 8 | (track.next() & 0xFF);
//                    //System.out.printf("Searching for %08X\n", tetra);
//                    tetra2 = tetra;
//                    for (long i = 0L; i < 0x10000L; i++) {
//                        if ((tetra2 = (tetra2 << 8) | (track.next() & 0xFF)) == tetra) {
//                            System.out.println("Increment " + inc /* + ", " + inc2 */ + " has period " + i + ", total is "
//                                    + (track.total - (byte) (tetra >>> 24) - (byte) (tetra >>> 16 & 0xFF)
//                                    - (byte) (tetra >>> 8 & 0xFF) - (byte) (tetra & 0xFF)));
//                            break;
//                        }
//                    }
//                //}
//            }
            for (long i = 0; i < 0x100000000L; i++) {
                track.next();
            }
            int wrap = track.next(), wrap2 = track.next(),
                    wrap3 = track.next(), wrap4 = track.next(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0, loops = 0;
            System.out.printf("Looking for 0x%08X, 0x%08X, 0x%08X, 0x%08X\n", wrap, wrap2, wrap3, wrap4);
            track.total = 0;
            for (long m = 0; m < 0x2000000000L; m++) {
                if (bonus == (bonus = track.next()))
                {
                    if(bonus == track.next())
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": STRIKE 1, " + bonus2 + ", " + (bonus2 = track.next()));
//                        m++;
//                        bonus2 = track.next();
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": STRIKE 2, "  + bonus3 + ", " + (bonus3 = track.next()));
//                            m++;
//                            bonus3 = track.next();
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": STRIKE 3, " + bonus4 + ", " +  (bonus4 = track.next()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!! " + StringKit.hex(track.total));
                                    ++loops;
                                }
                            }
                        }
                    }
                }
            }



            System.out.println("DONE! total: " + StringKit.hex(track.total) + " in " + loops + " loops with next 4 random values: "
                    + track.next() + "  " + track.next() + "  "
                    + track.next() + "  " + track.next());
        }

//        {
//            BirdRNG r = new BirdRNG(0); //-1999262892926553691L
//            System.out.println(r);
//            System.out.println();
//            for (int i = 0; i < 256; i++) {
//                System.out.println(StringKit.hex(r.nextInt()));
//            }
//            r.setState(r.nextLong());
//            System.out.println();
//            System.out.println(r);
//            System.out.println();
//
//            int wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
//                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
//            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
//
//            for (long m = 1; m <= 0x100000104L; m++) {
//                if (bonus == (bonus = r.nextInt()))
//                {
//                    if(bonus == r.nextInt())
//                        System.out.println("BAD. " + StringKit.hex(m));
//                }
//                else {
//                    if (wrap == bonus) {
//                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
//                        if (wrap2 == bonus2) {
//                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
//                            if (wrap3 == bonus3) {
//                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
//                                if (wrap4 == bonus4) {
//
//                                    System.out.println(StringKit.hex(m) + "!!!");
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
//                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");
//        }
        /*{
            int state = 1234560;
            System.out.println("STARTING LFSR AT: " + state);
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(state = LFSR.determinePositiveInt(state)));
            }
            state = 1234560;
            System.out.println();
            System.out.println();

            int wrap = (state = LFSR.determinePositiveInt(state)),
                    wrap2 = (state = LFSR.determinePositiveInt(state)),
                    wrap3 = (state = LFSR.determinePositiveInt(state)),
                    wrap4 = (state = LFSR.determinePositiveInt(state)),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");

            for (long m = 1; m <= 0x100000104L; m++) {
                if(state <= 0)
                    System.out.println("NOT CORRECT! " + m);
                if (bonus == (bonus = (state = LFSR.determinePositiveInt(state))))
                {
                    if(bonus == (state = LFSR.determinePositiveInt(state)))
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = (state = LFSR.determinePositiveInt(state))));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = (state = LFSR.determinePositiveInt(state))));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = (state = LFSR.determinePositiveInt(state))));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + state + "  with next 4 random values: "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  ");
        }*/
        /*{
            HordeRNG r = new HordeRNG(); //-1999262892926553691L
            System.out.println();
            System.out.println(StringKit.hex(r.state)
                    + " with " + StringKit.hex(r.choice));
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(r.nextInt()));
            }
            r.setState(r.nextLong());
            System.out.println();
            System.out.println(r);
            System.out.println();
            long wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
            for (long m = 1; m <= 0x100000104L; m++) {
                if (bonus == (bonus = r.nextInt()))
                {
                    if(bonus == r.nextInt())
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");
        }*/
        /*{
            HerdRNG r = new HerdRNG(new int[16]); //-1999262892926553691L
            System.out.println();
            System.out.println(StringKit.hex(r.state)
                    + " with " + StringKit.hex(r.choice));
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(r.nextInt()));
            }
            r = new HerdRNG(new int[16]);
            System.out.println();
            System.out.println(r);
            System.out.println();
            long wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
            for (long m = 1; m <= 0x100000104L; m++) {
                if (bonus == (bonus = r.nextInt()))
                {
                    if(bonus == r.nextInt())
                        System.out.println("BAD. Position " + StringKit.hex(m) + " and value " + bonus + ", r is " + r);
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");

        }*/

    }
}
