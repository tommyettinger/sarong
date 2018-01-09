package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator that mixes aspects of a linear congruential generator with a multiplicative congruential generator
 * and several xorshift steps. It has 2 to the 126 possible streams, allowing 62 bits for one stream and 64 for another.
 * Its period is at least 2 to the 64, but you can change the stream after some large amount of generated numbers if you
 * want to effectively extend the period.
 * <br>
 * Currently, this is very slow, with half the speed of ThrustAltRNG, and significantly worse speed than VortexRNG,
 * which can be considered similar in goals. However, it has excellent quality when tested with gjrand (on 100GB, it
 * scored better than any other generator in this library, though that may have been due to the randomly-chosen seed).
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class MeshRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 3L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * A long that decides which stream this VortexRNG will generate numbers with; the stream changes in a Weyl
     * sequence (adding a large odd number), and the relationship between the Weyl sequence and the state determines how
     * numbers will be generated differently when stream or state changes. It's perfectly fine to supply a value of 0
     * for stream, since it won't be used verbatim and will also change during the first number generation.
     * <br>
     * This can be changed after construction but not with any guarantees of quality staying the same
     * relative to previously-generated numbers on a different stream.
     */
    public long stream0;

    /**
     * A long that must be odd, and is used as the state of a multiplicative congruential generator, and for other
     * purposes during generation.
     */
    private long stream1;
    /**
     * Creates a new generator seeded using Math.random.
     */
    public MeshRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Constructs a MeshRNG with the given seed, using the bitwise negation of seed as stream0, and fixing stream1's
     * starting value to the golden-ratio-based constant {@code 0x9E3779B97F4A7C15L}.
     * @param seed any long
     */
    public MeshRNG(long seed)
    {
        state = seed;
        stream0 = ~seed;
        stream1 = 0x9E3779B97F4A7C15L;
    }

    /**
     * Constructs a MeshRNG with a seed and two stream values, all of which are longs. The seed and stream0 can be any
     * long; stream1 will not be used as-is, and the most significant two bits (including the sign bit) will be
     * discarded.
     * @param seed any long
     * @param stream0 any long
     * @param stream1 any long, but the most significant 2 bits will be discarded and the rest shifted
     */
    public MeshRNG(final long seed, final long stream0, final long stream1) {
        state = seed;
        this.stream0 = stream0;
        this.stream1 = stream1 << 2 | 1L;
    }

    /**
     * Get the current internal state of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getStream0()}.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return state;
    }
    /**
     * Set the current internal state of this VortexRNG with a long.
     * @param state any long
     */
    public void setState(long state) {
        this.state = state;
    }
    /**
     * Get the primary internal stream of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getState()} and {@link #getStream1()}.
     *
     * @return the first current internal stream of this object.
     */
    public long getStream0() {
        return stream0;
    }
    /**
     * Set the primary current internal stream of this VortexRNG with a long.
     * @param stream0 any long
     */
    public void setStream0(long stream0) {
        this.stream0 = stream0;
    }
    /**
     * Get the extra current internal stream of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getState()} and {@link #getStream0()}.
     *
     * @return the current internal stream of this object.
     */
    public long getStream1() {
        return stream0;
    }
    /**
     * Set the extra current internal stream of this VortexRNG with a long.
     * @param stream1 any long, but the most significant 2 bits will be discarded and the rest shifted
     */
    public void setStream1(long stream1) {
        this.stream1 = (stream1 << 2) | 1L;
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
        final long z = (state = state * 0x369DEA0F31A53F85L + (stream1 *= 0x2545F4914F6CDD1DL)) ^ (((stream0 += 0x9E3779B97F4A7C15L) >>> 28) * stream1);
        return (int)(
                (z ^ (stream0 - z) >>> 28)
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
        final long z = (state = state * 0x369DEA0F31A53F85L + (stream1 *= 0x2545F4914F6CDD1DL)) ^ (((stream0 += 0x9E3779B97F4A7C15L) >>> 28) * stream1);
        return z ^ (stream0 - z) >>> 28;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public MeshRNG copy() {
        return new MeshRNG(state, stream0, stream1);
    }
    @Override
    public String toString() {
        return "MeshRNG on stream0 0x" + StringKit.hex(stream0) + "L and stream1 0x" + StringKit.hex(stream1) + "L with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeshRNG meshRNG = (MeshRNG) o;

        return state == meshRNG.state && stream0 == meshRNG.stream0 && stream1 == meshRNG.stream1;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32) + 31 * ((stream0 ^ stream0 >>> 32) + 31 * (stream1 >>> 1 ^ stream1 >>> 33)));
    }
}
