/*
Written in 2016 by Chris Doty-Humphrey and ported in 2018 by Tommy Ettinger.

To the extent possible under law, the author of the port has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A port of SFC64 (Small Fast Counting, 64-bit) from the PractRand project by Chris Doty-Humphrey. In the original C++
 * code, it benchmarks very well due to not using any multiplication during number generation (or seeding). It does not
 * perform well in this Java port, due in large part to needing 4 state fields that are all updated with each new random
 * number (fields seem disproportionately expensive to read from or write to in Java, compared to C++). Its guaranteed
 * period length is equal to that of {@link ThrustAltRNG} at 2 to the 64, but SFC64RNG's average period length is about
 * 2 to the 255. In most cases where the longer period and lack of multiplication may be useful, {@link XoRoRNG} does a
 * better job; in any other cases, ThrustAltRNG is going to be faster and is both a {@link StatefulRandomness} and a
 * {@link SkippingRandomness}, giving it more features.
 * <br>
 * The original SFC64 implementation is public domain, and I, Tommy Ettinger, release this Java port into the public
 * domain as well. That doesn't mean I'm encouraging its use... But I would like to see someone improve this generator.
 *
 * @author Chris Doty-Humphrey
 * @author Tommy Ettinger
 */
public final class SFC64RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1111111111111111111L;

    private long a, b, c, counter;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public SFC64RNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public SFC64RNG(final long seed) {
        setSeed(seed);
    }

    @Override
    public final int next(int bits) {
        final long tmp = a + b + counter++;
        a = b ^ (b >>> 11);
        b = c + (c << 3);
        c = ((c << 24) | (c >>> 40)) + tmp;
        return (int) (tmp >>> 64 - bits);
    }

    @Override
    public final long nextLong() {
        final long tmp = a + b + counter++;
        a = b ^ (b >>> 11);
        b = c + (c << 3);
        c = ((c << 24) | (c >>> 40)) + tmp;
        return tmp;
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(final int bound) {
        return (int) ((bound * (nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    public long nextLong(final long n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        for (; ; ) {
            final long bits = nextLong() >>> 1;
            final long value = bits % n;
            if (bits - value + (n - 1) >= 0) {
                return value;
            }
        }
    }

    public boolean nextBoolean() {
        return nextLong() < 0;
    }


    /**
     * Sets the seed of this generator. Passing this 0 will just set it to -1
     * instead. Not the same as the exact state-setting method implementations
     * of StatefulRandomness have, {@code setState()}; this is used to generate
     * 128 bits of state from a 64-bit (non-zero) seed.
     *
     * @param seed the number to use as the seed
     */
    public void setSeed(final long seed) {
        a = b = c = seed;
        counter = 1;
        for (int i = 0; i < 12; i++)
            nextLong();
    }

    @Override
    public String toString() {
        return "SFC64 with state hash 0x" + StringKit.hexHash(a, b, c, counter) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SFC64RNG sfc64RNG = (SFC64RNG) o;
        return a == sfc64RNG.a && b == sfc64RNG.b && c == sfc64RNG.c && counter == sfc64RNG.counter;
    }

    @Override
    public int hashCode() {
        return (int)(31L * (31L * (31L * (a ^ (a >>> 32)) + (b ^ (b >>> 32))) + (c ^ (c >>> 32))) + (counter ^ (counter >>> 32)));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SFC64RNG copy() {
        SFC64RNG next = new SFC64RNG(a);
        next.a = a;
        next.b = b;
        next.c = c;
        next.counter = counter;
        return next;
    }
}
