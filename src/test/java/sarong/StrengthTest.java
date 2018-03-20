package sarong;

import org.huldra.math.BigInt;
import org.junit.Test;
import sarong.util.StringKit;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

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


    //@Test
    public void testFlap()
    {
        FlapRNG random = new FlapRNG(); //0xABC7890456123DEFL
        System.out.println(StringKit.hex(random.state0) + StringKit.hex(random.state1));
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
        System.out.println("FLAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }


    //@Test
    public void testSlap()
    {
        SlapRNG random = new SlapRNG(); //0xABC7890456123DEFL
        System.out.println(random);
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("SLAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }
    //@Test
    public void testHorde()
    {
        HordeRNG random = new HordeRNG(new long[16]); //0xABC7890456123DEFL
        System.out.println("HordeRNG (testing nextLong): " +
                StringKit.hex(random.state)
                + " with " + StringKit.hex(random.choice));
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
        System.out.println("HORDE: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    //@Test
    public void testHerd()
    {
        HerdRNG random = new HerdRNG(new int[16]); //0xABC7890456123DEFL
        System.out.println("HerdRNG (testing nextInt): " +
                StringKit.hex(random.state)
                + " with " + StringKit.hex(random.choice));
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("HERD: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void testBird()
    {
        BirdRNG random = new BirdRNG(new int[32]); //0xABC7890456123DEFL
        System.out.println("BirdRNG (testing nextInt): " + random.toString());
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("BIRD: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void testBeard()
    {
        BeardRNG random = new BeardRNG(new long[32]); //0xABC7890456123DEFL
        System.out.println("BeardRNG (testing nextLong): " + random.toString());
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
        System.out.println("BEARD: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) >= 0.01) ? "!!!" : "");
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

    @Test
    public void testVortexStreams()
    {
        VortexRNG streamA = new VortexRNG(0xBCD7890456123DEFL, 0);
        VortexRNG streamB = new VortexRNG(0xBCD7890456123DEFL, 1);
        VortexRNG streamC = new VortexRNG(0xBCD7890456123DEFL, 2);
        System.out.println("VortexRNG (testing stream correlation)");
        long[] bitsAB = new long[64], bitsAC = new long[64], bitsBC = new long[64];
        long currA = streamA.nextLong(), currB = streamB.nextLong(), currC = streamC.nextLong(), a, b, c;
        long xorA = currA, xorB = currB, xorC = currC;
        BigInt totalA = new BigInt(currA), totalB = new BigInt(currB), totalC = new BigInt(currC);
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            a = currA ^ (currA = streamA.nextLong());
            b = currB ^ (currB = streamB.nextLong());
            c = currC ^ (currC = streamC.nextLong());
            xorA ^= currA;
            xorB ^= currB;
            xorC ^= currC;
            totalA.add(currA);
            totalB.add(currB);
            totalC.add(currC);
            bi = 63;
            for (long bit = 0x8000000000000000L; bit != 0; bit >>>= 1, bi--) {
                bitsAB[bi] += (a & b & bit) >>> bi;
                bitsAC[bi] += (a & c & bit) >>> bi;
                bitsBC[bi] += (b & c & bit) >>> bi;
            }
        }
        System.out.println("THRUST: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("Stream 0 stats: bit xor " + StringKit.bin(xorA) + " ; total " + totalA.toString());
        System.out.println("Stream 1 stats: bit xor " + StringKit.bin(xorB) + " ; total " + totalB.toString());
        System.out.println("Stream 2 stats: bit xor " + StringKit.bin(xorC) + " ; total " + totalC.toString());
        System.out.println("each bit changes in both of two streams this often relative to 0.0 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("With stream 0 and stream 1: %02d : % .24f\n", i, bitsAB[i] / 0x1p24);
        }
        for (int i = 0; i < 64; i++) {
            System.out.printf("With stream 0 and stream 2: %02d : % .24f\n", i, bitsAC[i] / 0x1p24);
        }
        for (int i = 0; i < 64; i++) {
            System.out.printf("With stream 1 and stream 2: %02d : % .24f\n", i, bitsBC[i] / 0x1p24);
        }
    }
    @Test
    public void testVortexStreamsLow32()
    {
        VortexRNG streamA = new VortexRNG(0xABD7890456123DEFL, 0);
        VortexRNG streamB = new VortexRNG(0xABD7890456123DEFL, 1);
        VortexRNG streamC = new VortexRNG(0xABD7890456123DEFL, 2);
        System.out.println("VortexRNG (testing stream correlation)");
        long[] bitsAB = new long[32], bitsAC = new long[32], bitsBC = new long[32];
        long currA = streamA.nextLong() & 0xFFFFFFFFL, currB = streamB.nextLong() & 0xFFFFFFFFL, currC = streamC.nextLong() & 0xFFFFFFFFL, a, b, c;
        long xorA = currA, xorB = currB, xorC = currC;
        BigInt totalA = new BigInt(currA), totalB = new BigInt(currB), totalC = new BigInt(currC);
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            a = currA ^ (currA = (streamA.nextLong() & 0xFFFFFFFFL));
            b = currB ^ (currB = (streamB.nextLong() & 0xFFFFFFFFL));
            c = currC ^ (currC = (streamC.nextLong() & 0xFFFFFFFFL));
            xorA ^= currA;
            xorB ^= currB;
            xorC ^= currC;
            totalA.add(currA);
            totalB.add(currB);
            totalC.add(currC);
            bi = 31;
            for (long bit = 0x80000000L; bit != 0; bit >>>= 1, bi--) {
                bitsAB[bi] += (a & b & bit) >>> bi;
                bitsAC[bi] += (a & c & bit) >>> bi;
                bitsBC[bi] += (b & c & bit) >>> bi;
            }
        }
        System.out.println("THRUST: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("Stream 0 stats: bit xor " + StringKit.bin(xorA).substring(32) + " ; total " + totalA.toString());
        System.out.println("Stream 1 stats: bit xor " + StringKit.bin(xorB).substring(32) + " ; total " + totalB.toString());
        System.out.println("Stream 2 stats: bit xor " + StringKit.bin(xorC).substring(32) + " ; total " + totalC.toString());
        System.out.println("each bit changes in both of two streams this often relative to 0.0 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("With stream 0 and stream 1: %02d : % .24f\n", i, bitsAB[i] / 0x1p24);
        }
        for (int i = 0; i < 32; i++) {
            System.out.printf("With stream 0 and stream 2: %02d : % .24f\n", i, bitsAC[i] / 0x1p24);
        }
        for (int i = 0; i < 32; i++) {
            System.out.printf("With stream 1 and stream 2: %02d : % .24f\n", i, bitsBC[i] / 0x1p24);
        }
    }

    //@Test
    public void adjustBeard() {
        long[] empty = new long[64];
        BeardRNG random = new BeardRNG(empty); //0xABC7890456123DEFL
        TreeMap<Double, Long> incs = new TreeMap<>();
        /*
        0x9C7B5B2D9E4B6917L with net value 0.06916801
        0x9C7B5B2D9E4B3EE3L with net value 0.06887801
        0x9C7B5B2D9E4B2B8FL with net value 0.05961601

        Using all 16 both times:
        0x9C7B5B2D9E4B369DL with net value 0.06507901
        0x9C7B5B2D9E4BDE89L with net value 0.06393401

        Other measurement, worst single:
        0xC6D7021614D11BD5L with worst bit 0.00267001

        Other single:
        0xAC08F61DB2F84069L with worst bit 0.000484
        0xB816232F6A4C86EDL with worst bit 0.000469
        0xF4C4967D8B6EAD1BL with worst bit 0.000311
        0x91BEF7193B004F97L with worst bit 0.000291
        0x8B5ABD635323F47BL with worst bit 0.000295
        0x862D1CFE86DB78ABL with worst bit 0.000252
        0xF1B188FEB7A8C8F5L with worst bit 0.000221

        Using full then half, worst single:
        0xD47E811F8240DB2FL with worst bit 0.000238
        0xB8D8B8036E6866BDL with worst bit 0.000217
        With >>> 28 then & 14
        0xAF54DE5518402FE9L with worst bit 0.000227
        Back to full and full, worst single:
        0xA254DD1F79DBFBA5L with worst bit 0.000245 // also works well with full and half
        0x8A68578A95364FDDL with worst bit 0.000229
        With & 15 then >>> 29
        0xF6133FE5E2B8660BL with worst bit 0.000250
        0xB56D558197C83115L with worst bit 0.000234
        (now subtracting the inc)
        0xB05EAB5941BE4B45L with worst bit 0.000228
        0xAC8C0FE02D14624DL with worst bit 0.000218
        Same as above, but with 4096 bits of state (64 longs)
        0x82A7DD1255C3757FL with worst bit 0.000256
        0xCABEFFDC0E041E79L with worst bit 0.000255
        0xA2E5A18461343D25L with worst bit 0.000243
        0x80B5966577ABD595L with worst bit 0.000216
        */
        {
            int[] empty2 = new int[32];
            BirdRNG birdRNG = new BirdRNG(empty2);
            long[] bits = new long[64];
            long curr = birdRNG.nextLong(), t;
            int bi;
            double diff = 0.0;
            for (int i = 0; i < 0x1000000; i++) {
                t = curr ^ (curr = birdRNG.nextLong());
                bi = 63;
                for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                    bits[bi] += (t & b) >>> bi;
                }
            }
            System.out.println("BIRD: Out of 0x1000000 random numbers,");
            //System.out.println("Out of 4,294,967,296 random numbers,");
            System.out.println("each bit changes this often relative to 0.5 probability...");
            for (int i = 0; i < 64; i++) {
                System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double)0x1000000, (Math.abs(0.5 - bits[i] / (double)0x1000000) > 0.01) ? "!!!" : "");
                diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double)0x1000000));
            }
            System.out.printf("and the worst bit's value is % .24f\n", diff);

        }
        XoRoRNG seedRandom = new XoRoRNG();
        BIG:
        for (int n = 0; n < 0x10000; n++)
        {
            random.setState(empty);
            long[] bits = new long[64];
            long l = seedRandom.nextLong() | 0x8000000000000001L, curr, t;
            while (Long.bitCount(l) < 20)
                l = seedRandom.nextLong() | 0x8000000000000001L;
            curr = random.calibrate(l);
            int bi;
            for (int i = 0; i < 0x1000000; i++) {
                t = curr ^ (curr = random.calibrate(l));
                bi = 63;
                for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                    bits[bi] += (t & b) >>> bi;
                }
            }
            double diff = 0.0;
            for (int i = 0; i < 64; i++) {
                if(bits[i] == 0.5)
                    continue BIG;
                diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double)0x1000000));
            }
            incs.put(diff, l);
            if(diff <= 0.00022)
                break;
        }
        Map.Entry<Double, Long> ent;
        for (int i = 0; i < 16 && !incs.isEmpty(); i++) {
            ent = incs.pollFirstEntry();
            System.out.printf("%02d: 0x%016XL with worst bit %f\n", i, ent.getValue(),  ent.getKey());
        }
    }
    //@Test
    public void adjustBird() {
        int[] empty = new int[32];
        BirdRNG random = new BirdRNG(empty);
        TreeMap<Double, Long> incs = new TreeMap<>();
        BirdRNG birdRNG = new BirdRNG(empty);

        /*
        We start with an inc of 0x9C7B7B99 . It gets a worst-bit of 0.0002518296241760254
        0xB17643FB with worst bit  0.000243246555328369140000
        0x942DCAA9 with worst bit  0.000231385231018066400000
        0xAAA5E48F with worst bit  0.000228166580200195300000
        0xEAC5B1FF with worst bit  0.000193595886230468750000
        0xBA31BDBD with worst bit  0.000190615653991699220000
        0xE4BC5EE5 with worst bit  0.000183880329132080080000
        0xBD6E6FFF with worst bit  0.000169575214385986330000
        0xCD1E7003 with worst bit  0.000162243843078613280000
        0x8B516D1B with worst bit  0.000161945819854736330000
        0x97360623 with worst bit  0.000142931938171386720000
        POSITIVE ONLY:
        0xCE13C149 with worst bit  0.000148296356201171880000
        0x3EB20065 with worst bit  0.000149607658386230470000
        0x4C213FDD with worst bit  0.000169157981872558600000
        0x6B35312D with worst bit  0.000175118446350097660000
        0x3D87D65F with worst bit  0.000183701515197753900000
        0x775B8301 with worst bit  0.000187039375305175780000
        0x90528B37 with worst bit  0.000199377536773681640000

        Different measurement:
        0xE650BAC7 with worst bit  0.002487182617187500000000
        0xDDD8B643 with worst bit  0.002380371093750000000000
        0xCD8F6C99 with worst bit  0.002334594726562500000000
        0xEE7582EB with worst bit  0.002243041992187500000000
        0xB5087949 with worst bit  0.001998901367187500000000

        0x9F89259B with worst bit  0.002288818359375000000000
        0x8EABB261 with worst bit  0.002349853515625000000000

        0xD65D2301 with worst bit  0.002197265625000000000000
        0xBB85D583 with worst bit  0.002166748046875000000000
        0xC6A78E99 with worst bit  0.002151489257812500000000
        0xC0216E67 with worst bit  0.002090454101562500000000
        0xC241E5C7 with worst bit  0.002090454101562500000000
        0xC38939A9 with worst bit  0.002120971679687500000000
        0xFBA06527 with worst bit  0.001998901367187500000000
        0xFFAFF99F with worst bit  0.001968383789062500000000
        0xF8EDCF47 with worst bit  0.001968383789062500000000
        0xC52B2639 with worst bit  0.001953125000000000000000
        0xB2886441 with worst bit  0.001937866210937500000000
        0xC59EA5CD with worst bit  0.001922607421875000000000

        With signed shift:
        0xB8DB18C9 with worst bit  0.002288818359375000000000
        0x92B48913 with worst bit  0.002166748046875000000000

        With only a quarter in the second section:
        0x87F2C853 with worst bit  0.001968383789062500000000
        0x9104B09D with worst bit  0.001907348632812500000000

        With full and full:
        0x87199791 with worst bit  0.001998901367187500000000
        0xD54B131D with worst bit  0.001937866210937500000000
        0x8E4CC1CB with worst bit  0.001876831054687500000000

        With even half then full:
        0xF5511A8F with worst bit  0.001953125000000000000000
        0xA5A55DB1 with worst bit  0.001922607421875000000000
        0xDC364B89 with worst bit  0.001907348632812500000000

        With & 27 then full:
        0xE55E9861 with worst bit  0.001876831054687500000000
        0xCC2F600B with worst bit  0.001831054687500000000000
        0xFBB724F7 with worst bit  0.001800537109375000000000

        0xD3318799 with choice 0xDA646A7D and worst bit  0.001953125000000000000000
        0xB6EBF7C1 with choice 0xC7753EB1 and worst bit  0.001907348632812500000000
        0xDB245BCF with choice 0x9D95189F and worst bit  0.001907348632812500000000
        0xB42B8459 with choice 0xAB481C87 and worst bit  0.001892089843750000000000
        0xA67201ED with choice 0xF5EC5B8F and worst bit  0.001892089843750000000000
        0xA30B6679 with choice 0xFE2D3BF1 and worst bit  0.001861572265625000000000
        0xB266311F with choice 0xF2FC9381 and worst bit  0.001846313476562500000000
        0xA2BEE947 with choice 0xB352933B and worst bit  0.001846313476562500000000
        0xF743D4C7 with choice 0xE33EC697 and worst bit  0.001846313476562500000000
        0xAA78B075 with choice 0xF224C7E9 and worst bit  0.001831054687500000000000
        0xD3318799 with choice 0xF7A0E53F and worst bit  0.001831054687500000000000

        0x9C3840C1 with choice 0xFFFF92B9 and worst bit  0.001358032226562500000000
        0xAD34B41F with choice 0xFFFF9B0F and worst bit  0.001251220703125000000000
        0xBB966F35 with choice 0xFFFF7467 and worst bit  0.001144409179687500000000
        0xB5D24837 with choice 0xFFFF82D7 and worst bit  0.001007080078125000000000
        0x99B31F7D with choice 0xFFFFEAA9 and worst bit  0.000961303710937500000000
        0xA1EAC45B with choice 0xFFFF8EDF and worst bit  0.000915527343750000000000

        Full &, then half shift:
        0x848B938F with choice 0x9E99C7C5 and worst bit  0.001983642578125000000000
        0x8BDAE947 with choice 0xFC1B01D7 and worst bit  0.001815795898437500000000

        Full 8 &, then quarter shift:
        0x8ED34985 with choice 0x0DBB0941 and worst bit  0.001815795898437500000000

        Full &, then half shift, no extra assign:
        0x8FA39AAF with choice 0xCF69B4A1 and worst bit  0.001998901367187500000000

        Funky multiply scheme:
        0x934AA4DB with choice 0xE80166FF and worst bit  0.001754760742187500000000
        0x89514FCF with choice 0xE53528E1 and worst bit  0.001098632812500000000000

        Just choice:
        choice 0xDC917F99 with worst bit  0.001876831054687500000000

        return (state[(choice += chooser) & 31] += (state[choice >>> 28] ^= choice ) >>> 1);
        0x9A7A122D with choice 0x93CF4FE3 and worst bit  0.001937866210937500000000

        0x941CCBAD with choice 0x8A532AEF and worst bit  0.001739501953125000000000
        0x9FA5A83D with choice 0xF1F643F3 and worst bit  0.001861572265625000000000

        TRY OVER, 64 ints
        0x89BF5693 with choice 0x979DA1A1 and worst bit  0.002075195312500000000000
        0x92B121D1 with choice 0x8C188ACD and worst bit  0.002029418945312500000000
        0x92FAF5C3 with choice 0xB7B99889 and worst bit  0.001907348632812500000000
        0x8EAB85D5 with choice 0x86EF12FB and worst bit  0.001907348632812500000000
        0x8B196EF9 with choice 0x96D9801D and worst bit  0.001892089843750000000000
        0x869F76D7 with choice 0x99EAE66B and worst bit  0.001876831054687500000000
        0x8E4609A9 with choice 0xA1743AFB and worst bit  0.001861572265625000000000

        0xB892AAE3 with choice 0x5497F549 and worst bit  0.001937866210937500000000
        0x82A93DC1 with choice 0x8D63310D and worst bit  0.001907348632812500000000
        0x83A72621 with choice 0xB9628BB7 and worst bit  0.001892089843750000000000
        0x9CFC3EE1 with choice 0xD64D623F and worst bit  0.001876831054687500000000
        0xBEDEE5ED with choice 0xFFA10063 and worst bit  0.001785278320312500000000
        0x83DADBB7 with choice 0xC98EE799 and worst bit  0.001724243164062500000000
        0x9296FE47 with choice 0xB9A2842F and worst bit  0.001663208007812500000000
        0x92970945 with choice 0xB9A2442F and worst bit  0.001983642578125000000000
        0x9297052B with choice 0xB9A29C2F and worst bit  0.001983642578125000000000
        0x9296FB45 with choice 0xB9A4842F and worst bit  0.001968383789062500000000
        0x929700AD with choice 0x31A2842F and worst bit  0.001724243164062500000000
        0x9296FCA7 with choice 0x39A6842F and worst bit  0.001617431640625000000000
        0x9297029B with choice 0x39A7852F and worst bit  0.001449584960937500000000
        */
        {
            int[] bits = new int[32];
            int curr = birdRNG.nextInt(), t;
            int bi;
            double diff = 0.0;
            for (int i = 0; i < 0x1000000; i++) {
                t = curr ^ (curr = birdRNG.nextInt());
                bi = 31;
                for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                    bits[bi] += (t & b) >>> bi;
                }

            }
            System.out.println("BIRD: Out of 0x1000000 random numbers,");
            //System.out.println("Out of 4,294,967,296 random numbers,");
            System.out.println("each bit changes this often relative to 0.5 probability...");
            for (int i = 0; i < 32; i++) {
                System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.01) ? "!!!" : "");
                diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double)0x1000000));
            }
            System.out.printf("and the worst bit's value is % .24f\n", diff);

        }

        PintRNG pr = new PintRNG();
        //int state = BirdRNG.splitMix32(pr.nextInt());
        int[] bits = new int[32];
        //0x9296FE47 with choice 0xB9A2842F
        for (int n = -0xE00; n < 0xE00; n+= 2) {
            //int l = BirdRNG.splitMix32(state += 0x9E3779B9) >>> 2 | 0x80000001,
            //        curr, t, ch = pr.nextInt() >>> 2 | 0x80000001;
            int curr, t;
            int l =  0x9296FCA7 /*0x869F76D7*/ + n, ch;
            BIG:
            for (int p = 0; p < 0x20; p++) {
                ch = 0xB9A6842F /*0x99EAE66B*/ ^ (2 << pr.next(5)) ^ (2 << pr.next(5));
                random.setState(empty);
                for (int i = 0; i < 32; i++) {
                    bits[i] = 0;
                }
                //while (Integer.bitCount(l) <= 12)
                //    l = BirdRNG.splitMix32(state += 0x9E3779B9) >>> 2 | 0x80000001;
                //while (Integer.bitCount(ch) <= 10)
                //    ch = pr.nextInt() >>> 2 | 0x80000001;
                curr = random.calibrate(l, ch);
                int bi;
                for (int i = 0; i < 0x10000; i++) {
                    t = curr ^ (curr = random.calibrate(l, ch));
                    bi = 31;
                    for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                        bits[bi] += (t & b) >>> bi;
                    }
                }

                double diff = 0.0;
                for (int i = 0; i < 32; i++) {
                    if (bits[i] == 0x8000)
                        continue BIG;
                    diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double) 0x10000));
                }
                incs.put(diff, (long) l << 32 | (ch & 0xFFFFFFFFL));
                if (diff <= 0.0016)
                    break;
            }
        }
        Map.Entry<Double, Long> ent;
        for (int i = 0; i < 16 && !incs.isEmpty(); i++) {
            ent = incs.pollFirstEntry();
            System.out.printf("%03d: 0x%08X with choice 0x%08X and worst bit % .24f\n", i, ent.getValue() >>> 32, ent.getValue() & 0xFFFFFFFFL,  ent.getKey());
        }

    }

    //@Test
    public void adjustBard() {
        int[] empty = new int[32];
        BardRNG random = new BardRNG(empty);
        TreeMap<Double, Long> incs = new TreeMap<>();
        BardRNG bardRNG = new BardRNG(empty);
        {
            int[] bits = new int[32];
            int curr = bardRNG.nextInt(), t;
            int bi;
            double diff = 0.0;
            for (int i = 0; i < 0x1000000; i++) {
                t = curr ^ (curr = bardRNG.nextInt());
                bi = 31;
                for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                    bits[bi] += (t & b) >>> bi;
                }

            }
            System.out.println("BIRD: Out of 0x1000000 random numbers,");
            //System.out.println("Out of 4,294,967,296 random numbers,");
            System.out.println("each bit changes this often relative to 0.5 probability...");
            for (int i = 0; i < 32; i++) {
                System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.01) ? "!!!" : "");
                diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double)0x1000000));
            }
            System.out.printf("and the worst bit's value is % .24f\n", diff);

        }

        PintRNG pr = new PintRNG();
        int state = BirdRNG.splitMix32(pr.nextInt());
        int[] bits = new int[32];
        //0x9296FE47 with choice 0xB9A2842F
        for (int n = 0; n < 0x200; n+= 2) {
            int l = BirdRNG.splitMix32(state += 0x9E3779B9) >>> 2 | 0x80000001,
                    curr, t, ch = pr.nextInt() >>> 2 | 0x80000001;
            BIG:
            for (int p = 0; p < 0x20; p++) {

                random.setState(empty);
                for (int i = 0; i < 32; i++) {
                    bits[i] = 0;
                }
                while (Integer.bitCount(l) <= 12)
                    l = BirdRNG.splitMix32(state += 0x9E3779B9) >>> 2 | 0x80000001;
                while (Integer.bitCount(ch) <= 10)
                    ch = pr.nextInt() >>> 2 | 0x80000001;
                curr = random.calibrate(l, ch);
                int bi;
                for (int i = 0; i < 0x10000; i++) {
                    t = curr ^ (curr = random.calibrate(l, ch));
                    bi = 31;
                    for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                        bits[bi] += (t & b) >>> bi;
                    }
                }

                double diff = 0.0;
                for (int i = 0; i < 32; i++) {
                    if (bits[i] == 0x8000)
                        continue BIG;
                    diff = Math.max(diff, Math.abs(0.5 - bits[i] / (double) 0x10000));
                }
                incs.put(diff, (long) l << 32 | (ch & 0xFFFFFFFFL));
                if (diff <= 0.0016)
                    break;
            }
        }
        Map.Entry<Double, Long> ent;
        for (int i = 0; i < 16 && !incs.isEmpty(); i++) {
            ent = incs.pollFirstEntry();
            System.out.printf("%03d: 0x%08X with choice 0x%08X and worst bit % .24f\n", i, ent.getValue() >>> 32, ent.getValue() & 0xFFFFFFFFL,  ent.getKey());
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

    private static BigInteger lcm(long[] values, int span)
    {
        BigInteger result = BigInteger.valueOf(values[0]), tmp;
        for (int i = 1; i < span; i++) {
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
            best = best.max(l = lcm(buckets, buckets.length));
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
