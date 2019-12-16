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
 * RosenbergStrongUnpairingBench score: 65782164.000000 (65.78M 1800.2%)
 *                           uncertainty:   0.5%
 */
public final class RosenbergStrongUnpairingBench extends MiniBench {

	public final int rosenbergStrongUnpairShared(int z)
	{
		return (int) Math.sqrt(z);
	}

	public final int rosenbergStrongUnpairX(int z, int m)
	{
		final int zm = z - m * m;
		z = Math.min(zm, m);
		return -(z & 1) ^ z >>> 1;
	}

	public final int rosenbergStrongUnpairY(int z, int m)
	{
		final int zm = z - m * m;
		z = (zm < m) ? m : (m << 1) - zm;
		return -(z & 1) ^ z >>> 1;
	}
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		pauseTimer();
		int x = 0x9E3779B9, y = 0x12345678, w;
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				w = rosenbergStrongUnpairShared(j ^ x ^ y);
				x += rosenbergStrongUnpairX(j, w);
				y += rosenbergStrongUnpairY(j, w);
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

