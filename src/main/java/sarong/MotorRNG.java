package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator based on {@link VortexRNG} but with the stream fixed to a known-good number.
 * Its period is 2 to the 64.
 * <br>
 * This implements SkippingRandomness and StatefulRandomness, meaning you can skip forwards or backwards from
 * any given state in constant time, as well as set the state or get the current state as a long.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class MotorRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 3L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public MotorRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public MotorRNG(long seed)
    {
        state = seed;
    }

    /**
     * Get the current internal state of this MotorRNG as a long.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return state;
    }
    /**
     * Set the current internal state of this MotorRNG with a long.
     * @param state any 64-bit long
     */
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
        long z = (state += 0x6C8E9CF970932BD5L);
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
        long z = (state += 0x6C8E9CF970932BD5L);
        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
        return z ^ (z >>> 25);

//        long a = (state = state * 0x369DEA0F31A53F85L + 1L);
//        a ^= Long.reverseBytes(a) ^ a >>> 29;
//        return a ^ a >>> 25;

//        final long z = (state += 0x6C8E9CF970932BD5L);
//        return (z ^ z >>> 25) ^ Long.reverseBytes(z) * 0x59071D96D81ECD35L;

        //(z ^ z >> 25) * 0x59071D96D81ECD35L ^ ((z << 12) | (z >> 52)) ^ ((z << 47) | (z >> 17));

    }

    /**
     * Call with {@code MotorRNG.determine(++state)}, where state can be any long; this will act like a MotorRNG 
     * constructed with {@link #MotorRNG(long)}, but the state of a MotorRNG won't be the same as the s parameter here.
     * You can use {@code MotorRNG.determine(--state)} to go backwards. If you have control over state, you may prefer
     * {@link #determineBare(long)}, which requires adding a specific large number to each parameter but may be faster.
     * @param s any long; increment while calling with {@code ++state}
     * @return a pseudo-random long obtained from the given state and stream deterministically
     */
    public static long determine(long s) { 
        return (s = (s = ((s *= 0x6C8E9CF970932BD5L) ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25;
    }

    /**
     * Call with {@code MotorRNG.determineBare(state += 0x6C8E9CF970932BD5L)}, where state can be any long; and the
     * number added to state should usually keep its less-significant 32 bits unchanged. If the assignment to state has
     * stayed intact on the next time this is called in the same way, it will have the same qualities as MotorRNG
     * normally does, though if the less-significant 32 bits of the increment change from {@code 0x70932BD5}, then it
     * may have very different quality that MotorRNG should have.
     * <br>
     * You can probably experiment with different increments for stream. The number 0x6C8E9CF970932BD5L was obtained 
     * through quite a lot of trial and error, modified to give optimal results for the common case of stream being 0,
     * and is also a lucky find, but it's just a Weyl sequence increment, which means its bit structure is as
     * unpredictable as an irrational number in fixed-point form. A common number used for this is 0x9E3779B97F4A7BB5L,
     * which is the golden ratio times 2 to the 64. Here, we need to keep the bottom 32 bits intact to keep the same
     * assurances MotorRNG normally gets; even if you disregard that and change those bits, the increment must be an
     * odd number (this is true for every Weyl sequence).
     * @param s any long; increment while calling with {@code state += 0x6C8E9CF970932BD5L} (only change the more-significant 32 bits of that increment)
     * @return a pseudo-random long obtained from the given state deterministically
     */
    public static long determineBare(long s)
    {
        return (s = (s = (s ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25;
    }
//public static long motor(long state) { (s = (s = ((s *= 0x6C8E9CF970932BD5L) ^ s >>> 25) * 0x2545F4914F6CDD1DL) ^ ((s << 19) | (s >>> 45)) ^ ((s << 53) | (s >>> 11))) ^ s >>> 25; } //motor(++state)
    /**
     * Advances or rolls back the MotorRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        long z = (state += 0x6C8E9CF970932BD5L * advance);
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
    public MotorRNG copy() {
        return new MotorRNG(state);
    }
    @Override
    public String toString() {
        return "MotorRNG with state 0x" + StringKit.hex(state) + "L";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MotorRNG motorRNG = (MotorRNG) o;

        return state == motorRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32));
    }
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/MotorRNG > motor_asm.txt
//         */
//        long seed = 1L, state = 1L, stream = 11L;
//        //MotorRNG rng = new MotorRNG(seed);
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
