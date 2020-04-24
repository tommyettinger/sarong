package sarong.discouraged;

import sarong.StatefulRandomness;
import sarong.ThrustAltRNG;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that uses only 32-bit math when producing 32-bit numbers. This generator does as
 * well as you could hope for on statistical tests, considering it can only generate 2 to the 32 ints before repeating
 * the cycle. On <a href="http://pracrand.sourceforge.net/">PractRand</a>, this completes testing on 16GB of generated
 * ints (the amount of space all possible ints would use) without finding any failures. Some big-name number generators
 * sometimes fail PractRand tests at only 256 MB, so this is pretty good. Like ThrustAltRNG, this changes its state
 * with a steady fixed increment, and does non-invertible adjustments to the current state to randomize it, although
 * the changes here are necessarily more involved than those in ThrustAltRNG because there are less bits of state to use
 * to randomize output. The period on ThrustAlt32RNG is 2 to the 32. Unlike some generators (like
 * PermutedRNG), changing the seed even slightly generally produces completely different results, which applies
 * primarily to determine() but also the first number generated in a series of nextInt() calls.
 * <br>
 * This generator is meant to function the same on GWT as on desktop, server, or Android JREs, and the implementation of
 * ints on GWT is accounted for here. On GWT, ints are just JavaScript doubles, and can go beyond the typical range of
 * an int without overflowing but are locked back down into the 32-bit signed integer range when bitwise operations are
 * used. To make sure multiplication stays within the precise range for JavaScript doubles (with a maximum of 2 to the
 * 53), any multiplications are limited to at most 32 bit (signed) numbers times 21 bit (effectively unsigned) numbers.
 * This class is also super-sourced on GWT with an alternate implementation that replaces {@code foo += bar} with the
 * normally-pointless {@code foo = foo + bar | 0}; on GWR this enforces overflow wrapping to the int range, and similar
 * bitwise code is used elsewhere in the super-sourced version. This should be enough to ensure consistent behavior
 * across platforms.
 * <br>
 * Created by Tommy Ettinger on 2/13/2017.
 */
public final class ThrustAlt32RNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any int value.
     */
    public int state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public ThrustAlt32RNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public ThrustAlt32RNG(final int seed) {
        state = seed;
    }

    public ThrustAlt32RNG(final long seed) {
        state = (int)(seed ^ seed >>> 32);
    }
    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with the least significant 32 bits of a long.
     *
     * @param state any 32-bit int; though longs are accepted only the int part (least significant 32 bits) will be used
     */
    @Override
    public void setState(long state) {
        this.state = (int)state;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return an int containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
        final int a = (state += 0x62BD5);
        final int y = (a ^ a >>> 11 ^ a >>> 21) * (a | 0xFFE00001);
        return (y ^ y >>> 13 ^ y >>> 19) >>> (32 - bits);
    }
    public final int nextInt()
    {
        final int a = (state += 0x62BD5);
        final int y = (a ^ a >>> 11 ^ a >>> 21) * (a | 0xFFE00001);
        return y ^ y >>> 13 ^ y >>> 19;

//        final int z = (a ^ a >>> 13) * ((a & 0xFFFF8) ^ 0xCD7B5);
//        return (((z << 21) | (z >>> 11)) ^ (((z << 7) | (z >>> 25)) * 0x62BD5));
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        final int b = (state += 0xC57AA);
        final int a = (b - 0x62BD5);
        final int y = (a ^ a >>> 11 ^ a >>> 21) * (a | 0xFFE00001);
        final int z = (b ^ b >>> 11 ^ b >>> 21) * (b | 0xFFE00001);
        return (long) (y ^ y >>> 13 ^ y >>> 19) << 32
                | ((z ^ z >>> 13 ^ z >>> 19) & 0xFFFFFFFFL);
    }

    /**
     * Advances or rolls back the ThrustAltRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random int generated after skipping forward or backwards by {@code advance} numbers
     */
    public final int skip(final int advance) {
        final int a = (state += 0x62BD5 * advance);
        final int y = (a ^ a >>> 11 ^ a >>> 21) * (a | 0xFFE00001);
        return y ^ y >>> 13 ^ y >>> 19;
    }


    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public ThrustAlt32RNG copy() {
        return new ThrustAlt32RNG(state);
    }
    @Override
    public String toString() {
        return "ThrustAlt32RNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrustAlt32RNG thrustAlt32RNG = (ThrustAlt32RNG) o;

        return state == thrustAlt32RNG.state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    /**
     * Returns a random non-reversible alteration of state; if state is the same on two calls to this, this will return
     * the same number. This is expected to be called with some changing variable, e.g.
     * {@code determine(state = state + 1 | 0)}, where the increment for state should be odd but otherwise doesn't
     * really matter (the {@code | 0} is needed to force overflow to occur correctly on GWT; if you know you won't
     * target GWT you can use {@code determine(++state)} instead). This is designed to function properly with any
     * constant int increment, as long as it is odd. The period is 2 to the 32 if you increment or decrement by 1 (and
     * perform any bitwise operation, such as {@code | 0}, if you might target GWT). If you use this on GWT and don't
     * perform a bitwise operation on the new value for state, then the period will gradually shrink as precision is
     * lost by the JavaScript double that GWT will use for state as a Java int. If you know that state will start at 0
     * and you call with {@code determine(++state)}, then on GWT you may not have to worry at all until 2 to the 54
     * calls have been made, after which state may cease to have the precision to represent an increase by 1 when the
     * math inside this method is considered. The period will have been exhausted by that point anyway (over 2 million
     * times), and running that many calls would take weeks,
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(state = state + 1 | 0)} is recommended to go forwards or
     *              {@code determine(state = state - 1 | 0)} to generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static int determineInt(int state) {
        return (state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19;
    }
    
    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determineFloat(state = state + 1 | 0)}, where the {@code | 0} corrects for GWT's different overflow
     * behavior. This is designed to function properly with any constant int increment, as long as it is odd. The period
     * is 2 to the 32 if you increment or decrement by 1, but there are only 2 to the 30 possible floats between 0 and
     * 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineFloat(state = state + 1 | 0)} is recommended to go forwards or
     *              {@code determineFloat(state = state - 1 | 0)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(int state) {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFF) * 0x1p-24f;
    }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * This is expected to be called with a changing variable, e.g.
     * {@code determineBounded(state = state + 1 | 0, bound)}, where the {@code | 0} corrects for GWT's different
     * overflow behavior.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(state = state + 1 | 0, bound)} is recommended to go forwards or
     *              {@code determineBounded(state = state - 1 | 0, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(int state, final int bound)
    {
        return (int) ((((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL) * bound >> 32);
    }
}
