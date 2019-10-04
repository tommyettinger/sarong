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
import sarong.GWTRNG;

/**
 * OpenJDK 13, OpenJ9, less-recent Windows 7 laptop:
 * <br>
 * GWTRNGBench score: 763204928.000000 (763.2M 2045.3%)
 *         uncertainty:   0.6%
 * <br>
 * OpenJDK 12, Hotspot, less-recent Windows 7 laptop:
 * <br>
 * GWTRNGBench score: 513676992.000000 (513.7M 2005.7%)
 *         uncertainty:   0.5%
 * <br>
 * OpenJDK 8, Hotspot, less-recent Windows 7 laptop:
 * <br>
 * GWTRNGBench score: 495813824.000000 (495.8M 2002.2%)
 *         uncertainty:   0.2%
 */
public final class GWTRNGBench extends MicroBench {

	protected long doBatch(long numIterations) throws InterruptedException {
		GWTRNG rng = new GWTRNG(0x1234, 0x5678);
		int sum = 0;
		for (long i = 0; i < numIterations; i++)
			sum += rng.nextInt();
		return numIterations;
	}
}

