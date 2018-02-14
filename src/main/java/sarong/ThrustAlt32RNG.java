package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that uses only 32-bit math when producing 32-bit numbers. This generator does as
 * well as you could hope for on statistical tests, considering it can only generate 2 to the 32 ints before repeating
 * the cycle. On <a href="http://pracrand.sourceforge.net/">PractRand</a>, this completes testing on 16GB of generated
 * ints (the amount of space all possible ints would use) without finding any failures. Some big-name number generators
 * sometimes fail PractRand tests at only 256 MB, so this is pretty good. Like ThrustRNG and LightRNG, this changes its
 * state with a steady fixed increment, and does cipher-like adjustments to the current state to randomize it, although
 * the changes here are necessarily more involved than those in ThrustAltRNG because there are less bits of state to use
 * to randomize output. The period on ThrustAlt32RNG is 2 to the 32. Unlike some generators (like
 * PermutedRNG), changing the seed even slightly generally produces completely different results, which applies
 * primarily to determine() but also the first number generated in a series of nextInt() calls.
 * <br>
 * This generator is meant to function the same on GWT as on desktop, server, or Android JREs, but this still needs to
 * be verified. Some changes may be made if they will allow GWT compatibility to be confirmed.
 * <br>
 * Created by Tommy Ettinger on 2/13/2017.
 */
public final class ThrustAlt32RNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 0L;
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
        final int a = (state += 0x70932BD5);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return ((((z << 7) | (z >>> 25)) - a) ^ (z >>> 13)) >>> (32 - bits);
    }
    public final int nextInt()
    {
        final int a = (state += 0x70932BD5);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (((z << 7) | (z >>> 25)) - a) ^ (z >>> 13);
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
        final int b = (state += 0xE12657AA);
        final int a = (b - 0x70932BD5);
        final int y = (a ^ a >>> 13) * ((a & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        final int z = (b ^ b >>> 13) * ((b & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (long) ((((y << 7) | (y >>> 25)) - a) ^ (y >>> 13)) << 32 | (((((z << 7) | (z >>> 25)) - b) ^ (z >>> 13)) & 0xFFFFFFFFL);
    }

    /**
     * Advances or rolls back the ThrustAltRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random int generated after skipping forward or backwards by {@code advance} numbers
     */
    public final int skip(int advance) {
        final int a = (state += 0x70932BD5 * advance);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (((z << 7) | (z >>> 25)) - a) ^ (z >>> 13);
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
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x70932BD5} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 32 if you increment or decrement by 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static int determine(int state) {
        final int z = ((state *= 0x70932BD5) ^ state >>> 13) * ((state & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (((z << 7) | (z >>> 25)) - state) ^ (z >>> 13);
    }
    //for quick one-line pastes of how the algo can be used with "randomize(++state)"
    //public static int randomize(int state) { final int z = ((state *= 0x70932BD5) ^ state >>> 13) * ((state & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05; return (((z << 7) | (z >>> 25)) - state) ^ (z >>> 13); }

    /**
     * Limited-use; when called with successive state values that differ by 0x70932BD5, this produces fairly
     * high-quality random 32-bit numbers. You should call this with
     * {@code ThrustAltRNG.randomize(state += 0x70932BD5)} to go forwards or
     * {@code ThrustAltRNG.randomize(state -= 0x70932BD5)} to go backwards in the sequence.
     * @param state must be changed between calls to get changing results;
     *              you should probably use {@code ThrustAltRNG.randomize(state += 0x70932BD5)}
     * @return a pseudo-random number generated from state
     */
    public static int randomize(int state) {
        final int z = (state ^ state >>> 13) * ((state & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (((z << 7) | (z >>> 25)) - state) ^ (z >>> 13);
    }
    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x70932BD5} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 32 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(int state) {
        final int z = ((state *= 0x70932BD5) ^ state >>> 13) * ((state & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (((((z << 7) | (z >>> 25)) - state) ^ (z >>> 13)) & 0xFFFFFF) * 0x1p-24f;
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
    public static int determineBounded(int state, final int bound)
    {
        final int z = ((state *= 0x70932BD5) ^ state >>> 13) * ((state & 0xFFFFFFF8) ^ 0x2C9277B5) + 0xAC564B05;
        return (int) (((((((z << 7) | (z >>> 25)) - state) ^ (z >>> 13)) & 0xFFFFFFFFL) * bound) >> 32);
    }
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/ThrustAltRNG > ../../thrustalt_asm.txt
//         */
//        long seed = 1L;
//        ThrustAltRNG rng = new ThrustAltRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        System.out.println(seed);
//    }

}
