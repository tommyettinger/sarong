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
import sarong.BellRNG;

/**
 * TODO: Tested on older, slower laptop, need to test on the same machine as the other benchmarks 
 * BellBench score: 462194208.000000 (462.2M 1995.1%)
 *       uncertainty:   0.5%
 */
public final class BellBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		BellRNG rng = new BellRNG(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
}

