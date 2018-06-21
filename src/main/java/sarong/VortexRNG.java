package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator based on {@link ThrustAltRNG} but with 2 to the 64 possible streams instead of 1 stream.
 * Its period is 2 to the 64, but you can change the stream after some large amount of generated numbers if you want to
 * effectively extend the period. It is currently slightly slower than LightRNG, a generator that at least in theory
 * supports 2 to the 63 switchable streams, but its SplitMix64 algorithm in practice requires disallowing many of
 * those streams. It is unclear how many streams of Vortex may be unsuitable, though because the stream variable changes
 * in-step with the state variable, it seems less likely that a single stream would be problematic for long.
 * <br>
 * This implements SkippingRandomness but not StatefulRandomness, because while you can skip forwards or backwards from
 * any given state in constant time, you would need to set two variables (state and stream) to accurately change the
 * state, while StatefulRandomness only permits returning one 64-bit long for state or setting the state with one long.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class VortexRNG implements RandomnessSource, SkippingRandomness, Serializable {
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
    public long stream;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public VortexRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public VortexRNG(long seed)
    {
        state = seed;
        stream = 0x6C8E9CF970932BD5L;
    }
    public VortexRNG(final long seed, final int stream) {
        state = seed;
        this.stream = 0x6C8E9CF970932BD5L ^ (long) stream << 32;
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
     * Get the stream of this VortexRNG as an int.
     * This int can be passed back to {@link #setStream(int)} directly; it is not the internally-used long stream.
     *
     * @return the stream of this object in a format that can be used with {@link #setStream(int)}.
     */
    public int getStream() {
        return (int) ((stream ^ 0x6C8E9CF970932BD5L) >>> 32);
    }
    /**
     * Set the internal stream of this VortexRNG with an int.
     * Internally, the 32 bits of the given int are used to change the upper 32 bits of a known-good 64-bit long,
     * ensuring the lower 32 bits of that long are the same since they seem to matter more for quality.
     * @param stream any int
     */
    public void setStream(int stream) {
        this.stream = 0x6C8E9CF970932BD5L ^ (long) (stream) << 32;
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
        //0x6C8E9CF570932BD5 0x6C8E9CF970932BD5
        long z = (state += stream);
        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
        return (int)(
                (z ^ (z >>> 25))
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
        long z = (state = state * 0x41C64E6DL + stream);
        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
        return z ^ (z >>> 25);
    }

    /**
     * Call with {@code VortexRNG.determine(++state)}, where state can be any long; this will act like a VortexRNG 
     * constructed with {@link #VortexRNG(long)} or a stream of 0. You can use {@code VortexRNG.determine(--state)} to
     * go backwards. If you have control over state and stream, you may prefer {@link #determineBare(long)}, which
     * requires adding a specific large number to each parameter but may be slightly faster.
     * @param s any long; increment while calling with {@code ++state}
     * @return a pseudo-random long obtained from the given state and stream deterministically
     */
    public static long determine(long s) { 
        return (s = (s = ((s *= 0x6C8E9CF970932BD5L) ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25;
    }

    /**
     * Call with {@code VortexRNG.determineBare(state += 0x6C8E9CF970932BD5L)}, where state can be any long; and the
     * number added to state should usually keep its less-significant 32 bits unchanged. If the assignment to state has
     * stayed intact on the next time this is called in the same way, it will have the same qualities as VortexRNG
     * normally does, though if the less-significant 32 bits of the increment change from {@code 0x70932BD5}, then it
     * may have very different quality that VortexRNG should have.
     * <br>
     * You can probably experiment with different increments for stream. The number 0x6C8E9CF970932BD5L was obtained 
     * through quite a lot of trial and error, modified to give optimal results for the common case of stream being 0,
     * and is also just a lucky find, but it's just a Weyl sequence increment, which means its bit structure is as
     * unpredictable as an irrational number in fixed-point form. A common number used for this is 0x9E3779B97F4A7BB5L,
     * which is the golden ratio times 2 to the 64. Here, we need to keep the bottom 32 bits intact to keep the same
     * assurances VortexRNG normally gets; even if you disregard that and change those bits, the increment must be an
     * odd number (this is true for every Weyl sequence).
     * @param s any long; increment while calling with {@code state += 0x6C8E9CF970932BD5L} (only change the more-significant 32 bits of that increment)
     * @return a pseudo-random long obtained from the given state deterministically
     */
    public static long determineBare(long s)
    {
        return (s = (s = (s ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25;
    }
//public static long vortex(long state) { (s = (s = ((s *= 0x6C8E9CF970932BD5L) ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25; } //vortex(++state)
    /**
     * Advances or rolls back the VortexRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        long z = (state += stream * advance);
        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
        return z ^ (z >>> 25);
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
        return new VortexRNG(state, (int) ((stream ^ 0x6C8E9CF970932BD5L) >>> 32));
    }
    @Override
    public String toString() {
        return "VortexRNG with state 0x" + StringKit.hex(state) + "L using stream 0x" + StringKit.hex((int) ((stream ^ 0x6C8E9CF970932BD5L) >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VortexRNG vortexRNG = (VortexRNG) o;

        return state == vortexRNG.state && stream == vortexRNG.stream;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32) + 31L * stream);
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
//                seed += determineBare(state += 0x6C8E9CF970932BD5L);
//                //seed += rng.nextLong();
//            }
//        }
//        System.out.println(seed);
//
//    }
}
