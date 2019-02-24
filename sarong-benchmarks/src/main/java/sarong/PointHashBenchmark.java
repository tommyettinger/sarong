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
 * Benchmarks of hashes that take a fixed number of arguments, usually for geometric things like Perlin noise.
 * <pre>
 * Benchmark                                    Mode  Cnt   Score   Error  Units
 * PointHashBenchmark.measureCantorPointHash2   avgt    5   6.241 ± 0.025  ns/op
 * PointHashBenchmark.measureCantorPointHash3   avgt    5   8.096 ± 0.155  ns/op
 * PointHashBenchmark.measureCantorPointHash4   avgt    5  10.427 ± 0.136  ns/op
 * PointHashBenchmark.measureCantorPointHash6   avgt    5  13.904 ± 0.213  ns/op
 * PointHashBenchmark.measureGoldenPointHash2   avgt    5   5.978 ± 0.148  ns/op
 * PointHashBenchmark.measureGoldenPointHash3   avgt    5   6.974 ± 0.043  ns/op
 * PointHashBenchmark.measureGoldenPointHash4   avgt    5   8.201 ± 0.179  ns/op
 * PointHashBenchmark.measureGoldenPointHash6   avgt    5  11.242 ± 0.171  ns/op
 * PointHashBenchmark.measureHastyPointHash2    avgt    5   8.498 ± 0.175  ns/op
 * PointHashBenchmark.measureHastyPointHash3    avgt    5  11.083 ± 0.114  ns/op
 * PointHashBenchmark.measureHastyPointHash4    avgt    5  13.417 ± 1.215  ns/op
 * PointHashBenchmark.measureHastyPointHash6    avgt    5  19.454 ± 0.100  ns/op
 * PointHashBenchmark.measurePointHash2         avgt    5   6.932 ± 0.217  ns/op
 * PointHashBenchmark.measurePointHash3         avgt    5   8.551 ± 0.234  ns/op
 * PointHashBenchmark.measurePointHash4         avgt    5  10.112 ± 0.068  ns/op
 * PointHashBenchmark.measurePointHash6         avgt    5  13.027 ± 0.215  ns/op
 * PointHashBenchmark.measureSzudzikPointHash2  avgt    5  11.974 ± 0.249  ns/op
 * PointHashBenchmark.measureSzudzikPointHash3  avgt    5  18.125 ± 0.084  ns/op
 * PointHashBenchmark.measureSzudzikPointHash4  avgt    5  23.321 ± 0.584  ns/op
 * PointHashBenchmark.measureSzudzikPointHash6  avgt    5  33.887 ± 0.413  ns/op
 * </pre>
 * This benchmark shows the time for each hash in nanoseconds per op (the second decimal is the range of error).
 * GoldenPointHash is the fastest, PointHash and CantorPointHash are in a surprising tie for second place, and the Hasty
 * and Szudzik point hashes don't offer much here, in the last two places. The Szudzik hashes slow down a lot more than
 * the others with extra dimensions (they're always in last place, so it doesn't matter anyway). Golden seems to be
 * surprisingly fast, and I wasn't really considering its algorithm seriously until I saw these benchmarks. PointHash is
 * also surprisingly fast, considering how much more code it is than Golden. Cantor potentially is better as an actual
 * hash-table hash function because it involves a perfect hash function, but it is still a little slower than Golden.
 * Cantor also may possibly be symmetrical across the line {@code y = -x} in 2D, and similar hyperplanes in higher
 * dimensions. Golden has replaced HastyPointHash (the one here) in SquidLib, and some noise functions have seen gains.
 * <br>
 * For just 2D hashes, here's a benchmark that includes SquidLib's Coord.hashCode() algorithm extended to use two long
 * inputs instead of its normal two ints, as LathePointHash. Interestingly, it's faster than the previous fastest 2D
 * point hash, and has comparable collision rates to Cantor, but isn't at all random like Golden. LathePointHash doesn't
 * use any arithmetic operations, only bitwise ones, which may help its performance on GWT.
 * <pre>
 * Benchmark                                    Mode  Cnt   Score   Error  Units
 * PointHashBenchmark.measureCantorPointHash2   avgt    6   5.645 ± 0.030  ns/op
 * PointHashBenchmark.measureGoldenPointHash2   avgt    6   5.355 ± 0.043  ns/op
 * PointHashBenchmark.measureLathePointHash2    avgt    6   5.013 ± 0.006  ns/op
 * PointHashBenchmark.measurePointHash2         avgt    6   6.372 ± 0.336  ns/op
 * PointHashBenchmark.measureSzudzikPointHash2  avgt    6  10.182 ± 0.076  ns/op
 * </pre>
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class PointHashBenchmark {
    private static volatile int[] inputs = new int[0x100000];
    static {
        for (int i = 0; i < 0x100000; i++) {
            inputs[i] = (int)PelicanRNG.determine(i);
        }
    }
    private int x = 1, y = 12, z = 23, w = 34, u = 45, v = 56;

    public static final class PointHash {
        /**
         * @param x
         * @param y
         * @param state
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long state) {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long state) {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long state) {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long state) {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;
        }
    }

    public static final class HastyPointHash {
        /**
         * @param x
         * @param y
         * @param state
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long state) {
            x += y += state += 0x9E3779B97F4A7C15L;
            state *= ((y ^= 0xC6BC279692B5CC8BL + x - (x << 35 | x >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((x ^= 0xC6BC279692B5CC8BL + y - (y << 35 | y >>> 29)) | 1);
            state ^= state >>> 31;
            x ^= y ^ state;
            return x ^ x >>> 25;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long state) {
            x += y += z += state += 0x9E3779B97F4A7C15L;
            state *= ((y ^= 0xC6BC279692B5CC8BL + x - (x << 35 | x >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((z ^= 0xC6BC279692B5CC8BL + y - (y << 35 | y >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((x ^= 0xC6BC279692B5CC8BL + z - (z << 35 | z >>> 29)) | 1);
            state ^= state >>> 31;
            x ^= y ^ z ^ state;
            return x ^ x >>> 25;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long state) {
            x += y += z += w += state += 0x9E3779B97F4A7C15L;
            state *= ((y ^= 0xC6BC279692B5CC8BL + x - (x << 35 | x >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((z ^= 0xC6BC279692B5CC8BL + y - (y << 35 | y >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((w ^= 0xC6BC279692B5CC8BL + z - (z << 35 | z >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((x ^= 0xC6BC279692B5CC8BL + w - (w << 35 | w >>> 29)) | 1);
            state ^= state >>> 31;
            x ^= y ^ z ^ w ^ state;
            return x ^ x >>> 25;
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long state) {
            x += y += z += w += u += v += state += 0x9E3779B97F4A7C15L;
            state *= ((y ^= 0xC6BC279692B5CC8BL + x - (x << 35 | x >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((z ^= 0xC6BC279692B5CC8BL + y - (y << 35 | y >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((w ^= 0xC6BC279692B5CC8BL + z - (z << 35 | z >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((u ^= 0xC6BC279692B5CC8BL + w - (w << 35 | w >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((v ^= 0xC6BC279692B5CC8BL + u - (u << 35 | u >>> 29)) | 1);
            state ^= state >>> 31;
            state *= ((x ^= 0xC6BC279692B5CC8BL + v - (v << 35 | v >>> 29)) | 1);
            state ^= state >>> 31;
            x ^= y ^ z ^ w ^ u ^ v ^ state;
            return x ^ x >>> 25;
        }
    }
    public static final class SzudzikPointHash {
        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
         * This uses a 3D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long state)
        {
            return ((x >= y ? x * x + x + y : x + y * y) * (state<<1|1L)) ^ state;
//            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }
        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
         * This uses a 3D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long state)
        {
            y = (y >= z ? y * y + y + z : y + z * z);
            return ((x >= y ? x * x + x + y : x + y * y) * (state<<1|1L)) ^ state;
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
         * This uses a 4D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long state)
        {
            z = (z >= w ? z * z + z + w : z + w * w);
            y = (y >= z ? y * y + y + z : y + z * z);
            return ((x >= y ? x * x + x + y : x + y * y) * (state<<1|1L)) ^ state;
//            x = ((x >= y ? x * x + x + y : x + y * y) ^ state) * 0xC6BC279692B5CC8BL;
//            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
         * This uses a 4D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long state)
        {
            u = (u >= v ? u * u + u + v : u + v * v);
            w = (w >= u ? w * w + w + u : w + u * u);
            z = (z >= w ? z * z + z + w : z + w * w);
            y = (y >= z ? y * y + y + z : y + z * z);
            return ((x >= y ? x * x + x + y : x + y * y) * (state<<1|1L)) ^ state;
        }
    }
//    public static final class Szudzik2PointHash {
//        /**
//         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
//         * This uses a 3D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
//         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
//         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
//         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param state any long
//         * @return 64-bit hash of the x,y,z point with the given state
//         */
//        public static long hashAll(long x, long y, long state)
//        {
//            return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 21 | x >>> 43) ^ (x << 52 | x >>> 12) ^ state) * 0x9E3779B97F4A7C15L;
//        }
//        /**
//         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
//         * This uses a 3D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
//         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
//         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
//         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param state any long
//         * @return 64-bit hash of the x,y,z point with the given state
//         */
//        public static long hashAll(long x, long y, long z, long state)
//        {
//            y = (y >= z ? y * y + y + z : y + z * z);
//            return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 21 | x >>> 43) ^ (x << 52 | x >>> 12) ^ state) * 0x9E3779B97F4A7C15L;
////            return (state += (x >= y ? x * x + x + y : x + y * y) * 0x9E3779B97F4A7C15L) ^ state >>> 26;
//        }
//
//        /**
//         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
//         * This uses a 4D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
//         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
//         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
//         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param state any long
//         * @return 64-bit hash of the x,y,z,w point with the given state
//         */
//        public static long hashAll(long x, long y, long z, long w, long state)
//        {
//            z = (z >= w ? z * z + z + w : z + w * w);
//            y = (y >= z ? y * y + y + z : y + z * z);
//            return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 21 | x >>> 43) ^ (x << 52 | x >>> 12) ^ state) * 0x9E3779B97F4A7C15L;
//        }
//
//        /**
//         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
//         * This uses a 4D variant of Matthew Szudzik's function for perfect hashing in 2D, then feeds that (very non-random)
//         * perfect hash through a simple RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
//         * @see <a href="https://stackoverflow.com/a/13871379/786740">Szudzik's function described on StackOverflow</a>
//         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
//         * @param state any long
//         * @return 64-bit hash of the x,y,z,w point with the given state
//         */
//        public static long hashAll(long x, long y, long z, long w, long u, long v, long state)
//        {
//            u = (u >= v ? u * u + u + v : u + v * v);
//            w = (w >= u ? w * w + w + u : w + u * u);
//            z = (z >= w ? z * z + z + w : z + w * w);
//            y = (y >= z ? y * y + y + z : y + z * z);
//            return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 21 | x >>> 43) ^ (x << 52 | x >>> 12) ^ state) * 0x9E3779B97F4A7C15L;
//        }
//    }

    public static final class GoldenPointHash {
        /**
         * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED689L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E7L;
            s += x * 0x881403B9339BD42DL;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782FL;
            w += u * 0xC1EDBC5B5C68AC25L;
            z += w * 0xB0C8AC50F0EDEF5DL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB3L;
            s += x * 0x85EB75C3024385C3L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }
    }

    public static final class Golden2PointHash {
        /**
         * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long s) {
            s += (x + (y + s * 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L) * 0x8CB92BA72F3D8DD7L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long s) {
            s += (x + (y + (z + s * 0xDB4F0B9175AE2165L) * 0xBBE0563303A4615FL) * 0xA0F2EC75A1FE1575L) * 0x89E182857D9ED689L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long s) {
            s += (x + (y + (z + (w + s * 0xE19B01AA9D42C633L) * 0xC6D1D6C8ED0C9631L) * 0xAF36D01EF7518DBBL) * 0x9A69443F36F710E7L) * 0x881403B9339BD42DL;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has just about the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
            s += (x + (y + (z + (w + (u + (v + s * 0xE95E1DD17D35800DL) * 0xD4BC74E13F3C782FL) * 0xC1EDBC5B5C68AC25L) * 0xB0C8AC50F0EDEF5DL) * 0xA127A31C56D1CDB5L) * 0x92E852C80D153DB3L) * 0x85EB75C3024385C3L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }
    }

    public static final class CantorPointHash {
        /**
         * Gets a 64-bit point hash of a 3D point (x and y are both longs) and a state/seed as a long.
         * This uses the 2D Cantor tuple function, then feeds that (very non-random) perfect hash through a simple
         * RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long state)
        {
            x += ((((x+y) * (x+y+1) >> 1) + y) ^ state) * 0xC13FA9A902A6328FL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }
        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
         * This uses the 3D Cantor tuple function, then feeds that (very non-random) perfect hash through a simple
         * RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long state)
        {
            y += (((y+z) * (y+z+1) >> 1) + z);
            x += ((((x+y) * (x+y+1) >> 1) + y) ^ state) * 0xC13FA9A902A6328FL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
         * This uses the 3D Cantor tuple function, then feeds that (very non-random) perfect hash through a simple
         * RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long state)
        {
            z += (((z+w) * (z+w+1) >> 1) + w);
            y += (((y+z) * (y+z+1) >> 1) + z);
            x += ((((x+y) * (x+y+1) >> 1) + y) ^ state) * 0xC13FA9A902A6328FL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }

        /**
         * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
         * This uses the 6D Cantor tuple function, then feeds that (very non-random) perfect hash through a simple
         * RNG-like unary hash (it is close to SquidLib's old ThrustRNG).
         * @param x x position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param y y position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param z z position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param w w position, as a non-negative long (it may work for negative longs, but no guarantees)
         * @param state any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long state)
        {
            u += (((u+v) * (u+v+1) >> 1) + v);
            w += (((w+u) * (w+u+1) >> 1) + u);
            z += (((z+w) * (z+w+1) >> 1) + w);
            y += (((y+z) * (y+z+1) >> 1) + z);
            x += ((((x+y) * (x+y+1) >> 1) + y) ^ state) * 0xC13FA9A902A6328FL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }
    }

    public static final class PelotonPointHash {
        /**
         * A 32-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y point with the given state s
         */
        public static int hashAll(int x, int y, int s) {
            s ^= x * 0x1827F5 ^ y * 0x123C21;
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;
        }
        /**
         * A 32-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y,z point with the given state s
         */
        public static int hashAll(int x, int y, int z, int s) {
            s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;
        }

        /**
         * A 32-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y,z,w point with the given state s
         */
        public static int hashAll(int x, int y, int z, int w, int s) {
            s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;
        }

        /**
         * A 32-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
         * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
         * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
         * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
         * hash used here has been stripped down heavily, both for speed and because unless points are selected
         * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
         * rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param u u position, as an int
         * @param v v position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point 
         * @return 32-bit hash of the x,y,z,w,u,v point with the given state s
         */
        public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
            s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;
        }

//        /**
//         * A 32-bit point hash that smashes x and y into s using XOR and multiplications by 21-bit harmonious numbers,
//         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
//         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
//         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The  unary hash used here
//         * has been stripped down heavily, both for speed and because unless points are selected specifically to target
//         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
//         * @param x x position, as an int
//         * @param y y position, as an int
//         * @param s any int
//         * @return 32-bit hash of the x,y point with the given state s
//         */
//        public static int hashAll(int x, int y, int s) {
//            s ^= 0x1827F5 * (x ^ y * 0x123C21);
//            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
//        }
////            s ^= x + y;
////            s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1827F5;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x123C21;
////            return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
////            s ^= x * 0x1827F5 ^ y * 0x123C21;
//        //return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
//
//        public static int hashAll(int x, int y, int z, int s) {
//            s ^= 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
//            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
//        }
////            s ^= x + y + z;
////            s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1A36A9;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x157931;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (z ^ (z << 11 | z >>> 21) ^ (z << 23 | z >>> 9) ^ s) * 0x119725;
////            return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
////            s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
//        //return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
//
//        public static int hashAll(int x, int y, int z, int w, int s) {
////            s ^= x + y + z + w;
////            s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1B69E1;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x177C0B;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (z ^ (z << 11 | z >>> 21) ^ (z << 23 | z >>> 9) ^ s) * 0x141E5D;
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (w ^ (w << 13 | w >>> 19) ^ (w << 25 | w >>> 7) ^ s) * 0x113C31;
////            return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
////            s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
//            //return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
//            s ^= 0x1B69E1 * (x ^ 0x177C0B * (y ^ 0x141E5D * (z ^ w * 0x113C31)));
//            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
//        }
//
//
//        public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
////            s ^= x + y + z + w + u + v;
////            s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * (0x1CC1C5);
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * (0x19D7AF);
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (z ^ (z << 11 | z >>> 21) ^ (z << 23 | z >>> 9) ^ s) * (0x173935);
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (w ^ (w << 13 | w >>> 19) ^ (w << 25 | w >>> 7) ^ s) * (0x14DEAF);
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (z ^ (z << 15 | z >>> 17) ^ (z << 27 | z >>> 5) ^ s) * (0x12C139);
////            s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
////            s = (w ^ (w << 17 | w >>> 15) ^ (w << 29 | w >>> 3) ^ s) * (0x10DAA3);
////            return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
////            s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
//            //return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
//            s ^= 0x1CC1C5 * (x ^ 0x19D7AF * (y ^ 0x173935 * (z ^ 0x14DEAF * (w ^ 0x12C139 * (u ^ v * 0x10DAA3)))));
//            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
//        }
        
    }
    
    public static final class LathePointHash {
        public static long hashAll(long x, long y) {
            y ^= x;
            y ^= (x << 24 | x >>> 40) ^ (y << 16) ^ (y << 37 | y >>> 27);
            y = x ^ (y << 21 | y >>> 43);
            return y ^ (y << 51 | y >>> 13);
        }
    }

    @Benchmark
    public long measurePointHash2()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash3()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash4()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash6()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1L);
    }

