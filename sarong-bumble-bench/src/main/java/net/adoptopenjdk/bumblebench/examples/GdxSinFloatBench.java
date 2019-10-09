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

import com.badlogic.gdx.math.MathUtils;
import net.adoptopenjdk.bumblebench.core.MiniBench;

/**
 * GdxSinFloatBench code:
 * argument += MathUtils.sin((i + argument));
 * 
 * GdxSinFloatBench score: 61188656.000000 (61.19M 1792.9%)
 *              uncertainty:   0.9%
 * vs.
 * GdxSinFloatModBench code:
 * argument += MathUtils.sin((i + argument) % MathUtils.PI2);
 * 
 * GdxSinFloatModBench score: 61241308.000000 (61.24M 1793.0%)
 *                 uncertainty:   0.5%
 */
public final class GdxSinFloatBench extends MiniBench {
	protected int maxIterationsPerLoop(){ return 10000007; }

	protected long doBatch(long numLoops, int numIterationsPerLoop) throws InterruptedException {
		startTimer();
		MathUtils.initialize();
		float argument = MathUtils.sin(0.1f);
		pauseTimer();
		for (long i = 0; i < numLoops; i++) {
			for (int j = 0; j < numIterationsPerLoop; j++) {
				startTimer();
				argument += MathUtils.sin((i + argument));
				pauseTimer();
			}
		}
		return numLoops * numIterationsPerLoop;
	}
}

