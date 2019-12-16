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
 * CantorPairingBench score: 73935544.000000 (73.94M 1811.9%)
 *                uncertainty:   2.5%
 */
public final class CantorPairingBench extends MiniBench {

	public final int cantorPair(int x, int y) {
		x = x << 1 ^ x >> 31;
		y = y << 1 ^ y >> 31;
		return y + ((x+y) * (x+y+1) >> 1);
	}
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		int sum = 0x9E3779B9;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				sum += cantorPair(j, sum + 0x9E3779B9 >>> 12);
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