//    @Benchmark
//    public long measureHastyPointHash2()
//    {
//        return HastyPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1L);
//    }
//    @Benchmark
//    public long measureHastyPointHash3()
//    {
//        return HastyPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1L);
//    }
//    @Benchmark
//    public long measureHastyPointHash4()
//    {
//        return HastyPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1L);
//    }
//    @Benchmark
//    public long measureHastyPointHash6()
//    {
//        return HastyPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
//                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1L);
//    }
    @Benchmark
    public long measureSzudzikPointHash2()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash3()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash4()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash6()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1L);
    }

    @Benchmark
    public long measureGoldenPointHash2()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash3()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash4()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash6()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1L);
    }

    @Benchmark
    public long measureCantorPointHash2()
    {
        return CantorPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureCantorPointHash3()
    {
        return CantorPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureCantorPointHash4()
    {
        return CantorPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1L);
    }
    @Benchmark
    public long measureCantorPointHash6()
    {
        return CantorPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1L);
    }

    @Benchmark
    public int measurePelotonPointHash2()
    {
        return PelotonPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], 1);
    }
    @Benchmark
    public int measurePelotonPointHash3()
    {
        return PelotonPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], 1);
    }
    @Benchmark
    public int measurePelotonPointHash4()
    {
        return PelotonPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF], 1);
    }
    @Benchmark
    public int measurePelotonPointHash6()
    {
        return PelotonPointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF], inputs[z++ & 0xFFFFF], inputs[w++ & 0xFFFFF],
                inputs[u++ & 0xFFFFF], inputs[v++ & 0xFFFFF], 1);
    }
    @Benchmark
    public long measureLathePointHash2()
    {
        return LathePointHash.hashAll(inputs[x++ & 0xFFFFF], inputs[y++ & 0xFFFFF]);
    }

}
