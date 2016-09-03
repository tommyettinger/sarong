package sarong.rng;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * Like LightRNG, but shares a lot in common with CrossHash's hashing mechanism. The name comes from its
 * similarity to the nickname for that hash, Lightning, but also to how the current version acts like LightRNG,
 * sort-of, but involves a thunder-like "echo" where the earlier results are used as additional state for the
 * next result. Why should you consider it? It appears to be the fastest RandomnessSource we have available,
 * and is the only RNG in the library that can generate 1 billion random long values in under 1 second (or
 * rather, under 900 ms) on an Intel i7-4700MQ laptop processor (second-fastest RandomnessSource depends on
 * other factors, but is effectively a tie between LightRNG and XoRoRNG at roughly 1200 ms on the same laptop).
 * Any layer on top of generating long values slows this down, which is the case for most of the
 * RandomnessSource implementations, but ThunderRNG's {@link #nextInt()} method, which gets the most
 * significant 32 bits of a random long and returns them as an int, is also the fastest way we have to generate
 * int values. This does not implement StatefulRandomness because it stores state in two parts, each a long;
 * each is incremented by a different addend with each number generated. Part B is always odd, and is
 * incremented by a large, empirically-chosen number that is even; because odd + even = odd, always, part B
 * never becomes even. Part A is always incremented by an irregular selection of the bits in Part B, but the
 * selection never causes the increment to be by an even number (this also means it never increments by 0).
 * This irregular increment seems to increase the period, but by how much is not clear.
 * <br>
 * The reason why nextInt() uses only the most significant half of the bits, even though it requires a shift in
 * addition to a cast, is because the period of the less significant bits is lower, though by how much isn't
 * clear. One early test used a 512x512 pixel window with a call to ThunderRNG's next() method for each pixel
 * (2 to the 18 calls per render), getting only two bits at time (yes, this is wasteful and a bad idea in
 * practice). Though the seed wasn't being reset each frame, the generated 2-bit values were effectively
 * identical between frames (adding one additional call to next() made the random sections move predictably
 * along one axis, one pixel at a time, which indicates they would be the same values every frame if the extra
 * call was removed). The early version that had this likely flaw always took the lowest bits, here the lowest
 * two bits, in next(), but changing next() to use the same number of bits, but from higher in the random long,
 * eliminated this issue. Calls to nextLong() should still be expected to have a lower-than-normal period for
 * the low bits, with the bottom 2 bits likely having a period of 4096 or less. The period of the full 64
 * bits is unknown at this time, but is probably higher than 2 to the 64, and is almost certainly at least 2 to
 * the 63 (which is the probable period of Part B on its own, and because Part A changes every time by a
 * random-seeming, non-zero subset of Part B where the LSB is always set, the final result can't have a lower
 * period than Part B).
 * <br>
 * The tool used for testing this RNG is PractRand, http://pracrand.sourceforge.net/ > The binaries it provides
 * don't seem to work as intended on Windows, so I built from source, generated 64MB files of random 64-bit
 * output with various generators as "Thunder.dat", "Light.dat" and so on, then ran the executables I had
 * built with the MS compilers, with the command line {@code RNG_test.exe stdin64 < Thunder.dat} . For most of
 * the other generators I tried, there were no or nearly-no statistical failures it could find, and as of the
 * commit on September 3, 2016, ThunderRNG also has no statistical failures or even anomalies. Earlier versions
 * were slightly faster (at best speed, 600-700ms) but had multiple outright failures (the fastest ones failed
 * the majority of tests).
 * <br>
 * Created by Tommy Ettinger on 8/23/2016.
 */
