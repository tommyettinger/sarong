package sarong;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntIntOrderedMap;
import com.github.tommyettinger.ds.support.sort.IntComparator;
import com.github.tommyettinger.ds.support.sort.IntComparators;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sarong.TestDistribution.clz8;

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

    /**
     * ~Integer.rotateLeft(stateA, 1): 0x00000020
     * ~Integer.rotateLeft(stateA, 2): 0x0000002F
     * ~Integer.rotateLeft(stateA, 3): 0x0000004E
     * ~Integer.rotateLeft(stateA, 4): 0x00000055
     * ~Integer.rotateLeft(stateA, 5): 0x00000074
     * ~Integer.rotateLeft(stateA, 6): 0x00000083
     * ~Integer.rotateLeft(stateA, 7): 0x000000A2
     * ~Integer.rotateLeft(stateA, 8): 0x000000A5
     * ~Integer.rotateLeft(stateA, 9): 0x000000C4
     * ~Integer.rotateLeft(stateA, 10): 0x000000D3
     * ~Integer.rotateLeft(stateA, 11): 0x000000F2
     * ~Integer.rotateLeft(stateA, 12): 0x000000F9
     * ~Integer.rotateLeft(stateA, 13): 0x00000118
     * ~Integer.rotateLeft(stateA, 14): 0x00000127
     * ~Integer.rotateLeft(stateA, 15): 0x00000146
     * ~Integer.rotateLeft(stateA, 16): 0x00000147
     * ~Integer.rotateLeft(stateA, 17): 0x00000166
     * ~Integer.rotateLeft(stateA, 18): 0x00000175
     * ~Integer.rotateLeft(stateA, 19): 0x00000194
     * ~Integer.rotateLeft(stateA, 20): 0x0000019B
     * ~Integer.rotateLeft(stateA, 21): 0x000001BA
     * ~Integer.rotateLeft(stateA, 22): 0x000001C9
     * ~Integer.rotateLeft(stateA, 23): 0x000001E8
     * ~Integer.rotateLeft(stateA, 24): 0x000001EB
     * ~Integer.rotateLeft(stateA, 25): 0x0000020A
     * ~Integer.rotateLeft(stateA, 26): 0x00000219
     * ~Integer.rotateLeft(stateA, 27): 0x00000238
     * ~Integer.rotateLeft(stateA, 28): 0x0000023F
     * ~Integer.rotateLeft(stateA, 29): 0x0000025E
     * ~Integer.rotateLeft(stateA, 30): 0x0000026D
     * ~Integer.rotateLeft(stateA, 31): 0x0000028C
     */
    @Test
    public void checkPeriod32_Weird(){
        int stateA = 1;
        int i = 1;
        EACH_A:
        for (int a = 1; a < 32; a++) {
            for (; i != 0; i++) {
                if ((stateA = ~Integer.rotateLeft(stateA, a)) == 1) {
                    System.out.printf("~Integer.rotateLeft(stateA, %d): 0x%08X\n", a, i);
                    continue EACH_A;
                }
            }
            System.out.printf("~Integer.rotateLeft(stateA, %d): FULL PERIOD\n", a);
        }
    }

    /**
     * I have no idea what exactly is happening here...
     * <br>
     * stateA + 1 ^ 1: 0x80000000
     * stateA + 1 ^ 2: BAD SUBCYCLE... Ended on 0x80000000
     * stateA + 1 ^ 3: BAD SUBCYCLE... Ended on 0x80000002
     * stateA + 1 ^ 4: BAD SUBCYCLE... Ended on 0x80000007
     * stateA + 1 ^ 5: BAD SUBCYCLE... Ended on 0x8000000D
     * stateA + 1 ^ 6: BAD SUBCYCLE... Ended on 0x80000008
     * stateA + 1 ^ 7: BAD SUBCYCLE... Ended on 0x8000000E
     * stateA + 1 ^ 8: BAD SUBCYCLE... Ended on 0x80000007
     * stateA + 1 ^ 9: BAD SUBCYCLE... Ended on 0x80000001
     * stateA + 1 ^ 10: BAD SUBCYCLE... Ended on 0x80000008
     * stateA + 1 ^ 11: BAD SUBCYCLE... Ended on 0x80000002
     * stateA + 1 ^ 12: BAD SUBCYCLE... Ended on 0x8000000F
     * stateA + 1 ^ 13: BAD SUBCYCLE... Ended on 0x8000001D
     * stateA + 1 ^ 14: BAD SUBCYCLE... Ended on 0x80000010
     * stateA + 1 ^ 15: BAD SUBCYCLE... Ended on 0x8000001E
     * stateA + 1 ^ 16: BAD SUBCYCLE... Ended on 0x8000000F
     * stateA + 1 ^ 17: BAD SUBCYCLE... Ended on 0x80000001
     * stateA + 1 ^ 18: BAD SUBCYCLE... Ended on 0x80000010
     * stateA + 1 ^ 19: BAD SUBCYCLE... Ended on 0x80000002
     * stateA + 1 ^ 20: BAD SUBCYCLE... Ended on 0x80000017
     * stateA + 1 ^ 21: BAD SUBCYCLE... Ended on 0x8000000D
     * stateA + 1 ^ 22: BAD SUBCYCLE... Ended on 0x80000018
     * stateA + 1 ^ 23: BAD SUBCYCLE... Ended on 0x8000000E
     * stateA + 1 ^ 24: BAD SUBCYCLE... Ended on 0x80000017
     * stateA + 1 ^ 25: BAD SUBCYCLE... Ended on 0x80000001
     * stateA + 1 ^ 26: BAD SUBCYCLE... Ended on 0x80000018
     * stateA + 1 ^ 27: BAD SUBCYCLE... Ended on 0x80000002
     * stateA + 1 ^ 28: BAD SUBCYCLE... Ended on 0x8000001F
     * stateA + 1 ^ 29: BAD SUBCYCLE... Ended on 0x8000003D
     * stateA + 1 ^ 30: BAD SUBCYCLE... Ended on 0x80000020
     * stateA + 1 ^ 31: BAD SUBCYCLE... Ended on 0x8000003E
     */
    @Test
    public void checkPeriod32_AddXor(){
        int stateA = 1;
        long i = 1;
        EACH_A:
        for (int a = 1; a < 32; a++) {
            for (; i != 0x100000000L; i++) {
                if ((stateA = stateA + 1 ^ a) == 1) {
                    System.out.printf("stateA + 1 ^ %d: 0x%08X\n", a, i);
                    continue EACH_A;
                }
            }
            if ((stateA = stateA + 1 ^ a) == 1)
                System.out.printf("stateA + 1 ^ %d: FULL PERIOD! WOOHOO!!!!!!!\n", a);
            else
                System.out.printf("stateA + 1 ^ %d: BAD SUBCYCLE... Ended on 0x%08X\n", a, stateA);
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
     * {@code stateA = (stateB = rotate8(stateB, 6) ^ (stateC = stateC + 0xD3 & 255)) + rotate8(stateA, 3) & 255;}
     * Worst cycle was 256 with states 0 0 164, appearing 65536 times; best cycle was 33280
     * <br>
     * {@code stateA = (stateB = rotate8(stateB, 6) ^ (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) & 255;}
     * Worst cycle was 256 with states 0 0 172, appearing 62464 times; best cycle was 1024
     * <br>
     * {@code stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) & 255;}
     * Worst cycle was 768 with states 0 88 59, appearing 1176 times; best cycle was 5525248
     * <br>
     * {@code stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) + 1 & 255;}
     * Worst cycle was 768 with states 0 187 106, appearing 780 times; best cycle was 2444800
     */
    @Test
    public void checkWorstPeriod24_Thrash(){
        int stateA = 1, stateB = 1, stateC = 1, wa = 0, wb = 0, wc = 0;
        long worst = 0x1000000, best = 0;
        int numWorst = 0;
        for (int sa = 0; sa < 256; sa++) {
            for (int sb = 0; sb < 256; sb++) {
                for (int sc = 0; sc < 256; sc++) {
                    stateA = sa;
                    stateB = sb;
                    stateC = sc;
                    long i = 0;
                    while (++i <= worst) {
//                        stateA = (stateB = rotate8(stateB, 6) ^ (stateC = stateC + 0xD3 & 255)) + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) ^ (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) & 255;
                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xB5 & 255)) + rotate8(stateA, 3) + 1 & 255;
                        if (stateA == sa && stateB == sb && stateC == sc) {
                            best = Math.max(best, i);
                            if (i < worst) {
                                worst = i;
                                wa = sa;
                                wb = sb;
                                wc = sc;
                                numWorst = 1;
                            } else if(i == worst)
                                numWorst++;
                            break;
                        }
                    }
                }

            }
        }
        System.out.printf("Worst cycle was %d with states %d %d %d, appearing %d times; best cycle was %d\n", worst, wa, wb, wc, numWorst, best);
    }


    /**
     * SoloRandom verbatim:
     * Worst cycle was 256 with states 0 62 72, appearing 256 times; best cycle was 2597888
     * <br>
     * Without incorporating c twice:
     * Worst cycle was 256 with states 1 233 66, appearing 256 times; best cycle was 2582784
     * <br>
     * Adding only, but using stateC increment of 0xD5:
     * Worst cycle was 1792 with states 0 20 27, appearing 3584 times; best cycle was 6132736
     * <br>
     * Adding only, and also adding 5 to stateA's next value:
     * Worst cycle was 768 with states 0 53 170, appearing 1536 times; best cycle was 3232256
     * <br>
     * Adding 3 instead of 5:
     * Worst cycle was 1024 with states 0 16 213, appearing 3072 times; best cycle was 3428608
     * <br>
     * Adding 5, but stateC increment is 0xD5:
     * Worst cycle was 2560 with states 0 20 203, appearing 2560 times; best cycle was 14290432
     * <br>
     * Adding 3 instead of 5, but stateC increment is 0xD5: (!)
     * Worst cycle was 3328 with states 0 12 51, appearing 6656 times; best cycle was 13885440
     * <br>
     * Adding 1 instead of 5, but stateC increment is 0xD5:
     * Worst cycle was 6656 with states 0 2 38, appearing 6656 times; best cycle was 4802048
     * <br>
     * Adding 1 instead of 5:
     * Worst cycle was 256 with states 0 75 149, appearing 256 times; best cycle was 3594752
     * <br>
     * Adding 3 to stateB is about the same...
     * Worst cycle was 256 with states 1 233 63, appearing 256 times; best cycle was 2582784
     * <br>
     * Adding 6 to stateB:
     * Worst cycle was 256 with states 1 233 60, appearing 256 times; best cycle was 4782848
     * <br>
     * Adding 2 to stateB:
     * Worst cycle was 256 with states 1 233 64, appearing 256 times; best cycle was 117248
     * <br>
     * Adding 1 to stateB:
     * Worst cycle was 256 with states 1 233 65, appearing 256 times; best cycle was 4782848
     * <br>
     * Without incorporating c twice, using XOR between stateB and rotated stateC, and increment 0xD5:
     * Worst cycle was 256 with states 0 1 93, appearing 67840 times; best cycle was 36864
     */
    @Test
    public void checkWorstPeriod24_Solo(){
        int stateA = 1, stateB = 1, stateC = 1, wa = 0, wb = 0, wc = 0;
        long worst = 0x1000000, best = 0;
        int numWorst = 0;
        for (int sa = 0; sa < 256; sa++) {
            for (int sb = 0; sb < 256; sb++) {
                for (int sc = 0; sc < 256; sc++) {
                    stateA = sa;
                    stateB = sb;
                    stateC = sc;
                    long i = 0;
                    while (++i <= worst) {
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD3 & 255) & 255) ^ (stateC + rotate8(stateA, 3) & 255);
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD3 & 255) & 255) + rotate8(stateA, 3) & 255;
                        stateA = (stateB = rotate8(stateB, 6) ^ (stateC = stateC + 0xD5 & 255)) + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD3 & 255) & 255) + 5 + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD3 & 255) & 255) + 3 + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD5 & 255) & 255) + 3 + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD5 & 255) & 255) + 1 + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD5 & 255) & 255) + 5 + rotate8(stateA, 3) & 255;
