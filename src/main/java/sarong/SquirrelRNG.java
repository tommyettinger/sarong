package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * An int-math version of the Middle Square Weyl Sequence RNG, this has high quality (passes PractRand with no anomalies
 * or failures), has a period of at least {@code pow(2, 32)} (but probably a fair amount more than that), and unknown
 * speed at this time (probably not great, definitely not for {@link #nextLong()}). Based on
 * <a href="https://arxiv.org/pdf/1704.00358.pdf">this paper (PDF)</a> and its corresponding
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
//        final int z = (state0 += (state1 + ((state1 += 0x9E3779B9) >>> 14)) * 0x2C9277B5) * 0x5F356495;
//        return (z ^ z >>> 13) * 0x01C8E815 >>> (32 - bits);
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return (state0 = (state0 >> 13) * (state1 | 65537)) >>> (32 - bits);

//        return (state0 += (((state1 += 0xC6BC278D) * 0x01C8E815 + 0x632D978F >>> 16) + 60) * 0x2C9277B5) >>> (32 - bits);
        //return (state0 += (((state1 += 0xC6BC278D) >>> 28) + 60) * 0x632D978F) >>> (32 - bits);
    }

    /**
     * Gets a pseudo-random int, which can be positive or negative but is likely to be drawn from less possible options
     * than the full range of {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}. Very fast, though.
     *
     * @return a pseudo-random 32-bit int
     */
    public final int nextInt() {
//        return (state0 += ((state1 += 0xC6BC278D) * 0x01C8E815 + 0x632D978F) * 0x2C9277B5);
//        final int z = (state0 += (((state1 += 0x9E3779B9) * 0x01C8E815 + 0x632D978F) >>> 13) * 0x2C9277B5);
//        return (z ^ z >>> 14) * 0x5F356495;
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return (state0 = (state0 >> 13) * (state1 | 65537));
        //return (state0 += (((state1 += 0xC6BC278D) >>> 28) + 60) * 0x632D978F);
        //return (state0 += 0x9E3779B9 + ((state1 += 0xC6BC278D) >> 28) * 0x632DB5AB);
        //return (state0 += (state1 += 0x9E3779B9) ^ 0x632BE5AB);
        //return (state0 += (0x632BE5AB ^ (state1 += 0x9E3779B9)) >> 1) * 0xC6BC278D;
        /*
        final int s0 = state0;
        int s1 = state1;
        final int result = s0 + s1;
        s1 ^= s0 + 0x9E3779B9;
        state0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 7); // a, b
        state1 = (s1 << 18 | s1 >>> 14); // c
        return result;
        */
        //0x632BE5AB
        //final int z = (state += 0x9E3779B9);
        //return (z >> (z & 15)) * 0xC6BC278D;
        //final int z = (state + 0x9E3779B9);
        //return state += ((state + 0x9E3779B9) >> 5) * 0xC6BC278D;
        //final int z = (state += 0x9E3779B9), r = (z & 15);
        //return (z >> r) * 0xC6BC278D;
        //return state += (state >> 5) * 0xC6BC279692B5CC83L + 0x9E3779B97F4A7C15L;

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
//        final int x = (state0 += (((state1 += 0x9E3779B9) * 0x01C8E815 + 0x632D978F) >>> 13) * 0x2C9277B5),
//                y = (state0 += (((state1 += 0x9E3779B9) * 0x01C8E815 + 0x632D978F) >>> 13) * 0x2C9277B5);
//        return (x ^ x >>> 14) * 0x5F35649500000000L ^ (y ^ y >>> 14) * 0x5F356495L;
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        final long z = (long) (state0 = (state0 >> 13) * (state1 | 65537)) << 32;
        state0 *= state0;
        state0 += (state1 += 0xC6BC278D);
        return z ^ (state0 = (state0 >> 13) * (state1 | 65537));

        //0x9E3779B97F4A7C15L
        //final long r = (state0 += (((state1 += 0xC6BC278D) >>> 28) + 60) * 0x632D978F);
        //return r * 0xC6BC279692B5CC53L ^ r << 32;
//        return (long) (state0 += (((state1 += 0xC6BC278D) * 0x01C8E815 + 0x632D978F >>> 16) + 60) * 0x2C9277B5) << 32
//                ^ (state0 += (((state1 += 0xC6BC278D) * 0x01C8E815 + 0x632D978F >>> 16) + 60) * 0x2C9277B5);
        //final long r = (state0 += ((((state1 += 0xC6BC278D) >>> 24) + 0x9E3779A) >>> 4) * 0x632D978F);
        //return r * 0xC6AC279692B5CC53L ^ r << 32;
        //final long r = (state0 += ((state1 += 0xC6BC278D) >> 28) * 0x632DB5AB) * 0x9E3779B97F4A7C15L;
        //return r * 0x85157AF5L ^ r << 32;

        // return ((state += 0x9E3779B97F4A7C15L ^ state << 1) >> 16) * 0xC6BC279692B5CC83L;

        /*
        final int s0 = state0;
        final int s1 = state1;
        return (s0 + s1) ^ (((state0 = s0 + 0x632BE5AB ^ (state1 = s1 + 0x9E3779B9)) >> 13) * 0xC6BC279692B5CC83L) << 32;
        *//*
        final int s0 = state0;
        int s1 = state1;
        final long result = s0 * 0xD0E89D2D311E289FL + s1 * 0xC6BC279692B5CC83L;
        s1 ^= s0 + 0x9E3779B9;
        state0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 7); // a, b
        state1 = (s1 << 18 | s1 >>> 14); // c
        return result;
        */
        /*
        int z = state + 0x9E3779B9;
        state += (z >> (z >>> 28)) * 0xC6BC279692B5CC83L;
        z = (state + 0x9E3779B9);
        return (state) ^ (long)(state += ((z >> (z >>> 28)) * 0xC6BC279692B5CC83L)) << 32;
        */
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
        return new SquirrelRNG(state0, state1);
    }

    /**
     * A simple "output stage" applied to state; this method does not update state on its own. If you expect to call
     * this method more than once, you should perform some change to state as part of the call; a simple way to do this
     * is to call this method like {@code FlapRNG.determine(state += 0x9E3779B9)}. The int 0x9E3779B9 is derived from
     * the golden ratio, and shows up often as an optimal part of hashes and random number generators, but the constant
     * can be any odd-number int, preferably a large one. This method doesn't offer very good quality assurances, but
     * should be very fast.
     *
     * @param state should be changed when you call this (see above), e.g. {@code state += 0x9E3779B9}
     * @return an altered version of state that should be very fast to compute but doesn't promise great quality
     */
    public static int determine(final int state) {
        return (state + (((state * 0xC6BC278D) >>> 28) + 60) * 0x632D978F);
    }

    /**
     * A simple "output stage" applied to a two-part state like what FlapRNG uses normally; this method does not update
     * state0 or state1 on its own. If you expect to call this method more than once, you should perform some change to
     * state0 and state1 as part of the call; a simple way to do this is to call this method like
     * {@code (state0 += FlapRNG.determine(state0, state1 += 0x9E3779B9))}. The int 0x9E3779B9 is derived from
     * the golden ratio, and shows up often as an optimal part of hashes and random number generators, but the constant
     * can be any odd-number int, preferably a large one. This method doesn't offer very good quality assurances, but
     * should be very fast.
     *
     * @param state0 should be changed when you call this (see above), e.g. by adding the result to state0
     * @param state1 should be changed when you call this (see above), e.g. {@code state1 += 0x9E3779B9}
     * @return an altered version of state0/state1 that should be very fast to compute but doesn't promise great quality
     */
    public static int determine(final int state0, final int state1) {
        return (state0 + (((state1 * 0xC6BC278D) >>> 28) + 60) * 0x632D978F);
    }

    /**
     * Like {@link #determine(int)}, but limits its results to between 0 (inclusive) and bound (exclusive). You can give
     * a negative value for bound, which will produce a negative result or 0. If you expect to call this method more
     * than once, you should perform some change to state as part of the call; a simple way to do this is to call this
     * method like {@code FlapRNG.determineBounded(state += 0x9E3779B9)}. The int 0x9E3779B9 is derived from the golden
     * ratio, and shows up often as an optimal part of hashes and random number generators, but the constant can be any
     * odd-number int, preferably a large one.
     * @param state should usually be changed when you call this (see above), e.g. {@code state += 0x9E3779B9}
     * @param bound the exclusive outer bound; may be negative
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(int state, final int bound)
    {
        return (int)((bound * ((state + (((state * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Like {@link #determine(int, int)}, but limits its results to between 0 (inclusive) and bound (exclusive). You can
     * give a negative value for bound, which will produce a negative result or 0. this method does not update state0 or
     * state1 on its own. If you expect to call this method more than once, you should perform some change to
     * state0 and state1 as part of the call; a simple way to do this is to call this method like
     * {@code FlapRNG.determineBounded(state0 += 0x9E3779B9, state1 += state0 >> 1)}. The int 0x9E3779B9 is derived from
     * the golden ratio, and shows up often as an optimal part of hashes and random number generators, but this constant
     * can be any odd-number int, preferably a large one. This method doesn't offer very good quality assurances, but
     * should be very fast.
     *
     * @param state0 should be changed when you call this (see above), e.g. {@code state0 += 0x9E3779B9}
     * @param state1 should be changed when you call this (see above), e.g. by adding some portion of state0 to state1
     * @param bound the exclusive outer bound; may be negative
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(final int state0, final int state1, final int bound)
    {
        return (int)((bound * ((state0 + (((state1 * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) & 0x7FFFFFFFL)) >> 31);
    }
    /**
     * Gets a pseudo-random float between 0f (inclusive) and 1f (exclusive) using the given state. If you expect to call
     * this method more than once, you should perform some change to state as part of the call; a simple way to do this
     * is to call this method like {@code FlapRNG.determine(state += 0x9E3779B9)}. The int 0x9E3779B9 is derived from
     * the golden ratio, and shows up often as an optimal part of hashes and random number generators, but the constant
     * can be any odd-number int, preferably a large one.
     *
     * @param state any int
     * @return a pseudo-random float from -0f (inclusive) to 1f (exclusive)
     */
    public static float randomFloat(final int state) {
        return NumberTools.intBitsToFloat(((state + (((state * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) >>> 9) | 0x3f800000) - 1f;
    }

    /**
     * Gets a pseudo-random float between 0f (inclusive) and 1f (exclusive) using the given states. If you expect to
     * call this method more than once, you should perform some change to state as part of the call; a simple way to do
     * this is to call this method like {@code FlapRNG.randomFloat(state0 += state1, state1 += 0x9E3779B9)}.
     * The int 0x9E3779B9 is derived from the golden ratio, and shows up often as an optimal part of hashes and random
     * number generators, the constant can be any odd-number int, preferably a large one. Here, state0 is incremented by
     * the before-value of state1, which gives a good distribution of inputs on repeated calls.
     *
     * @param state0 any int
     * @param state1 any int
     * @return a pseudo-random float from -0f (inclusive) to 1f (exclusive)
     */
    public static float randomFloat(final int state0, final int state1) {
        return NumberTools.intBitsToFloat(((state0 + (((state1 * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) >>> 9) | 0x3f800000) - 1f;
    }

    /**
     * Gets a pseudo-random float between -1f (inclusive) and 1f (exclusive) using the given state. If you expect to call
     * this method more than once, you should perform some change to state as part of the call; a simple way to do this
     * is to call this method like {@code FlapRNG.determine(state += 0x9E3779B9)}. The int 0x9E3779B9 is derived from
     * the golden ratio, and shows up often as an optimal part of hashes and random number generators, but the constant
     * can be any odd-number int, preferably a large one.
     *
     * @param state any int
     * @return a pseudo-random float from -1f (inclusive) to 1f (exclusive)
     */
    public static float randomSignedFloat(final int state) {
        return NumberTools.intBitsToFloat(((state + (((state * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) >>> 9) | 0x40000000) - 3f;
    }

    /**
     * Gets a pseudo-random float between -1f (inclusive) and 1f (exclusive) using the given states. If you expect to
     * call this method more than once, you should perform some change to state as part of the call; a simple way to do
     * this is to call this method like {@code FlapRNG.randomSignedFloat(state0 += state1, state1 += 0x9E3779B9)}.
     * The int 0x9E3779B9 is derived from the golden ratio, and shows up often as an optimal part of hashes and random
     * number generators, the constant can be any odd-number int, preferably a large one. Here, state0 is incremented by
     * the before-value of state1, which gives a good distribution of inputs on repeated calls.
     *
     * @param state0 any int
     * @param state1 any int
     * @return a pseudo-random float from -1f (inclusive) to 1f (exclusive)
     */
    public static float randomSignedFloat(final int state0, final int state1) {
        return NumberTools.intBitsToFloat(((state0 + (((state1 * 0xC6BC278D) >>> 28) + 60) * 0x632D978F) >>> 9) | 0x40000000) - 3f;
    }

    @Override
    public String toString() {
        return "FlapRNG with state0 0x" + StringKit.hex(state0) + ", state1 0x" + StringKit.hex(state1);
    }

    @Override
    public int hashCode() {
        return 0x632BE5AB * state0 ^ state1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SquirrelRNG flapRNG = (SquirrelRNG) o;

        if (state0 != flapRNG.state0) return false;
        return state1 == flapRNG.state1;
    }
}