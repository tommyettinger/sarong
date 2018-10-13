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

import java.util.concurrent.TimeUnit;

/**
 * Shuffling distinct int sequences:
 * <br>
 * <pre>
 * Benchmark                                 Mode  Cnt   Score   Error  Units
 * ShufflerBenchmark.measureSIS_10_Bound     avgt    5  24.386 ± 0.138  ns/op
 * ShufflerBenchmark.measureSIS_11_Bound     avgt    5  23.927 ± 0.239  ns/op
 * ShufflerBenchmark.measureSIS_12_Bound     avgt    5  20.674 ± 0.178  ns/op
 * ShufflerBenchmark.measureSIS_13_Bound     avgt    5  19.793 ± 1.683  ns/op
 * ShufflerBenchmark.measureSIS_14_Bound     avgt    5  16.578 ± 0.132  ns/op
 * ShufflerBenchmark.measureSIS_15_Bound     avgt    5  15.145 ± 0.050  ns/op
 * ShufflerBenchmark.measureSIS_16_Bound     avgt    5  12.756 ± 0.066  ns/op
 * ShufflerBenchmark.measureSIS_17_Bound     avgt    5  45.882 ± 0.418  ns/op
 * ShufflerBenchmark.measureSIS_4_Bound      avgt    5  16.720 ± 0.053  ns/op
 * ShufflerBenchmark.measureSIS_5_Bound      avgt    5  49.284 ± 0.304  ns/op
 * ShufflerBenchmark.measureSIS_6_Bound      avgt    5  39.610 ± 0.587  ns/op
 * ShufflerBenchmark.measureSIS_7_Bound      avgt    5  35.743 ± 0.203  ns/op
 * ShufflerBenchmark.measureSIS_8_Bound      avgt    5  24.795 ± 1.955  ns/op
 * ShufflerBenchmark.measureSIS_9_Bound      avgt    5  26.620 ± 0.385  ns/op
 * ShufflerBenchmark.measureSNSIS_255_Bound  avgt    5  21.177 ± 0.128  ns/op
 * ShufflerBenchmark.measureSNSIS_256_Bound  avgt    5  21.172 ± 0.127  ns/op
 * ShufflerBenchmark.measureSNSIS_257_Bound  avgt    5  21.122 ± 0.068  ns/op
 * </pre>
 * This is with 2 rounds for SIS (ShuffledIntSequence) and 6 rounds for SNSIS (SNShuffledIntSequence).
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
public class ShufflerBenchmark {

    private final ShuffledIntSequence
            sis4  = new ShuffledIntSequence(4 , 1),
            sis5  = new ShuffledIntSequence(5 , 1),
            sis6  = new ShuffledIntSequence(6 , 1),
            sis7  = new ShuffledIntSequence(7 , 1),
            sis8  = new ShuffledIntSequence(8 , 1),
            sis9  = new ShuffledIntSequence(9 , 1),
            sis10 = new ShuffledIntSequence(10, 1),
            sis11 = new ShuffledIntSequence(11, 1),
            sis12 = new ShuffledIntSequence(12, 1),
            sis13 = new ShuffledIntSequence(13, 1),
            sis14 = new ShuffledIntSequence(14, 1),
            sis15 = new ShuffledIntSequence(15, 1),
            sis16 = new ShuffledIntSequence(16, 1),
            sis17 = new ShuffledIntSequence(17, 1);
    @Benchmark
    public int measureSIS_4_Bound(){
        return sis4.next();
    }
    @Benchmark
    public int measureSIS_5_Bound(){
        return sis5.next();
    }
    @Benchmark
    public int measureSIS_6_Bound(){
        return sis6.next();
    }
    @Benchmark
    public int measureSIS_7_Bound(){
        return sis7.next();
    }
    @Benchmark
    public int measureSIS_8_Bound(){
        return sis8.next();
    }
    @Benchmark
    public int measureSIS_9_Bound(){
        return sis9.next();
    }
    @Benchmark
    public int measureSIS_10_Bound(){
        return sis10.next();
    }
    @Benchmark
    public int measureSIS_11_Bound(){
        return sis11.next();
    }
    @Benchmark
    public int measureSIS_12_Bound(){
        return sis12.next();
    }
    @Benchmark
    public int measureSIS_13_Bound(){
        return sis13.next();
    }
    @Benchmark
    public int measureSIS_14_Bound(){
        return sis14.next();
    }
    @Benchmark
    public int measureSIS_15_Bound(){
        return sis15.next();
    }
    @Benchmark
    public int measureSIS_16_Bound(){
        return sis16.next();
    }
    @Benchmark
    public int measureSIS_17_Bound(){
        return sis17.next();
    }

    private final SNShuffledIntSequence
            snsis255 = new SNShuffledIntSequence(255, 31337L),
            snsis256 = new SNShuffledIntSequence(256, 31337L),
            snsis257 = new SNShuffledIntSequence(257, 31337L);
    @Benchmark
    public int measureSNSIS_255_Bound(){
        return snsis255.next();
    }
    @Benchmark
    public int measureSNSIS_257_Bound(){
        return snsis257.next();
    }
    @Benchmark
    public int measureSNSIS_256_Bound(){
        return snsis256.next();
    }
}
