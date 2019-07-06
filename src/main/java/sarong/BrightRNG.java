/*
Written in 2019 by Tommy Ettinger.

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * This is a small, surprisingly-simple generator that allows reading the state and setting it to any long value, as
 * well as skipping ahead in the stream of random numbers this can produce (all 2 to the 64 possible longs). It passes
 * 32TB (at least) of PractRand testing with no failures or even anomalies. The generator uses a simple large-increment
 * counter (also called a Weyl sequence) to change its state, and runs the current state through a small but complex
 * unary hash to make the obvious patterns in a Weyl sequence disappear. It is modeled after SplitMix64, but does not
 * permit changing the counter like SplitMix64 does, and involves less multiplication but more bitwise operations.
 * <br>
 * If you're reading the source, you may have doubts that the generator can produce all longs; although the counter is
 * fairly clear that it will traverse the whole span of 2 to the 64 states, and the last step is a garden-variety
 * xorshift, the second step, {@code (z ^ z >>> 29 ^ z >>> 43 ^ z << 7 ^ z << 53)}, doesn't immediately appear to be
 * reversible. Apparently it is, because GNU Octave can find a matrix that inverts it, but I don't know what that matrix
 * is nor how to translate it into Java operations. If all longs are used as inputs and the operation applied to each
 * state is a bijection, then all longs will be produced as outputs.
 * <a href="https://mostlymangling.blogspot.com/2018/09/invertible-additions-mod-2.html">This article</a> explains why
 * the complex set of xorshifts is a bijection.
 * <br>
 * Written in 2019 by Tommy Ettinger.
 * Uses <a href="https://github.com/pellevensen/bijections">this data</a> found by Pelle Evensen.
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public final class BrightRNG implements StatefulRandomness, SkippingRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    public long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public BrightRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public BrightRNG(final long seed) {
        state = seed;
    }

    public BrightRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state += 0xEB44ACCAB455D165L);
        z = (z ^ z >>> 29 ^ z >>> 43 ^ z << 7 ^ z << 53) * 0xDB4F0B9175AE2165L;
        return (int) ((z ^ z >>> 26) >>> 64 - bits);
   }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state += 0xEB44ACCAB455D165L);
        z = (z ^ z >>> 29 ^ z >>> 43 ^ z << 7 ^ z << 53) * 0xDB4F0B9175AE2165L;
        return z ^ z >>> 26;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public BrightRNG copy() {
        return new BrightRNG(state);
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
        return (int)((bound * (nextLong() & 0xFFFFFFFFL)) >>> 32);
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
    public final long nextLong(long bound) {
        long rand = nextLong();
        if (bound <= 0) return 0;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
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
        return (nextLong()  & 0x1fffffffffffffL) * 0x1p-53;
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
        return ((nextLong() & 0xFFFFFFL) * 0x1p-24f);
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
        int i = bytes.length, n;
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
        long z = (state += 0xEB44ACCAB455D165L * advance);
        z = (z ^ z >>> 29 ^ z >>> 43 ^ z << 7 ^ z << 53) * 0xDB4F0B9175AE2165L;
        return z ^ z >>> 26;
    }


    @Override
    public String toString() {
        return "LightRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(long state)
    {
        return (state = ((state *= 0xEB44ACCAB455D165L) ^ state >>> 29 ^ state >>> 43 ^ state << 7 ^ state << 53) * 0xDB4F0B9175AE2165L) ^ state >>> 26;
    }

    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state *= 0xEB44ACCAB455D165L) ^ state >>> 29 ^ state >>> 43 ^ state << 7 ^ state << 53) * 0xDB4F0B9175AE2165L) ^ state >>> 26) & 0x7FFFFFFFL)) >> 31);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrightRNG brightRNG = (BrightRNG) o;

        return state == brightRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ state >>> 32);
    }
}
