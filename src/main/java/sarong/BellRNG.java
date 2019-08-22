package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A new and different RNG type that uses two 64-bit states, with one a 64-bit Galois LFSR and another that updates by
 * adding a large constant and the result of the LFSR to its current value. It runs the value of this erratic-updating
 * second state through a bare-bones unary hash (a xorshift, a multiply, and a xorshift, which is normally not enough)
 * and returns it. This does well in PractRand testing, getting only one "unusual" anomaly at 4TB. It has a large period
 * of {@code pow(2, 128) - pow(2, 64)}, and importantly is 1-dimensionally equidistributed (every result occurs exactly
 * {@code pow(2, 64) - 1} times, which is technically better than xoroshiro128+ because that generator, despite
 * identical state size and a longer period by {@code pow(2, 64) - 1}, doesn't produce 0 as often as it does other
 * results). Getting ordered pairs of results, every result will be followed by every other result except one, which
 * means this falls just shy of being 2-dimensionally equidistributed.
 * <br>
 * The name comes from Dodgers baseball star Cody Bellinger, who also came from a middling earlier set of
 * accomplishments (like a simple Galois LFSR), figured out how to improve, and became something great all-around.
 * I had also just written TroutRNG (keeping with the aquatic animal theme), and Cody Bellinger had just surpassed Mike
 * Trout's home run count so far for the season.
 * <br>
 * Created by Tommy Ettinger on 8/21/2019.
 */
public final class BellRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any long value.
     */
    private long stateA;

    /**
     * Must be non-zero.
     */
    private long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public BellRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public BellRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25);
        if(stateB == 0L)
            stateB = 1L;
    }

    public BellRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB == 0L ? 1L : seedB;
    }

    /**
     * Get the "A" part of the internal state as a long.
     *
     * @return the current internal "A" state of this object.
     */
    public long getStateA() {
        return stateA;
    }

    /**
     * Set the "A" part of the internal state with a long.
     *
     * @param stateA a 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    /**
     * Get the "B" part of the internal state as a long.
     *
     * @return the current internal "B" state of this object.
     */
    public long getStateB() {
        return stateB;
    }

    /**
     * Set the "B" part of the internal state with a long; if given 0, this will ignore it and use 1 instead.
     *
     * @param stateB a 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB == 0L ? 1L : stateB;
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
        long s = (stateA += (stateB = (stateB >>> 1 ^ (-(stateB & 1L) & 0xD800000000000000L))) + 0x9E3779B97F4A7C15L);
        s = (s ^ s >>> 30) * 0x369DEA0F31A53F85L;
        return (int)(s ^ s >>> 28) >>> (32 - bits);
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
        long s = (stateA += (stateB = (stateB >>> 1 ^ (-(stateB & 1L) & 0xD800000000000000L))) + 0x9E3779B97F4A7C15L);
        s = (s ^ s >>> 30) * 0x369DEA0F31A53F85L;
        return s ^ s >>> 28;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public BellRNG copy() {
        return new BellRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "BellRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BellRNG bellRNG = (BellRNG) o;

        return stateA == bellRNG.stateA && stateB == bellRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }
}
