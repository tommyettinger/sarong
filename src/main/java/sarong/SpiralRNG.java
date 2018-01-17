package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator based on combining a multiplicative congruential generator (MCG) and a Weyl Sequence. Using the
 * MCG as the stream, this has 2 to the 63 possible streams instead of 1 stream for something like LightRNG.
 * Its period is 2 to the 64, but you can change the stream after some large amount of generated numbers if you want to
 * effectively extend the period. It is currently slightly slower than VortexRNG, and since that generator also offers
 * twice as many possible streams and comparable quality (high), VortexRNG is currently recommended over this class.
 * Changes may be made that could improve this class in the future.
 * <br>
 * This implements SkippingRandomness but not StatefulRandomness, because while you can skip forwards or backwards from
 * any given state in constant time, you would need to set two variables (state and stream) to accurately change the
 * state, while StatefulRandomness only permits returning one 64-bit long for state or setting the state with one long.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class SpiralRNG implements RandomnessSource, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 3L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Can be any odd-number long value, and this will change to other odd longs as numbers are produceed. However,
     * because this is used in a multiplicative congruential generator with a power-of-2 modulus, only half of all odd
     * numbers will actually be used with a given starting state.
     * <br>
     * This can be changed after construction but not with any guarantees of quality staying the same
     * relative to previously-generated numbers on a different stream.
     */
    private long stream;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public SpiralRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public SpiralRNG(long seed)
    {
        state = seed;
        stream = -1L;
    }

    /**
     *
     * @param seed any long
     * @param stream any long; the most-significant bit (the sign bit, here) is discarded
     */
    public SpiralRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream << 1 | 1L;
    }

    /**
     * Get the current internal state of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getStream()}.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return state;
    }
    /**
     * Set the current internal state of this VortexRNG with a long.
     * @param state any 64-bit long
     */
    public void setState(long state) {
        this.state = state;
    }
    /**
     * Get the current internal stream of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getState()}.
     *
     * @return the current internal stream of this object.
     */
    public long getStream() {
        return stream;
    }
    /**
     * Set the current internal stream of this VortexRNG with a long.
     * @param stream any 64-bit long
     */
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
        long z = (state += 0x6C8E9CF570932BD5L);
        z ^= (z >>> 26) * (stream *= 0x2545F4914F6CDD1DL);
        return (int)(
                (z ^ (~z >>> 26))
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
        z ^= (z >>> 26) * (stream *= 0x2545F4914F6CDD1DL);
        return z ^ (~z >>> 26);
    }

    /**
     * Call with {@code VortexRNG.determine(++state, stream)}, where state can be any long and stream can be any odd
     * long; if the assignment to state has stayed intact on the next time this is called in the same way (and stream is
     * the same), it will have the same qualities as VortexRNG normally does. You can use
     * {@code VortexRNG.determine(--state, stream)} to go backwards.
     * @param state any long; increment while calling with {@code ++state}
     * @param stream any odd-number long; does not need to be changed on each call
     * @return a pseudo-random long obtained from the given state and stream deterministically
     */
    public static long determine(long state, long stream)
    {
        stream *= state * 0x2545F4914F6CDD1DL;
        return (state = (state *= 0x6C8E9CF570932BD5L) ^ (state >>> 26) * stream) ^ ~state >>> 26;
    }
//public static long vortex(long state, long stream) { state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (stream * 0x9E3779B97F4A7BB5L | 1L); return state ^ (state >>> 28); } //vortex(++state, ++stream)
    /**
     * Advances or rolls back the ThrustAltRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        long z = (state += 0x6C8E9CF570932BD5L * advance);
        z ^= (z >>> 26) * (stream *= (advance > 0 ? advance * 0x2545F4914F6CDD1DL : (advance < 0 ? advance * 0x59071D96D81ECD35L : 1L)));
        return z ^ (~z >>> 26);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SpiralRNG copy() {
        return new SpiralRNG(state, stream);
    }
    @Override
    public String toString() {
        return "SpiralRNG on stream 0x" + StringKit.hex(stream) + "L with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpiralRNG spiralRNG = (SpiralRNG) o;

        return state == spiralRNG.state && stream == spiralRNG.stream;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32) + 31 * (stream >>> 1 ^ stream >>> 32));
    }
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/VortexRNG > vortex_asm.txt
//         */
//        long seed = 1L, state = 1L, stream = 11L;
//        //VortexRNG rng = new VortexRNG(seed);
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 1000000007; i++) {
//                seed += determineBare(state += 0x6C8E9CF570932BD5L, stream += 0x9E3779B97F4A7BB5L);
//                //seed += rng.nextLong();
//            }
//        }
//        System.out.println(seed);
//
//    }
}
