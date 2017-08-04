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

public class RNGBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

    public long doThunder()
    {
        ThunderRNG rng = new ThunderRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureThunder() throws InterruptedException {
        seed = 9000;
        doThunder();
    }

    public long doThunderInt()
    {
        ThunderRNG rng = new ThunderRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureThunderInt() throws InterruptedException {
        iseed = 9000;
        doThunderInt();
    }
    public long doThunderR()
    {
        RNG rng = new RNG(new ThunderRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureThunderR() throws InterruptedException {
        seed = 9000;
        doThunderR();
    }

    public long doThunderIntR()
    {
        RNG rng = new RNG(new ThunderRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureThunderIntR() throws InterruptedException {
        iseed = 9000;
        doThunderIntR();
    }

    public long doXoRo()
    {
        XoRoRNG rng = new XoRoRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureXoRo() throws InterruptedException {
        seed = 9000;
        doXoRo();
    }

    public long doXoRoInt()
    {
        XoRoRNG rng = new XoRoRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureXoRoInt() throws InterruptedException {
        iseed = 9000;
        doXoRoInt();
    }

    public long doXoRoR()
    {
        RNG rng = new RNG(new XoRoRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureXoRoR() throws InterruptedException {
        seed = 9000;
        doXoRoR();
    }

    public long doXoRoIntR()
    {
        RNG rng = new RNG(new XoRoRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureXoRoIntR() throws InterruptedException {
        iseed = 9000;
        doXoRoIntR();
    }
    public long doLongPeriod()
    {
        LongPeriodRNG rng = new LongPeriodRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLongPeriod() throws InterruptedException {
        seed = 9000;
        doLongPeriod();
    }

    public long doLongPeriodInt()
    {
        LongPeriodRNG rng = new LongPeriodRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLongPeriodInt() throws InterruptedException {
        iseed = 9000;
        doLongPeriodInt();
    }

    public long doLongPeriodR()
    {
        RNG rng = new RNG(new LongPeriodRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLongPeriodR() throws InterruptedException {
        seed = 9000;
        doLongPeriodR();
    }

    public long doLongPeriodIntR()
    {
        RNG rng = new RNG(new LongPeriodRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLongPeriodIntR() throws InterruptedException {
        iseed = 9000;
        doLongPeriodIntR();
    }

    public long doLight()
    {
        LightRNG rng = new LightRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLight() throws InterruptedException {
        seed = 9000;
        doLight();
    }

    public long doLightInt()
    {
        LightRNG rng = new LightRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLightInt() throws InterruptedException {
        iseed = 9000;
        doLightInt();
    }
    public long doLightR()
    {
        RNG rng = new RNG(new LightRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLightR() throws InterruptedException {
        seed = 9000;
        doLightR();
    }

    public long doLightIntR()
    {
        RNG rng = new RNG(new LightRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLightIntR() throws InterruptedException {
        iseed = 9000;
        doLightIntR();
    }

    public long doFlap()
    {
        FlapRNG rng = new FlapRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureFlap() throws InterruptedException {
        seed = 9000;
        doFlap();
    }

    public long doFlapInt()
    {
        FlapRNG rng = new FlapRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureFlapInt() throws InterruptedException {
        iseed = 9000;
        doFlapInt();
    }

    public long doFlapR()
    {
        RNG rng = new RNG(new FlapRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureFlapR() throws InterruptedException {
        seed = 9000;
        doFlapR();
    }

    public long doFlapIntR()
    {
        RNG rng = new RNG(new FlapRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureFlapIntR() throws InterruptedException {
        iseed = 9000;
        doFlapIntR();
    }

    public long doLap()
    {
        LapRNG rng = new LapRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLap() throws InterruptedException {
        seed = 9000;
        doLap();
    }

    public long doLapInt()
    {
        LapRNG rng = new LapRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLapInt() throws InterruptedException {
        iseed = 9000;
        doLapInt();
    }

    public long doLapR()
    {
        RNG rng = new RNG(new LapRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLapR() throws InterruptedException {
        seed = 9000;
        doLapR();
    }

    public long doLapIntR()
    {
        RNG rng = new RNG(new LapRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLapIntR() throws InterruptedException {
        iseed = 9000;
        doLapIntR();
    }


    public long doHorde()
    {
        HordeRNG rng = new HordeRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureHorde() throws InterruptedException {
        seed = 9000;
        doHorde();
    }

    public long doHordeInt()
    {
        HordeRNG rng = new HordeRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureHordeInt() throws InterruptedException {
        iseed = 9000;
        doHordeInt();
    }

    public long doHordeR()
    {
        RNG rng = new RNG(new HordeRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureHordeR() throws InterruptedException {
        seed = 9000;
        doHordeR();
    }

    public long doHordeIntR()
    {
        RNG rng = new RNG(new HordeRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureHordeIntR() throws InterruptedException {
        iseed = 9000;
        doHordeIntR();
    }
    public long doHerd()
    {
        HerdRNG rng = new HerdRNG((int)seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureHerd() throws InterruptedException {
        seed = 9000;
        doHerd();
    }

    public long doHerdInt()
    {
        HerdRNG rng = new HerdRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureHerdInt() throws InterruptedException {
        iseed = 9000;
        doHerdInt();
    }

    public long doHerdR()
    {
        RNG rng = new RNG(new HerdRNG((int)seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureHerdR() throws InterruptedException {
        seed = 9000;
        doHerdR();
    }

    public long doHerdIntR()
    {
        RNG rng = new RNG(new HerdRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureHerdIntR() throws InterruptedException {
        iseed = 9000;
        doHerdIntR();
    }

    public long doBeard()
    {
        BeardRNG rng = new BeardRNG((int)seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureBeard() throws InterruptedException {
        seed = 9000;
        doBeard();
    }

    public long doBeardInt()
    {
        BeardRNG rng = new BeardRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureBeardInt() throws InterruptedException {
        iseed = 9000;
        doBeardInt();
    }

    public long doBeardR()
    {
        RNG rng = new RNG(new BeardRNG((int)seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureBeardR() throws InterruptedException {
        seed = 9000;
        doBeardR();
    }

    public long doBeardIntR()
    {
        RNG rng = new RNG(new BeardRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureBeardIntR() throws InterruptedException {
        iseed = 9000;
        doBeardIntR();
    }


    public long doBird()
    {
        BirdRNG rng = new BirdRNG((int)seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBird() throws InterruptedException {
        seed = 9000;
        doBird();
    }

    public long doBirdInt()
    {
        BirdRNG rng = new BirdRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBirdInt() throws InterruptedException {
        iseed = 9000;
        doBirdInt();
    }

    public long doBirdR()
    {
        RNG rng = new RNG(new BirdRNG((int)seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBirdR() throws InterruptedException {
        seed = 9000;
        doBirdR();
    }

    public long doBirdIntR()
    {
        RNG rng = new RNG(new BirdRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBirdIntR() throws InterruptedException {
        iseed = 9000;
        doBirdIntR();
    }
    public long doBard()
    {
        BardRNG rng = new BardRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBard() throws InterruptedException {
        seed = 9000;
        doBard();
    }

    public long doBardInt()
    {
        BardRNG rng = new BardRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBardInt() throws InterruptedException {
        iseed = 9000;
        doBardInt();
    }

    public long doBardR()
    {
        RNG rng = new RNG(new BardRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBardR() throws InterruptedException {
        seed = 9000;
        doBardR();
    }

    public long doBardIntR()
    {
        RNG rng = new RNG(new BardRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureBardIntR() throws InterruptedException {
        iseed = 9000;
        doBardIntR();
    }

    public long doLight32()
    {
        Light32RNG rng = new Light32RNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLight32() throws InterruptedException {
        seed = 9000;
        doLight32();
    }

    public long doLight32Int()
    {
        Light32RNG rng = new Light32RNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLight32Int() throws InterruptedException {
        iseed = 9000;
        doLight32Int();
    }

    public long doLight32R()
    {
        RNG rng = new RNG(new Light32RNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLight32R() throws InterruptedException {
        seed = 9000;
        doLight32R();
    }

    public long doLight32IntR()
    {
        RNG rng = new RNG(new Light32RNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureLight32IntR() throws InterruptedException {
        iseed = 9000;
        doLight32IntR();
    }

    public long doThrust()
    {
        ThrustRNG rng = new ThrustRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrust() throws InterruptedException {
        seed = 9000;
        doThrust();
    }

    public long doThrustInt()
    {
        ThrustRNG rng = new ThrustRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustInt() throws InterruptedException {
        iseed = 9000;
        doThrustInt();
    }

    public long doThrustR()
    {
        RNG rng = new RNG(new ThrustRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustR() throws InterruptedException {
        seed = 9000;
        doThrustR();
    }

    public long doThrustIntR()
    {
        RNG rng = new RNG(new ThrustRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustIntR() throws InterruptedException {
        iseed = 9000;
        doThrustIntR();
    }

/*
    public long doZap()
    {
        ZapRNG rng = new ZapRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureZap() throws InterruptedException {
        seed = 9000;
        doZap();
    }

    public long doZapInt()
    {
        ZapRNG rng = new ZapRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureZapInt() throws InterruptedException {
        iseed = 9000;
        doZapInt();
    }

    public long doZapR()
    {
        RNG rng = new RNG(new ZapRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureZapR() throws InterruptedException {
        seed = 9000;
        doZapR();
    }

    public long doZapIntR()
    {
        RNG rng = new RNG(new ZapRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measureZapIntR() throws InterruptedException {
        iseed = 9000;
        doZapIntR();
    }




    public long doSlap()
    {
        SlapRNG rng = new SlapRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureSlap() throws InterruptedException {
        seed = 9000;
        doSlap();
    }

    public long doSlapInt()
    {
        SlapRNG rng = new SlapRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureSlapInt() throws InterruptedException {
        iseed = 9000;
        doSlapInt();
    }

    public long doSlapR()
    {
        RNG rng = new RNG(new SlapRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureSlapR() throws InterruptedException {
        seed = 9000;
        doSlapR();
    }

    public long doSlapIntR()
    {
        RNG rng = new RNG(new SlapRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureSlapIntR() throws InterruptedException {
        iseed = 9000;
        doSlapIntR();
    }

*/
    
    
    
    
    
    
    
    
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
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
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
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
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
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
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
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void aa_measurePlaceholderIntR() throws InterruptedException {
        iseed = 9000;
        doPlaceholderIntR();
    }
*/



    public long doJDK()
    {
        Random rng = new Random(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureJDK() throws InterruptedException {
        seed = 9000;
        doJDK();
    }

    public long doJDKInt()
    {
        Random rng = new Random(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    //@Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureJDKInt() throws InterruptedException {
        iseed = 9000;
        doJDKInt();
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
