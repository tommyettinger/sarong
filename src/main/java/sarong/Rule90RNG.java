package sarong;

import sarong.util.StringKit;

/**
 * Experimental StatefulRandomness relating to the elementary cellular automaton called "Rule 90."
 * <br>
 * Created by Tommy Ettinger on 10/1/2017.
 */
public class Rule90RNG implements StatefulRandomness {
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public Rule90RNG() {
        this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    public Rule90RNG(final long seed) {
        state = seed;
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
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You may want to avoid passing 0 for compatibility, though this implementation can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = state;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(int bits) {
        return (int)((state ^ ((state = (state >>> 1 ^ state << 1) + 0x27BB2EE687B0B0FDL) >>> 24) * 0x5851F42D4C957F2DL) >>> (64 - bits));
        //return (int)(((state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> 28)) >>> (64 - bits));
        //(state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> 28)

        //(state *= 0x2545F4914F6CDD1DL) + (state >> 28)
        //((state += 0x2545F4914F6CDD1DL) ^ (state >>> 30 & state >> 27) * 0xBF58476D1CE4E5B9L)
        //(state ^ (state += 0x2545F4914F6CDD1DL)) * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL
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
        //final long z = (state += 36277L);
        //return (z ^ ((z >>> 1 ^ z << 1) + 0x14057B7EF767814FL >>> 24) * 0x5851F42D4C957F2DL);
        //return state += determine(state * (1000000L | 277L));
        //use this next one as a known good version:
        return (state ^ ((state = (state >>> 1 ^ state << 1) + 0x27BB2EE687B0B0FDL) >>> 24) * 0x5851F42D4C957F2DL);
        //final long z = (state + 0x9E3779B97F4A7C15L);
        //return (state ^= (0x27BB2EE687B0B0FDL ^ z >>> 14) + (z ^ z >>> 24) * 0x5851F42D4C957F2DL);
        // 0x27BB2EE687B0B0FDL L'Ecuyer
        //return ((state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> 28));

        //return (state = state * 0x59A2B8F555F5828FL % 0x7FFFFFFFFFFFFFE7L) ^ state << 2;
        //return (state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL);
        //return (state ^ (state += 0x2545F4914F6CDD1DL)) * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL;
        //return (state * 0x5851F42D4C957F2DL) + ((state += 0x14057B7EF767814FL) >> 28);
        //return (((state += 0x14057B7EF767814FL) >>> 24) * 0x5851F42D4C957F2DL + (state >>> 1));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new Rule90RNG(state);
    }
    @Override
    public String toString() {
        return "Rule90RNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule90RNG rule90RNG = (Rule90RNG) o;

        return state == rule90RNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, but since this type of randomness isn't as
     * statistically strong as ThrustRNG or especially LightRNG, this needs a fairly specific kind of update that isn't
     * as fast as calling {@link #nextLong()} on a Rule90RNG: {@code (state += determine(state * 36277L))}, where state
     * is a long that changes with each call and 36277L can be any large-enough odd number (typically, you can choose
     * any number and bitwise-OR it with 277, such as {@code 1000000L | 277L}, and you will get a good multiplier).
     * If you use the result of that call, which is the same as the value of state after calling, then it will be a
     * rather-high-quality random number for most multipliers.
     * @param state a variable that should be different every time you want a different random result;
     *              using the result of {@code (state += determine(state * 36277L))} as the random number is recommended
     *              and allows many large odd numbers in place of 36277L
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state) { return (state ^ ((state >>> 1 ^ state << 1) + 0x14057B7EF767814FL >>> 24) * 0x5851F42D4C957F2DL); } // call with (state += determine(state * 36277L))

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * This is expected to be called with some changing variable, but since this type of randomness isn't as
     * statistically strong as ThrustRNG or especially LightRNG, this needs a fairly specific kind of update that isn't
     * as fast as calling {@link #nextLong()} on a Rule90RNG:
     * {@code result = determineBounded(state * 36277L, bound)); state += result;}, where result stores the int between
     * 0 and bound, state is a long that changes with each call, bound is the outer limit on the returned int and 36277L
     * can be any large-enough odd number (typically, you can choose any number and bitwise-OR it with 277, such as
     * {@code 1000000L | 277L}, and you will get a good  multiplier). You may need to change {@code state += result;} to
     * {@code state += result + 0x27BB2EE687B0B0FDL} or some other large increment to make the state change enough if
     * bound is small.
     * @param state a variable that should be different every time you want a different random result; using
     *              {@code result = determineBounded(state * 36277L, bound)); state += result + 0x27BB2EE687B0B0FDL;} is
     *              recommended if you use result as the desired int (36277 can be changed to other odd numbers).
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (
                (state ^ ((state >>> 1 ^ state << 1) + 0x14057B7EF767814FL >>> 24) * 0x5851F42D4C957F2DL)
                        & 0x7FFFFFFFL)) >> 31);
    }

}
