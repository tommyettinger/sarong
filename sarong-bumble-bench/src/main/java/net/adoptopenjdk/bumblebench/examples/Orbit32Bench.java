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
import sarong.Orbit32RNG;

/**
 * Orbit32Bench score: 811839488.000000 (811.8M 2051.5%)
 *          uncertainty:   0.2%
 */
public final class Orbit32Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		Orbit32RNG rng = new Orbit32RNG(0x1234, 0x5678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

