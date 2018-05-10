package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A very-high-quality StatefulRandomness that is the fastest 64-bit generator in this library that passes statistical
 * tests and is equidistributed. Has 64 bits of state and natively outputs 64 bits at a time, changing the state with a
 * basic linear congruential generator (it is simply {@code state = state * 1103515245 + 1}). Starting with that LCG's
 * output, it xorshifts that output, multiplies by a very large negative long, then returns another xorshift. For
 * whatever reason, the output of this simple function passes all 32TB of PractRand with no anomalies, meaning its
 * statistical quality is excellent. The closest generator in terms of PractRand quality is {@link DervishRNG} with 1
 * anomaly, and this is much faster. As mentioned earlier, this is the fastest high-quality generator here other than
 * {@link ThrustAltRNG}. Unlike ThrustAltRNG, this can produce all long values as output; ThrustAltRNG bunches some
 * outputs and makes producing them more likely while others can't be produced at all. Notably, this generator is faster
 * than {@link LightRNG} while keeping the same or higher quality, and also faster than {@link XoRoRNG} while passing
 * tests that XoRoRNG always or frequently fails, such as binary matrix rank tests. This generator is a
 * StatefulRandomness but not a SkippingRandomness, so it can't use (efficiently, anyway) the skip() method that
 * LightRNG has. A method could be written to run the generator's state backwards, though.
 * <br>
 * The name comes from LINear congruential generator this uses to change it state, while the rest is a NORMal
 * SplitMix64-like generator. "Linnorm" is a Norwegian name for a kind of dragon, as well. 
 * <br>
 * Written May 9, 2018 by Tommy Ettinger.
 * @author Tommy Ettinger
 */
public final class LinnormRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public LinnormRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public LinnormRNG(final long seed) {
        state = seed;
    }

    public LinnormRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (z ^ z >>> 25);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public LinnormRNG copy() {
        return new LinnormRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)((bound * ((z ^ z >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Inclusive inner, exclusive outer.
     *
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative, usually greater than inner
     * @return a random int between inner (inclusive) and outer (exclusive)
     */
    public final int nextInt(final int inner, final int outer) {
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
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        long z = (state = state * 0x41C64E6DL + 1L);
        return ((z ^ z >>> 27) * 0xAEF17502108EF2D9L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        long z = (state = state * 0x41C64E6DL + 1L);
        return ((z ^ z >>> 27) * 0xAEF17502108EF2D9L) < 0;
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

    @Override
    public String toString() {
        return "LinnormRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((LinnormRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

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
//        LinnormRNG rng = new LinnormRNG(1L);
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
