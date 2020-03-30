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
import sarong.XoRoRNG;

/**
 * With Java 8, HotSpot, on an 8th-gen i7 hexacore mobile processor running Manjaro Linux:
 * <br>
 * XoRoBench score: 1033620544.000000 (1.034G 2075.6%)
 *       uncertainty:   0.1%
 * <br>
 * With Java 14, OpenJ9 (build 20200327_17), same hardware:
 * <br>
 * XoRoBench score: 691192832.000000 (691.2M 2035.4%)
 *       uncertainty:   0.1%
 */
public final class XoRoBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		XoRoRNG rng = new XoRoRNG(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
}

