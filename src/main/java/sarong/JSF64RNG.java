package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A high-quality 64-bit generator using one of
 * <a href="http://www.pcg-random.org/posts/bob-jenkins-small-prng-passes-practrand.html">Bob Jenkins' designs</a>; JSF
 * is short for Jenkins Small Fast and was coined by Chris Doty-Humphrey when he reviewed it favorably in PractRand.
 * This is a "chaotic" generator, meaning its state transition is very challenging to predict; it is essentially
 * impossible to invert the generator and get the state from even quite a lot of outputs, unlike generators like
 * {@link LightRNG}. It uses no multiplication internally and primarily depends on bitwise rotation combined with fast
 * operations like addition and XOR to achieve quality results. Its period is unknown beyond that for all seeds
 * possible, the period must be at minimum 2 to the 20, and is at most a little less than 2 to the 256.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It always takes an long when being seeded.
 * <br>
 * Special thanks to everyone who's reviewed this type of RNG, especially M.E. O'Neill, whose blog showed me how useful
 * JSF is. Of course, thanks go to Bob Jenkins for writing yet another remarkable piece of code.
 * <br>
 * Created by Tommy Ettinger on 11/3/2019.
 * @author Bob Jenkins
 * @author Tommy Ettinger
 */
public final class JSF64RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 2L;
    private long stateA, stateB, stateC, stateD;

    /**
     * Calls {@link #seed(long)} with a random long value (obtained using {@link Math#random()}).
     */
    public JSF64RNG()
    {
        seed((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * The recommended constructor, this guarantees the generator will have a period of at least 2 to the 20, and makes
     * it likely that the period is actually much larger. All longs are permissible values for {@code state}. Uses
     * {@link #seed(long)} to handle the actual spread of bits into the states.
     * @param state any long; will be used to get the actual state used in the generator (which is four longs internally)
     */
    public JSF64RNG(final long state)
    {
        seed(state);
    }

    /**
     * Not recommended for general use, only for remaking existing generators from their states. This can put the
     * generator in a low-period subcycle (which would be bad), but not if the states were taken from a generator
     * in the longest-period subcycle (which is the case for generators that have used {@link #seed(long)})
     * @param stateA state a; can technically be any long but should probably not be the same as all other states
     * @param stateB state b; can technically be any long
     * @param stateC state c; can technically be any long
     * @param stateD state d; can technically be any long
     */
    public JSF64RNG(final long stateA, final long stateB, final long stateC, final long stateD)
    {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }
    /**
     * Seeds the state using all bits of the given long {@code s}. This is guaranteed to put the generator on its
     * longest subcycle, and 2 to the 64 states are possible.
     * @param s all bits are used to affect 3 of 4 states verbatim (0 is tolerated, and one state is unaffected by seed)
     */
    public final void seed(final long s) {
        stateA = 0xf1ea5eed;
        stateB = s;
        stateC = s;
        stateD = s;
        for (int i = 0; i < 20; i++) {
            final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
            stateC = stateD + e;
            stateD = e + stateA;
        }
    }

    public final int nextInt()
    {
        final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
        stateC = stateD + e;
        return (int)(stateD = e + stateA);
    }
    @Override
    public final int next(final int bits)
    {
        final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
        stateC = stateD + e;
        return (int)((stateD = e + stateA) >>> 64 - bits);
    }

    @Override
    public final long nextLong() {
        final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
        stateC = stateD + e;
        return  (stateD = e + stateA);
    }

    /**
     * Gets a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive).
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public final double nextDouble() {
        final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
        stateC = stateD + e;
        return ((stateD = e + stateA) & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive).
     * @return a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public final float nextFloat() {
        final long e = stateA - (stateB << 39 | stateB >>> 25);
            stateA = stateB ^ (stateC << 11 | stateC >>> 53);
            stateB = stateC + stateD;
        stateC = stateD + e;
        return ((stateD = e + stateA) & 0xffffffL) * 0x1p-24f;
    }

    /**
     * Produces a copy of this MegaMover32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this MegaMover32RNG
     */
    @Override
    public JSF64RNG copy() {
        return new JSF64RNG(stateA, stateB, stateC, stateD);
    }

    public long getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given long; this can put the generator on a bad subcycle.
     * @param stateA any long
     */
    public void setStateA(long stateA)
    {
        this.stateA = stateA;
    }
    public long getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given long; this can put the generator on a bad subcycle.
     * @param stateB any long
     */
    public void setStateB(long stateB)
    {
        this.stateB = stateB;
    }
    public long getStateC()
    {
        return stateC;
    }

    /**
     * Sets the third part of the state to the given long; this can put the generator on a bad subcycle.
     * @param stateC any long
     */
    public void setStateC(long stateC)
    {
        this.stateC = stateC;
    }

    public long getStateD()
    {
        return stateD;
    }

    /**
     * Sets the fourth part of the state to the given long; this can put the generator on a bad subcycle.
     * @param stateD any long
     */
    public void setStateD(long stateD)
    {
        this.stateD = stateD;
    }

    /**
     * Sets the current internal state of this MegaMover32RNG with four longs, where each can be any long; this can put
     * the generator on a bad subcycle.
     * @param stateA any long
     * @param stateB any long
     * @param stateC any long
     * @param stateD any long
     */
    public void setState(final long stateA, final long stateB, final long stateC, final long stateD)
    {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }

    @Override
    public String toString() {
        return "JSF64RNG with stateA 0x" + StringKit.hex(stateA) +
                ", stateB 0x" + StringKit.hex(stateB) + ", stateC 0x" + StringKit.hex(stateC)
                + ", stateD 0x" + StringKit.hex(stateD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JSF64RNG jsf64RNG = (JSF64RNG) o;

        return stateA == jsf64RNG.stateA && stateB == jsf64RNG.stateB &&
                stateC == jsf64RNG.stateC && stateD == jsf64RNG.stateD;
    }

    @Override
    public int hashCode() {
        long h = 31L * 31L * 31L * stateA + 31L * 31L * stateB + 31L * stateC + stateD;
        return (int)(h ^ h >>> 32);
    }
}
