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

import java.util.Random;
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
    {
        for (int i = 0; i < 65536; i++) {
            floatInputs[i] = (float) (inputs[i] =
                    //-2.0 + (i * 0.0625)
                    NumberTools.randomDouble(i + 107) * 4096.0
            );
        }

    }

    private LongPeriodRNG LongPeriod = new LongPeriodRNG(9999L);
    private RNG LongPeriodR = new RNG(LongPeriod);
    @Benchmark
    public long measureLongPeriod()
    {
        return LongPeriod.nextLong();
    }

    @Benchmark
    public long measureLongPeriodInt()
    {
        return LongPeriod.next(32);
    }
    @Benchmark
    public long measureLongPeriodR()
    {
        return LongPeriodR.nextLong();
    }

    @Benchmark
    public long measureLongPeriodIntR()
    {
        return LongPeriodR.nextInt();
    }

//    private LightRNG Light = new LightRNG(9999L);
//    private RNG LightR = new RNG(Light);
//    @Benchmark
//    public long measureLight()
//    {
//        return Light.nextLong();
//    }
//
//    @Benchmark
//    public long measureLightInt()
//    {
//        return Light.next(32);
//    }
//    @Benchmark
//    public long measureLightR()
//    {
//        return LightR.nextLong();
//    }
//
//    @Benchmark
//    public long measureLightIntR()
//    {
//        return LightR.nextInt();
//    }
//




    private IsaacRNG Isaac = new IsaacRNG(9999L);
    private RNG IsaacR = new RNG(Isaac);
    @Benchmark
    public long measureIsaac()
    {
        return Isaac.nextLong();
    }

    @Benchmark
    public long measureIsaacInt()
    {
        return Isaac.next(32);
    }
    @Benchmark
    public long measureIsaacR()
    {
        return IsaacR.nextLong();
    }

    @Benchmark
    public long measureIsaacIntR()
    {
        return IsaacR.nextInt();
    }







    private Isaac32RNG Isaac32 = new Isaac32RNG(9999L);
    private RNG Isaac32R = new RNG(Isaac32);
    @Benchmark
    public long measureIsaac32()
    {
        return Isaac32.nextLong();
    }

    @Benchmark
    public long measureIsaac32Int()
    {
        return Isaac32.next(32);
    }
    @Benchmark
    public long measureIsaac32R()
    {
        return Isaac32R.nextLong();
    }

    @Benchmark
    public long measureIsaac32IntR()
    {
        return Isaac32R.nextInt();
    }
    
    
    
    
    
/*
    public long doPlaceholder()
    {
        PlaceholderRNG rng = new PlaceholderRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholder() throws InterruptedException {
        seed = 9000;
        doPlaceholder();
    }

    public long doPlaceholderInt()
    {
        PlaceholderRNG rng = new PlaceholderRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderInt() throws InterruptedException {
        iseed = 9000;
        doPlaceholderInt();
    }

    public long doPlaceholderR()
    {
        RNG rng = new RNG(new PlaceholderRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderR() throws InterruptedException {
        seed = 9000;
        doPlaceholderR();
    }

    public long doPlaceholderIntR()
    {
        RNG rng = new RNG(new PlaceholderRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderIntR() throws InterruptedException {
        iseed = 9000;
        doPlaceholderIntR();
    }
*/


    private LFSR LFSR = new LFSR(9999L);
    private RNG LFSRR = new RNG(LFSR);
    @Benchmark
    public long measureLFSR()
    {
        return LFSR.nextLong();
    }

    @Benchmark
    public long measureLFSRInt()
    {
        return LFSR.next(32);
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



    private Random JDK = new Random(9999L);
    @Benchmark
    public long measureJDK()
    {
        return JDK.nextLong();
    }

    @Benchmark
    public long measureJDKInt()
    {
        return JDK.nextInt();
    }

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
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin().  The difference between the result of this method and
     * {@link Math#sin(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
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
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin(). The difference between the result of this method and
     * {@link Math#sin(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
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
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * {@link Math#cos(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
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
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * {@link Math#cos(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
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
    public static void main(String[] args)
    {
        UncommonBenchmark u = new UncommonBenchmark();
        double cosOldError = 0.0, approxFError = 0.0, climatianoError = 0.0, clLPError = 0.0, cosNickError = 0.0,
                sinOldError = 0.0, sinOldFError = 0.0,  sinNickError = 0.0,
                floatError = 0.0, cosBitError = 0.0, sinBitError = 0.0, cosBitFError = 0.0, sinBitFError = 0.0;
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.cos()       : " + u.measureMathCos());
        System.out.println("double sin approx: " + u.measureSinApprox());
        System.out.println("double cos approx: " + u.measureCosApprox());
//        System.out.println("float approx     : " + u.measureCosApproxFloat());
//        System.out.println("Climatiano       : " + u.measureCosApproxClimatiano());
//        System.out.println("ClimatianoLP     : " + u.measureCosApproxClimatianoLP());
        for (long r = 100L; r < 4197; r++) {
            //margin += 0.0001;
            short i = (short) (ThrustAltRNG.determine(r) & 0xFFFF);
            u.mathCos = i;
            u.mathSin = i;
            u.cosOld = i;
            u.sinOld = i;
            u.sinNick = i;
            u.cosNick = i;
            u.sinBit = i;
            u.cosBit = i;
            u.sinBitF = i;
            u.cosBitF = i;
            u.cosFloat = i;
            u.sinFloat = i;
            u.cosClimatiano = i;
            u.cosClimatianoLP = i;
            double c = u.measureMathCos(), s = u.measureMathSin();
            floatError += Math.abs(c - (float)c);
            cosOldError += Math.abs(u.measureCosApproxOld() - c);
            approxFError += Math.abs(u.measureCosApproxFloat() - c);
            climatianoError += Math.abs(u.measureCosApproxClimatiano() - c);
            clLPError += Math.abs(u.measureCosApproxClimatianoLP() - c);
            cosNickError += Math.abs(u.measureCosApprox() - c);
            sinOldError += Math.abs(u.measureSinApproxOld() - s);
            sinNickError += Math.abs(u.measureSinApprox() - s);
            sinOldFError += Math.abs(u.measureSinApproxFloat() - s);
            cosBitError += Math.abs(u.measureCosApproxNickBit() - c);
            sinBitError += Math.abs(u.measureSinApproxNickBit() - s);
            cosBitFError += Math.abs(u.measureCosApproxNickBitF() - c);
            sinBitFError += Math.abs(u.measureSinApproxNickBitF() - s);
        }
        //System.out.println("Margin allowed   : " + margin);
        System.out.println("double approx    : " + cosOldError);
        System.out.println("base float error : " + floatError);
        System.out.println("float approx     : " + approxFError);
        System.out.println("Climatiano       : " + climatianoError);
        System.out.println("Climatiano LP    : " + clLPError);
        System.out.println("sin approx       : " + sinOldError);
        System.out.println("sin approx float : " + sinOldFError);
        System.out.println("sin Nick approx  : " + sinNickError);
        System.out.println("cos Nick approx  : " + cosNickError);
        System.out.println("sin Bit approx   : " + sinBitError);
        System.out.println("cos Bit approx   : " + cosBitError);
        System.out.println("sin BitF approx  : " + sinBitFError);
        System.out.println("cos BitF approx  : " + cosBitFError);
    }
}
