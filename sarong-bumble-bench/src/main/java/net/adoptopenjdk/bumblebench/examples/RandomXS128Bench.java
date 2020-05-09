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

import com.badlogic.gdx.math.RandomXS128;
import net.adoptopenjdk.bumblebench.core.MicroBench;

/**
 * With Java 8, HotSpot, on an 6th-gen i7 quadcore mobile processor running Windows 7:
 * <br>
 * RandomXS128Bench score: 508509184.000000 (508.5M 2004.7%)
 *              uncertainty:   0.6%
 * <br>
 * With Java 14, OpenJ9 (build 20200327_17), same hardware:
 * <br>
 * RandomXS128Bench score: 660909504.000000 (660.9M 2030.9%)
 *              uncertainty:   0.2%
 */
public final class RandomXS128Bench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		RandomXS128 rng = new RandomXS128(0x12345678);
		long sum = 0L;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextLong();
		return numIterations;
	}
}

