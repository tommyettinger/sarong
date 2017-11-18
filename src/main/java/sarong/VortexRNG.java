package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that gives up some speed to increase its period substantially (it should be 2 to
 * the 128 instead of ThrustAltRNG's 2 to the 64). It switches between 8 different streams, all with fairly high quality
 * and no statistical failures with gjrand on 100GB of data, and makes the switch in a pseudo-random way after each 2 to
 * the 64 numbers it generates. This switch is just a selection of a different stream based on the top 3 bits (which are
 * the highest-period) of a linear congruential generator's result, and the LCG is only advanced once per 2 to the 64
 * generated numbers. This generator is faster than both XoRoRNG and LightRNG after it warms up, and may have comparable
 * quality to XoRoRNG while having a slightly-larger period (by one number). This also doesn't fail one test that
 * XoRoRNG fails by its nature, although that test, binary matrix rank, isn't usually a great indicator of quality.
 * Because this only differs from ThrustAltRNG after a very large amount of generated numbers, usually, many practical
 * tests may not ever reach a point where they would be able to detect a failure, even though it is very possible there
 * are detectable issues after 2 to the 64 generated numbers, even before 2 to the 128.
 * <br>
 * As a side note, because XoRoRNG and VortexRNG differ in period, but by the smallest amount possible, you could run
 * both generators simultaneously and they wouldn't cycle until 2 to the 256 minus 2 to the 128 numbers are produced.
 * The output from XOR-ing or summing the results of both generators might have better quality than one on its own.
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
    private long stream;

    private static final long[] increments = {
             0x6C8E9CF570932BD5L,  0x6C8E9CD570932BD5L,  0x6C8E9CD7D0932BD5L,  0x6C8E9CF5E0932BD5L,
            -0x6C8E9CF5E0932BD5L, -0x6C8E9CF570932BD5L, -0x6C8E9CD570932BD5L, -0x6C8E9CD7D0932BD5L
    };
    private long pick;
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
        pick = increments[0];
    }
    public VortexRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream;
        pick = increments[(int) (stream >>> 61)];
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

    public long getStream() {
        return stream;
    }

    public void setStream(long stream) {
        this.stream = stream;
        pick = increments[(int)(stream >>> 61)];
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
        long z = (state += pick);
        if(z == 0)
            pick = increments[(int) ((stream = stream * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 61)];
        z = (z ^ (z >>> 25)) * (z | 0xA529L);
        return (int)(z ^ (z >>> 22)) >>> (32 - bits);
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
        long z = (state += pick);
        if(z == 0)
            pick = increments[(int) ((stream = stream * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 61)];
        z = (z ^ (z >>> 25)) * (z | 0xA529L);
        return z ^ (z >>> 22);
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
        return (int) (31 * (state ^ (state >>> 32)) + (stream ^ (stream >>> 32)));
    }
    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by a large and
     * specifically-chosen number selected by {@code stream} within this method, so using a small increment won't be
     * much different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1 and keep stream the same. The value for stream can be any int, but only the 2 least-significant
     * bits are used; it's recommended but not required to only use the values 0, 1, 2, and 3 for stream.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @param stream an int that determines which of four streams to use; only the bottom 2 bits are used (using 0, 1, 2, or 3 here is recommended)
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state, final int stream) {
        return (state = ((state *= increments[stream & 3]) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22);
    }
    //for quick one-line pastes of how the algo can be used with "randomize(++state)"
    //public static long randomize(long state) { return (state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22); }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by a large
     * and specifically-chosen number selected by {@code stream} within this method, so using a small increment won't be
     * much different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1 and keep stream the same, but there are only 2 to the 30 possible floats between 0 and 1. The
     * value for stream can be any int, but only the 2 least-significant bits are used; it's recommended but not
     * required to only use the values 0, 1, 2, and 3 for stream.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @param stream an int that determines which of four streams to use; only the bottom 2 bits are used (using 0, 1, 2, or 3 here is recommended)
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state, final int stream) { return (((state = ((state *= increments[stream & 3]) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22)) & 0xFFFFFF) * 0x1p-24f; }


    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by a large and specifically-chosen number selected by {@code stream} within this method, so
     * using a small increment won't be much different from using a very large one, as long as it is odd. The period is
     * 2 to the 64 if you increment or decrement by 1 and keep stream the same, but there are only 2 to the 62 possible
     * doubles between 0 and 1. The value for stream can be any int, but only the 2 least-significant bits are used;
     * it's recommended but not required to only use the values 0, 1, 2, and 3 for stream.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @param stream an int that determines which of four streams to use; only the bottom 2 bits are used (using 0, 1, 2, or 3 here is recommended)
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state, final int stream) { return (((state = ((state *= increments[stream & 3]) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * The period is usually 2 to the 64 when you increment or decrement by 1, but some bounds may reduce the period (in
     * the extreme case, a bound of 1 would force only 0 to be generated, so that would make the period 1). You can
     * change the sequence of random numbers this draws from by using a different int for {@code stream}; only the 2
     * least-significant bits are used, so only using 0, 1, 2, and 3 as values for stream is recommended.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @param stream an int that determines which of four streams to use; only the bottom 2 bits are used (using 0, 1, 2, or 3 here is recommended)
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound, final int stream)
    {
        return (int)((bound * (
                ((state = ((state *= increments[stream & 3]) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22))
                        & 0xFFFFFFFFL)) >> 32);
    }

}
