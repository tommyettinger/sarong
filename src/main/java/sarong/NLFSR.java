/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A Non-Linear Feedback Shift Register that may be used like a StatefulRandomness but is not truly random. This is
 * based on the {@link LFSR} class, and is less predictable but is otherwise less than optimal in some ways. It has a
 * period of (2 to the 27) minus 1, and uses data from
 * http://people.kth.se/~dubrova/nlfsr.html and https://eprint.iacr.org/2012/314.pdf . You would normally only prefer
 * NLFSR over LFSR if you expect players to scrutinize your randomly-generated data, or if you want to use it as part of
 * a more complex process such as encoding a saved file in a more robust way.
 * It is important to note that an NLFSR or LFSR will produce each number from 1 until its maximum exactly once before
 * repeating, so this may be useful as a way of generating test data in an unpredictable order.
 * <br>
 * There are multiple implementations of NLFSR with different sizes for the produced numbers, which affects their
 * period, and also very different performance qualities. The faster of the two is {@link NLFSR25}, which takes about
 * 2.2x as much time to produce a 25-bit integer as it takes {@link LFSR} to produce a 64-bit number. It has a period of
 * 2 to the 25 minus 1. The slower one is {@link NLFSR27}, which has over 4 times as long of a period, at 2 to the 27
 * minus 1, but also takes more than 6x as long to produce a 27-bit integer as it takes LFSR to produce a 64-bit number.
 * Typically, you should prefer NLFSR25 if speed is even remotely a concern; NLFSR27 is probably the slowest way to
 * generate a 32-bit-or-less value in the whole library.
 * @author Tommy Ettinger
 */
public abstract class NLFSR implements StatefulRandomness, Serializable {

	private static final long serialVersionUID = -1473549048478690391L;

    public long state;

    public abstract int bitLimit();
    /**
     * Creates a new generator seeded using Math.random.
     */
    public NLFSR() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public NLFSR(final CharSequence seed)
    {
        this(CrossHash.hash64(seed));
    }

    public NLFSR(final long seed) {
        setState(seed);
    }

    /**
     * A non-linear feedback shift register with maximal period for 27 bits (2 to the 27 minus 1). It is very complex
     * internally, especially compared to {@link NLFSR25}. If you don't need 27 bits of period, which really isn't very
     * much anyway, you may have better performance with NLFSR25. No maximal-period NLFSRs are publicly known with
     * longer periods than this one that still use only one variable for state, though there are ways to construct much
     * larger NLFSRs by combining existing ones.
     */
    public static class NLFSR27 extends NLFSR {

        public NLFSR27() {
            super();
        }

        public NLFSR27(CharSequence seed) {
            super(seed);
        }

        public NLFSR27(long seed) {
            super(seed);
        }

        @Override
        public final int bitLimit() {
            return 27;
        }

        @Override
        public final int next(int bits) {
            return (int) (nextLong() >>> (27 - bits));
        }

        /**
         * Produces up to 27 bits of random long, with a minimum result of 1 and a max of 134217727 (both inclusive).
         *
         * @return a random long between 1 and 134217727, both inclusive
         */
        @Override
        public final long nextLong() {
            return state = (state >>> 1 | (0x4000000 & (
                    (state << 26) //0
                            ^ (state << 22) //4
                            ^ (state << 18) //8
                            ^ (state << 17) //9
                            ^ (state << 15) //11
                            ^ (state << 14) //12
                            ^ (state << 11) //15
                            ^ (state << 10) //16
                            ^ (state << 3)  //23
                            ^ ((state << 14) & (state << 4)) //12 22
                            ^ ((state << 13) & (state << 3)) //13 23
                            ^ ((state << 13) & (state << 1)) //13 25
                            ^ ((state << 4) & (state << 3))  //22 23
                            ^ ((state << 19) & (state << 18) & (state << 2))  //7 8 24
                            ^ ((state << 14) & (state << 12) & (state))       //12 14 26
                            ^ ((state << 20) & (state << 15) & (state << 7) & (state << 4))       //6 11 19 22

            )));
        }

        /**
         * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
         * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
         * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
         *
         * @return a copy of this RandomnessSource
         */
        @Override
        public NLFSR27 copy() {
            return new NLFSR27(state);
        }
    }
    /**
     * A non-linear feedback shift register with maximal period for 25 bits (2 to the 25 minus 1). It is fairly simple
     * internally, especially compared to {@link NLFSR27}. This is currently suggested as a potential better option over
     * NLFSR27, due to improved performance, unless you need 4 times the period. Both NLFSR25 and NLFSR27 have very
     * small periods for a PRNG to begin with, so they should probably be combined with another generator.
     */
    public static class NLFSR25 extends NLFSR {

        public NLFSR25() {
            super();
        }

        public NLFSR25(CharSequence seed) {
            super(seed);
        }

        public NLFSR25(long seed) {
            super(seed);
        }

        @Override
        public final int bitLimit() {
            return 25;
        }

        @Override
        public final int next(int bits) {
            return (int) (nextLong() >>> (25 - bits));
        }

        /**
         * Produces up to 25 bits of random long, with a minimum result of 1 and a max of 134217727 (both inclusive).
         *
         * @return a random long between 1 and 134217727, both inclusive
         */
        @Override
        public final long nextLong() {
            return state = (state >>> 1 | (0x1000000 & (
                    (state << 24) //0
                            ^ (state << 20) //4
                            ^ (state << 8) //16
                            ^ ((state << 23) & (state << 3) & (state << 1))  //1 21 23
            )));
        }

        /**
         * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
         * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
         * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
         *
         * @return a copy of this RandomnessSource
         */
        @Override
        public NLFSR25 copy() {
            return new NLFSR25(state);
        }
    }
    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Sets the seed of this generator using one long, running that through LightRNG's algorithm twice to get the state.
     * @param seed the number to use as the seed
     */
    @Override
    public void setState(final long seed) {
        state = seed & (-1 >>> (64 - bitLimit()));
        if(state == 0) state = 1;
    }

    @Override
    public String toString() {
        return "NLFSR" + bitLimit() + " with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NLFSR nlfsr = (NLFSR) o;

        return (bitLimit() == nlfsr.bitLimit() && state == nlfsr.state);
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }
}
