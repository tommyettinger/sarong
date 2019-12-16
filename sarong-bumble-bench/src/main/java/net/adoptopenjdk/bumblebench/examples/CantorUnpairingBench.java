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
 * CantorUnpairingBench score: 83045640.000000 (83.05M 1823.5%)
 *                  uncertainty:   1.2%
 */
public final class CantorUnpairingBench extends MiniBench {

	public final int cantorUnpairShared(long z)
	{
		return (int) Math.sqrt(z << 3 | 1) - 1 >>> 1;
	}

	public final int cantorUnpairX(int z, int w)
	{
		z -= (w * (w + 1) >> 1);
		return -(z & 1) ^ z >>> 1;
	}

	public final int cantorUnpairY(int z, int w)
	{
		z = (w * (w + 3) >> 1) - z;
		return -(z & 1) ^ z >>> 1;
	}
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		int x = 0x9E3779B9, y = 0x12345678, w;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				w = cantorUnpairShared(j ^ x ^ y);
				x += cantorUnpairX(j, w);
				y += cantorUnpairY(j, w);
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

