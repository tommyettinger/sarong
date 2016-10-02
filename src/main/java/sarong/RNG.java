package sarong;

import sarong.util.CrossHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A wrapper class for working with random number generators in a more friendly
 * way.
 * <p>
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG implements Serializable {

    protected static final float FLOAT_UNIT = 1.0f / (1 << 24);
    protected RandomnessSource random;
    protected double nextNextGaussian;
    protected boolean haveNextNextGaussian = false;
    protected Random ran = null;

    private static final long serialVersionUID = 2352426757973945149L;

    /**
     * Default constructor; uses SplitMix64, which is of high quality, but low period (which rarely matters for games),
     * and has good speed, tiny state size, and excellent 64-bit number generation.
     * <br>
     * Compatibility note: previous versions of SquidLib used Mersenne Twister by default. Due to the incompatibility
     * of the threads used by this Mersenne Twister implementation with GWT and HTML5 applications, the randomness
     * algorithm has been changed to a faster, more compatible algorithm, though it does suffer from a much lower
     * period. If you need drastically larger periods than 2^64, you can pass a LongPeriodRNG (or MersenneTwister on
     * targets other than HTML) object to the constructor that takes a RandomnessSource. If you don't know what the
     * period of a PRNG is, you probably don't need to worry about it; it's mainly relevant to heavily multi-threaded
     * applications anyway. The addition of LongPeriodRNG on March 21, 2016 should help to take the part of a fast,
     * large-period RNG, which MersenneTwister is unable to act as on GWT. The default may change again some time after
     * May 1, 2016, now that we have XoRoRNG, which is approximately as fast as LightRNG and has a substantially better
     * period (pow(2, 128) - 1).
     */
    public RNG() {
        this(new LightRNG());
    }

    /**
     * Seeded constructor; uses LightRNG, which is of high quality, but low period (which rarely matters for games),
     * and has good speed, tiny state size, and excellent 64-bit number generation.
     */
    public RNG(long seed) {
        this(new LightRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for LightRNG, which is of high quality, but low period (which rarely matters for games), and has good speed,
     * tiny state size, and excellent 64-bit number generation.
     */
    public RNG(String seedString) {
        this(new LightRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This
     * constructor should be used if an alternate RandomnessSource other than LightRNG is desirable.
     *
     * @param random the source of pseudo-randomness, such as a MersenneTwister or SobolQRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = random;
    }

    /**
     * A subclass of java.util.Random that uses a RandomnessSource supplied by the user instead of the default.
     *
     * @author Tommy Ettinger
     */
    public static class CustomRandom extends Random {

        private static final long serialVersionUID = 8211985716129281944L;
        private final RandomnessSource randomnessSource;

        /**
         * Creates a new random number generator. This constructor uses
         * a LightRNG with a random seed.
         */
        public CustomRandom() {
            randomnessSource = new LightRNG();
        }

        /**
         * Creates a new random number generator. This constructor uses
         * the seed of the given RandomnessSource if it has been seeded.
         *
         * @param randomnessSource a way to get random bits, supplied by RNG
         */
        public CustomRandom(RandomnessSource randomnessSource) {
            this.randomnessSource = randomnessSource;
        }

        /**
         * Generates the next pseudorandom number. Subclasses should
         * override this, as this is used by all other methods.
         * <p>
         * <p>The general contract of {@code next} is that it returns an
         * {@code int} value and if the argument {@code bits} is between
         * {@code 1} and {@code 32} (inclusive), then that many low-order
         * bits of the returned value will be (approximately) independently
         * chosen bit values, each of which is (approximately) equally
         * likely to be {@code 0} or {@code 1}. The method {@code next} is
         * implemented by class {@code Random} by atomically updating the seed to
         * <pre>{@code (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)}</pre>
         * and returning
         * <pre>{@code (int)(seed >>> (48 - bits))}.</pre>
         *
         * This is a linear congruential pseudorandom number generator, as
         * defined by D. H. Lehmer and described by Donald E. Knuth in
         * <i>The Art of Computer Programming,</i> Volume 3:
         * <i>Seminumerical Algorithms</i>, section 3.2.1.
         *
         * @param bits random bits
         * @return the next pseudorandom value from this random number
         * generator's sequence
         * @since 1.1
         */
        @Override
        protected int next(int bits) {
            return randomnessSource.next(bits);
        }
    }

    /**
     * @return a Random instance that can be used for legacy compatibility
     */
    public Random asRandom() {
        if (ran == null) {
            ran = new CustomRandom(random);
        }
        return ran;
    }

    /**
     * Returns a value from a even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public int between(int min, int max) {
        return nextInt(max - min) + min;
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public long between(long min, long max) {
        return nextLong(max - min) + min;
    }

    /**
     * Returns the average of a number of randomly selected numbers from the
     * provided range, with min being inclusive and max being exclusive. It will
     * sample the number of times passed in as the third parameter.
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     * <p>
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min     the minimum bound on the return value (inclusive)
     * @param max     the maximum bound on the return value (exclusive)
     * @param samples the number of samples to take
     * @return the found value
     */
    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        return Math.round((float) sum / samples);
    }

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     *
     * @param <T>   the type of the returned object
     * @param array the array to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(T[] array) {
        if (array.length < 1) {
            return null;
        }
        return array[nextInt(array.length)];
    }

    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     *
     * @param <T>  the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a random element from the provided Collection, which should have predictable iteration order if you want
     * predictable behavior for identical RNG seeds, though it will get a random element just fine for any Collection
     * (just not predictably in all cases). If you give this a Set, it should be a LinkedHashSet or some form of sorted
     * Set like TreeSet if you want predictable results. Any List or Queue should be fine. Map does not implement
     * Collection, thank you very much Java library designers, so you can't actually pass a Map to this, though you can
     * pass the keys or values. If coll is empty, returns null.
     * <p>
     * <p>
     * Requires iterating through a random amount of coll's elements, so performance depends on the size of coll but is
     * likely to be decent, as long as iteration isn't unusually slow. This replaces {@code getRandomElement(Queue)},
     * since Queue implements Collection and the older Queue-using implementation was probably less efficient.
     * </p>
     *
     * @param <T>  the type of the returned object
     * @param coll the Collection to get an element from; remember, Map does not implement Collection
     * @return the randomly selected element
     */
    public <T> T getRandomElement(Collection<T> coll) {
        if (coll.size() <= 0) {
            return null;
        }
        int n = nextInt(coll.size());
        T t = null;
        Iterator<T> it = coll.iterator();
        while (n-- >= 0 && it.hasNext())
            t = it.next();
        return t;
    }

    /**
     * Get an Iterable that starts at a random location in list and continues on through list in its current order.
     * Loops around to the beginning after it gets to the end, stops when it returns to the starting location.
     * <br>
     * You should not modify {@code list} while you use the returned reference. And there'll be no
     * ConcurrentModificationException to detect such erroneous uses.
     *
     * @param list A list <b>with a constant-time {@link List#get(int)} method</b> (otherwise performance degrades).
     * @return An {@link Iterable} that iterates over {@code list} but start at
     * a random index. If the chosen index is {@code i}, the iterator
     * will return:
     * {@code list[i]; list[i+1]; ...; list[list.length() - 1]; list[0]; list[i-1]}
     */
    public <T> Iterable<T> getRandomStartIterable(final List<T> list) {
        final int sz = list.size();
        if (sz == 0)
            return Collections.<T>emptyList();

		/*
         * Here's a tricky bit: Defining 'start' here means that every Iterator
		 * returned by the returned Iterable will have the same iteration order.
		 * In other words, if you use more than once the returned Iterable,
		 * you'll will see elements in the same order every time, which is
		 * desirable.
		 */
        final int start = nextInt(sz);

        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    int next = -1;

                    @Override
                    public boolean hasNext() {
                        return next != start;
                    }

                    @Override
                    public T next() {
                        if (next == start)
                            throw new NoSuchElementException("Iteration terminated; check hasNext() before next()");
                        if (next == -1)
                    /* First call */
                            next = start;
                        final T result = list.get(next);
                        if (next == sz - 1)
					/*
					 * Reached the list's end, let's continue from the list's
					 * left.
					 */
                            next = 0;
                        else
                            next++;
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Remove is not supported from a randomStartIterable");
                    }

                    @Override
                    public String toString() {
                        return "RandomStartIterator at index " + next;
                    }
                };
            }
        };
    }

    /**
     * Shuffles an array in place using the Fisher-Yates algorithm. If you don't want the array modified, use the
     * overload that takes two array arguments.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param elements an array of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffle(T[] elements) {
        for (int i = elements.length - 1; i > 0; i--) {
            int r = nextInt(i + 1);
            T t = elements[r];
            elements[r] = elements[i];
            elements[i] = t;
        }
        return elements;
    }

    /**
     * Shuffle an array using the "inside-out" Fisher-Yates algorithm. DO NOT give the same array for both elements and
     * dest, since the prior contents of dest are rearranged before elements is used, and if they refer to the same
     * array, then you can end up with bizarre bugs where one previously-unique item shows up dozens of times. If
     * you want to leave elements unmodified, create a new array with the same length as elements and pass it as dest;
     * the returned value can be assigned to whatever you want and will have the same items as the newly-formed array.
     * You can also use the one-argument version of this method to shuffle an array in-place.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_.22inside-out.22_algorithm
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *                 randomPortion method of this class to fill the smaller dest. MUST NOT be the same array as elements!
     * @return {@code dest} after modifications
     */
    /* This method has this prototype to be compatible with GWT. */
    public <T> T[] shuffle(T[] elements, T[] dest) {
        if (dest.length != elements.length)
            return randomPortion(elements, dest);
        for (int i = 0; i < elements.length; i++) {
            int r = nextInt(i + 1);
            if (r != i)
                dest[i] = dest[r];
            dest[r] = elements[i];
        }
        return dest;
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     *
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
        ArrayList<T> al = new ArrayList<T>(elements);
        int n = al.size();
        for (int i = 0; i < n; i++) {
            Collections.swap(al, i + nextInt(n - i), i);
        }
        return al;
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    public int[] randomOrdering(int length)
    {
        if(length <= 0)
            return new int[0];
        int[] dest = new int[length];
        for (int i = 0; i < length; i++)
        {
            int r = nextInt(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive) and stores it in
     * the dest parameter, avoiding allocations.
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     * @param length the size of the ordering to produce
     * @param dest the destination array; will be modified
     * @return dest, filled with a random ordering containing all ints from 0 to length (exclusive)
     */
    public int[] randomOrdering(int length, int[] dest)
    {
        if(dest == null || dest.length <= 0) return dest;
        for (int i = 0; i < length && i < dest.length; i++)
        {
            int r = nextInt(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
    }

    /**
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once; does this by
     * generating random indices for data's elements, but only as much as needed, assigning the copied section to output
     * and not modifying data.
     * <br>
     * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
     * copying or creating a new generic array (a problem on GWT).
     *
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain unchanged elements (null if output
     * was empty) if data is shorter than output
     */
    public <T> T[] randomPortion(T[] data, T[] output) {
        int length = data.length;
        int n = Math.min(length, output.length);
        int[] mapping = new int[n];
        for (int i = 0; i < n; i++) {
            mapping[i] = i;
        }

        for (int i = 0; i < n; i++) {
            int r = nextInt(length);
            output[i] = data[mapping[r]];
            mapping[r] = mapping[--length];
        }

        return output;
    }

    /**
     * Gets a random portion of a List and returns it as a new List. Will only use a given position in the given
     * List at most once; does this by shuffling a copy of the List and getting a section of it.
     *
     * @param data  a List of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T>   can be any non-primitive type
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    public <T> List<T> randomPortion(List<T> data, int count) {
        return shuffle(data).subList(0, Math.min(count, data.size()));
    }

    /**
     * Gets a random shuffle of the subrange of non-negative ints from start (inclusive) to end (exclusive), using count
     * elements. May return an empty array if the parameters are invalid (end is less than/equal to start, or start is
     * negative). Normally, start and end should have a difference that is larger than count, and the actual values
     * returned by this will probably not be in consecutive order, but will never repeat the same value twice.
     *
     * @param start the start of the range of numbers to potentially use (inclusive)
     * @param end   the end of the range of numbers to potentially use (exclusive)
     * @param count the total number of elements to use; will be less if the range is smaller than count
     * @return an int array that contains at most one of each number in the range
     */
    public int[] randomRange(int start, int end, int count) {
        if (end <= start || start < 0)
            return new int[0];

        int n = end - start;
        int[] data = new int[n];

        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }

        for (int i = 0; i < n; i++) {
            int r = i + nextInt(n - i);
            int t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        int[] array = new int[Math.min(count, n)];
        System.arraycopy(data, 0, array, 0, Math.min(count, n));
        return array;
    }

    /**
     * @return a value from the gaussian distribution
     */
    public synchronized double nextGaussian() {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * nextDouble() - 1; // between -1 and 1
                v2 = 2 * nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = Math.sqrt(-2 * Math.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    public double nextDouble() {
        return Double.longBitsToDouble(0x3FFL << 52 | random.nextLong() >>> 12) - 1.0;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    public double nextDouble(double max) {
        return nextDouble() * max;
    }

    /**
     * This returns a maximum of 0.99999994 because that is the largest Float
     * value that is less than 1.0f
     *
     * @return a value between 0 (inclusive) and 0.99999994 (inclusive)
     */
    public float nextFloat() {
        return next(24) * FLOAT_UNIT;
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     *
     * @return a random boolean.
     */
    public boolean nextBoolean() {
        return next(1) != 0;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public long nextLong(final long bound) {
        if (bound <= 0) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = random.nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    /**
     * Returns a random integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public int nextInt(final int bound) {
        if (bound <= 0) return 0;
        return (int)((bound * (random.nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    public int nextInt() {
        return next(32);
    }

    /**
     * Get up to 32 bits (inclusive) of random state from the RandomnessSource.
     *
     * @param bits 1 to 32
     * @return a random number that fits in the specified number of bits.
     */
    public int next(int bits) {
        return random.next(bits);
    }

    public RandomnessSource getRandomness() {
        return random;
    }

    public void setRandomness(RandomnessSource random) {
        this.random = random;
    }

    /**
     * Creates a copy of this RNG; it will generate the same random numbers, given the same calls in order, as this RNG
     * at the point copy() is called. The copy will not share references with this RNG.
     *
     * @return a copy of this RNG
     */
    public RNG copy() {
        return new RNG(random.copy());
    }

    @Override
    public String toString() {
        return "RNG with Randomness Source " + random;
    }
}
