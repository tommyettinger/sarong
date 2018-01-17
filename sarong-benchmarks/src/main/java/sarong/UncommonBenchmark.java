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
 * method of approximating the shape of Math.cos() (when scaled so the peaks and troughs are on even and odd integers,
 * respectively). The performance results here are startling:
 * <br>
 * Benchmark                                Mode  Cnt    Score    Error  Units
 * UncommonBenchmark.measureCosApprox       avgt    8    9.443 ±  0.553  ns/op // approximation with doubles
 * UncommonBenchmark.measureCosApproxFloat  avgt    8    9.934 ±  0.506  ns/op // approximation with floats
 * UncommonBenchmark.measureMathCos         avgt    8  431.759 ± 20.522  ns/op // Math.cos(x * Math.PI)
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

    private double wave = 1.0, waveApprox = 1.0;

    @Benchmark
    public double measureMathCos()
    {
        return Math.cos((wave += 0.0625) * 3.141592653589793);
    }

    @Benchmark
    public double measureCosApprox() {
        waveApprox += 0.0625;
        final long s = Double.doubleToLongBits(waveApprox + (waveApprox < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }
    private float waveFloat = 1f;

    @Benchmark
    public float measureCosApproxFloat() {
        waveFloat += 0.0625f;
        final int s = Float.floatToIntBits(waveFloat + (waveFloat < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * (3f - 2f * a) * 2f - 1f;
    }


    /*
mvn clean install
java -jar target/benchmarks.jar UncommonBenchmark -wi 5 -i 5 -f 1 -gc true
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UncommonBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(60))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
