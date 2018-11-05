package sarong;

import sarong.util.StringKit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * A mid-quality and very fast RNG that has no apparent visual artifacts here; uses an XLCG-type base that guarantees a
 * period of 2 to the 64 and that all seeds are valid, then adjusts the result of the XLCG irreversibly to improve
 * quality. This cannot produce all long values and will produce some longs more often than others. Even though it isn't
 * equidistributed, that is, it produces various outputs at different frequencies, it still passes 32TB of PractRand
 * testing with only one anomaly, considered "unusual." It is about 0.06 ns slower per long than ThrustAltRNG, and has
 * the same caveats to its quality (both are not equidistributed, both have the same period, both allow all seeds) but
 * this is not a SkippingRandomness like ThrustAltRNG.
 * <br>
 * It implements RandomnessSource, but if you want to copy this class with no dependencies, then the class declaration
 * can easily be changed to {@code public class BasicRandom64 extends Random implements Serializable} without any other
 * changes. Note, it does extend java.util.Random for additional ease of integration, but doesn't use the slow
 * {@code synchronized} keyword that Random's implementations do.
 * @author Tommy Ettinger
 */
public class BasicRandom64 extends Random implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    public long state;

    public BasicRandom64()
    {
        state = (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L);
    }

    public BasicRandom64(final long seed) {
        setState(seed);
    }

    public void setState(final long seed)
    {
        state = seed;
    }

    public final long nextLong() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xD1B54A32D192ED03L);
        z -= z >> 28;
        return z ^ z >> 26;
    }

    /**
     * Gets an int with at most the specified amount of bits; don't confuse this with {@link #nextInt(int)}, which gets
     * a number between 0 and its int argument, where this draws from a different (larger) range of random results. For
     * example, {@code next(2)} can return any 2-bit int,
     * which is limited to 0, 1, 2, or 3. Note that if you request 0 bits, this can give you any int (32 bits).
     * @param bits the number of bits to get, from 1 to 32
     * @return an int with at most the specified bits
     */
    public final int next(final int bits) {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xD1B54A32D192ED03L);
        z -= z >> 28;
        return (int) ((z ^ z >> 26) >>> (64 - bits));
    }
    public final int nextInt() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xD1B54A32D192ED03L);
        z -= z >> 28;
        return (int) (z ^ z >> 26);
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextInt(final int bound) {
        return (int) ((bound * (nextLong() & 0xFFFFFFFFL)) >> 32);
    }
    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. It also always gets
     * exactly two random numbers, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = nextLong();
        if (bound <= 0) return 0;
        final long boundLow = bound & 0xFFFFFFFFL;
        final long randLow = rand & 0xFFFFFFFFL;
        final long z = (randLow * boundLow >> 32);
        bound >>= 32;
        rand >>>= 32;
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
    }

    /**
     * Sets the seed using a long, passing its argument to {@link #setState(long)}. That method just sets the public
     * field {@link #state} to its argument currently, but it may do more to ensure cycle length in the future.
     * @param seed the initial seed
     */
    @Override
    public void setSeed(long seed) {
        setState(seed);
    }
    /**
     * Mutates the array arr by switching the contents at pos1 and pos2.
     * @param arr an array of T; must not be null
     * @param pos1 an index into arr; must be at least 0 and no greater than arr.length
     * @param pos2 an index into arr; must be at least 0 and no greater than arr.length
     */
    protected static <T> void swap(T[] arr, int pos1, int pos2) {
        final T tmp = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = tmp;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy, freshly-allocated, without
     * modifying elements.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled copy of elements
     */
    public <T> T[] shuffle(T[] elements) {
        final int size = elements.length;
        final T[] array = Arrays.copyOf(elements, size);
        for (int i = size; i > 1; i--) {
            swap(array, i - 1, nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm, affecting indices from 0 (inclusive) to length
     * (exclusive). May be useful with libGDX Array instances, which can be shuffled with
     * {@code random.shuffleInPlace(arr.items, arr.size)}. If you don't want the array modified, use
     * {@link #shuffle(Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffleInPlace(T[] elements, int length) {
        final int size = Math.min(elements.length, length);
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextInt(i));
        }
        return elements;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm.
     * If you don't want the array modified, use {@link #shuffle(Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffleInPlace(T[] elements) {
        final int size = elements.length;
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextInt(i));
        }
        return elements;
    }

    public BasicRandom64 copy() {
        return new BasicRandom64(state);
    }

    @Override
    public String toString() {
        return "BasicRandom64{" +
                "state=" + StringKit.hex(state) +
                '}';
    }

}
