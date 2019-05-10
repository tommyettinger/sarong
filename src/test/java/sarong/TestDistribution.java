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
    public void test32Bit()
    {
//        int result, xor = 0;
        int r;
//        BigInt sum = new BigInt(0);
        RoaringBitmap all = new RoaringBitmap();
        int i = 0x80000000;
        r = 1;
        for (; i < 0x7FFFFFFF; i++) {
//            result = (i << 6) + (i << 28 | i >>> 4);
//            result = i - (i << 11 | i >>> 21);
//            result = (result << 28 | result >>> 4);
//            result = (i ^ i >>> 15) + (i << 23);
//            xor ^= result;
//            sum.add(result);
//            r = (i >>> 12) ^ (i << 19 | i >>> 13);
            //r = (i << 5 | i >>> 27) ^ (i << 7); // full range
//            r = (i << 27 | i >>> 5) ^ (i >>> 7);
            //(a = (a << 23 | a >>> 9) * 0x402AB) 
            //(b = (b << 28 | b >>> 4) * 0x01621)
            //(c = (c << 24 | c >>> 8) * 0x808E9)
            //(d = (d << 29 | d >>> 3) * 0x8012D)
            
            //r = (r << 23 | r >>> 9) * 0x402AB; //start at 0x00000001, stop at 0x0003A96B
            //r = (r << 28 | r >>> 4) * 0x01621; //start at 0x00000001, stop at 0x0002973F
            //r = (r << 24 | r >>> 8) * 0x808E9; //start at 0x00001D79, stop at 0x000664C8
            //r = (r << 29 | r >>> 3) * 0x8012D; //start at 0x0001682B, stop at 0x0003219A
            //r = (r << 13 | r >>> 19) * 0x89A7;  //start at 0x000015D5, stop at 0x00017DDE
            r = (r << 17 | r >>> 15) * 0xBCFD;  //start at 0x000015D5, stop at 
            if(!all.checkedAdd(r))
            {
                break;
//                System.out.println("UH OH, duplicate " + r + " at " + (i + 0x80000000L));
//                return;
            }
        }
//        t = 0x7FFFFFFF + 0x9E3779B9;
//        result = (0x7FFFFFFF << 17 | 0x7FFFFFFF >>> 15) ^ t;
//        result = (0x7FFFFFFF << 6) + (0x7FFFFFFF << 28 | 0x7FFFFFFF >>> 4);
//        result = 0x7FFFFFFF - (0x7FFFFFFF << 11 | 0x7FFFFFFF >>> 21);
//        result = (result << 28 | result >>> 4);
//        result = (0x7FFFFFFF ^ 0x7FFFFFFF >>> 15) + (0x7FFFFFF << 23);
//        xor ^= result;
//        r = (i >>> 12) ^ (i << 19 | i >>> 13);
        //r = (i << 5 | i >>> 27) ^ (i << 7); // full range
//        r = (i << 27 | i >>> 5) ^ (i >>> 7);
//        r = (r << 23 | r >>> 9) * 0x402AB;
//        if(!all.checkedAdd(r))
//            System.out.println("TROUBLE AT THE END!");
        all.flip(1L, 0x100000000L);
        System.out.println("[0] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(0)));
        System.out.println("[1] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(1)));
        System.out.println("[2] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(2)));
        System.out.println("[3] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(3)));
        System.out.println("[4] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(4)));
        System.out.println("[5] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(5)));
        System.out.println("[6] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(6)));
        System.out.println("[7] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(7)));
        System.out.println("[8] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(8)));
        System.out.println("[9] non-zero integer not in largest cycle: 0x" + StringKit.hex(all.select(9)));
//        all.addInt((0x7FFFFFFF >>> 6) ^ (0x7FFFFFFF << 28 | 0x7FFFFFFF >>> 4));
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

}
