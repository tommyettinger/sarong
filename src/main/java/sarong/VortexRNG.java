package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that gives up more speed to gain multiple streams, much like SplittableRandom can
 * in the JDK. There can be all sorts of potential issues with the quality of multi-stream generators, as M.E. O'Neill
 * found <a href="http://www.pcg-random.org/posts/critiquing-pcg-streams.html">on her blog about random numbers</a>.
 * I'm trying to do things differently from SplitMix's "any odd-number increment" approach or the more measured approach
 * taken by SplittableRandom, and as O'Neill found, a linear congruential generator step has the potential to be very
 * valuable (compared to sequential increments, AKA a Weyl sequence) if followed by more rigorous adjustments. This
 * generator is still being evaluated for general quality, though at least some streams seem very good. There may be
 * some kind of known-good stream listing that this will choose seeds from in the future.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class VortexRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Which stream this VortexRNG will generate numbers with; each stream is effectively a completely different
     * algorithm, and may produce specific numbers more or less frequently, and should always produce them in a
     * different order. This can be changed after construction but not with any guarantees of quality staying the same
     * relative to previously-generated numbers on a different stream.
     */
    public long stream;
    /**
     * Creates a new generator seeded using Math.random.
     */
    public VortexRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public VortexRNG(long seed)
    {
        state = seed;
        stream = 0;
    }
    public VortexRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream;
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
     * @param state a 64-bit long
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
    public final int next(final int bits) {
        final long s = (state += 0x6C8E9CF570932BD5L);
        long z = (s ^ stream) * 0x5851F42D4C957F2DL + 0xC83E52FE9D9EEC75L;
        z = (z ^ (z >>> 25)) * (z | 0xA529L);
        return (int)(z ^ (z >>> 22) ^ s ^ (s >>> 30)) >>> (32 - bits);
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
        final long s = (state += 0x6C8E9CF570932BD5L);
        long z = (s ^ stream) * 0x5851F42D4C957F2DL + 0xC83E52FE9D9EEC75L;
        z = (z ^ (z >>> 25)) * (z | 0xA529L);
        return z ^ (z >>> 22) ^ s ^ (s >>> 30);
    }

    /**
     * Advances or rolls back the VortexRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    public final long skip(long advance) {
        final long s = (state += 0x6C8E9CF570932BD5L * advance);
        long z = (s ^ stream) * 0x5851F42D4C957F2DL + 0xC83E52FE9D9EEC75L;
        z = (z ^ (z >>> 25)) * (z | 0xA529L);
        return z ^ (z >>> 22) ^ s ^ (s >>> 30);
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
        return new VortexRNG(state, stream);
    }
    @Override
    public String toString() {
        return "VortexRNG on stream 0x" + StringKit.hex(stream) + "L with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VortexRNG vortexRNG = (VortexRNG) o;

        return state == vortexRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state) {
        final long z = (((state *= 0x6C8E9CF570932BD5L) >>> 25) ^ state) * (state | 0xA529L);
        return z ^ (z >>> 22) ^ state ^ (state >>> 30);
    }
    //for quick one-line pastes of how the algo can be used with "randomize(++state)"
    //public static long randomize(long state) { return (state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22); }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) {
        final long z = (((state *= 0x6C8E9CF570932BD5L) >>> 25) ^ state) * (state | 0xA529L);
        return ((z ^ (z >>> 22) ^ state ^ (state >>> 30)) & 0xFFFFFF) * 0x1p-24f;
    }


    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) {
        final long z = (((state *= 0x6C8E9CF570932BD5L) >>> 25) ^ state) * (state | 0xA529L);
        return ((z ^ (z >>> 22) ^ state ^ (state >>> 30)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * The period is usually 2 to the 64 when you increment or decrement by 1, but some bounds may reduce the period (in
     * the extreme case, a bound of 1 would force only 0 to be generated, so that would make the period 1).
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        final long z = (((state *= 0x6C8E9CF570932BD5L) >>> 25) ^ state) * (state | 0xA529L);
        return (int)((bound * (
                (z ^ (z >>> 22) ^ state ^ (state >>> 30))
                        & 0xFFFFFFFFL)) >> 32);
    }
}
