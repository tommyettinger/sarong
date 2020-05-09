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
import sarong.GoatRNG;

/**
 * With Java 8, HotSpot, on an 6th-gen i7 quadcore mobile processor running Windows 7:
 * <br>
 * GoatBench score: 525908224.000000 (525.9M 2008.1%)
 *       uncertainty:   0.3%
 * <br>
 * With Java 14, OpenJ9 (build 20200327_17), same hardware:
 * <br>
 * GoatBench score: 464595072.000000 (464.6M 1995.7%)
 *       uncertainty:   0.4%
 */
public final class GoatBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		GoatRNG rng = new GoatRNG(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
}

