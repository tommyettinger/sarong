package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A random number generator with 64 bits of state that natively outputs 64 bits at a time, based around a reversible
 * rearrangement of the state's bits that seems very robust quality-wise. This technique is similar to
 * {@link PermutedRNG}, but the exact mechanism is rather different. The state updates linearly, using a large fixed
 * increment like {@link LightRNG}. That state's value is bitwise-rotated 4 times and all rotations are XORed with the
 * state, before one multiplication and one right xorshift, then that is returned. All 64-bit states will be used by
 * this generator over its full period of 2 to the 64, and because it is fully reversible, we know that all 64-bit
 * outputs will be produced over that period (this is also like LightRNG). Using four bitwise rotations is uncommon, but
 * anything less is either non-reversible or fails statistical quality tests. The {@link #determine(long)} methods in
 * this class should be fairly good, and require one less 64-bit multiplication than {@link LightRNG#determine(long)} at
 * the expense of more bitwise operations. This generator passes PractRand at 32TB with only one anomaly (rated
 * "unusual" and near the end of testing, and the last two steps are free of anomalies). It failed one test on TestU01
 * with a seed of 1, but didn't fail any with the same seed when the bits are reversed. It is unclear currently if the
 * TestU01 failure (out of 160 tests run) is a sign of a deeper problem, but PractRand couldn't find one. In fact, the 1
 * anomaly at the 8TB mark of PractRand is better than LightRNG's 2 anomalies at its 8GB mark in PractRand, testing on
 * 1/1024 the data (neither fails PractRand).
 * <br>
 * The name comes from the many rotations the code performs by different amounts, like a whirling dervish.
 * <br>
 * Written in 2018 by Tommy Ettinger.
 * @author Tommy Ettinger
 */
public final class DervishRNG implements StatefulRandomness, SkippingRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public DervishRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DervishRNG(final long seed) {
        state = seed;
    }

    public DervishRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return (int)(y ^ y >>> 26) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return y ^ y >>> 26;
        

        //return determine(state++);
        //final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        //return y ^ y >>> 26;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DervishRNG copy() {
        return new DervishRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return (int)(y ^ y >>> 26);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return (int)((bound * ((y ^ y >>> 26) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Inclusive inner, exclusive outer.
     *
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative, usually greater than inner
     * @return a random int between inner (inclusive) and outer (exclusive)
     */
    public int nextInt(final int inner, final int outer) {
        return inner + nextInt(outer - inner);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     *
     * @param bound the upper bound; should be positive (if negative, this returns 0)
     * @return a random long less than n
     */
    public final long nextLong(final long bound) {
        if (bound <= 0) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = determine(state++) & 0x7fffffffffffffffL;
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
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return ((y ^ y >>> 26) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L;
        return ((y ^ y >>> 26) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        final long z = state;
        state += 0x9E3779B97F4A7C15L;
        return (((z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L) >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        final long z = state;
        state += 0x9E3779B97F4A7C15L;
        return ((z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L) < 0;
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
     * Advances or rolls back the DervishRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(final long advance) {
        final long z = state;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        state += 0x9E3779B97F4A7C15L * advance;
        return y ^ y >>> 26;
    }


    @Override
    public String toString() {
        return "DervishRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(final long state) {
        final long z = state * 0x9E3779B97F4A7C15L;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        return y ^ y >>> 26;
    }

    public static int determineBits(final long state, final int bits) {
        final long z = state * 0x9E3779B97F4A7C15L;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        return (int)(y ^ y >>> 26) >>> (32 - bits);
    }
    
    public static int determineBounded(final long state, final int bound) {
        final long z = state * 0x9E3779B97F4A7C15L;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        return (int)((bound * ((y ^ y >>> 26) & 0xFFFFFFFFL)) >> 32);
    }
    public static float determineFloat(final long state) {
        final long z = state * 0x9E3779B97F4A7C15L;
        return (((z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L) >>> 40) * 0x1p-24f;
    }
    public static double determineDouble(final long state) {
        final long z = state * 0x9E3779B97F4A7C15L;
        final long y = (z ^ (z << 13 | z >>> 51) ^ (z << 31 | z >>> 33) ^ (z << 41 | z >>> 23) ^ (z << 59 | z >>> 5)) * 0x6C8E9CF570932BD3L;
        return ((y ^ y >>> 26) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DervishRNG dervishRNG = (DervishRNG) o;

        return state == dervishRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
//    
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/DervishRNG > Dervish_asm.txt
//         */
//        long longState = 1L;
//        int intState = 1;
//        float floatState = 0f;
//        double doubleState = 0.0;
//        DervishRNG rng = new DervishRNG(1L);
//        //longState += determine(i);
//        //longState = longState + 0x9E3779B97F4A7C15L;
//        //seed += determine(longState++);
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                longState += rng.nextLong();
//            }
//        }
//        System.out.println(longState);
//        
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                intState += rng.next(16);
//            }
//        }
//        System.out.println(intState);
//
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                floatState += rng.nextFloat();
//            }
//        }
//        System.out.println(floatState);
//
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                doubleState += rng.nextDouble();
//            }
//        }
//        System.out.println(doubleState);
//
//    }

}
