package sarong.discouraged;

import sarong.RandomnessSource;
import sarong.util.StringKit;

import java.io.Serializable;


/**
 * A modification of Blackman and Vigna's xoroshiro128 generator using 32-bit math and 3 states instead of 64-bit math
 * and two states; it is close to being two-dimensionally equidistributed where xoroshiro128+ is close to being
 * one-dimensionally equidistributed.
 * In statistical testing, xoroshiro128 (which returns one state unchanged) and xoroshiro128+ (which returns the sum of
 * both states) always fail some binary matrix rank tests, but smaller-state versions fail other tests as well. The
 * changes Churro makes apply only to the output of xoroshiro, not its well-tested state transition for the "xoroshiro
 * state" part of this generator, and these changes eliminate all statistical failures on 32TB of tested data, avoiding
 * the failures the small-state variant of xoroshiro suffers on BCFN, DC6, and FPF. It avoids multiplication, like
 * xoroshiro and much of the xorshift family of generators, and any arithmetic it performs is safe for GWT. Churro works
 * by running a "Weyl sequence," essentially a large-increment counter that overflows and wraps frequently, in tandem to
 * the xoroshiro state transition, and the result is obtained by XORing the two xoroshiro states, rotating that by the
 * random-like upper-five bits of the Weyl counter, then XORing that with the full Weyl counter. The period is also
 * improved by incorporating the Weyl sequence, up to 0xFFFFFFFFFFFFFFFF00000000 .
 * <br>
 * The name comes from the dessert foods called churros that rotate in frying oil and combine the separate flavors of
 * oil and cinnamon-sugar, akin to how this combines two different state transitions with different periods. It also
 * sounds like "xoro."
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
public final class Churro32RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB, stateC;

    /**
     * Creates a new generator seeded using three calls to Math.random().
     */
    public Churro32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Churro32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public Churro32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Churro32RNG by calling {@link #setState(int, int, int)} on stateA and stateB as given but
     * producing stateC via {@code stateA ^ stateB}; see that method for the specific details (stateA and stateB are
     * kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public Churro32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB, stateA ^ stateB);
    }

    /**
     * Constructs this Churro32RNG by calling {@link #setState(int, int, int)} on the arguments as given; see that
     * method for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     * @param stateC the number to use as the counter part of the state (third part)
     */
    public Churro32RNG(final int stateA, final int stateB, final int stateC) {
        setState(stateA, stateB, stateC);
    }

    @Override
    public final int next(int bits) {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int s2 = (stateC = stateC + 0x9E3779BD | 0);
        stateA = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        s0 = s2 >>> 27;
        return ((s1 << s0 | s1 >>> -s0) ^ s2) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int s2 =  (stateC = stateC + 0x9E3779BD | 0);
        stateA = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        s0 = s2 >>> 27;
        return ((s1 << s0 | s1 >>> -s0) ^ s2);
    }

    @Override
    public final long nextLong() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int s2 =  (stateC + 0x9E3779BD | 0);
        final int t0 = (s0 << 26 | s0 >>>  6) ^ s1 ^ (s1 << 9);
        final int t1 = (s1 << 13 | s1 >>> 19) ^ t0;
        s0 = s2 >>> 27;
        final long result = ((s1 << s0 | s1 >>> -s0) ^ s2);
        final int t2 =  (stateC = stateC + 0x3C6EF37A | 0);
        stateA = (t0 << 26 | t0 >>>  6) ^ t1 ^ (t1 << 9);
        stateB = (t1 << 13 | t1 >>> 19);
        s0 = t2 >>> 27;
        return result << 32 ^ ((t1 << s0 | t1 >>> -t0) ^ t2);

    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Churro32RNG copy() {
        return new Churro32RNG(stateA, stateB, stateC);
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
     * Sets the current internal state of this Churro32RNG with three ints, where stateA and stateB can each be any int
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
        return "Churro32RNG with stateA 0x" + StringKit.hex(stateA) + ", stateB 0x" + StringKit.hex(stateB) + ", and stateC 0x" + StringKit.hex(stateC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Churro32RNG churro32RNG = (Churro32RNG) o;

        return stateA == churro32RNG.stateA && stateB == churro32RNG.stateB && stateC == churro32RNG.stateC;
    }

    @Override
    public int hashCode() {
        return (31 * (31 * stateA + stateB) + stateC) | 0;
    }
}