public class ThunderRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 3L;

    /** The state can be seeded with any value. */
    public long state;
    protected long jumble;
    /** Creates a new generator seeded using Math.random. */
    public ThunderRNG() {
        this((long) (Math.random() * Long.MAX_VALUE));
    }

    public ThunderRNG( final long seed ) {
        state = (seed + bitPermute(seed + 0xC6BC279692B5CC83L)) * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L;
        jumble = (state + bitPermute(state + 0x9E3779B97F4A7C15L)) * 0xC6BC279692B5CC83L + 0x632BE59BD9B4E019L | 1L;
        /*
        jumble = (seed ^ ((seed + 0x9E3779B97F4A7C15L) >> 18)) * 0xC6BC279692B5CC83L;
        jumble ^= (((seed + 0x9E3779B97F4A7C15L) ^ ((seed + 0x9E3779B97F4A7C15L + 0x9E3779B97F4A7C15L) >> 18)) * 0xC6BC279692B5CC83L) >>> 32;
        jumble |= 1L;
        */
    }
    public ThunderRNG(final long partA, final long partB)
    {
        state = partA;
        jumble = partB | 1L;
    }

    /**
     * Not needed for external use, but it may be handy in code that needs to alter a long in some random-seeming way.
     * Passing 0 to this yields 0. May actually change the number of set bits, so it isn't quite a permutation in the.
     * @param p a number that should have its bits permuted, as a long
     * @return a permuted-bits version of p, as a long
     */
    public static long bitPermute(long p)
    {
        p ^= p >>> (5 + (p >>> 59));
        p *= 0xAEF17502108EF2D9L;
        return p ^ (p >>> 43);
    }
    @Override
    public int next( int bits ) {
        //return (int)( nextLong() & ( 1L << bits ) - 1 );
        return (int)( nextLong() >>> (64 - bits) );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {

        //0x632BE59BD9B4E019L    0xD0E89D2D311E289FL        0x3195F2CDECDA700CL
        //return (((((0x632BE59BD9B4E019L + state) >>> 7)) * (state += 0x9E3779B97F4A7C15L)) >> 5) * 0xC6BC279692B5CC83L;
        //final long z = state; return state ^ Long.rotateLeft((z + 0xC6BC279692B5CC83L) * (state += 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
        //return (state = ((state >>> 2) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        //return ((state << 4L) + 0xC6BC279692B5CC83L) * ((state += 0x9E3779B97F4A7C15L) >>> 5) + 0x632BE59BD9B4E019L;
        //return ((state += 0x9E3779B97F4A7C15L) >> 5) * 0xD0E89D2D311E289FL + 0x632BE59BD9B4E019L;
        //return 0xC6BC279692B5CC83L * (state = ((state + 0xD0E89D2D311E289FL) >> 5) * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L;
        //return ((((state += 0x9E3779B97F4A7C15L) >> 13) * 0xD0E89D2D311E289FL) >> 9) * 0xC6BC279692B5CC83L;
        //return (state << 13L) + ((state += 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >> 16L);
        //return ((state += 0x9E3779B97F4A7C15L) >> (state & 28L)) * 0xC6BC279692B5CC83L;
        //return ((state += 0x9E3779B97F4A7C15L) >> 13) ^ ((state *= 0xD0E89D2D311E289FL) >>> 9);
        //return ((state += 0x9E3779B97F4A7C15L) >> (state >>> 60)) * 0xD0E89D2D311E289FL;
        //return Long.rotateRight((state += 0x9E3779B97F4A7C15L), (int)(state >>> 58)) * 0xC6BC279692B5CC83L;

        //return ((state << 4L) + 0xC6BC279692B5CC83L) * ((state += 0x9E3779B97F4A7C15L) >>> 5) + 0x632BE59BD9B4E019L;
        //return 0xD0E89D2D311E289FL * ((state += 0x9E3779B97F4A7C15L) >> 18L); //very fast
        //return ((state *= 0x9E3779B97F4A7C15L) * (++state >>> 7));
        //return ((((state += 0x9E3779B97F4A7C15L) >> 13) * 0xD0E89D2D311E289FL) >> 9) * 0xC6BC279692B5CC83L;
        //return ((state += 0x9E3779B97F4A7C15L) >> 16) * 0xD0E89D2D311E289FL;
        //return state * ((state += 0x9E3779B97F4A7C15L) >> 5) * 0xD0E89D2D311E289FL;
        //return ((state += 0x9E3779B97F4A7C15L) >> (state >>> 60L)) * 0xD0E89D2D311E289FL;
        //return (state * 0xD0E89D2D311E289FL) ^ (state += 0x9E3779B97F4A7C15L);
        //return ((state >> 5) * 0xC6BC279692B5CC83L) ^ (state += 0x9E3779B97F4A7C15L);
        //return ((state += 0x9E3779B97F4A7C15L) >>> (state >>> 60L)) * 0x632BE59BD9B4E019L; //pretty good quality
        //return (lag ^= 0xD0E89D2D311E289FL * ((state += 0x9E3779B97F4A7C15L) >> 18L));// * 0xC6BC279692B5CC83L;
        //return (((lag += (state += 0x9E3779B97F4A7C15L)) >>> 36) | (lag << 44)) * 2862933555777941757L + 7046029254386353087L;
        //return (lag += ((state += 0x9E3779B97F4A7C15L) ^ (lag >>> 7)) & 0xDF5DFFDADFE8FFFFL) * 2862933555777941757L + 7046029254386353087L;
        //return (-state >> ((state += 0x9E3779B97F4A7C15L) & 31)) * 0xC6BC279692B5CC83L;
        //return state + ((state += 0x9E3779B97F4A7C15L) >> 18) * 0xC6BC279692B5CC83L;
        //return (state ^ ((state += 0x9E3779B97F4A7C15L) >> 18)) * 0xD0E89D2D311E289FL;
        //return ((state += 0x9E3779B97F4A7C15L) >> 18) * 0xC6BC279692B5CC83L ^ state;
        //return (state >> 18) * 0xC6BC279692B5CC83L + (state += 0x9E3779B97F4A7C15L);
        //return (state ^ ((state += 0x9E3779B97F4A7C15L) >> 18)) * 0xC6BC279692B5CC83L; // previous, best speed, not-great quality
        //return (state ^ ((state += jumble) >> 18)) * 0xC6BC279692B5CC83L * (jumble += 0xA1D13A5A623C513EL); //0x8D784F2D256B9906L
        //return (state ^ ((state += jumble) >> 17)) * 0xC6BC279692B5CC83L * (jumble += 0xA1D13A5A623C513EL);
        //return (state ^ ((state += jumble) >> 17)) * (jumble += 0x8D784F2D256B9906L); // 0x3C6EF372FE94F82AL
        //return (state ^ ((state += jumble + 0xA1D13A5A623C513EL) >> 17)) * (jumble += 0x8D784F2D256B9906L); // 0x3C6EF372FE94F82AL
        //return state ^ jumble * ((state += jumble & (jumble += 0xA1D13A5A623C513EL)) >>> 16);
        //return state ^ ((state += jumble) ^ (jumble += 0x9E3779B97F4A7C15L));
        //0xBC6EF372FEB7FC6AL -> 0xBC6EF375FEB7FDAL


        //return state ^ 0xC6BC279692B5CC83L * ((state += jumble & (jumble += 0xBC6EF372FEB7FC6AL)) >> 16);

        //Best Known
        return state ^ (0x9E3779B97F4A7C15L * ((state += jumble & (jumble += 0xAB79B96DCD7FE75EL)) >> 20));

        //WIP zone
        //return state ^ (0x9E3779B97F4A7C15L * ((state += jumble & (jumble += 0xA536C939B656C3FEL)) >> 10)); //C5A3 0xAAAAAAAA55555555L
        //0xB4EC695A63719B6BL //0xBC6756B4A5B16C57L with 16 //0x96C5D94B6EA595AFL with 12

        //return (state >> ((state += 0xC6BC279692B5CC83L) >>> 60)) * 0x9E3779B97F4A7C15L;
        //return (state += (state >>> 12) * 0xC6BC279692B5CC83L) * 0x9E3779B97F4A7C15L;
        //return (state += ((jumble += 0xD0E89D2D311E289FL) >> 28) * 0xC6BC279692B5CC83L) * 0x9E3779B97F4A7C15L;

        //return state = state * 2862933555777941757L + 7046029254386353087L; // LCG for comparison
    }

    public int nextInt()
    {
        return (int)(nextLong() >>> 32);
    }
    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    public double nextDouble() {
        return Double.longBitsToDouble(0x3FFL << 52 | nextLong() >>> 12) - 1.0;
    }

    /**
     * Get "Part A" of the current internal state of the ThunderRNG as a long. Reconstituting the state of this
     * ThunderRNG requires Part A (given by this) and Part B (given by {@link #getStatePartB()})
     *
     * @return part A of the current internal state of this object.
     */
    public long getStatePartA() {
        return state;
    }

    /**
     * Get "Part A" of the current internal state of the ThunderRNG as a long. Reconstituting the state of this
     * ThunderRNG requires Part A (given by {@link #getStatePartA()}) and Part B (given by this).
     *
     * @return part B of the current internal state of this object.
     */
    public long getStatePartB() {
        return jumble;
    }

    /**
     * Set the current internal state of this ThunderRNG with two long parts, often obtained using the previous
     * state of another ThunderRNG using {@link #getStatePartA()} and {@link #getStatePartB()}, but any values
     * are allowed for both parts. Only the upper 63 bits are used of partB; the bottom bit of partB is always
     * changed to 1 internally so the RNG works as intended.
     *
     * @param partA any 64-bit long
     * @param partB any 64-bit long, but the least significant bit will be ignored (2 and 3 are identical).
     */
    public void setState(long partA, long partB) {
        state = partA;
        jumble = partB | 1L;

    }

    /**
     * Replicates the behavior of the constructor that takes one long, and sets both parts of the state to what that
     * constructor would assign given the same seed.
     * @param seed any long
     */
    public void reseed( final long seed ) {
        state = (seed + bitPermute(seed + 0xC6BC279692B5CC83L)) * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L;
        jumble = (state + bitPermute(state + 0x9E3779B97F4A7C15L)) * 0xC6BC279692B5CC83L + 0x632BE59BD9B4E019L | 1L;
        /*
        jumble = (seed ^ ((seed + 0x9E3779B97F4A7C15L) >> 18)) * 0xC6BC279692B5CC83L;
        jumble ^= (((seed + 0x9E3779B97F4A7C15L) ^ ((seed + 0x9E3779B97F4A7C15L + 0x9E3779B97F4A7C15L) >> 18)) * 0xC6BC279692B5CC83L) >>> 32;
        jumble |= 1L;
        */
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produces a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new ThunderRNG(state, jumble);
    }

    @Override
    public String toString() {
        return "ThunderRNG with state parts A=0x" + StringKit.hex(state) + "L, B=0x"  + StringKit.hex(jumble)+ 'L';
    }

    /*
    public static void main(String[] args)
    {
        ThunderRNG t = new ThunderRNG(0xD0E89D2D311E289EL);
        for (int i = 0; i < 512; i++) {
            System.out.println(StringKit.bin(t.state) + " and " + StringKit.bin(t.jumble) + " produce " + StringKit.bin(t.nextLong()));
        }
    }
    */

    /*
    public static void main(String[] args)
    {
        ThunderRNG t = new ThunderRNG(0xD0E89D2D311E289FL);
        long sa = t.state, sb = t.jumble, bigger = 0, big = 0, little = 0;
        MAIN_LOOP:
        for (bigger = 0L; bigger < 0x1000000L; bigger++) {
            for (big = 0L; big < 0x1000000L; big++) {
                for (little = 0L; little < 0x100000000L; little++) {
                    t.nextLong();
                    if(sa == t.state)
                        break MAIN_LOOP;
                }
            }
        }
        System.out.println("State Part A repeats with info...\nBigger: " + bigger + ", Big: " + big + ", Little: " + little);
    }*/
    /*
    public static void main(String[] args)
    {
        for (int i = 0; i < 32; i++) {
            System.out.println(t.nextInt() + " has " + t.state);
        }
        System.out.println();
        t.setState(1, 4);
        for (int i = 0; i < 32; i++) {
            System.out.println(t.nextInt() + " has " + t.state);
        }
        System.out.println();
        t.setState(-1, 4);
        for (int i = 0; i < 32; i++) {
            System.out.println(t.nextInt() + " has " + t.state);
        }
    }*/
}
