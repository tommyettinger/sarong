package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A modified 64-bit linear congruential generator that allows 2 to the 63 possible streams (any odd long).
 * Its period is 2 to the 64, but you can change the stream after some large amount of generated numbers if you want to
 * effectively extend the period. It is currently slightly slower than LightRNG, a generator that at least in theory
 * also supports 2 to the 63 switchable streams, but the SplitMix64 algorithm in practice requires disallowing many of
 * those streams. It is unclear how many streams of Vortex may be unsuitable, though because the stream variable changes
 * in-step with the state variable, it seems less likely that a single stream would be problematic for long.
 * <br>
 * Changed from several earlier versions with speed or quality issues.
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
     * An odd number that decides which stream this VortexRNG will generate numbers with; the stream changes in a Weyl
     * sequence (adding a large odd number), and the relationship between the Weyl sequence and the state determines how
     * numbers will be generated differently when stream or state changes. As stated, this must be odd.
     * <br>
     * This can be changed after construction but not with any guarantees of quality staying the same
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
        stream = -1L;
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
     * Set the current internal state of this StatefulRandomness with a long; the least-significant bit is disregarded.
     *
     * @param state a 64-bit long
     */
    @Override
    public void setState(long state) {
        this.state = state | 1L;
    }

    public long getStream() {
        return stream;
    }

    public void setStream(long stream) {
        this.stream = stream;
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
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ z >>> 26) * (stream += 0x6A5D39EAE12657BAL);
        return (int)(
                (z ^ z >>> 28)
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
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ z >>> 26) * (stream += 0x6A5D39EAE12657BAL);
        return (z ^ (z ^ stream) >>> 28);
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
        return (int) ((state ^ state >>> 32) + 31 * (stream >>> 1 ^ stream >>> 33));
    }
    public static void main(String[] args)
    {
        /*
        cd target/classes
        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/VortexRNG > vortex_asm.txt
         */
        long seed = 1L;
        VortexRNG rng = new VortexRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        System.out.println(seed);

    }
}
