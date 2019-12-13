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
 * MathAbsLongBench score: 58895120.000000 (58.90M 1789.1%)
 *              uncertainty:   0.3%
 */
public final class MathAbsLongBench extends MiniBench {

	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		long sum = 0x9E3779B97F4A7C15L;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				sum += Math.abs(sum) + 0x6E3779B97F4A7C15L;
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

