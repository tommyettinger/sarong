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
 * Takes a unitary counter, does a xor-rotate-xor-rotate then a multiply by a large odd constant, then feeds that
 * directly into the hash used by SplitMix64 (David Stafford's Variant 13 of MurmurHash3). Passes 32TB of PractRand with
 * no anomalies. Also passes a short sanity-test that takes all 64 rotations of its input state and feeds them to the
 * generator (the input state still increments by 1, and is rotated before the other steps), as well as all of those
 * steps with the bits reversed in order and all of both of those operations with all bits flipped. This sanity-test was
 * only run on 64GB per rotation, but anomalies were minimal (out of 7 shown results for each of the 256 total tests,
 * there were 111 "unusual" anomalies and 7 "mildly suspicious" ones, none of which persisted over a whole test),
 * @author Tommy Ettinger
 */
public final class ToppingRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 1L;

    public long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public ToppingRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public ToppingRNG(final long seed) {
        setState(seed);
    }

    public ToppingRNG(final String seed) {
        setState(CrossHash.hash64(seed));
    }

    @Override
    public final int next(int bits)
    {
        long z = (state++);
        z = (z ^ (z << 23 | z >>> 41) ^ (z << 47 | z >>> 17)) * 0xEB44ACCAB455D165L;
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
        long z = (state++);
        z = (z ^ (z << 23 | z >>> 41) ^ (z << 47 | z >>> 17)) * 0xEB44ACCAB455D165L;
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
    public ToppingRNG copy() {
        return new ToppingRNG(state);
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
    public long nextLong(long bound) {
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
        return (nextLong() & 0x1fffffffffffffL) * 0x1p-53;
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
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) bytes[--i] = (byte) bits;
        }
    }

    /**
     * Sets the seed (also the current state) of this generator.
     *
     * @param seed the seed to use for this ToppingRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this ToppingRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    /**
     * Advances or rolls back the ToppingRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        long z = (state += advance);
        z = (z ^ (z << 23 | z >>> 41) ^ (z << 47 | z >>> 17)) * 0xEB44ACCAB455D165L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }


    @Override
    public String toString() {
        return "ToppingRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(long state)
    {
        state = (state ^ (state << 23 | state >>> 41) ^ (state << 47 | state >>> 17)) * 0xEB44ACCAB455D165L;
        state = (state ^ (state >>> 30)) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static int determineBounded(long state, final int bound)
    {
        state = (state ^ (state << 23 | state >>> 41) ^ (state << 47 | state >>> 17)) * 0xEB44ACCAB455D165L;
        state = (state ^ (state >>> 30)) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return (int)((bound * ((state ^ (state >>> 31)) & 0x7FFFFFFFL)) >> 31);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToppingRNG toppingRNG = (ToppingRNG) o;

        return state == toppingRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
}
