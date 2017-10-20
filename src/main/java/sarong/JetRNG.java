package sarong;

import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * High-quality, high-period, faster than several other high-period generators, and uses int math.
 * Effectively an improved-quality version of HerdRNG, with comparable speed.
 * <br>
 * Created by Tommy Ettinger on 6/14/2017.
 */
public final class JetRNG implements RandomnessSource, Serializable {
    /*
// Thanks umireon! This is CC0 licensed code in this comment block.
// https://github.com/umireon/my-random-stuff/blob/master/xorshift/splitmix32.c
// Written in 2016 by Kaito Udagawa
// Released under CC0 <http://creativecommons.org/publicdomain/zero/1.0/>
// [1]: Guy L. Steele, Jr., Doug Lea, and Christine H. Flood. 2014. Fast splittable pseudorandom number generators. In Proceedings of the 2014 ACM International Conference on Object Oriented Programming Systems Languages & Applications (OOPSLA '14). ACM, New York, NY, USA, 453-472.
uint32_t splitmix32(uint32_t *x) {
  uint32_t z = (*x += 0x9e3779b9);
  z = (z ^ (z >> 16)) * 0x85ebca6b;
  z = (z ^ (z >> 13)) * 0xc2b2ae35;
  return z ^ (z >> 16);
}
     */


    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with {@code thrust32(state += 0x7F4A7C15)} (-= to go backwards).
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code thrust32(state += 0x7F4A7C15)} is recommended to go forwards or
     *              {@code thrust32(state -= 0x7F4A7C15)} to generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    private static int thrust32(int state)
    {
        state = (state ^ state >>> 14) * (0x41C64E6D + (state & 0x7FFE));
        return state ^ state >>> 13;
    }

    private static final long serialVersionUID = 1L;
    public final int[] state = new int[16];
    public int choice = 0;
    public JetRNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public JetRNG(final int seed) {
        setState(seed);
    }


    public JetRNG(final long seed) {
        setState(seed);
    }

    public JetRNG(final int[] seed) {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 16; i++) {
                choice += (state[i] = thrust32(0x632D978F + i * 0x7F4A7C15));
            }
        } else if (len < 16) {
            for (int i = 0, s = 0; i < 16; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= thrust32(seed[s] + i * 0x7F4A7C15));
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 15) {
                choice += (state[i] ^= seed[s]);
            }
        }
    }

    /**
     * Uses the given String or other CharSequence as the basis for the 64 ints this uses as state, assigning choice to
     * be the sum of the rest of state.
     * Internally, this gets a 32-bit hash for seed with 48 different variations on the {@link CrossHash.Mist} hashing
     * algorithm and 16 variations on the {@link CrossHash.Storm} algorithm, and uses one for each int in state. This
     * tolerates null and empty-String values for seed.
     * @param seed a String or other CharSequence; may be null
     */
    public JetRNG(final CharSequence seed)
    {
        for (int i = 0; i < 16; i++) {
            choice += (state[i] = CrossHash.Mist.predefined[i].hash(seed));
        }
    }

    public void setState(final int seed) {
        choice = 0;
        for (int i = 0; i < 16; i++) {
            choice += (state[i] = thrust32(seed + i * 0x7F4A7C15));
        }
    }

    public void setState(final long seed) {
        choice = 0;
        for (int i = 0; i < 16; i++) {
            choice += (state[i] = (int)LightRNG.determine(seed + i));
        }
    }

    public void setState(final int[] seed)
    {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 16; i++) {
                choice += (state[i] = thrust32(0x632D978F + i * 0x7F4A7C15));
            }
        } else if (len < 16) {
            for (int i = 0, s = 0; i < 16; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= thrust32(seed[s] + i * 0x7F4A7C15));
            }
        } else {
            if (len == 16) {
                choice = 0;
                for (int i = 0; i < 16; i++) {
                    choice += (state[i] = seed[i]);
                }
            } else {
                for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 15) {
                    choice += (state[i] ^= seed[s]);
                }
            }
        }
    }

