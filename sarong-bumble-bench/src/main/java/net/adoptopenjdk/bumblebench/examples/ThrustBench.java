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

import net.adoptopenjdk.bumblebench.core.MicroBench;
import sarong.ThrustRNG;

/**
 * ThrustBench score: 4356944384.000000 (4.357G 2219.5%)
 *         uncertainty:   0.5%
 * Note, ThrustRNG hasn't yet passed PractRand, and may need adjustments,
 * but this is a good result!
 */
public final class ThrustBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		ThrustRNG rng = new ThrustRNG(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
}

