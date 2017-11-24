package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A modified 64-bit linear congruential generator that allows 2 to the 63 possible streams (any odd long).
 * Its period is 2 to the 64, but you can change the stream after some large amount of generated numbers if you want to
 * effectively extend the period. It is currently rather slow, but this seems to be due to the JVM mis-optimizing (or
 * pessimizing, to use another term) the rotation code this uses, or some other section. This is likely to change to try
 * to improve performance. With a stream of 1, the quality this has is exceptional; other streams of course can't all be
 * tested, so they may have some minor issues.
 * <br>
 * Changed from an earlier version that switched between 8 streams, using an algorithm based on ThrustAltRNG, because
 * that version had some strange quality issues that ThrustAltRNG does not have on its own.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class VortexRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 2L;
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
        stream = 1;
    }
    public VortexRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream | 1L;
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
        this.stream = stream | 1L;
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
        final long z = (state = state * 0x369DEA0F31A53F85L + stream);
        return (int)(
                ((z << 23) | (z >>> 41)) - ((z << 19) | (z >>> 45)) - ((z >>> 27) ^ z)
                >>> (64 - bits));
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
        final long z = (state = state * 0x369DEA0F31A53F85L + stream);
        return ((z << 23) | (z >>> 41)) - ((z << 19) | (z >>> 45)) - ((z >>> 27) ^ z) - ((z >>> 27) ^ z);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public VortexRNG copy() {
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
        return (int) (31 * (state ^ (state >>> 32)) + ((stream >>> 1) ^ (stream >>> 32)));
    }
}