//    final int calibrate(final int inc, final int chooser) {
//        return (state[choice & 63] += (state[choice >>> 27] + inc >>> 1) + (choice += chooser));
//    }

    @Override
    public final long nextLong() {
        //final int x = (choice + 0x7F4A7C15), y = (choice += 0xFE94F82A);
//        x = (state[x >>> 28] += (x ^ x >>> 14)) * 0x2C9277B5;
//        y = (state[y >>> 28] += (y ^ y >>> 14)) * 0x2C9277B5;
//        return (long) (state[x >>> 28] += ((state[x & 15] += x) >>> 14) * 0x2C9277B5) << 32
//                ^ (state[y >>> 28] += ((state[y & 15] += y) >>> 14) * 0x2C9277B5);
//        return (long)((state[(choice += 0x7F4A7C15) >>> 28] += (state[choice & 15] += choice) >>> 13) * 0x2C9277B5) << 32
//                ^ ((state[(choice += 0x7F4A7C15) >>> 28] += (state[choice & 15] += choice) >>> 13) * 0x2C9277B5);
        //  return ((state[choice >>> 28] += (choice ^ choice >>> 14) * 0x2C9277B5) >>> 13 ^ (choice += 0x7F4A7C15)) * 0x5F35649500000000L ^
        //          ((state[choice >>> 28] += (choice ^ choice >>> 14) * 0x2C9277B5) >>> 13 ^ (choice += 0x7F4A7C15)) * 0x5F356495;
        return (state[(choice += 0x9CBC276D) & 15] += (state[choice >>> 28] >>> 13) + 0x5F356495) * 0x2C9277B500000000L ^
                (state[(choice += 0x9CBC276D) & 15] += (state[choice >>> 28] >>> 13) + 0x5F356495) * 0x2C9277B5;
    }
    public final int nextInt() {
//        final int z = (choice += 0x7F4A7C15),
//                s = (state[z >>> 28] += (z ^ z >>> 14)) * 0x2C9277B5; //[(z >>> 1) * 89 >>> 28]
//        return (s ^ s >>> 13);
//        final int z = (choice += 0x7F4A7C15);
//        return (state[z >>> 28] += ((state[z & 15] += z) >>> 14) * 0x2C9277B5);
        //return (state[(choice += 0x7F4A7C15) >>> 28] += (state[choice & 15] += choice) >>> 13) * 0x2C9277B5;
        //return ((state[choice >>> 28] += (choice ^ choice >>> 14) * 0x2C9277B5) >>> 13 ^ (choice += 0x7F4A7C15)) * 0x5F356495;
        //  return (state[choice >>> 28] += ((state[choice & 15] *= (choice += 0x7F4A7C15)) >>> 14)) * 0x2C9277B5;
        return (state[(choice += 0x9CBC276D) & 15] += (state[choice >>> 28] >>> 13) + 0x5F356495) * 0x2C9277B5;
    }
    @Override
    public final int next(final int bits) {
//        return (state[(choice += 0x7F4A7C15) >>> 28] += (state[choice & 15] += choice) >>> 13) * 0x2C9277B5 >>> (32 - bits); // 0x5F356495
        //return ((state[choice >>> 28] += (choice ^ choice >>> 15) * 0x2C9277B5) >>> 13) * (choice ^ (choice += 0x7F4A7C15)) >>> (32 - bits); // 0x5F356495
        //return ((state[c >>> 28] += (c ^ c >>> 14) * 0x2C9277B5) >>> 13) * ((choice += 0x7F4A7C15)|1) >>> (32 - bits);
        //return ((state[choice >>> 28] += (choice ^ choice >>> 14) * 0x2C9277B5) >>> 13 ^ (choice += 0x7F4A7C15)) * 0x5F356495 >>> (32 - bits);
        //  return (state[choice >>> 28] += ((state[choice & 15] += (choice += 0x7F4A7C15)) >>> 14)) * 0x2C9277B5 >>> (32 - bits);
        return (state[(choice += 0x9CBC276D) & 15] += (state[choice >>> 28] >>> 13) + 0x5F356495) * 0x2C9277B5 >>> (32 - bits);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        JetRNG br = new JetRNG(state);
        br.choice = choice;
        return br;
    }

    @Override
    public String toString() {
        return "JetRNG{" +
                "state=" + StringKit.hex(state) +
                ", choice=" + choice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JetRNG jetRNG = (JetRNG) o;

        return choice == jetRNG.choice && Arrays.equals(state, jetRNG.state);
    }

    @Override
    public int hashCode() {
        return 31 * choice + CrossHash.hash(state);
    }
}
