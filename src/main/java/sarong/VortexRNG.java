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
 * Changed from several earlier versions with speed or quality issues. This enabled the return of {@link #skip(long)},
 * which is present in LightRNG, ThrustRNG and ThrustAltRNG but was not in earlier versions of this generator.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class VortexRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 3L;
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

    @Override
    public void setState(long state) {
        this.state = state;
    }

    public long getStream() {
        return stream;
    }
    /**
     * Set the current internal stream of this StatefulRandomness with a long; the least-significant bit is disregarded.
     *
     * @param stream a 64-bit long; the least-significant bit is disregarded (i.e. 2 and 3 will be treated the same)
     */
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
        long z = (state += 0x6C8E9CF570932BD5L);
        z = (z ^ (z >>> 25)) * (stream += 0x6A5D39EAE12657BAL);
        return (int)(
                (stream ^ z ^ (z >>> 28))
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
        long z = (state += 0x6C8E9CF570932BD5L);
        z = (z ^ (z >>> 25)) * (stream += 0x6A5D39EAE12657BAL);
        return stream ^ z ^ (z >>> 28);
    }
    /**
     * Advances or rolls back the ThrustAltRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    public final long skip(long advance) {
        long z = (state += 0x6C8E9CF570932BD5L * advance);
        z = (z ^ z >>> 25) * (stream += 0x6A5D39EAE12657BAL * advance);
        return stream ^ z ^ (z >>> 28);
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
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/VortexRNG > vortex_asm.txt
//         */
//        long seed = 1L;
//        VortexRNG rng = new VortexRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        System.out.println(seed);
//
//    }
}
