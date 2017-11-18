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

public class UncommonBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

    public long doLongPeriod()
    {
        LongPeriodRNG rng = new LongPeriodRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLightIntR() throws InterruptedException {
        iseed = 9000;
        doLightIntR();
    }

//    public long doFlap()
//    {
//        FlapRNG rng = new FlapRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlap() throws InterruptedException {
//        seed = 9000;
//        doFlap();
//    }
//
//    public long doFlapInt()
//    {
//        FlapRNG rng = new FlapRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapInt() throws InterruptedException {
//        iseed = 9000;
//        doFlapInt();
//    }
//
//    public long doFlapR()
//    {
//        RNG rng = new RNG(new FlapRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapR() throws InterruptedException {
//        seed = 9000;
//        doFlapR();
//    }
//
//    public long doFlapIntR()
//    {
//        RNG rng = new RNG(new FlapRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapIntR() throws InterruptedException {
//        iseed = 9000;
//        doFlapIntR();
//    }
//
//    public long doLap()
//    {
//        LapRNG rng = new LapRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLap() throws InterruptedException {
//        seed = 9000;
//        doLap();
//    }
//
//    public long doLapInt()
//    {
//        LapRNG rng = new LapRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLapInt() throws InterruptedException {
//        iseed = 9000;
//        doLapInt();
//    }
//
//    public long doLapR()
//    {
//        RNG rng = new RNG(new LapRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLapR() throws InterruptedException {
//        seed = 9000;
//        doLapR();
//    }
//
//    public long doLapIntR()
//    {
//        RNG rng = new RNG(new LapRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLapIntR() throws InterruptedException {
//        iseed = 9000;
//        doLapIntR();
//    }
//
//    public long doBird()
//    {
//        BirdRNG rng = new BirdRNG((int)seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBird() throws InterruptedException {
//        seed = 9000;
//        doBird();
//    }
//
//    public long doBirdInt()
//    {
//        BirdRNG rng = new BirdRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBirdInt() throws InterruptedException {
//        iseed = 9000;
//        doBirdInt();
//    }
//
//    public long doBirdR()
//    {
//        RNG rng = new RNG(new BirdRNG((int)seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBirdR() throws InterruptedException {
//        seed = 9000;
//        doBirdR();
//    }
//
//    public long doBirdIntR()
//    {
//        RNG rng = new RNG(new BirdRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBirdIntR() throws InterruptedException {
//        iseed = 9000;
//        doBirdIntR();
//    }
//
//    public long doBeard()
//    {
//        BeardRNG rng = new BeardRNG((int)seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBeard() throws InterruptedException {
//        seed = 9000;
//        doBeard();
//    }
//
//    public long doBeardInt()
//    {
//        BeardRNG rng = new BeardRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBeardInt() throws InterruptedException {
//        iseed = 9000;
//        doBeardInt();
//    }
//
//    public long doBeardR()
//    {
//        RNG rng = new RNG(new BeardRNG((int)seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBeardR() throws InterruptedException {
//        seed = 9000;
//        doBeardR();
//    }
//
//    public long doBeardIntR()
//    {
//        RNG rng = new RNG(new BeardRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureBeardIntR() throws InterruptedException {
//        iseed = 9000;
//        doBeardIntR();
//    }







    public long doIsaac()
    {
        IsaacRNG rng = new IsaacRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaac() throws InterruptedException {
        seed = 9000;
        doIsaac();
    }

    public long doIsaacInt()
    {
        IsaacRNG rng = new IsaacRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaacInt() throws InterruptedException {
        iseed = 9000;
        doIsaacInt();
    }

    public long doIsaacR()
    {
        RNG rng = new RNG(new IsaacRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaacR() throws InterruptedException {
        seed = 9000;
        doIsaacR();
    }

    public long doIsaacIntR()
    {
        RNG rng = new RNG(new IsaacRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaacIntR() throws InterruptedException {
        iseed = 9000;
        doIsaacIntR();
    }






    public long doIsaac32()
    {
        Isaac32RNG rng = new Isaac32RNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaac32() throws InterruptedException {
        seed = 9000;
        doIsaac32();
    }

    public long doIsaac32Int()
    {
        Isaac32RNG rng = new Isaac32RNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }


    public void aa_measureIsaac32Int() throws InterruptedException {
        iseed = 9000;
        doIsaac32Int();
    }

    public long doIsaac32R()
    {
        RNG rng = new RNG(new Isaac32RNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaac32R() throws InterruptedException {
        seed = 9000;
        doIsaac32R();
    }

    public long doIsaac32IntR()
    {
        RNG rng = new RNG(new Isaac32RNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureIsaac32IntR() throws InterruptedException {
        iseed = 9000;
        doIsaac32IntR();
    }


//    public long doIsaacAlt()
//    {
//        IsaacAltRNG rng = new IsaacAltRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAlt() throws InterruptedException {
//        seed = 9000;
//        doIsaacAlt();
//    }
//
//    public long doIsaacAltInt()
//    {
//        IsaacAltRNG rng = new IsaacAltRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAltInt() throws InterruptedException {
//        iseed = 9000;
//        doIsaacAltInt();
//    }
//
//    public long doIsaacAltR()
//    {
//        RNG rng = new RNG(new IsaacAltRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAltR() throws InterruptedException {
//        seed = 9000;
//        doIsaacAltR();
//    }
//
//    public long doIsaacAltIntR()
//    {
//        RNG rng = new RNG(new IsaacAltRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAltIntR() throws InterruptedException {
//        iseed = 9000;
//        doIsaacAltIntR();
//    }
//
//
//
//
//
//
//    public long doIsaacAlt32()
//    {
//        Isaac32AltRNG rng = new Isaac32AltRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAlt32() throws InterruptedException {
//        seed = 9000;
//        doIsaacAlt32();
//    }
//
//    public long doIsaacAlt32Int()
//    {
//        Isaac32AltRNG rng = new Isaac32AltRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAlt32Int() throws InterruptedException {
//        iseed = 9000;
//        doIsaacAlt32Int();
//    }
//
//    public long doIsaacAlt32R()
//    {
//        RNG rng = new RNG(new Isaac32AltRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAlt32R() throws InterruptedException {
//        seed = 9000;
//        doIsaacAlt32R();
//    }
//
//    public long doIsaacAlt32IntR()
//    {
//        RNG rng = new RNG(new Isaac32AltRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void aa_measureIsaacAlt32IntR() throws InterruptedException {
//        iseed = 9000;
//        doIsaacAlt32IntR();
//    }
    


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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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

    public long doLFSR()
    {
        LFSR rng = new LFSR(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLFSR() throws InterruptedException {
        seed = 9000;
        doLFSR();
    }

    public long doLFSRInt()
    {
        LFSR rng = new LFSR(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLFSRInt() throws InterruptedException {
        iseed = 9000;
        doLFSRInt();
    }
    public long doLFSRR()
    {
        RNG rng = new RNG(new LFSR(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLFSRR() throws InterruptedException {
        seed = 9000;
        doLFSRR();
    }

    public long doLFSRIntR()
    {
        RNG rng = new RNG(new LFSR(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }



    public long doNLFSR27()
    {
        NLFSR.NLFSR27 rng = new NLFSR.NLFSR27(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR27() {
        seed = 9000;
        doNLFSR27();
    }

    public long doNLFSR27Int()
    {
        NLFSR.NLFSR27 rng = new NLFSR.NLFSR27(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR27Int() {
        iseed = 9000;
        doNLFSR27Int();
    }
    public long doNLFSR27R()
    {
        RNG rng = new RNG(new NLFSR.NLFSR27(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR27R() {
        seed = 9000;
        doNLFSR27R();
    }

    public long doNLFSR27IntR()
    {
        RNG rng = new RNG(new NLFSR.NLFSR27(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR27IntR() {
        iseed = 9000;
        doNLFSR27IntR();
    }


    public long doNLFSR25()
    {
        NLFSR.NLFSR25 rng = new NLFSR.NLFSR25(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR25() {
        seed = 9000;
        doNLFSR25();
    }

    public long doNLFSR25Int()
    {
        NLFSR.NLFSR25 rng = new NLFSR.NLFSR25(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR25Int() {
        iseed = 9000;
        doNLFSR25Int();
    }
    public long doNLFSR25R()
    {
        RNG rng = new RNG(new NLFSR.NLFSR25(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR25R() {
        seed = 9000;
        doNLFSR25R();
    }

    public long doNLFSR25IntR()
    {
        RNG rng = new RNG(new NLFSR.NLFSR25(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a________measureNLFSR25IntR() {
        iseed = 9000;
        doNLFSR25IntR();
    }





    public long doJDK()
    {
        Random rng = new Random(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
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
    @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJDKInt() throws InterruptedException {
        iseed = 9000;
        doJDKInt();
    }

    /*
mvn clean install
java -jar target/benchmarks.jar UncommonBenchmark -wi 5 -i 5 -f 1
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UncommonBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
