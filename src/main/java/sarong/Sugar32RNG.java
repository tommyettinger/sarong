package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * An oddly simple generator that uses only Add, Rotate, and XOR operations to combine 3 states and get their next values,
 * as well as a counter that ensures a minimum period of 2 to the 32 and a typical period that is drastically higher. It is
 * known to pass at least 8TB of PractRand without anomalies. This design is inspired by, but not directly related to, JSF
 * and SFC generators by Bob Jenkins and Chris Doty-Humphrey, respectively.
 * <br>
 * Created by Tommy Ettinger on 11/5/2019.
 * @author Tommy Ettinger
 */
public final class Sugar32RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 2L;
    private int stateA, stateB, stateC, stateD;

    /**
     * Sets the states with four random int values (obtained using {@link Math#random()}).
     */
    public Sugar32RNG ()
    {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
           (int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
        for (int i = 0; i < 16; i++) {
            nextInt();
        }
    }
    
    /**
     * 
     * @param state any long; will be used to get the actual state used in the generator (which is four ints internally)
     */
    public Sugar32RNG (final long state)
    {
        seed(state);
    }

    /**
     * This is probably a fine constructor for general use; if stateA, stateB and stateC are all very small or 0, then this might
     * not have high-quality results at first. Useful for replicating generators.
     * @param stateA state a; can technically be any int
     * @param stateB state b; can technically be any int
     * @param stateC state c; can technically be any int
     * @param stateD state d; can technically be any int
     */
    public Sugar32RNG (final int stateA, final int stateB, final int stateC, final int stateD)
    {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }
    /**
     * Seeds the state using all bits of the given long {@code s}.
     * @param s all bits are used, none verbatim (0 is tolerated)
     */
    public final void seed(long s) {
        s -= 0x9E3779B97F4A7C15L;
        s ^= s >>> 13;
        s = (s << 19) - s;
        s ^= s >>> 12;
        s = (s << 17) - s;
        s ^= s >>> 14;
        s = (s << 13) - s;
        stateA = (int)s;
        stateB = (int)(s >>> 32);
        s -= 0x9E3779B97F4A7C15L;
        s ^= s >>> 13;
        s = (s << 19) - s;
        s ^= s >>> 12;
        s = (s << 17) - s;
        s ^= s >>> 14;
        s = (s << 13) - s;
        stateC = (int)s;
        stateD = (int)(s >>> 32);

        for (int i = 0; i < 16; i++) {
            nextInt();
        }
    }

    public int nextInt() {
        final int t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        return t;
    }
    @Override
    public final int next(final int bits)
    {
        final int t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        return t >>> (32 - bits);
    }


    @Override
    public final long nextLong() {

        int t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        final long high = t & 0xFFFFFFFFL;
        t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        return high << 32 | (t & 0xFFFFFFFFL);
    }

    /**
     * Gets a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive).
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public final double nextDouble() {
        int t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        final long high = t & 0xFFFFFFFFL;
        t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        return ((high << 32 | (t & 0xFFFFFFFFL)) & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive).
     * @return a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public final float nextFloat() {
        final int t = stateA + stateB ^ stateD++;
        stateA ^= (stateB << 13 | stateB >>> 19);
        stateB += (stateC << 3 | stateC >>> 29);
        stateC += (t << 25 | t >>> 7);
        return (t & 0xffffff) * 0x1p-24f;
    }

    /**
     * Produces a copy of this Sugar32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Sugar32RNG
     */
    @Override
    public Sugar32RNG copy() {
        return new Sugar32RNG(stateA, stateB, stateC, stateD);
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
     * Sets the current internal state of this Sugar32RNG with four ints, where each can be any int.
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
        return "Sugar32RNG with stateA 0x" + StringKit.hex(stateA) +
                ", stateB 0x" + StringKit.hex(stateB) + ", stateC 0x" + StringKit.hex(stateC)
                + ", stateD 0x" + StringKit.hex(stateD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sugar32RNG sugar32RNG = (Sugar32RNG) o;

        return stateA == sugar32RNG.stateA && stateB == sugar32RNG.stateB &&
                stateC == sugar32RNG.stateC && stateD == sugar32RNG.stateD;
    }

    @Override
    public int hashCode() {
        return 31 * 31 * 31 * stateA + 31 * 31 * stateB + 31 * stateC + stateD | 0;
    }
}
