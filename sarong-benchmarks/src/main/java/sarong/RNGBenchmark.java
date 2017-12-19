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
 * Results:
 * Benchmark                           Mode  Cnt     Score     Error  Units
 * RNGBenchmark.a__measureThrust       avgt    5  1131.805 ±  12.320  ms/op
 * RNGBenchmark.a__measureThrustInt    avgt    5  1216.479 ±  11.241  ms/op
 * RNGBenchmark.a__measureThrustIntR   avgt    5  1391.226 ±  12.886  ms/op
 * RNGBenchmark.a__measureThrustR      avgt    5  1216.642 ±  11.867  ms/op
 * RNGBenchmark.measureBeard        avgt    5  1397.082 ±  10.088  ms/op
 * RNGBenchmark.measureBeardInt     avgt    5  1491.380 ±  13.591  ms/op
 * RNGBenchmark.measureBeardIntR    avgt    5  2111.136 ±  23.276  ms/op
 * RNGBenchmark.measureBeardR       avgt    5  1932.245 ±  24.188  ms/op
 * RNGBenchmark.measureHerd         avgt    5  1827.514 ±   4.833  ms/op
 * RNGBenchmark.measureHerdInt      avgt    5  1313.211 ±   4.702  ms/op
 * RNGBenchmark.measureHerdIntR     avgt    5  1763.570 ±   7.817  ms/op
 * RNGBenchmark.measureHerdR        avgt    5  2371.093 ±  15.626  ms/op
 * RNGBenchmark.measureBard            avgt    5  3171.348 ±  12.367  ms/op
 * RNGBenchmark.measureBardInt         avgt    5  1707.016 ±   7.395  ms/op
 * RNGBenchmark.measureBardIntR        avgt    5  1967.688 ±   9.738  ms/op
 * RNGBenchmark.measureBardR           avgt    5  3128.905 ±  31.023  ms/op
 * RNGBenchmark.measureBird            avgt    5  3390.618 ±  53.830  ms/op
 * RNGBenchmark.measureBirdInt         avgt    5  1478.199 ±  16.278  ms/op
 * RNGBenchmark.measureBirdIntR        avgt    5  2278.648 ±  20.423  ms/op
 * RNGBenchmark.measureBirdR           avgt    5  4798.228 ±  28.583  ms/op
 * RNGBenchmark.measureFlap            avgt    5  1396.436 ±  27.774  ms/op
 * RNGBenchmark.measureFlapInt         avgt    5   847.606 ±  41.703  ms/op
 * RNGBenchmark.measureFlapIntR        avgt    5   861.466 ±   6.423  ms/op
 * RNGBenchmark.measureFlapR           avgt    5  1414.728 ±  12.184  ms/op
 * RNGBenchmark.measureHorde           avgt    5  1320.661 ±   5.766  ms/op
 * RNGBenchmark.measureHordeInt        avgt    5  1457.654 ±  10.749  ms/op
 * RNGBenchmark.measureHordeIntR       avgt    5  1982.037 ± 257.399  ms/op
 * RNGBenchmark.measureHordeR          avgt    5  2135.787 ± 100.537  ms/op
 * RNGBenchmark.measureLap             avgt    5   610.649 ±   4.141  ms/op
 * RNGBenchmark.measureLapInt          avgt    5   872.487 ±  20.719  ms/op
 * RNGBenchmark.measureLapIntR         avgt    5   958.474 ±   3.734  ms/op
 * RNGBenchmark.measureLapR            avgt    5   698.221 ±   4.486  ms/op
 * RNGBenchmark.measureLight           avgt    5  1385.913 ±   8.300  ms/op
 * RNGBenchmark.measureLight32         avgt    5  3225.607 ±  22.691  ms/op
 * RNGBenchmark.measureLight32Int      avgt    5  1583.562 ±  14.601  ms/op
 * RNGBenchmark.measureLight32IntR     avgt    5  1713.205 ±  13.471  ms/op
 * RNGBenchmark.measureLight32R        avgt    5  3337.331 ±  18.807  ms/op
 * RNGBenchmark.measureLightInt        avgt    5  1391.640 ±   9.243  ms/op
 * RNGBenchmark.measureLightIntR       avgt    5  1560.652 ±  19.582  ms/op
 * RNGBenchmark.measureLightR          avgt    5  1387.966 ±   8.571  ms/op
 * RNGBenchmark.measureLongPeriod      avgt    5  2100.508 ±  16.138  ms/op
 * RNGBenchmark.measureLongPeriodInt   avgt    5  2119.153 ±   5.913  ms/op
 * RNGBenchmark.measureLongPeriodIntR  avgt    5  2101.944 ±  10.242  ms/op
 * RNGBenchmark.measureLongPeriodR     avgt    5  2102.563 ±  19.739  ms/op
 * RNGBenchmark.measureThunder         avgt    5   960.652 ±   9.994  ms/op
 * RNGBenchmark.measureThunderInt      avgt    5  1131.314 ±   7.729  ms/op
 * RNGBenchmark.measureThunderIntR     avgt    5  1214.030 ±  17.255  ms/op
 * RNGBenchmark.measureThunderR        avgt    5  1045.408 ±  10.720  ms/op
 * RNGBenchmark.a____measureXoRo            avgt    5  1422.306 ±   5.445  ms/op
 * RNGBenchmark.a____measureXoRoInt         avgt    5  1534.223 ±  10.123  ms/op
 * RNGBenchmark.a____measureXoRoIntR        avgt    5  1656.485 ±  71.371  ms/op
 * RNGBenchmark.a____measureXoRoR           avgt    5  1485.865 ±   2.754  ms/op
 *
 *
 * Improved benchmarks, JMH 1.19:
 * Benchmark                                 Mode  Cnt     Score    Error  Units
 * RNGBenchmark.a_________measureVortex      avgt    4  1640.233 ±  5.436  ms/op
 * RNGBenchmark.a_________measureVortexInt   avgt    4  1813.730 ± 22.128  ms/op
 * RNGBenchmark.a_________measureVortexIntR  avgt    4  1618.856 ± 11.315  ms/op
 * RNGBenchmark.a_________measureVortexR     avgt    4  1725.289 ± 24.195  ms/op
 * RNGBenchmark.a________measureAltThrust    avgt    4  1122.029 ±  8.399  ms/op // out of order, with this group:
 * RNGBenchmark.a________measureDetermine    avgt    4  1209.380 ± 14.145  ms/op
 * RNGBenchmark.a________measureJab63        avgt    4  1038.138 ± 13.519  ms/op
 * RNGBenchmark.a________measureJab63Int     avgt    4  1122.019 ± 20.745  ms/op
 * RNGBenchmark.a________measureJab63IntR    avgt    4  1123.782 ± 19.098  ms/op
 * RNGBenchmark.a________measureJab63R       avgt    4  1035.747 ± 14.015  ms/op
 * RNGBenchmark.a________measureJabWeird     avgt    4  1035.967 ± 17.499  ms/op
 * RNGBenchmark.a________measureRandomize    avgt    4  1120.833 ± 23.762  ms/op
 * RNGBenchmark.a______measureAltThrustInt   avgt    4  1206.633 ±  8.731  ms/op // measureAltThrust goes above here
 * RNGBenchmark.a______measureAltThrustIntR  avgt    4  1316.511 ± 14.109  ms/op
 * RNGBenchmark.a______measureAltThrustR     avgt    4  1276.617 ±  8.209  ms/op
 * RNGBenchmark.a______measureThrust         avgt    4   955.838 ± 11.137  ms/op // not sure on why this is an outlier
 * RNGBenchmark.a______measureThrustInt      avgt    4  1133.596 ± 27.151  ms/op
 * RNGBenchmark.a______measureThrustIntR     avgt    4  1133.384 ± 10.672  ms/op
 * RNGBenchmark.a______measureThrustR        avgt    4  1044.728 ± 21.555  ms/op
 * RNGBenchmark.measureLight                 avgt    4  1393.720 ± 18.765  ms/op
 * RNGBenchmark.measureLightInt              avgt    4  1393.613 ± 14.924  ms/op
 * RNGBenchmark.measureLightIntR             avgt    4  1567.682 ± 12.248  ms/op
 * RNGBenchmark.measureLightR                avgt    4  1396.734 ± 38.029  ms/op
 * RNGBenchmark.measureLongPeriod            avgt    4  1944.006 ± 32.163  ms/op
 * RNGBenchmark.measureLongPeriodInt         avgt    4  2125.583 ± 23.592  ms/op
 * RNGBenchmark.measureLongPeriodIntR        avgt    4  2117.233 ± 63.391  ms/op
 * RNGBenchmark.measureLongPeriodR           avgt    4  1922.065 ± 31.988  ms/op
 * RNGBenchmark.measureXoRo                  avgt    4  1379.341 ± 16.543  ms/op
 * RNGBenchmark.measureXoRoInt               avgt    4  1539.025 ± 70.004  ms/op
 * RNGBenchmark.measureXoRoIntR              avgt    4  1445.883 ± 18.645  ms/op
 * RNGBenchmark.measureXoRoR                 avgt    4  1467.185 ± 16.354  ms/op
 *
 * ThrustAltRNG is the fastest so far that passes stringent quality tests (no failures with gjrand on many seeds and few
 * seeds cause severe failures, none systematically; 32TB PractRand testing completed without failure). LightRNG passes
 * PractRand but has more frequent issues with gjrand. XoRo fails PractRand unless you disregard binary matrix rank
 * tests, as the author recommends; because gjrand can't take a test out of consideration, XoRo probably fails it fully.
 * ThrustRNG does reasonably well on gjrand but fails on PractRand at only 32GB. VortexRNG does very well on gjrand but
 * it is unknown how it does on PractRand, and it's also slower than XoRo with a smaller period.
 */
