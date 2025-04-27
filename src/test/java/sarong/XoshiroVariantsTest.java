package sarong;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntIntOrderedMap;
import com.github.tommyettinger.ds.support.sort.IntComparators;
import org.junit.Test;

public class XoshiroVariantsTest {

    private int clz5(int v) {
        return Integer.numberOfLeadingZeros(v & 31) - 27;
    }

    /**
     * <pre>
     * Period was 0x3FFFFC00
     * Took 45080 ms.
     * 33554432/33554432 5-tuples were present.
     * 0.0% of 5-tuples were missing.
     * Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:
     * 2 repetitions occurred for 16 5-tuples.
     * 4 repetitions occurred for 256 5-tuples.
     * 6 repetitions occurred for 880 5-tuples.
     * 8 repetitions occurred for 3184 5-tuples.
     * 10 repetitions occurred for 9680 5-tuples.
     * 12 repetitions occurred for 26792 5-tuples.
     * 13 repetitions occurred for 2 5-tuples.
     * 14 repetitions occurred for 69326 5-tuples.
     * 15 repetitions occurred for 2 5-tuples.
     * 16 repetitions occurred for 157430 5-tuples.
     * 17 repetitions occurred for 8 5-tuples.
     * 18 repetitions occurred for 329088 5-tuples.
     * 19 repetitions occurred for 14 5-tuples.
     * 20 repetitions occurred for 635594 5-tuples.
     * 21 repetitions occurred for 20 5-tuples.
     * 22 repetitions occurred for 1128676 5-tuples.
     * 23 repetitions occurred for 32 5-tuples.
     * 24 repetitions occurred for 1816584 5-tuples.
     * 25 repetitions occurred for 56 5-tuples.
     * 26 repetitions occurred for 2664864 5-tuples.
     * 27 repetitions occurred for 76 5-tuples.
     * 28 repetitions occurred for 3537748 5-tuples.
     * 29 repetitions occurred for 126 5-tuples.
     * 30 repetitions occurred for 4187434 5-tuples.
     * 31 repetitions occurred for 120 5-tuples.
     * 32 repetitions occurred for 4418536 5-tuples.
     * 33 repetitions occurred for 104 5-tuples.
     * 34 repetitions occurred for 4187456 5-tuples.
     * 35 repetitions occurred for 126 5-tuples.
     * 36 repetitions occurred for 3537698 5-tuples.
     * 37 repetitions occurred for 86 5-tuples.
     * 38 repetitions occurred for 2664834 5-tuples.
     * 39 repetitions occurred for 98 5-tuples.
     * 40 repetitions occurred for 1816518 5-tuples.
     * 41 repetitions occurred for 72 5-tuples.
     * 42 repetitions occurred for 1128624 5-tuples.
     * 43 repetitions occurred for 38 5-tuples.
     * 44 repetitions occurred for 635570 5-tuples.
     * 45 repetitions occurred for 22 5-tuples.
     * 46 repetitions occurred for 329074 5-tuples.
     * 47 repetitions occurred for 16 5-tuples.
     * 48 repetitions occurred for 157416 5-tuples.
     * 49 repetitions occurred for 4 5-tuples.
     * 50 repetitions occurred for 69324 5-tuples.
     * 52 repetitions occurred for 26792 5-tuples.
     * 53 repetitions occurred for 2 5-tuples.
     * 54 repetitions occurred for 9678 5-tuples.
     * 56 repetitions occurred for 3184 5-tuples.
     * 58 repetitions occurred for 880 5-tuples.
     * 60 repetitions occurred for 256 5-tuples.
     * 62 repetitions occurred for 16 5-tuples.
     * </pre>
     */
    @Test
    public void check5TupleFrequencyOrbitalByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 25, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, stateF = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x1FFFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE, endF = stateF;

