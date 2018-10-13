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
 * PointHashBenchmark.measureGoldenPointHash2   avgt    5   6.022 ± 0.123  ns/op
 * PointHashBenchmark.measureHastyPointHash2    avgt    5   8.565 ± 0.051  ns/op
 * PointHashBenchmark.measurePointHash2         avgt    5   6.997 ± 0.061  ns/op
 * PointHashBenchmark.measureSzudzikPointHash2  avgt    5  12.069 ± 0.044  ns/op
 * </pre>
 * This benchmark just shows the 2D hashes (2 parameters and also a state parameter). The ranking of fastest to slowest
 * doesn't change for higher dimensions, though the Szudzik hashes slow down a lot more than the others with extra
 * dimensions (they're always in last place, so it doesn't matter anyway). Golden seems to be surprisingly fast, and I
 * wasn't really considering its algorithm seriously until I saw these benchmarks. PointHash is also surprisingly fast,
 * considering how much more code it is than Golden.
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class PointHashBenchmark {
    private final long[] inputs = new long[65536];

    {
        for (int i = 0; i < 65536; i++) {
            inputs[i] = LinnormRNG.determine(i);
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
            x = ((x >= y ? x * x + x + y : x + y * y) ^ state) * 0xC6BC279692B5CC8BL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
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
            x = ((x >= y ? x * x + x + y : x + y * y) ^ state) * 0xC6BC279692B5CC8BL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
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
            x = ((x >= y ? x * x + x + y : x + y * y) ^ state) * 0xC6BC279692B5CC8BL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
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
            x = ((x >= y ? x * x + x + y : x + y * y) ^ state) * 0xC6BC279692B5CC8BL;
            return (x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ (x >>> 22);
        }
    }

    public static final class GoldenPointHash {
        /**
         * @param x
         * @param y
         * @param s
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return ((s = (s ^ s >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param s
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED688L;
            return ((s = (s ^ s >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param s
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E6L;
            s += x * 0x881403B9339BD42DL;
            return ((s = (s ^ s >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param s
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782EL;
            w += u * 0xC1EDBC5B5C68AC24L;
            z += w * 0xB0C8AC50F0EDEF5CL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB2L;
            s += x * 0x85EB75C3024385C3L;
            return ((s = (s ^ s >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }
    }
    @Benchmark
    public long measurePointHash2()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash3()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash4()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measurePointHash6()
    {
        return PointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF],
                inputs[u++ & 0xFFFF], inputs[v++ & 0xFFFF], 1L);
    }

    @Benchmark
    public long measureHastyPointHash2()
    {
        return HastyPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureHastyPointHash3()
    {
        return HastyPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureHastyPointHash4()
    {
        return HastyPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureHastyPointHash6()
    {
        return HastyPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF],
                inputs[u++ & 0xFFFF], inputs[v++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash2()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash3()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash4()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureSzudzikPointHash6()
    {
        return SzudzikPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF],
                inputs[u++ & 0xFFFF], inputs[v++ & 0xFFFF], 1L);
    }

    @Benchmark
    public long measureGoldenPointHash2()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash3()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash4()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF], 1L);
    }
    @Benchmark
    public long measureGoldenPointHash6()
    {
        return GoldenPointHash.hashAll(inputs[x++ & 0xFFFF], inputs[y++ & 0xFFFF], inputs[z++ & 0xFFFF], inputs[w++ & 0xFFFF],
                inputs[u++ & 0xFFFF], inputs[v++ & 0xFFFF], 1L);
    }


}
