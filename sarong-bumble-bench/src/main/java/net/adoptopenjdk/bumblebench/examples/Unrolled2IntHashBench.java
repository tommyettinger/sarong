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

import net.adoptopenjdk.bumblebench.core.MiniBench;

/**
 * With data size 2100:
 * On Windows laptop, 6th gen i7 processor:
 * <br>
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * Unrolled2IntHashBench score: 1255585.375000 (1.256M 1404.3%)
 *                   uncertainty:   0.1%
 * <br>
 * With much smaller data size (10):
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * Unrolled2IntHashBench score: 49594004.000000 (49.59M 1771.9%)
 *                   uncertainty:   0.1%
 */
public final class Unrolled2IntHashBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 300007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		final int[] data = new int[SharedConstants.DATA_SIZE];
		LargeArrayGenerator.generate(-1, 10000, data);
		final DumbHash hash = new DumbHash(1);
		int result = 0;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				result += hash.unrolledHash2(data);
				pauseTimer();
				LargeArrayGenerator.generate(j + result, 9999 - j, data);
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

