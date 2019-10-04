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
import sarong.Orbit32RNG;

/**
 * Older code; probably not as high-quality. Tested on a newer Linux machine
 * running OpenJDK 8 with Hotspot:
 * <br>
 * Orbit32Bench score: 811839488.000000 (811.8M 2051.5%)
 *          uncertainty:   0.2%
 * <br>
 * Tested on an older Windows machine running OpenJDK 13 with OpenJ9:
 * <br>
 * Orbit32Bench score: 1147231744.000000 (1.147G 2086.1%)
 *          uncertainty:   0.1%
 * <br>
 * That's the fastest a 32-bit generator I've tested has gotten. An earlier version wasn't
 * GWT-safe (it would produce different results on GWT than on desktop JVMs), but making the
 * generator more compatible also sped it up, because it has to avoid large multipliers.
 * <br>
 * It isn't as fast on older non-J9 JDKs; on the same older machine running OpenJDK 8 with Hotspot:
 * <br>
 * Orbit32Bench score: 427554944.000000 (427.6M 1987.4%)
 *          uncertainty:   0.2%
 * <br>
 * It isn't as fast on older JDKs in general; on the same older machine running OpenJDK 12 with OpenJ9:
 * <br>
 * Orbit32Bench score: 402202656.000000 (402.2M 1981.2%)
 *          uncertainty:   0.3%
 * <br>
 * Just as an extra note, here this is benchmarked on IBM J9 for JDK 8 (before OpenJ9):
 * <br>
 * Orbit32Bench score: 11511290880.000000 (11.51G 2316.7%)
 *          uncertainty:   0.2%
 * <br>
 * I'm almost certain that speed is misleading, and some optimization is eliminating the loop contents.
 * I think this because GWTRNGBench on the same version of J9 showed its throughput as a 27-digit (base
 * 10) number, before crashing for entirely predictable reasons.
 */
public final class Orbit32Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		Orbit32RNG rng = new Orbit32RNG(0x1234, 0x5678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

