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
 * AltAbs2Bench score: 68268416.000000 (68.27M 1803.9%)
 *          uncertainty:   1.0%
 */
public final class AltAbs2Bench extends MiniBench {

	public final int altAbs(final int a) {
		final int b = a >> 31;
		return (a ^ b) - b;
	}
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		int sum = 0x9E3779B9;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				sum += altAbs(sum) + 0x6E3779B9;
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

