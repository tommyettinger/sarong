/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;


/**
 * A modification of Blackman and Vigna's xoroshiro128 generator using 32-bit math and 3 states instead of 64-bit math
 * and two states; it is close to being two-dimensionally equidistributed where xoroshiro128+ is close to being
 * one-dimensionally equidistributed. This is a modified version of {@link Churro32RNG} that uses an XLCG instead of a
 * Weyl sequence, and even though XLCGs and xoroshiro128 have the same weakness to binary rank tests, simply xorshifting
 * the XLCG twice and adding that to the XOR of the two xoroshiro states is enough to pass at least 2TB of PractRand
 * with only one (minor) anomaly. Strangely, the XLCG used doesn't seem to matter very much to the statistical quality;
 * the one used is {@code x = (x ^ 0x9E3779BD) * 3;}, and multiplying by 3 can be optimized by HotSpot into a shift and
 * add. Testing may reveal flaws in the days ahead, and it is unclear if this will fail tests when they are run on the
 * reversed output of DizzyRNG. The period of Dizzy32RNG is the product of the periods of the 32-bit variant of
 * xoroshiro and the period of a 32-bit XLCG, 0xFFFFFFFFFFFFFFFF00000000 .
 * <br>
 * The name comes from the rotations xoroshiro uses, the di- prefix referring to the two types of generator, and how
 * bewildered I am at what passes and what doesn't pass PractRand with even minor tweaks. 
 * <br>
 * <a href="http://xoroshiro.di.unimi.it/xoroshiro128plus.c">Original version here for xorshiro128+</a>; this version
 * uses <a href="https://groups.google.com/d/msg/prng/Ll-KDIbpO8k/bfHK4FlUCwAJ">different constants</a> by the same
 * author, Sebastiano Vigna. This was also inspired by the 2D equidistribution work done by Christopher Rutz,
 * <a href="https://gist.github.com/XoroshiroNOT/bd393b7e86739ef5a807cddcf310441c">available on Gist</a>. Some of the
 * code used to evaluate possible algorithms would not have been possible without the xoroshiro constant triplets that
 * Rutz posted for various word sizes, so thanks are in order.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class Dizzy32RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB, stateC;

    /**
     * Creates a new generator seeded using three calls to Math.random().
     */
    public Dizzy32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Churro32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public Dizzy32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Churro32RNG by calling {@link #setState(int, int, int)} on stateA and stateB as given but
     * producing stateC via {@code stateA ^ stateB}; see that method for the specific details (stateA and stateB are
     * kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public Dizzy32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB, stateA ^ stateB);
    }

    /**
     * Constructs this Churro32RNG by calling {@link #setState(int, int, int)} on the arguments as given; see that
     * method for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     * @param stateC the number to use as the counter part of the state (third part)
     */
    public Dizzy32RNG(final int stateA, final int stateB, final int stateC) {
        setState(stateA, stateB, stateC);
    }

    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        stateA = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        int s2 = (stateC = (stateC ^ 0x9E3779BD) * 3 | 0);
        s2 ^= s2 >>> 16;
        return ((s2 ^ s2 >>> 15) + s1) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        stateA = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        int s2 = (stateC = (stateC ^ 0x9E3779BD) * 3 | 0);
        s2 ^= s2 >>> 16;
        return ((s2 ^ s2 >>> 15) + s1);
    }
    
    public final int nextInt2() {
        final int s0 = stateA;
        int s1 = stateB ^ s0;
        stateA = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        final int s2 = (stateC = (stateC ^ 0x9E3779BD) * 3 | 0);
        s1 += s2 ^ s2 >>> 16;
        return (s1 ^ s1 >>> 15);
    }
    
    @Override
    public final long nextLong() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        int s2 = (stateC = (stateC ^ 0x9E3779BD) * 3 | 0);
        s2 ^= s2 >>> 16;
        final int t0 = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        final int t1 = (s1 << 13 | s1 >>> 19) ^ t0;
        final long result = ((s2 ^ s2 >>> 15) + s1);
        stateA = (t0 << 26 | t0 >>>  6) ^ t1 ^ (t1 << 9);
        stateB = (t1 << 13 | t1 >>> 19);
        s2 = (stateC = (stateC ^ 0x9E3779BD) * 3 | 0);
        s2 ^= s2 >>> 16;
        return result << 32 ^ ((s2 ^ s2 >>> 15) + t1);

    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Dizzy32RNG copy() {
        return new Dizzy32RNG(stateA, stateB, stateC);
    }

    /**
     * Sets the state of this generator using one int, running it through a modified SplitMix32 algorithm three times to
     * get three ints.
     * @param seed the int to use to assign this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateA = z ^ (z >>> 16);
        z = seed + 0x8E9D5AAA;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateB = z ^ (z >>> 16);
        if((stateA | stateB) == 0)
            stateA = 1;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateC = z ^ (z >>> 16);
    }

    public int getStateA()
    {
        return stateA;
    }
    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }
    public int getStateC()
    {
        return stateC;
    }
    public void setStateC(int stateC)
    {
        this.stateC = stateC;
    }

    /**
     * Sets the current internal state of this Dizzy32RNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0, and stateC can be any int without restrictions.
     * @param stateA any int; if stateA and stateB are both 0 this will be treated as 1
     * @param stateB any int
     * @param stateC any int
     */
    public void setState(int stateA, int stateB, int stateC)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
        this.stateB = stateB;
        this.stateC = stateC;
    }
    @Override
    public String toString() {
        return "Dizzy32RNG with stateA 0x" + StringKit.hex(stateA) + ", stateB 0x" + StringKit.hex(stateB) + ", and stateC 0x" + StringKit.hex(stateC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dizzy32RNG dizzy32RNG = (Dizzy32RNG) o;

        return stateA == dizzy32RNG.stateA && stateB == dizzy32RNG.stateB && stateC == dizzy32RNG.stateC;
    }

    @Override
    public int hashCode() {
        return (31 * (31 * stateA + stateB) + stateC) | 0;
    }
}
