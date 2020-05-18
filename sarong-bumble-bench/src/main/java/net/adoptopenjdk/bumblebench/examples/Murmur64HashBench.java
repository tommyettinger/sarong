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
import net.openhft.hashing.LongHashFunction;

/**
 */
public final class Murmur64HashBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 300007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		final long[] data = new long[SharedConstants.DATA_SIZE];
		LargeArrayGenerator.generate(-1L, data);
		LongHashFunction murmur3 = LongHashFunction.murmur_3();
		long result = 0;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				result += murmur3.hashLongs(data);
				pauseTimer();
				LargeArrayGenerator.generate(j, data);
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

