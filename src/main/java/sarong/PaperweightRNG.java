package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * This is a SplittableRandom-style generator that uses very few kinds of operations,
 * just XOR, logical shifts (left and right), and subtraction, but still has excellent
 * quality. It has passed 16TB of PractRand testing with no anomalies (waiting for 32TB
 * still). It does not need efficient bitwise rotation like Xoroshiro-based generators
 * do. It is not as fast as {@link LightRNG}, which offers the same features, though
 * this has slightly fewer statistical anomalies in testing.
 * <br>
 * The name comes from how this is a lightweight generator that has had the "weight" of
 * shift operations carefully tweaked so the right xorshifts have a total weight of 63.
 * With the wrong weight, it had much lower quality early on in development.
 * @author Tommy Ettinger
 */
public final class PaperweightRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = -374415589203474497L;

    public long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public PaperweightRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public PaperweightRNG(final long seed) {
        setState(seed);
    }

    public PaperweightRNG(final String seed) {
        setState(CrossHash.hash64(seed));
    }

    @Override
    public final int next(int bits)
    {
        long z = (state -= 0x9E3779B97F4A7C15L);
        z ^= z >>> 13;
        z = (z << 19) - z;
        z ^= z >>> 12;
        z = (z << 17) - z;
        z ^= z >>> 14;
        z = (z << 13) - z;
        return (int) ((z ^ z >>> 24) >>> (64 - bits));
   }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state -= 0x9E3779B97F4A7C15L);
//        z = (z ^ z >>> 13) * 0x7FFFF;
//        z = (z ^ z >>> 12) * 0x1FFFF;
//        z = (z ^ z >>> 14) * 0x1FFF;
        z ^= z >>> 13;
        z = (z << 19) - z;
        z ^= z >>> 12;
        z = (z << 17) - z;
        z ^= z >>> 14;
        z = (z << 13) - z;
        return z ^ z >>> 24;

    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public PaperweightRNG copy() {
        return new PaperweightRNG(state);
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
        return (int) ((bound * (nextLong() & 0x7FFFFFFFL)) >>> 31) & ~(bound >> 31);
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
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets
     * exactly one random number, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
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
        long z = (state -= 0x9E3779B97F4A7C15L * advance);
        z = (z ^ z >>> 13) * 0x7FFFF;
        z = (z ^ z >>> 12) * 0x1FFFF;
        z = (z ^ z >>> 14) * 0x1FFF;
        return z ^ z >>> 24;
    }


    @Override
    public String toString() {
        return "LightRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        state = (state ^ state >>> 13) * 0x7FFFF;
        state = (state ^ state >>> 12) * 0x1FFFF;
        state = (state ^ state >>> 14) * 0x1FFF;
        return state ^ state >>> 24;
    }

    public static int determineBounded(long state, final int bound)
    {
        state *= 0x9E3779B97F4A7C15L;
        state = (state ^ state >>> 13) * 0x7FFFF;
        state = (state ^ state >>> 12) * 0x1FFFF;
        state = (state ^ state >>> 14) * 0x1FFF;
        return (int) ((bound * ((state ^ state >>> 24) & 0x7FFFFFFFL)) >> 31);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaperweightRNG paperweightRNG = (PaperweightRNG) o;

        return state == paperweightRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
}
