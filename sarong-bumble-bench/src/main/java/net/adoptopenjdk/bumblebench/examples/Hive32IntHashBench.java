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
 * Hive32IntHashBench score: 321823.625000 (321.8K 1268.2%)
 *                uncertainty:   0.3%
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
 * Hive32IntHashBench score: 52656432.000000 (52.66M 1777.9%)
 *                uncertainty:   0.4%
 * <br>
 * With mid-range data size (50):
 * <br>
 * Hive32IntHashBench score: 14193711.000000 (14.19M 1646.8%)
 *                uncertainty:   0.1%
 * <br>
 * With larger data size (10000):
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * Hive32IntHashBench score: 77463.421875 (77.46K 1125.8%)
 *                uncertainty:   0.2%
 */
public final class Hive32IntHashBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 300007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		final int[] data = new int[SharedConstants.DATA_SIZE];
		LargeArrayGenerator.generate(-1, 10000, data);
		int result = 0;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				result += CrossHash.Hive.hash(data);
				pauseTimer();
				LargeArrayGenerator.generate(j, 9999 - j, data);
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

