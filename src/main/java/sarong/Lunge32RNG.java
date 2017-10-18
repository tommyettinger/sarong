package sarong;

import sarong.util.StringKit;

/**
 * Work-in-progress.
 * <br>
 * Created by Tommy Ettinger on 9/24/2017.
 */
public class Lunge32RNG implements StatefulRandomness {
    /**
     * Can be any int value.
     */
    public int state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public Lunge32RNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public Lunge32RNG(final int seed) {
        state = seed;
    }

    public Lunge32RNG(final long seed) {
        state = (int) (seed ^ seed >>> 32);
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
        this.state = (int) state;
    }

    public final int nextInt()
    {
//        return (state += (state >> 13) + 0x5F356495) * 0x2C9277B5;
        int z = (state += 0x7F4A7C15);
        z = (z ^ z >>> 14) * (0x2C9277B5 + (z * 0x632BE5A6));
        return (z ^ z >>> 14) * 0x5F356495;
//        int z = (state += 0x7F4A7C15);
//        z = (z ^ z >>> 14) * (z ^ z + 0x2C9277B5);
//        return (z ^ z >>> 13);
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
//        return (state += state ^ ((state >>> (state & 7) + 7) + 0x2C9277B5) * 0x5F356495) >>> (32 - bits);
        //return (state ^ (state += ((state >>> 13) + 0x5F356495) * 0x2C9277B5)) >>> (32 - bits);
        int z = (state += 0x7F4A7C15);
        z = (z ^ z >>> 14) * (0x2C9277B5 + (z * 0x632BE5A6));
        return (z ^ z >>> 14) * 0x5F356495 >>> (32 - bits);
//        int z = (state += 0x7F4A7C15);
//        z = (z ^ z >>> 14) * (z ^ z + 0x2C9277B5);
//        return (z ^ z >>> 13) >>> (32 - bits);
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
//        return (state += (state >> 13) + 0x5F356495) * 0x2C9277B500000000L ^
//                (state += (state >> 13) + 0x5F356495) * 0x2C9277B5;
//        int x = state + 0x7F4A7C15, y = (state += 0xFE94F82A);
//        x = (x ^ x >>> 14) * (x ^ x + 0x2C9277B5);
//        y = (y ^ y >>> 14) * (y ^ y + 0x2C9277B5);
//        return (long) (x ^ x >>> 13) << 32 ^ (y ^ y >>> 13);

        int x = state + 0x7F4A7C15, y = (state += 0xFE94F82A);
        //0x5F356495
        x = (x ^ x >>> 14) * (0x2C9277B5 + (x * 0x632BE5A6));
        y = (y ^ y >>> 14) * (0x2C9277B5 + (y * 0x632BE5A6));
        return (x ^ x >>> 14) * 0x5F35649500000000L ^ (y ^ y >>> 14) * 0x5F356495;
        // * 0x27BB2EE687B0B0FDL;
        //return ((state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> 28));

        //return (state = state * 0x59A2B8F555F5828FL % 0x7FFFFFFFFFFFFFE7L) ^ state << 2;
        //return (state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL);
        //return (state ^ (state += 0x2545F4914F6CDD1DL)) * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL;
        //return (state * 0x5851F42D4C957F2DL) + ((state += 0x14057B7EF767814FL) >> 28);
        //return (((state += 0x14057B7EF767814FL) >>> 28) * 0x5851F42D4C957F2DL + (state >>> 1));
    }

    /**
     * Advances or rolls back the Lunge32RNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    public final int skip(int advance) {
//        int z = (state += 0x7F4A7C15 * advance);
//        z = (z ^ z >>> 14) * (0x2C9277B5 + (z * 0x632BE5A6));
//        return (z ^ z >>> 13);
        int z = (state += 0x7F4A7C15 * advance);
        z = (z ^ z >>> 14) * (0x2C9277B5 + (z * 0x632BE5A6));
        return (z ^ z >>> 14) * 0x5F356495;
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
        return new Lunge32RNG(state);
    }
    @Override
    public String toString() {
        return "Lunge32RNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lunge32RNG lungeRNG = (Lunge32RNG) o;

        return state == lungeRNG.state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x7F4A7C15} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static int determine(int state)
    {
        state = ((state *= 0x7F4A7C15) ^ state >>> 14) * (0x2C9277B5 + (state * 0x632BE5A6));
        return state ^ state >>> 12;
    }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(int state, final int bound)
    {
        state = ((state *= 0x7F4A7C15) ^ state >>> 14) * (0x2C9277B5 + (state * 0x632BE5A6));
        return (int)((bound * ((state ^ state >>> 14) * 0x5F356495 & 0x7FFFFFFFL)) >> 31);
    }

}