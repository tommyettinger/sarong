package sarong;

/**
 * An improvement on the performance of http://extremelearning.com.au/isotropic-blue-noise-point-sets/ .
 * Does not currently validate that the permutations it has found are distinct from each other.
 */
public class PermutationEtc {

	/**
	 * Change SIZE to increase the size of the balanced permutations.
	 */
	public static final int SIZE = 30, HALF_SIZE = SIZE >>> 1;

	public PermutationEtc(){}
	public PermutationEtc(long state){
		stateA = state;
	}
	/**
	 * Can be any long.
	 */
	private long stateA = 12345678987654321L; //System.nanoTime();
	/**
	 * Must be odd.
	 */
	private long stateB = 0x1337DEADBEEFL;

	/**
	 * It's a weird RNG. Returns a slightly-biased pseudo-random int between 0 inclusive and bound exclusive. The bias comes from
	 * not completely implementing Daniel Lemire's fastrange algorithm, but it should only be relevant for huge bounds. The number
	 * generator itself passes PractRand without anomalies, has a state size of 127 bits, and a period of 2 to the 127.
	 * @param bound upper exclusive bound
	 * @return an int between 0 (inclusive) and bound (exclusive)
	 */
	private int nextIntBounded (int bound) {
		final long s = (stateA += 0xC6BC279692B5C323L);
		final long z = ((s < 0x800000006F17146DL) ? stateB : (stateB += 0x9479D2858AF899E6L)) * (s ^ s >>> 31);
		return (int)(bound * ((z ^ z >>> 25) & 0xFFFFFFFFL) >>> 32);
	}

	private static void swap(int[] arr, int pos1, int pos2) {
		final int tmp = arr[pos1];
		arr[pos1] = arr[pos2];
		arr[pos2] = tmp;
	}

	/**
	 * Fisher-Yates and/or Knuth shuffle, done in-place on an int array.
	 * @param elements will be modified in-place by a relatively fair shuffle
	 */
	public void shuffleInPlace(int[] elements) {
		final int size = elements.length;
		for (int i = size; i > 1; i--) {
			swap(elements, i - 1, nextIntBounded(i));
		}
	}

	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		PermutationEtc p = new PermutationEtc(startTime);
		final int[] items = new int[SIZE], delta = new int[SIZE], targets = new int[SIZE];
		for (int g = 0; g < 100; g++) {
			BIG_LOOP:
			while (true) {
				for (int i = 0; i < HALF_SIZE; i++) {
					delta[i] = i + 1;
					delta[i + HALF_SIZE] = ~i;
				}
				p.shuffleInPlace(delta);
				for (int i = 0; i < SIZE; i++) {
					targets[i] = i + 1; 
				}
				targets[(items[0] = p.nextIntBounded(SIZE) + 1) - 1] = 0;
				for (int i = 1; i < SIZE; i++) {
					int d = 0;
					for (int j = 0; j < SIZE; j++) {
						d = delta[j];
						if(d == 0) continue;
						int t = items[i-1] + d;
						if(t > 0 && t <= SIZE && targets[t-1] != 0){
							items[i] = t;
							targets[t-1] = 0;
							delta[j] = 0;
							break;
						}
						else d = 0;
					}
					if(d == 0) continue BIG_LOOP;
				}
				int d = items[0] - items[SIZE - 1];
				for (int j = 0; j < SIZE; j++) {
					if(d == delta[j]) {
						System.out.print(items[0]);
						for (int i = 1; i < SIZE; i++) {
							System.out.print(", " + items[i]);
						}
						System.out.println();
						break BIG_LOOP;
					}
				}
				break;
			}
		}
		System.out.println("Took " + (System.currentTimeMillis() - startTime) * 0.001 +
			" seconds to generate 100 sequences with size " + SIZE);
	}
}
