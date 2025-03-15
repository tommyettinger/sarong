package sarong;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tommy Ettinger on 9/13/2018.
 */
public class PeriodTest {
    private int rotate8(int v, int amt) {
        return (v << (amt & 7) & 255) | ((v & 255) >>> (8 - amt & 7));
    }

    @Test
    public void checkPeriod32(){
        int stateA = 1, i = 0;
        //final int r = 13, m = 0x89A7;
        final int r = 17, m = 0xBCFD, a = 0xA5F152BF;
        for (; i != -1; i++) {
            if ((stateA = Integer.rotateLeft(stateA, r) * m + a) == 1) {
                //if (i >>> 24 == 0xFF)
                System.out.printf("(state + 0x%08X, rotation %02d: 0x%08X\n", m, r, i);
                break;
            }
        }
    }

    @Test
    public void checkPeriod32_Weird(){
        int stateA = 1;
        long i = 0;
        final int a = 3;
        for (; i != -1; i++) {
            if ((stateA = Integer.rotateLeft(~stateA, 1)) == 1) {
                System.out.printf("(~(state + 0x%08X): 0x%08X\n", a, i);
                break;
            }
        }
    }

    @Test
    public void checkPeriod32_LFSR(){
        int state = 1, i = 1;
        for (; i != -1; i++) {
//            if ((state = state >>> 1 ^ (-(state & 1) & 0xA3000000)) == 1) {
            if ((state = state << 1 ^ ((state >> 31) & 0x000000C5)) == 1) {
                System.out.printf("LFSR: 0x%08X\n", i);
                return;
            }
        }
        System.out.printf("Full period: %08X", i);
    }
    @Test
    public void checkPeriod32_Sprig(){
        short stateA = 1, stateB = 1;
        long i = 0;
        while (++i < 0x200000000L) {
//            if ((stateB = (short) (0x81 + ((stateB >>> 1 & 0x7FFF) ^ (-(stateB & 1) & 0xB400)))) == 1 && stateB == 1) {
            if ((stateA = (short) ((stateB += 0xDE4D) + (stateA << 10 | (stateA & 0xFFFF) >>> 6))) == 1 && stateB == 1) {
                System.out.printf("Sprig: 0x%08X\n", i);
                break;
            }
        }
    }

    /**
     * Testing a 16-bit Galois LFSR, in stateB, as the changing increment for a 16-bit counter, stateA.
     * The total period is 0xFFFF0000, or (2 to the 16) times ((2 to the 16) minus 1); as long as stateB isn't set to 0,
     * it won't have any smaller subcycles.
     */
    @Test
    public void checkPeriod32_Ginger(){
        short stateA = 1, stateB = 1;
        long i = 0;
        while (++i < 0x200000000L) {
//            if ((stateB = (short) (0x81 + ((stateB >>> 1 & 0x7FFF) ^ (-(stateB & 1) & 0xB400)))) == 1 && stateB == 1) {
            if ((stateA += ~(stateB = (short) ((stateB << 1) ^ (stateB >> 31 & 0x2D)))) == 1 && stateB == 1) {
//            if ((stateA += ~(stateB = (short) ((stateB >>> 1 & 0x7FFF) ^ (-(stateB & 1) & 0xB400)))) == 1 && stateB == 1) {
                System.out.printf("Ginger: 0x%08X\n", i);
                break;
            }
        }
    }

    @Test
    public void checkPeriod32_Giblet(){
        short stateA = 1, stateB = 1;
        long i = 0;
        while (++i < 0x200000000L) {
            //					uint64_t s = (stateA += 0xCC62FCEB9202FAADUL);
            //					s = (s ^ s >> 31 ^ (stateB = s < 0xD1342543DE82EF95UL ? stateB : (stateB >> 1UL ^ (0UL - (stateB & 1UL) & 0xD800000000000000UL)))) * 0xC6BC279692B5C323UL;
            //					return s ^ s >> 28;
            short s = (stateA += 0xFAAB);
            if(s < 0x5195) stateB = (short) ((stateB >>> 1 & 0x7FFF) ^ (-(stateB & 1) & 0xB400));
            if (s == 1 && stateB == 1) {
                System.out.printf("Giblet: 0x%08X\n", i);
                break;
            }
        }
    }

