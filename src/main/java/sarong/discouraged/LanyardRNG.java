package sarong.discouraged;

import sarong.LightRNG;
import sarong.RandomnessSource;
import sarong.ThrustAltRNG;

/**
 * An interesting idea for an RNG algo, LanyardRNG is a simplified, non-splittable version of TwinLinear (which appears
 * to be designed to replace the JDK's splitmix64 algorithm used in SplittableRandom). Unlike spltmix64, which is in
 * this library as {@link LightRNG} (also as a non-splittable version), TwinLinear cannot produce all possible outputs,
 * which makes it closer to {@link ThrustAltRNG} than most of the "good" RNGs here. It has two longs of state, but only
 * has a period of 2 to the 64 because the states overlap entirely. It can be expected to be unable to produce roughly a
 * third of all possible long results, though it may be able to produce all int results with varying frequencies.
 * <br>
 * Based on the paper <a href="https://labs.oracle.com/pls/apex/f?p=LABS:0::APPLICATION_PROCESS%3DGETDOC_INLINE:::DOC_ID:1050">
 * Better Splittable Pseudorandom Number Generators (and Almost As Fast)</a> by anonymous author(s) (probably Guy Steele Jr.).
 * <br>
 * Created by Tommy Ettinger on 6/22/2019.
 */
public class LanyardRNG implements RandomnessSource {
	private long a, b;

	public LanyardRNG()
	{
		this((long) ((Math.random() - 0.5) * 0x10000000000000L)
				^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
				(long) ((Math.random() - 0.5) * 0x10000000000000L)
						^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
	}
	public LanyardRNG(long stateA, long stateB)
	{
		a = stateA;
		b = stateB;
	}
	
	public int next(int bits)
	{
		long r = (a << 32 | a >>> 32) ^ b;
		final long t = a >>> 58;
		r = (r << t | r >>> -t);
		r *= 0x2545F4914F6CDD1DL;
		a = a * 0x2C6FE96EE78B6955L + 5;
		b = b * 0x369DEA0F31A53F85L + 17;
		return (int)(r >>> 64 - bits);
	}
	public long nextLong() {
		long r = (a << 32 | a >>> 32) ^ b;
		final long t = a >>> 58;
		r = (r << t | r >>> -t);
		r *= 0x2545F4914F6CDD1DL;
		a = a * 0x2C6FE96EE78B6955L + 5;
		b = b * 0x369DEA0F31A53F85L + 17;
		return r ^ r >>> 32;
	}

	@Override
	public LanyardRNG copy() {
		return new LanyardRNG(a, b);
	}
}
