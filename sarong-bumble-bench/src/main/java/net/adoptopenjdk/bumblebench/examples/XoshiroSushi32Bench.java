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
import sarong.XoshiroSushi32RNG;

/**
 * XoshiroSushi32Bench score: 643074432.000000 (643.1M 2028.2%)
 *                 uncertainty:   0.4%
 */
public final class XoshiroSushi32Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		XoshiroSushi32RNG rng = new XoshiroSushi32RNG(0x12345678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

