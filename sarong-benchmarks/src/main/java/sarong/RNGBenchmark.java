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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureThunder() throws InterruptedException {
        seed = 9000;
        doThunder();
    }

    public long doThunderInt()
    {
        ThunderRNG rng = new ThunderRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureXoRo() throws InterruptedException {
        seed = 9000;
        doXoRo();
    }

    public long doXoRoInt()
    {
        XoRoRNG rng = new XoRoRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureXoRoIntR() throws InterruptedException {
        iseed = 9000;
        doXoRoIntR();
    }

    public long doLight()
    {
        LightRNG rng = new LightRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureLight() throws InterruptedException {
        seed = 9000;
        doLight();
    }

    public long doLightInt()
    {
        LightRNG rng = new LightRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureLightIntR() throws InterruptedException {
        iseed = 9000;
        doLightIntR();
    }
    
    
    public long doDash()
    {
        DashRNG rng = new DashRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureDash() throws InterruptedException {
        seed = 9000;
        doDash();
    }

    public long doDashInt()
    {
        DashRNG rng = new DashRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureDashInt() throws InterruptedException {
        iseed = 9000;
        doDashInt();
    }
    public long doDashR()
    {
        RNG rng = new RNG(new DashRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureDashR() throws InterruptedException {
        seed = 9000;
        doDashR();
    }

    public long doDashIntR()
    {
        RNG rng = new RNG(new DashRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureDashIntR() throws InterruptedException {
        iseed = 9000;
        doDashIntR();
    }


    /*
mvn clean install
java -jar target/benchmarks.jar RNGBenchmark -wi 5 -i 5 -f 1
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RNGBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
