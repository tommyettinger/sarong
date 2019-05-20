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
import sarong.XoshiroAra32RNG;

/**
 * XoshiroAra32Bench score: 891977536.000000 (892.0M 2060.9%)
 *               uncertainty:   0.2%
 */
public final class XoshiroAra32Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		XoshiroAra32RNG rng = new XoshiroAra32RNG(0x12345678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

