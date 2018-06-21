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
 * statistical quality is excellent. Other than the nearly-identical algorithm used by {@link MizuchiRNG}, the closest
 * generator in terms of PractRand quality is {@link DervishRNG} with 1 anomaly, and this is much faster. As mentioned
 * earlier, this is the fastest high-quality generator here (tied with MizuchiRNG) other than {@link ThrustAltRNG}.
 * Unlike ThrustAltRNG, this can produce all long values as output; ThrustAltRNG bunches some outputs and makes
 * producing them more likely while others can't be produced at all. Notably, this generator is faster than
 * {@link LightRNG} while keeping the same or higher quality, and also faster than {@link XoRoRNG} while passing tests
 * that XoRoRNG always or frequently fails, such as binary matrix rank tests.
 * <br>
 * This generator is a StatefulRandomness but not a SkippingRandomness, so it can't (efficiently) have the skip() method
 * that LightRNG has. A method could be written to run the generator's state backwards, though, as well as to get the
 * state from an output of {@link #nextLong()}. {@link MizuchiRNG} uses the same algorithm except for the number added
 * in the LCG state update; here this number is always 1, but in MizuchiRNG it can be any odd long. This means that any
 * given MizuchiRNG object has two long values stored in it instead of the one here, but it allows two MizuchiRNG
 * objects with different streams to produce different, probably-not-correlated sequences of results, even with the same
 * seed. This property may be useful for cases where an adversary is trying to predict results in some way, though using
 * different streams for this purpose isn't enough and should be coupled with truncation of a large part of output (see
 * PCG-Random's techniques for this).
 * <br>
 * The name comes from LINear congruential generator this uses to change it state, while the rest is a NORMal
 * SplitMix64-like generator. "Linnorm" is a Norwegian name for a kind of dragon, as well. 
 * <br>
 * Written May 19, 2018 by Tommy Ettinger. Thanks to M.E. O'Neill for her insights into the family of generators both
 * this and her PCG-Random fall into, and to the team that worked on SplitMix64 for SplittableRandom in JDK 8. Chris
 * Doty-Humphrey's work on PractRand has been invaluable, and the LCG multiplier this uses is the same one used by
 * PractRand in its "varqual" LCGs (the other, longer multiplier is from PCG-Random, and that's both the
 * nothing-up-my-sleeve numbers used here). Thanks also to Sebastiano Vigna and David Blackwell for creating the
 * incredibly fast xoroshiro128+ generator and also very fast <a href="http://xoshiro.di.unimi.it/hwd.php">HWD tool</a>;
 * the former inspired me to make my code even faster and the latter tool seems useful so far in proving the quality of
 * the generator (LinnormRNG passes over 100TB of HWD, and probably would pass much more if I gave it more days to run).
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
        long rand = (state = state * 0x41C64E6DL + 1L);
        rand = (rand ^ rand >>> 27) * 0xAEF17502108EF2D9L;
        rand ^= rand >>> 25;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        long t = rand * boundLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>>= 32;
        return rand * bound + t + (tLow + randLow * bound >> 32) - (z >> 63) - (bound >> 63);
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

    
    
//    public final long nextLong(final long bound) {
//        if (bound <= 0) return 0;
//        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
//        for (; ; ) {
//            long bits = nextLong() & 0x7fffffffffffffffL;
//            if (bits >= threshold)
//                return bits % bound;
//        }
//    }
    public final long nextLongOld(final long bound) {
        if (bound <= 0) return 0;
        long bits, value, z;
        do {
            z = (state = state * 0x41C64E6DL + 1L);
            z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
            bits = (z ^ z >>> 25) & 0x7fffffffffffffffL;
            value = bits % bound;
        }while(bits - value + bound <= 0);
        return value;
    }
    public final long nextLongOther(final long bound) {
        if (bound <= 0) return 0;
        final long threshold = (0x8000000000000000L - bound) % bound;
        long z, bits;
        do {
            z = (state = state * 0x41C64E6DL + 1L);
            z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
            bits = (z ^ z >>> 25) & 0x7fffffffffffffffL;
        }while (bits < threshold);
        return bits % bound;
    }
    public final long nextLongOriginal(final long bound) {
        if (bound <= 0) return 0;
        for (;;) {
            long z = (state = state * 0x41C64E6DL + 1L);
            z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
            final long bits = (z ^ z >>> 25) & 0x7fffffffffffffffL;
            final long value = bits % bound;
            if (bits - value + bound > 0) return value;
        }
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
        final long z = (state = state * 0x41C64E6DL + 1L);
        return ((z ^ z >>> 27) * 0xAEF17502108EF2D9L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        final long z = (state = state * 0x41C64E6DL + 1L);
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

    /**
     * Static randomizing method that takes its state as a parameter; state is expected to change between calls to this.
     * It is recommended that you use {@code LinnormRNG.determine(++state)} or {@code LinnormRNG.determine(--state)} to
     * produce a sequence of different numbers, but you can also use {@code LinnormRNG.determine(state += 12345L)} or
     * any odd-number increment. All longs are accepted by this method, and all longs can be produced; unlike several
     * other classes' determine() methods, passing 0 here does not return 0.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state)
    {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * Like {@link #determine(long)}, but assumes state has already been multiplied by {@code 0x632BE59BD9B4E019L} or
     * some other very-large and very-complex long (use other numbers at your own risk!). A common usage is to call
     * randomize() like {@code LinnormRNG.randomize(state += 0x632BE59BD9B4E019L)}, which acts like a multiplier applied
     * to an incrementing state variable. 0x632BE59BD9B4E019L is "Neely's number", a large prime that has been truncated
     * and bitwise-rotated and has good properties in other places.
     * @param state a long that should change between calls with {@code state += 0x632BE59BD9B4E019L}
     * @return
     */
    public static long randomize(long state)
    {
        return (state = ((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * Static randomizing method that takes its state as a parameter and limits output to an int between 0 (inclusive)
     * and bound (exclusive); state is expected to change between calls to this. It is recommended that you use
     * {@code LinnormRNG.determineBounded(++state, bound)} or {@code LinnormRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers, but you can also use
     * {@code LinnormRNG.determineBounded(state += 12345L, bound)} or any odd-number increment. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x632BE59BD9B4E019L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 32) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f; }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x632BE59BD9B4E019L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }

//    public static void main(String[] args){
//        //Oriole32RNG oriole = new Oriole32RNG(123, 456, 789);
//        LinnormRNG r = new LinnormRNG(123456789L);
//        for (int j = 0; j < 15; j++) {
//            for (long i = 0x100000000L + j; i <= 0x30000000FL; i += 0x100000000L) {
//                long limit = 4L;//oriole.nextInt();
//                long result = r.nextLong(limit);
//                System.out.printf("%016X %021d %016X %021d %b, ", result, result, limit, limit,Math.abs(limit) - Math.abs(result) >= 0 && (limit >> 63) == (result >> 63));
//            }
//            System.out.println();
//        }
//    }
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
