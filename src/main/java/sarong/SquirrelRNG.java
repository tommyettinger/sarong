package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * An int-math version of the Middle Square Weyl Sequence RNG, this has high quality (passes PractRand with no anomalies
 * or failures), has a period of at least {@code pow(2, 32)} (but probably a fair amount more than that), and poor speed
 * (especially for {@link #nextLong()}). This generator takes more than twice as long as {@link Lunge32RNG} to do
 * equivalent work. Based on <a href="https://arxiv.org/pdf/1704.00358.pdf">this paper (PDF)</a> and its corresponding
 * <a href="https://en.wikipedia.org/wiki/Middle-square_method">Wikipedia article</a>, though those versions use 64-bit
 * state variables (two of them) and only use 32 bits of one state variable as output, where this uses two 32-bit state
 * variables and uses one of them in full as its output, incorporating the other as part of a multiplier. Credit for the
 * original method goes to John von Neumann, and credit for finding the Weyl sequence method goes to Bernard Widynski;
 * the technique of multiplying portions of the two states to get the output is novel (to my knowledge).
 * <br>
 * The name comes from SQU-ared-midddle WEYL sequence, which sounds like "Squirrel," kind of. I do like squirrels.
 * <br>
 * Created by Tommy Ettinger on 10/6/2017.
 */
public class SquirrelRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;

    public SquirrelRNG() {
        this((int) ((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int) ((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public SquirrelRNG(final int seed) {
        state0 = seed;
        state1 = (state0 + 0x7F4A7C15);
        state1 = (state1 ^ state1 >>> 14) * (0x2C9277B5 + (state1 * 0x632BE5A6));
        state1 ^= state1 >>> 13;
    }

    public SquirrelRNG(final int seed0, final int seed1) {
        state0 = seed0;
        state1 = seed1;
    }

    public SquirrelRNG(final long seed) {
        state0 = (int) (seed & 0xFFFFFFFFL);
        state1 = (int) (seed >>> 32);
    }

    public SquirrelRNG(final CharSequence seed) {
        this(CrossHash.hash64(seed));
    }

    public int state0, state1;

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return (long) (state1) << 32 | (state0 & 0xFFFFFFFFL);
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long, but this is always truncated when used; the upper 32 bits are discarded
     */
    @Override
    public void setState(final long state) {
        state0 = (int) (state & 0xFFFFFFFFL);
        state1 = (int) (state >>> 32);
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
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return (state0 = (state0 >> 13) * (state1 | 65537)) >>> (32 - bits);
    }

    /**
     * Gets a pseudo-random int, which can be positive or negative but is likely to be drawn from less possible options
     * than the full range of {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}. Very fast, though.
     *
     * @return a pseudo-random 32-bit int
     */
    public final int nextInt() {
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return (state0 = (state0 >> 13) * (state1 | 65537));

    }

    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * This implementation produces a different result than calling {@link #nextInt()} twice and shifting the bits
     * to make a long from the two ints, which is what most int-centric generators do. The technique this uses, as this
     * class usually does, reduces quality but sacrifices as little speed as possible. You get a long from this with
     * only slightly longer time than it takes to produce than an int, from a primarily-int generator! Hooray. The
     * downside is that only 2 to the 32 longs can be produced by this method (not the full 2 to the 64 range that would
     * be ideal), though the period is a little higher than that (2 to the 33). It may be important to note that this
     * changes the sequence of random numbers exactly in the same way as calling {@link #nextInt()}, so you could
     * employ any combination of nextInt() and nextLong() calls and get the same result on a subsequent nextInt() call,
     * given the same starting state.
     * <p>
     * Pseudo-random results may be between between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        final long z = (long) (state0 = (state0 >> 13) * (state1 | 65537)) << 32;
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return z ^ (state0 = (state0 >> 13) * (state1 | 65537));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SquirrelRNG copy() {
        return new SquirrelRNG(state0, state1);
    }

    /**
     * A simple "output stage" applied to two ints of state; this method does not update the states on its own. If you
     * expect to call this method more than once, you should perform some change to state as part of the call; the best
     * way to do this is to call this method like {@code state0 = SquirrelRNG.determine(state0, state1 += 0xC6BC278D)}.
     *
     * @param state0 should be changed to be the result: {@code state0 = determine(state0, state1 += 0xC6BC278D)}
     * @param state1 should be changed when you call this with {@code state1 += 0xC6BC278D}
     * @return a pseudo-random int that should be assigned back to state0 if this should be called again
     */
    public static int determine(int state0, final int state1) {
        state0 *= state0;
        state0 += state1;
        return (state0 >> 13) * (state1 | 65537);
    }
    /**
     * A static version of {@link #nextInt()} that updates a two-or-more-element int array for state. If you call this
     * more than once, simply passing in the same array for state will work fine; state's contents will change with each
     * call, and the result is also stored in the first element of the array for later reuse. You cannot pass null for
     * state, nor any array with a length of less than two; this method does not check for performance reasons.
     *
     * @param state a 2-or-more-element int array that will have its first two elements changed
     * @return a pseudo-random int that will equal the first element in state
     */
    public static int determine(int[] state) {
        state[0] *= state[0];
        state[0] += (state[1] += 0xC6BC278D);
        return (state[0] = (state[0] >> 13) * (state[1] | 65537));
    }

    /**
     * A static random float generator that returns a float between 0.0 (inclusive) and 1.0 (exclusive), and updates a
     * two-or-more-element int array for state. If you call this more than once, simply passing in the same array for
     * state will work fine; state's contents will change with each call. You cannot pass null for state, nor any array
     * with a length of less than two; this method does not check for performance reasons.
     *
     * @param state a 2-or-more-element int array that will have its first two elements changed
     * @return a pseudo-random float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float randomFloat(int[] state) {
        state[0] *= state[0];
        state[0] += (state[1] += 0xC6BC278D);
        return ((state[0] = (state[0] >> 13) * (state[1] | 65537)) & 0xFFFFFF) * 0x1p-24f;
    }

    /**
     * A static random float generator that returns a float between -1.0 (inclusive) and 1.0 (exclusive), and updates a
     * two-or-more-element int array for state. If you call this more than once, simply passing in the same array for
     * state will work fine; state's contents will change with each call. You cannot pass null for state, nor any array
     * with a length of less than two; this method does not check for performance reasons.
     *
     * @param state a 2-or-more-element int array that will have its first two elements changed
     * @return a pseudo-random float from -1f (inclusive) to 1f (exclusive)
     */
    public static float randomSignedFloat(int[] state) {
        state[0] *= state[0];
        state[0] += (state[1] += 0xC6BC278D);
        return ((state[0] = (state[0] >> 13) * (state[1] | 65537)) >> 7) * 0x1p-24f;
    }

    @Override
    public String toString() {
        return "SquirrelRNG with state0 0x" + StringKit.hex(state0) + ", state1 0x" + StringKit.hex(state1);
    }

    @Override
    public int hashCode() {
        return 0x632BE5AB * state0 ^ state1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SquirrelRNG squirrelRNG = (SquirrelRNG) o;

        if (state0 != squirrelRNG.state0) return false;
        return state1 == squirrelRNG.state1;
    }
}