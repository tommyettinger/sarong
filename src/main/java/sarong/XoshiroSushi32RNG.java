/*  Written in 2018 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A modification of Blackman and Vigna's xoshiro128 generator with a very different "scrambler" than the default; this
 * generator has four 32-bit states and passes 32 TB of PractRand with no anomalies. It is four-dimensionally
 * equidistributed, which is an uncommon feature of a PRNG, and means every output is equally likely not just when one
 * value is generated with {@link #nextInt()}, but also that when up to four 32-bit values are generated and treated as
 * up to a 128-bit output, then all possible 128-bit outputs are equally likely (with the exception of a single 128-bit
 * value that is never produced). The scrambler simply takes the second state variable, adds a number obtained from the
 * golden ratio, phi (0x9E3779B9) to that to get a temporary result, then returns (that result shifted left by 7) minus
 * (that result rotated left by 3). The subtraction-from-shift is where the scrambler gets its name, Sushi; this
 * scrambler is meant to be harder to accidentally weaken than the StarStar or Ara scramblers. It is, however, slower
 * than the Ara, Xara, StarPhi, and StarStar scramblers by almost 20% relative to Ara or 12% relative to StarStar. It
 * may have all sorts of issues since this scrambler hasn't been analyzed much, but 128 bits of state help make most
 * issues less severe. XoshiroSushi32RNG is optimized for GWT, like {@link Starfish32RNG} and
 * {@link XoshiroStarStar32RNG}, which means any non-bitwise math in the source is followed by bitwise math later, and
 * this sometimes can result in obtuse-looking code along the lines of {@code int foo = bar + 0x9E3779B9 | 0;}.
 * <br>
 * This generator does not have one group of vulnerabilities held by the "StarStar" scrambler, where multiplying the
 * result by numbers even somewhat similar to the modulus-2-to-the-32 multiplicative inverse of the last multiplier used
 * in the StarStar scrambler usually results in a binary rank failure in as little as 512MB of PractRand testing. As far
 * as I can tell, that failure occurs in XoshiroStarStar32RNG whenever the output is reliably multiplied by an integer
 * where the last byte is 0x39 (or 57 in decimal), additionally affects at least some multipliers that have their last 7
 * bits equal to 0b0111001 (the same as in 0x39 before, but requiring only 7 bits to be equivalent), and this seems to
 * be related to the choice of rotation amount (the StarStar scrambler rotates by 7 places). This generator may have a
 * similar vulnerability because it also involves a rotation, but it hasn't been easy to find a vulnerability that
 * occurs in practice.
 * <br>
 * <a href="http://xoshiro.di.unimi.it/xoshiro128starstar.c">Original version here for xoshiro128**</a>, by Sebastiano
 * Vigna and David Blackman.
 * <br>
 * Written in 2018 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Xara scrambler written in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class XoshiroSushi32RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB, stateC, stateD;

    /**
     * Creates a new generator seeded using four calls to Math.random().
     */
    public XoshiroSushi32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this XoshiroSushi32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the four
     * parts of state this has.
     * @param seed an int that won't be used exactly, but will affect all components of state
     */
    public XoshiroSushi32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this XoshiroSushi32RNG by dispersing the bits of seed using {@link #setSeed(long)} across the four
     * parts of state this has.
     * @param seed a long that will be split across all components of state
     */
    public XoshiroSushi32RNG(final long seed) {
        setSeed(seed);
    }
    /**
     * Constructs this XoshiroSushi32RNG by calling {@link #setState(int, int, int, int)} on stateA and stateB as
     * given; see that method for the specific details (the states are kept as-is unless they are all 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     * @param stateC the number to use as the third part of the state
     * @param stateD the number to use as the fourth part of the state
     */
    public XoshiroSushi32RNG(final int stateA, final int stateB, final int stateC, final int stateD) {
        setState(stateA, stateB, stateC, stateD);
    }

    @Override
    public final int next(int bits) {
        final int result = stateB + 0x9E3779B9;	        
        final int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
//        return (result ^ result >>> 17) * 3 >>> (32 - bits);
        return (result << 7) - (result << 3 | result >>> 29) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int result = stateB + 0x9E3779B9;
        final int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
//        return (result ^ result >>> 17) * 3 | 0;
        return (result << 7) - (result << 3 | result >>> 29) | 0;
    }

    @Override
    public final long nextLong() {
        int result = stateB + 0x9E3779B9;
        int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
//        long high = (result ^ result >>> 17) * 3;
        long high = (result << 7) - (result << 3 | result >>> 29);
        stateD = (stateD << 11 | stateD >>> 21);
        result = stateB + 0x9E3779B9;
        t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
//        return high << 32 ^ ((result ^ result >>> 17) * 3);
        return high << 32 ^ ((result << 7) - (result << 3 | result >>> 29));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public XoshiroSushi32RNG copy() {
        return new XoshiroSushi32RNG(stateA, stateB, stateC, stateD);
    }

    /**
     * Sets the state of this generator using one int, running it through a GWT-compatible variant of SplitMix32 four
     * times to get four ints of state, all guaranteed to be different.
     * @param seed the int to use to produce this generator's states
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
        z = seed + 0x55EC07FF;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateC = z ^ (z >>> 16);
        z = seed + 0x1D3AB554;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateD = z ^ (z >>> 16);
    }

    /**
     * Sets the state of this generator using one long, running it through a GWT-compatible variant of SplitMix32 four
     * times to get four ints of state, guaranteed to repeat a state no more than two times.
     * @param seed the long to use to produce this generator's states
     */
    public void setSeed(final long seed) {
        int z = (int)((seed & 0xFFFFFFFFL) + 0xC74EAD55);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateA = z ^ (z >>> 16);
        z = (int)((seed & 0xFFFFFFFFL) + 0x8E9D5AAA);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateB = z ^ (z >>> 16);
        z = (int)((seed >>> 32) + 0xC74EAD55);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateC = z ^ (z >>> 16);
        z = (int)((seed >>> 32) + 0x8E9D5AAA);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateD = z ^ (z >>> 16);
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 instead. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateA any int
     */
    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB | stateC | stateD) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateB to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }
    public int getStateC()
    {
        return stateC;
    }

    /**
     * Sets the third part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateC to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateC any int
     */
    public void setStateC(int stateC)
    {
        this.stateC = stateC;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }
    
    public int getStateD()
    {
        return stateD;
    }

    /**
     * Sets the fourth part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateD to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateD any int
     */
    public void setStateD(int stateD)
    {
        this.stateD = stateD;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this XoshiroSushi32RNG with four ints, where each can be any int unless
     * they are all 0 (which will be treated as if stateA is 1 and the rest are 0).
     * @param stateA any int (if all parameters are both 0, this will be treated as 1)
     * @param stateB any int
     * @param stateC any int
     * @param stateD any int
     */
    public void setState(final int stateA, final int stateB, final int stateC, final int stateD)
    {
        this.stateA = (stateA | stateB | stateC | stateD) == 0 ? 1 : stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }
    
    @Override
    public String toString() {
        return "XoshiroSushi32RNG with stateA 0x" + StringKit.hex(stateA) + ", stateB 0x" + StringKit.hex(stateB)
                + ", stateC 0x" + StringKit.hex(stateC) + ", and stateD 0x" + StringKit.hex(stateD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XoshiroSushi32RNG xoshiroSushi32RNG = (XoshiroSushi32RNG) o;

        return stateA == xoshiroSushi32RNG.stateA && stateB == xoshiroSushi32RNG.stateB &&
                stateC == xoshiroSushi32RNG.stateC && stateD == xoshiroSushi32RNG.stateD;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * stateA + stateB) + stateC) + stateD | 0;
    }
}
