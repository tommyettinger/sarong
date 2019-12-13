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
 * Running with HotSpot, OpenJDK 13.0.0.33:
 * <br>
 * AltAbsLongBench score: 62545384.000000 (62.55M 1795.1%)
 *             uncertainty:   0.5%
 */
public final class AltAbsLongBench extends MiniBench {

	public final long altAbs(final long a) {
		final long b = a >> 63;
		return (a ^ b) - b;
	}
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		long sum = 0x9E3779B97F4A7C15L;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				sum += altAbs(sum) + 0x6E3779B97F4A7C15L;
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

