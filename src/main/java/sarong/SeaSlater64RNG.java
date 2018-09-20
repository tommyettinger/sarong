/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org), ported in 2018 by Tommy Ettinger

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A modification of Blackman and Vigna's xoroshiro128** generator; like {@link Starfish32RNG}, but doesn't use any
 * multiplication and instead uses a pair of hard-to-reverse operations (what I'm calling lerosu, left-rotate-subtract;
 * a left shift and a left rotation of a variable, with the latter subtracted from the former). These lerosu operations
 * are arranged so no bits of the state should be directly accessible from the output without serious effort, though it
 * is possible if you have a large enough table of outputs to inputs. It passes PractRand with 3 anomalies over 32TB of
 * testing, none more serious than "unusual," and is two-dimensionally equidistributed (unlike {@link XoRoRNG} and
 * {@link Lathe64RNG}, which are only one-dimensionally equidistributed). The main reasons to use SeaSlater are that it
 * offers slightly better security against low-skilled intentional efforts to predict future outputs given past outputs,
 * and that it offers much better promises of keeping its high statistical quality regardless of what simple math
 * operations are applied to its output. Referring to the second reason, generators like {@link XoshiroAra32RNG} fail
 * tests if a specific number is subtracted from every output, while its precursor {@link XoshiroStarStar32RNG} fails
 * tests when every output is multiplied by one of an extremely large group of numbers (2 to the 57 multipliers seem to
 * all be able to break it). As for the first reason, {@link Lobster32RNG} uses the same lerosu operation but
 * only once, yielding roughly 11 bits of its current 64-bit state unchanged in the output (just rotated), so it should
 * be fairly easy to figure out the full state of a Lobster32RNG given a small number of full outputs (at least 2, in
 * the worst case that every output can be reversed fully, but more likely between 4 and 24). A lerosu operation seems
 * like it should be an irreversible random mapping because there's no convenient math to get the previous state from a
 * subsequent state, but it is a 1-to-1 mapping so it is technically reversible. Actually getting the state of a
 * SeaSlater64RNG from its full outputs (from {@link #nextInt()}, {@link #nextLong()}, or {@link #next(int)} iff the
 * parameter is 32) is a challenge, and would normally require a large table of known reversals and enough time to
 * encounter a pair of immediately adjacent outputs that are each in the table.
 * <br>
 * The name comes from the sea creature theme I'm using for this family of generators and the heavily-armored shoreline
 * crustacean called a sea slater or rock louse, which is nicer-looking than its name suggests and 
 * <a href="https://upload.wikimedia.org/wikipedia/commons/4/42/Ligia_oceanica_Flickr.jpg">looks like this</a>. Some of
 * their relatives can roll up into a ball and rotate to escape prey, much like how this generator rotates to escape
 * various issues.
 * <br>
 * <a href="http://xoshiro.di.unimi.it/xoroshiro128starstar.c">Original version here for xoroshiro128**</a>.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class SeaSlater64RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private long stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public SeaSlater64RNG() {
        setState((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    /**
     * Constructs this SeaSlater64RNG by using LightRNG's algorithm (SplitMix64) to generate two different values for
     * the two parts of state this has with {@link #setSeed(long)}.
     * @param seed a long that will be used to generate both components of state
     */
    public SeaSlater64RNG(final long seed) {
        setSeed(seed);
    }
    /**
     * Constructs this SeaSlater64RNG by calling {@link #setState(long, long)} on stateA and stateB as given; see that
     * method for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public SeaSlater64RNG(final long stateA, final long stateB) {
        setState(stateA, stateB);
    }
    
    @Override
    public final int next(int bits) {
        final long s0 = stateA;
        final long s1 = stateB ^ s0;
        final long result = (s0 << 7) - (s0 << 5 | s0 >>> 59);
        stateA = (s0 << 24 | s0 >>> 40) ^ s1 ^ (s1 << 16);
        stateB = (s1 << 37 | s1 >>> 27);
        return (int) ((result << 12) - (result << 9 | result >>> 55) >>> (64 - bits));
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final long s0 = stateA;
        final long s1 = stateB ^ s0;
        final long result = (s0 << 7) - (s0 << 5 | s0 >>> 59);
        stateA = (s0 << 24 | s0 >>> 40) ^ s1 ^ (s1 << 16);
        stateB = (s1 << 37 | s1 >>> 27);
        return (int) ((result << 12) - (result << 9 | result >>> 55) >>> 32);
    }

    @Override
    public final long nextLong() {
        final long s0 = stateA;
        final long s1 = stateB ^ s0;
        final long result = (s0 << 7) - (s0 << 5 | s0 >>> 59);
        stateA = (s0 << 24 | s0 >>> 40) ^ s1 ^ (s1 << 16);
        stateB = (s1 << 37 | s1 >>> 27);
        return (result << 12) - (result << 9 | result >>> 55);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SeaSlater64RNG copy() {
        return new SeaSlater64RNG(stateA, stateB);
    }

    /**
     * Sets the state of this generator using one long, running it through LightRNG's algorithm two times to get 
     * two longs. If the states would both be 0, state A is assigned 1 instead.
     * @param seed the long to use to produce this generator's state
     */
    public void setSeed(final long seed) {
        long z = seed;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        stateA = z ^ (z >>> 31);
        z = (seed + 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        stateB = z ^ (z >>> 31);
        // not possible with SplitMix64
//        if((stateA | stateB) == 0)
//            stateA = 1;
    }

    public long getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and stateB is
     * already 0, this will set stateA to 1 instead, since both states cannot be 0 at the same time. Usually, you
     * should use {@link #setState(long, long)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateA any int
     */

    public void setStateA(long stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public long getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and stateA is
     * already 0, this will set stateA to 1 and stateB to 0, since both cannot be 0 at the same time. Usually, you
     * should use {@link #setState(long, long)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateB any int
     */
    public void setStateB(long stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this SeaSlater64RNG with three ints, where stateA and stateB can each be any
     * int unless they are both 0 (which will be treated as if stateA is 1 and stateB is 0).
     * @param stateA any int (if stateA and stateB are both 0, this will be treated as 1)
     * @param stateB any int
     */
    public void setState(long stateA, long stateB)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
        this.stateB = stateB;
    }
    
    @Override
    public String toString() {
        return "SeaSlater32RNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeaSlater64RNG seaSlater64RNG = (SeaSlater64RNG) o;

        if (stateA != seaSlater64RNG.stateA) return false;
        return stateB == seaSlater64RNG.stateB;
    }

    @Override
    public int hashCode() {
        final long h = (31L * stateA + stateB);
        return (int)(h ^ h >>> 32);
    }
}
