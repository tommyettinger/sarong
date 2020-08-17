/*******************************************************************************
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package net.adoptopenjdk.bumblebench.examples;

import net.adoptopenjdk.bumblebench.core.MicroBench;
import sarong.RandomnessSource;

import java.io.Serializable;

/**
 * With Java 8, HotSpot, on an 8th-gen i7 hexacore mobile processor running Manjaro Linux:
 * <br>
 * GhostBench score: 1154841856.000000 (1.155G 2086.7%)
 *        uncertainty:   0.1%
 * <br>
 * With Java 14, OpenJ9 (build 20200327_17), same hardware:
 * <br>
 * GhostBench score: 1019444992.000000 (1.019G 2074.3%)
 *        uncertainty:   0.1%
 */
public final class GhostBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		GhostRNG rng = new GhostRNG(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
	public static final class GhostRNG implements RandomnessSource, Serializable {
		private static final long serialVersionUID = 1L;
		public long stateA;
		public long stateB;

		public GhostRNG() {
			this((long) ((Math.random() - 0.5) * 0x10000000000000L)
							^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
					(long) ((Math.random() - 0.5) * 0x10000000000000L)
							^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
		}

		public GhostRNG(long seed) {
			stateA = (seed = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25);
			stateB =         (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
		}

		public GhostRNG(final long seedA, final long seedB) {
			stateA = seedA;
			stateB = seedB;
		}

		@Override
		public final int next(final int bits) {
			long s = (stateA += 0xCB9C59B3F9F87D4DL);
			final long t = s == 0L ? stateB : (stateB += 0x3463A64C060782B1L);
			s = (s ^ s >>> 31) * ((t ^ t << 9) | 1L);
			return (int)(s ^ s >>> 27) >>> -bits;
		}
		
		@Override
		public final long nextLong() {
			// long s = (stateA += 0xD1342543DE82EF95L);
			// s ^= s >>> 31 ^ s >>> 23;
			// if(s == 0L) {
			// 	final long t = stateB;
			// 	s *= ((t ^ t << 9) | 1L);
			// 	return s ^ s >>> 25;
			// }
			// else {
			// 	final long t = (stateB += 0xB1E131D6149D9795L);
			// 	s *= ((t ^ t << 9) | 1L);
			// 	return s ^ s >>> 25;
			// }
            //		final long s = (stateA += 0xD1342543DE82EF95L);
            //        final long z = (s ^ s >>> 31 ^ s >> 23) * (stateB | 1L);
            //		//0x46BC279692B5C323L
            //		//0x4000000000000000L
            //        if (s > 0x3000000000000000L) stateB += 0xB1E131D6149D9795L;
            //        return z ^ z >>> 25;

			final long s = (stateA += 0xCB9C59B3F9F87D4DL);
			final long t = (s == 0L ? 0L : (stateB += 0x3463A64C060782B2L) * (s ^ s >>> 31));
			return t ^ t >>> 27;
		}

		@Override
		public GhostRNG copy() {
			return new GhostRNG(stateA, stateB);
		}
	}
}

