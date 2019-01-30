package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A very-high-quality StatefulRandomness that is not especially fast, but is designed to be robust against frequent
 * state changes, and is built around the strongest determine() method in this library. PelicanRNG is one-dimensionally
 * equidistributed across all 64-bit outputs, has 64 bits of state, natively outputs 64 bits at a time, and can be
 * inverted (it transforms its state with a bijection, and the state is a counter incremented by 1 each time). It is
 * mostly the work of Pelle Evensen, who discovered one of the seemingly-very-rare combinations of bitwise rotations
 * that allow not just a counter that adds 1 to work as a state change, but rotations and reversals of such a counter.
 * He found that the combination of rotations {@code n ^ rotateLeft(n, 39) ^ rotateLeft(n, 14)} and
 * {@code n ^ rotateLeft(n, 40) ^ rotateLeft(n, 15)}, with multiplications between and ended with {@code n ^ n >>> 28},
 * can pass very large amounts of PractRand testing on rotations and reversals of a {@code ++n} counter. The test
 * procedure he uses takes several days on my hardware, and is described <a href="https://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">here</a>.
 * The multipliers he found (through great perseverance) do very well, but one rotation has issues in PractRand at the
 * end of his tested period, considered "suspicious" at 1TB, though this doesn't mean it has failed or will fail soon.
 * The multipliers I found here (through sheer luck) are {@code 0xAEF17502108EF2D9L}, which was already used by
 * PCG-Random, and {@code 0xDB4F0B9175AE2165L}, which is 2 to the 64 divided by a generalization of the golden ratio
 * (specifically, the fourth in the sequence described as Harmonious Numbers <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">here under Generalizing the Golden Ratio</a>).
 * Using these two multipliers instead of the earlier (still good) multipliers used by Evensen (which are
 * {@code 0xA24BAED4963EE407L} and {@code 9FB21C651E98DF25L}) allows the test he recommends to pass for at least the 64
 * reversed rotations and 27 or more of the non-reversed rotations of a counter. Rotating by 19 without reversing has
 * one "suspicious" result early on that dissipates into no anomalies later on. This may turn out to be weaker than
 * Evensen's multipliers if flaws are found in the last 37 of 128 tests, but it also passes 32TB of PractRand normally.
 * @author Pelle Evensen
 * @author Tommy Ettinger
 */
public final class PelicanRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public PelicanRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public PelicanRNG(final long seed) {
        state = seed;
    }

    public PelicanRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return (int)((z ^ z >> 28) >>> (64 - bits));
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return (z ^ z >>> 28);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public PelicanRNG copy() {
        return new PelicanRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return (int)(z ^ z >>> 28);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return (int)((bound * ((z ^ z >>> 28) & 0xFFFFFFFFL)) >> 32);
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
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets at
     * most one random number, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        z ^= z >>> 28;
        final long randLow = z & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        z >>>= 32;
        bound >>= 32;
        final long carry = (randLow * boundLow >> 32);
        final long t = z * boundLow + carry;
        final long tLow = t & 0xFFFFFFFFL;
        return z * bound + (t >>> 32) + (tLow + randLow * bound >> 32) - (carry >> 63) - (bound >> 63);
    }
    /**
     * Inclusive inner, exclusive outer; lower and upper can be positive or negative and there's no requirement for one
     * to be greater than or less than the other.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, can be positive or negative
     * @return a random long that may be equal to lower and will otherwise be between lower and upper
     */
    public final long nextLong(final long lower, final long upper) {
        return lower + nextLong(upper - lower);
    }
    
    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        z = (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        return ((z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        long z = state++;
        z = (z ^ (z << 39 | z >>> 25) ^ (z << 14 | z >>> 50)) * 0xAEF17502108EF2D9L;
        return  (z ^ (z << 40 | z >>> 24) ^ (z << 15 | z >>> 49)) * 0xDB4F0B9175AE2165L < 0;
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
     * @param seed the seed to use for this PelicanRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this PelicanRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "PelicanRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((PelicanRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ state >>> 32);
    }

    /**
     * Static randomizing method that takes its state as a parameter; state is expected to change between calls to this.
     * It is recommended that you use {@code PelicanRNG.determine(++state)} or {@code PelicanRNG.determine(--state)} to
     * produce a sequence of different numbers, but you can also use {@code PelicanRNG.determine(state += 12345L)} or
     * any odd-number increment. All longs are accepted by this method, and all longs can be produced; unlike several
     * other classes' determine() methods, passing 0 here does not return 0.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state)
    {
        return ((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L) ^ state >>> 28);
    }
    
    /**
     * Static randomizing method that takes its state as a parameter and limits output to an int between 0 (inclusive)
     * and bound (exclusive); state is expected to change between calls to this. It is recommended that you use
     * {@code DiverRNG.determineBounded(++state, bound)} or {@code DiverRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers, but you can also use
     * {@code DiverRNG.determineBounded(state += 12345L, bound)} or any odd-number increment. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0xFFFFFFFFL)) >> 32);
    }
//    public static int determineBounded(long state, final int bound)
//    {
//        return (int)((bound * (((state = ((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xDB4F0B9175AE2165L) ^ state >>> 29) & 0x7FFFFFFFL)) >> 31);
//    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This should tolerate just about
     * any increment as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) {
        return (((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;
    }
//        return (
//            (((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;

//    public static float determineFloat(long state) {
//        return ((((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xDB4F0B9175AE2165L) >>> 40) * 0x1p-24f; 
//    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * should tolerate just about any increment, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * This was the same as {@link LinnormRNG#determineDouble(long)}, but was changed to a slightly-faster method
     * that also has the advantage of being much harder to accidentally disrupt the input sequence. With LinnormRNG's
     * version, some odd-number increments will affect the sequence badly, such as 0xCB2C135370DC7C29, and using such an
     * increment there would ruin the quality of the determine() calls. That's because 0xCB2C135370DC7C29 is the
     * modular multiplicative inverse of 0x632BE59BD9B4E019, which LinnormRNG.determine() multiplies the input by as its
     * first step. Incrementing by 0xCB2C135370DC7C29 and then multiplying by 0x632BE59BD9B4E019 is the same as
     * incrementing by 1 every time, which LinnormRNG can handle only up to about 16GB in PractRand tests before failing
     * in a hurry. The algorithm used by DiverRNG is much more robust to unusual inputs (as long as they are odd), using
     * PCG-Random's style of random xorshift both to the left (to adjust the input) and to the right (after a large
     * multiplication, to bring more-random bits down to the less-significant end). Like LinnormRNG, this determine()
     * method is reversible, though it isn't easy to do. The algorithm used here is unrelated to DiverRNG, LinnormRNG,
     * and LinnormRNG.determine(), and passes PractRand to at least 2TB with no anomalies (extremely similar versions
     * have passed to 16TB and 32TB with no anomalies as well).
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) {
        return (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
}
