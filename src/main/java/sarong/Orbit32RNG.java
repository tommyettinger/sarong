/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A StatefulRandomness that has 2 32-bit states and allows all values for both of them; it has a period of 2 to the 64
 * and passes PractRand tests to 32TB with no anomalies or failures. Speed is unclear at this point, but since this uses
 * an additive recurrence (two big-increment counters, really) to update its states, it may have advantages in loops.
 * <br>
 * Written in 2019 by Tommy Ettinger
 * @author Tommy Ettinger
 */
public final class Orbit32RNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB;

    /**
     * Creates a new Orbit32RNG seeded using two calls to Math.random().
     */
    public Orbit32RNG() {
        stateA = (int)((Math.random() * 2.0 - 1.0) * 0x80000000);
        stateB = (int)((Math.random() * 2.0 - 1.0) * 0x80000000);
    }
    /**
     * Constructs this Orbit32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of
     * state this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public Orbit32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Orbit32RNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public Orbit32RNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this Orbit32RNG with the given stateA and stateB, which will be used as-is.
     * @param stateA the number to use as the first part of the state
     * @param stateB the number to use as the second part of the state
     */
    public Orbit32RNG(final int stateA, final int stateB) {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    @Override
    public final int next(final int bits) {
        final int s = (stateA += 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB += 0x9E3779BB) | 1);
        if(s == 0) stateB -= 0x9E3779BB;
        x = (x ^ x >>> 16) * 0xAC4C1B51;
        return (x ^ x >>> 15) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s = (stateA += 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB += 0x9E3779BB) | 1);
        if(s == 0) stateB -= 0x9E3779BB;
        x = (x ^ x >>> 16) * 0xAC4C1B51;
        return (x ^ x >>> 15);
    }

    @Override
    public final long nextLong() {
        int s = (stateA + 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB += 0x9E3779BB) | 1);
        if(s == 0) stateB -= 0x9E3779BB;
        x = (x ^ x >>> 16) * 0xAC4C1B51;
        final long high = (x ^ x >>> 15);
        s = (stateA += 0x838C9CDA);
        x = (s ^ s >>> 17) * ((stateB += 0x9E3779BB) | 1);
        if(s == 0) stateB -= 0x9E3779BB;
        x = (x ^ x >>> 16) * 0xAC4C1B51;
        return (high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL);
    }

    /**
     * Produces a copy of this Orbit32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Orbit32RNG
     */
    @Override
    public Orbit32RNG copy() {
        return new Orbit32RNG(stateA, stateB);
    }

    /**
     * Sets the state of this generator using one int, running it through Zog32RNG's algorithm two times to get 
     * two ints.
     * @param seed the int to use to produce this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13);
        z = seed + 0x8E9D5AAA;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13);
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

    /**
     * Sets the current internal state of this Orbit32RNG with two ints, where stateA and stateB can each be any int.
     * @param stateA any int
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0; this implementation will treat it as 1.
     */
    @Override
    public void setState(long state) {
        stateA = (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public String toString() {
        return "Orbit2RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Orbit32RNG orbit32RNG = (Orbit32RNG) o;

        return stateA == orbit32RNG.stateA && stateB == orbit32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
}
