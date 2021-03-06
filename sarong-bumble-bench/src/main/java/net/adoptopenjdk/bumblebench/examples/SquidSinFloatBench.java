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

import net.adoptopenjdk.bumblebench.core.MiniBench;
import sarong.NumberTools;

/**
 * SquidSinFloatBench score: 44307408.000000 (44.31M 1760.7%)
 *                uncertainty:   0.1%
 */
public final class SquidSinFloatBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 10000007; }
	
	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		startTimer();
		//MathUtils.initialize();
		float argument = NumberTools.sin(0.1f);
		pauseTimer();
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				argument += NumberTools.sin(argument) + 1.6180339887498949f;
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

