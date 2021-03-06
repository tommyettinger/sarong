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
import sarong.JSF32RNG;

/**
 * With Java 8, HotSpot, on an 8th-gen i7 hexacore mobile processor:
 * <br>
 * JSF32Bench score: 861402304.000000 (861.4M 2057.4%)
 *        uncertainty:   0.4%
 * <br>
 * With Java 13, OpenJ9 nightly build (should be similar to OpenJ9 0.18.0), same hardware:
 * <br>
 * JSF32Bench score: 1029743680.000000 (1.030G 2075.3%)
 *        uncertainty:   0.1%
 */
public final class JSF32Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		JSF32RNG rng = new JSF32RNG(0x12345678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

