package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A high-quality StatefulRandomness that is tied for the fastest 64-bit generator in this library that passes
 * statistical tests tests and is equidistributed. Has 64 bits of state and natively outputs 64 bits at a time, changing
 * the state with a variant on a linear congruential generator (an "XLCG" that XORs then multiplies the state with each
 * step; it is {@code state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL}). Starting with that XLCG's output, it
 * xorshifts that output, adds a very large negative long, then returns another xorshift. For whatever reason, the
 * output of this simple function passes all 32TB of PractRand, meaning its statistical quality is excellent. This
 * algorithm is almost identical to {@link LinnormRNG} and also to {@link MizuchiRNG} (in speed and quality), but uses
 * an XLCG instead of an LCG for the state and adds rather than multiplies the same large negative long. As mentioned
 * earlier, this is the fastest high-quality generator here (tied with LinnormRNG and MizuchiRNG) other than
 * {@link ThrustAltRNG}. Unlike ThrustAltRNG, this can produce all long values as output; ThrustAltRNG bunches some
 * outputs and makes producing them more likely while others can't be produced at all. Notably, this generator is faster
 * than {@link LightRNG} while keeping the same or higher quality, and also faster than {@link XoRoRNG} while passing
 * tests that XoRoRNG always or frequently fails, such as binary matrix rank tests.
 * <br>
 * This generator is a StatefulRandomness but not a SkippingRandomness, so it can't (efficiently) have the skip() method
 * that LightRNG has. A method could be written to run the generator's state backwards, though, as well as to get the
 * state from an output of {@link #nextLong()}.
 * <br>
 * The name comes from its Quick speed, X for XLCG and XorShift operations used, and the literary reference to Don
 * Quixote because I felt like trying to improve LinnormRNG was like jousting at windmills (before I found this one). 
 * <br>
 * Regarding constants used here (magic numbers): The number that is XORed with state was tinkered with manually to
 * avoid long runs of 0 bits, mimicking the pattern of numbers used in Weyl sequence increments; it can probably be
 * changed if it stays 63 bits long, though it must have the last 3 bits be {@code 101}, meaning the last hex digit must
 * be 5 or B, because of how an XLCG works. The single multiplier is an LCG multiplier used in PractRand, minus 2 to fit
 * the XLCG requirement for the last 3 bits to be {@code 011}. The single addend is a multiplier from PCG-Random; it may
 * also be changeable without affecting quality, as long as it is odd and 64 bits long.
 * <br>
 * Written June 19, 2018 by Tommy Ettinger. Thanks to M.E. O'Neill for her insights into the family of generators both
 * this and her PCG-Random fall into, and to the team that worked on SplitMix64 for SplittableRandom in JDK 8. Chris
 * Doty-Humphrey's work on PractRand has been invaluable, as has his rediscovery of the XLCG technique from somewhere on
 * Usenet. Thanks also to Sebastiano Vigna and David Blackwell for creating the incredibly fast xoroshiro128+ generator
 * and also very fast <a href="http://xoshiro.di.unimi.it/hwd.php">HWD tool</a>; the former inspired me to make my code
 * even faster and the latter tool seems useful so far in proving the quality of the generator.
 * <br>
 * <em>NOTE:</em> This generator is still being adapted and improvements may be found to its speed or to other aspects
 * of how it works (it lacks a determine() method, for instance). These should be expected to change the output of a
 * QuixoticRNG across versions/commits until it stabilizes.
 * @author Tommy Ettinger
 */
public final class QuixoticRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public QuixoticRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public QuixoticRNG(final long seed) {
        state = seed;
    }

    public QuixoticRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
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
    public QuixoticRNG copy() {
        return new QuixoticRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
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
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
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
        long rand = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        rand = (rand ^ rand >>> 27) + 0xAEF17502108EF2D9L;
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
    
    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
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
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        z = (z ^ z >>> 27) + 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL);
        return ((z ^ z >>> 27) + 0xAEF17502108EF2D9L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        return (state = (state ^ 0x6C8E9CF570932BD5L) * 0x41C64E6BL) < 0;
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
        return "QuixoticRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((QuixoticRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
    
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
