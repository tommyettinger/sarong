/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org), ported in 2018 by Tommy Ettinger

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * Don't use this yet; it does very well in early testing but approaches failure at 16TB and 32TB of PractRand testing.
 * It isn't doing enough to mitigate a failure on the DC6 test that xoroshiro suffers from with 32-bit words.
 * <br>
 * A modification of Blackman and Vigna's xoroshiro64** generator; like {@link Starfish32RNG}, but doesn't use any
 * multiplication and instead uses a pair of hard-to-reverse operations (what I'm calling lerosu, left-rotate-subtract;
 * a left shift and a left rotation of a variable, with the latter subtracted from the former). These lerosu operations
 * are arranged so no bits of the state should be directly accessible from the output without serious effort, though it
 * is possible if you have a large enough table of outputs to inputs. It's doing very well in statistical testing (1TB
 * of PractRand with no anomalies; ongoing), but benchmarks in the browser give varied results -- fast in Firefox,
 * but a little slower than similar generators that do use multiplication in recent Chrome. In older Chrome/Chromium,
 * it's totally different, and this generator performs almost twice as quickly as Starfish; if whatever regression in
 * the V8 JavaScript engine causes this can be addressed, SeaSlater could be very fast again in the most common browser.
 * It's slower than Starfish32RNG on desktop platforms, but not by a whole lot, and is comparable in speed to
 * {@link XoshiroAra32RNG} (sometimes a little faster, sometimes a little slower). The main reasons to use SeaSlater are
 * that it offers slightly better security against low-skilled intentional efforts to predict future outputs given past
 * outputs, and that it offers much better promises of keeping its high statistical quality regardless of what simple
 * math operations are applied to its output. Referring to the second reason, generators like {@link XoshiroAra32RNG}
 * fail tests if a specific number is subtracted from every output, while its precursor {@link XoshiroStarStar32RNG}
 * fails tests when every output is multiplied by one of an extremely large group of numbers (2 to the 57 multipliers
 * seem to all be able to break it). As for the first reason, {@link Lobster32RNG} uses the same lerosu operation but
 * only once, yielding roughly 11 bits of its current 64-bit state unchanged in the output (just rotated), so it should
 * be fairly easy to figure out the full state given a small number of full outputs (at least 2, in the worst case that
 * every output can be reversed fully, but more likely 4 to 24).
 * <br>
 * The name comes from the sea creature theme I'm using for this family of generators and the heavily-armored shoreline
 * crustacean called a sea slater or rock louse, which is nicer-looking than its name suggests and 
 * <a href="https://upload.wikimedia.org/wikipedia/commons/4/42/Ligia_oceanica_Flickr.jpg">looks like this</a>. Some of
 * their relatives can roll up into a ball and rotate to escape prey, much like how this generator rotates to escape
 * various issues.
 * <br>
 * <a href="http://xoshiro.di.unimi.it/xoroshiro64starstar.c">Original version here for xoroshiro64**</a>.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class SeaSlater32RNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public SeaSlater32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Lathe32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public SeaSlater32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Lathe32RNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public SeaSlater32RNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this Lathe32RNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public SeaSlater32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }
    
    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = (s0 << 5) - (s0 << 3 | s0 >>> 29);
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 10) - (result << 7 | result >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = (s0 << 5) - (s0 << 3 | s0 >>> 29);
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 10) - (result << 7 | result >>> 25) | 0;
    }

    @Override
    public final long nextLong() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = (s0 << 5) - (s0 << 3 | s0 >>> 29);
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        final int low = (s0 << 5) - (s0 << 3 | s0 >>> 29);
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        final long result = (high << 10) - (high << 7 | high >>> 25);
        return result << 32 ^ ((low << 10) - (low << 7 | low >>> 25));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SeaSlater32RNG copy() {
        return new SeaSlater32RNG(stateA, stateB);
    }

    /**
     * Sets the state of this generator using one int, running it through Zog32RNG's algorithm two times to get 
     * two ints. If the states would both be 0, state A is assigned 1 instead.
     * @param seed the int to use to produce this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55 | 0, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13) | 0;
        z = seed + 0x8E9D5AAA | 0;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13) | 0;
        if((stateA | stateB) == 0)
            stateA = 1;
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and stateB is
     * already 0, this will set stateA to 1 instead, since both states cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateA any int
     */

    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and stateA is
     * already 0, this will set stateA to 1 and stateB to 0, since both cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this Lathe32RNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0 (which will be treated as if stateA is 1 and stateB is 0).
     * @param stateA any int (if stateA and stateB are both 0, this will be treated as 1)
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
        this.stateB = stateB;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return (stateA & 0xFFFFFFFFL) | ((long)stateB) << 32;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0; this implementation will treat it as 1.
     */
    @Override
    public void setState(long state) {
        stateA = state == 0 ? 1 : (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public String toString() {
        return "SeaSlater32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeaSlater32RNG seaSlater32RNG = (SeaSlater32RNG) o;

        if (stateA != seaSlater32RNG.stateA) return false;
        return stateB == seaSlater32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }
}
