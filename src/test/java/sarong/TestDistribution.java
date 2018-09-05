package sarong;

import org.huldra.math.BigInt;
import org.junit.Test;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

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

    @Test
    public void test32Bit()
    {
//        int result, xor = 0;
//        BigInt sum = new BigInt(0);
        Roaring64NavigableMap all = new Roaring64NavigableMap();
        for (int i = 0x80000000; i < 0x7FFFFFFF; i++) {
//            result = (i << 6) + (i << 28 | i >>> 4);
//            result = i - (i << 11 | i >>> 21);
//            result = (result << 28 | result >>> 4);
//            result = (i ^ i >>> 15) + (i << 23);
//            xor ^= result;
//            sum.add(result);
            all.addInt((i >>> 6) ^ (i << 28 | i >>> 4));
        }
//        t = 0x7FFFFFFF + 0x9E3779B9;
//        result = (0x7FFFFFFF << 17 | 0x7FFFFFFF >>> 15) ^ t;
//        result = (0x7FFFFFFF << 6) + (0x7FFFFFFF << 28 | 0x7FFFFFFF >>> 4);
//        result = 0x7FFFFFFF - (0x7FFFFFFF << 11 | 0x7FFFFFFF >>> 21);
//        result = (result << 28 | result >>> 4);
//        result = (0x7FFFFFFF ^ 0x7FFFFFFF >>> 15) + (0x7FFFFFF << 23);
//        xor ^= result;
        all.addInt((0x7FFFFFFF >>> 6) ^ (0x7FFFFFFF << 28 | 0x7FFFFFFF >>> 4));
//        sum.add(result);
//        System.out.println(sum.toBinaryString() + ", should be -" + Long.toBinaryString(0x80000000L));
//        System.out.println(sum.toString() + ", should be -" + (0x80000000L));
//        System.out.println(Integer.toBinaryString(xor) + " " + xor);
        System.out.println(all.getLongCardinality());
//        int b = -1;
//        for (int i = 0; i < 32; i++) {
//            System.out.printf("%03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X  %03d: %08X\n",
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b],
//                    ++b, counts[b], ++b, counts[b], ++b, counts[b], ++b, counts[b]);
//        }
    }

    @Test
    public void test16Bit()
    {
        short t, result, xor = 0;
        BigInt sum = new BigInt(0);
        //long[] counts = new long[256];
        Roaring64NavigableMap all = new Roaring64NavigableMap();
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

}
