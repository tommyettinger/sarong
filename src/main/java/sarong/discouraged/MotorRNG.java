package sarong.discouraged;

import sarong.LightRNG;
import sarong.LinnormRNG;
import sarong.SkippingRandomness;
import sarong.StatefulRandomness;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator that should be like {@link LightRNG} but faster; period is 2 to the 64, Stateful and Skipping.
 * One-dimensionally equidistributed. Passes at least 2TB of PractRand with no anomalies. Speed is very close to
 * {@link LinnormRNG}, but this has more features (just {@link #skip(long)}, really). {@link #determine(long)} should
 * also be a good alternative to {@link LinnormRNG#determine(long)}, and about 10% faster (which is hard to attain).
 * <br>
 * This implements SkippingRandomness and StatefulRandomness, meaning you can skip forwards or backwards from
 * any given state in constant time, as well as set the state or get the current state as a long.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
public final class MotorRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 6L;
    /**
     * Can be any long value.
     */
    private long state;

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
        final long y = (state += 0x9E3779B97F4A7C15L);
        final long z = (y ^ (y << 23 | y >>> 41) ^ (y << 29 | y >>> 35)) * 0xDB4F0B9175AE2165L;
        return (int) ((z ^ (z << 11 | z >>> 53) ^ (z << 19 | z >>> 45)) >>> 64 - bits);

//        final long y = (state += 0x9E3779B97F4A7C15L);
//        final long z = (y ^ y >>> 28) * 0xD2B74407B1CE6E93L;
//        return (int) ((z ^ (z << 21 | z >>> 43) ^ (z << 41 | z >>> 23)) >>> 64 - bits);
//        long z = (state += 0x9E3779B97F4A7C15L);
//        z = (z ^ z >>> 21) + 0xC6BC279692B5CC85L;
//        z = (z ^ z >>> 19) + 0x6C8E9CF970932BD5L;
//        return (int) ((z ^ z >>> 25) >>> 64 - bits);

//        final long z = (state += 0x9E3779B97F4A7C15L), x = (z ^ (z << 18 | z >>> 46) ^ (z << 47 | z >>> 17)) * 0x41C64E6DL;
//        return (int) ((x ^ (x << 25 | x >>> 39) ^ (x << 38 | x >>> 26)) >>> (64 - bits));

//        long z = (state += 0x6C8E9CF970932BD5L);
//        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
//        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
//        return (int)(
//                (z ^ (z >>> 25))
//                        >>> (64 - bits));
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
    public final long nextLong() {//0xD1B54A32D192ED03L, 0xABC98388FB8FAC02L, 0x8CB92BA72F3D8DD7L
        final long l = (state += 0x9E3779B97F4A7C15L);
        final long z = (l ^ l >>> 28 ^ 0xDB4F0B9175AE2165L) * 0xC6BC279692B5CC83L + 0x632BE59BD9B4E019L;
        return (z ^ (z << 46 | z >>> 18) ^ (z << 19 | z >>> 45));
//        return z ^ z >>> 26;
//        return determineBare(state += 0x9E3779B97F4A7C15L);
//        long s = (state += 0x9E3779B97F4A7C15L);
//        return ((s = (s ^ (s << 13 | s >>> 51) ^ (s << 29 | s >>> 35)) * 0xDB4F0B9175AE2165L) ^ s >>> 28);
//        final long y = (state += 0x9E3779B97F4A7C15L);
//        final long z = (y ^ (y << 13 | y >>> 51) ^ (y << 29 | y >>> 35)) * 0xDB4F0B9175AE2165L;
//        return (z ^ z >>> 28);
//        final long z = (y ^ (y << 23 | y >>> 41) ^ (y << 29 | y >>> 35)) * 0xDB4F0B9175AE2165L;
//        return (z ^ (z << 11 | z >>> 53) ^ (z << 19 | z >>> 45));

//        final long z = (y ^ y >>> 28) * 0xD2B74407B1CE6E93L;
//        return (z ^ (z << 21 | z >>> 43) ^ (z << 41 | z >>> 23));

//        z = (z ^ z >>> 31 ^ 0xC6BC279692B5CC85L) * 0x41C64E6BL + 0x6C8E9CF970932BD5L;
//        return z ^ z >>> 31;

//        final long y = state;
//        final long z = y ^ (state += 0xC6BC279692B5CC85L);
//        final long r = (y - (z << 29 | z >>> 35)) * z;
//        return r ^ r >>> 28;

//        final long z = (state += 0x9E3779B97F4A7C15L),
//                x = (z ^ (z << 18 | z >>> 46) ^ (z << 47 | z >>> 17)) * 0x41C64E6DL;
//        return (x ^ (x << 25 | x >>> 39) ^ (x << 38 | x >>> 26));

//        long z = (state += 0x6C8E9CF970932BD5L);
//        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
//        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
//        return z ^ (z >>> 25);

//        long a = (state = state * 0x369DEA0F31A53F85L + 1L);
//        a ^= Long.reverseBytes(a) ^ a >>> 29;
//        return a ^ a >>> 25;

//        final long z = (state += 0x6C8E9CF970932BD5L);
//        return (z ^ z >>> 25) ^ Long.reverseBytes(z) * 0x59071D96D81ECD35L;

        //(z ^ z >>> 25) * 0x59071D96D81ECD35L ^ ((z << 12) | (z >>> 52)) ^ ((z << 47) | (z >>> 17));

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
//        return (s = ((s = ((s *= 0x9E3779B97F4A7C15L) << 35 | s >>> 29) * 0xD1B54A32D192ED03L) << 17 | s >>> 47) * 0xABC98388FB8FAC03L) ^ s >>> 28;
//        return ((s = ((s *= 0x9E3779B97F4A7C15L) ^ (s << 13 | s >>> 51) ^ (s << 29 | s >>> 35)) * 0xDB4F0B9175AE2165L) ^ s >>> 28);
        return ((s = ((s *= 0x9E3779B97F4A7C15L) ^ s >>> 25 ^ 0xDB4F0B9175AE2165L) * 0xC6BC279692B5CC83L + 0x632BE59BD9B4E019L)  ^ (s << 46 | s >>> 18) ^ (s << 19 | s >>> 45));
//        return ((s = ((s *= 0x9E3779B97F4A7C15L) ^ (s << 23 | s >>> 41) ^ (s << 29 | s >>> 35)) * 0xDB4F0B9175AE2165L) ^ (s << 11 | s >>> 53) ^ (s << 19 | s >>> 45));
    }

    /**
     * Call with {@code MotorRNG.determineBare(state += 0x9E3779B97F4A7C15L)}, where state can be any long; and the
     * number added to state should usually be fairly similar to 0x9E3779B97F4A7C15L (not many bits should differ, and
     * the number must be odd). If the assignment to state has stayed intact on the next time this is called in the same
     * way, it will have the same qualities as MotorRNG normally does, though if the increment has been changed a lot
     * from {@code 0x9E3779B97F4A7C15L}, then it may have very different quality that MotorRNG should have.
     * <br>
     * You can probably experiment with different increments for stream. The number 0x9E3779B97F4A7C15L is 2 to the 64
     * divided by the golden ratio and adjusted to be an odd number, but it's just a Weyl sequence increment, which
     * means its bit structure is as unpredictable as an irrational number in fixed-point form.
     * @param s any long; increment while calling with {@code state += 0x9E3779B97F4A7C15L}
     * @return a pseudo-random long obtained from the given state deterministically
     */
    public static long determineBare(long s)
    {
        return ((s = (s ^ (s << 13 | s >>> 51) ^ (s << 29 | s >>> 35)) * 0xDB4F0B9175AE2165L) ^ s >>> 28);
//        return ((s = (s ^ (s << 23 | s >>> 41) ^ (s << 29 | s >>> 35)) * 0xDB4F0B9175AE2165L) ^ (s << 11 | s >>> 53) ^ (s << 19 | s >>> 45));
    }
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
        final long y = (state += 0x9E3779B97F4A7C15L * advance);
        final long z = (y ^ (y << 23 | y >>> 41) ^ (y << 29 | y >>> 35)) * 0xDB4F0B9175AE2165L;
        return (z ^ (z << 11 | z >>> 53) ^ (z << 19 | z >>> 45));
        
//        final long y = (state += 0x9E3779B97F4A7C15L * advance);
//        final long z = (y ^ y >>> 28) * 0xD2B74407B1CE6E93L;
//        return (z ^ (z << 21 | z >>> 43) ^ (z << 41 | z >>> 23));
//        final long y = (state += 0xC6BC279692B5CC85L * advance) - 0xC6BC279692B5CC85L;
//        final long z = y ^ state;
//        final long r = (y - (z << 29 | z >>> 35)) * z;
//        return r ^ r >>> 28;

//        final long z = (state += 0x9E3779B97F4A7C15L * advance), x = (z ^ (z << 18 | z >>> 46) ^ (z << 47 | z >>> 17)) * 0x41C64E6DL;
//        return (x ^ (x << 25 | x >>> 39) ^ (x << 38 | x >>> 26));

//        long z = (state += 0x6C8E9CF970932BD5L * advance);
//        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
//        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
//        return z ^ (z >>> 25);
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
