package sarong.discouraged;

import sarong.RandomnessSource;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * Inspired by Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>
 * but designed to use just one, GWT-safe, multiplication and some extra xorshifts that aren't often in subcycle
 * generators. It has a period of just under 2 to the 64, 0xFFFE870A6BECE609, which is roughly 2 to the 63.999968, and
 * allows all seeds because there's no clear way of telling whether a generator is off of the ideal cycle.
 * <br>
 * This seems to do well in PractRand testing (32 TB passed with one "unusual" anomaly early on), but this is not a
 * generator Overton tested. "Chaotic" generators like this one tend to score well in PractRand, but it isn't clear if
 * they will fail other tests (in particular, they can't generate all possible long values, and also probably can't
 * generate some ints).
 * <br>
 * Its period is 0xFFFE870A6BECE609 for the largest cycle.
 * <br>
 * This is a RandomnessSource and a StatefulRandomness; it does not currently validate seeds to make sure they are on
 * the largest cycle. Either of the two substates can be on a smaller cycle, but if only one has that happening, then
 * the period should still be at least 4 billion.
 * <br>
 * The name comes from how this adds up, and how it's way of moving (a reference to Mover32RNG, which references M.
 * Overton, who discovered this category of subcycle generators).
 * <br>
 * Created by Tommy Ettinger on 6/26/2019.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class Ladder32RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    private int stateA, stateB;
    public Ladder32RNG()
    {
        stateA = (int)((Math.random() * 2.0 - 1.0) * 0x80000000);
        stateB = (int)((Math.random() * 2.0 - 1.0) * 0x80000000);
    }
    public Ladder32RNG(int state)
    {
        setState(state);
    }

    public Ladder32RNG(final int stateA, final int stateB)
    {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    public final void setState(int state) {
        stateA = state;
        state = ((state *= 0x9E3779B9) ^ (state >>> 16)) * 0x85EBCA6B;
        state = (state ^ state >>> 13) * 0xC2B2AE35;
        stateB = state ^ state >>> 16;
    }

    public final int nextInt()
    {
        stateA += 0xAA78EDD7;
        stateB += 0xC4DE9951;
        final int r = ((stateA = (stateA << 1 | stateA >>> 31)) ^ (stateB = (stateB << 25 | stateB >>> 7))) * 0x9E37B;
        return r ^ r >> 11 ^ r >> 21;
    }
    @Override
    public final int next(final int bits)
    {
        stateA += 0xAA78EDD7;
        stateB += 0xC4DE9951;
        final int r = ((stateA = (stateA << 1 | stateA >>> 31)) ^ (stateB = (stateB << 25 | stateB >>> 7))) * 0x9E37B;
        return (r ^ r >> 11 ^ r >> 21) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        stateA += 0xAA78EDD7;
        stateB += 0xC4DE9951;
        int r = ((stateA = (stateA << 1 | stateA >>> 31)) ^ (stateB = (stateB << 25 | stateB >>> 7))) * 0x9E37B;
        final long t = (r ^ r >> 11 ^ r >> 21) & 0xFFFFFFFFL;
        stateA += 0xAA78EDD7;
        stateB += 0xC4DE9951;
        r = ((stateA = (stateA << 1 | stateA >>> 31)) ^ (stateB = (stateB << 25 | stateB >>> 7))) * 0x9E37B;
        return t << 32 | ((r ^ r >> 11 ^ r >> 21)& 0xFFFFFFFFL);
    }

    /**
     * Produces a copy of this Ladder32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Ladder32RNG
     */
    @Override
    public Ladder32RNG copy() {
        return new Ladder32RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state.
     * @return the "A" part of the state, an int
     */
    public int getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state.
     * @return the "B" part of the state, an int
     */
    public int getStateB()
    {
        return stateB;
    }
    /**
     * Sets the "A" part of the state to any int, which may put the generator in a low-period subcycle.
     * @param stateA any int
     */
    public void setStateA(final int stateA)
    {
        this.stateA = stateA;
    }

    /**
     * Sets the "B" part of the state to any int, which may put the generator in a low-period subcycle.
     * @param stateB any int
     */
    public void setStateB(final int stateB)
    {
        this.stateB = stateB;
    }
    
    @Override
    public String toString() {
        return "Ladder32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ladder32RNG ladder32RNG = (Ladder32RNG) o;

        return stateA == ladder32RNG.stateA && stateB == ladder32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }
}