        long i = 0L;
        while (++i <= 0x40000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x1FFFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE && stateF == endF) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 25) + " 5-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-25 + "% of 5-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(128, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:");
        System.out.println(inv.toString(" 5-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 5-tuples.");
    }
    /**
     * <pre>
     * Period was 0x3FFFFC00
     * Took 26267 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     * Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:
     * 1021 repetitions occurred for 2 4-tuples.
     * 1022 repetitions occurred for 14 4-tuples.
     * 1023 repetitions occurred for 990 4-tuples.
     * 1024 repetitions occurred for 1047570 4-tuples.
     * </pre>
     */
    @Test
    public void check4TupleFrequencyOrbitalByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 20, 0.6f);

        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, stateF = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x00FFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE, endF = stateF;

        long i = 0L;
        while (++i <= 0x40000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x00FFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE && stateF == endF) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 20) + " 4-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-20 + "% of 4-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(128, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:");
        System.out.println(inv.toString(" 4-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 4-tuples.");
    }
    /**
     * <pre>
     * Period was 0x3FFFFC00
     * Took 6688 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 32764 repetitions occurred for 2 3-tuples.
     * 32765 repetitions occurred for 6 3-tuples.
     * 32766 repetitions occurred for 74 3-tuples.
     * 32767 repetitions occurred for 850 3-tuples.
     * 32768 repetitions occurred for 31836 3-tuples.
     * </pre>
     */
    @Test
    public void check3TupleFrequencyOrbitalByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 15, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, stateF = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x0007FE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE, endF = stateF;

        long i = 0L;
        while (++i <= 0x40000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x0007FE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE && stateF == endF) {
                break;
            }
        }

        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 15) + " 3-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-15 + "% of 3-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(400, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:");
        System.out.println(inv.toString(" 3-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 3-tuples.");
    }
    /**
     * <pre>
     * Period was 0x3FFFFC00
     * Took 5674 ms.
     * 1024/1024 2-tuples were present.
     * 0.0% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 1048568 repetitions occurred for 2 2-tuples.
     * 1048569 repetitions occurred for 2 2-tuples.
     * 1048570 repetitions occurred for 4 2-tuples.
     * 1048571 repetitions occurred for 4 2-tuples.
     * 1048572 repetitions occurred for 40 2-tuples.
     * 1048573 repetitions occurred for 42 2-tuples.
     * 1048574 repetitions occurred for 200 2-tuples.
     * 1048575 repetitions occurred for 264 2-tuples.
     * 1048576 repetitions occurred for 466 2-tuples.
     * </pre>
     */
    @Test
    public void check2TupleFrequencyOrbitalByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 10, 0.6f);

        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, stateF = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x000003E0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE, endF = stateF;

        long i = 0L;
        while (++i <= 0x40000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x000003E0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE && stateF == endF) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 10) + " 2-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-10 + "% of 2-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(1000, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:");
        System.out.println(inv.toString(" 2-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 2-tuples.");
    }
    /**
     * <pre>
     * Period was 0x3FFFFC00
     * Took 3811 ms.
     * Frequency of 00: 33554400
     * Frequency of 01: 33554400
     * Frequency of 02: 33554400
     * Frequency of 03: 33554400
     * Frequency of 04: 33554400
     * Frequency of 05: 33554400
     * Frequency of 06: 33554400
     * Frequency of 07: 33554400
     * Frequency of 08: 33554400
     * Frequency of 09: 33554400
     * Frequency of 10: 33554400
     * Frequency of 11: 33554400
     * Frequency of 12: 33554400
     * Frequency of 13: 33554400
     * Frequency of 14: 33554400
     * Frequency of 15: 33554400
     * Frequency of 16: 33554400
     * Frequency of 17: 33554400
     * Frequency of 18: 33554400
     * Frequency of 19: 33554400
     * Frequency of 20: 33554400
     * Frequency of 21: 33554400
     * Frequency of 22: 33554400
     * Frequency of 23: 33554400
     * Frequency of 24: 33554400
     * Frequency of 25: 33554400
     * Frequency of 26: 33554400
     * Frequency of 27: 33554400
     * Frequency of 28: 33554400
     * Frequency of 29: 33554400
     * Frequency of 30: 33554400
     * Frequency of 31: 33554400
     * </pre>
     */
    @Test
    public void checkFrequencyOrbitalByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[32];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, stateF = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE, endF = stateF;

        long i = 0L;
        while (++i <= 0x40000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ stateF;
            stateE = stateE + 0x1D + stateC & 31;
            stateF = stateF + 0x17 + clz5(stateE) & 31;
            int t = stateB >>> 1;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE && stateF == endF) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        for (int j = 0; j < 32; j++) {
            System.out.printf("Frequency of %02d: %d \n", j, frequencies[j]);
        }
    }

}
