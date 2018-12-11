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
 * <br>
 * <pre>
 * Benchmark                                 Mode  Cnt   Score   Error  Units
 * RNGBenchmark.measureAltThrustDetermine    avgt    5   3.887 ± 0.055  ns/op
 * RNGBenchmark.measureAltThrustRandomize    avgt    5   3.532 ± 0.035  ns/op
 * RNGBenchmark.measureFlap                  avgt    5   3.938 ± 0.024  ns/op
 * RNGBenchmark.measureFlapInt               avgt    5   3.491 ± 0.047  ns/op
 * RNGBenchmark.measureFlapIntR              avgt    5   3.951 ± 0.028  ns/op
 * RNGBenchmark.measureFlapR                 avgt    5   4.511 ± 0.038  ns/op
 * RNGBenchmark.measureInlineJab63           avgt    5   3.194 ± 0.032  ns/op  :)  best speed, not well-distributed
 * RNGBenchmark.measureInlineThrust          avgt    5   3.354 ± 0.026  ns/op
 * RNGBenchmark.measureInlineThrustAlt       avgt    5   3.532 ± 0.033  ns/op
 * RNGBenchmark.measureInlineThrustAltOther  avgt    5   3.527 ± 0.039  ns/op
 * RNGBenchmark.measureInlineVortex          avgt    5   3.622 ± 0.046  ns/op
 * RNGBenchmark.measureJDK                   avgt    5  24.726 ± 0.174  ns/op  :(  worst speed
 * RNGBenchmark.measureJDKInt                avgt    5  12.352 ± 0.057  ns/op
 * RNGBenchmark.measureJab63                 avgt    5   3.373 ± 0.016  ns/op
 * RNGBenchmark.measureJab63Int              avgt    5   3.566 ± 0.033  ns/op
 * RNGBenchmark.measureJab63IntR             avgt    5   4.069 ± 0.037  ns/op
 * RNGBenchmark.measureJab63R                avgt    5   3.756 ± 0.045  ns/op
 * RNGBenchmark.measureLap                   avgt    5   3.367 ± 0.028  ns/op
 * RNGBenchmark.measureLapInt                avgt    5   3.674 ± 0.079  ns/op
 * RNGBenchmark.measureLapIntR               avgt    5   4.128 ± 0.038  ns/op
 * RNGBenchmark.measureLapR                  avgt    5   3.870 ± 0.019  ns/op
 * RNGBenchmark.measureLight                 avgt    5   3.978 ± 0.034  ns/op
 * RNGBenchmark.measureLightInt              avgt    5   4.340 ± 0.135  ns/op
 * RNGBenchmark.measureLightIntR             avgt    5   4.892 ± 0.026  ns/op
 * RNGBenchmark.measureLightR                avgt    5   4.449 ± 0.027  ns/op
 * RNGBenchmark.measureLongPeriod            avgt    5   4.963 ± 0.058  ns/op
 * RNGBenchmark.measureLongPeriodInt         avgt    5   5.276 ± 0.044  ns/op
 * RNGBenchmark.measureLongPeriodIntR        avgt    5   5.947 ± 0.046  ns/op
 * RNGBenchmark.measureLongPeriodR           avgt    5   5.571 ± 0.026  ns/op
 * RNGBenchmark.measureThrust                avgt    5   3.542 ± 0.137  ns/op  :? unusual Error result
 * RNGBenchmark.measureThrustAlt             avgt    5   3.541 ± 0.018  ns/op  :) best quality/speed/distribution mix
 * RNGBenchmark.measureThrustAltInt          avgt    5   3.746 ± 0.045  ns/op
 * RNGBenchmark.measureThrustAltIntR         avgt    5   4.143 ± 0.019  ns/op
 * RNGBenchmark.measureThrustAltR            avgt    5   3.982 ± 0.184  ns/op
 * RNGBenchmark.measureThrustInt             avgt    5   3.609 ± 0.058  ns/op
 * RNGBenchmark.measureThrustIntR            avgt    5   4.118 ± 0.010  ns/op
 * RNGBenchmark.measureThrustR               avgt    5   3.930 ± 0.031  ns/op
 * RNGBenchmark.measureVortex                avgt    5   3.750 ± 0.018  ns/op
 * RNGBenchmark.measureVortexDetermine       avgt    5   4.595 ± 0.053  ns/op
 * RNGBenchmark.measureVortexDetermineBare   avgt    5   3.627 ± 0.071  ns/op
 * RNGBenchmark.measureVortexInt             avgt    5   4.075 ± 0.039  ns/op
 * RNGBenchmark.measureVortexIntR            avgt    5   4.831 ± 0.047  ns/op
 * RNGBenchmark.measureVortexR               avgt    5   4.298 ± 0.070  ns/op
 * RNGBenchmark.measureXoRo                  avgt    5   3.890 ± 0.016  ns/op
 * RNGBenchmark.measureXoRoInt               avgt    5   4.206 ± 0.049  ns/op
 * RNGBenchmark.measureXoRoIntR              avgt    5   4.674 ± 0.069  ns/op
 * RNGBenchmark.measureXoRoR                 avgt    5   4.206 ± 0.053  ns/op
 * </pre>
 * <br>
 * ThrustAltRNG is the fastest so far that passes stringent quality tests (no failures with gjrand on many seeds and few
 * seeds cause severe failures, none systematically; 32TB PractRand testing completed without failure). Jab63, inlined
 * in particular, is faster and still tests as having high quality, but neither it nor ThrustAltRNG can produce all
 * possible 64-bit longs. LightRNG passes PractRand but has more frequent issues with gjrand. XoRo fails PractRand
 * unless you disregard binary matrix rank tests, as the author recommends; because gjrand can't take a test out of
 * consideration, XoRo probably fails it fully. ThrustRNG does reasonably well on gjrand but fails on PractRand at only
 * 32GB. VortexRNG does very well on gjrand and passes PractRand at 32TB, but it's also slower than XoRo with a smaller
 * period on the same state.
 * <br>
 * As for the recently-added GWT-friendly generators Zig32RNG, Zag32RNG, Zog32RNG, and XoRo32RNG, the first three all
 * perform about equally well on GWT and pass PractRand, while XoRo32RNG performs very well on GWT but fails a few tests
 * in PractRand fairly early on (There are ways to eliminate the statistical quality issues, but they also slow down the
 * generator significantly). Even though Zig and Zag are similar, Zog32RNG performs quite a bit better on desktop:
 * <br>
 * <pre>
 * Benchmark                       Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureXoRo32      avgt    5  5.148 ± 0.352  ns/op
 * RNGBenchmark.measureXoRo32Int   avgt    5  3.825 ± 0.427  ns/op
 * RNGBenchmark.measureXoRo32IntR  avgt    5  4.111 ± 0.396  ns/op
 * RNGBenchmark.measureXoRo32R     avgt    5  6.029 ± 1.172  ns/op
 * RNGBenchmark.measureZag32       avgt    5  7.638 ± 1.260  ns/op
 * RNGBenchmark.measureZag32Int    avgt    5  4.732 ± 0.851  ns/op
 * RNGBenchmark.measureZag32IntR   avgt    5  5.393 ± 0.919  ns/op
 * RNGBenchmark.measureZag32R      avgt    5  8.506 ± 1.333  ns/op
 * RNGBenchmark.measureZig32       avgt    5  8.167 ± 1.734  ns/op
 * RNGBenchmark.measureZig32Int    avgt    5  4.843 ± 0.582  ns/op
 * RNGBenchmark.measureZig32IntR   avgt    5  5.573 ± 0.647  ns/op
 * RNGBenchmark.measureZig32R      avgt    5  9.015 ± 1.248  ns/op
 * RNGBenchmark.measureZog32       avgt    5  7.151 ± 1.485  ns/op
 * RNGBenchmark.measureZog32Int    avgt    5  4.488 ± 0.899  ns/op
 * RNGBenchmark.measureZog32IntR   avgt    5  5.248 ± 0.758  ns/op
 * RNGBenchmark.measureZog32R      avgt    5  7.950 ± 1.415  ns/op
 * </pre>
 * 
 * Testing the newly-added variants on XoRo32RNG called Oriole32RNG and Lathe32RNG, Lathe is the faster of the two, and
 * both beat Zog on speed (Oriole very narrowly, Lathe comfortably) while all three have about the same quality.
 * Lathe, Oriole, and Zog trounce XoRo32 on quality but are still slower than it. Oriole also has the best period of the
 * group, but isn't a StatefulRandomness, while Lathe has the same period as XoRo32 and is a StatefulRandomness.
 * <pre>
 * Benchmark                         Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureLathe32       avgt   10  5.692 ± 0.054  ns/op
 * RNGBenchmark.measureLathe32Int    avgt   10  3.971 ± 0.022  ns/op
 * RNGBenchmark.measureLathe32IntR   avgt   10  4.684 ± 0.460  ns/op
 * RNGBenchmark.measureLathe32R      avgt   10  6.456 ± 0.109  ns/op
 * RNGBenchmark.measureOriole32      avgt   10  6.168 ± 0.029  ns/op
 * RNGBenchmark.measureOriole32Int   avgt   10  4.262 ± 0.020  ns/op
 * RNGBenchmark.measureOriole32IntR  avgt   10  4.816 ± 0.038  ns/op
 * RNGBenchmark.measureOriole32R     avgt   10  6.884 ± 0.101  ns/op
 * RNGBenchmark.measureXoRo32        avgt   10  5.047 ± 0.026  ns/op
 * RNGBenchmark.measureXoRo32Int     avgt   10  3.717 ± 0.022  ns/op
 * RNGBenchmark.measureXoRo32IntR    avgt   10  4.034 ± 0.029  ns/op
 * RNGBenchmark.measureXoRo32R       avgt   10  5.749 ± 0.024  ns/op
 * RNGBenchmark.measureZog32         avgt   10  6.839 ± 0.029  ns/op
 * RNGBenchmark.measureZog32Int      avgt   10  4.305 ± 0.026  ns/op
 * RNGBenchmark.measureZog32IntR     avgt   10  4.967 ± 0.028  ns/op
 * RNGBenchmark.measureZog32R        avgt   10  7.586 ± 0.065  ns/op
 * </pre>
 * <br>
 * Testing the top 3 contenders among one-dimensionally equidistributed generators (LightRNG and LinnormRNG pass 32TB on
 * PractRand but XoRoRNG reliably fails one group of tests and sometimes fails others):
 * <pre>
 * Benchmark                       Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureLight       avgt    5  3.763 ± 0.204  ns/op
 * RNGBenchmark.measureLightInt    avgt    5  4.047 ± 0.008  ns/op
 * RNGBenchmark.measureLinnorm     avgt    5  3.442 ± 0.018  ns/op
 * RNGBenchmark.measureLinnormInt  avgt    5  3.668 ± 0.010  ns/op
 * RNGBenchmark.measureXoRo        avgt    5  3.656 ± 0.028  ns/op
 * RNGBenchmark.measureXoRoInt     avgt    5  3.941 ± 0.034  ns/op
 * ...and one result for the non-equidistributed ThrustAltRNG...
 * RNGBenchmark.measureThrustAlt   avgt    5  3.322 ± 0.053  ns/op
 * </pre>
 * Linnorm is the new best generator we have, except that it isn't a SkippingRandomness and its period is "just" 2 to
 * the 64. Every other need seems to be met by its high speed, easily-stored state, unsurpassed statistical quality, and
 * ability to produce all long values. ThrustAltRNG may be faster, but since it isn't known how many numbers it is
 * incapable of producing, it probably shouldn't be used for procedural generation. If you need to target GWT, though,
 * your needs are suddenly completely different...
 * <br>
 * GWT-compatible generators need to work with an "int" type that isn't equivalent to Java's "int" and is closer to a
 * Java "double" that gets cast to an int when bitwise operations are used on it. This JS int is about 10x-20x faster to
 * do math operations on than GWT's "long" type, which is emulated using three JS numbers internally, but you need to be
 * vigilant about the possibility of exceeding the limits of Integer.MAX_VALUE and Integer.MIN_VALUE, since math won't
 * overflow, and about precision loss if you do exceed those limits severely, since JS numbers are floating-point. So,
 * you can't safely multiply by too large of an int (I limit my multipliers to 20 bits), you need to follow up normal
 * math with bitwise math to bring any overflowing numbers back to the 32-bit range, and you should avoid longs and math
 * on them whenever possible. So here's some GWT-safe generators, measured on a desktop JDK:
 * <pre>
 * Benchmark                                  Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureDizzy32                avgt    3  7.742 ± 0.144  ns/op // 1D equidistribution
 * RNGBenchmark.measureDizzy32Int             avgt    3  5.094 ± 0.084  ns/op // 2D equidistribution
 * RNGBenchmark.measureDizzy32IntR            avgt    3  5.826 ± 0.113  ns/op // 2D equidistribution
 * RNGBenchmark.measureDizzy32R               avgt    3  8.636 ± 0.079  ns/op // 1D equidistribution
 * RNGBenchmark.measureLathe32                avgt    3  6.181 ± 0.159  ns/op // no equidistribution
 * RNGBenchmark.measureLathe32Int             avgt    3  4.409 ± 0.024  ns/op // 1D equidistribution
 * RNGBenchmark.measureLathe32IntR            avgt    3  4.791 ± 0.242  ns/op // 1D equidistribution
 * RNGBenchmark.measureLathe32R               avgt    3  7.147 ± 0.013  ns/op // no equidistribution
 * RNGBenchmark.measureOriole32               avgt    3  6.578 ± 0.058  ns/op // no equidstribution
 * RNGBenchmark.measureOriole32Int            avgt    3  4.640 ± 0.118  ns/op // 1D equidistribution
 * RNGBenchmark.measureOriole32IntR           avgt    3  5.352 ± 0.098  ns/op // 1D equidistribution
 * RNGBenchmark.measureOriole32R              avgt    3  7.729 ± 0.127  ns/op // no equidistribution
 * RNGBenchmark.measureXoshiroAra32           avgt    3  7.175 ± 0.696  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroAra32Int        avgt    3  4.953 ± 0.132  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroAra32IntR       avgt    3  5.513 ± 0.227  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroAra32R          avgt    3  7.770 ± 0.215  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroStarPhi32       avgt    3  7.294 ± 0.386  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroStarPhi32Int    avgt    3  5.032 ± 0.045  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroStarPhi32IntR   avgt    3  5.618 ± 0.064  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroStarPhi32R      avgt    3  8.017 ± 0.202  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroStarStar32      avgt    3  7.690 ± 0.127  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroStarStar32Int   avgt    3  5.210 ± 0.102  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroStarStar32IntR  avgt    3  5.856 ± 0.291  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroStarStar32R     avgt    3  8.475 ± 0.266  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroXara32          avgt    3  7.309 ± 0.083  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroXara32Int       avgt    3  5.027 ± 0.139  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroXara32IntR      avgt    3  5.567 ± 0.186  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroXara32R         avgt    3  8.075 ± 0.131  ns/op // 2D equidistribution
 * </pre>
 * And here's some of the best GWT-safe generators compared against each other, including the new Starfish generator
 * (these benchmarks were performed while a multi-threaded test was also running, so they are slower):
 * <pre>
 * Benchmark                             Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureLathe32           avgt    3  8.073 ± 0.388  ns/op // no equidistribution
 * RNGBenchmark.measureLathe32Int        avgt    3  5.780 ± 0.976  ns/op // 1D equidistribution
 * RNGBenchmark.measureLathe32IntR       avgt    3  6.358 ± 0.823  ns/op // 1D equidistribution
 * RNGBenchmark.measureLathe32R          avgt    3  9.102 ± 1.079  ns/op // no equidistribution
 * RNGBenchmark.measureStarfish32        avgt    3  8.285 ± 0.439  ns/op // 1D equidistribution
 * RNGBenchmark.measureStarfish32Int     avgt    3  5.866 ± 0.699  ns/op // 2D equidistribution
 * RNGBenchmark.measureStarfish32IntR    avgt    3  6.448 ± 1.158  ns/op // 2D equidistribution
 * RNGBenchmark.measureStarfish32R       avgt    3  9.297 ± 1.122  ns/op // 1D equidistribution
 * RNGBenchmark.measureXoshiroAra32      avgt    3  9.048 ± 1.296  ns/op // 2D equidistribution
 * RNGBenchmark.measureXoshiroAra32Int   avgt    3  6.440 ± 0.188  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroAra32IntR  avgt    3  7.181 ± 0.497  ns/op // 4D equidistribution
 * RNGBenchmark.measureXoshiroAra32R     avgt    3  9.879 ± 1.205  ns/op // 2D equidistribution
 * </pre>
 * And testing (under load, again) Starfish vs. the newer Otter generator; Otter tends to be faster on GWT because
 * multiplication isn't as fast in browsers, but it is a little slower on desktop.
 * <pre>
 * Benchmark                          Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureLathe32        avgt    3  6.328 ± 0.568  ns/op
 * RNGBenchmark.measureLathe32Int     avgt    3  4.455 ± 0.165  ns/op
 * RNGBenchmark.measureLobster32      avgt    3  6.793 ± 0.461  ns/op
 * RNGBenchmark.measureLobster32Int   avgt    3  4.601 ± 0.056  ns/op
 * RNGBenchmark.measureStarfish32     avgt    3  6.503 ± 0.109  ns/op
 * RNGBenchmark.measureStarfish32Int  avgt    3  4.505 ± 1.135  ns/op
 * </pre>
 * You can benchmark most of these in GWT for yourself on
 * <a href="https://tommyettinger.github.io/SquidLib-Demos/bench/rng/">this SquidLib-Demos page</a>; comparing "runs"
 * where higher is better is a good way of estimating how fast a generator is. Each "run" is 10,000 generated numbers.
 * Lathe32RNG is by far the best on speed if you consider both desktop and GWT, but it can't generate all possible
 * "long" values, while XoshiroAra can generate all possible "long" values with equal frequency, and even all possible
 * pairs of "long" values (less one). XoshiroAra is also surprisingly fast compared to similar Xoshiro-based generators,
 * especially since it does just as well in PractRand testing. The equidistribution comment at the end of each line
 * refers to that specific method; calling {@code myOriole32RNG.next(32)} repeatedly will produce all ints with equal
 * frequency over the full period of that generator, ((2 to the 64) - 1) * (2 to the 32), but the same is not true of
 * calling {@code myOriole32RNG.nextLong()}, which would produce some longs more frequently than others (and probably
 * would not produce some longs at all). The Xoshiro-based generators have the best period and equidistribution
 * qualities, with XoshiroAra having the best performance (all of them pass 32TB of PractRand). The Xoroshiro-based
 * generators tend to be faster, but only Starfish has better equidistribution of the bunch (it can produce all longs
 * except one, while Lathe can't and Oriole won't with equal frequency). Starfish seems to have comparable quality and
 * speed relative to Oriole (excellent and pretty good, respectively), but improves on its distribution at the expense
 * of its period, and has a smaller state.
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
public class RNGBenchmark {