    @Test
    public void checkPeriod32_Slide(){
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestInc = 1;
        for (int b = 1; b < 256; b++) {
            long i = 0;
            while (++i <= 0x100000100L) {
                final int fa = stateA;
                final int fb = stateB;
                final int fc = stateC;
                final int fd = stateD;
//                stateA = fc ^ fd; // best b is 227, 0xFEF72B34
//                stateA = fb ^ fc ^ fd; // best b is 155, 0xFE1803FB
//                stateA = (fc ^ fd + fb) & 255; // best b is 233, 0xFF00632A
//                stateB = (fa << 5 | fa >>> 3) & 255;
//                stateC = fa + fb & 255;
//                stateD = fc + b & 255;

//                stateA = (fc + fd) & 255; // best b is 34, 0xFFCA0B79, worst cycle is 5, from [20 254 127 19]
                stateA = fc ^ fd; // best b is 18, 0xFF7A48C6, worst cycle is , from []
                stateB = (fa << 5 | fa >>> 3) & 255;
                stateC = fa + fb & 255;
                stateD = fc ^ b;
                if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                    System.out.printf(b + ": 0x%08X\n", i);
                    if(i > best)
                    {
                        best = i;
                        bestInc = b;
                    }
                    break;
                }
            }
        }
        System.out.println("Best addend was " + bestInc);
    }

    @Test
    public void checkPeriod32_Slip(){
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestInc = 1;
        for (int b = 1; b < 256; b+=2) {
            long i = 0;
            while (++i <= 0x100000100L) {
                final int fa = stateA;
                final int fb = stateB;
                final int fc = stateC;
                final int fd = stateD;
                stateA = fc ^ fd; // best b is 189, 0xFB0A5400, worst cycle is 768, from [0 132 3 118]
                stateB = (fa << 5 | fa >>> 3) & 255;
                stateC = fa + fb & 255;
                stateD = fd + b & 255;
                if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                    System.out.printf(b + ": 0x%08X\n", i);
                    if(i > best)
                    {
                        best = i;
                        bestInc = b;
                    }
                    break;
                }
            }
        }
        System.out.println("Best increment was " + bestInc);
    }

    @Test
    public void checkWorstPeriod32_Slide(){
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, wa = 0, wb = 0, wc = 0, wd = 0;
        long worst = ~0xFFCA0B79;
        for (int sa = 0; sa < 256; sa++) {
            for (int sb = 0; sb < 256; sb++) {
                for (int sc = 0; sc < 256; sc++) {
                    for (int sd = 0; sd < 256; sd++) {
                        stateA = sa;
                        stateB = sb;
                        stateC = sc;
                        stateD = sd;
                        long i = 0;
                        while (++i <= worst) {
                            final int fa = stateA;
                            final int fb = stateB;
                            final int fc = stateC;
                            final int fd = stateD;
//                            stateA = fc ^ fd; // best b is 227, 0xFEF72B34, worst cycle is 2, from [92 80 13 143]
//                            stateA = fb ^ fc ^ fd; // best b is 155, 0xFE1803FB, worst cycle is 1, from [104 13 117 16]
//                            stateA = (fc ^ fd + fb) & 255; // best b is 233, 0xFF00632A, worst cycle is 1, from [49 38 87 64]
//                            stateB = (fa << 5 | fa >>> 3) & 255;
//                            stateC = fa + fb & 255;
//                            stateD = fc + 233 & 255;

//                            stateA = (fc + fd) & 255; // best b is 34, 0xFFCA0B79, worst cycle is 5, from [20 254 127 19]
                            stateA = fc ^ fd; // best b is 18, 0xFF7A48C6, worst cycle is 1, from [18 66 84 70]
                            stateB = (fa << 5 | fa >>> 3) & 255;
                            stateC = fa + fb & 255;
                            stateD = fc ^ 18;
                            if (stateA == sa && stateB == sb && stateC == sc && stateD == sd) {
                                if (i < worst) {
                                    worst = i;
                                    wa = sa;
                                    wb = sb;
                                    wc = sc;
                                    wd = sd;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("Worst cycle was %d with states %d %d %d %d\n", worst, wa, wb, wc, wd);
    }

    @Test
    public void checkWorstPeriod32_Slip(){
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, wa = 0, wb = 0, wc = 0, wd = 0;
        long worst = ~0xFB0A5400;
        for (int sa = 0; sa < 256; sa++) {
            for (int sb = 0; sb < 256; sb++) {
                for (int sc = 0; sc < 256; sc++) {
                    for (int sd = 0; sd < 256; sd++) {
                        stateA = sa;
                        stateB = sb;
                        stateC = sc;
                        stateD = sd;
                        long i = 0;
                        while (++i <= worst) {
                            final int fa = stateA;
                            final int fb = stateB;
                            final int fc = stateC;
                            final int fd = stateD;
                            stateA = fc ^ fd; // best b is 189, 0xFB0A5400, worst cycle is 768, from [0 132 3 118]
                            stateB = (fa << 5 | fa >>> 3) & 255;
                            stateC = fa + fb & 255;
                            stateD = fd + 189 & 255;
                            if (stateA == sa && stateB == sb && stateC == sc && stateD == sd) {
                                if (i < worst) {
                                    worst = i;
                                    wa = sa;
                                    wb = sb;
                                    wc = sc;
                                    wd = sd;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("Worst cycle was %d with states %d %d %d %d\n", worst, wa, wb, wc, wd);
    }

    /**
     * Best right shift was 1, best left rotation was 2, with period 0x0FFFFF
     * Best left  shift was 1, best left rotation was 3, with period 0x0FFFFF
     */
    @Test
    public void checkPeriod20_Xoshiro4x5(){
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestShift = 1, bestRot = 1;
        for (int shift = 1; shift < 5; shift++) {
            for (int rot = 1; rot < 5; rot++) {
                int i = 0;
                while (++i <= 0x100100) {
                    int t = stateB << shift & 31;
//                    int t = stateB >>> shift;
                    stateC ^= stateA;
                    stateD ^= stateB;
                    stateB ^= stateC;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << rot | stateD >>> 5 - rot) & 31;

                    if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                        System.out.printf("left shift %d, rotl %d: 0x%08X\n", shift, rot, i);
                        if (i > best) {
                            best = i;
                            bestShift = shift;
                            bestRot = rot;
                        }
                        break;
                    }
                }
            }
        }
        System.out.printf("Best left shift was %d, best left rotation was %d, with period 0x%06X\nTook %d ms.\n",
                bestShift, bestRot, best, (System.currentTimeMillis() - startTime));
    }

    /**
     * Best right shift was 1, best left rotation was 1, with period 3FFFFF
     */
    @Test
    public void checkPeriod24_Xoshiro4x6(){
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestShift = 1, bestRot = 1;
        for (int shift = 1; shift < 6; shift++) {
            for (int rot = 1; rot < 6; rot++) {
                int i = 0;
                while (++i <= 0x1000100) {
//                    int t = stateB << shift & 63;
                    int t = (stateB & 63) >>> shift;
                    stateC ^= stateA;
                    stateD ^= stateB;
                    stateB ^= stateC;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << rot | stateD >>> 6 - rot) & 63;

                    if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                        System.out.printf("right shift %d, rotl %d: 0x%08X\n", shift, rot, i);
                        if (i > best) {
                            best = i;
                            bestShift = shift;
                            bestRot = rot;
                        }
                        break;
                    }
                }
            }
        }
        System.out.printf("Best right shift was %d, best left rotation was %d, with period %06X\nTook %d ms.\n",
                bestShift, bestRot, best, (System.currentTimeMillis() - startTime));
    }

    /**
     * Best right shift was 2, best left rotation was 6, with period 0xFFFFFFF
     * Best left  shift was 2, best left rotation was 1, with period 0xFFFFFFF
     */
    @Test
    public void checkPeriod28_Xoshiro4x7(){
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestShift = 1, bestRot = 1;
        for (int shift = 1; shift < 7; shift++) {
            for (int rot = 1; rot < 7; rot++) {
                int i = 0;
                while (++i <= 0x10000100) {
                    int t = stateB << shift & 127;
//                    int t = stateB >>> shift;
                    stateC ^= stateA;
                    stateD ^= stateB;
                    stateB ^= stateC;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << rot | stateD >>> 7 - rot) & 127;

                    if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                        System.out.printf("right shift %d, rotl %d: 0x%08X\n", shift, rot, i);
                        if (i > best) {
                            best = i;
                            bestShift = shift;
                            bestRot = rot;
                        }
                        break;
                    }
                }
            }
        }
        System.out.printf("Best left shift was %d, best left rotation was %d, with period 0x%06X\nTook %d ms.\n",
                bestShift, bestRot, best, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x007FFFFFF80
     */
    @Test
    public void checkPeriodCountingByXoshiro4x7(){
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        int stateE = 1;
                long i = 0L;
                while (++i <= 0x1000000100L) {
                    int t = stateB >>> 2;
                    stateC ^= stateA;
                    stateD ^= stateB;
                    stateE = stateE + ~(stateB ^= stateC) & 127;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << 6 | stateD >>> 1) & 127;

                    if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1 && stateE == 1) {
                        break;
            }
        }
        System.out.printf("Period was 0x%011X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x007FFFFFF80 when XORing stateE into stateC.
     * Period was 0x007FFFFFF80 when XORing stateE into stateD.
     * Period was 0x007FFFFFF80 when XORing stateE with stateC at the second point, too.
     * Period was 0x007FFFFFF80 when XORing stateE with stateC, but using (stateE + 0x35 ^ 0x66) as the new value.
     * (Same for (stateE + 0x35 ^ 0x60).)
     * Period was 0x0023B8CB487 when doing {@code stateC ^= (stateE = stateE + stateA & 127);}
     * Period was 0x002B0B6FA00 when adding stateE.
     * When XORing stateE with stateD at the second point, something goes wrong and the generator goes into a subcycle.
     */
    @Test
    public void checkPeriodCounterInXoshiro4x7(){
        long startTime = System.currentTimeMillis();
        int stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        int stateE = -0x65 & 127;
                long i = 0L;
                while (++i <= 0x1000000100L) {
                    int t = stateB >>> 2;
                    stateC ^= stateA ^ (stateE = stateE + 0x65 & 127);
                    stateD ^= stateB;
                    stateB ^= stateC;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << 6 | stateD >>> 1) & 127;
                    if (stateA == 0 && stateB == 0 && stateC == 0 && stateD == 0 && stateE == (-0x65 & 127)) {
                        break;
            }
        }
        System.out.printf("Period was 0x%011X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Best left shift was 3, best left rotation was 1, with period FFFFFFFF
     */
    @Test
    public void checkPeriod32_Xoshiro4x8(){
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1;
        int bestShift = 1, bestRot = 1;
        for (int shift = 1; shift < 8; shift++) {
            for (int rot = 1; rot < 8; rot++) {
                long i = 0;
                while (++i <= 0x100000100L) {
                    int t = stateB << shift & 255;
                    stateC ^= stateA;
                    stateD ^= stateB;
                    stateB ^= stateC;
                    stateA ^= stateD;
                    stateC ^= t;
                    stateD = (stateD << rot | stateD >>> 8 - rot) & 255;

                    if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1) {
                        System.out.printf("left shift %d, rotl %d: 0x%08X\n", shift, rot, i);
                        if (i > best) {
                            best = i;
                            bestShift = shift;
                            bestRot = rot;
                        }
                        break;
                    }
                }
            }
        }
        System.out.printf("Best left shift was %d, best left rotation was %d, with period %08X\nTook %d ms.\n", bestShift, bestRot, best, (System.currentTimeMillis() - startTime));
    }
    /**
     * Period was 0x0FFFF00 for most tried, but some are much shorter.
     */
    @Test
    public void checkPeriodCountingByLFSR16() {
        long startTime = System.currentTimeMillis();
        short stateA = 1;
        byte stateE = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
//                    stateE += (stateA = (short)((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400))); // Period was 0x000FFFF
//                    stateE += ~(stateA = (short)((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400))); // Period was 0x0FFFF00
//                    stateE += ~(stateA = (short)( (stateA & 0x7FFF) << 1 ^ ((stateA >> 31) & 0x002D) )); // Period was 0x0FFFF00
            // Period was 0x0FFFF00
//            stateA = (short) ((stateA & 0x7FFF) << 1 ^ ((stateA >> 31) & 0x002D));
//            stateE += 0x97;

            // Period was 0x0FFFF00
            // This only permits increments to stateE that are an odd number, that is, the lowest-order bit is 1 .
            // Other increments have shorter periods.
            // This uses the "reverse direction" LFSR, but it doesn't seem to matter.
//            stateA = (short) ((stateA & 0x7FFF) << 1 ^ ((stateA >> 31) & 0x002D) ^ (stateE += 0x7B));

            // Period was 0x0FFFF00, increment to stateE must be odd.
//            stateA = (short) ((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400) ^ ((stateE += 0x81) & 255)); // mask not necessary.
            stateA = (short) ((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400) ^ (stateE += 0x61));

//                    stateE += Integer.numberOfLeadingZeros(stateA = (short)((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400))); // Period was 0x0FFFF00
            if (stateA == 1 && stateE == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    @Test
    public void checkPeriod32_Frog(){
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        long best = 1, worst = Long.MAX_VALUE;
        OUTER:
        for (int o = 0; o < 256; o++) {
            stateA = 1 + o & 255;
            stateB = 1;
            stateC = 1;
            stateD = 1;
            long i = 0;
            while (++i <= 0x1000000L) {
                int z = stateC + (stateA = stateA + 0xCD & 255) & 255;
                int w = stateD + (stateB = stateB + 0x91 & 255) & 255;
                stateC = stateA + (w ^ rotate8(w, 3) ^ rotate8(w, 4)) & 255;
                stateD = stateB + (z ^ rotate8(z, 2) ^ rotate8(z, 7)) & 255;

                if (stateA == (1 + o & 255) && stateB == 1 && stateC == 1 && stateD == 1) {
                    System.out.printf("With offset %d, 0x%08X\n", o, i);
                    if (i > best) {
                        best = i;
                    }
                    if (i < worst) {
                        worst = i;
                    }
                    continue OUTER;
                }
            }
            System.out.printf("Something is wrong with offset %d; states are %02X %02X %02X %02X\n", o, stateA, stateB, stateC, stateD);
        }
        System.out.printf("Best period was %08X", best);
        System.out.printf("Worst period was %08X", worst);
    }

    /**
     * This at least shows that the bottom 4 bits of a combined xoshiro ^ weyl sequence generator are very close to
     * 4-dimensionally equidistributed. This only shows 256 4-tuples, and there are 2^32, so combined with the later
     * results we know that the results are only very close to 4D equidistributed, but should be 1D for sure.
     * <br>
     * Bottom 4 bits output, increment 5:
     * <pre>
     *          0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         10: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         20: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         30: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         40: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         50: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         60: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         70: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         80: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         90: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         A0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         B0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         C0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         D0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         E0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         F0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     * </pre>
     * Upper 5 bits output, increment 1:
     * <pre>
     *          0: 000003FFFF 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         10: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         20: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         30: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         40: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         50: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         60: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         70: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         80: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         90: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         A0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         B0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         C0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         D0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         E0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *         F0: 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     *             0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000 0000040000
     * </pre>
     * Upper 4 bits output, increment 1:
     * <pre>
     *          0: 00003FFFFD 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         10: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         20: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         30: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         40: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         50: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         60: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         70: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         80: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         90: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         A0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         B0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         C0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         D0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         E0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *         F0: 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000
     *             0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 0000400000 00003FFFFF
     * </pre>
     * @param args
     */
//    public void checkDistributionOverload40(String[] args) {
    public static void main(String[] args) {
        int[] counts = new int[0x10000];
//        int[] counts = new int[0x100000];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1, t, join;
        final int inc = 1;
        System.out.printf("Starting:\nstateA=%08X, stateB=%08X, stateC=%08X, stateD=%08X, stateE=%08X\n", stateA, stateB, stateC, stateD, stateE);
        long total = 0, startTime = System.currentTimeMillis();
        for (long i = 0; i < 0x3FFFFFFFC0L; i++) {
//            join = (stateB) >>> 4 & 0x000F;
            join = (stateB ^ (stateE = stateE + inc & 255)) >>> 4 & 0x000F;
//            join = (stateB ^ (stateE = stateE + inc & 255)) >>> 3 & 0x0001F;
            t = stateB << 3 & 255;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 7) & 255;
//            join |= (stateB) & 0x00F0;
            join |= (stateB ^ (stateE = stateE + inc & 255)) & 0x00F0;
//            join |= (stateB ^ (stateE = stateE + inc & 255)) << 2 & 0x003E0;
            t = stateB << 3 & 255;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 7) & 255;
//            join |= (stateB) << 4 & 0x0F00;
            join |= (stateB ^ (stateE = stateE + inc & 255)) << 4 & 0x0F00;
//            join |= (stateB ^ (stateE = stateE + inc & 255)) << 7 & 0x07C00;
            t = stateB << 3 & 255;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 7) & 255;
//            counts[join | ((stateB) << 8 & 0xF000)]++;
            counts[join | ((stateB ^ (stateE = stateE + inc & 255)) << 8 & 0xF000)]++;
//            counts[join | ((stateB ^ (stateE = stateE + inc & 255)) << 12 & 0xF8000)]++;
            t = stateB << 3 & 255;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 7) & 255;
            total += 4;
        }
        System.out.println("Generated: " + total);
        System.out.println("Took " + (System.currentTimeMillis() - startTime) * 0.001 + " seconds.");
        System.out.printf("Ending:\nstateA=%08X, stateB=%08X, stateC=%08X, stateD=%08X, stateE=%08X\n", stateA, stateB, stateC, stateD, stateE);
        for (int i = 0; i < 0x10000;) {
            System.out.printf("%10X: %010X %010X %010X %010X %010X %010X %010X %010X\n            %010X %010X %010X %010X %010X %010X %010X %010X\n", i
                    , counts[i++], counts[i++], counts[i++], counts[i++]
                    , counts[i++], counts[i++], counts[i++], counts[i++]
                    , counts[i++], counts[i++], counts[i++], counts[i++]
                    , counts[i++], counts[i++], counts[i++], counts[i++]
            );
        }
    }


    @Test
    public void checkPeriod32_MWC(){
        final int initial = 1;
        int state = initial;
        long i = 0;
        while (++i < 0x100000001L) {
            state = 36969 * (state & 0xFFFF) + (state >>> 16);
            if (state == initial) {
                System.out.printf("MWC: 0x%08X\n", i);
                break;
            }
        }
    }

    /**
     * Best multiplier for an 8-bit result MWC generator is 249, with a period of 0x7C7F. Not equidistributed at all.
     */
    @Test
    public void optimize16_MWC(){
        final short initial = 1;
        int bestMul = 1, bestPeriod = 0;
        for (int m = 2; m < 256; m++) {
            short state = initial;
            int i = 0;
            while (++i < 0x10001L) {
                state = (short) ((0xFFFF & m * (state & 0xFF)) + (state >>> 8 & 0xFF));
                if (state == initial) {
                    System.out.printf("multiplier=%3d has period=0x%04X\n", m, i);
                    if(i > bestPeriod) {
                        bestMul = m;
                        bestPeriod = i;
                    }
                    break;
                }
            }
        }
        System.out.printf("Best multiplier=%d with period=0x%04X\n", bestMul, bestPeriod);
    }

    @Test
    public void checkPeriod64() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                //state += state >>> 48;
                if ((
                        //state -= (m << 35 | m >>> 29)
//                        state = 0xC6BC279692B5CC8BL - (state << 45 | state >>> 19) // gets to 10438 iterations
                        state =   0xC6BC279692B5CC8BL - (state << 35 | state >>> 29) // gets past all 65536 iterations
                        //state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
        System.out.printf("(state = 0xC6BC279692B5CC8BL - (state << 45 | state >>> 19)): 0x%08X\n", i);
//        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }

    @Test
    public void checkPeriod64_cers() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                //state += state >>> 48;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        state = 0x5320B74ECA44ADADL - (state << 23 | state >>> 41)
                        //state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
        System.out.printf("(state = 0x5320B74ECA44ADADL - (state << 23 | state >>> 41)): 0x%08X\n", i);
//        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }
    @Test
    public void checkPeriod64_iadla() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                state += state >>> 47;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                        state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
//        System.out.printf("(state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)): 0x%08X\n", i);
        System.out.printf("(state += state >>> 47; state += state << 16): 0x%08X\n", i);
    }
    @Test
    public void checkPeriod64_cmr() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                state *= 0x41C64E6BL;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                        state = (state << 28 | state >>> 36) // gets to at least 58320 iterations
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
//        System.out.printf("(state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)): 0x%08X\n", i);
        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }
    ///////// BEGIN subcycle finder code and period evaluator
    public static void subcycleFinder(String[] args)
//    @Test
//    public void testSubcycle32()
    {
        // multiplying
        // A refers to 0x9E377
        // A 10 0xC010AEB4
        // B refers to 0x64E6D
        // B 22 0x195B9108
        // all  0x04C194F3485D5A68

        // A=Integer.rotateLeft(A*0x9E377, 17) 0xF7F87D28
        // B=Integer.rotateLeft(A*0x64E6D, 14) 0xF023E25B 
        // all  0xE89BB7902049CD38


        // A11 B14 0xBBDA9763B6CA318D
        // A8  B14 0xC109F954C76CB09C
        // A17 B14 0xE89BB7902049CD38
//        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
//        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("0x%016X\n", result.longValue());
        // 0x9E37
        // rotation 27: 0xEE06F34D
        // 0x9E35
        // rotation 6 : 0xE1183C3A
        // rotation 19: 0xC4FCFC55
        // 0x9E3B
        // rotation 25: 0xE69313ED
        // 0xDE4D
        // rotation 3 : 0xF6C16607
        // rotation 23: 0xD23AD58D
        // rotation 29: 0xC56DC41F
        // 0x1337
        // rotation 7:  0xF41BD009
        // rotation 20: 0xF5846878
        // rotation 25: 0xF38658F9
        // 0xACED
        // rotation 28: 0xFC98CC08
        // rotation 31: 0xFA18CD57
        // 0xBA55
        // rotation 19: 0xFB059E43
        // 0xC6D5
        // rotation 05: 0xFFD78FD4
        // 0x5995
        // rotation 28: 0xFF4AB87D
        // rotation 02: 0xFF2AA5D5
        // 0xA3A9
        // rotation 09: 0xFF6B3AF7
        // 0xB9EF
        // rotation 23: 0xFFAEB037
        // 0x3D29
        // rotation 04: 0xFF6B92C5
        // 0x5FAB
        // rotation 09: 0xFF7E3277 // seems to be very composite
        // 0xCB7F
        // rotation 01: 0xFF7F28FE
        // 0x89A7
        // rotation 13: 0xFFFDBF50 // wow! note that this is a multiple of 16
        // 0xBCFD
        // rotation 17: 0xFFF43787 // second-highest yet, also an odd number
        // 0xA01B
        // rotation 28: 0xFFEDA0B5
        // 0xC2B9
        // rotation 16: 0xFFEA9001

        // slightly larger multiplications, GWT-safe
        //// 0xFFFFD5A9 0xFFFF687B 0xFFFF2E45 0xFFFF1928 0xFFFEE5E1 0xFFFEAB53 0xFFFE9CA7
        // (a = (a << 23 | a >>> 9) * 0x402AB) ^ (b = (b << 28 | b >>> 4) * 0x01621) ^
        // (c = (c << 24 | c >>> 8) * 0x808E9) ^ (d = (d << 29 | d >>> 3) * 0x8012D)
        // 0x402AB, rotation 23: 0xFFFFD5A9
        // 0x01621, rotation 28: 0xFFFF687B
        // 0x808E9, rotation 24: 0xFFFF2E45
        // 0x40809, rotation 05: 0xFFFF1928
        // 0xC134D, rotation 31: 0xFFFF04B2
        // 0x81411, rotation 09: 0xFFFEF47E
        // 0xC15DF, rotation 28: 0xFFFEE5E1
        // 0x8012D, rotation 29: 0xFFFEAB53
        // 0xC122D, rotation 19: 0xFFFE9CA7
        // 0x02323, rotation 24: 0xFFFE9916
        // 0x82245, rotation 02: 0xFFFE7A52
        // 0xC22C7, rotation 21: 0xFFFE6D98
        // 0x420DD, rotation 18: 0xFFFE3E93
        // 0x01F59, rotation 28: 0xFFFDFE3F
        // 0x01681, rotation 16: 0xFFFDEE49
        // 0x01ADD, rotation 21: 0xFFFDBC12
        // 0x41B85, rotation 13: 0xFFFD9F0D
        // 0x805D1, rotation 13: 0xFFFD966F
        // 0x0023F, rotation 21: 0xFFFD9550
        // 0xC14B7, rotation 26: 0xFFFD94BD
        // 0xC20AF, rotation 01: 0xFFFD7A4A
        // 0x80455, rotation 22: 0xFFFD7792
        // 0x40113, rotation 09: 0xFFFD71A1
        // 0x4062D, rotation 16: 0xFFFD43FA
        // 0x016CB, rotation 23: 0xFFFD3678
        // 0x41625, rotation 01: 0xFFFD23A9
        // 0x00D09, rotation 19: 0xFFFD015D


        // adding
        // 0x9E3779B9
        // rotation 2 : 0xFFCC8933
        // rotation 7 : 0xF715CEDF
        // rotation 25: 0xF715CEDF
        // rotation 30: 0xFFCC8933
        // 0x6C8E9CF5
        // rotation 6 : 0xF721971A
        // 0x41C64E6D
        // rotation 13: 0xFA312DBF
        // rotation 19: 0xFA312DBF
        // rotation 1 : 0xF945B8A7
        // rotation 31: 0xF945B8A7
        // 0xC3564E95
        // rotation 1 : 0xFA69E895 also 31
        // rotation 5 : 0xF2BF5E23 also 27
        // 0x76BAF5E3
        // rotation 14: 0xF4DDFC5A also 18
        // 0xA67943A3 
        // rotation 11: 0xF1044048 also 21
        // 0x6C96FEE7
        // rotation 2 : 0xF4098F0D
        // 0xA3014337
        // rotation 15: 0xF3700ABF also 17
        // 0x9E3759B9
        // rotation 1 : 0xFB6547A2 also 31
        // 0x6C8E9CF7
        // rotation 7 : 0xFF151D74 also 25
        // rotation 13: 0xFD468E2B also 19
        // rotation 6 : 0xF145A7EB also 26
        // 0xB531A935
        // rotation 13: 0xFF9E2F67 also 19
        // 0xC0EF50EB
        // rotation 07: 0xFFF8A98D also 25
        // 0x518DC14F
        // rotation 09: 0xFFABD755 also 23 // probably not prime
        // 0xA5F152BF
        // rotation 07: 0xFFB234B2 also 25
        // 0x8092D909
        // rotation 10: 0xFFA82F7C also 22
        // 0x73E2CCAB
        // rotation 09: 0xFF9DE8B1 also 23
        // 0x98EA52FD
        // rotation 07: 0xFFEBC6C7 also 25
        // 0x87194377
        // rotation 03: 0xFFF4D1C8 also 29
        // 0x9504583F
        // rotation 15: 0xFFFA683A also 17
        // 0x8F2554F7
        // rotation 11: 0xFFFA5B00 also 21
        // 0xC91ED343
        // rotation 10: 0xFFF8F071 also 22
        // 0x847A859B
        // rotation 15: 0xFFF8BBBC also 17
        // 0xC4DE9951
        // rotation 07: 0xFFFEEAA9 also 25
        // 0xAA78EDD7
        // rotation 01: 0xFFFF9C61 also 31
        // 0x929E7143
        // rotation 14: 0xFFFA8F94 also 18
        // stateB = rotate32(stateB + 0xB531A935, 13)
        // stateC = rotate32(stateC + 0xC0EF50EB, 7)

        // to get period of 0xFFFF9C61:
        // a = rotate32(a, 1) + 0xAA78EDD7
        // to get period of 0xFFFEEAA9:
        // b = rotate32(b, 25) + 0xC4DE9951


        // subtracting, rotating, and bitwise NOT:
        // 0xC68E9CF3
        // rotation 13: 0xFEF97E17, also 19 
        // 0xC68E9CB7
        // rotation 12: 0xFE3D7A2E

        // left xorshift
        // 5
        // rotation 15: 0xFFF7E000
        // 13
        // rotation 17: 0xFFFD8000

        // minus left shift, then xor
        // state - (state << 12) ^ 0xC68E9CB7, rotation 21: 0xFFD299CB
        // add xor
        // state + 0xC68E9CB7 ^ 0xDFF4ECB9, rotation 30: 0xFFDAEDF7
        // state + 0xC68E9CB7 ^ 0xB5402ED7, rotation 01: 0xFFE73631
        // state + 0xC68E9CB7 ^ 0xB2B386E5, rotation 24: 0xFFE29F5D
        // sub xor
        // state - 0x9E3779B9 ^ 0xE541440F, rotation 22: 0xFFFC9E3E


        // best power of two:
        // can get 63.999691 with: (period is 0xFFF1F6F18B2A1330)
        // multiplying A by 0x89A7 and rotating left by 13
        // multiplying B by 0xBCFD and rotating left by 17
        // can get 63.998159 with: (period is 0xFFAC703E2B6B1A30)
        // multiplying A by 0x89A7 and rotating left by 13
        // multiplying B by 0xB9EF and rotating left by 23
        // can get 63.998 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // xorshifting B left by 5 (B ^ B << 5) and rotating left by 15
        // can get 63.99 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // adding 0x6C8E9CF7 for B and rotating left by 7
        // can get 63.98 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // multiplying by 0xACED, NOTing, and rotating left by 28 for B
        // 0xFF6B3AF7L 0xFFAEB037L 0xFFD78FD4L

        // 0xFF42E24AF92DCD8C, 63.995831
        //BigInteger result = BigInteger.valueOf(0xFF6B3AF7L), tmp = BigInteger.valueOf(0xFFD78FD4L);

        int stateA, i;
        int r = 0x80001;
        for (int c = 1; c <= 262144; c++) {
            r = ((int)DiverRNG.determine(c) >>> 11) | 1;
//            a = (int)DiverRNG.determine(r + c) | 1;
            System.out.printf("%05d/262144, testing r = 0x%08X\n", c, r);
//            System.out.printf("%05d/262144, testing r = 0x%08X, a = 0x%08X\n", c, r, a);
            for (int j = 1; j < 32; j++) {
                i = 1;
                stateA = 1;
                for (; ; i++) {
                    if ((stateA = Integer.rotateLeft(stateA, j) * r) == 1) {
//                    if ((stateA = stateA * r + Integer.rotateLeft(a, j)) == 1) {
                        if (i >>> 20 == 0xFFF || i == 0)
                            System.out.printf("state = rotl(state, %02d) * 0x%08X): 0x%08X\n", j, r, i);
                        break;
                    }
                }
            }
        }

//        ExecutorService executor = Executors.newFixedThreadPool(4);
//        try {
//            executor.invokeAll(Arrays.asList(cmr32Runner(1), cmr32Runner(0x40001), cmr32Runner(0x80001), cmr32Runner(0xC0001)));
//            executor.awaitTermination(10, TimeUnit.DAYS); // it won't take this long
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        int target;
//            for (int s = 1; s < 32; s++) {
//                for (int r = 1; r < 32; r++) {
////            for (int s : new int[]{5, 9, 27}) {
////                for (int r : new int[]{1, 8, 20}) {
//                    System.out.printf("testing (x << %d) + rotl(x, %d) ... ", s, r);
//                    stateA = 1;
//                    i = 0;
//                    for (int j = 0; j < 0x100000; j++) {
//                        stateA = (stateA << s) + Integer.rotateLeft(stateA, r);
//                    }
//                    target = stateA;
//                    System.out.printf("target is 0x%08X, ", target);
//                    for (; ; ) {
//                        if ((stateA = (stateA << s) + Integer.rotateLeft(stateA, r)) == target) {
//                            System.out.printf("period is 0x%08X\n", i);
//                            break;
//                        }
//                        if (++i == 0) {
//                            System.out.printf("cycled strangely, state ended on 0x%08X\n", stateA);
//                            break;
//                        }
//                    }
//                }
//            }


//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }         
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());

    }

    private static Callable<Integer> cmr32Runner(final int start)
    {
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int stateA, i;
                int r = start;
                for (int c = 1; c <= 131072; c++) {
                    r += 2;
                    System.out.printf("%05d/131072, testing r = 0x%08X\n", c, r);
                    for (int j = 1; j < 32; j++) {
                        i = 0;
                        stateA = 1;
                        for (; ; i++) {
                            if ((stateA = Integer.rotateLeft(stateA, j) * r) == 1) {
                                if (i >>> 20 == 0xFFF)
                                    System.out.printf("state * 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
                                break;
                            }
                        }
                    }
                }
                return r;
            }
        };
    }

    /////// END subcycle finder code and period evaluator
    @Test
    public void showCombined()
    {
        //// 0xFFFFD5A9 0xFFFF687B 0xFFFF2E45 0xFFFF1928 0xFFFEE5E1 0xFFFEAB53 0xFFFE9CA7
        // add 0xFFFF9C61L 0xFFFEEAA9L
        // mul 0xFFFDBF50L 0xFFF43787L 0xFFFD3B83L 0xFFF60EDDL : 127.999411
        BigInteger result = BigInteger.valueOf(0xFFFF9C61L), tmp = BigInteger.valueOf(0xFFFEEAA9L); // 5/1, 6/-4
//        BigInteger result = BigInteger.valueOf(0xFF8F603FL), tmp = BigInteger.valueOf(0xFD6D7E76L); // 5/1, 9/8 
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFF2E45L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFEAB53L);
//        tmp = BigInteger.valueOf(0xFDD16277L); // 27/-14
//        tmp = BigInteger.valueOf(0xFBD0F379L); // 27/20
        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        BigInteger result = BigInteger.valueOf(0xFFFDBF50L), tmp = BigInteger.valueOf(0xFFFD3B83L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFD3B83L); //mul 0xFFEDA0B5L //add 0xFFF8A98DL
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFF60EDDL); //add 0xFFF8A98DL
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
//        tmp = BigInteger.valueOf(0xFFABD755L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
    }
    @Test
    public void testSubcycle64()
    {
//        long i;
//        long state = 1L;
//        LinnormRNG lin = new LinnormRNG(2L);//3676505223501568873L
//        System.out.println(lin.getState());
        //Random rand = new RNG(lin).asRandom();
        //for (int c = 1; c <= 200; c++) {
            //final int r = lin.nextInt()|1;
//        final long r = 0x7FFFFFFFL;//0x41C64E6BL;//0x41C64E6D;
//            //final int r = BigInteger.probablePrime(32, rand).intValue();
//            //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//        //System.out.printf("%03d/200, testing r = 0x%08X\n", c, r);
//        System.out.printf("testing r = 0x%08X\n", r);
//        for (int j = 3; j < 64; j++) {
//            i = 0L;
//            state = 1L;
//            OUTER:
//            for (; i < 0x40000000000L; ) {
//                for (int k = 0x80000000; k < 0; k++) {
//                    if ((state = (state << j | state >>> -j) * 0x7FFFFFFFL) == 1L) {
//                        //if (i > 0x100000000L)
//                        System.out.printf("state * 0x%08X, rotation %02d: 0x%016X\n", r, j, i);
//                        break OUTER;
//                    }
//                    i++;
//                }
//                System.out.printf("Period is at least 0x%016X\n", i);
//            }
//            System.out.printf("state * 0x%08X, rotation %02d: 0x%016X\n", r, j, i);
//        }
        //}

//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());
        long stateA, i, target;
//        for (int c = 1; c <= 200; c++) {
        //final int r = (Light32RNG.determine(20007 + c) & 0xFFFF)|1;
//            final int r = BigInteger.probablePrime(31, rand).intValue();
        //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//            for (int s = 1; s < 32; s++) {
//                for (int r = 1; r < 32; r++) {
        for (int s : new int[]{5, 9, 27}) {
            for (int r : new int[]{1, 8, 20}) {
                System.out.printf("testing (x << %d) + rotl(x, %d) ...\n", s, r);
                stateA = 1L;
                i = 0L;
                for (int j = 0; j < 0x100000; j++) {
                    stateA = (stateA << s) + Long.rotateLeft(stateA, r);
                }
                target = stateA;
                System.out.printf("target is 0x%08X,\n", target);
                for (; ; ) {
                    if ((stateA = (stateA << s) + Long.rotateLeft(stateA, r)) == target) {
                        System.out.printf("period is 0x%08X\n", i);
                        break;
                    }
                    if (++i == 0) {
                        System.out.printf("cycled strangely, state ended on 0x%08X\n", stateA);
                        break;
                    }
                    else if((i & 0xFFFFFFFFL) == 0L)
                    {
                        System.out.printf("Currently at 0x%016X\n", i);
                    }
                }
            }
        }
    }
    // MiniMover64RNG good-period range finder
    //public static void main(String[] args)
    @Test
    public void miniRangeFinder()
    {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            executor.invokeAll(Arrays.asList(runner(0), runner(0x800000), runner(0x1000000), runner(0x1800000)));
            executor.awaitTermination(10, TimeUnit.DAYS); // it won't take this long
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static Callable<Long> runner(final long start)
    {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long state, ctr = start;
                for (int i = 0; i < 0x80; i++) {
                    for (int j = 0; j < 0x10000; j++) {
                        state = ++ctr;
                        for (int p = 0; p < 0x100000; p++) {
//                    state = (state << 21 | state >>> 43) * 0x9E3779B9L;
                            state = (state << 29 | state >>> 35) * 0xAC564B05L;
                            if (state == ctr) {
                                System.out.println(ctr + " is NOT OKAY, period cycles at " + p);
                                throw new InterruptedException("period cycles at " + p);
                            }
                        }
                        //System.out.println("Seed of " + i + " is okay");
                    }
                    System.out.println("Successfully checked seeds " + (ctr - 0x10000) + " to " + ctr);
                }
                return ctr;
            }
        };
    }
}
