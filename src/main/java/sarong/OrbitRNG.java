package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A larger-period generator with 128 bits of state, that gives up some speed to gain a much better period and the
 * ability to produce all possible long values over that period (with equal likelihood for each long, but it is only
 * 1-dimensionally equidistributed). Its closest competitor on speed and period is {@link Lathe64RNG}, and its quality
 * should be similar or slightly better than Lathe64. Lathe32 has some troubling anomalies near the end of PractRand
 * testing at 32TB, and there may be issues with Lathe64 then or later on. OrbitRNG is somewhat similar to LightRNG or
 * ThrustAltRNG in implementation, but unlike ThrustAltRNG, OrbitRNG can produce all possible outputs. For some purposes
 * you may want to instead consider {@link TangleRNG}, which also has two states and uses a very similar algorithm, but
 * it skips some work Orbit does and in doing so speeds up a lot and drops its period down to 2 to the 64. An individual
 * TangleRNG can't produce all possible long outputs and can produce some duplicates, but each pair of states for a
 * TangleRNG has a different set of which outputs will be skipped and which will be duplicated. Since it would require
 * months of solid number generation to exhaust the period of a TangleRNG, and that's the only time an output can be
 * confirmed as skipped, it's probably fine for most usage to use many different TangleRNGs, all seeded differently.
 * In other cases you could use one OrbitRNG, {@link DiverRNG} (if you don't mind that it never produces a duplicate
 * output), {@link IsaacRNG} (if speed is less important but more secure output is), or Lathe64RNG, though all of those
 * are probably slower than using many TangleRNG objects.
 * <br>
 * The algorithm here changed on April 20, 2019 after finding the previous set of constants reliably produced
 * "suspicious" or worse results in PractRand at the 32TB mark (although it never failed). With the PractRand arguments
 * {@code -tf 2 -seed 0}, which enables some extra tests and uses a specific seed, OrbitRNG now passes 32TB with no
 * anomalies at any point.
 * <br>
 * The name comes from how the pair of states act like two planets orbiting a star at different rates, and also evokes
 * the larger-scale period relative to {@link TangleRNG}.
 * <br>
 * Created by Tommy Ettinger on 7/9/2018.
 */
public final class OrbitRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 5L;
    /**
     * Can be any long value.
     */
    public long stateA;
    /**
     * Can be any long value.
     */
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
        stateA = (seed = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25);
        stateB =         (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
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
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 27) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        if (s == 0L) stateB -= 0x9E3779B97F4A7C15L;
        return (int)(z ^ (z >>> 27)) >>> (32 - bits);
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
//        final long s = (stateA += 0x6C8E9CF570932BD5L);
//        if(s == 0L)
//            stateB += 0x9E3779B97F4A7C15L;
//        final long z = (s ^ (s >>> 27)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
//        return z ^ (z >>> 25);

        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 27) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        if (s == 0L) stateB -= 0x9E3779B97F4A7C15L;
        return z ^ z >>> 27;
    }
    public final long nextLong1() {
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ (s >>> 27)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return z ^ (z >>> 25);
    }
    public final long nextLong2() {
//        final long b = (stateB += 0x9E3779B97F4A7C15L);
//        final long s = (stateA += b > 0x1000000000000L ? 0x6C8E9CF570932BD5L : 0xD91D39EAE12657AAL);
//        final long z = (s ^ (s >>> 27)) * (b | 1L);
//        return z ^ (z >>> 25);
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z;
        if(s == 0L) z = (s ^ s >>> 27) * ((stateB + 0x9E3779B97F4A7C15L) | 1L);
        else z = (s ^ s >>> 27) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return z ^ z >>> 27;

    }
    public final long nextLong3() {
        final long s = (stateA += (stateB += 0x9E3779B97F4A7C15L) == 0L ? 0x6C8E9CF570932BD5L : 0xD91D39EAE12657AAL);
        final long z = (s ^ (s >>> 27)) * (stateB | 1L);
        return z ^ (z >>> 25);
    }
    public final long nextLong4() {
        long s;
        if((stateB += 0x6C8E9CF570932BD5L) == 0L)
            s = (stateA += 0xD91D39EAE12657AAL);
        else
            s = (stateA += 0x6C8E9CF570932BD5L);
        s = (s ^ (s >>> 27)) * (stateB | 1L);
        return s ^ (s >>> 25);
    }
    public final long nextLong5() {
        if((stateB += 0x6C8E9CF570932BD5L) == 0L)
            stateA += 0xD91D39EAE12657AAL;
        else
            stateA += 0x6C8E9CF570932BD5L;
        return ((stateA ^ stateA >>> 27)) * (stateB | 1L) + (stateB << 23 | stateB >>> 41);
    }
    public final long nextLong6() {
        if((stateB += 0x6C8E9CF570932BD5L) == 0L)
            stateA += 0xD91D39EAE12657AAL;
        else
            stateA += 0x6C8E9CF570932BD5L;
        return ((stateA ^ stateA >>> 27)) * (stateB | 1L) + stateB;
        //return s ^ (s >>> 25);
    }
    public final long nextLong7() {
        final long s = (stateA += 0x9E3779B97F4A7C15L);
        long z;
        if(s == 0L)
            z = (stateB += 0xDA77A83CC694FE14L);
        else
            z = (stateB += 0x6D3BD41E634A7F0AL);
        z *= (s ^ (z >>> 26));
        return z ^ (z + s >>> 24);
    }
    public final long nextLong8() {
        final long s = (stateA += 0x9E3779B97F4A7C15L);
        long z = (stateB += 0x6D3BD41E634A7F0AL);
        z *= (s ^ (z >>> 26));
        return z ^ (z + s >>> 24);
    }
    public final long nextLong9() {
        final long s = (stateA += 0x9E3779B97F4A7C15L);
        if(s == 0L)
            stateB += 0x6C8E9CF570932BD5L;
        final long z = (s ^ (s >>> 27)) * (s + stateB | 0xA529);
        return z ^ (z >>> 25);
    }
    public final long nextLong10() {
        long z = (stateA += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 27)) * (z + stateB | 0xA529);
        return z ^ (z >>> 25);
    }
    public final long nextLong11()
    { 
        long z;
        if(stateB == 0) // -0x91
        {
            z = (stateA *= 0x369DEA0F31A53F85L);
            stateB += 0x9E3779B97F4A7C15L;
        }
        else
            z = (stateA = (stateA * 0x369DEA0F31A53F85L + (stateB += 0x9E3779B97F4A7C15L)));
        return z ^ z >>> 26;
    }
    public final long nextLong12()
    {
        final long z = (stateA = stateA * 0x369DEA0F31A53F85L + (stateB += 0x9E3779B97F4A7C15L));
        return z ^ z >>> 26;
    }
    public final long nextLong13() {
        if((stateB += 0xC6BC279692B5CC85L) != 0L)
            stateA += 0x6C8E9CF570932BD5L;
        final long z = (stateA ^ (stateA >>> 27)) * 0x41C64E6DL + stateB;
        return z ^ (z >>> 25);
    }
    public final long nextLong14(){
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ s >>> 27) * 0x41C64E6DL + (stateB += 0x9E3779B97F4A7C15L);
        return z ^ (z >>> 25);
    }
    public final long nextLong15(){
        if(stateB == 0L)             
            stateA += 0xD91D39EAE12657AAL;
        else
            stateA += 0x6C8E9CF570932BD5L;
        stateB ^= (stateA ^ stateA >>> 27) * 0x41C64E6DL + 0x9E3779B97F4A7C15L;
        return stateB ^ (stateB >>> 25);
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
    
}
