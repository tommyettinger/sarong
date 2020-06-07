/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package sarong;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import sarong.discouraged.*;

import java.util.concurrent.TimeUnit;

/**
 * Mostly, this is just benchmarks of rarely-used or slower generators. However, there's also a test of a somewhat-new
 * method of approximating the shape of Math.cos() and Math.sin(). The performance results here are startling, trying
 * a "more traditional" method using a quadratic curve adapted from
 * <a href="http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve/">this blog</a>, a technique that
 * is able to avoid one of the slowest parts of the curve method (it uses bitwise operations on the representation of
 * the double or float input as a long or int to be able to handle out-of-range inputs), and the current default of a
 * mix of the two (measureCosApproxNick2, based more on the original forum thread that inspired Climatiano's blog post,
 * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">available here</a>,
 * but incorporating the bitwise technique used earlier; this is the version used in {@link NumberTools}). The quality
 * of the approximation in measureCosApproxNick2 is best by far (distance of roughly 2 from the results of 4097 calls),
 * then measureCosApproxClimatiano (distance of roughly 12), followed by measureCosApproxOld (distance of roughly 47), then
 * measureCosApproxClimatianoLP (distance of 122, probably not good enough of an approximation for many tasks). The
 * speed of measureCosApproxNick2 is almost identical to the older, much-less-precise measureCosApproxOld, in a dead heat
 * for first place. Each sin() approximation is pretty much the same speed as cos(), within the margin of error.
 * <br>
 * Testing using sequential doubles/floats going up from 1.0:
 * <pre>
 * Benchmark                                       Mode  Cnt    Score    Error  Units
 * UncommonBenchmark.measureCosApproxOld           avgt    5    8.951 ±  2.169  ns/op // old approximation with doubles
 * UncommonBenchmark.measureCosApproxClimatiano    avgt    5   39.926 ±  0.589  ns/op // mid precision quadratic curve
 * UncommonBenchmark.measureCosApproxClimatianoLP  avgt    5   35.233 ±  0.769  ns/op // low precision quadratic curve
 * UncommonBenchmark.measureCosApproxFloat         avgt    5    8.800 ±  0.752  ns/op // new approximation with floats
 * UncommonBenchmark.measureCosApproxNick2         avgt    5    8.518 ±  0.346  ns/op // new approximation with doubles
 * UncommonBenchmark.measureMathCos                avgt    5  387.259 ± 12.720  ns/op // Math.cos(x)
 * UncommonBenchmark.measureMathCosStrict          avgt    5  391.382 ± 22.580  ns/op // StrictMath.cos(x)
 * UncommonBenchmark.measureMathSin                avgt    5  385.910 ± 15.013  ns/op // Math.sin(x)
 * UncommonBenchmark.measureSinApproxOld           avgt    5    8.503 ±  0.463  ns/op // old approximation with doubles
 * UncommonBenchmark.measureSinApproxFloat         avgt    5    8.583 ±  0.260  ns/op // new approximation with floats
 * UncommonBenchmark.measureSinApproxNick2         avgt    5    8.706 ±  0.278  ns/op // new approximation with doubles
 * </pre>
 * <br>
 * Testing using sequentially-picked, randomly-produced floats and doubles from an array with values from -2048 to 2048:
 * <pre>
 * Benchmark                                       Mode  Cnt   Score   Error  Units
 * UncommonBenchmark.measureBaseline               avgt    5   3.733 ± 0.226  ns/op
 * UncommonBenchmark.measureCosApproxOld           avgt    5  15.159 ± 0.465  ns/op
 * UncommonBenchmark.measureCosApproxClimatiano    avgt    5  50.581 ± 1.408  ns/op
 * UncommonBenchmark.measureCosApproxClimatianoLP  avgt    5  49.684 ± 1.181  ns/op
 * UncommonBenchmark.measureCosApproxFloat         avgt    5  15.671 ± 1.169  ns/op
 * UncommonBenchmark.measureCosApproxNick2         avgt    5  16.068 ± 0.799  ns/op
 * UncommonBenchmark.measureMathCos                avgt    5  53.715 ± 0.635  ns/op
 * UncommonBenchmark.measureMathSin                avgt    5  54.113 ± 3.777  ns/op
 * UncommonBenchmark.measureSinApproxOld           avgt    5  15.121 ± 0.245  ns/op
 * UncommonBenchmark.measureSinApproxFloat         avgt    5  15.496 ± 0.518  ns/op
 * UncommonBenchmark.measureSinApproxNick2         avgt    5  16.158 ± 0.981  ns/op
 * </pre>
 * <br>
 * This shows the approximation can be 40x faster than Math.cos(), though only when Math.cos() fails to optimize
 * correctly in the first block of benchmarks. It is about 4x faster without that, in the second block. The
 * approximations are also slower when branch prediction frequently fails; measureSinApproxNick2 is about half the speed
 * when its input may be either positive or negative, compared to when its input is only positive. This suggests that
 * use of the approximations should favor positive arguments (such as between 0 and 2 * PI instead of between -PI and
 * PI). See <a href="https://www.desmos.com/calculator/g0ebg0fjmr">this graph, using the Desmos graphing calculator</a>
 * for a comparison of how closely the approximation matches; Math.sin() is in green and the approximation is in black.
 * The green and black lines should almost overlap except at extremely high zoom levels.
 * <br>
 * For the inverse trigonometric functions asin() and acos(), using an approximation can really help on performance.
 * There's two options tested here as approximations, DKC from formula 201 in
 * <a href="http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis Kjaer Christensen's ArcSin approximations</a>,
 * and JOH, from apparently a well-known trigonometric identity but learned from John O'Harrow's submissions to
 * <a href="http://www.fastcode.dk/fastcodeproject/fastcodeproject/55.htm">a Delphi coding challenge to optimize ArcSin and ArcCos</a>.
 * They are compared to {@link Math#asin(double)} in measureAsinJDK() and similarly for acos().
 * <pre>
 * Benchmark                         Mode  Cnt    Score   Error  Units
 * UncommonBenchmark.measureAcosDKC  avgt    5    7.298 ± 0.074  ns/op
 * UncommonBenchmark.measureAcosJDK  avgt    5  344.364 ± 1.904  ns/op
 * UncommonBenchmark.measureAcosJOH  avgt    5   20.739 ± 1.329  ns/op
 * UncommonBenchmark.measureAsinDKC  avgt    5    6.316 ± 0.058  ns/op
 * UncommonBenchmark.measureAsinJDK  avgt    5  355.110 ± 2.817  ns/op
 * UncommonBenchmark.measureAsinJOH  avgt    5   19.720 ± 0.499  ns/op
 * </pre>
 * <br>
 * Yeah, the JDK versions are <i>really slow</i>. The JOH version has almost no total error on 65536 tested floats, with
 * an absolute total error of 4.2537 radians, while DKC is faster but has a much higher absolute total error, 562.7881
 * radians for acos(). The JOH version is built on an optimized version of an atan2() approximation that doesn't check
 * for cases that are impossible with asin() or acos(). The DKC version is based on a complicated polynomial.
 * <br>
 * Shuffling distinct int sequences:
 * <br>
 * <pre>
 * Benchmark                                  Mode  Cnt   Score   Error  Units
 * UncommonBenchmark.measureSIS_1024_Bound    avgt    5  11.627 ± 0.046  ns/op
 * UncommonBenchmark.measureSIS_1025_Bound    avgt    5  33.014 ± 0.393  ns/op
 * UncommonBenchmark.measureSIS_16_Bound      avgt    5  12.873 ± 0.050  ns/op
 * UncommonBenchmark.measureSIS_17_Bound      avgt    5  45.768 ± 0.201  ns/op
 * UncommonBenchmark.measureSIS_256_Bound     avgt    5  11.300 ± 0.135  ns/op
 * UncommonBenchmark.measureSIS_257_Bound     avgt    5  37.772 ± 0.186  ns/op
 * UncommonBenchmark.measureSNSIS_1024_Bound  avgt    5  25.618 ± 0.417  ns/op
 * UncommonBenchmark.measureSNSIS_1025_Bound  avgt    5  25.309 ± 0.134  ns/op
 * UncommonBenchmark.measureSNSIS_16_Bound    avgt    5  26.337 ± 0.182  ns/op
 * UncommonBenchmark.measureSNSIS_17_Bound    avgt    5  26.449 ± 0.483  ns/op
 * UncommonBenchmark.measureSNSIS_256_Bound   avgt    5  25.116 ± 0.209  ns/op
 * UncommonBenchmark.measureSNSIS_257_Bound   avgt    5  25.417 ± 0.131  ns/op
 * </pre>
 * This is with 2 rounds for SIS (ShuffledIntSequence) and 7 rounds for SNSIS (SNShuffledIntSequence).
 * If you instead use 3 rounds for SNSIS, you get the following:
 * <pre>
 * Benchmark                                  Mode  Cnt   Score   Error  Units
 * UncommonBenchmark.measureSIS_1024_Bound    avgt    5  11.575 ± 0.259  ns/op
 * UncommonBenchmark.measureSIS_1025_Bound    avgt    5  33.425 ± 0.694  ns/op
 * UncommonBenchmark.measureSIS_16_Bound      avgt    5  12.753 ± 0.064  ns/op
 * UncommonBenchmark.measureSIS_17_Bound      avgt    5  45.023 ± 0.786  ns/op
 * UncommonBenchmark.measureSIS_256_Bound     avgt    5  11.366 ± 0.465  ns/op
 * UncommonBenchmark.measureSIS_257_Bound     avgt    5  36.702 ± 0.158  ns/op
 * UncommonBenchmark.measureSNSIS_1024_Bound  avgt    5  11.664 ± 0.061  ns/op
 * UncommonBenchmark.measureSNSIS_1025_Bound  avgt    5  11.631 ± 0.291  ns/op
 * UncommonBenchmark.measureSNSIS_16_Bound    avgt    5  12.442 ± 0.551  ns/op
 * UncommonBenchmark.measureSNSIS_17_Bound    avgt    5  12.373 ± 0.250  ns/op
 * UncommonBenchmark.measureSNSIS_256_Bound   avgt    5  12.010 ± 0.170  ns/op
 * UncommonBenchmark.measureSNSIS_257_Bound   avgt    5  11.999 ± 0.031  ns/op
 * </pre>
 * But, the rounds for SNSIS are very simple, so it needs more to be suitably random.
 * It seems like 3 rounds is enough for very small bounds, but even at a bound of 30 it's not even close to enough.
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class UncommonBenchmark {

    private final double[] inputs = new double[65536];
    private final float[] floatInputs = new float[65536];
    private final double[] arcInputs = new double[65536];
    {
        for (int i = 0; i < 65536; i++) {
            floatInputs[i] = (float) (inputs[i] =
                    (DiverRNG.determine(i) >> 11) * 0x1p-40);
            arcInputs[i] = (DiverRNG.determine(i) >> 9) * 0x1p-54;
        }
    }


    public static double determineB(final int base, final int index) {
        if (base <= 2) {
            return (Integer.reverse(index + 1) >>> 1) * 0x1p-31;
        }
        final int s = (index + 1 & 0x7fffffff);
        int num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / (double) den;
    }

    private int VDC3_A = 0, VDC5_A = 0, VDC19_A = 0, VDC0xDE4D_A = 0,
            VDC3_B = 0, VDC5_B = 0, VDC19_B = 0, VDC0xDE4D_B = 0;
    @Benchmark
    public double measureVDC3_A()
    {
        return VanDerCorputQRNG.determine(3, VDC3_A++);
    }
    @Benchmark
    public double measureVDC5_A()
    {
        return VanDerCorputQRNG.determine(5, VDC5_A++);
    }
    @Benchmark
    public double measureVDC19_A()
    {
        return VanDerCorputQRNG.determine(19, VDC19_A++);
    }
    @Benchmark
    public double measureVDC0xDE4D_A()
    {
        return VanDerCorputQRNG.determine(0xDE4D, VDC0xDE4D_A++);
    }
    @Benchmark
    public double measureVDC3_B()
    {
        return determineB(3, VDC3_B++);
    }
    @Benchmark
    public double measureVDC5_B()
    {
        return determineB(5, VDC5_B++);
    }
    @Benchmark
    public double measureVDC19_B()
    {
        return determineB(19, VDC19_B++);
    }
    @Benchmark
    public double measureVDC0xDE4D_B()
    {
        return determineB(0xDE4D, VDC0xDE4D_B++);
    }
    

    private LFSR lfsr = new LFSR(9999L);
    private RNG LFSRR = new RNG(lfsr);
    @Benchmark
    public long measureLFSR()
    {
        return lfsr.nextLong();
    }

    @Benchmark
    public long measureLFSRInt()
    {
        return lfsr.next(32);
    }
    @Benchmark
    public long measureLFSRR()
    {
        return LFSRR.nextLong();
    }

    @Benchmark
    public long measureLFSRIntR()
    {
        return LFSRR.nextInt();
    }

    @Benchmark
    public long measureTweakLFSR()
    {
        return lfsr.nextLongTweak();
    }

    @Benchmark
    public long measureXSBasic()
    {
        return lfsr.xorshift();
    }

    @Benchmark
    public long measureXSBasic2()
    {
        return lfsr.xorshift2();
    }



    private NLFSR.NLFSR27 NLFSR27 = new NLFSR.NLFSR27(9999L);
    private RNG NLFSR27R = new RNG(NLFSR27);
    @Benchmark
    public long measureNLFSR27()
    {
        return NLFSR27.nextLong();
    }

    @Benchmark
    public long measureNLFSR27Int()
    {
        return NLFSR27.next(32);
    }
    @Benchmark
    public long measureNLFSR27R()
    {
        return NLFSR27R.nextLong();
    }

    @Benchmark
    public long measureNLFSR27IntR()
    {
        return NLFSR27R.nextInt();
    }


    private NLFSR.NLFSR25 NLFSR25 = new NLFSR.NLFSR25(9999L);
    private RNG NLFSR25R = new RNG(NLFSR25);
    @Benchmark
    public long measureNLFSR25()
    {
        return NLFSR25.nextLong();
    }

    @Benchmark
    public long measureNLFSR25Int()
    {
        return NLFSR25.next(32);
    }
    @Benchmark
    public long measureNLFSR25R()
    {
        return NLFSR25R.nextLong();
    }

    @Benchmark
    public long measureNLFSR25IntR()
    {
        return NLFSR25R.nextInt();
    }

//    private final Jab63RNG jab = new Jab63RNG(9999L);
//
//    private final LinnormRNG Linnorm1 = new LinnormRNG(9999L);
//    @Benchmark
//    public long measureLinnormRangedLong65537()
//    {
//        return Linnorm1.nextLongOld(65537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLong655537655537()
//    {
//        return Linnorm1.nextLongOld(655537655537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLong7()
//    {
//        return Linnorm1.nextLongOld(7L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongUnknown()
//    {
//        return Linnorm1.nextLongOld(jab.nextLong() >>> 1);
//    }
//
//    private LinnormRNG Linnorm2 = new LinnormRNG(9999L);
//    @Benchmark
//    public long measureLinnormRangedLongOther65537()
//    {
//        return Linnorm2.nextLongOther(65537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOther655537655537()
//    {
//        return Linnorm2.nextLongOther(655537655537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOther7()
//    {
//        return Linnorm2.nextLongOther(7L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOtherUnknown()
//    {
//        return Linnorm2.nextLongOther(jab.nextLong() >>> 1);
//    }
//
//    private LinnormRNG Linnorm3 = new LinnormRNG(9999L);
//    @Benchmark
//    public long measureLinnormRangedLongOriginal65537()
//    {
//        return Linnorm3.nextLongOriginal(65537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOriginal655537655537()
//    {
//        return Linnorm3.nextLongOriginal(655537655537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOriginal7()
//    {
//        return Linnorm3.nextLongOriginal(7L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOriginalUnknown()
//    {
//        return Linnorm3.nextLongOriginal(jab.nextLong() >>> 1);
//    }
//
//    private LinnormRNG Linnorm4 = new LinnormRNG(9999L);
//    @Benchmark
//    public long measureLinnormRangedLongOroboro65537()
//    {
//        return Linnorm4.nextLong(65537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOroboro655537655537()
//    {
//        return Linnorm4.nextLong(655537655537L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOroboro7()
//    {
//        return Linnorm4.nextLong(7L);
//    }
//
//    @Benchmark
//    public long measureLinnormRangedLongOroboroUnknown()
//    {
//        return Linnorm4.nextLong(jab.nextLong() >>> 1);
//    }



    private short mathCos = -0x8000;
    private short mathSin = -0x8000;
    private short cosOld = -0x8000;
    private short sinOld = -0x8000;
    private short sinNick = -0x8000;
    private short cosNick = -0x8000;
    private short sinBit = -0x8000;
    private short cosBit = -0x8000;
    private short sinBitF = -0x8000;
    private short cosBitF = -0x8000;
    private short cosFloat = -0x8000;
    private short sinFloat = -0x8000;
    private short cosClimatiano = -0x8000;
    private short cosClimatianoLP = -0x8000;
    private short baseline = -0x8000;
    private short mathAtan2X = -0x4000;
    private short mathAtan2Y = -0x8000;
    private short atan2ApproxX = -0x4000;
    private short atan2ApproxY = -0x8000;
    private short atan2ApproxXF = -0x4000;
    private short atan2ApproxYF = -0x8000;
    private short asinDKC = -0x8000;
    private short acosDKC = -0x8000;
    private short asinJOH = -0x8000;
    private short acosJOH = -0x8000;
    private short asinJDK = -0x8000;
    private short acosJDK = -0x8000;


    @Benchmark
    public double measureBaseline()
    {
        return inputs[baseline++ & 0xFFFF];
    }



    @Benchmark
    public double measureMathCos()
    {
        return Math.cos(inputs[mathCos++ & 0xFFFF]);
        //return Math.cos((mathCos += 0.0625) * 3.141592653589793);
    }

    @Benchmark
    public double measureMathSin()
    {
//        double s = Math.sin((mathSin += 0.0625));
//        System.out.println("Math: " + s);
//        return s;
        return Math.sin(inputs[mathSin++ & 0xFFFF]);
        //return Math.cos((mathCos += 0.0625) * 3.141592653589793);
    }

    @Benchmark
    public double measureMathAtan2()
    {
        return Math.atan2(inputs[mathAtan2Y++ & 0xFFFF], inputs[mathAtan2X++ & 0xFFFF]);
    }

    @Benchmark
    public double measureApproxAtan2()
    {
        return NumberTools.atan2(inputs[atan2ApproxY++ & 0xFFFF], inputs[atan2ApproxX++ & 0xFFFF]);
    }

    @Benchmark
    public float measureApproxAtan2Float()
    {
        return NumberTools.atan2(floatInputs[atan2ApproxYF++ & 0xFFFF], floatInputs[atan2ApproxXF++ & 0xFFFF]);
    }


    @Benchmark
    public double measureCosApproxOld() {
        return cosOld(inputs[cosOld++ & 0xFFFF]);
//        cosOld += 0.0625;
//        final long s = Double.doubleToLongBits(cosOld * 0.3183098861837907 + (cosOld < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * -2.0 + 1.0;
    }

    @Benchmark
    public double measureSinApproxOld() {
        return sinOld(inputs[sinOld++ & 0xFFFF]);
//        sinOld += 0.0625;
//        final long s = Double.doubleToLongBits(sinOld * 0.3183098861837907 + (sinOld < -1.5707963267948966 ? -1.5 : 2.5)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }

    private static double sinOld(final double radians)
    {
        final long s = Double.doubleToLongBits(radians * 0.3183098861837907 + (radians < -1.5707963267948966 ? -1.5 : 2.5)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }

    private static float sinOld(final float radians)
    {
        final int s = Float.floatToIntBits(radians * 0.3183098861837907f + (radians < -1.5707963267948966f ? -1.5f : 2.5f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * (3f - 2f * a) * 2f - 1f;
    }

    private static double cosOld(final double radians)
    {
        final long s = Double.doubleToLongBits(radians * 0.3183098861837907 + (radians < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
        return a * a * (3.0 - 2.0 * a) * -2.0 + 1.0;
    }

    private static float cosOld(final float radians)
    {
        final int s = Float.floatToIntBits(radians * 0.3183098861837907f + (radians < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * (3f - 2f * a) * -2f + 1f;
    }

    @Benchmark
    public float measureCosApproxFloat() {
        return NumberTools.cos(floatInputs[cosFloat++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinApproxFloat() {
        return NumberTools.sin(floatInputs[sinFloat++ & 0xFFFF]);
    }
//    private double sinNick = 1.0;
//    @Benchmark
//    public double measureSinApproxNick()
//    {
//        double a = Math.abs(sinNick += 0.0625), n = (a % 3.141592653589793);
//        n *= 1.2732395447351628 - 0.4052847345693511 * n;
//        return n * (0.775 + 0.225 * n) * Math.signum(((a + 3.141592653589793) % 6.283185307179586) - 3.141592653589793) * Math.signum(sinNick);
//    }


    /**
     * Sine approximation code from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick".
     * @return a close approximation of the sine of an internal variable this changes by 0.0625 each time
     */
    @Benchmark
    public double measureSinApprox()
    {
        return NumberTools.sin(inputs[sinNick++ & 0xFFFF]);
    }

    @Benchmark
    public double measureCosApprox()
    {
        return  NumberTools.cos(inputs[cosNick++ & 0xFFFF]);
    }

    @Benchmark
    public double measureSinApproxNickBit()
    {
        return sinBit(inputs[sinBit++ & 0xFFFF]);
    }

    @Benchmark
    public double measureCosApproxNickBit()
    {
        return cosBit(inputs[cosBit++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinApproxNickBitF()
    {
        return sinBit(floatInputs[sinBitF++ & 0xFFFF]);
    }

    @Benchmark
    public float measureCosApproxNickBitF()
    {
        return cosBit(floatInputs[cosBitF++ & 0xFFFF]);
    }
    /**
     * A fairly-close approximation of Math.sin() that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin().  The difference between the result of this method and
     * Math.sin() should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. Coercion between float and
     * double takes about as long as this method normally takes to run, so if you have floats you should usually use
     * methods that take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(double)}.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a double between -1.0 and 1.0 (probably exclusive on -1.0, but not 1.0)
     */
    public static double sinBit(final double radians)
    {
        long sign, s;
        if(radians < 0.0) {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 - 2.0);
            sign = 1L;
        }
        else {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 + 2.0);
            sign = -1L;
        }
        final long m = (s >>> 52 & 0x7FFL) - 0x400L, sm = s << m, sn = -((sm & 0x8000000000000L) >> 51);
        double n = (Double.longBitsToDouble(((sm ^ sn) & 0xfffffffffffffL) | 0x4010000000000000L) - 4.0);
        n *= 2.0 - n;
        return n * (-0.775 - 0.225 * n) * ((sn ^ sign) | 1L);
    }

    /**
     * A fairly-close approximation of Math.sin() that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin(). The difference between the result of this method and
     * Math.sin() should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. The error for this float
     * version is extremely close to the double version, {@link NumberTools#sin(double)}, so you should choose based on what type
     * you have as input and/or want to return rather than on quality concerns. Coercion between float and double takes
     * about as long as this method normally takes to run, so if you have floats you should usually use methods that
     * take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(float)}.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a float between -1f and 1f (probably exclusive on -1f, but not 1f)
     */
    public static float sinBit(final float radians)
    {
        int sign, s;
        if(radians < 0.0f) {
            s = Float.floatToIntBits(radians * 0.3183098861837907f - 2f);
            sign = 1;
        }
        else {
            s = Float.floatToIntBits(radians * 0.3183098861837907f + 2f);
            sign = -1;
        }
        final int m = (s >>> 23 & 0xFF) - 0x80, sm = s << m, sn = -((sm & 0x00400000) >> 22);
        float n = (Float.intBitsToFloat(((sm ^ sn) & 0x007fffff) | 0x40800000) - 4f);
        n *= 2f - n;
        return n * (-0.775f - 0.225f * n) * ((sn ^ sign) | 1);
    }

    /**
     * A fairly-close approximation of Math.cos() that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * Math.cos() should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though.Coercion between float and
     * double takes about as long as this method normally takes to run, so if you have floats you should usually use
     * methods that take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(double)}.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a double between -1.0 and 1.0 (probably exclusive on 1.0, but not -1.0)
     */
    public static double cosBit(final double radians)
    {
        long sign, s;
        if(radians < -1.5707963267948966) {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 - 1.5);
            sign = 1L;
        }
        else {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 + 2.5);
            sign = -1L;
        }
        final long m = (s >>> 52 & 0x7FFL) - 0x400L, sm = s << m, sn = -((sm & 0x8000000000000L) >> 51);
        double n = (Double.longBitsToDouble(((sm ^ sn) & 0xfffffffffffffL) | 0x4010000000000000L) - 4.0);
        n *= 2.0 - n;
        return n * (-0.775 - 0.225 * n) * ((sn ^ sign) | 1L);
    }

    /**
     * A fairly-close approximation of Math.cos() that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * Math.cos() should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. The error for this float
     * version is extremely close to the double version, {@link NumberTools#cos(double)}, so you should choose based on what type
     * you have as input and/or want to return rather than on quality concerns. Coercion between float and double takes
     * about as long as this method normally takes to run, so if you have floats you should usually use methods that
     * take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(float)}.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a float between -1f and 1f (probably exclusive on 1f, but not -1f)
     */
    public static float cosBit(final float radians)
    {
        int sign, s;
        if(radians < -1.5707963267948966f) {
            s = Float.floatToIntBits(radians * 0.3183098861837907f - 1.5f);
            sign = 1;
        }
        else {
            s = Float.floatToIntBits(radians * 0.3183098861837907f + 2.5f);
            sign = -1;
        }
        final int m = (s >>> 23 & 0xFF) - 0x80, sm = s << m, sn = -((sm & 0x00400000) >> 22);
        float n = (Float.intBitsToFloat(((sm ^ sn) & 0x007fffff) | 0x40800000) - 4f);
        n *= 2f - n;
        return n * (-0.775f - 0.225f * n) * ((sn ^ sign) | 1);
    }
    /**
     * Climatiano code is adapted from <a href="http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve/">this blog</a>.
     * @return an approximation of cosine, with the argument in radians
     */
    @Benchmark
    public float measureCosApproxClimatiano() {
        float cos = floatInputs[cosClimatiano++ & 0xFFFF];
        cos = (((cos < 0 ? -cos : cos) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //float cos = (((cosClimatiano += 0.0625f) * (cosClimatiano < 0 ? -3.141592653589793f : 3.141592653589793f) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //float cos = (((((cosClimatianoLP += 0.0625f) * 3.141592653589793f - 1.5707963267948966f) % 6.283185307179586f) + 6.283185307179586f) % 6.283185307179586f) - 3.141592653589793f;
        if (cos < 0) {
            cos *= (1.2732395447351628f + 0.4052847345693511f * cos);
            if (cos < 0)
                return cos * (-0.255f * (cos + 1) + 1);
            else
                return cos * (0.255f * (cos - 1) + 1);
        } else {
            cos *= (1.2732395447351628f - 0.4052847345693511f * cos);
            if (cos < 0)
                return cos * (-0.255f * (cos + 1) + 1);
            else
                return cos * (0.255f * (cos - 1) + 1);
        }
    }
    /**
     * Climatiano code is adapted from <a href="http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve/">this blog</a>.
     * This is the low-precision variant.
     * @return an approximation of cosine, with the argument in radians
     */
    @Benchmark
    public float measureCosApproxClimatianoLP()
    {
        float cos = floatInputs[cosClimatianoLP++ & 0xFFFF];
        cos = (((cos < 0 ? -cos : cos) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //final float cos = (((cosClimatianoLP += 0.0625f) * (cosClimatianoLP < 0 ? -3.141592653589793f : 3.141592653589793f) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //final float cos = (((cosClimatianoLP += 0.0625f) * 3.141592653589793f % 6.283185307179586f) - 4.71238898038469f) % 3.141592653589793f;
        //final float cos = (((((cosClimatianoLP += 0.0625f) * 3.141592653589793f - 1.5707963267948966f) % 6.283185307179586f) + 6.283185307179586f) % 6.283185307179586f) - 3.141592653589793f;
        return cos * (1.2732395447351628f + (cos < 0 ? 0.4052847345693511f : -0.4052847345693511f) * cos);
    }
    /*
mvn clean install
java -jar target/benchmarks.jar UncommonBenchmark -wi 5 -i 5 -f 1 -gc true
     */
    public static void main2(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UncommonBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(60))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    
    public static double asinJOH(final double n)
    {
        if(n == 0.0) return 0.0;
        final double ax = Math.sqrt(1.0 - n * n), ay = Math.abs(n);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 1.57079637 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? -r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? -r : r;
        }
    }
    
    public static double acosJOH(final double n)
    {
        if(n == 1.0 || n == -1.0) return 0.0;
        final double ax = Math.abs(n), ay = Math.sqrt((1.0 + n) * (1.0 - n));
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 1.57079637 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? 3.14159274 - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? 3.14159274 - r : r;
        }
    }
    
    public static double asinDKC(double a) {
        return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) /
                (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
    }
    public static double acosDKC(double a) {
        return 1.5707963267948966 - (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) /
                (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
    }
    @Benchmark
    public double measureAsinDKC()
    {
        return asinDKC(arcInputs[asinDKC++ & 0xFFFF]);
    }
    @Benchmark
    public double measureAcosDKC()
    {
        return acosDKC(arcInputs[acosDKC++ & 0xFFFF]);
    }
    @Benchmark
    public double measureAsinJOH()
    {
        return asinJOH(arcInputs[asinJOH++ & 0xFFFF]);
    }
    @Benchmark
    public double measureAcosJOH()
    {
        return acosJOH(arcInputs[acosJOH++ & 0xFFFF]);
    }
    @Benchmark
    public double measureAsinJDK()
    {
        return Math.asin(arcInputs[asinJDK++ & 0xFFFF]);
    }
    @Benchmark
    public double measureAcosJDK()
    {
        return Math.acos(arcInputs[acosJDK++ & 0xFFFF]);
    }

    public static void main(String[] args)
    {
        UncommonBenchmark u = new UncommonBenchmark();
        double cosOldError = 0.0, cosNickFError = 0.0, climatianoError = 0.0, clLPError = 0.0, cosNickError = 0.0,
                sinOldError = 0.0, sinNickFError = 0.0,  sinNickError = 0.0,
                floatError = 0.0, cosBitError = 0.0, sinBitError = 0.0, cosBitFError = 0.0, sinBitFError = 0.0,
                asinDennis = 0.0, acosDennis = 0.0, asinJohn = 0.0, acosJohn = 0.0;
        ;
        final double iroot3 = 1.0 / Math.sqrt(3.0);
        System.out.println("Math.sin()        : " + Math.sin(iroot3));
        System.out.println("Math.cos()        : " + Math.cos(iroot3));
        System.out.println("Math.asin()       : " + Math.asin(iroot3));
        System.out.println("Math.acos()       : " + Math.acos(iroot3));

        System.out.println("double sin approx:  " + NumberTools.sin(iroot3));
        System.out.println("double cos approx:  " + NumberTools.cos(iroot3));

        System.out.println("asin approx Dennis: " + asinDKC(iroot3));
        System.out.println("acos approx Dennis: " + acosDKC(iroot3));

        System.out.println("asin approx John  : " + asinJOH(iroot3));
        System.out.println("acos approx John  : " + acosJOH(iroot3));

//        System.out.println("float approx     : " + u.measureCosApproxFloat());
//        System.out.println("Climatiano       : " + u.measureCosApproxClimatiano());
//        System.out.println("ClimatianoLP     : " + u.measureCosApproxClimatianoLP());
        for (int r = 0; r < 65536; r++) {
            //margin += 0.0001;
            short i = (short) r;//(DiverRNG.determine(r) & 0xFFFF);
            u.mathCos = i;
            u.mathSin = i;
//            u.cosOld = i;
//            u.sinOld = i;
            u.sinNick = i;
            u.cosNick = i;
//            u.sinBit = i;
//            u.cosBit = i;
//            u.sinBitF = i;
//            u.cosBitF = i;
            u.cosFloat = i;
            u.sinFloat = i;
//            u.cosClimatiano = i;
//            u.cosClimatianoLP = i;
            double c = u.measureMathCos(), s = u.measureMathSin(), ac = Math.acos(c), as = Math.asin(s);
            floatError += Math.abs(c - (float)c);
//            cosOldError += Math.abs(u.measureCosApproxOld() - c);
            cosNickFError += Math.abs(u.measureCosApproxFloat() - c);
//            climatianoError += Math.abs(u.measureCosApproxClimatiano() - c);
//            clLPError += Math.abs(u.measureCosApproxClimatianoLP() - c);
            cosNickError += Math.abs(u.measureCosApprox() - c);
//            sinOldError += Math.abs(u.measureSinApproxOld() - s);
            sinNickError += Math.abs(u.measureSinApprox() - s);
            sinNickFError += Math.abs(u.measureSinApproxFloat() - s);
            asinDennis += Math.abs(asinDKC(s) - as);
            acosDennis += Math.abs(acosDKC(c) - ac);
            asinJohn += Math.abs(asinJOH(s) - as);
            acosJohn += Math.abs(acosJOH(c) - ac);
//            cosBitError += Math.abs(u.measureCosApproxNickBit() - c);
//            sinBitError += Math.abs(u.measureSinApproxNickBit() - s);
//            cosBitFError += Math.abs(u.measureCosApproxNickBitF() - c);
//            sinBitFError += Math.abs(u.measureSinApproxNickBitF() - s);
        }
        //System.out.println("Margin allowed   : " + margin);
//        System.out.println("double approx    : " + cosOldError);
        System.out.println("base float error : " + floatError);
//        System.out.println("Climatiano       : " + climatianoError);
//        System.out.println("Climatiano LP    : " + clLPError);
//        System.out.println("sin approx       : " + sinOldError);
        System.out.println("sin Nick approx  : " + sinNickError);
        System.out.println("sin approx float : " + sinNickFError);
        System.out.println("cos Nick approx  : " + cosNickError);
        System.out.println("cos approx float : " + cosNickFError);
        System.out.println("asin Dennis      : " + asinDennis);
        System.out.println("acos Dennis      : " + acosDennis);
        System.out.println("asin John        : " + asinJohn);
        System.out.println("acos John        : " + acosJohn);
//        System.out.println("sin Bit approx   : " + sinBitError);
//        System.out.println("cos Bit approx   : " + cosBitError);
//        System.out.println("sin BitF approx  : " + sinBitFError);
//        System.out.println("cos BitF approx  : " + cosBitFError);
    }
}