public class RNGBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

//    public long doThunder()
//    {
//        ThunderRNG rng = new ThunderRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunder() throws InterruptedException {
//        seed = 9000;
//        doThunder();
//    }
//
//    public long doThunderInt()
//    {
//        ThunderRNG rng = new ThunderRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderInt() throws InterruptedException {
//        iseed = 9000;
//        doThunderInt();
//    }
//    public long doThunderR()
//    {
//        RNG rng = new RNG(new ThunderRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderR() throws InterruptedException {
//        seed = 9000;
//        doThunderR();
//    }
//
//    public long doThunderIntR()
//    {
//        RNG rng = new RNG(new ThunderRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderIntR() throws InterruptedException {
//        iseed = 9000;
//        doThunderIntR();
//    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureXoRo()
    {
        long seed = 9000L;
        XoRoRNG rng = new XoRoRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureXoRoInt()
    {
        int iseed = 9000;
        XoRoRNG rng = new XoRoRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureXoRoR()
    {
        long seed = 9000L;
        RNG rng = new RNG(new XoRoRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureXoRoIntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new XoRoRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    /*
    public long doXar()
    {
        XarRNG rng = new XarRNG(seed);
        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXar() throws InterruptedException {
        seed = 9000;
        doXar();
    }

    public long doXarInt()
    {
        XarRNG rng = new XarRNG(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarInt() throws InterruptedException {
        iseed = 9000;
        doXarInt();
    }

    public long doXarR()
    {
        RNG rng = new RNG(new XarRNG(seed));
        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarR() throws InterruptedException {
        seed = 9000;
        doXarR();
    }

    public long doXarIntR()
    {
        RNG rng = new RNG(new XarRNG(iseed));
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarIntR() throws InterruptedException {
        iseed = 9000;
        doXarIntR();
    }
    */

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLongPeriod()
    {
        long seed = 9000L;
        LongPeriodRNG rng = new LongPeriodRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLongPeriodInt()
    {
        int iseed = 9000;
        LongPeriodRNG rng = new LongPeriodRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLongPeriodR()
    {
        long seed = 9000L;
        RNG rng = new RNG(new LongPeriodRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLongPeriodIntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new LongPeriodRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }



    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLight()
    {
        long seed = 9000L;
        LightRNG rng = new LightRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLightInt()
    {
        int iseed = 9000;
        LightRNG rng = new LightRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLightR()
    {
        long seed = 9000L;
        RNG rng = new RNG(new LightRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long measureLightIntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new LightRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    
//    public long doFlap()
//    {
//        FlapRNG rng = new FlapRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureFlap() throws InterruptedException {
//        seed = 9000;
//        doFlap();
//    }
//
//    public long doFlapInt()
//    {
//        FlapRNG rng = new FlapRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureFlapInt() throws InterruptedException {
//        iseed = 9000;
//        doFlapInt();
//    }
//
//    public long doFlapR()
//    {
//        RNG rng = new RNG(new FlapRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureFlapR() throws InterruptedException {
//        seed = 9000;
//        doFlapR();
//    }
//
//    public long doFlapIntR()
//    {
//        RNG rng = new RNG(new FlapRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureFlapIntR() throws InterruptedException {
//        iseed = 9000;
//        doFlapIntR();
//    }
//
//    public long doLap()
//    {
//        LapRNG rng = new LapRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureLap() throws InterruptedException {
//        seed = 9000;
//        doLap();
//    }
//
//    public long doLapInt()
//    {
//        LapRNG rng = new LapRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureLapInt() throws InterruptedException {
//        iseed = 9000;
//        doLapInt();
//    }
//
//    public long doLapR()
//    {
//        RNG rng = new RNG(new LapRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureLapR() throws InterruptedException {
//        seed = 9000;
//        doLapR();
//    }
//
//    public long doLapIntR()
//    {
//        RNG rng = new RNG(new LapRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureLapIntR() throws InterruptedException {
//        iseed = 9000;
//        doLapIntR();
//    }
//
//
//    public long doHorde()
//    {
//        HordeRNG rng = new HordeRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHorde() throws InterruptedException {
//        seed = 9000;
//        doHorde();
//    }
//
//    public long doHordeInt()
//    {
//        HordeRNG rng = new HordeRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHordeInt() throws InterruptedException {
//        iseed = 9000;
//        doHordeInt();
//    }
//
//    public long doHordeR()
//    {
//        RNG rng = new RNG(new HordeRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHordeR() throws InterruptedException {
//        seed = 9000;
//        doHordeR();
//    }
//
//    public long doHordeIntR()
//    {
//        RNG rng = new RNG(new HordeRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHordeIntR() throws InterruptedException {
//        iseed = 9000;
//        doHordeIntR();
//    }
//    public long doHerd()
//    {
//        HerdRNG rng = new HerdRNG((int)seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHerd() throws InterruptedException {
//        seed = 9000;
//        doHerd();
//    }
//
//    public long doHerdInt()
//    {
//        HerdRNG rng = new HerdRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHerdInt() throws InterruptedException {
//        iseed = 9000;
//        doHerdInt();
//    }
//
//    public long doHerdR()
//    {
//        RNG rng = new RNG(new HerdRNG((int)seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHerdR() throws InterruptedException {
//        seed = 9000;
//        doHerdR();
//    }
//
//    public long doHerdIntR()
//    {
//        RNG rng = new RNG(new HerdRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureHerdIntR() throws InterruptedException {
//        iseed = 9000;
//        doHerdIntR();
//    }
//
//    public long doBeard()
//    {
//        BeardRNG rng = new BeardRNG((int)seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBeard() throws InterruptedException {
//        seed = 9000;
//        doBeard();
//    }
//
//    public long doBeardInt()
//    {
//        BeardRNG rng = new BeardRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBeardInt() throws InterruptedException {
//        iseed = 9000;
//        doBeardInt();
//    }
//
//    public long doBeardR()
//    {
//        RNG rng = new RNG(new BeardRNG((int)seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBeardR() throws InterruptedException {
//        seed = 9000;
//        doBeardR();
//    }
//
//    public long doBeardIntR()
//    {
//        RNG rng = new RNG(new BeardRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBeardIntR() throws InterruptedException {
//        iseed = 9000;
//        doBeardIntR();
//    }
//
//
//    public long doBird()
//    {
//        BirdRNG rng = new BirdRNG((int)seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBird() throws InterruptedException {
//        seed = 9000;
//        doBird();
//    }
//
//    public long doBirdInt()
//    {
//        BirdRNG rng = new BirdRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBirdInt() throws InterruptedException {
//        iseed = 9000;
//        doBirdInt();
//    }
//
//    public long doBirdR()
//    {
//        RNG rng = new RNG(new BirdRNG((int)seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBirdR() throws InterruptedException {
//        seed = 9000;
//        doBirdR();
//    }
//
//    public long doBirdIntR()
//    {
//        RNG rng = new RNG(new BirdRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBirdIntR() throws InterruptedException {
//        iseed = 9000;
//        doBirdIntR();
//    }
//    public long doBard()
//    {
//        BardRNG rng = new BardRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBard() throws InterruptedException {
//        seed = 9000;
//        doBard();
//    }
//
//    public long doBardInt()
//    {
//        BardRNG rng = new BardRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBardInt() throws InterruptedException {
//        iseed = 9000;
//        doBardInt();
//    }
//
//    public long doBardR()
//    {
//        RNG rng = new RNG(new BardRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBardR() throws InterruptedException {
//        seed = 9000;
//        doBardR();
//    }
//
//    public long doBardIntR()
//    {
//        RNG rng = new RNG(new BardRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureBardIntR() throws InterruptedException {
//        iseed = 9000;
//        doBardIntR();
//    }
//
//    public long doLight32()
//    {
//        Light32RNG rng = new Light32RNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_measureLight32() throws InterruptedException {
//        seed = 9000;
//        doLight32();
//    }
//
//    public long doLight32Int()
//    {
//        Light32RNG rng = new Light32RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_measureLight32Int() throws InterruptedException {
//        iseed = 9000;
//        doLight32Int();
//    }
//
//    public long doLight32R()
//    {
//        RNG rng = new RNG(new Light32RNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_measureLight32R() throws InterruptedException {
//        seed = 9000;
//        doLight32R();
//    }
//
//    public long doLight32IntR()
//    {
//        RNG rng = new RNG(new Light32RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_measureLight32IntR() throws InterruptedException {
//        iseed = 9000;
//        doLight32IntR();
//    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a______measureThrust()
    {
        long seed = 9000L;
        ThrustRNG rng = new ThrustRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a______measureThrustInt()
    {
        long iseed = 9000;
        ThrustRNG rng = new ThrustRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a______measureThrustR()
    {
        long seed = 9000L;
        RNG rng = new RNG(new ThrustRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a______measureThrustIntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new ThrustRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    /*

    public long doThrust3()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong3();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust3() throws InterruptedException {
        seed = 9000;
        doThrust3();
    }

    public long doThrust3Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next3(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust3Int() throws InterruptedException {
        iseed = 9000;
        doThrust3Int();
    }

    public long doThrust2()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong2();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust2() throws InterruptedException {
        seed = 9000;
        doThrust2();
    }

    public long doThrust2Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next2(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust2Int() throws InterruptedException {
        iseed = 9000;
        doThrust2Int();
    }
    public long doThrust4()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed|1L);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong4();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4() throws InterruptedException {
        seed = 9000;
        doThrust4();
    }

    public long doThrust4Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed|1L);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next4(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4Int() throws InterruptedException {
        iseed = 9000;
        doThrust4Int();
    }
*/


    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureDetermine() {
        long seed = 9000L;
        long state = 9000L;
        for (int i = 0; i < 1000000007; i++) {
            seed += ThrustAltRNG.determine(++state);
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureRandomize() {
        long seed = 9000L;
        long state = 9000L;
        for (int i = 0; i < 1000000007; i++, state += 0x6C8E9CF570932BD5L) {
            seed += ThrustAltRNG.randomize(state);
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureAltThrust() {
        long seed = 9000L;
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public int a______measureAltThrustInt() {
        int iseed = 9000;
        ThrustAltRNG rng = new ThrustAltRNG(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a______measureAltThrustR() {
        long seed = 9000L;
        RNG rng = new RNG(new ThrustAltRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public int a______measureAltThrustIntR() {
        int iseed = 9000;
        RNG rng = new RNG(new ThrustAltRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureJab63()
    {
        long seed = 9000L;
        Jab63RNG rng = new Jab63RNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureJab63Int()
    {
        int iseed = 9000;
        Jab63RNG rng = new Jab63RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureJab63R()
    {
        long seed = 9000L;
        RNG rng = new RNG(new Jab63RNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureJab63IntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new Jab63RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a________measureJabWeird() {
        long seed = 9000L;
        long state = 9001L;
        for (int i = 0; i < 1000000007; i++) {
            seed += Jab63RNG.randomize(state += 0x6A5D39EAE12657BAL);
        }
        return seed;
    }





    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a_________measureVortex()
    {
        long seed = 9000L;
        VortexRNG rng = new VortexRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a_________measureVortexInt()
    {
        int iseed = 9000;
        VortexRNG rng = new VortexRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a_________measureVortexR()
    {
        long seed = 9000L;
        RNG rng = new RNG(new VortexRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public long a_________measureVortexIntR()
    {
        int iseed = 9000;
        RNG rng = new RNG(new VortexRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

//    public long doJab63()
//    {
//        Jab63RNG rng = new Jab63RNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_____measureJab63() {
//        seed = 9000;
//        doJab63();
//    }
//
//    public long doJab63Int()
//    {
//        Jab63RNG rng = new Jab63RNG(iseed);
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_____measureJab63Int() {
//        iseed = 9000;
//        doJab63Int();
//    }
//    public long doJab63R()
//    {
//        RNG rng = new RNG(new Jab63RNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_____measureJab63R() {
//        seed = 9000;
//        doJab63R();
//    }
//
//    public long doJab63IntR()
//    {
//        RNG rng = new RNG(new Jab63RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a_____measureJab63IntR() {
//        iseed = 9000;
//        doJab63IntR();
//    }
//
//    public long doThrust32()
//    {
//        Thrust32RNG rng = new Thrust32RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThrust32() {
//        seed = 9000;
//        doThrust32();
//    }
//
//    public long doThrust32Int()
//    {
//        Thrust32RNG rng = new Thrust32RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThrust32Int() {
//        iseed = 9000;
//        doThrust32Int();
//    }
//
//    public long doThrust32R()
//    {
//        RNG rng = new RNG(new Thrust32RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThrust32R() {
//        seed = 9000;
//        doThrust32R();
//    }
//
//    public long doThrust32IntR()
//    {
//        RNG rng = new RNG(new Thrust32RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThrust32IntR() {
//        iseed = 9000;
//        doThrust32IntR();
//    }

    /*
    public long doJet()
    {
        JetRNG rng = new JetRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJet() {
        seed = 9000;
        doJet();
    }

    public long doJetInt()
    {
        JetRNG rng = new JetRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetInt() {
        iseed = 9000;
        doJetInt();
    }

    public long doJetR()
    {
        RNG rng = new RNG(new JetRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetR() {
        seed = 9000;
        doJetR();
    }

    public long doJetIntR()
    {
        RNG rng = new RNG(new JetRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetIntR() {
        iseed = 9000;
        doJetIntR();
    }

    public long doLunge32()
    {
        Lunge32RNG rng = new Lunge32RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32() {
        seed = 9000;
        doLunge32();
    }

    public long doLunge32Int()
    {
        Lunge32RNG rng = new Lunge32RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32Int() {
        iseed = 9000;
        doLunge32Int();
    }

    public long doLunge32R()
    {
        RNG rng = new RNG(new Lunge32RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32R() {
        seed = 9000;
        doLunge32R();
    }

    public long doLunge32IntR()
    {
        RNG rng = new RNG(new Lunge32RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32IntR() {
        iseed = 9000;
        doLunge32IntR();
    }


    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
    public void a________measureDetermine() {
        seed = 9000;
        long state = 9000L;
        for (int i = 0; i < 1000000007; i++) {
            seed += ThrustAltRNG.determine(++state);
        }
    }

//    // Performs rather poorly, surprisingly. JIT needs method calls rather than inlined code, it looks like.
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
//    public void a________measureDetermineBare() {
//        seed = 9000;
//        long running = seed, state = 9000L;
//        for (int i = 0; i < 1000000007; i++) {
//            seed += ((state = ((running += 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22));
//        }
//    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
    public void a________measureRandomness() {
        seed = 9000;
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
    }

    */






//    public long doVortex()
//    {
//        VortexRNG rng = new VortexRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortex() {
//        seed = 9000;
//        doVortex();
//    }
//
//    public long doVortexInt()
//    {
//        VortexRNG rng = new VortexRNG(iseed);
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexInt() {
//        iseed = 9000;
//        doVortexInt();
//    }
//    public long doVortexR()
//    {
//        RNG rng = new RNG(new VortexRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexR() {
//        seed = 9000;
//        doVortexR();
//    }
//
//    public long doVortexIntR()
//    {
//        RNG rng = new RNG(new VortexRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexIntR() {
//        iseed = 9000;
//        doVortexIntR();
//    }



//    public long doSquirrel()
//    {
//        SquirrelRNG rng = new SquirrelRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrel() throws InterruptedException {
//        seed = 9000;
//        doSquirrel();
//    }
//
//    public long doSquirrelInt()
//    {
//        SquirrelRNG rng = new SquirrelRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelInt() throws InterruptedException {
//        iseed = 9000;
//        doSquirrelInt();
//    }
//
//    public long doSquirrelR()
//    {
//        RNG rng = new RNG(new SquirrelRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelR() throws InterruptedException {
//        seed = 9000;
//        doSquirrelR();
//    }
//
//    public long doSquirrelIntR()
//    {
//        RNG rng = new RNG(new SquirrelRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelIntR() throws InterruptedException {
//        iseed = 9000;
//        doSquirrelIntR();
//    }


//    public long doRule90()
//    {
//        Rule90RNG rng = new Rule90RNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90() throws InterruptedException {
//        seed = 9000;
//        doRule90();
//    }
//
//    public long doRule90Int()
//    {
//        Rule90RNG rng = new Rule90RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90Int() throws InterruptedException {
//        iseed = 9000;
//        doRule90Int();
//    }
//
//    public long doRule90R()
//    {
//        RNG rng = new RNG(new Rule90RNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90R() throws InterruptedException {
//        seed = 9000;
//        doRule90R();
//    }
//
//    public long doRule90IntR()
//    {
//        RNG rng = new RNG(new Rule90RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90IntR() throws InterruptedException {
//        iseed = 9000;
//        doRule90IntR();
//    }


    
    /*
    public long doZap()
    {
        ZapRNG rng = new ZapRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZap() throws InterruptedException {
        seed = 9000;
        doZap();
    }

    public long doZapInt()
    {
        ZapRNG rng = new ZapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapInt() throws InterruptedException {
        iseed = 9000;
        doZapInt();
    }

    public long doZapR()
    {
        RNG rng = new RNG(new ZapRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapR() throws InterruptedException {
        seed = 9000;
        doZapR();
    }

    public long doZapIntR()
    {
        RNG rng = new RNG(new ZapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapIntR() throws InterruptedException {
        iseed = 9000;
        doZapIntR();
    }




    public long doSlap()
    {
        SlapRNG rng = new SlapRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlap() throws InterruptedException {
        seed = 9000;
        doSlap();
    }

    public long doSlapInt()
    {
        SlapRNG rng = new SlapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapInt() throws InterruptedException {
        iseed = 9000;
        doSlapInt();
    }

    public long doSlapR()
    {
        RNG rng = new RNG(new SlapRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapR() throws InterruptedException {
        seed = 9000;
        doSlapR();
    }

    public long doSlapIntR()
    {
        RNG rng = new RNG(new SlapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapIntR() throws InterruptedException {
        iseed = 9000;
        doSlapIntR();
    }

*/
    
    
    
    
    
    
    
    
/*
    public long doPlaceholder()
    {
        PlaceholderRNG rng = new PlaceholderRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
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

        for (int i = 0; i < 1000000007; i++) {
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

        for (int i = 0; i < 1000000007; i++) {
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

        for (int i = 0; i < 1000000007; i++) {
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

    public long doJDK()
    {
        Random rng = new Random(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJDK() {
        seed = 9000;
        doJDK();
    }

    public long doJDKInt()
    {
        Random rng = new Random(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    //@Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJDKInt() {
        iseed = 9000;
        doJDKInt();
    }

    /*
mvn clean install
java -jar target/benchmarks.jar RNGBenchmark -wi 4 -i 4 -f 1 -gc true
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RNGBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
