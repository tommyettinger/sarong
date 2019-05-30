package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A high-quality 32-bit GWT-safe generator that combines 4 of Mark Overton's subcycle generators from
 * <a href="http://www.drdobbs.com/tools/229625477">this article</a>, specifically different CMR each with a 32-bit
 * state, that has all generator results XORed together. Its period is 0xFFFD17BED0F5C7B680C162E3671157ED, or
 * approximately 2 to the 127.999936, and all states that can be produced using {@link #seed(long)} have that same
 * maximal period. Quality seems very strong so far, since {@link Mover32RNG} has two component generators and can pass
 * PractRand to 32TB (with different constants), while this has four component generators. MegaMover32RNG has passed all
 * 32TB of PractRand with no anomalies, and it has more theoretical capabilities, like being able to possibly
 * generate all 64-bit values (it might not generate some, though, and they will have different frequencies). However,
 * it is in most regards a weaker generator than {@link XoshiroAra32RNG}, which has the same state size, a larger
 * period, simpler seeding, and guarantees about its distribution, while being a little faster than MegaMover32RNG.
 * If you want a "Bob-Jenkins-style no-nice-math" generator, following the same principles as Jenkins' {@link IsaacRNG},
 * you might prefer MegaMover32RNG, though.
 * <br>
 * The choice of constants for the multipliers and for the rotation just need to be checked for the period of each
 * component generator, which doesn't take much time, and for the period of all generators to be checked to make sure
 * they are all relatively coprime (as in, they share no common factors). Finding long periods with small-enough
 * multipliers to stay safe on GWT takes longer; an exhaustive search would have taken months, but the findings in the
 * first few days of running such a search yielded some very good component generators.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. Several long tests brute-force checked all seeds in the longest cycle for each of the
 * four component generators, and found sections where at least 2 to the 16 seeds all end up in the longest cycle. A
 * starting state can be quickly chosen from a 64-bit long by using 16 bits per component generator, which doesn't need
 * any stepping through states as in other generators.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed. This generator goes in the opposite direction from {@link MiniMover64RNG}, using
 * more state variables to increase period and make the spacing between repeat occurrences of a result less predictable,
 * so it's "Mega" instead of "Mini" and of course it uses 32-bit math, so it's MegaMover32RNG.
 * <br>
 * Created by Tommy Ettinger on 5/9/2019.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class MegaMover32RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 2L;
    private int stateA, stateB, stateC, stateD;

    /**
     * Calls {@link #seed(long)} with a random long value (obtained using {@link Math#random()}).
     */
    public MegaMover32RNG()
    {
        seed((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    
    /**
     * The recommended constructor, this guarantees the generator will have a period of
     * 0xFFFD17BED0F5C7B680C162E3671157ED, or approximately 2 to the 127.999936. All longs are permissible values
     * for {@code state}. Uses {@link #seed(long)} to handle the actual spread of bits into the states.
     * @param state any long; will be used to get the actual state used in the generator (which is four ints internally)
     */
    public MegaMover32RNG(final long state)
    {
        seed(state);
    }

    /**
     * Not recommended for general use, only for remaking existing generators from their states. This can put the
     * generator in a low-period subcycle (which would be bad), but not if the states were taken from a generator
     * in the longest-period subcycle (which is the case for generators that have used {@link #seed(long)})
     * @param stateA state a; can technically be any int but should not be 0
     * @param stateB state b; can technically be any int but should not be 0
     * @param stateC state c; can technically be any int but should not be 0
     * @param stateD state d; can technically be any int but should not be 0
     */
    public MegaMover32RNG(final int stateA, final int stateB, final int stateC, final int stateD)
    {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }
    /**
     * Seeds the state using all bits of the given long {@code s}. This is guaranteed to put the generator on its
     * longest subcycle, and 2 to the 64 states are possible.
     * @param s all bits are used, none verbatim (0 is tolerated)
     */
    public final void seed(final long s) {
        stateA = 1 + (int)(s & 0xFFFFL);
        stateB = 1 + (int)(s >>> 16 & 0xFFFFL);
        stateC = 0x1D79 + (int)(s >>> 32 & 0xFFFFL);
        stateD = 0x1682B + (int)(s >>> 48);
    }

    public final int nextInt()
    {
        return ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D));
    }
    @Override
    public final int next(final int bits)
    {
        return ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D)) >>> (32 - bits);
    }