//                        stateA = (stateB = rotate8(stateB, 6) + (stateC = stateC + 0xD5 & 255) & 255) + rotate8(stateA, 3) & 255;
                        if (stateA == sa && stateB == sb && stateC == sc) {
                            best = Math.max(best, i);
                            if (i < worst) {
                                worst = i;
                                wa = sa;
                                wb = sb;
                                wc = sc;
                                numWorst = 1;
                            } else if(i == worst)
                                numWorst++;
                            break;
                        }
                    }
                }

            }
        }
        System.out.printf("Worst cycle was %d with states %d %d %d, appearing %d times; best cycle was %d\n", worst, wa, wb, wc, numWorst, best);
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
     * Period was 0x01FFFFE0
     * Took 2005 ms.
     * 33554400/33554432 outputs were present.
     * 9.5367431640625E-5% of outputs were missing.
     * 0 0 0 0 0
     * 0 0 0 0 1
     * 0 0 0 0 2
     * 0 0 0 0 3
     * 0 0 0 0 4
     */
    @Test
    public void checkPeriodCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final RoaringBitmap all = new RoaringBitmap();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        int stateE = 1;
        long i = 0L;
        while (++i <= 0x10000100L) {
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.add(stateA | stateB << 5 | stateC << 10 | stateD << 15 | stateE << 20);
            if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1 && stateE == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.getLongCardinality() + "/" + (1 << 25) + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-25 + "% of outputs were missing.");
        all.flip(0L, 1L << 25);
        all.forEach((int ii) -> System.out.printf("%d %d %d %d %d\n", ii & 31, ii >>> 5 & 31, ii >>> 10 & 31, ii >>> 15 & 31, ii >>> 20 & 31));
    }

    /**
     * Period was 0x01FFFFE0
     * Took 841 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     */
    @Test
    public void check4TuplesCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final RoaringBitmap all = new RoaringBitmap();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x00FFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.add(joined = (joined << 5 & 0x00FFFE0) | result);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.getLongCardinality() + "/" + (1 << 20) + " 4-tuples were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-20 + "% of 4-tuples were missing.");
    }

    /**
     * <pre>
     * Period was 0x01FFFFE0
     * Took 1296 ms.
     * 21223326/33554432 5-tuples were present.
     * 36.7495596408844% of 5-tuples were missing.
     * Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:
     * 1 repetitions occurred for 12355478 5-tuples.
     * 2 repetitions occurred for 6180614 5-tuples.
     * 3 repetitions occurred for 2054982 5-tuples.
     * 4 repetitions occurred for 511138 5-tuples.
     * 5 repetitions occurred for 101588 5-tuples.
     * 6 repetitions occurred for 16806 5-tuples.
     * 7 repetitions occurred for 2386 5-tuples.
     * 8 repetitions occurred for 292 5-tuples.
     * 9 repetitions occurred for 38 5-tuples.
     * 10 repetitions occurred for 4 5-tuples.
     * </pre>
     */
    @Test
    public void check5TupleFrequencyCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 25, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x1FFFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x1FFFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 886 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     * Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:
     * 7 repetitions occurred for 2 4-tuples.
     * 9 repetitions occurred for 2 4-tuples.
     * 10 repetitions occurred for 8 4-tuples.
     * 11 repetitions occurred for 16 4-tuples.
     * 12 repetitions occurred for 24 4-tuples.
     * 13 repetitions occurred for 74 4-tuples.
     * 14 repetitions occurred for 164 4-tuples.
     * 15 repetitions occurred for 316 4-tuples.
     * 16 repetitions occurred for 730 4-tuples.
     * 17 repetitions occurred for 1324 4-tuples.
     * 18 repetitions occurred for 2402 4-tuples.
     * 19 repetitions occurred for 4230 4-tuples.
     * 20 repetitions occurred for 6806 4-tuples.
     * 21 repetitions occurred for 10124 4-tuples.
     * 22 repetitions occurred for 15236 4-tuples.
     * 23 repetitions occurred for 21278 4-tuples.
     * 24 repetitions occurred for 28108 4-tuples.
     * 25 repetitions occurred for 35856 4-tuples.
     * 26 repetitions occurred for 44134 4-tuples.
     * 27 repetitions occurred for 52646 4-tuples.
     * 28 repetitions occurred for 61342 4-tuples.
     * 29 repetitions occurred for 67242 4-tuples.
     * 30 repetitions occurred for 71950 4-tuples.
     * 31 repetitions occurred for 74122 4-tuples.
     * 32 repetitions occurred for 74294 4-tuples.
     * 33 repetitions occurred for 72472 4-tuples.
     * 34 repetitions occurred for 68146 4-tuples.
     * 35 repetitions occurred for 62602 4-tuples.
     * 36 repetitions occurred for 55266 4-tuples.
     * 37 repetitions occurred for 47274 4-tuples.
     * 38 repetitions occurred for 40026 4-tuples.
     * 39 repetitions occurred for 32958 4-tuples.
     * 40 repetitions occurred for 25734 4-tuples.
     * 41 repetitions occurred for 20170 4-tuples.
     * 42 repetitions occurred for 15194 4-tuples.
     * 43 repetitions occurred for 11170 4-tuples.
     * 44 repetitions occurred for 8160 4-tuples.
     * 45 repetitions occurred for 5586 4-tuples.
     * 46 repetitions occurred for 3982 4-tuples.
     * 47 repetitions occurred for 2698 4-tuples.
     * 48 repetitions occurred for 1756 4-tuples.
     * 49 repetitions occurred for 1054 4-tuples.
     * 50 repetitions occurred for 698 4-tuples.
     * 51 repetitions occurred for 508 4-tuples.
     * 52 repetitions occurred for 302 4-tuples.
     * 53 repetitions occurred for 148 4-tuples.
     * 54 repetitions occurred for 104 4-tuples.
     * 55 repetitions occurred for 70 4-tuples.
     * 56 repetitions occurred for 24 4-tuples.
     * 57 repetitions occurred for 18 4-tuples.
     * 58 repetitions occurred for 8 4-tuples.
     * 59 repetitions occurred for 12 4-tuples.
     * 60 repetitions occurred for 4 4-tuples.
     * 66 repetitions occurred for 2 4-tuples.
     * </pre>
     */
    @Test
    public void check4TupleFrequencyCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 20, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x00FFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x00FFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 182 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 927 repetitions occurred for 4 3-tuples.
     * 932 repetitions occurred for 4 3-tuples.
     * 935 repetitions occurred for 4 3-tuples.
     * 936 repetitions occurred for 4 3-tuples.
     * 939 repetitions occurred for 4 3-tuples.
     * 940 repetitions occurred for 4 3-tuples.
     * 941 repetitions occurred for 8 3-tuples.
     * 942 repetitions occurred for 4 3-tuples.
     * 943 repetitions occurred for 12 3-tuples.
     * 945 repetitions occurred for 4 3-tuples.
     * 946 repetitions occurred for 8 3-tuples.
     * 947 repetitions occurred for 16 3-tuples.
     * 948 repetitions occurred for 8 3-tuples.
     * 951 repetitions occurred for 4 3-tuples.
     * 952 repetitions occurred for 8 3-tuples.
     * 953 repetitions occurred for 24 3-tuples.
     * 954 repetitions occurred for 4 3-tuples.
     * 955 repetitions occurred for 4 3-tuples.
     * 956 repetitions occurred for 12 3-tuples.
     * 957 repetitions occurred for 8 3-tuples.
     * 958 repetitions occurred for 20 3-tuples.
     * 959 repetitions occurred for 16 3-tuples.
     * 960 repetitions occurred for 60 3-tuples.
     * 961 repetitions occurred for 28 3-tuples.
     * 962 repetitions occurred for 32 3-tuples.
     * 963 repetitions occurred for 32 3-tuples.
     * 964 repetitions occurred for 20 3-tuples.
     * 965 repetitions occurred for 24 3-tuples.
     * 966 repetitions occurred for 48 3-tuples.
     * 967 repetitions occurred for 44 3-tuples.
     * 968 repetitions occurred for 48 3-tuples.
     * 969 repetitions occurred for 48 3-tuples.
     * 970 repetitions occurred for 64 3-tuples.
     * 971 repetitions occurred for 74 3-tuples.
     * 972 repetitions occurred for 70 3-tuples.
     * 973 repetitions occurred for 72 3-tuples.
     * 974 repetitions occurred for 64 3-tuples.
     * 975 repetitions occurred for 64 3-tuples.
     * 976 repetitions occurred for 72 3-tuples.
     * 977 repetitions occurred for 100 3-tuples.
     * 978 repetitions occurred for 76 3-tuples.
     * 979 repetitions occurred for 128 3-tuples.
     * 980 repetitions occurred for 124 3-tuples.
     * 981 repetitions occurred for 128 3-tuples.
     * 982 repetitions occurred for 148 3-tuples.
     * 983 repetitions occurred for 112 3-tuples.
     * 984 repetitions occurred for 144 3-tuples.
     * 985 repetitions occurred for 184 3-tuples.
     * 986 repetitions occurred for 188 3-tuples.
     * 987 repetitions occurred for 208 3-tuples.
     * 988 repetitions occurred for 180 3-tuples.
     * 989 repetitions occurred for 236 3-tuples.
     * 990 repetitions occurred for 224 3-tuples.
     * 991 repetitions occurred for 208 3-tuples.
     * 992 repetitions occurred for 198 3-tuples.
     * 993 repetitions occurred for 254 3-tuples.
     * 994 repetitions occurred for 228 3-tuples.
     * 995 repetitions occurred for 284 3-tuples.
     * 996 repetitions occurred for 236 3-tuples.
     * 997 repetitions occurred for 288 3-tuples.
     * 998 repetitions occurred for 284 3-tuples.
     * 999 repetitions occurred for 324 3-tuples.
     * 1000 repetitions occurred for 280 3-tuples.
     * 1001 repetitions occurred for 356 3-tuples.
     * 1002 repetitions occurred for 316 3-tuples.
     * 1003 repetitions occurred for 332 3-tuples.
     * 1004 repetitions occurred for 456 3-tuples.
     * 1005 repetitions occurred for 328 3-tuples.
     * 1006 repetitions occurred for 420 3-tuples.
     * 1007 repetitions occurred for 476 3-tuples.
     * 1008 repetitions occurred for 424 3-tuples.
     * 1009 repetitions occurred for 424 3-tuples.
     * 1010 repetitions occurred for 488 3-tuples.
     * 1011 repetitions occurred for 488 3-tuples.
     * 1012 repetitions occurred for 496 3-tuples.
     * 1013 repetitions occurred for 404 3-tuples.
     * 1014 repetitions occurred for 524 3-tuples.
     * 1015 repetitions occurred for 376 3-tuples.
     * 1016 repetitions occurred for 428 3-tuples.
     * 1017 repetitions occurred for 454 3-tuples.
     * 1018 repetitions occurred for 574 3-tuples.
     * 1019 repetitions occurred for 474 3-tuples.
     * 1020 repetitions occurred for 532 3-tuples.
     * 1021 repetitions occurred for 510 3-tuples.
     * 1022 repetitions occurred for 568 3-tuples.
     * 1023 repetitions occurred for 452 3-tuples.
     * 1024 repetitions occurred for 436 3-tuples.
     * 1025 repetitions occurred for 552 3-tuples.
     * 1026 repetitions occurred for 496 3-tuples.
     * 1027 repetitions occurred for 568 3-tuples.
     * 1028 repetitions occurred for 528 3-tuples.
     * 1029 repetitions occurred for 498 3-tuples.
     * 1030 repetitions occurred for 460 3-tuples.
     * 1031 repetitions occurred for 550 3-tuples.
     * 1032 repetitions occurred for 486 3-tuples.
     * 1033 repetitions occurred for 478 3-tuples.
     * 1034 repetitions occurred for 420 3-tuples.
     * 1035 repetitions occurred for 496 3-tuples.
     * 1036 repetitions occurred for 492 3-tuples.
     * 1037 repetitions occurred for 418 3-tuples.
     * 1038 repetitions occurred for 366 3-tuples.
     * 1039 repetitions occurred for 468 3-tuples.
     * 1040 repetitions occurred for 404 3-tuples.
     * 1041 repetitions occurred for 428 3-tuples.
     * 1042 repetitions occurred for 444 3-tuples.
     * 1043 repetitions occurred for 360 3-tuples.
     * 1044 repetitions occurred for 360 3-tuples.
     * 1045 repetitions occurred for 320 3-tuples.
     * 1046 repetitions occurred for 388 3-tuples.
     * 1047 repetitions occurred for 360 3-tuples.
     * 1048 repetitions occurred for 360 3-tuples.
     * 1049 repetitions occurred for 344 3-tuples.
     * 1050 repetitions occurred for 270 3-tuples.
     * 1051 repetitions occurred for 234 3-tuples.
     * 1052 repetitions occurred for 356 3-tuples.
     * 1053 repetitions occurred for 246 3-tuples.
     * 1054 repetitions occurred for 290 3-tuples.
     * 1055 repetitions occurred for 232 3-tuples.
     * 1056 repetitions occurred for 248 3-tuples.
     * 1057 repetitions occurred for 232 3-tuples.
     * 1058 repetitions occurred for 164 3-tuples.
     * 1059 repetitions occurred for 180 3-tuples.
     * 1060 repetitions occurred for 184 3-tuples.
     * 1061 repetitions occurred for 164 3-tuples.
     * 1062 repetitions occurred for 164 3-tuples.
     * 1063 repetitions occurred for 160 3-tuples.
     * 1064 repetitions occurred for 152 3-tuples.
     * 1065 repetitions occurred for 136 3-tuples.
     * 1066 repetitions occurred for 148 3-tuples.
     * 1067 repetitions occurred for 136 3-tuples.
     * 1068 repetitions occurred for 108 3-tuples.
     * 1069 repetitions occurred for 132 3-tuples.
     * 1070 repetitions occurred for 92 3-tuples.
     * 1071 repetitions occurred for 100 3-tuples.
     * 1072 repetitions occurred for 56 3-tuples.
     * 1073 repetitions occurred for 56 3-tuples.
     * 1074 repetitions occurred for 80 3-tuples.
     * 1075 repetitions occurred for 80 3-tuples.
     * 1076 repetitions occurred for 64 3-tuples.
     * 1077 repetitions occurred for 70 3-tuples.
     * 1078 repetitions occurred for 42 3-tuples.
     * 1079 repetitions occurred for 56 3-tuples.
     * 1080 repetitions occurred for 52 3-tuples.
     * 1081 repetitions occurred for 24 3-tuples.
     * 1082 repetitions occurred for 56 3-tuples.
     * 1083 repetitions occurred for 24 3-tuples.
     * 1084 repetitions occurred for 32 3-tuples.
     * 1085 repetitions occurred for 36 3-tuples.
     * 1086 repetitions occurred for 24 3-tuples.
     * 1087 repetitions occurred for 52 3-tuples.
     * 1088 repetitions occurred for 24 3-tuples.
     * 1089 repetitions occurred for 20 3-tuples.
     * 1090 repetitions occurred for 20 3-tuples.
     * 1091 repetitions occurred for 20 3-tuples.
     * 1092 repetitions occurred for 12 3-tuples.
     * 1093 repetitions occurred for 8 3-tuples.
     * 1094 repetitions occurred for 20 3-tuples.
     * 1095 repetitions occurred for 28 3-tuples.
     * 1096 repetitions occurred for 8 3-tuples.
     * 1097 repetitions occurred for 28 3-tuples.
     * 1098 repetitions occurred for 8 3-tuples.
     * 1099 repetitions occurred for 20 3-tuples.
     * 1100 repetitions occurred for 4 3-tuples.
     * 1103 repetitions occurred for 4 3-tuples.
     * 1106 repetitions occurred for 4 3-tuples.
     * 1107 repetitions occurred for 4 3-tuples.
     * 1111 repetitions occurred for 8 3-tuples.
     * 1113 repetitions occurred for 4 3-tuples.
     * </pre>
     */
    @Test
    public void check3TupleFrequencyCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 15, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x0007FE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x0007FE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 162 ms.
     * 1024/1024 2-tuples were present.
     * 0.0% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 32767 repetitions occurred for 32 2-tuples.
     * 32768 repetitions occurred for 992 2-tuples.
     * </pre>
     */
    @Test
    public void check2TupleFrequencyCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 10, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x000003E0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x000003E0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 86 ms.
     * Frequency of 00: 1048575
     * Frequency of 01: 1048575
     * Frequency of 02: 1048575
     * Frequency of 03: 1048575
     * Frequency of 04: 1048575
     * Frequency of 05: 1048575
     * Frequency of 06: 1048575
     * Frequency of 07: 1048575
     * Frequency of 08: 1048575
     * Frequency of 09: 1048575
     * Frequency of 10: 1048575
     * Frequency of 11: 1048575
     * Frequency of 12: 1048575
     * Frequency of 13: 1048575
     * Frequency of 14: 1048575
     * Frequency of 15: 1048575
     * Frequency of 16: 1048575
     * Frequency of 17: 1048575
     * Frequency of 18: 1048575
     * Frequency of 19: 1048575
     * Frequency of 20: 1048575
     * Frequency of 21: 1048575
     * Frequency of 22: 1048575
     * Frequency of 23: 1048575
     * Frequency of 24: 1048575
     * Frequency of 25: 1048575
     * Frequency of 26: 1048575
     * Frequency of 27: 1048575
     * Frequency of 28: 1048575
     * Frequency of 29: 1048575
     * Frequency of 30: 1048575
     * Frequency of 31: 1048575
     * </pre>
     */
    @Test
    public void checkFrequencyCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[32];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        for (int j = 0; j < 32; j++) {
            System.out.printf("Frequency of %02d: %d \n", j, frequencies[j]);
        }
    }
    /*
    int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
    stateE = stateE + (0x1D ^ stateC) & 31;
        3D-equidistributed

    int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
    stateE = (stateE + 0x15 + stateC) & 31;
        3D-equidistributed

    int result = stateE;
    stateE = (stateE + 0x15 + stateC) & 31;
        4D-equidistributed

    int result = stateE;
    stateE = (stateE + ~stateA) & 31;
        4D-equidistributed

     */
    /**
     * <pre>
     * Period was 0x01FFFFE0
     * Took 1247 ms.
     * 16777216/33554432 5-tuples were present.
     * 50.0% of 5-tuples were missing.
     * Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:
     * 1 repetitions occurred for 32 5-tuples.
     * 2 repetitions occurred for 16777184 5-tuples.
     * </pre>
     * With rotate(stateE) + 0x19 ^ stateB:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 1471 ms.
     * 21307846/33554432 5-tuples were present.
     * 36.4976704120636% of 5-tuples were missing.
     * Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:
     * 1 repetitions occurred for 12436215 5-tuples.
     * 2 repetitions occurred for 6231331 5-tuples.
     * 3 repetitions occurred for 2036408 5-tuples.
     * 4 repetitions occurred for 493111 5-tuples.
     * 5 repetitions occurred for 93271 5-tuples.
     * 6 repetitions occurred for 15284 5-tuples.
     * 7 repetitions occurred for 2032 5-tuples.
     * 8 repetitions occurred for 176 5-tuples.
     * 9 repetitions occurred for 16 5-tuples.
     * 10 repetitions occurred for 2 5-tuples.
     * </pre>
     */
    @Test
    public void check5TupleFrequencyCountingByXoshiro4x5Simple() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 25, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = stateE;
            int t = stateB << 1 & 31;
            stateE = (stateE + ~stateA) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x1FFFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = stateE;
            int t = stateB << 1 & 31;
            stateE = (stateE + ~stateA) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x1FFFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 776 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     * Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:
     * 31 repetitions occurred for 32 4-tuples.
     * 32 repetitions occurred for 1048544 4-tuples.
     * </pre>
     * With rotate(stateE) + 0x19 ^ stateB:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 874 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     * Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:
     * 12 repetitions occurred for 32 4-tuples.
     * 13 repetitions occurred for 32 4-tuples.
     * 14 repetitions occurred for 96 4-tuples.
     * 15 repetitions occurred for 192 4-tuples.
     * 16 repetitions occurred for 288 4-tuples.
     * 17 repetitions occurred for 768 4-tuples.
     * 18 repetitions occurred for 2112 4-tuples.
     * 19 repetitions occurred for 3072 4-tuples.
     * 20 repetitions occurred for 4128 4-tuples.
     * 21 repetitions occurred for 7488 4-tuples.
     * 22 repetitions occurred for 12928 4-tuples.
     * 23 repetitions occurred for 19840 4-tuples.
     * 24 repetitions occurred for 27010 4-tuples.
     * 25 repetitions occurred for 33662 4-tuples.
     * 26 repetitions occurred for 43906 4-tuples.
     * 27 repetitions occurred for 53182 4-tuples.
     * 28 repetitions occurred for 61921 4-tuples.
     * 29 repetitions occurred for 68386 4-tuples.
     * 30 repetitions occurred for 74911 4-tuples.
     * 31 repetitions occurred for 81376 4-tuples.
     * 32 repetitions occurred for 81251 4-tuples.
     * 33 repetitions occurred for 75900 4-tuples.
     * 34 repetitions occurred for 70787 4-tuples.
     * 35 repetitions occurred for 66590 4-tuples.
     * 36 repetitions occurred for 56033 4-tuples.
     * 37 repetitions occurred for 47423 4-tuples.
     * 38 repetitions occurred for 39231 4-tuples.
     * 39 repetitions occurred for 31359 4-tuples.
     * 40 repetitions occurred for 23808 4-tuples.
     * 41 repetitions occurred for 18816 4-tuples.
     * 42 repetitions occurred for 13217 4-tuples.
     * 43 repetitions occurred for 8895 4-tuples.
     * 44 repetitions occurred for 6753 4-tuples.
     * 45 repetitions occurred for 4927 4-tuples.
     * 46 repetitions occurred for 2624 4-tuples.
     * 47 repetitions occurred for 2240 4-tuples.
     * 48 repetitions occurred for 1536 4-tuples.
     * 49 repetitions occurred for 864 4-tuples.
     * 50 repetitions occurred for 608 4-tuples.
     * 51 repetitions occurred for 192 4-tuples.
     * 52 repetitions occurred for 32 4-tuples.
     * 53 repetitions occurred for 32 4-tuples.
     * 54 repetitions occurred for 64 4-tuples.
     * 55 repetitions occurred for 64 4-tuples.
     * </pre>
     * With result = rotate(stateE) ^ rotate(stateA) + stateE, and stateE += stateD + 21:
     * (Not equidistributed, but no 4-tuples are missing. Repetitions range from 12 to 61.)
     */
    @Test
    public void check4TupleFrequencyCountingByXoshiro4x5Simple() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 20, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 1 | stateE >>> 4) & 31) ^ (((stateA << 3 | stateA >>> 2) + stateE) & 31 );
            int t = stateB >>> 1;
            stateE = (stateE + stateD + 21) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x00FFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 1 | stateE >>> 4) & 31) ^ (((stateA << 3 | stateA >>> 2) + stateE) & 31 );
            int t = stateB >>> 1;
            stateE = (stateE + stateD + 21) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x00FFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 151 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 1023 repetitions occurred for 32 3-tuples.
     * 1024 repetitions occurred for 32736 3-tuples.
     * </pre>
     * With rotate(stateE) + 0x19 ^ stateB:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 189 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 1023 repetitions occurred for 32 3-tuples.
     * 1024 repetitions occurred for 32736 3-tuples.
     * </pre>
     * With result = rotate(stateE) ^ rotate(stateA) + stateE, and stateE += stateD + 21:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 178 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 1023 repetitions occurred for 32 3-tuples.
     * 1024 repetitions occurred for 32736 3-tuples.
     * </pre>
     */
    @Test
    public void check3TupleFrequencyCountingByXoshiro4x5Simple() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 15, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 1 | stateE >>> 4) & 31) ^ (((stateA << 3 | stateA >>> 2) + stateE) & 31 );
            int t = stateB >>> 1;
            stateE = (stateE + stateD + 21) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            joined = (joined << 5 & 0x0007FE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 1 | stateE >>> 4) & 31) ^ (((stateA << 3 | stateA >>> 2) + stateE) & 31 );
            int t = stateB >>> 1;
            stateE = (stateE + stateD + 21) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x0007FE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 127 ms.
     * 1024/1024 2-tuples were present.
     * 0.0% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 32767 repetitions occurred for 32 2-tuples.
     * 32768 repetitions occurred for 992 2-tuples.
     * </pre>
     * With rotate(stateE) + 0x19 ^ stateB:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 137 ms.
     * 1024/1024 2-tuples were present.
     * 0.0% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 32767 repetitions occurred for 32 2-tuples.
     * 32768 repetitions occurred for 992 2-tuples.
     * </pre>
     */
    @Test
    public void check2TupleFrequencyCountingByXoshiro4x5Simple() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 10, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = stateE;
            int t = stateB << 1 & 31;
            stateE = (stateE + ~stateA) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x000003E0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = stateE;
            int t = stateB << 1 & 31;
            stateE = (stateE + ~stateA) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x000003E0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 65 ms.
     * Frequency of 00: 1048575
     * Frequency of 01: 1048575
     * Frequency of 02: 1048575
     * Frequency of 03: 1048575
     * Frequency of 04: 1048575
     * Frequency of 05: 1048575
     * Frequency of 06: 1048575
     * Frequency of 07: 1048575
     * Frequency of 08: 1048575
     * Frequency of 09: 1048575
     * Frequency of 10: 1048575
     * Frequency of 11: 1048575
     * Frequency of 12: 1048575
     * Frequency of 13: 1048575
     * Frequency of 14: 1048575
     * Frequency of 15: 1048575
     * Frequency of 16: 1048575
     * Frequency of 17: 1048575
     * Frequency of 18: 1048575
     * Frequency of 19: 1048575
     * Frequency of 20: 1048575
     * Frequency of 21: 1048575
     * Frequency of 22: 1048575
     * Frequency of 23: 1048575
     * Frequency of 24: 1048575
     * Frequency of 25: 1048575
     * Frequency of 26: 1048575
     * Frequency of 27: 1048575
     * Frequency of 28: 1048575
     * Frequency of 29: 1048575
     * Frequency of 30: 1048575
     * Frequency of 31: 1048575
     * </pre>
     * With rotate(stateE) + 0x19 ^ stateB:
     * <pre>
     * Period was 0x01FFFFE0
     * Took 106 ms.
     * Frequency of 00: 1048575
     * Frequency of 01: 1048575
     * Frequency of 02: 1048575
     * Frequency of 03: 1048575
     * Frequency of 04: 1048575
     * Frequency of 05: 1048575
     * Frequency of 06: 1048575
     * Frequency of 07: 1048575
     * Frequency of 08: 1048575
     * Frequency of 09: 1048575
     * Frequency of 10: 1048575
     * Frequency of 11: 1048575
     * Frequency of 12: 1048575
     * Frequency of 13: 1048575
     * Frequency of 14: 1048575
     * Frequency of 15: 1048575
     * Frequency of 16: 1048575
     * Frequency of 17: 1048575
     * Frequency of 18: 1048575
     * Frequency of 19: 1048575
     * Frequency of 20: 1048575
     * Frequency of 21: 1048575
     * Frequency of 22: 1048575
     * Frequency of 23: 1048575
     * Frequency of 24: 1048575
     * Frequency of 25: 1048575
     * Frequency of 26: 1048575
     * Frequency of 27: 1048575
     * Frequency of 28: 1048575
     * Frequency of 29: 1048575
     * Frequency of 30: 1048575
     * Frequency of 31: 1048575
     * </pre>
     */
    @Test
    public void checkFrequencyCountingByXoshiro4x5Simple() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[32];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = stateE;
            int t = stateB << 1 & 31;
            stateE = (stateE + ~stateA) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        for (int j = 0; j < 32; j++) {
            System.out.printf("Frequency of %02d: %d \n", j, frequencies[j]);
        }
    }

    /**
     * 1D equidistributed!
     * <br>
     * Each byte result occurs 32385 times over the period of 0x007E8100.
     */
    @Test
    public void checkFrequencyWackyWumpus() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[256];
        int stateA = 1, stateB = 2, stateC = 1;
        int endA = stateA, endB = stateB, endC = stateC;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = (stateA = (stateA * 0x65 + 0x01 ^ (stateC = (stateC << 1 & 0xFF) ^ (-(stateC >>> 7) & 0xA9))) & 0xFF) * ((stateB = ((stateB << 1 & 0xFE) ^ (-(stateB >>> 7) & 0xEE)) & 0xFE) ^ 0xAB) & 0xFF;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        for (int j = 0; j < 256; j++) {
            System.out.printf("Frequency of %03d: %d \n", j, frequencies[j]);
        }
    }

    /**
     * <pre>
     * Period was 0x01FFFFE0
     * Took 1202 ms.
     * 20971510/33554432 5-tuples were present.
     * 37.50002980232239% of 5-tuples were missing.
     * Number of repetitions of a 5-tuple to the number of 5-tuples that repeated that often:
     * 1 repetitions occurred for 10485766 5-tuples.
     * 2 repetitions occurred for 8388598 5-tuples.
     * 3 repetitions occurred for 2097146 5-tuples.
     * </pre>
     */
    @Test
    public void check5TupleFrequencyCounterWithXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 25, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x1FFFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x1FFFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 853 ms.
     * 1048576/1048576 4-tuples were present.
     * 0.0% of 4-tuples were missing.
     * Number of repetitions of a 4-tuple to the number of 4-tuples that repeated that often:
     * 31 repetitions occurred for 32 4-tuples.
     * 32 repetitions occurred for 1048544 4-tuples.
     * </pre>
     * Combining stateB and stateE using XOR or addition (masked) both produce identical results to above.
     */
    @Test
    public void check4TupleFrequencyCounterWithXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 20, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = stateE + stateB & 31;