    private long state = 9000, stream = 9001, oddState = 9999L;

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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunder() throws InterruptedException {
//        seed = 9000;
//        doThunder();
//    }
//
//    public int doThunderInt()
//    {
//        ThunderRNG rng = new ThunderRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderR() throws InterruptedException {
//        seed = 9000;
//        doThunderR();
//    }
//
//    public int doThunderIntR()
//    {
//        RNG rng = new RNG(new ThunderRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderIntR() throws InterruptedException {
//        iseed = 9000;
//        doThunderIntR();
//    }

    private XoRoRNG XoRo = new XoRoRNG(9999L);
    private RNG XoRoR = new RNG(XoRo);
    @Benchmark
    public long measureXoRo()
    {
        return XoRo.nextLong();
    }

    @Benchmark
    public int measureXoRoInt()
    {
        return XoRo.next(32);
    }
    @Benchmark
    public long measureXoRoR()
    {
        return XoRoR.nextLong();
    }

    @Benchmark
    public int measureXoRoIntR()
    {
        return XoRoR.nextInt();
    }


    private Lathe64RNG Lathe64 = new Lathe64RNG(9999L);
    private RNG Lathe64R = new RNG(Lathe64);
    @Benchmark
    public long measureLathe64()
    {
        return Lathe64.nextLong();
    }

    @Benchmark
    public int measureLathe64Int()
    {
        return Lathe64.next(32);
    }
    @Benchmark
    public long measureLathe64R()
    {
        return Lathe64R.nextLong();
    }

    @Benchmark
    public int measureLathe64IntR()
    {
        return Lathe64R.nextInt();
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXar() throws InterruptedException {
        seed = 9000;
        doXar();
    }

    public int doXarInt()
    {
        XarRNG rng = new XarRNG(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarR() throws InterruptedException {
        seed = 9000;
        doXarR();
    }

    public int doXarIntR()
    {
        RNG rng = new RNG(new XarRNG(iseed));
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarIntR() throws InterruptedException {
        iseed = 9000;
        doXarIntR();
    }
    */

    private LongPeriodRNG LongPeriod = new LongPeriodRNG(9999L);
    private RNG LongPeriodR = new RNG(LongPeriod);
    @Benchmark
    public long measureLongPeriod()
    {
        return LongPeriod.nextLong();
    }

    @Benchmark
    public int measureLongPeriodInt()
    {
        return LongPeriod.next(32);
    }
    @Benchmark
    public long measureLongPeriodR()
    {
        return LongPeriodR.nextLong();
    }

    @Benchmark
    public int measureLongPeriodIntR()
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
    public int measureLightInt()
    {
        return Light.next(32);
    }
    @Benchmark
    public long measureLightR()
    {
        return LightR.nextLong();
    }

    @Benchmark
    public int measureLightIntR()
    {
        return LightR.nextInt();
    }
    
    private PaperweightRNG Paperweight = new PaperweightRNG(9999L);
    private RNG PaperweightR = new RNG(Paperweight);
    @Benchmark
    public long measurePaperweight()
    {
        return Paperweight.nextLong();
    }

    @Benchmark
    public int measurePaperweightInt()
    {
        return Paperweight.next(32);
    }
    @Benchmark
    public long measurePaperweightR()
    {
        return PaperweightR.nextLong();
    }

    @Benchmark
    public int measurePaperweightIntR()
    {
        return PaperweightR.nextInt();
    }
    
    private FlapRNG Flap = new FlapRNG(9999L);
    private RNG FlapR = new RNG(Flap);
    @Benchmark
    public long measureFlap()
    {
        return Flap.nextLong();
    }

    @Benchmark
    public int measureFlapInt()
    {
        return Flap.next(32);
    }
    @Benchmark
    public long measureFlapR()
    {
        return FlapR.nextLong();
    }

    @Benchmark
    public int measureFlapIntR()
    {
        return FlapR.nextInt();
    }

    private LapRNG Lap = new LapRNG(9999L);
    private RNG LapR = new RNG(Lap);
    @Benchmark
    public long measureLap()
    {
        return Lap.nextLong();
    }

    @Benchmark
    public int measureLapInt()
    {
        return Lap.next(32);
    }
    @Benchmark
    public long measureLapR()
    {
        return LapR.nextLong();
    }
    private ThunderRNG Thunder = new ThunderRNG(9999L);
    private RNG ThunderR = new RNG(Thunder);
    @Benchmark
    public long measureThunder()
    {
        return Thunder.nextLong();
    }

    @Benchmark
    public int measureThunderInt()
    {
        return Thunder.next(32);
    }
    @Benchmark
    public long measureThunderR()
    {
        return ThunderR.nextLong();
    }

    @Benchmark
    public int measureThunderIntR()
    {
        return ThunderR.nextInt();
    }


    @Benchmark
    // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public int measureLapIntR()
    {
        return LapR.nextInt();
    }

    private SeaSlater64RNG SeaSlater64 = new SeaSlater64RNG(9999L);
    private RNG SeaSlater64R = new RNG(SeaSlater64);
    @Benchmark
    public long measureSeaSlater64()
    {
        return SeaSlater64.nextLong();
    }

    @Benchmark
    public int measureSeaSlater64Int()
    {
        return SeaSlater64.next(32);
    }
    @Benchmark
    public long measureSeaSlater64R()
    {
        return SeaSlater64R.nextLong();
    }

    @Benchmark
    public int measureSeaSlater64IntR()
    {
        return SeaSlater64R.nextInt();
    }


    private ThrustRNG Thrust = new ThrustRNG(9999L);
    private RNG ThrustR = new RNG(Thrust);
    @Benchmark
    public long measureThrust()
    {
        return Thrust.nextLong();
    }

    @Benchmark
    public int measureThrustInt()
    {
        return Thrust.next(32);
    }
    @Benchmark
    public long measureThrustR()
    {
        return ThrustR.nextLong();
    }

    @Benchmark
    public int measureThrustIntR()
    {
        return ThrustR.nextInt();
    }

    //@Benchmark
    public long measureInlineThrust()
    {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ z >>> 26) * 0x2545F4914F6CDD1DL;
        return z ^ z >>> 28;
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust3() throws InterruptedException {
        seed = 9000;
        doThrust3();
    }

    public int doThrust3Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next3(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust2() throws InterruptedException {
        seed = 9000;
        doThrust2();
    }

    public int doThrust2Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next2(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4() throws InterruptedException {
        seed = 9000;
        doThrust4();
    }

    public int doThrust4Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed|1L);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next4(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4Int() throws InterruptedException {
        iseed = 9000;
        doThrust4Int();
    }
*/


    @Benchmark
    public long measureAltThrustDetermine() {
        return ThrustAltRNG.determine(state++);
    }
//
//
//    @Benchmark
//    public long measureDervishDetermine() {
//        return DervishRNG.determine(state++);
//    }
//

    @Benchmark
    public long measureLightDetermine() {
        return LightRNG.determine(state++);
    }
    @Benchmark
    public long measureLinnormDetermine() {
        return LinnormRNG.determine(state++);
    }

    @Benchmark
    public long measureDirkDetermine() {
        return DirkRNG.determine(state++);
    }
//    @Benchmark
//    public long measureMotorDetermine() {
//        return MotorRNG.determine(state++);
//    }

//
//    //@Benchmark
//    public long measureVortexDetermine() {
//        return VortexRNG.determine(state++);
//    }
//
//    //@Benchmark
//    public long measureVortexDetermineBare() {
//        return VortexRNG.determineBare(state += 0x6C8E9CF570932BD5L);
//    }
//
//    @Benchmark
//    public long measureAltThrustRandomize() {
//        return ThrustAltRNG.randomize(state += 0x6C8E9CF570932BD5L);
//    }
//    
//    @Benchmark
//    public long measureLinnormRandomize() { return LinnormRNG.randomize(state += 0x632BE59BD9B4E019L); }

    private ThrustAltRNG ThrustAlt = new ThrustAltRNG(9999L);
    private RNG ThrustAltR = new RNG(ThrustAlt);
    @Benchmark
    public long measureThrustAlt()
    {
        return ThrustAlt.nextLong();
    }

    @Benchmark
    public int measureThrustAltInt()
    {
        return ThrustAlt.next(32);
    }
    @Benchmark
    public long measureThrustAltR()
    {
        return ThrustAltR.nextLong();
    }

    @Benchmark
    public int measureThrustAltIntR()
    {
        return ThrustAltR.nextInt();
    }
//    @Benchmark
//    public long measureThrustAltInline()
//    {
//        final long s = (state += 0x6C8E9CF570932BD5L);
//        final long z = (s ^ (s >>> 25)) * (s | 0xA529L);
//        return z ^ (z >>> 22);
//    }
//    @Benchmark
//    public long measureInlineThrustAltOther()
//    {
//        long z = (state += 0x6C8E9CF570932BD5L);
//        z = (z ^ (z >>> 25)) * (z | 0xA529L);
//        return z ^ (z >>> 22);
//    }

    private Jab63RNG Jab63 = new Jab63RNG(9999L);
    private RNG Jab63R = new RNG(Jab63);
    @Benchmark
    public long measureJab63()
    {
        return Jab63.nextLong();
    }

    @Benchmark
    public int measureJab63Int()
    {
        return Jab63.next(32);
    }
    @Benchmark
    public long measureJab63R()
    {
        return Jab63R.nextLong();
    }

    @Benchmark
    public int measureJab63IntR()
    {
        return Jab63R.nextInt();
    }

//    @Benchmark
//    public long measureInlineJab63()
//    {
//        long z = (oddState += 0x3C6EF372FE94F82AL);
//        z *= (z ^ (z >>> 21));
//        return z - (z >>> 28);
//    }
//
//
//    @Benchmark
//    public long measureInlineVortex()
//    {
//        long z = (state += 0x6C8E9CF970932BD5L);
//        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
//        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
//        return z ^ (z >>> 25);
//    }

    private VortexRNG Vortex = new VortexRNG(9999L);
    private RNG VortexR = new RNG(Vortex);
    @Benchmark
    public long measureVortex()
    {
        return Vortex.nextLong();
    }

    @Benchmark
    public int measureVortexInt()
    {
        return Vortex.next(32);
    }
    @Benchmark
    public long measureVortexR()
    {
        return VortexR.nextLong();
    }

    @Benchmark
    public int measureVortexIntR()
    {
        return VortexR.nextInt();
    }

    private BasicRandom64 BasicRandom64 = new BasicRandom64(1L);
    private RNG BasicRandom64R = new RNG(BasicRandom64);
    @Benchmark
    public long measureBasicRandom64()
    {
        return BasicRandom64.nextLong();
    }

    @Benchmark
    public int measureBasicRandom64Int()
    {
        return BasicRandom64.next(32);
    }
    @Benchmark
    public long measureBasicRandom64R()
    {
        return BasicRandom64R.nextLong();
    }

    @Benchmark
    public int measureBasicRandom64IntR()
    {
        return BasicRandom64R.nextInt();
    }

    private BasicRandom32 BasicRandom32 = new BasicRandom32(1);
    private RNG BasicRandom32R = new RNG(BasicRandom32);
    @Benchmark
    public long measureBasicRandom32()
    {
        return BasicRandom32.nextLong();
    }

    @Benchmark
    public int measureBasicRandom32Int()
    {
        return BasicRandom32.next(32);
    }
    @Benchmark
    public long measureBasicRandom32R()
    {
        return BasicRandom32R.nextLong();
    }

    @Benchmark
    public int measureBasicRandom32IntR()
    {
        return BasicRandom32R.nextInt();
    }

    private MotorRNG Motor = new MotorRNG(9999L);
    private RNG MotorR = new RNG(Motor);
    @Benchmark
    public long measureMotor()
    {
        return Motor.nextLong();
    }

    @Benchmark
    public int measureMotorInt()
    {
        return Motor.next(32);
    }
    @Benchmark
    public long measureMotorR()
    {
        return MotorR.nextLong();
    }

    @Benchmark
    public int measureMotorIntR()
    {
        return MotorR.nextInt();
    }


    private MeshRNG Mesh = new MeshRNG(9999L);
    private RNG MeshR = new RNG(Mesh);
    @Benchmark
    public long measureMesh()
    {
        return Mesh.nextLong();
    }

    @Benchmark
    public int measureMeshInt()
    {
        return Mesh.next(32);
    }
    @Benchmark
    public long measureMeshR()
    {
        return MeshR.nextLong();
    }

    @Benchmark
    public int measureMeshIntR()
    {
        return MeshR.nextInt();
    }


    private SpiralRNG Spiral = new SpiralRNG(9999L);
    private RNG SpiralR = new RNG(Spiral);
    @Benchmark
    public long measureSpiral()
    {
        return Spiral.nextLong();
    }

    @Benchmark
    public int measureSpiralInt()
    {
        return Spiral.next(32);
    }
    @Benchmark
    public long measureSpiralR()
    {
        return SpiralR.nextLong();
    }

    @Benchmark
    public int measureSpiralIntR()
    {
        return SpiralR.nextInt();
    }

    private SpiralRNG spiralA = new SpiralRNG(9999L, 1337L),
            spiralB = new SpiralRNG(9999L, 1337L),
            spiralC = new SpiralRNG(9999L, 1337L),
            spiralD = new SpiralRNG(9999L, 1337L),
            spiralE = new SpiralRNG(9999L, 1337L),
            spiralF = new SpiralRNG(9999L, 1337L),
            spiralG = new SpiralRNG(9999L, 1337L),
            spiralH = new SpiralRNG(9999L, 1337L),
            spiralI = new SpiralRNG(9999L, 1337L);
    @Benchmark
    public long measureSpiralA()
    {
        return spiralA.nextLongOld();
    }
    @Benchmark
    public long measureSpiralB()
    {
        return spiralB.nextLongAlt();
    }
    @Benchmark
    public long measureSpiralC()
    {
        return spiralC.nextLongNew();
    }
    @Benchmark
    public long measureSpiralD()
    {
        return spiralD.nextLongAgain();
    }
    @Benchmark
    public long measureSpiralE()
    {
        return spiralE.nextLongAgain3();
    }
    @Benchmark
    public long measureSpiralF()
    {
        return spiralF.nextLongAgain4();
    }
    @Benchmark
    public long measureSpiralG()
    {
        return spiralG.nextLongAgain5();
    }
    @Benchmark
    public long measureSpiralH()
    {
        return spiralH.nextLongAgain6();
    }
    @Benchmark
    public long measureSpiralI()
    {
        return spiralI.nextLongAgain7();
    }

    private OrbitRNG Orbit = new OrbitRNG(9999L, 1337L);
    private RNG OrbitR = new RNG(Orbit);

    @Benchmark
    public long measureOrbit()
    {
        return Orbit.nextLong();
    }

    @Benchmark
    public int measureOrbitInt()
    {
        return Orbit.next(32);
    }
    @Benchmark
    public long measureOrbitR()
    {
        return OrbitR.nextLong();
    }

    @Benchmark
    public int measureOrbitIntR()
    {
        return OrbitR.nextInt();
    }

    private TangleRNG Tangle = new TangleRNG(9999L, 1337L);
    private RNG TangleR = new RNG(Tangle);

    @Benchmark
    public long measureTangle()
    {
        return Tangle.nextLong();
    }

    @Benchmark
    public int measureTangleInt()
    {
        return Tangle.next(32);
    }
    @Benchmark
    public long measureTangleR()
    {
        return TangleR.nextLong();
    }

    @Benchmark
    public int measureTangleIntR()
    {
        return TangleR.nextInt();
    }

    private OrbitRNG OrbitA = new OrbitRNG(9999L, 1337L),
            OrbitB = new OrbitRNG(9999L, 1337L),
            OrbitC = new OrbitRNG(9999L, 1337L),
            OrbitD = new OrbitRNG(9999L, 1337L),
            OrbitE = new OrbitRNG(9999L, 1337L),
            OrbitF = new OrbitRNG(9999L, 1337L),
            OrbitG = new OrbitRNG(9999L, 1337L),
            OrbitH = new OrbitRNG(9999L, 1337L),
            OrbitI = new OrbitRNG(9999L, 1337L),
            OrbitJ = new OrbitRNG(9999L, 1337L),
            OrbitK = new OrbitRNG(9999L, 1337L),
            OrbitL = new OrbitRNG(9999L, 1337L),
            OrbitM = new OrbitRNG(9999L, 1337L),
            OrbitN = new OrbitRNG(9999L, 1337L),
            OrbitO = new OrbitRNG(9999L, 1337L);
    @Benchmark
    public long measureOrbitA()
    {
        return OrbitA.nextLong1();
    }
    @Benchmark
    public long measureOrbitB()
    {
        return OrbitB.nextLong2();
    }
    @Benchmark
    public long measureOrbitC()
    {
        return OrbitC.nextLong3();
    }
    @Benchmark
    public long measureOrbitD()
    {
        return OrbitD.nextLong4();
    }
    @Benchmark
    public long measureOrbitE()
    {
        return OrbitE.nextLong5();
    }
    @Benchmark
    public long measureOrbitF()
    {
        return OrbitF.nextLong6();
    }
    @Benchmark
    public long measureOrbitG()
    {
        return OrbitG.nextLong7();
    }
    @Benchmark
    public long measureOrbitH()
    {
        return OrbitH.nextLong8();
    }
    @Benchmark
    public long measureOrbitI()
    {
        return OrbitI.nextLong9();
    }
    @Benchmark
    public long measureOrbitJ()
    {
        return OrbitJ.nextLong10();
    }
    @Benchmark
    public long measureOrbitK()
    {
        return OrbitK.nextLong11();
    }
    @Benchmark
    public long measureOrbitL()
    {
        return OrbitL.nextLong12();
    }
    @Benchmark
    public long measureOrbitM()
    {
        return OrbitM.nextLong13();
    }
    @Benchmark
    public long measureOrbitN()
    {
        return OrbitN.nextLong14();
    }
    @Benchmark
    public long measureOrbitO()
    {
        return OrbitO.nextLong15();
    }


    private TangleRNG TangleA = new TangleRNG(9999L, 1337L),
            TangleB = new TangleRNG(9999L, 1337L),
            TangleC = new TangleRNG(9999L, 1337L),
            TangleD = new TangleRNG(9999L, 1337L);
    @Benchmark
    public long measureTangleA()
    {
        return TangleA.nextLong1();
    }
    @Benchmark
    public long measureTangleB()
    {
        return TangleB.nextLong2();
    }
    @Benchmark
    public long measureTangleC()
    {
        return TangleC.nextLong3();
    }

    @Benchmark
    public long measureTangleD()
    {
        return TangleD.nextLong4();
    }


    private Mover32RNG Mover32 = new Mover32RNG(0);
    private RNG Mover32R = new RNG(Mover32);

    @Benchmark
    public long measureMover32()
    {
        return Mover32.nextLong();
    }

    @Benchmark
    public int measureMover32Int()
    {
        return Mover32.next(32);
    }
    @Benchmark
    public long measureMover32R()
    {
        return Mover32R.nextLong();
    }

    @Benchmark
    public int measureMover32IntR()
    {
        return Mover32R.nextInt();
    }

//    private Mover32RNG Mover32A = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32A()
//    {
//        return Mover32A.nextIntA();
//    }
//
//    private Mover32RNG Mover32B = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32B()
//    {
//        return Mover32B.nextIntB();
//    }
//    private Mover32RNG Mover32C = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32C()
//    {
//        return Mover32C.nextIntC();
//    }

    private Mover64RNG Mover64 = new Mover64RNG(0);
    private RNG Mover64R = new RNG(Mover64);

    @Benchmark
    public long measureMover64()
    {
        return Mover64.nextLong();
    }

    @Benchmark
    public int measureMover64Int()
    {
        return Mover64.next(32);
    }
    @Benchmark
    public long measureMover64R()
    {
        return Mover64R.nextLong();
    }

    @Benchmark
    public int measureMover64IntR()
    {
        return Mover64R.nextInt();
    }
    
    private Molerat32RNG Molerat32 = new Molerat32RNG(0);
    private RNG Molerat32R = new RNG(Molerat32);

    @Benchmark
    public long measureMolerat32()
    {
        return Molerat32.nextLong();
    }

    @Benchmark
    public int measureMolerat32Int()
    {
        return Molerat32.next(32);
    }
    @Benchmark
    public long measureMolerat32R()
    {
        return Molerat32R.nextLong();
    }

    @Benchmark
    public int measureMolerat32IntR()
    {
        return Molerat32R.nextInt();
    }
    private MiniMover64RNG MiniMover64 = new MiniMover64RNG(0);
    private RNG MiniMover64R = new RNG(MiniMover64);

    @Benchmark
    public long measureMiniMover64()
    {
        return MiniMover64.nextLong();
    }

    @Benchmark
    public int measureMiniMover64Int()
    {
        return MiniMover64.next(32);
    }
    @Benchmark
    public long measureMiniMover64R()
    {
        return MiniMover64R.nextLong();
    }

    @Benchmark
    public int measureMiniMover64IntR()
    {
        return MiniMover64R.nextInt();
    }

    private SFC64RNG SFC64 = new SFC64RNG(9999L);
    private RNG SFC64R = new RNG(SFC64);
    @Benchmark
    public long measureSFC64()
    {
        return SFC64.nextLong();
    }

    @Benchmark
    public int measureSFC64Int()
    {
        return SFC64.next(32);
    }
    @Benchmark
    public long measureSFC64R()
    {
        return SFC64R.nextLong();
    }

    @Benchmark
    public int measureSFC64IntR()
    {
        return SFC64R.nextInt();
    }
    
    private Overdrive64RNG Overdrive64 = new Overdrive64RNG(0);
    private RNG Overdrive64R = new RNG(Overdrive64);

    @Benchmark
    public long measureOverdrive64()
    {
        return Overdrive64.nextLong();
    }

    @Benchmark
    public int measureOverdrive64Int()
    {
        return Overdrive64.next(32);
    }
    @Benchmark
    public long measureOverdrive64R()
    {
        return Overdrive64R.nextLong();
    }

    @Benchmark
    public int measureOverdrive64IntR()
    {
        return Overdrive64R.nextInt();
    }

    private MoverCounter64RNG MoverCounter64 = new MoverCounter64RNG(9999L);
    private RNG MoverCounter64R = new RNG(MoverCounter64);
    @Benchmark
    public long measureMoverCounter64()
    {
        return MoverCounter64.nextLong();
    }

    @Benchmark
    public int measureMoverCounter64Int()
    {
        return MoverCounter64.next(32);
    }
    @Benchmark
    public long measureMoverCounter64R()
    {
        return MoverCounter64R.nextLong();
    }

    @Benchmark
    public int measureMoverCounter64IntR()
    {
        return MoverCounter64R.nextInt();
    }




    private DirkRNG Dirk = new DirkRNG(9999L);
    private RNG DirkR = new RNG(Dirk);
    @Benchmark
    public long measureDirk()
    {
        return Dirk.nextLong();
    }

    @Benchmark
    public int measureDirkInt()
    {
        return Dirk.next(32);
    }
    @Benchmark
    public long measureDirkR()
    {
        return DirkR.nextLong();
    }

    @Benchmark
    public int measureDirkIntR()
    {
        return DirkR.nextInt();
    }

//    private Overdrive64RNG Overdrive1 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive2 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive3 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive4 = new Overdrive64RNG(0);
//
//    @Benchmark
//    public long measureOverdrive1()
//    {
//        return Overdrive1.nextLong1();
//    }
//    @Benchmark
//    public long measureOverdrive2()
//    {
//        return Overdrive2.nextLong2();
//    }
//    @Benchmark
//    public long measureOverdrive3()
//    {
//        return Overdrive3.nextLong3();
//    }
//    @Benchmark
//    public long measureOverdrive4()
//    {
//        return Overdrive4.nextLong4();
//    }

    //    private Thrust32RNG Thrust32 = new Thrust32RNG(9999);
//    private RNG Thrust32R = new RNG(Thrust32);
//
//    @Benchmark
//    public long measureThrust32()
//    {
//        return Thrust32.nextLong();
//    }
//
//    @Benchmark
//    public int measureThrust32Int()
//    {
//        return Thrust32.next(32);
//    }
//    @Benchmark
//    public long measureThrust32R()
//    {
//        return Thrust32R.nextLong();
//    }
//
//    @Benchmark
//    public int measureThrust32IntR()
//    {
//        return Thrust32R.nextInt();
//    }
//
//
    private ThrustAlt32RNG ThrustAlt32 = new ThrustAlt32RNG(9999);
    private RNG ThrustAlt32R = new RNG(ThrustAlt32);

    @Benchmark
    public long measureThrustAlt32()
    {
        return ThrustAlt32.nextLong();
    }

    @Benchmark
    public int measureThrustAlt32Int()
    {
        return ThrustAlt32.next(32);
    }
    @Benchmark
    public long measureThrustAlt32R()
    {
        return ThrustAlt32R.nextLong();
    }

    @Benchmark
    public int measureThrustAlt32IntR()
    {
        return ThrustAlt32R.nextInt();
    }

    private Light32RNG Light32 = new Light32RNG(9999);
    private RNG Light32R = new RNG(Light32);

    @Benchmark
    public long measureLight32()
    {
        return Light32.nextLong();
    }

    @Benchmark
    public int measureLight32Int()
    {
        return Light32.next(32);
    }
    @Benchmark
    public long measureLight32R()
    {
        return Light32R.nextLong();
    }

    @Benchmark
    public int measureLight32IntR()
    {
        return Light32R.nextInt();
    }
    private Zig32RNG Zig32 = new Zig32RNG(9999L);
    private RNG Zig32R = new RNG(Zig32);
    @Benchmark
    public long measureZig32()
    {
        return Zig32.nextLong();
    }

    @Benchmark
    public int measureZig32Int()
    {
        return Zig32.next(32);
    }
    @Benchmark
    public long measureZig32R()
    {
        return Zig32R.nextLong();
    }

    @Benchmark
    public int measureZig32IntR()
    {
        return Zig32R.nextInt();
    }

    private Zag32RNG Zag32 = new Zag32RNG(9999L);
    private RNG Zag32R = new RNG(Zag32);
    @Benchmark
    public long measureZag32()
    {
        return Zag32.nextLong();
    }

    @Benchmark
    public int measureZag32Int()
    {
        return Zag32.next(32);
    }
    @Benchmark
    public long measureZag32R()
    {
        return Zag32R.nextLong();
    }

    @Benchmark
    public int measureZag32IntR()
    {
        return Zag32R.nextInt();
    }

    private Zog32RNG Zog32 = new Zog32RNG(9999L);
    private RNG Zog32R = new RNG(Zog32);
    @Benchmark
    public long measureZog32()
    {
        return Zog32.nextLong();
    }

    @Benchmark
    public int measureZog32Int()
    {
        return Zog32.next(32);
    }
    @Benchmark
    public long measureZog32R()
    {
        return Zog32R.nextLong();
    }

    @Benchmark
    public int measureZog32IntR()
    {
        return Zog32R.nextInt();
    }

    private XoRo32RNG XoRo32 = new XoRo32RNG(9999L);
    private RNG XoRo32R = new RNG(XoRo32);
    @Benchmark
    public long measureXoRo32()
    {
        return XoRo32.nextLong();
    }

    @Benchmark
    public int measureXoRo32Int()
    {
        return XoRo32.next(32);
    }
    @Benchmark
    public long measureXoRo32R()
    {
        return XoRo32R.nextLong();
    }

    @Benchmark
    public int measureXoRo32IntR()
    {
        return XoRo32R.nextInt();
    }



    private Oriole32RNG Oriole32 = new Oriole32RNG(9999, 999, 99);
    private RNG Oriole32R = new RNG(Oriole32);
    @Benchmark
    public long measureOriole32()
    {
        return Oriole32.nextLong();
    }

    @Benchmark
    public int measureOriole32Int()
    {
        return Oriole32.next(32);
    }
    @Benchmark
    public long measureOriole32R()
    {
        return Oriole32R.nextLong();
    }

    @Benchmark
    public int measureOriole32IntR()
    {
        return Oriole32R.nextInt();
    }

    private Lathe32RNG Lathe32 = new Lathe32RNG(9999, 999);
    private RNG Lathe32R = new RNG(Lathe32);
    @Benchmark
    public long measureLathe32()
    {
        return Lathe32.nextLong();
    }

    @Benchmark
    public int measureLathe32Int()
    {
        return Lathe32.next(32);
    }
    @Benchmark
    public long measureLathe32R()
    {
        return Lathe32R.nextLong();
    }

    @Benchmark
    public int measureLathe32IntR()
    {
        return Lathe32R.nextInt();
    }

    private Starfish32RNG Starfish32 = new Starfish32RNG(9999, 999);
    private RNG Starfish32R = new RNG(Starfish32);
    @Benchmark
    public long measureStarfish32()
    {
        return Starfish32.nextLong();
    }

    @Benchmark
    public int measureStarfish32Int()
    {
        return Starfish32.next(32);
    }

    @Benchmark
    public int measureStarfish32NextInt()
    {
        return Starfish32.nextInt();
    }
    @Benchmark
    public long measureStarfish32R()
    {
        return Starfish32R.nextLong();
    }

    @Benchmark
    public int measureStarfish32IntR()
    {
        return Starfish32R.nextInt();
    }


    private GWTRNG GWT = new GWTRNG(9999, 999);
    @Benchmark
    public long measureGWT()
    {
        return GWT.nextLong();
    }

    @Benchmark
    public int measureGWTInt()
    {
        return GWT.next(32);
    }

    @Benchmark
    public int measureGWTNextInt()
    {
        return GWT.nextInt();
    }

    private Otter32RNG Otter32 = new Otter32RNG(9999, 999);
    private RNG Otter32R = new RNG(Otter32);
    @Benchmark
    public long measureOtter32()
    {
        return Otter32.nextLong();
    }

    @Benchmark
    public int measureOtter32Int()
    {
        return Otter32.next(32);
    }
    @Benchmark
    public long measureOtter32R()
    {
        return Otter32R.nextLong();
    }

    @Benchmark
    public int measureOtter32IntR()
    {
        return Otter32R.nextInt();
    }


    private Lobster32RNG Lobster32 = new Lobster32RNG(9999, 999);
    private RNG Lobster32R = new RNG(Lobster32);
    @Benchmark
    public long measureLobster32()
    {
        return Lobster32.nextLong();
    }

    @Benchmark
    public int measureLobster32Int()
    {
        return Lobster32.next(32);
    }
    @Benchmark
    public long measureLobster32R()
    {
        return Lobster32R.nextLong();
    }

    @Benchmark
    public int measureLobster32IntR()
    {
        return Lobster32R.nextInt();
    }

    private SeaSlater32RNG SeaSlater32 = new SeaSlater32RNG(9999, 999);
    private RNG SeaSlater32R = new RNG(SeaSlater32);
    @Benchmark
    public long measureSeaSlater32()
    {
        return SeaSlater32.nextLong();
    }

    @Benchmark
    public int measureSeaSlater32Int()
    {
        return SeaSlater32.next(32);
    }
    @Benchmark
    public long measureSeaSlater32R()
    {
        return SeaSlater32R.nextLong();
    }

    @Benchmark
    public int measureSeaSlater32IntR()
    {
        return SeaSlater32R.nextInt();
    }


    private Churro32RNG Churro32 = new Churro32RNG(9999, 999, 99);
    private RNG Churro32R = new RNG(Churro32);
    @Benchmark
    public long measureChurro32()
    {
        return Churro32.nextLong();
    }

    @Benchmark
    public int measureChurro32Int()
    {
        return Churro32.next(32);
    }
    @Benchmark
    public long measureChurro32R()
    {
        return Churro32R.nextLong();
    }

    @Benchmark
    public int measureChurro32IntR()
    {
        return Churro32R.nextInt();
    }
    private Dizzy32RNG Dizzy32 = new Dizzy32RNG(9999, 999, 99);
    private RNG Dizzy32R = new RNG(Dizzy32);
    @Benchmark
    public long measureDizzy32()
    {
        return Dizzy32.nextLong();
    }

    @Benchmark
    public int measureDizzy32Int()
    {
        return Dizzy32.next(32);
    }
    @Benchmark
    public long measureDizzy32R()
    {
        return Dizzy32R.nextLong();
    }

    @Benchmark
    public int measureDizzy32IntR()
    {
        return Dizzy32R.nextInt();
    }

    @Benchmark
    public long measureDizzy32IntNative1()
    {
        return Dizzy32.nextInt();
    }
    @Benchmark
    public long measureDizzy32IntNative2()
    {
        return Dizzy32.nextInt2();
    }


    private XoshiroStarStar32RNG XoshiroStarStar32 = new XoshiroStarStar32RNG(9999);
    private RNG XoshiroStarStar32R = new RNG(XoshiroStarStar32);

    @Benchmark
    public long measureXoshiroStarStar32()
    {
        return XoshiroStarStar32.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarStar32Int()
    {
        return XoshiroStarStar32.next(32);
    }
    @Benchmark
    public long measureXoshiroStarStar32R()
    {
        return XoshiroStarStar32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarStar32IntR()
    {
        return XoshiroStarStar32R.nextInt();
    }


    private XoshiroStarPhi32RNG XoshiroStarPhi32 = new XoshiroStarPhi32RNG(9999);
    private RNG XoshiroStarPhi32R = new RNG(XoshiroStarPhi32);

    @Benchmark
    public long measureXoshiroStarPhi32()
    {
        return XoshiroStarPhi32.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarPhi32Int()
    {
        return XoshiroStarPhi32.next(32);
    }
    @Benchmark
    public long measureXoshiroStarPhi32R()
    {
        return XoshiroStarPhi32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarPhi32IntR()
    {
        return XoshiroStarPhi32R.nextInt();
    }

    private XoshiroXara32RNG XoshiroXara32 = new XoshiroXara32RNG(9999);
    private RNG XoshiroXara32R = new RNG(XoshiroXara32);

    @Benchmark
    public long measureXoshiroXara32()
    {
        return XoshiroXara32.nextLong();
    }

    @Benchmark
    public int measureXoshiroXara32Int()
    {
        return XoshiroXara32.next(32);
    }
    @Benchmark
    public long measureXoshiroXara32R()
    {
        return XoshiroXara32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroXara32IntR()
    {
        return XoshiroXara32R.nextInt();
    }

    private XoshiroAra32RNG XoshiroAra32 = new XoshiroAra32RNG(9999);
    private RNG XoshiroAra32R = new RNG(XoshiroAra32);

    @Benchmark
    public long measureXoshiroAra32()
    {
        return XoshiroAra32.nextLong();
    }

    @Benchmark
    public int measureXoshiroAra32Int()
    {
        return XoshiroAra32.next(32);
    }
    @Benchmark
    public long measureXoshiroAra32R()
    {
        return XoshiroAra32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroAra32IntR()
    {
        return XoshiroAra32R.nextInt();
    }

    private DervishRNG Dervish = new DervishRNG(9999L);
    private RNG DervishR = new RNG(Dervish);
    @Benchmark
    public long measureDervish()
    {
        return Dervish.nextLong();
    }

    @Benchmark
    public int measureDervishInt()
    {
        return Dervish.next(32);
    }
    @Benchmark
    public long measureDervishR()
    {
        return DervishR.nextLong();
    }

    @Benchmark
    public int measureDervishIntR()
    {
        return DervishR.nextInt();
    }


    private LinnormRNG Linnorm = new LinnormRNG(9999L);
    private RNG LinnormR = new RNG(Linnorm);
    @Benchmark
    public long measureLinnorm()
    {
        return Linnorm.nextLong();
    }

    @Benchmark
    public int measureLinnormInt()
    {
        return Linnorm.next(32);
    }
    @Benchmark
    public long measureLinnormR()
    {
        return LinnormR.nextLong();
    }

    @Benchmark
    public int measureLinnormIntR()
    {
        return LinnormR.nextInt();
    }

    private MizuchiRNG Mizuchi = new MizuchiRNG(9999L);
    private RNG MizuchiR = new RNG(Mizuchi);
    @Benchmark
    public long measureMizuchi()
    {
        return Mizuchi.nextLong();
    }

    @Benchmark
    public int measureMizuchiInt()
    {
        return Mizuchi.next(32);
    }
    @Benchmark
    public long measureMizuchiR()
    {
        return MizuchiR.nextLong();
    }

    @Benchmark
    public int measureMizuchiIntR()
    {
        return MizuchiR.nextInt();
    }

    private QuixoticRNG Quixotic = new QuixoticRNG(9999L);
    private RNG QuixoticR = new RNG(Quixotic);
    @Benchmark
    public long measureQuixotic()
    {
        return Quixotic.nextLong();
    }

    @Benchmark
    public int measureQuixoticInt()
    {
        return Quixotic.next(32);
    }
    @Benchmark
    public long measureQuixoticR()
    {
        return QuixoticR.nextLong();
    }

    @Benchmark
    public int measureQuixoticIntR()
    {
        return QuixoticR.nextInt();
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
    public long doJet()
    {
        JetRNG rng = new JetRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJet() {
        seed = 9000;
        doJet();
    }

    public int doJetInt()
    {
        JetRNG rng = new JetRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetR() {
        seed = 9000;
        doJetR();
    }

    public int doJetIntR()
    {
        RNG rng = new RNG(new JetRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32() {
        seed = 9000;
        doLunge32();
    }

    public int doLunge32Int()
    {
        Lunge32RNG rng = new Lunge32RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32R() {
        seed = 9000;
        doLunge32R();
    }

    public int doLunge32IntR()
    {
        RNG rng = new RNG(new Lunge32RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32IntR() {
        iseed = 9000;
        doLunge32IntR();
    }


    @Benchmark
    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
    public void a________measureThrustAltDetermine() {
        seed = 9000;
        long state = 9000L;
        for (int i = 0; i < 1000000007; i++) {
            seed += ThrustAltRNG.determine(++state);
        }
    }

//    // Performs rather poorly, surprisingly. JIT needs method calls rather than inlined code, it looks like.
//    @Benchmark
//    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
//    public void a________measureDetermineBare() {
//        seed = 9000;
//        long running = seed, state = 9000L;
//        for (int i = 0; i < 1000000007; i++) {
//            seed += ((state = ((running += 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22));
//        }
//    }
    @Benchmark
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortex() {
//        seed = 9000;
//        doVortex();
//    }
//
//    public int doVortexInt()
//    {
//        VortexRNG rng = new VortexRNG(iseed);
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexR() {
//        seed = 9000;
//        doVortexR();
//    }
//
//    public int doVortexIntR()
//    {
//        RNG rng = new RNG(new VortexRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrel() throws InterruptedException {
//        seed = 9000;
//        doSquirrel();
//    }
//
//    public int doSquirrelInt()
//    {
//        SquirrelRNG rng = new SquirrelRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelR() throws InterruptedException {
//        seed = 9000;
//        doSquirrelR();
//    }
//
//    public int doSquirrelIntR()
//    {
//        RNG rng = new RNG(new SquirrelRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90() throws InterruptedException {
//        seed = 9000;
//        doRule90();
//    }
//
//    public int doRule90Int()
//    {
//        Rule90RNG rng = new Rule90RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90R() throws InterruptedException {
//        seed = 9000;
//        doRule90R();
//    }
//
//    public int doRule90IntR()
//    {
//        RNG rng = new RNG(new Rule90RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZap() throws InterruptedException {
        seed = 9000;
        doZap();
    }

    public int doZapInt()
    {
        ZapRNG rng = new ZapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapR() throws InterruptedException {
        seed = 9000;
        doZapR();
    }

    public int doZapIntR()
    {
        RNG rng = new RNG(new ZapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlap() throws InterruptedException {
        seed = 9000;
        doSlap();
    }

    public int doSlapInt()
    {
        SlapRNG rng = new SlapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapR() throws InterruptedException {
        seed = 9000;
        doSlapR();
    }

    public int doSlapIntR()
    {
        RNG rng = new RNG(new SlapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholder() throws InterruptedException {
        seed = 9000;
        doPlaceholder();
    }

    public int doPlaceholderInt()
    {
        PlaceholderRNG rng = new PlaceholderRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderR() throws InterruptedException {
        seed = 9000;
        doPlaceholderR();
    }

    public int doPlaceholderIntR()
    {
        RNG rng = new RNG(new PlaceholderRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderIntR() throws InterruptedException {
        iseed = 9000;
        doPlaceholderIntR();
    }
*/

    private Random JDK = new Random(9999L);
    @Benchmark
    public long measureJDK()
    {
        return JDK.nextLong();
    }

    @Benchmark
    public int measureJDKInt()
    {
        return JDK.nextInt();
    }

    /*
mvn clean install
java -jar target/benchmarks.jar RNGBenchmark -wi 5 -i 5 -f 1 -gc true
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
