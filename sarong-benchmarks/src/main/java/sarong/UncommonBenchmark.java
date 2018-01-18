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
 * both a "more traditional" method using a quadratic curve adapted from
 * <a href="http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve/">this blog</a>, and a newer
 * technique that is able to avoid one of the slowest parts of the curve method (it uses bitwise operations on the
 * representation of the double or float input as a long or int to be able to handle out-of-range inputs). The quality
 * of the approximation in measureCosApproxClimatiano is best, followed by measureCosApprox (which is the fastest), then
 * measureCosApproxClimatianoLP (which is much worse, probably not good enough of an approximation for many tasks).
 * Calculating the sin() approximation is pretty much the same speed as cos(), within the margin of error.
 * <br>
 * Benchmark                                       Mode  Cnt    Score   Error  Units
 * UncommonBenchmark.measureCosApprox              avgt    5    8.398 ± 0.050  ns/op // approximation with doubles
 * UncommonBenchmark.measureCosApproxClimatiano    avgt    5   39.962 ± 0.753  ns/op // high precision quadratic curve
 * UncommonBenchmark.measureCosApproxClimatianoLP  avgt    5   35.040 ± 0.161  ns/op // low precision quadratic curve
 * UncommonBenchmark.measureCosApproxFloat         avgt    5    8.638 ± 0.173  ns/op // approximation with floats
 * UncommonBenchmark.measureMathCos                avgt    5  384.818 ± 2.157  ns/op // Math.cos(x)
 * UncommonBenchmark.measureMathCosStrict          avgt    5  384.922 ± 3.201  ns/op // StrictMath.cos(x)
 * UncommonBenchmark.measureMathSin                avgt    5  383.923 ± 1.560  ns/op // Math.sin(x)
 * UncommonBenchmark.measureSinApprox              avgt    5    8.407 ± 0.126  ns/op // approximation with doubles
 * UncommonBenchmark.measureSinApproxFloat         avgt    5    8.510 ± 0.073  ns/op // approximation with floats
 * <br>
 * This shows the approximation is at last 40x faster than Math.cos(), though it's by less than a microsecond per call.
 * See <a href="https://www.desmos.com/calculator/lgozarjsq3">this graph, using the Desmos graphing calculator</a> for
 * a comparison of how closely the approximation matches; Math.cos() is in red and the approximation is in orange, with
 * blue showing an alternate attempt that isn't as fast or as accurate. The red and orange lines should almost overlap
 * except at fairly high zoom levels.
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class UncommonBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

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

    private LightRNG Light = new LightRNG(9999L);
    private RNG LightR = new RNG(Light);
    @Benchmark
    public long measureLight()
    {
        return Light.nextLong();
    }

    @Benchmark
    public long measureLightInt()
    {
        return Light.next(32);
    }
    @Benchmark
    public long measureLightR()
    {
        return LightR.nextLong();
    }

    @Benchmark
    public long measureLightIntR()
    {
        return LightR.nextInt();
    }





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

    private double wave = 1.0;

    @Benchmark
    public double measureMathCos()
    {
        return Math.cos((wave += 0.0625));
        //return Math.cos((wave += 0.0625) * 3.141592653589793);
    }

    private double waveSin = 1.0;

    @Benchmark
    public double measureMathSin()
    {
        return Math.sin((waveSin += 0.0625));
        //return Math.cos((wave += 0.0625) * 3.141592653589793);
    }

    @Benchmark
    public double measureMathCosStrict()
    {
        return StrictMath.cos((wave += 0.0625));
        //return StrictMath.cos((wave += 0.0625) * 3.141592653589793);
    }

    private double waveApprox = 1.0;
    @Benchmark
    public double measureCosApprox() {
        return NumberTools.cos(waveApprox += 0.0625);
//        waveApprox += 0.0625;
//        final long s = Double.doubleToLongBits(waveApprox * 0.3183098861837907 + (waveApprox < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * -2.0 + 1.0;
    }
    private double sinApprox = 1.0;
    @Benchmark
    public double measureSinApprox() {
        return NumberTools.sin(sinApprox += 0.0625);
//        sinApprox += 0.0625;
//        final long s = Double.doubleToLongBits(sinApprox * 0.3183098861837907 + (sinApprox < -1.5707963267948966 ? -1.5 : 2.5)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }

    private float waveFloat = 1f;

    @Benchmark
    public float measureCosApproxFloat() {
        return NumberTools.cos(waveFloat += 0.0625f);
    }

    private float sinFloat = 1f;

    @Benchmark
    public float measureSinApproxFloat() {
        return NumberTools.sin(sinFloat += 0.0625f);
    }


    private float waveClimatiano = 1f;

    /**
     * Climatiano code is adapted from <a href="http://www.mclimatiano.com/faster-sine-approximation-using-quadratic-curve/">this blog</a>.
     * @return an approximation of cosine, with the argument in radians
     */
    @Benchmark
    public float measureCosApproxClimatiano() {
        float cos = ((((waveClimatiano += 0.0625f) < 0 ? -waveClimatiano : waveClimatiano) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //float cos = (((waveClimatiano += 0.0625f) * (waveClimatiano < 0 ? -3.141592653589793f : 3.141592653589793f) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //float cos = (((((waveClimatianoLP += 0.0625f) * 3.141592653589793f - 1.5707963267948966f) % 6.283185307179586f) + 6.283185307179586f) % 6.283185307179586f) - 3.141592653589793f;
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
    private float waveClimatianoLP = 1f;
    @Benchmark
    public float measureCosApproxClimatianoLP()
    {
        //final float cos = (((waveClimatianoLP += 0.0625f) * (waveClimatianoLP < 0 ? -3.141592653589793f : 3.141592653589793f) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        final float cos = ((((waveClimatianoLP += 0.0625f) < 0 ? -waveClimatianoLP : waveClimatianoLP) + 4.71238898038469f) % 6.283185307179586f) - 3.141592653589793f;
        //final float cos = (((waveClimatianoLP += 0.0625f) * 3.141592653589793f % 6.283185307179586f) - 4.71238898038469f) % 3.141592653589793f;
        //final float cos = (((((waveClimatianoLP += 0.0625f) * 3.141592653589793f - 1.5707963267948966f) % 6.283185307179586f) + 6.283185307179586f) % 6.283185307179586f) - 3.141592653589793f;
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
        double mathError = 0.0, approxError = 0.0, approxFError = 0.0, climatianoError = 0.0, clLPError = 0.0,
                sinApproxError = 0.0, sinApproxFError = 0.0,
                floatError = 0.0, margin = 0.0;
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.cos()       : " + u.measureMathSin());
        System.out.println("double sin approx: " + u.measureSinApprox());
        System.out.println("double cos approx:" + u.measureCosApprox());
//        System.out.println("float approx     : " + u.measureCosApproxFloat());
//        System.out.println("Climatiano       : " + u.measureCosApproxClimatiano());
//        System.out.println("ClimatianoLP     : " + u.measureCosApproxClimatianoLP());
        for (double i = -64.0; i <= 64.0; i+= 0x1p-5) {
            margin += 0.0001;
            u.wave = i;
            u.waveSin = i;
            u.waveApprox = i;
            u.sinApprox = i;
            u.waveFloat = (float)i;
            u.sinFloat = (float)i;
            u.waveClimatiano = (float)i;
            u.waveClimatianoLP = (float)i;
            double c = u.measureMathCosStrict(), s = u.measureMathSin();
            u.wave = i;
            mathError += Math.abs(u.measureMathCos() - c);
            floatError += Math.abs(c - (float)c);
            approxError += Math.abs(u.measureCosApprox() - c);
            approxFError += Math.abs(u.measureCosApproxFloat() - c);
            climatianoError += Math.abs(u.measureCosApproxClimatiano() - c);
            clLPError += Math.abs(u.measureCosApproxClimatianoLP() - c);
            sinApproxError += Math.abs(u.measureSinApprox() - s);
            sinApproxFError += Math.abs(u.measureSinApproxFloat() - s);
        }
        System.out.println("Margin allowed   : " + margin);
        System.out.println("Math.cos()       : " + mathError);
        System.out.println("double approx    : " + approxError);
        System.out.println("base float error : " + floatError);
        System.out.println("float approx     : " + approxFError);
        System.out.println("Climatiano       : " + climatianoError);
        System.out.println("Climatiano LP    : " + clLPError);
        System.out.println("sin approx       : " + sinApproxError);
        System.out.println("sin approx float : " + sinApproxFError);

    }
}