//            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x00FFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = stateE + stateB & 31;
//            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x00FFFE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 158 ms.
     * 32768/32768 3-tuples were present.
     * 0.0% of 3-tuples were missing.
     * Number of repetitions of a 3-tuple to the number of 3-tuples that repeated that often:
     * 1023 repetitions occurred for 32 3-tuples.
     * 1024 repetitions occurred for 32736 3-tuples.
     * </pre>
     */
    @Test
    public void check3TupleFrequencyCounterWithXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 15, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x0007FE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x0007FE0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 139 ms.
     * 1024/1024 2-tuples were present.
     * 0.0% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 32767 repetitions occurred for 32 2-tuples.
     * 32768 repetitions occurred for 992 2-tuples.
     * </pre>
     */
    @Test
    public void check2TupleFrequencyCounterWithXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 10, 0.6f);
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x000003E0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.getAndIncrement((joined = (joined << 5 & 0x000003E0) | result), 0, 1);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
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
     * Period was 0x01FFFFE0
     * Took 79 ms.
     * Frequency of 00: 1048575
     * Frequency of 01: 1048575
     * Frequency of 02: 1048575
     * Frequency of 03: 1048575
     * Frequency of 04: 1048575
     * Frequency of 05: 1048575
     * Frequency of 06: 1048575
     * Frequency of 07: 1048575
     * Frequency of 08: 1048575
     * Frequency of 09: 1048575
     * Frequency of 10: 1048575
     * Frequency of 11: 1048575
     * Frequency of 12: 1048575
     * Frequency of 13: 1048575
     * Frequency of 14: 1048575
     * Frequency of 15: 1048575
     * Frequency of 16: 1048575
     * Frequency of 17: 1048575
     * Frequency of 18: 1048575
     * Frequency of 19: 1048575
     * Frequency of 20: 1048575
     * Frequency of 21: 1048575
     * Frequency of 22: 1048575
     * Frequency of 23: 1048575
     * Frequency of 24: 1048575
     * Frequency of 25: 1048575
     * Frequency of 26: 1048575
     * Frequency of 27: 1048575
     * Frequency of 28: 1048575
     * Frequency of 29: 1048575
     * Frequency of 30: 1048575
     * Frequency of 31: 1048575
     * </pre>
     */
    @Test
    public void checkFrequencyCounterWithXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[32];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >>> 1) + 0x19 & 31) ^ stateB;
            int t = stateB << 1 & 31;
            stateE = stateE + 0x1D & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        for (int j = 0; j < 32; j++) {
            System.out.printf("Frequency of %02d: %d \n", j, frequencies[j]);
        }
    }

    @Test
    public void check5TuplesCountingByXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final RoaringBitmap all = new RoaringBitmap();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int joined = 0;
        for (int g = 0; g < 5; g++) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            joined = (joined << 5 & 0x1FFFFE0) | result;
        }
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            all.add(joined = (joined << 5 & 0x1FFFFE0) | result);
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.getLongCardinality() + "/" + (1 << 25) + " 5-tuples were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-25 + "% of 5-tuples were missing.");
    }

    /**
     * Period was 0x01FFFFE0
     * Took 109 ms.
     * Frequency of 00: 32767 (#1 off)
     * Frequency of 52: 32767 (#2 off)
     * Frequency of 65: 32767 (#3 off)
     * Frequency of 117: 32767 (#4 off)
     * Frequency of 130: 32767 (#5 off)
     * Frequency of 182: 32767 (#6 off)
     * Frequency of 195: 32767 (#7 off)
     * Frequency of 247: 32767 (#8 off)
     * Frequency of 260: 32767 (#9 off)
     * Frequency of 272: 32767 (#10 off)
     * Frequency of 325: 32767 (#11 off)
     * Frequency of 337: 32767 (#12 off)
     * Frequency of 390: 32767 (#13 off)
     * Frequency of 402: 32767 (#14 off)
     * Frequency of 455: 32767 (#15 off)
     * Frequency of 467: 32767 (#16 off)
     * Frequency of 532: 32767 (#17 off)
     * Frequency of 544: 32767 (#18 off)
     * Frequency of 597: 32767 (#19 off)
     * Frequency of 609: 32767 (#20 off)
     * Frequency of 662: 32767 (#21 off)
     * Frequency of 674: 32767 (#22 off)
     * Frequency of 727: 32767 (#23 off)
     * Frequency of 739: 32767 (#24 off)
     * Frequency of 804: 32767 (#25 off)
     * Frequency of 816: 32767 (#26 off)
     * Frequency of 869: 32767 (#27 off)
     * Frequency of 881: 32767 (#28 off)
     * Frequency of 934: 32767 (#29 off)
     * Frequency of 946: 32767 (#30 off)
     * Frequency of 999: 32767 (#31 off)
     * Frequency of 1011: 32767 (#32 off)
     * All other results occurred with frequency 32768 .
     */
    @Test
    public void checkFrequencyCountingByXoshiro4x5Long() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[1024];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = ((stateA << 2 | stateA >>> 8) + stateB + (stateC << 6 | stateC >> 4) + (stateE << 4 | stateE >> 6) + (stateE << 8 | stateE >> 2) & 1023);
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        int howMany = 0;
        for (int j = 0; j < 1024; j++) {
            if(frequencies[j] != 32768)
                System.out.printf("Frequency of %02d: %d (#%d off)\n", j, frequencies[j], ++howMany);
        }
        System.out.println("All other results occurred with frequency 32768 .");
    }

    /**
     * Period was 0x01FFFFE0
     * Took 218 ms.
     * Frequency of 00: 32767 (#1 off)
     * Frequency of 31: 32767 (#2 off)
     * Frequency of 36: 32767 (#3 off)
     * Frequency of 59: 32767 (#4 off)
     * Frequency of 72: 32767 (#5 off)
     * Frequency of 87: 32767 (#6 off)
     * Frequency of 108: 32767 (#7 off)
     * Frequency of 115: 32767 (#8 off)
     * Frequency of 257: 32767 (#9 off)
     * Frequency of 286: 32767 (#10 off)
     * Frequency of 293: 32767 (#11 off)
     * Frequency of 314: 32767 (#12 off)
     * Frequency of 329: 32767 (#13 off)
     * Frequency of 342: 32767 (#14 off)
     * Frequency of 365: 32767 (#15 off)
     * Frequency of 370: 32767 (#16 off)
     * Frequency of 514: 32767 (#17 off)
     * Frequency of 541: 32767 (#18 off)
     * Frequency of 550: 32767 (#19 off)
     * Frequency of 569: 32767 (#20 off)
     * Frequency of 586: 32767 (#21 off)
     * Frequency of 597: 32767 (#22 off)
     * Frequency of 622: 32767 (#23 off)
     * Frequency of 625: 32767 (#24 off)
     * Frequency of 771: 32767 (#25 off)
     * Frequency of 796: 32767 (#26 off)
     * Frequency of 807: 32767 (#27 off)
     * Frequency of 824: 32767 (#28 off)
     * Frequency of 843: 32767 (#29 off)
     * Frequency of 852: 32767 (#30 off)
     * Frequency of 879: 32767 (#31 off)
     * Frequency of 880: 32767 (#32 off)
     * All other results occurred with frequency 32768 .
     */
    @Test
    public void checkFrequencyCountingByXoshiro4x5LongTwoInts() {
        long startTime = System.currentTimeMillis();
        final int[] frequencies = new int[1024];
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        int endA = stateA, endB = stateB, endC = stateC, endD = stateD, endE = stateE;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int hi = ((stateE << 4 | stateE >> 1) & 31) ^ ((stateA << 2 | stateA >>> 3) + stateB & 31);
            int lo = ((((stateC << 3 | stateC >> 2) & 31) ^ ((stateE << 1 | stateE >>> 4) + stateD & 31)) << 27) >> 27;
            int result = (hi << 5 ^ lo) & 1023;
            int t = stateB << 1 & 31;
            stateE = stateE + (0x1D ^ stateC) & 31;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 3 | stateD >>> 2) & 31;
            frequencies[result]++;
            if (stateA == endA && stateB == endB && stateC == endC && stateD == endD && stateE == endE) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        int howMany = 0;
        for (int j = 0; j < 1024; j++) {
            if(frequencies[j] != 32768)
                System.out.printf("Frequency of %02d: %d (#%d off)\n", j, frequencies[j], ++howMany);
        }
        System.out.println("All other results occurred with frequency 32768 .");
    }
    /**
     * Period was 0x01FFFFE0
     * Took 2224 ms.
     * 33554400/33554432 outputs were present.
     * 9.5367431640625E-5% of outputs were missing.
     * 17 2 24 29 0
     * 29 4 23 7 1
     * 9 23 20 16 2
     * 21 1 19 0 3
     * 12 16 5 1 4
     */
    @Test
    public void checkPeriodCounterInXoshiro4x5() {
        long startTime = System.currentTimeMillis();
        final RoaringBitmap all = new RoaringBitmap();
        int stateA = 0, stateB = 0, stateC = 0, stateD = 0;
        int stateE = 0;
        long i = 0L;
        while (++i <= 0x10000100L) {
            int t = stateB >>> 1;
            stateC ^= stateA ^ (stateE = stateE + 0x17 & 31);
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 2 | stateD >>> 3) & 31;
            all.add(stateA | stateB << 5 | stateC << 10 | stateD << 15 | stateE << 20);
            if (stateA == 0 && stateB == 0 && stateC == 0 && stateD == 0 && stateE == 0) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.getLongCardinality() + "/" + (1 << 25) + " outputs were present.");
        System.out.println(100.0 - all.getLongCardinality() * 0x64p-25 + "% of outputs were missing.");
        all.flip(0L, 1L << 25);
        all.forEach((int ii) -> System.out.printf("%d %d %d %d %d\n", ii & 31, ii >>> 5 & 31, ii >>> 10 & 31, ii >>> 15 & 31, ii >>> 20 & 31));
    }

    /**
     * Best right shift was 1, best left rotation was 1, with period 3FFFFF
     */
    @Test
    public void checkPeriod24_Xoshiro4x6() {
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
    public void checkPeriod28_Xoshiro4x7() {
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
     * Took 38623 ms.
     */
    @Test
    public void checkPeriodCountingByXoshiro4x7() {
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        int stateE = 1;
        long i = 0L;
        while (++i <= 0x10000000100L) {
            int t = stateB << 2 & 127;
            stateE = stateE + (0x6D ^ stateC) & 127;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 6) & 127;

            if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1 && stateE == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%011X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x00FFFFFFF00
     * Took 85532 ms.
     */
    @Test
    public void checkPeriodByteCountingByXoshiro4x7() {
        long startTime = System.currentTimeMillis();
        int stateA = 1, stateB = 1, stateC = 1, stateD = 1;
        int stateE = 1;
        long i = 0L;
        while (++i <= 0x10000000100L) {
            int t = stateB << 2 & 127;
            stateE = stateE + (0xC5 ^ stateC) & 255;
            stateC ^= stateA;
            stateD ^= stateB;
            stateB ^= stateC;
            stateA ^= stateD;
            stateC ^= t;
            stateD = (stateD << 1 | stateD >>> 6) & 127;

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
    public void checkPeriodCounterInXoshiro4x7() {
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
     * Returned to initial state after 1099511627520 steps. Theoretical maximum is 1099511627776.
     * Period was 0x0FFFFFFFF00
     * Took 3610564 ms.
     */
    @Test
    public void checkPeriodCountingByXoshiro4x8() {
        long startTime = System.currentTimeMillis();
//        final Roaring64Bitmap all = new Roaring64Bitmap();
        byte stateA = 1, stateB = 1, stateC = 1, stateD = 1, stateE = 1;
        long i = 0;
        OUTER:
        for (int j = 1; j < 260; j++) {
            long inner = 0;
            while (++inner <= 0x100000000L) {
                ++i;
                byte t = (byte) (stateB << 3);
                stateE += 0xC7 ^ stateC; // Period was 0x0FFFFFFFFFF !!!
                stateC ^= stateA;
                stateD ^= stateB;
                stateB ^= stateC;
//                    stateE = stateE + (stateB ^= stateC) ^ 1; // Period was 001FFFFFFFE, or twice xoshiro's period.
                stateA ^= stateD;
                stateC ^= t;
                stateD = (byte) rotate8(stateD, 1);

//                all.add((stateA&255L)|(stateB&255L)<<8|(stateC&255L)<<16|(stateD&255L)<<24|(stateE&255L)<<32);
                if (stateA == 1 && stateB == 1 && stateC == 1 && stateD == 1 && stateE == 1) {
                    System.out.printf("Returned to initial state after %d steps. Theoretical maximum is %d.\n", i, (1L << 40));
                    break OUTER;
                }
            }
            System.out.println("Finished " + j + " * (1L << 32) steps; taken " + (System.currentTimeMillis() - startTime) + " ms");
        }
        System.out.printf("Period was 0x%011X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
//        System.out.println(all.getLongCardinality() + "/" + (1 << 25) + " outputs were present.");
//        System.out.println(100.0 - all.getLongCardinality() * 0x64p-25 + "% of outputs were missing.");
//        all.flip(0L, 1L << 40);
//        all.forEach((long ii) -> System.out.printf("%d %d %d %d %d\n", ii & 255, ii >>> 8 & 255, ii >>> 16 & 255, ii >>> 24 & 255, ii >>> 32 & 255));
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
            stateA = (short) ((stateA & 0x7FFF) << 1 ^ ((stateA >> 31) & 0x002D) ^ (stateE += 0x7B));
//            (stateA = (stateA << 1) ^ (stateA >> 63 & 0xfeedbabedeadbeefL));
            // Period was 0x0FFFF00, increment to stateE must be odd.
//            stateA = (short) ((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400) ^ ((stateE += 0x81) & 255)); // mask not necessary.
//            stateA = (short) ((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400) ^ (stateE += 0x61));

//                    stateE += Integer.numberOfLeadingZeros(stateA = (short)((stateA & 0xFFFF) >>> 1 ^ (-(stateA & 1) & 0xB400))); // Period was 0x0FFFF00
            if (stateA == 1 && stateE == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x0010000
     */
    @Test
    public void checkPeriodLCG8CLZ8() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateB = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
            // Period is 0x10000, full-period.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ((stateB + 0x35 + clz8(stateA)) & 0xFF);

            // With `stateB + odd ^ clz8(stateA)`, this has half the period.
            // Period is 0x08000 . Half of states aren't reached.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ((stateB + 0x35 ^ clz8(stateA)) & 0xFF);

            // full-period, should work with imul on JS.
            // What's more, any odd multiplier seems to work.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ((stateB + clz8(stateA) * 0x37) & 0xFF);
            // full period!
            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
            stateB = ((stateB + clz8(stateA)) * 0x37) & 0xFF;
            // full period!
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = (~(stateB + clz8(stateA)) & 0xFF);
            // full period! Any XOR constant seems to work.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = (0x80 ^ stateB + clz8(stateA) & 0xFF);
            if (stateA == 1 && stateB == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x0010000
     */
    @Test
    public void checkPeriodLCG8CLZ8CLZ8() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateB = 1;
        int stateC = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
            // period is only the same as two states (stateC is wasted).
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ((stateB + clz8(stateA)) * 0x31) & 0xFF;
//            stateC = ((stateC + clz8(stateB)) * 0x4F) & 0xFF;

            // full-period, should work with imul on JS.
            // What's more, any odd multiplier seems to work.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ((stateB + clz8(stateA)) * 0x31) & 0xFF;
//            stateC = ((stateC + clz8(stateA & stateB)) * 0x4F) & 0xFF;

            // full period!
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = ~(stateB + clz8(stateA)) & 0xFF;
//            stateC = ~(stateC + clz8(stateA & stateB)) & 0xFF;

            // full period! Any XOR constants seem to work. They can be the same, as above.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = (clz8(stateA) + stateB ^ 0x11) & 0xFF;
//            stateC = (clz8(stateA & stateB) + stateC ^ 0x48) & 0xFF;

            // Has only a quarter of the expected period.
//            stateA = ((stateA * 0xAB ^ 0xCD) & 0xFF);
//            stateB = (clz8(stateA) ^ stateB) * 0x13 & 0xFF;
//            stateC = (clz8(stateA & stateB) ^ stateC) * 0x43 & 0xFF;

            // full period.
            // should be GWT-friendly.
//            stateA = ((stateA + 0xAF ^ 0xCE) & 0xFF);
//            stateB = ((stateB + clz8(stateA)) * 0x31) & 0xFF;
//            stateC = ((stateC + clz8(stateA & stateB)) * 0x4F) & 0xFF;

            // full period.
            // GWT-friendly.
            stateA = ((stateA + 0xAF ^ 0x52) & 0xFF);
            stateB = ((stateB + clz8(stateA)) ^ 0xFA) & 0xFF;
            stateC = ((stateC + clz8(stateA & stateB)) ^ 0x66) & 0xFF;

            if (stateA == 1 && stateB == 1 && stateC == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x0007F00
     * (As expected! Good!)
     */
    @Test
    public void checkPeriodLCG8LFSR7() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateB = 2;
        long i = 0;
        while (++i <= 0x1000100L) {
            stateA = (stateA * 0x65 + 0x01 ^ (stateB = ((stateB << 1 & 0xFE) ^ (-(stateB >>> 7) & 0xEE)) & 0xFE)) & 0xFF;
            if (stateA == 1 && stateB == 2) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x07E8100
     * (As expected! Full! Good!)
     */
    @Test
    public void checkPeriodLCG8LFSR7LFSR8() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateB = 2;
        int stateC = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
            stateA = (stateA * 0x65 + 0x01 ^ (stateB = ((stateB << 1 & 0xFE) ^ (-(stateB >>> 7) & 0xEE)) & 0xFE)) & 0xFF;
            stateC = (stateC << 1 & 0xFF) ^ (-(stateC >>> 7) & 0xA9);
            if (stateA == 1 && stateB == 2 && stateC == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x07E8100
     * (As expected! Full! Good!)
     */
    @Test
    public void checkPeriodWackyWumpus() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateB = 2;
        int stateC = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
            stateA = (stateA * 0x65 + 0x01 ^ (stateC = (stateC << 1 & 0xFF) ^ (-(stateC >>> 7) & 0xA9))) & 0xFF;
            stateB = ((stateB << 1 & 0xFE) ^ (-(stateB >>> 7) & 0xEE)) & 0xFE;
            if (stateA == 1 && stateB == 2 && stateC == 1) {
                break;
            }
        }
        System.out.printf("Period was 0x%07X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
    }

    /**
     * Period was 0x000FF00
     * (Full!)
     */
    @Test
    public void checkPeriodLCG8LFSR8() {
        long startTime = System.currentTimeMillis();
        int stateA = 1;
        int stateC = 1;
        long i = 0;
        while (++i <= 0x1000100L) {
            stateA = (stateA * 0x65 + 0x5B ^ (stateC = (stateC << 1 & 0xFF) ^ (-(stateC >>> 7) & 0xA9))) & 0xFF;
            if (stateA == 1 && stateC == 1) {
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

    /**
     * {@code int result = (stateA ^ stateB) & 255;}
     * <br>
     * Period was 0x0000FF00
     * Took 14 ms.
     * 43436/65536 2-tuples were present.
     * 33.721923828125% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 1 repetitions occurred for 21592 2-tuples.
     * 2 repetitions occurred for 21844 2-tuples.
     * <br>
     * {@code int result = (stateA + stateB) & 255;}
     * <br>
     * Period was 0x0000FF00
     * Took 9 ms.
     * 41472/65536 2-tuples were present.
     * 36.71875% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 1 repetitions occurred for 17664 2-tuples.
     * 2 repetitions occurred for 23808 2-tuples.
     */
    @Test
    public void check2TupleFrequencyCounterWithL8X8() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 16, 0.6f);
        byte stateA = 1, stateB = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = (stateA + stateB) & 255;
            stateA = (byte) (stateA << 1 ^ (stateA >> 31 & 0xE7));
            stateB++;
            joined = (joined << 8 & 0x0000FF00) | result;
        }
        int endA = stateA, endB = stateB;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = (stateA + stateB) & 255;
            stateA = (byte) (stateA << 1 ^ (stateA >> 31 & 0xE7));
            stateB++;
            all.getAndIncrement((joined = (joined << 8 & 0x0000FF00) | result), 0, 1);
            if (stateA == endA && stateB == endB) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 16) + " 2-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-16 + "% of 2-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(1000, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:");
        System.out.println(inv.toString(" 2-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 2-tuples.");
    }
    @Test
    public void checkSingleFrequencyCounterWithL8X8() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 16, 0.6f);
        byte stateA = 1, stateB = 1;
        for (int g = 0; g < 20; g++) {
            stateA = (byte) (stateA << 1 ^ (stateA >> 31 & 0xE7));
            stateB++;
        }
        int endA = stateA, endB = stateB;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = (stateA + stateB) & 255;
            stateA = (byte) (stateA << 1 ^ (stateA >> 31 & 0xE7));
            stateB++;
            all.getAndIncrement(result, 0, 1);
            if (stateA == endA && stateB == endB) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 8) + " results were present.");
        System.out.println(100.0 - all.size() * 0x64p-8 + "% of results were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(1000, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a result to the number of results that repeated that often:");
        System.out.println(inv.toString(" 2-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 2-tuples.");
    }

    /**
     * Period was 0x00010000
     * Took 12 ms.
     * 47616/65536 2-tuples were present.
     * 27.34375% of 2-tuples were missing.
     * Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:
     * 1 repetitions occurred for 31232 2-tuples.
     * 2 repetitions occurred for 14848 2-tuples.
     * 3 repetitions occurred for 1536 2-tuples.
     */
    @Test
    public void check2TupleFrequencyCounterWithLCG8CLZ8() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 16, 0.6f);
        int stateA = 1, stateB = 1;
        int joined = 0;
        for (int g = 0; g < 20; g++) {
            int result = (rotate8(stateA, 5) + stateB) & 0xFF;
            stateB = (stateB * 0x35 + clz8(stateA) & 0xFF);
            stateA = (stateA * 0x65 + 0x01 & 0xFF);
            joined = (joined << 8 & 0x0000FF00) | result;
        }
        int endA = stateA, endB = stateB;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = (rotate8(stateA, 5) + stateB) & 0xFF;
            stateB = (stateB * 0x35 + clz8(stateA) & 0xFF);
            stateA = (stateA * 0x65 + 0x01 & 0xFF);

            all.getAndIncrement((joined = (joined << 8 & 0x0000FF00) | result), 0, 1);
            if (stateA == endA && stateB == endB) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 16) + " 2-tuples were present.");
        System.out.println(100.0 - all.size() * 0x64p-16 + "% of 2-tuples were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(1000, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a 2-tuple to the number of 2-tuples that repeated that often:");
        System.out.println(inv.toString(" 2-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 2-tuples.");
    }

    /**
     * Period was 0x00010000
     * Took 11 ms.
     * 256/256 results were present.
     * 0.0% of results were missing.
     * Number of repetitions of a result to the number of results that repeated that often:
     * 256 repetitions occurred for 256 2-tuples.
     */
    @Test
    public void checkSingleFrequencyCounterWithLCG8CLZ8() {
        long startTime = System.currentTimeMillis();
        final IntIntMap all = new IntIntMap(1 << 16, 0.6f);
        int stateA = 1, stateB = 1;
        for (int g = 0; g < 20; g++) {
            stateB = (stateB * 0x35 + clz8(stateA) & 0xFF);
            stateA = (stateA * 0x65 + 0x01 & 0xFF);
        }
        int endA = stateA, endB = stateB;

        long i = 0L;
        while (++i <= 0x10000100L) {
            int result = (rotate8(stateA, 5) + stateB) & 0xFF;
            stateB = (stateB * 0x35 + clz8(stateA) & 0xFF);
            stateA = (stateA * 0x65 + 0x01 & 0xFF);
            all.getAndIncrement(result, 0, 1);
            if (stateA == endA && stateB == endB) {
                break;
            }
        }
        System.out.printf("Period was 0x%08X\nTook %d ms.\n", i, (System.currentTimeMillis() - startTime));
        System.out.println(all.size() + "/" + (1 << 8) + " results were present.");
        System.out.println(100.0 - all.size() * 0x64p-8 + "% of results were missing.");
        IntIntOrderedMap inv = new IntIntOrderedMap(1000, 0.6f);
        for(IntIntMap.Entry ent : all){
            inv.getAndIncrement(ent.value, 0, 1);
        }
        inv.sort(IntComparators.NATURAL_COMPARATOR);
        System.out.println("Number of repetitions of a result to the number of results that repeated that often:");
        System.out.println(inv.toString(" 2-tuples.\n", " repetitions occurred for ", false, Base::appendReadable, Base::appendReadable) + " 2-tuples.");
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
