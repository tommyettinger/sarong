/*
Written in 2015 by Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * This is a SplittableRandom-style generator, meant to have a tiny state
 * that permits storing many different generators with low overhead.
 * It should be rather fast, though no guarantees can be made on all hardware.
 * <br>
 * Benchmarking on a Windows laptop with an i7-4700MQ processor running OpenJDK 8
 * reports generation of 64-bit random long output as 17.8x faster than generating
 * an equivalent number of random longs with java.util.Random, and generation of
 * 32-bit random int output as 9.8x faster. Specifically, generating 1 billion longs
 * took about 1.28 nanoseconds per long (1.277 seconds for the whole group) with
 * LightRNG, while java.util.Random (which is meant to produce int, to be fair) took
 * about 22.8 nanoseconds per long (22.797 seconds for the whole group). XorRNG
 * appears to be occasionally faster on int output than LightRNG, but it isn't clear
 * why or what causes that (JIT or GC internals, possibly). XorRNG is slightly
 * slower at generating 64-bit random data, including long and double, but not by
 * a significant degree (a multiplier between 0.9 and 1.2 times). The only deciding
 * factors then are period, where all other generators are the same as LightRNG or
 * better (but the period doesn't matter for most short-running or non-parallel
 * apps), and state size, where LightRNG is as small as possible for any JVM object
 * with even a single field: 16 bytes (on a 64-bit JVM; 8-byte objects with 4 bytes
 * or less of non-static members may be possible on 32-bit JVMs but I can't find
 * anything confirming that guess).
 * <br>
 * So yes, this should be very fast, and with only a single long used per LightRNG,
 * it is about as memory-efficient as these generators get. It is approximately as
 * fast as XoRoRNG on modern PC hardware, but LightRNG is likely faster on phones
 * and older PCs.
 * <br>
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 *
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public final class LightRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    /**
     * 2 raised to the 53, - 1.
     */
    private static final long DOUBLE_MASK = (1L << 53) - 1;
    /**
     * 2 raised to the -53.
     */
    private static final double NORM_53 = 1. / (1L << 53);
    /**
     * 2 raised to the 24, -1.
     */
    private static final long FLOAT_MASK = (1L << 24) - 1;
    /**
     * 2 raised to the -24.
     */
    private static final float NORM_24 = 1f / (1 << 24);

    private static final long serialVersionUID = -374415589203474497L;

    public long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public LightRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public LightRNG(final long seed) {
        setState(seed);
    }

    public LightRNG(final String seed) {
        setState(CrossHash.hash64(seed));
    }

    @Override
    public final int next(int bits)
    {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) ((z ^ (z >>> 31)) & (1L << bits) - 1);
   }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public LightRNG copy() {
        return new LightRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        return (int) nextLong();
    }

    /**
     * Exclusive on the upper bound.  The lower bound is 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public final int nextInt(final int bound) {
        if (bound <= 0) return 0;
        return (int)((bound * (nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Inclusive lower, exclusive upper.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt(final int lower, final int upper) {
        if (upper - lower <= 0) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random long less than n
     */
    public final long nextLong(final long bound) {
        if (bound <= 0) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    /**
     * Inclusive lower, exclusive upper.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public final long nextLong(final long lower, final long upper) {
        if (upper - lower <= 0) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextLong(upper - lower);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        return (nextLong() & DOUBLE_MASK) * NORM_53;
    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        return ((nextLong() & FLOAT_MASK) * NORM_24);
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     *
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public final void nextBytes(final byte[] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) bytes[--i] = (byte) bits;
        }
    }

    /**
     * Sets the seed (also the current state) of this generator.
     *
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    /**
     * Advances or rolls back the LightRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    public final long skip(long advance) {
        long z = (state += 0x9E3779B97F4A7C15L * advance);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }


    @Override
    public String toString() {
        return "LightRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(long state)
    {
        state = (((state *= 0x9E3779B97F4A7C15L) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static long determine(final int a, final int b)
    {
        long state = 0x9E3779B97F4A7C15L + (a & 0xFFFFFFFFL) + ((long)b << 32);
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static int determineBounded(long state, final int bound)
    {
        state = (((state *= 0x9E3779B97F4A7C15L) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return (int)((bound * ((state ^ (state >>> 31)) & 0x7FFFFFFFL)) >> 31);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightRNG lightRNG = (LightRNG) o;

        return state == lightRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
}
