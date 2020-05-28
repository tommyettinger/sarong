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

import java.util.Arrays;

/**
 * With data size 2100:
 * <br>
 * On Windows laptop, 6th gen i7 processor:
 * <br>
 * ArraysHashcodeIntBench score: 452305.968750 (452.3K 1302.2%)
 *                    uncertainty:   0.2%
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * ArraysHashcodeIntBench score: 639157.562500 (639.2K 1336.8%)
 *                    uncertainty:   0.1%
 * <br>
 * With much smaller data size (10):
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * ArraysHashcodeIntBench score: 54962792.000000 (54.96M 1782.2%)
 *                    uncertainty:   0.0%
 * <br>
 * With mid-range data size (50):
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * ArraysHashcodeIntBench score: 18998726.000000 (19.00M 1676.0%)
 *                    uncertainty:   0.1%
 * <br>
 * With larger data size (10000):
 * <br>
 * On Linux laptop, 8th gen i7 processor (JDK 8 HotSpot)
 * <br>
 * ArraysHashcodeIntBench score: 135304.781250 (135.3K 1181.5%)
 *                    uncertainty:   0.2%
 */
public final class ArraysHashcodeIntBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 300007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		final int[] data = new int[SharedConstants.DATA_SIZE];
		LargeArrayGenerator.generate(-1, 10000, data);
		int result = 0;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				result += Arrays.hashCode(data);
				pauseTimer();
				LargeArrayGenerator.generate(j + result, 9999 - j, data);
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

