package sarong.discouraged;

import sarong.StatefulRandomness;
import sarong.util.CrossHash;
import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A Linear Feedback Shift Register that may be used like a StatefulRandomness but is not truly random. This has a
 * period of (2 to the 64) minus 1, and is based on Wikipedia's code for a Galois LFSR but uses data from
 * http://web.archive.org/web/20161007061934/http://courses.cse.tamu.edu/csce680/walker/lfsr_table.pdf .
 * It is important to note that an LFSR will produce each number from 1 until its maximum exactly once before repeating,
 * so this may be useful as a way of generating test data in an unpredictable order.
 * @author Tommy Ettinger
 */
public class LFSR implements StatefulRandomness, Serializable {

	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);

	private static final long serialVersionUID = -2373549048478690398L;

    private long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public LFSR() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public LFSR(final long seed) {
        setState(seed);
    }

    public LFSR(final CharSequence seed)
    {
        this(CrossHash.hash64(seed));
    }


    @Override
    public int next(int bits) {
        return (int) (state = (state >>> 1 ^ (-(state & 1L) & 0xD800000000000000L))) >>> 32 - bits;
    }

    @Override
    public long nextLong() {
        return state = (state >>> 1 ^ (-(state & 1L) & 0xD800000000000000L));
    }

    public long nextLongTweak() {
        final long s = (state = (state >>> 1 ^ (-(state & 1L) & 0xD800000000000000L))) * 0xAEF17502108EF2D9L;
        return s ^ s >>> 30;
    }
    public long xorshift()
    {
        state ^=(state<<13);
        state ^=(state>>>7);
        return (state ^=(state<<17));
    }
    public long xorshift2()
    {
        long s = state;
        s ^=(s<<13);
        s ^=(s>>>7);
        return (state = s ^ s<<17);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public LFSR copy() {
        return new LFSR(state);
    }


    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)(state = (state >>> 1 ^ (-(state & 1L) & 0xD800000000000000L)));
    }

    /**
     * Exclusive on the upper bound.  The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int bound ) {
        return (int)((bound * (nextLong() & 0x7FFFFFFFL)) >> 31);
    }
    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random long less than n
     */
    public long nextLong( final long bound ) {
        if ( bound <= 0 ) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (;;) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    public double nextDouble() {
        return (nextLong() & DOUBLE_MASK) * NORM_53;
    }

    public float nextFloat() {
        return (float) ((nextLong() & FLOAT_MASK) * NORM_24);
    }

    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    public void nextBytes(final byte[] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) {
                bytes[--i] = (byte) bits;
            }
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
     * Sets the state of this generator to the given long, unless it is 0 (in which case it sets the state to 1).
     * @param seed the number to use as the seed; if 0 will be treated as 1
     */
    public void setState(final long seed) {
        if(seed == 0)
            state = 1L;
        else
            state = seed;
    }

    @Override
    public String toString() {
        return "LFSR with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LFSR lfsr = (LFSR) o;

        return (state == lfsr.state);
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Gets the next number that an LFSR would produce using {@link #nextLong()} if its state was {@code state}.
     * Does not allow state to be 0. Strongly consider using the result of this and assigning it to state if you expect
     * to call this again, such as with {@code (state = LFSR.determine(state))}, which will ensure the long-term
     * properties of an LFSR hold up (such as having a period of ((2 to the 64) minus 1), or the guarantee that every
     * number from 1 to ((2 to the 64) minus 1), inclusive on both, will be generated once per period).
     * @param state any long other than 0
     * @return the next long that a 64-bit LFSR would produce with the given state
     */
    public static long determine(final long state)
    {
        return state >>> 1 ^ (-(state & 1L) & 0xD800000000000000L);
    }

    /**
     * Gets the next number from 1 to 255 that a different kind of LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0. If given all byte values except 0 as arguments, will produce all ints 1-255.
     * Strongly consider using the result of this and assigning it to state if you expect to call this again, such as
     * with {@code (state = LFSR.determineByte(state))}, which will ensure the long-term properties of an LFSR hold up
     * (such as having a period of 255, or the guarantee that every number from 1 to 255, inclusive on both, will be
     * generated once per period).
     * @param state any byte other than 0
     * @return the next int between 1 and 255 that an 8-bit LFSR would produce with the given state
     */
    public static int determineByte(final byte state)
    {
        return (state & 0xFF) >>> 1 ^ (-(state & 1) & 0xB8);
    }

    /**
     * Gets the next number from 1 to 65535 that a different kind of LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0. If given all short values except 0 as arguments, will produce all ints 1-65535.
     * Strongly consider using the result of this and assigning it to state if you expect to call this again, such as
     * with {@code (state = LFSR.determineShort(state))}, which will ensure the long-term properties of an LFSR hold up
     * (such as having a period of 65535, or the guarantee that every number from 1 to 65535, inclusive on both, will be
     * generated once per period).
     * @param state any short other than 0
     * @return the next int between 1 and 65535 that a 16-bit LFSR would produce with the given state
     */
    public static int determineShort(final short state)
    {
        return (state & 0xFFFF) >>> 1 ^ (-(state & 1) & 0xB400);
    }

    /**
     * Gets the next number from 1 to 65535 that a different kind of LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0. If given all short values except 0 as arguments, will produce all ints 1-65535.
     * Strongly consider using the result of this and assigning it to state if you expect to call this again, such as
     * with {@code (state = LFSR.determineShort(state))}, which will ensure the long-term properties of an LFSR hold up
     * (such as having a period of 65535, or the guarantee that every number from 1 to 65535, inclusive on both, will be
     * generated once per period). This effectively runs in the "reverse direction" as {@link #determineShort(short)}.
     * @param state any short other than 0
     * @return the next int between 1 and 65535 that a 16-bit LFSR would produce with the given state
     */
    public static int determineShortAlt(final short state)
    {
        return (state & 0x7FFF) << 1 ^ ((state >> 31) & 0x002D);
    }

    /**
     * Gets the next number that a different kind of 32-bit LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0. If given all int values except 0 as arguments, will produce all ints except 0.
     * Strongly consider using the result of this and assigning it to state if you expect to call this again, such as
     * with {@code (state = LFSR.determineInt(state))}, which will ensure the long-term properties of an LFSR hold up
     * (such as having a period of ((2 to the 32) minus 1), or the guarantee that every number from 1 to ((2 to the 32)
     * minus 1), inclusive on both, will be generated once per period).
     * @param state any int other than 0
     * @return the next int that a 32-bit LFSR would produce with the given state
     */
    public static int determineInt(final int state)
    {
        return state >>> 1 ^ (-(state & 1) & 0xA3000000);
    }

    /**
     * Gets the next positive long that a different kind of 63-bit LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0 or negative. If given all positive long values (not including 0) as arguments, will
     * produce all longs greater than 0. Strongly consider using the result of this and assigning it to state if you
     * expect to call this again, such as with {@code (state = LFSR.determinePositiveLong(state))}, which will ensure
     * the long-term properties of an LFSR hold up (such as having a period of ((2 to the 63) minus 1), or the guarantee
     * that every number from 1 to ((2 to the 63) minus 1), inclusive on both, will be generated once per period).
     * @param state any positive long, not including 0
     * @return the next int that a 63-bit LFSR would produce with the given state
     */
    public static long determinePositiveLong(final long state)
    {
        return state >>> 1 ^ (-(state & 1L) & 0x6000000000000000L);
    }

    /**
     * Gets the next positive int that a different kind of 31-bit LFSR would produce if its state was {@code state}.
     * Does not allow state to be 0 or negative. If given all positive int values (not including 0) as arguments, will
     * produce all ints greater than 0. Strongly consider using the result of this and assigning it to state if you
     * expect to call this again, such as with {@code (state = LFSR.determinePositiveInt(state))}, which will ensure the
     * long-term properties of an LFSR hold up (such as having a period of ((2 to the 31) minus 1), or the guarantee
     * that every number from 1 to ((2 to the 31) minus 1), inclusive on both, will be generated once per period).
     * <br>
     * A potential benefit of using this particular LFSR type is that the period is a prime number, 2147483647; this can
     * sometimes be relevant if you simultaneously get pseudo-random numbers from sources of randomness with different
     * periods that are "relatively co-prime" (that is, they share no common factors other than 1). This case lengthens
     * the total period of the combined generators significantly, generally multiplying the periods together to get the
     * combined period, as opposed to other cases that may simply add them together.
     * @param state any positive int, not including 0
     * @return the next int that a 31-bit LFSR would produce with the given state
     */
    public static int determinePositiveInt(final int state)
    {
        return state >>> 1 ^ (-(state & 1) & 0x48000000);
    }

    /**
     * Gets the next int that a different kind of LFSR would produce if its state was {@code state}.
     * Does not allow state to be {@link Integer#MIN_VALUE}, nor will this produce a result of {@link Integer#MIN_VALUE}
     * (as long as {@link Integer#MIN_VALUE} was not the input). If given all int values except
     * {@link Integer#MIN_VALUE} as arguments, will produce all ints in the range {@code [-2147483647,2147483647]},
     * including 0 but not -2147483648 (the minimum int). Strongly consider using the result of this and assigning it to
     * state if you expect to call this again, such as with {@code (state = LFSR.determineIntSymmetrical(state))}, which
     * will ensure the long-term properties of an LFSR hold up (such as having a period of ((2 to the 32) minus 1), or
     * the guarantee that every int except {@link Integer#MIN_VALUE} will be generated once per period).
     * <br>
     * This is called Symmetrical because it produces the same amount of positive and negative numbers, instead of the
     * normal generation of more negative ones (due to how ints are represented, the min value is always further from 0
     * than the max value for any signed integer type).
     * @param state any int other than -2147483648 (0x80000000), which is {@link Integer#MIN_VALUE}; can produce 0
     * @return the next int other than -2147483648 that an LFSR would produce with the given state
     */
    public static int determineIntSymmetrical(final int state)
    {
        return (state >>> 1 ^ (-(state & 1) & 0xA3000000)) + 0x80000000;
    }

    /**
     * Gets the next number that an LFSR would produce using {@link #nextInt(int)} if its state was {@code state} and
     * {@code bound} was passed to nextInt(). Does not allow state to be 0, but bound can be negative, which causes this
     * not to produce positive numbers. This method is very predictable and its use is not encouraged; prefer using
     * {@link #determineBounded(int, int)}.
     * @param state any long other than 0
     * @param bound the exclusive bound on the result as an int; does better if the bound is not too high (below 10000?)
     * @return the next value that {@link LFSR#determine(long)} would produce with the given state, but limited to bound; can return 0
     */
    public static int determineBounded(final long state, final int bound)
    {
        return (int)((bound * (state >>> 1 & 0xFFFFFFFFL)) >> 32);
    }
    /**
     * Gets an int using {@link #determineInt(int)} and bounds it to fit between 0 (inclusive) and bound (exclusive).
     * Does not allow state to be 0, but bound can be negative, which causes this not to produce positive numbers.
     * @param state any int other than 0
     * @param bound the exclusive bound on the result as an int; does better if the bound is not too high (below 10000?)
     * @return the next int that {@link LFSR#determineInt(int)} would produce with the given state, but limited to bound; can return 0
     */
    public static int determineBounded(final int state, final int bound)
    {
        return (int)((bound * ((state >>> 1 ^ (-(state & 1) & 0xA3000000)) & 0xFFFFFFFFL)) >> 32);
    }
    
//    public static void main(String[] args)
//    {
//        byte a = 1, b = 1;
//        long ctr = 0L;
//        do {
//            a += (b = (byte)((b&0xFF) >>> 1 ^ (-(b & 1) & 0xB8))) + 0x95;
//            ctr++;
//        }while (!(a == 1 && b == 1));
//        System.out.println(ctr);
//    }
}
