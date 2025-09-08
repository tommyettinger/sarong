# Semi-Archived

Sarong is no longer under active development as a library for end-users. It never had any projects depend on it, to my
knowledge, and certainly didn't have many generators with adequate quality for serious usage. It is still updated in its
non-user-facing tests, because I have a lot of tests here already and I don't want to move them. You should strongly
consider using a newer, better library for random numbers, such as [juniper](https://github.com/tommyettinger/juniper).

Juniper generally considers generators to be high-quality if they pass 64TB of
[PractRand](https://pracrand.sourceforge.net) testing with few or no anomalies and no failures. In contrast, sarong
dates back to a time when the only tests I could run required writing a large file of random `long`s to disk and sending
it to PractRand, which sharply limited the amount of data I could test on and made generators appear better than they
actually were. Some may have been tested with DieHard, which is unusually easy to pass tests on because it only tests
31 bits and has a bias towards testing the high bits, or
[DieHarder](https://webhome.phy.duke.edu/~rgb/General/dieharder.php), which is a joke as far as a test suite for PRNGs
because it isn't even deterministic itself (no DieHarder test passes or failures are replicable).

Juniper also provides various statistical distributions, mostly where the algorithms could be ported from the .NET
library[Troschuetz.Random](https://gitlab.com/pomma89/troschuetz-random), which sarong does not.  Juniper depends on
[digital](https://github.com/tommyettinger/digital), which provides two fairly-high-quality non-cryptographic hashing
algorithms, both of which should be comparable to or better than the algorithms in `CrossHash` here.

So, yeah, use juniper and don't look back!

# sarong

Sarong provides Stand-Alone RandOm Number Generators, essentially pulled directly from the author's
work in [SquidLib](https://github.com/SquidPony/SquidLib), but with scope reduced to just random
number generators and hashing functions for arrays and text (useful for String seeds for RNGs).

## Usage

If you need a fast pseudo-random number generator with good quality, you may be satisfied with
Java 8's `java.util.SplittableRandom` class in many cases. However, SplittableRandom is only present
in Java 8, is not necessarily available on GWT, and its period (the number of numbers that it can
generate before it repeats the sequence) is a relatively low `2^64`. Each of these issues has some way
it can be addressed. For platform and version support, `sarong.LightRNG` uses the same algorithm as
SplittableRandom, called SplitMix64, and is similarly fast, but is additionally available on Java 6
or 7 (plus GWT and Android) because sarong is highly compatible. As for the low period, you can switch
to `sarong.XoRoRNG` (which implements XoRoShiRo 128+, one of the best generators in C but about the
same speed as LightRNG here on the JVM) to get a period of `(2^128)-1`, or `sarong.discouraged.ThunderRNG` (which
is pretty much a novel algorithm, and is a good deal faster than LightRNG but has worse statistical
traits) to get a period of about `2^127`, although the period is definitely lower for less-significant
bits in the generated numbers and probably lower for some of the higher bits as well. At the extreme
end, there's `sarong.LongPeriodRNG`, which has a period of `(2^1024)-1`, and `sarong.IsaacRNG`, which
has a period that can be described simply with "really, really big", and though it is at minimum `2^40`
for worst-case seeds, the period is on average `2^8295`. There's also `sarong.PintRNG`, which is meant
specifically for GWT, and avoids arithmetic with `long` values wherever possible because that tends to
be much slower on GWT. PintRNG is a simple modification of `sarong.PermutedRNG`; both are based on
[PCG-Random](http://www.pcg-random.org/), and though PCG is excellent in C/C++, here on the JVM it can
only be recommended in very limited circumstances -- it has excellent statistical qualities but a low
period (same as LightRNG) and is slower all around due to JVM quirks. Sarong also supplies two
"quasi-random number generators," which get numbers from sub-random sequences instead of pseudo-random
ones and generally have properties that can be considered desirable when producing random points in
2D, 3D, or higher dimensions that should not be too close to each other in distance. Neither
`sarong.SobolQRNG` nor `sarong.VanDerCorputQRNG` will probably be seen much outside some specialized
usage; see their JavaDocs for more information.

Usually you'd use LightRNG, LongPeriodRNG, PintRNG, XoRoRNG, or any other class implementing
`sarong.RandomnessSource` by giving it to the constructor for `sarong.RNG`, a wrapper that adds
many features, including:
  * A wide variety of methods for common usage, like `nextLong(long)` or `between(double, double)`.
  * The ability to get a `java.util.Random` subclass that produces numbers with the same properties
    as the RandomnessSource that RNG uses.
  * Methods to shuffle arrays or Collections, as well as to generate random orderings that can be
    applied to order collections in certain other libraries (SquidLib uses this, and work is being
    done on the [Salp data structures library](https://github.com/tommyettinger/salp) to make use of
    this and similar features).
You may want a more specialized variant on RNG, and there are several already.
  * `sarong.StatefulRNG` is a simple subclass of RNG that almost always uses LightRNG for its
    RandomnessSource implementation, but in turn allows getting and setting the current state as
    a long.
  * `sarong.DeckRNG` is an interesting "sub-randomizing" variant on a normal RNG that generates 16
    results at a time, each one in a different range of values (always with as many in the lower half
    as in the upper half of values) and hands them out like it's dealing cards, ensuring that no
    perceived "streak of bad luck" can persist very long, and when low numbers have been returned for
    a few generations, high numbers are sure to follow soon. It essentially makes the Gambler's
    Fallacy into a reliable trait of the RNG.
  * `sarong.EditRNG` allows tweaking the distribution of the RNG to favor lower or higher averages,
    as well as more or less central results (that is, how often it produces values that are close to
    or far from the average). It is related to `sarong.RandomBias`, which can be used to process
    any numbers to add a bias like EditRNG is able to.

You may also want to use the hashing functions this has in `sarong.util.CrossHash` for some reason;
they are rather fast and work cross-platform, but they can't compare on desktop usage to dedicated
hashing libraries like CityHash and XXHash. It's possible the speed will be competitive with some of
those hashes, though. All three kinds this currently supplies are faster than the implementation of
SipHash that once was here, and SipHash was removed because it was roughly 7-8x slower than Wisp,
the default hash used here, with little noticeable quality change. CrossHash supplies four different
types of hash algorithm, each able to generate 64-bit and 32-bit hashes. The default is used with
just `CrossHash.hash64()` or `CrossHash.hash()` and is nicknamed Wisp; it is the fastest of the four
hash types, has good quality, and may have particular benefits on GWT since the 32-bit hash() methods
don't use the slow, emulated 64-bit math on GWT unless hashing long or double arrays. It also avoids
certain other code that only performs well on certain Java runtime environments (Long.rotateLeft
needs a somewhat-modern processor and a JRE run with the -server flag to be as fast as it can be, and
Wisp avoids using this while Lightning and Storm do not). An alternative is Lightning, which has
rather high quality and good speed, and doesn't have any serious flaws in inputs found so far.
Lightning doesn't have any ability to "salt" the hash, changing the generated values in an
unpredictable way using an extra parameter at construction; `CrossHash.Storm` does, and so it offers
essentially the best quality of any of the three. Storm is slightly slower than Lightning, but
`CrossHash.Falcon` is faster than both, though it is slower than the default Wisp. Falcon has a
significant flaw if you generate 32-bit hashes using `long` or `double` arrays for input; half of
the bits in each input are discarded, and this is especially bad for `double` arrays because much of
the information is in the upper (discarded) half of bits. You should only use Falcon if you either
only use 64-bit results or only give 32-bit types for input (including Object arrays, which are OK);
otherwise use Wisp or Lightning.

## Installation
This project has no run-time dependencies other than Java 6 or higher and should work on GWT. To add
sarong as a dependency for Maven, Gradle, or some other JVM build tool, you can use
[the info on Maven Central](http://search.maven.org/#artifactdetails%7Ccom.github.tommyettinger%7Csarong%7C0.5%7Cjar)
or, for the easy cases of Maven and Gradle:

Maven dependency, latest stable:

```maven-pom
<dependency>
    <groupId>com.github.tommyettinger</groupId>
    <artifactId>sarong</artifactId>
    <version>0.5</version>
</dependency>
```

Gradle dependency, latest stable:

```groovy
compile 'com.github.tommyettinger:sarong:0.5'
```

I hope this can be useful!
