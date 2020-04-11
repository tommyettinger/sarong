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
import sarong.util.CrossHash;

/**
 * On Windows laptop, 6th gen i7 processor:
 * <br>
 *  Olm64IntHashBench score: 242126.203125 (242.1K 1239.7%)
 *               uncertainty:   0.3%
 */
public final class Olm64IntHashBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 300007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		final int[] data = new int[2100];
		LargeArrayGenerator.generate(-1, 10000, data);
		long result = 0;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				result += CrossHash.Olm.hash64(data);
				pauseTimer();
				LargeArrayGenerator.generate(j, 9999 - j, data);
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

