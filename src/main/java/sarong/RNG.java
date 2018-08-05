package sarong;

import sarong.util.CrossHash;

import java.io.Serializable;
import java.util.*;

import static sarong.NumberTools.intBitsToFloat;

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
public class RNG extends AbstractRNG implements Serializable, IRNG {
    
    protected RandomnessSource random;
    protected double nextNextGaussian;
    protected boolean haveNextNextGaussian = false;
    protected Random ran = null;

    private static final long serialVersionUID = 2352426757973945149L;


    /**
     * Default constructor; uses {@link LinnormRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers.
     * <br>
     * Previous versions of SquidLib used different implementations, including {@link LightRNG}, {@link ThrustAltRNG},
     * and {@link MersenneTwister}. You can still use one of these by instantiating one of those classes and passing it
     * to {@link #RNG(RandomnessSource)}, which may be the best way to ensure the same results across versions.
     */
    public RNG() {
        this(new LinnormRNG());
    }

    /**
     * Default constructor; uses {@link LinnormRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers. The seed can be
     * any long, including 0.
     * @param seed any long
     */
    public RNG(long seed) {
        this(new LinnormRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode,
     * instead using {@link CrossHash#hash64(CharSequence)}) as a seed for {@link LinnormRNG}, which is of high quality,
     * but low period (which rarely matters for games), and has excellent speed, tiny state size, and natively generates
     * 64-bit numbers.
     */
    public RNG(CharSequence seedString) {
        this(new LinnormRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This constructor should be used if an alternate
     * RandomnessSource other than LinnormRNG is desirable, such as to keep compatibility with earlier SquidLib
     * versions that defaulted to LightRNG or ThrustAltRNG.
     * <br>
     * If the parameter is null, this is equivalent to using {@link #RNG()} as the constructor.
     * @param random the source of pseudo-randomness, such as a LightRNG or LongPeriodRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = (random == null) ? new LinnormRNG() : random;
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
         * a ThrustAltRNG with a random seed.
         */
        public CustomRandom() {
            randomnessSource = new ThrustAltRNG();
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

    
    /*
     * Returns a random elements from the provided queue. If the queue is empty
     * then null is returned.
     *
     * <p>
     * Requires iterating through a random amount of the elements in set, so
     * performance depends on the size of set but is likely to be decent. This
     * is mostly meant for internal use, the same as ShortSet.
     * </p>
     *
     * @param <T> the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
	/*
	public <T> T getRandomElement(Queue<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return new ArrayList<>(list).get(nextInt(list.size()));
	}*/

    /**
     * Given a {@link List} l, this selects a random element of l to be the first value in the returned list l2. It
     * retains the order of elements in l after that random element and makes them follow the first element in l2, and
     * loops around to use elements from the start of l after it has placed the last element of l into l2.
     * <br>
     * Essentially, it does what it says on the tin. It randomly rotates the List l.
     * <br>
     * If you only need to iterate through a collection starting at a random point, the method getRandomStartIterable()
     * should have better performance. This was GWT incompatible before GWT 2.8.0 became the version SquidLib uses; now
     * this method works fine with GWT.
     *
     * @param l   A {@link List} that will not be modified by this method. All elements of this parameter will be
     *            shared with the returned List.
     * @param <T> No restrictions on type. Changes to elements of the returned List will be reflected in the parameter.
     * @return A shallow copy of {@code l} that has been rotated so its first element has been randomly chosen
     * from all possible elements but order is retained. Will "loop around" to contain element 0 of l after the last
     * element of l, then element 1, etc.
     */
    public <T> List<T> randomRotation(final List<T> l) {
        final int sz = l.size();
        if (sz == 0)
            return Collections.<T>emptyList();

        /*
         * Collections.rotate should prefer the best-performing way to rotate l,
         * which would be an in-place modification for ArrayLists and an append
         * to a sublist for Lists that don't support efficient random access.
         */
        List<T> l2 = new ArrayList<>(l);
        Collections.rotate(l2, nextInt(sz));
        return l2;
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
     * Gets a random subrange of the non-negative ints from start (inclusive) to end (exclusive), using count elements.
     * May return an empty array if the parameters are invalid (end is less than/equal to start, or start is negative).
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
        final int[] data = new int[n];

        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }

        for (int i = 0; i < n - 1; i++) {
            final int r = i + nextInt(n - i), t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        final int[] array = new int[Math.min(count, n)];
        System.arraycopy(data, 0, array, 0, Math.min(count, n));
        return array;
    }
    /**
     * Generates a random float with a curved distribution that centers on 0 (where it has a bias) and can (rarely)
     * approach -1f and 1f, but not go beyond those bounds. This is similar to {@link #nextGaussian()} in that it uses
     * a curved distribution, but it is not the same. The distribution for the values is similar to Irwin-Hall, and is
     * frequently near 0 but not too-rarely near -1f or 1f. It cannot produce values greater than or equal to 1f, or
     * less than -1f, but it can produce -1f.
     * @return a deterministic float between -1f (inclusive) and 1f (exclusive), that is very likely to be close to 0f
     */
    public float nextCurvedFloat() {
        final long start = random.nextLong();
        return   (intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                + intBitsToFloat(((int)(start ^ ~start >>> 20) & 0x007FFFFF) | 0x3F000000)
                + intBitsToFloat(((int) (~start ^ start >>> 30) & 0x007FFFFF) | 0x3F000000)
                - 3f);
    }

    /**
     * Gets a pseudo-random double from the Gaussian distribution, which will usually be between -1.0 and 1.0 but is not
     * always in that range. If you do want values to always be between -1 and 1 and a float is OK, consider using
     * {@link #nextCurvedFloat()}, which is a different distribution that is less sharply-curved towards 0 and
     * terminates at -1 and 1.
     * @return a value from the Gaussian distribution
     */
    public double nextGaussian() {
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
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1p-53;
        //this is here for a record of another possibility; it can't generate quite a lot of possible values though
        //return Double.longBitsToDouble(0x3FF0000000000000L | random.nextLong() >>> 12) - 1.0;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (exclusive)
     */
    @Override
    public double nextDouble(final double outer) {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1p-53 * outer;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public float nextFloat() {
        return random.next(24) * 0x1p-24f;
    }
    /**
     * This returns a random float between 0.0f (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (exclusive)
     */
    @Override
    public float nextFloat(final float outer) {
        return random.next(24) * 0x1p-24f * outer;
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This may have better behavior than {@code rng.next(1)}, depending on the RandomnessSource implementation; the
     * default ThrustAltRNG will behave fine, as will ThrustRNG and LightRNG (these all use similar algorithms), but the
     * normally-high-quality XoRoRNG will produce very predictable output with {@code rng.next(1)} and very good output
     * with {@code rng.nextBoolean()}. This is a known and considered flaw of Xoroshiro128+, the algorithm used by
     * XoRoRNG, and a large number of generators have lower quality on the least-significant bit than the most-
     * significant bit, where this method only checks the most-significant bit.
     * @return a random boolean.
     */
    public boolean nextBoolean() {
        return nextLong() < 0L;
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
     * Generates random bytes and places them into the given byte array, modifying it in-place.
     * The number of random bytes produced is equal to the length of the byte array. Unlike the
     * method in java.util.Random, this generates 8 bytes at a time, which can be more efficient
     * with many RandomnessSource types than the JDK's method that generates 4 bytes at a time.
     * <br>
     * Adapted from code in the JavaDocs of {@link Random#nextBytes(byte[])}.
     * <br>
     * @param  bytes the byte array to fill with random bytes; cannot be null, will be modified
     * @throws NullPointerException if the byte array is null
     */
    public void nextBytes(final byte[] bytes) {
        for (int i = 0; i < bytes.length; )
            for (long r = random.nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8)
                bytes[i++] = (byte) r;
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    public int nextInt() {
        return random.next(32);
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
    
    /**
     * Gets the minimum random long between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated long between 0 and bound out of the specified amount of trials
     */
    public long minLongOf(final long bound, final int trials)
    {
        long value = nextSignedLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextSignedLong(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random long between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated long between 0 and bound out of the specified amount of trials
     */
    public long maxLongOf(final long bound, final int trials)
    {
        long value = nextSignedLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextSignedLong(bound));
        }
        return value;
    }

    /**
     * Gets the minimum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated int between 0 and bound out of the specified amount of trials
     */
    public int minIntOf(final int bound, final int trials)
    {
        int value = nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextSignedInt(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated int between 0 and bound out of the specified amount of trials
     */
    public int maxIntOf(final int bound, final int trials)
    {
        int value = nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextSignedInt(bound));
        }
        return value;
    }

    /**
     * Gets the minimum random double between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated double between 0 and bound out of the specified amount of trials
     */
    public double minDoubleOf(final double bound, final int trials)
    {
        double value = nextDouble(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextDouble(bound));
        }
        return value;
    }

    /**
     * Gets the maximum random double between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated double between 0 and bound out of the specified amount of trials
     */
    public double maxDoubleOf(final double bound, final int trials)
    {
        double value = nextDouble(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextDouble(bound));
        }
        return value;
    }
    /**
     * Gets the minimum random float between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated float between 0 and bound out of the specified amount of trials
     */
    public float minFloatOf(final float bound, final int trials)
    {
        float value = nextFloat(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextFloat(bound));
        }
        return value;
    }

    /**
     * Gets the maximum random float between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated float between 0 and bound out of the specified amount of trials
     */
    public float maxFloatOf(final float bound, final int trials)
    {
        float value = nextFloat(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextFloat(bound));
        }
        return value;
    }


    @Override
    public String toString() {
        return "RNG with Randomness Source " + random;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RNG)) return false;

        RNG rng = (RNG) o;

        return random.equals(rng.random);
    }

    @Override
    public int hashCode() {
        return 31 * random.hashCode();
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
     * Returns this RNG in a way that can be deserialized even if only {@link IRNG}'s methods can be called.
     * @return a {@link Serializable} view of this RNG; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }

}
