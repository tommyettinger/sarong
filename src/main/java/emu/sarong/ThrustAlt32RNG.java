package sarong;
// This differs from the non-super-sourced version only by including " | 0" after any potentially-overflowing math.
// That's actually relevant on GWT, due to ints simply being JS doubles on that platform, and those don't overflow.
import sarong.util.StringKit;

import java.io.Serializable;

public final class ThrustAlt32RNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any int value.
     */
    public int state;

    public ThrustAlt32RNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public ThrustAlt32RNG(final int seed) {
        state = seed;
    }

    public ThrustAlt32RNG(final long seed) {
        state = (int)(seed ^ seed >>> 32);
    }

    @Override
    public long getState() {
        return state;
    }

    @Override
    public void setState(long state) {
        this.state = (int)state;
    }

    @Override
    public final int next(final int bits) {
        final int a = (state = state + 0x62BD5 | 0);
        final int y = (a ^ a >>> 11 ^ a >>> 21) * (a | 0xFFE00001);
        return (y ^ y >>> 13 ^ y >>> 19) >>> (32 - bits);
    }
    public final int nextInt()
    {
        final int a = (state = state + 0x62BD5 | 0);
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
        final int b = (state = state + 0xC57AA | 0);
        final int a = (b - 0x62BD5 | 0);
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
        final int a = (state = state + 0x62BD5 * advance | 0);
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
    public sarong.ThrustAlt32RNG copy() {
        return new sarong.ThrustAlt32RNG(state);
    }
    @Override
    public String toString() {
        return "ThrustAlt32RNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        sarong.ThrustAlt32RNG thrustAlt32RNG = (sarong.ThrustAlt32RNG) o;

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
        return (int) ((((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL) * bound) >> 32;
    }
}
