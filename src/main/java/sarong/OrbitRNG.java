package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that gives up some speed to gain a much better period and the ability to produce
 * all possible long values over that period.
 * Created by Tommy Ettinger on 7/9/2018.
 */
public final class OrbitRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any long value.
     */
    public long stateA;
    
    public long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public OrbitRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public OrbitRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
    }

    public OrbitRNG(final long seedA, final long seedB) {
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
     * @param stateA a 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
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
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ (s >>> 25)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return (int)(z ^ (z >>> 22)) >>> (32 - bits);
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
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ (s >>> 25)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return z ^ (z >>> 22);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public OrbitRNG copy() {
        return new OrbitRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "OrbitRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrbitRNG orbitRNG = (OrbitRNG) o;

        return stateA == orbitRNG.stateA && stateB == orbitRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }
    
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/ThrustAltRNG > ../../thrustalt_asm.txt
//         */
//        long seed = 1L;
//        ThrustAltRNG rng = new ThrustAltRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        System.out.println(seed);
//    }

}
