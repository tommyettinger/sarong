package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * Variant on OrbitRNG; a little slower than the default RNGs but with a long period of 2 to the 128 and very high
 * quality.
 * <br>
 * Created by Tommy Ettinger on 4/12/2019.
 */
public final class DuelistRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Can be any long value.
     */
    public long stateA;
    
    public long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public DuelistRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DuelistRNG(long seed) {
        // DiverRNG.determine()
        stateA = seed += (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        // DiverRNG.randomize()
        stateB = (seed = ((seed = (seed ^ (seed << 41 | seed >>> 23) ^ (seed << 17 | seed >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ seed >>> 43 ^ seed >>> 31 ^ seed >>> 23) * 0xDB4F0B9175AE2165L) ^ seed >>> 28;;
    }

    public DuelistRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB;
    }

    /**
     * Get the "A" part of the internal state as a long.
     *
     * @return the current internal state of this object.
     */
    public long getStateA() {
        return stateA;
    }

    /**
     * Set the "A" part of the internal state with a long.
     *
     * @param stateA any 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    /**
     * Get the "B" part of the internal state as a long.
     *
     * @return the current internal "B" state of this object.
     */
    public long getStateB() {
        return stateB;
    }

    /**
     * Set the "B" part of the internal state with a long.
     *
     * @param stateB any 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB;
    }


    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
        final long s = (stateA += 0x9E3779B97F4A7C15L);
        if(s == 0L)
            stateB -= 0x6C8E9CF570932BD5L;
        final long z = (s ^ s >>> 28) * ((stateB += 0x6C8E9CF570932BD5L) | 1L);
        return (int)(z ^ z >>> 28) >>> (32 - bits);
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() { 
        final long b = (stateB += 0x9E3779B97F4A7C15L) | 1L, z;
        if(b <= 0xBC6EF372FE94F82CL) stateA += 0x6C8E9CF570932BD5L;
        z = (stateA ^ stateA >> 28) * b;
        return z ^ z >> 28;
//        final long s = (stateA += 0x9E3779B97F4A7C15L);
//        if(s == 0L)
//            stateB -= 0x6C8E9CF570932BD5L;
//        final long z = (s ^ s >>> 28) * ((stateB += 0x6C8E9CF570932BD5L) | 1L);
//        return z ^ z >>> 28;

    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DuelistRNG copy() {
        return new DuelistRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "DuelistRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DuelistRNG duelistRNG = (DuelistRNG) o;

        return stateA == duelistRNG.stateA && stateB == duelistRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ stateA >>> 32) + (stateB ^ stateB >>> 32));
    }
    
}