//    public final int nextInt2()
//    {
//        stateA = (stateA << 23 | stateA >>> 9);
//        stateA *= 0x402AB;
//        stateB = (stateB << 28 | stateB >>> 4);
//        stateB *= 0x01621;
//        stateC = (stateC << 24 | stateC >>> 8);
//        stateC *= 0x808E9;
//        stateD = (stateD << 29 | stateD >>> 3);
//        stateD *= 0x8012D;
//        return (stateA ^ stateB ^ stateC ^ stateD);
//    }
//
//    public final int next2(final int bits)
//    {
//        stateA = (stateA << 23 | stateA >>> 9);
//        stateA *= 0x402AB;
//        stateB = (stateB << 28 | stateB >>> 4);
//        stateB *= 0x01621;
//        stateC = (stateC << 24 | stateC >>> 8);
//        stateC *= 0x808E9;
//        stateD = (stateD << 29 | stateD >>> 3);
//        stateD *= 0x8012D;
//        return (stateA ^ stateB ^ stateC ^ stateD) >>> (32 - bits);
//    }
//
//    public final int next3(final int bits)
//    {
//        stateA = (stateA << 23 | stateA >>> 9);
//        stateB = (stateB << 28 | stateB >>> 4);
//        stateC = (stateC << 24 | stateC >>> 8);
//        stateD = (stateD << 29 | stateD >>> 3);
//        stateA *= 0x402AB;
//        stateB *= 0x01621;
//        stateC *= 0x808E9;
//        stateD *= 0x8012D;
//        return (stateA ^ stateB ^ stateC ^ stateD) >>> (32 - bits);
//    }

    @Override
    public final long nextLong() {
        
        final long high = ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D)) & 0xFFFFFFFFL;
        return high << 32 ^ ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D));
    }

    /**
     * Gets a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive).
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public final double nextDouble() {
        final long high = ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D)) & 0xFFFFFFFFL;
        return ((high << 32 ^ ((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D))) & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive).
     * @return a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public final float nextFloat() {
        return (((stateA = (stateA << 23 | stateA >>> 9) * 0x402AB) ^ (stateB = (stateB << 28 | stateB >>> 4) * 0x01621) ^ (stateC = (stateC << 24 | stateC >>> 8) * 0x808E9) ^ (stateD = (stateD << 29 | stateD >>> 3) * 0x8012D)) & 0xffffffL) * 0x1p-24f;
    }

    /**
     * Produces a copy of this MegaMover32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this MegaMover32RNG
     */
    @Override
    public MegaMover32RNG copy() {
        return new MegaMover32RNG(stateA, stateB, stateC, stateD);
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int.
     * @param stateA any int
     */
    public void setStateA(int stateA)
    {
        this.stateA = stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int.
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
    }
    public int getStateC()
    {
        return stateC;
    }

    /**
     * Sets the third part of the state to the given int.
     * @param stateC any int
     */
    public void setStateC(int stateC)
    {
        this.stateC = stateC;
    }

    public int getStateD()
    {
        return stateD;
    }

    /**
     * Sets the fourth part of the state to the given int.
     * @param stateD any int
     */
    public void setStateD(int stateD)
    {
        this.stateD = stateD;
    }

    /**
     * Sets the current internal state of this MegaMover32RNG with four ints, where each can be any int.
     * @param stateA any int
     * @param stateB any int
     * @param stateC any int
     * @param stateD any int
     */
    public void setState(final int stateA, final int stateB, final int stateC, final int stateD)
    {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }

    @Override
    public String toString() {
        return "MegaMover32RNG with stateA 0x" + StringKit.hex(stateA) +
                ", stateB 0x" + StringKit.hex(stateB) + ", stateC 0x" + StringKit.hex(stateC)
                + ", stateD 0x" + StringKit.hex(stateD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaMover32RNG megaMover32RNG = (MegaMover32RNG) o;

        return stateA == megaMover32RNG.stateA && stateB == megaMover32RNG.stateB &&
                stateC == megaMover32RNG.stateC && stateD == megaMover32RNG.stateD;
    }

    @Override
    public int hashCode() {
        return 31 * 31 * 31 * stateA + 31 * 31 * stateB + 31 * stateC + stateD | 0;
    }
}
