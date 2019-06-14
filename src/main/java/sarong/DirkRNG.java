package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A generator built around making the most robust {@link #determine(long)} method possible, working innately on
 * sequential longs (incrementing by 1) instead of a Weyl sequence that increments by some huge constant.
 * Passes 32TB of PractRand with only one anomaly, "unusual" at 256GB, and passes TestU01's BigCrush in both normal and
 * reversed modes. Period is 2 to the 64, like LightRNG. All large constants here (not rotation
 * amounts) are obtained through generalized golden ratio formulas:
 * <ul>
 * <li>{@code 0x9E3779B97F4A7C15} is {@code x} = {@code x}<sup>{@code 2}</sup> - 1; {@code x} &times; 2<sup>64</sup></li>
 * <li>{@code 0xD1B54A32D192ED03} is {@code y} = {@code y}<sup>{@code 4}</sup> - 1; {@code y} &times; 2<sup>64</sup></li>
 * <li>{@code 0xDB4F0B9175AE2165} is {@code z} = {@code z}<sup>{@code 5}</sup> - 1; {@code z} &times; 2<sup>64</sup></li>
 * <li>{@code 0xE60E2B722B53AEEB} is {@code w} = {@code w}<sup>{@code 7}</sup> - 1; {@code w} &times; 2<sup>64</sup></li>
 * </ul>
 * Because this works on sequential numbers, its determine() methods appear to work equally well regardless of increment
 * between calls; there's no increment that breaks a pattern (like if you call {@link LightRNG#determine(long)} with
 * inputs separated by the modular multiplicative inverse of LightRNG's increment, it won't do well at all).
 * <br>
 * Created by Tommy Ettinger on 9/30/2018.
 */
public final class DirkRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public DirkRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DirkRNG(final long seed) {
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
        long z = (++state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L;
        z = (z ^ z >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL;
        return (int)(z ^ (z << 19 | z >>> 45) ^ (z << 46 | z >>> 18)) >>> (32 - bits);

        // previously used 0x8CB92BA72F3D8DD5L as second multiplier
//        long z = state;
//        z = (z ^ z >>> 26) * ((state += 0x9E3779B97F4A7C15L) ^ z);
//        return (int) (z ^ z >>> 28) >>> (32 - bits);
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
        long z = (++state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L;
        z = (z ^ z >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL;
        return z ^ (z << 19 | z >>> 45) ^ (z << 46 | z >>> 18);

//        long z = state;
//        z = (z ^ z >>> 26) * ((state += 0x9E3779B97F4A7C15L) ^ z);
//        return (z ^ z >>> 28);
//        long z = state;
//        z *= (z ^ (state += 0x9E3779B97F4A7C15L));
//        return (z - (z << 23 | z >>> 41));
    }

    /**
     * Advances or rolls back the DirkRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        long z = ((state += advance) ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L;
        z = (z ^ z >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL;
        return z ^ (z << 19 | z >>> 45) ^ (z << 46 | z >>> 18);

//        final long s = (state += 0x9E3779B97F4A7C15L * advance);
//        long z = s - 0x9E3779B97F4A7C15L;
//        z = (z ^ z >>> 26) * (s ^ z);
//        return (z ^ z >>> 28);
    }


    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DirkRNG copy() {
        return new DirkRNG(state);
    }
    @Override
    public String toString() {
        return "DirkRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirkRNG dirkRNG = (DirkRNG) o;

        return state == dirkRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Returns a reversible, deterministic adjustment of state; if state is the same on two calls to this, this will
     * return the same number. This is expected to be called with some changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random alteration of state
     */
    public static long determine(long state) {
        return ((state = ((state = (state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL) ^ (state << 19 | state >>> 45) ^ (state << 46 | state >>> 18));
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return (((state = ((state = (state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL) ^ (state << 19 | state >>> 45) ^ (state << 46 | state >>> 18)) & 0xFFFFFF) * 0x1p-24f; }
    
    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state = (state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL) ^ (state << 19 | state >>> 45) ^ (state << 46 | state >>> 18)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }

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
        return (int)((bound * (
                ((state = ((state = (state ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xE60E2B722B53AEEBL) ^ (state << 19 | state >>> 45) ^ (state << 46 | state >>> 18))
                        & 0xFFFFFFFFL)) >> 32);
    }
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/DirkRNG > ../../dirk_asm.txt
//         */
//        long seed = 1L;
//        DirkRNG rng = new DirkRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        System.out.println(seed);
//    }

}
