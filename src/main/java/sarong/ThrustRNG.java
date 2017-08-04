package sarong;

import sarong.util.StringKit;

/**
 * An odd tweak on a linear congruential generator that passes PractRand with no failures (just one "unusual" test),
 * allows all longs as states (including 0), and also implements StatefulRandomness. LightRNG does those same things,
 * but ThrustRNG is slightly slower than LightRNG. You may want ThrustRNG when you need to use its different algorithm
 * to seed another RNG or do something else where reusing an algorithm would be troublesome.
 * Created by Tommy Ettinger on 8/3/2017.
 */
public class ThrustRNG implements StatefulRandomness {
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public ThrustRNG() {
        this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    public ThrustRNG(final long seed) {
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
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
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
        return (int)(((state = state * 0x8329C6EB9E6AD3E3L + 0x632BE59BD9B4E019L) + (state >> 28)) >>> (64 - bits));
        //return (int)((state += ((state * state) >>> 28) * 0x8329C6EB9E6AD3E3L + 0x632BE59BD9B4E019L) >>> (64 - bits));
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
        return ((state = state * 0x8329C6EB9E6AD3E3L + 0x632BE59BD9B4E019L) + (state >> 28));
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
        return new ThrustRNG(state);
    }
    @Override
    public String toString() {
        return "ThrustRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrustRNG thrustRNG = (ThrustRNG) o;

        return state == thrustRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

}
