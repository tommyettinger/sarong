package sarong;

import sarong.util.CrossHash;

import java.io.Serializable;
import java.util.*;

import static sarong.NumberTools.intBitsToFloat;

/**
 * A wrapper class for working with random number generators in a more friendly way.
 * Implements {@link IRNG}, which covers most of the API surface, but RNG implements
 * a decent amount of additional methods. You should consider if your code needs
 * these additional methods, and if not you should use IRNG as the type for when you
 * need some random number generator.
 * <p>
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array. There are methods to shuffle
 * a collection and to get a random ordering that can be applied as one shuffle
 * across multiple collections, if those collections support it. You can construct
 * an RNG with all sorts of RandomnessSource implementations, and choosing them
 * is usually not a big concern because the default works very well. If you target
 * GWT, then it is suggested that you use {@link GWTRNG} instead of RNG; both
 * implement {@link IRNG}, which is enough for most usage across SquidLib, but
 * GWTRNG is optimized heavily for better performance on GWT, even returning long
 * values faster than implementations that natively do their math on longs. It has
 * worse performance on 64-bit PCs and mobile devices, but should also have better
 * performance on 32-bit PCs and mobile devices.
 * <br>
 * But if you do want advice on what RandomnessSource to use... {@link DiverRNG}
 * is the default, and is the fastest generator that passes most tests and can
 * produce all 64-bit values, and though relative to many of the others it has a
 * significantly shorter period (the amount of random numbers it will go through
 * before repeating the sequence), this almost never matters in games, and is
 * primarily relevant for massively-parallel scientific programs. DiverRNG has a
 * period of {@code pow(2, 64)} as opposed to {@link XoRoRNG}'s
 * {@code pow(2, 128) - 1}, or {@link LongPeriodRNG}'s {@code pow(2, 1024) - 1}.
 * {@link LightRNG} is a solid choice and a former default RandomnessSource;
 * additional features of LightRNG are exposed in {@link MoonwalkRNG} and using
 * MoonwalkRNG is recommended if you need unusual features like skipping backwards
 * in a random number sequence, taking a result of a nextLong() call and reversing
 * it to get the state that produced it, or calculating the distance in number of
 * nextLong() calls between two results of nextLong() calls. LightRNG is a
 * StatefulRandomness, which lets it be used in {@link StatefulRNG}, and so is
 * DiverRNG, but LightRNG is also a {@link SkippingRandomness}, which means you can
 * leap forward or backwards in its sequence very efficiently (DiverRNG is not a
 * SkippingRandomness). {@link ThrustAltRNG} provides similar qualities to LightRNG,
 * and is one of the fastest generators here, but can't produce all possible 64-bit
 * values (possibly some 32-bit values as well); it was the default at one point so
 * you may want to keep compatibility with some versions by specifying ThrustAltRNG.
 * The defaults have changed in the past as issues are found in various generators;
 * LightRNG has high quality all-around but is slower than the other defaults,
 * ThrustAltRNG can't produce all results, LinnormRNG passed tests in an earlier
 * version of the PractRand test suite but now fails in the current version, and now
 * the default is DiverRNG, which shares the known issue of LightRNG and LinnormRNG
 * that it can't produce the same result twice from {@link #nextLong()} until the
 * generator exhausts its period and repeats its output from the beginning.
 * For most cases, you should decide between DiverRNG, ThrustAltRNG, LightRNG,
 * LongPeriodRNG, MiniMover64RNG, and XoshiroStarPhi32RNG based on your priorities.
 * Some tasks are better solved by using a different class, usually {@link GWTRNG},
 * which can always be serialized on GWT to save its state easily and is usually the
 * fastest substitute for RNG on that platform. DiverRNG is the best if you want high
 * speed, very good quality of randomness, and expect to generate a reasonable quantity
 * of numbers for a game (less than 18446744073709551616 numbers) without any single
 * results being impossible. LightRNG is the second-best at the above criteria, but is
 * the best option if you need an RNG that can skip backwards or jump forwards without
 * incurring speed penalties. LongPeriodRNG is best if you for some reason need a massive
 * amount of random numbers (as in, ten quintillion would be far too little) or want to
 * split up such a large generator into unrelated subsequences. XoshiroStarPhi32RNG is
 * best if GWT is a possible target but you either need to generate more than
 * 18446744073709551616 numbers (but less than 340282366920938463463374607431768211455
 * numbers) or you need to ensure that each 128-bit chunk of output is unique; if GWT is
 * a target but those specific needs don't matter, use GWTRNG. ThrustAltRNG and
 * MiniMover64RNG are both faster than DiverRNG usually (MiniMover64RNG is the fastest),
 * but because they are unable to generate some outputs, that may make them a poor choice
 * for some usage (ThrustAltRNG also has some bias toward specific numbers and produces
 * them more frequently, but not frequently enough to make it fail statistical tests, and
 * ThrustAltRNG can skip around in its output sequence like LightRNG). {@link GearRNG} is
 * high-quality and surprisingly one of the fastest generators here.
 * <br>
 * There are many more RandomnessSource implementations! You might want significantly less
 * predictable random results, which  {@link IsaacRNG} can provide, along with a
 * large period. The quality of {@link PermutedRNG} is also good, usually, and it
 * has a sound basis in PCG-Random, an involved library with many variants on its
 * RNGs.
 * <br>
 * There may be reasons why you would want a random number generator that uses 32-bit
 * math instead of the more common 64-bit math, but using a 32-bit int on desktop and
 * Android won't act the same as that same 32-bit int on GWT. Since GWT is stuck with
 * JavaScript's implementation of ints with doubles, overflow (which is needed for an
 * RNG) doesn't work with ints as expected, but does with GWT's implementation of longs.
 * If targeting GWT, {@link Lathe32RNG} is significantly faster at producing int values
 * than any long-based generator, and will produce the same results on GWT as on desktop
 * or Android (not all 32-bit generators do this). {@link Starfish32RNG} goes one step
 * further than Lathe32RNG at an even distribution, and has better quality, but is
 * slightly slower. While Lathe32RNG can produce all ints over the course of its period,
 * it will produce some pairs of ints, or longs, more often than others and will never
 * produce some longs. Starfish32RNG will produce all longs but one. {@link Orbit32RNG}
 * can have better speed than Lathe32RNG or Starfish32RNG under some conditions, and is
 * two-dimensionally equidistributed (between Lathe and Starfish).
 * {@link Zag32RNG} and {@link Oriole32RNG} are also GWT-safe. Most other generators
 * use longs, and so will be slower than the recommended Starfish32RNG or Lathe32RNG on GWT,
 * but much faster on 64-bit JREs.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG implements Serializable, IRNG {

    protected RandomnessSource random;
    protected Random ran = null;

    private static final long serialVersionUID = 2352426757973945105L;


    /**
     * Default constructor; uses {@link DiverRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers.
     * <br>
     * Previous versions of SquidLib used different implementations, including {@link LightRNG}, {@link ThrustAltRNG},
     * {@link LinnormRNG}, and {@link MersenneTwister}. You can still use one of these by instantiating one of those
     * classes and passing it to {@link #RNG(RandomnessSource)}, which may be the best way to ensure the same results
     * across versions.
     */
    public RNG() {
        this(new DiverRNG());
    }

    /**
     * Default constructor; uses {@link DiverRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers. The seed can be
     * any long, including 0.
     * @param seed any long
     */
    public RNG(long seed) {
        this(new DiverRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode,
     * instead using {@link CrossHash#hash64(CharSequence)}) as a seed for {@link DiverRNG}, which is of high quality,
     * but low period (which rarely matters for games), and has excellent speed, tiny state size, and natively generates
     * 64-bit numbers.
     */
    public RNG(CharSequence seedString) {
        this(new DiverRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This constructor should be used if an alternate
     * RandomnessSource other than DiverRNG is desirable, such as to keep compatibility with earlier SquidLib
     * versions that defaulted to MersenneTwister, LightRNG, ThrustAltRNG, or LinnormRNG.
     * <br>
     * If the parameter is null, this is equivalent to using {@link #RNG()} as the constructor.
     * @param random the source of pseudo-randomness, such as a LightRNG or LongPeriodRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = (random == null) ? new DiverRNG() : random;
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
         * a DiverRNG with a random seed.
         */
        public CustomRandom() {
            randomnessSource = new DiverRNG();
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

        /**
         * Returns the next pseudorandom, uniformly distributed {@code long}
         * value from this random number generator's sequence. The general
         * contract of {@code nextLong} is that one {@code long} value is
         * pseudorandomly generated and returned.
         *
         * @return the next pseudorandom, uniformly distributed {@code long}
         * value from this random number generator's sequence
         */
        @Override
        public long nextLong() {
            return randomnessSource.nextLong();
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
     * Returns a value from an even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <br>
     * The inclusive and exclusive behavior is to match the behavior of the similar
     * method that deals with floating point values.
     * <br>
     * If {@code min} and {@code max} happen to be the same, {@code min} is returned
     * (breaking the exclusive behavior, but it's convenient to do so).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public <T> T getRandomElement(List<T> list) {
        if (list.isEmpty()) {
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
    @Override
    public <T> T getRandomElement(Collection<T> coll) {
        int n;
        if ((n = coll.size()) <= 0) {
            return null;
        }
        n = nextInt(n);
        T t = null;
        Iterator<T> it = coll.iterator();
        while (n-- >= 0 && it.hasNext())
            t = it.next();
        return t;
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
     * Mutates the array arr by switching the contents at pos1 and pos2.
     * @param arr an array of T; must not be null
     * @param pos1 an index into arr; must be at least 0 and no greater than arr.length
     * @param pos2 an index into arr; must be at least 0 and no greater than arr.length
     */
    private static <T> void swap(T[] arr, int pos1, int pos2) {
        final T tmp = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = tmp;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy.
     * GWT-compatible since GWT 2.8.0, which is the default if you use libGDX 1.9.5 or higher.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(final T[] elements) {
        final int size = elements.length;
        final T[] array = Arrays.copyOf(elements, size);
        for (int i = size; i > 1; i--) {
            swap(array, i - 1, nextIntHasty(i));
        }
        return array;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm.
     * If you don't want the array modified, use {@link #shuffle(Object[], Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    @Override
    public <T> T[] shuffleInPlace(T[] elements) {
        final int size = elements.length;
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextIntHasty(i));
        }
        return elements;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm. If possible, create a new array or reuse an existing array
     * with the same length as elements and pass it in as dest; the dest array will contain the shuffled contents of
     * elements and will also be returned as-is.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *                 randomPortion method of this class to fill the smaller dest.
     * @return {@code dest} after modifications
     */
    @Override
    public <T> T[] shuffle(T[] elements, T[] dest) {
        if (dest.length != elements.length)
            return randomPortion(elements, dest);
        System.arraycopy(elements, 0, dest, 0, elements.length);
        shuffleInPlace(dest);
        return dest;
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
        return shuffle(elements, null);
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm. The result
     * is allocated if {@code buf} is null or if {@code buf} isn't empty,
     * otherwise {@code elements} is poured into {@code buf}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param buf a buffer as an ArrayList that will be filled with the shuffled contents of elements;
     *            if null or non-empty, a new ArrayList will be allocated and returned
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements, /*@Nullable*/ ArrayList<T> buf) {
        final ArrayList<T> al;
        if (buf == null || !buf.isEmpty())
            al = new ArrayList<>(elements);
        else {
            al = buf;
            al.addAll(elements);
        }
        final int n = al.size();
        for (int i = n; i > 1; i--) {
            Collections.swap(al, nextInt(i), i - 1);
        }
        return al;
    }
    /**
     * Shuffles a Collection of T items in-place using the Fisher-Yates algorithm.
     * This only shuffles List data structures.
     * If you don't want the array modified, use {@link #shuffle(Collection)}, which returns a List as well.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements a Collection of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    @Override
    public <T> List<T> shuffleInPlace(List<T> elements) {
        final int n = elements.size();
        for (int i = n; i > 1; i--) {
            Collections.swap(elements, nextInt(i), i - 1);
        }
        return elements;
    }


    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    @Override
    public int[] randomOrdering(int length) {
        if (length <= 0)
            return new int[0];
        return randomOrdering(length, new int[length]);
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive) and stores it in
     * the dest parameter, avoiding allocations.
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @param dest   the destination array; will be modified
     * @return dest, filled with a random ordering containing all ints from 0 to length (exclusive)
     */
    @Override
    public int[] randomOrdering(int length, int[] dest) {
        if (dest == null) return null;

        final int n = Math.min(length, dest.length);
        for (int i = 0; i < n; i++) {
            dest[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            final int r = nextIntHasty(i+1),
                    t = dest[r];
            dest[r] = dest[i];
            dest[i] = t;
        }
        return dest;
    }

    /**
     * Gets a random portion of a Collection and returns it as a new List. Will only use a given position in the given
     * Collection at most once; does this by shuffling a copy of the Collection and getting a section of it.
     *
     * @param data  a Collection of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T>   can be any non-primitive type
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    public <T> List<T> randomPortion(Collection<T> data, int count) {
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
     * approach -1f and 1f, but not go beyond those bounds. This is similar to {@link Random#nextGaussian()} in that it
     * uses a curved distribution, but it is not the same. The distribution for the values is similar to Irwin-Hall, and
     * is frequently near 0 but not too-rarely near -1f or 1f. It cannot produce values greater than or equal to 1f, or
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
     * default DiverRNG will behave fine, as will LightRNG and ThrustAltRNG (these all use similar algorithms), but
     * the  normally-high-quality XoRoRNG will produce very predictable output with {@code rng.next(1)} and very good
     * output with {@code rng.nextBoolean()}. This is a known and considered flaw of Xoroshiro128+, the algorithm used
     * by XoRoRNG, and a large number of generators have lower quality on the least-significant bit than the most-
     * significant bit, where this method only checks the most-significant bit.
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets
     * exactly one random number, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = random.nextLong();
        if (bound <= 0) return 0;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
    }
    /**
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time. This uses a biased technique to get numbers from
     * large ranges, but the amount of bias is incredibly small (expected to be under 1/1000 if enough random ranged
     * numbers are requested, which is about the same as an unbiased method that was also considered).It may have
     * noticeable bias if the generator's period is exhausted by only calls to this method. Unlike all unbiased methods,
     * this advances the state by an equivalent to exactly one call to {@link #nextLong()}, where rejection sampling
     * would sometimes advance by one call, but other times by arbitrarily many more.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets at
     * most one random number, so it advances the state as much as {@link #nextInt(int)} or {@link #nextLong()}.
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextSignedLong(long bound) {
        long rand = random.nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * This is almost identical to the earlier {@link #nextIntHasty(int)} except that it will perform better when the
     * RandomnessSource this uses natively produces 32-bit output. It was added to the existing nextIntHasty() so
     * existing code using nextIntHasty would produce the same results, but new code matches the API with
     * {@link #nextSignedLong(long)}. This is implemented slightly differently in {@link AbstractRNG}, and different
     * results should be expected when using code based on that abstract class.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextSignedInt(final int bound) {
        return (int) ((bound * (long)random.next(31)) >> 31);
    }

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative. Always makes one call to the {@link RandomnessSource#next(int)} method of the RandomnessSource that
     * would be returned by {@link #getRandomness()}, even if bound is 0 or negative, to avoid branching and also to
     * ensure consistent advancement rates for the RandomnessSource (this can be important if you use a
     * {@link SkippingRandomness} and want to go back before a result was produced).
     * <br>
     * This method changed a fair amount on April 5, 2018 to better support RandomnessSource implementations with a
     * slower nextLong() method, such as {@link Lathe32RNG}, and to avoid branching/irregular state advancement/modulus
     * operations. It is now almost identical to {@link #nextIntHasty(int)}, but won't return negative results if bound
     * is negative (matching its previous behavior). This may have statistical issues (small ones) if bound is very
     * large (the estimate is still at least a bound of a billion or more before issues are observable). Consider
     * {@link #nextSignedInt(int)} if the bound should be allowed to be negative; {@link #nextIntHasty(int)} is here for
     * compatibility with earlier versions, but the two methods are very similar.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(final int bound) {
        return (int) ((bound * ((long)random.next(31))) >>> 31) & ~(bound >> 31);
//        int threshold = (0x7fffffff - bound + 1) % bound;
//        for (; ; ) {
//            int bits = random.next(31);
//            if (bits >= threshold)
//                return bits % bound;
//        }
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method is no more "hasty" than {@link #nextInt(int)}, but it is a little
     * faster than that method because this avoids special behavior for when bound is negative.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextIntHasty(final int bound) {
        return (int) ((bound * (random.nextLong() & 0x7FFFFFFFL)) >> 31);
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
    @Override
    public int nextInt() {
        return random.next(32);
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
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
    @Override
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
     * it can, and then returns output. Will only use a given position in the given data at most once; uses the
     * Swap-Or-Not Shuffle to accomplish this without allocations. Internally, makes 1 call to {@link #nextInt()} and
     * 6 calls to {@link #nextSignedInt(int)}, regardless of the data being randomized. It will get progressively less
     * random as output gets larger.
     * <br>
     * Uses approximately the same code as the Swap-or-Not Shuffle, but without any object or array allocations.
     *
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return output, after {@code Math.min(output.length, data.length)} unique items have been put into it from data
     */
    public <T> T[] randomPortion(T[] data, T[] output) {
        final int length = data.length, n = Math.min(length, output.length),
                func = nextInt(),
                a = nextSignedInt(length), b = nextSignedInt(length), c = nextSignedInt(length),
                d = nextSignedInt(length), e = nextSignedInt(length), f = nextSignedInt(length);
        int key, index;
        for (int i = 0; i < n; i++) {
            index = i;
            key = a - index;
            key += (key >> 31 & length);
            if(((func) + Math.max(index, key) & 1) == 0) index = key;
            key = b - index;
            key += (key >> 31 & length);
            if(((func >>> 1) + Math.max(index, key) & 1) == 0) index = key;
            key = c - index;
            key += (key >> 31 & length);
            if(((func >>> 2) + Math.max(index, key) & 1) == 0) index = key;
            key = d - index;
            key += (key >> 31 & length);
            if(((func >>> 3) + Math.max(index, key) & 1) == 0) index = key;
            key = e - index;
            key += (key >> 31 & length);
            if(((func >>> 4) + Math.max(index, key) & 1) == 0) index = key;
            key = f - index;
            key += (key >> 31 & length);
            if(((func >>> 5) + Math.max(index, key) & 1) == 0) index = key;

            output[i] = data[index];
        }
        return output;
    }


    /**
     * Gets a random double between 0.0 inclusive and 1.0 inclusive.
     *
     * @return a double between 0.0 (inclusive) and 1.0 (inclusive)
     */
    public double nextDoubleInclusive()
    {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1.0000000000001p-53;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and outer (inclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer inclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (inclusive)
     */
    public double nextDoubleInclusive(final double outer) {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1.0000000000001p-53 * outer;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f inclusive.
     *
     * @return a float between 0f (inclusive) and 1f (inclusive)
     */
    public float nextFloatInclusive() {
        return random.next(24) * 0x1.000002p-24f;
    }

    /**
     * This returns a random float between 0.0f (inclusive) and outer (inclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer inclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (inclusive)
     */
    public float nextFloatInclusive(final float outer) {
        return random.next(24) * 0x1.000002p-24f * outer;
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
