/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package sarong;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Results for all benchmarks on an i7-8750H CPU clocked at 2.20GHz :
 * <br>
 * <pre>
 * Benchmark                                  Mode  Cnt   Score   Error  Units
 * RNGBenchmark.measureAltThrustDetermine     avgt    5   2.636 ± 0.006  ns/op
 * RNGBenchmark.measureBasicRandom32          avgt    5   2.966 ± 0.004  ns/op
 * RNGBenchmark.measureBasicRandom32Int       avgt    5   2.178 ± 0.021  ns/op
 * RNGBenchmark.measureBasicRandom32IntR      avgt    5   2.383 ± 0.027  ns/op
 * RNGBenchmark.measureBasicRandom32R         avgt    5   3.212 ± 0.010  ns/op
 * RNGBenchmark.measureBasicRandom64          avgt    5   2.549 ± 0.020  ns/op
 * RNGBenchmark.measureBasicRandom64Int       avgt    5   2.660 ± 0.009  ns/op
 * RNGBenchmark.measureBasicRandom64IntR      avgt    5   2.896 ± 0.006  ns/op
 * RNGBenchmark.measureBasicRandom64R         avgt    5   2.822 ± 0.015  ns/op
 * RNGBenchmark.measureChurro32               avgt    5   6.071 ± 0.052  ns/op
 * RNGBenchmark.measureChurro32Int            avgt    5   3.663 ± 0.003  ns/op
 * RNGBenchmark.measureChurro32IntR           avgt    5   4.460 ± 0.021  ns/op
 * RNGBenchmark.measureChurro32R              avgt    5   8.272 ± 0.056  ns/op
 * RNGBenchmark.measureDervish                avgt    5   3.133 ± 0.038  ns/op
 * RNGBenchmark.measureDervishInt             avgt    5   3.248 ± 0.042  ns/op
 * RNGBenchmark.measureDervishIntR            avgt    5   3.588 ± 0.009  ns/op
 * RNGBenchmark.measureDervishR               avgt    5   3.575 ± 0.117  ns/op
 * RNGBenchmark.measureDirk                   avgt    5   3.137 ± 0.006  ns/op
 * RNGBenchmark.measureDirkDetermine          avgt    5   3.016 ± 0.011  ns/op
 * RNGBenchmark.measureDirkInt                avgt    5   3.135 ± 0.017  ns/op
 * RNGBenchmark.measureDirkIntR               avgt    5   3.576 ± 0.013  ns/op
 * RNGBenchmark.measureDirkR                  avgt    5   3.559 ± 0.022  ns/op
 * RNGBenchmark.measureDizzy32                avgt    5   5.429 ± 0.066  ns/op
 * RNGBenchmark.measureDizzy32Int             avgt    5   3.382 ± 0.011  ns/op
 * RNGBenchmark.measureDizzy32IntNative1      avgt    5   3.479 ± 0.029  ns/op
 * RNGBenchmark.measureDizzy32IntNative2      avgt    5   3.479 ± 0.009  ns/op
 * RNGBenchmark.measureDizzy32IntR            avgt    5   3.949 ± 0.037  ns/op
 * RNGBenchmark.measureDizzy32R               avgt    5   6.154 ± 0.076  ns/op
 * RNGBenchmark.measureFlap                   avgt    5   2.857 ± 0.014  ns/op
 * RNGBenchmark.measureFlapInt                avgt    5   2.380 ± 0.038  ns/op
 * RNGBenchmark.measureFlapIntR               avgt    5   2.703 ± 0.052  ns/op
 * RNGBenchmark.measureFlapR                  avgt    5   3.210 ± 0.018  ns/op
 * RNGBenchmark.measureGWT                    avgt    5   4.293 ± 0.026  ns/op
 * RNGBenchmark.measureGWTInt                 avgt    5   2.918 ± 0.040  ns/op
 * RNGBenchmark.measureGWTNextInt             avgt    5   2.914 ± 0.020  ns/op
 * RNGBenchmark.measureIsaac                  avgt    5   5.141 ± 0.012  ns/op
 * RNGBenchmark.measureIsaac32                avgt    5   8.779 ± 0.069  ns/op
 * RNGBenchmark.measureIsaac32Int             avgt    5   5.242 ± 0.021  ns/op
 * RNGBenchmark.measureIsaac32IntR            avgt    5   5.682 ± 0.018  ns/op
 * RNGBenchmark.measureIsaac32R               avgt    5   9.753 ± 0.066  ns/op
 * RNGBenchmark.measureIsaacInt               avgt    5   5.229 ± 0.037  ns/op
 * RNGBenchmark.measureIsaacIntR              avgt    5   5.590 ± 0.064  ns/op
 * RNGBenchmark.measureIsaacR                 avgt    5   5.540 ± 0.190  ns/op
 * RNGBenchmark.measureJDK                    avgt    5  17.561 ± 0.054  ns/op
 * RNGBenchmark.measureJDKInt                 avgt    5   8.767 ± 0.052  ns/op
 * RNGBenchmark.measureJab63                  avgt    5   2.397 ± 0.011  ns/op
 * RNGBenchmark.measureJab63Int               avgt    5   2.512 ± 0.013  ns/op
 * RNGBenchmark.measureJab63IntR              avgt    5   2.761 ± 0.043  ns/op
 * RNGBenchmark.measureJab63R                 avgt    5   2.691 ± 0.030  ns/op
 * RNGBenchmark.measureLap                    avgt    5   2.415 ± 0.027  ns/op
 * RNGBenchmark.measureLapInt                 avgt    5   2.530 ± 0.024  ns/op
 * RNGBenchmark.measureLapIntR                avgt    5   2.892 ± 0.065  ns/op
 * RNGBenchmark.measureLapR                   avgt    5   2.761 ± 0.013  ns/op
 * RNGBenchmark.measureLathe32                avgt    5   4.296 ± 0.005  ns/op
 * RNGBenchmark.measureLathe32Int             avgt    5   2.915 ± 0.022  ns/op
 * RNGBenchmark.measureLathe32IntR            avgt    5   3.244 ± 0.070  ns/op
 * RNGBenchmark.measureLathe32R               avgt    5   4.873 ± 0.028  ns/op
 * RNGBenchmark.measureLathe64                avgt    5   2.917 ± 0.028  ns/op
 * RNGBenchmark.measureLathe64Int             avgt    5   3.021 ± 0.036  ns/op
 * RNGBenchmark.measureLathe64IntR            avgt    5   3.432 ± 0.011  ns/op
 * RNGBenchmark.measureLathe64R               avgt    5   3.192 ± 0.008  ns/op
 * RNGBenchmark.measureLight                  avgt    5   2.826 ± 0.017  ns/op
 * RNGBenchmark.measureLight32                avgt    5   4.672 ± 0.038  ns/op
 * RNGBenchmark.measureLight32Int             avgt    5   2.881 ± 0.665  ns/op
 * RNGBenchmark.measureLight32IntR            avgt    5   3.280 ± 0.640  ns/op
 * RNGBenchmark.measureLight32R               avgt    5   5.463 ± 0.008  ns/op
 * RNGBenchmark.measureLightDetermine         avgt    5   2.805 ± 0.007  ns/op
 * RNGBenchmark.measureLightInt               avgt    5   2.830 ± 0.007  ns/op
 * RNGBenchmark.measureLightIntR              avgt    5   3.192 ± 0.032  ns/op
 * RNGBenchmark.measureLightR                 avgt    5   3.151 ± 0.027  ns/op
 * RNGBenchmark.measureLinnorm                avgt    5   2.620 ± 0.026  ns/op
 * RNGBenchmark.measureLinnormDetermine       avgt    5   2.770 ± 0.019  ns/op
 * RNGBenchmark.measureLinnormInt             avgt    5   2.665 ± 0.018  ns/op
 * RNGBenchmark.measureLinnormIntR            avgt    5   2.875 ± 0.009  ns/op
 * RNGBenchmark.measureLinnormR               avgt    5   2.989 ± 0.014  ns/op
 * RNGBenchmark.measureLobster32              avgt    5   4.654 ± 0.178  ns/op
 * RNGBenchmark.measureLobster32Int           avgt    5   3.039 ± 0.026  ns/op
 * RNGBenchmark.measureLobster32IntR          avgt    5   3.346 ± 0.029  ns/op
 * RNGBenchmark.measureLobster32R             avgt    5   5.140 ± 0.025  ns/op
 * RNGBenchmark.measureLongPeriod             avgt    5   3.530 ± 0.011  ns/op
 * RNGBenchmark.measureLongPeriodInt          avgt    5   3.607 ± 0.031  ns/op
 * RNGBenchmark.measureLongPeriodIntR         avgt    5   4.095 ± 0.018  ns/op
 * RNGBenchmark.measureLongPeriodR            avgt    5   3.955 ± 0.038  ns/op
 * RNGBenchmark.measureMesh                   avgt    5   3.298 ± 0.022  ns/op
 * RNGBenchmark.measureMeshInt                avgt    5   3.243 ± 0.008  ns/op
 * RNGBenchmark.measureMeshIntR               avgt    5   3.718 ± 0.068  ns/op
 * RNGBenchmark.measureMeshR                  avgt    5   3.701 ± 0.010  ns/op
 * RNGBenchmark.measureMiniMover64            avgt    5   2.398 ± 0.006  ns/op
 * RNGBenchmark.measureMiniMover64Int         avgt    5   2.447 ± 0.010  ns/op
 * RNGBenchmark.measureMiniMover64IntR        avgt    5   2.635 ± 0.017  ns/op
 * RNGBenchmark.measureMiniMover64R           avgt    5   2.634 ± 0.020  ns/op
 * RNGBenchmark.measureMizuchi                avgt    5   2.673 ± 0.019  ns/op
 * RNGBenchmark.measureMizuchiInt             avgt    5   2.704 ± 0.012  ns/op
 * RNGBenchmark.measureMizuchiIntR            avgt    5   2.939 ± 0.010  ns/op
 * RNGBenchmark.measureMizuchiR               avgt    5   3.008 ± 0.042  ns/op
 * RNGBenchmark.measureMolerat32              avgt    5   5.033 ± 0.040  ns/op
 * RNGBenchmark.measureMolerat32Int           avgt    5   3.050 ± 0.009  ns/op
 * RNGBenchmark.measureMolerat32IntR          avgt    5   3.480 ± 0.012  ns/op
 * RNGBenchmark.measureMolerat32R             avgt    5   5.426 ± 0.010  ns/op
 * RNGBenchmark.measureMotor                  avgt    5   3.768 ± 0.023  ns/op
 * RNGBenchmark.measureMotorInt               avgt    5   3.701 ± 0.007  ns/op
 * RNGBenchmark.measureMotorIntR              avgt    5   4.201 ± 0.012  ns/op
 * RNGBenchmark.measureMotorR                 avgt    5   4.098 ± 0.006  ns/op
 * RNGBenchmark.measureMover32                avgt    5   3.822 ± 0.015  ns/op
 * RNGBenchmark.measureMover32Int             avgt    5   2.621 ± 0.013  ns/op
 * RNGBenchmark.measureMover32IntR            avgt    5   2.775 ± 0.032  ns/op
 * RNGBenchmark.measureMover32R               avgt    5   4.209 ± 0.063  ns/op
 * RNGBenchmark.measureMover64                avgt    5   2.619 ± 0.025  ns/op
 * RNGBenchmark.measureMover64Int             avgt    5   2.636 ± 0.033  ns/op
 * RNGBenchmark.measureMover64IntR            avgt    5   2.772 ± 0.032  ns/op
 * RNGBenchmark.measureMover64R               avgt    5   2.733 ± 0.169  ns/op
 * RNGBenchmark.measureMoverCounter64         avgt    5   2.463 ± 0.006  ns/op
 * RNGBenchmark.measureMoverCounter64Int      avgt    5   2.466 ± 0.016  ns/op
 * RNGBenchmark.measureMoverCounter64IntR     avgt    5   2.691 ± 0.017  ns/op
 * RNGBenchmark.measureMoverCounter64R        avgt    5   2.687 ± 0.016  ns/op
 * RNGBenchmark.measureOrbit                  avgt    5   2.916 ± 0.012  ns/op
 * RNGBenchmark.measureOrbitA                 avgt    5   2.914 ± 0.005  ns/op
 * RNGBenchmark.measureOrbitB                 avgt    5   3.027 ± 0.010  ns/op
 * RNGBenchmark.measureOrbitC                 avgt    5   3.003 ± 0.021  ns/op
 * RNGBenchmark.measureOrbitD                 avgt    5   2.914 ± 0.031  ns/op
 * RNGBenchmark.measureOrbitE                 avgt    5   3.260 ± 0.027  ns/op
 * RNGBenchmark.measureOrbitF                 avgt    5   2.905 ± 0.026  ns/op
 * RNGBenchmark.measureOrbitG                 avgt    5   3.027 ± 0.013  ns/op
 * RNGBenchmark.measureOrbitH                 avgt    5   2.905 ± 0.026  ns/op
 * RNGBenchmark.measureOrbitI                 avgt    5   3.017 ± 0.012  ns/op
 * RNGBenchmark.measureOrbitInt               avgt    5   3.018 ± 0.017  ns/op
 * RNGBenchmark.measureOrbitIntR              avgt    5   3.357 ± 0.009  ns/op
 * RNGBenchmark.measureOrbitJ                 avgt    5   2.781 ± 0.009  ns/op
 * RNGBenchmark.measureOrbitK                 avgt    5   2.895 ± 0.011  ns/op
 * RNGBenchmark.measureOrbitL                 avgt    5   2.753 ± 0.012  ns/op
 * RNGBenchmark.measureOrbitM                 avgt    5   3.141 ± 0.011  ns/op
 * RNGBenchmark.measureOrbitN                 avgt    5   3.147 ± 0.022  ns/op
 * RNGBenchmark.measureOrbitO                 avgt    5   3.008 ± 0.031  ns/op
 * RNGBenchmark.measureOrbitR                 avgt    5   3.297 ± 0.019  ns/op
 * RNGBenchmark.measureOriole32               avgt    5   4.691 ± 0.005  ns/op
 * RNGBenchmark.measureOriole32Int            avgt    5   3.134 ± 0.040  ns/op
 * RNGBenchmark.measureOriole32IntR           avgt    5   3.522 ± 0.017  ns/op
 * RNGBenchmark.measureOriole32R              avgt    5   5.153 ± 0.070  ns/op
 * RNGBenchmark.measureOtter32                avgt    5   4.958 ± 0.009  ns/op
 * RNGBenchmark.measureOtter32Int             avgt    5   3.121 ± 0.031  ns/op
 * RNGBenchmark.measureOtter32IntR            avgt    5   3.509 ± 0.020  ns/op
 * RNGBenchmark.measureOtter32R               avgt    5   5.633 ± 0.023  ns/op
 * RNGBenchmark.measureOverdrive64            avgt    5   2.493 ± 0.022  ns/op
 * RNGBenchmark.measureOverdrive64Int         avgt    5   2.558 ± 0.084  ns/op
 * RNGBenchmark.measureOverdrive64IntR        avgt    5   2.735 ± 0.022  ns/op
 * RNGBenchmark.measureOverdrive64R           avgt    5   2.716 ± 0.025  ns/op
 * RNGBenchmark.measurePaperweight            avgt    5   3.370 ± 0.029  ns/op
 * RNGBenchmark.measurePaperweightInt         avgt    5   3.400 ± 0.019  ns/op
 * RNGBenchmark.measurePaperweightIntR        avgt    5   3.879 ± 0.019  ns/op
 * RNGBenchmark.measurePaperweightR           avgt    5   3.796 ± 0.026  ns/op
 * RNGBenchmark.measureQuixotic               avgt    5   2.608 ± 0.020  ns/op
 * RNGBenchmark.measureQuixoticInt            avgt    5   2.660 ± 0.012  ns/op
 * RNGBenchmark.measureQuixoticIntR           avgt    5   2.923 ± 0.012  ns/op
 * RNGBenchmark.measureQuixoticR              avgt    5   2.892 ± 0.023  ns/op
 * RNGBenchmark.measureSFC64                  avgt    5   3.214 ± 0.011  ns/op
 * RNGBenchmark.measureSFC64Int               avgt    5   3.307 ± 0.025  ns/op
 * RNGBenchmark.measureSFC64IntR              avgt    5   3.725 ± 0.023  ns/op
 * RNGBenchmark.measureSFC64R                 avgt    5   3.909 ± 0.156  ns/op
 * RNGBenchmark.measureSeaSlater32            avgt    5   4.992 ± 0.110  ns/op
 * RNGBenchmark.measureSeaSlater32Int         avgt    5   3.063 ± 0.011  ns/op
 * RNGBenchmark.measureSeaSlater32IntR        avgt    5   3.430 ± 0.026  ns/op
 * RNGBenchmark.measureSeaSlater32R           avgt    5   5.585 ± 0.100  ns/op
 * RNGBenchmark.measureSeaSlater64            avgt    5   3.074 ± 0.039  ns/op
 * RNGBenchmark.measureSeaSlater64Int         avgt    5   3.161 ± 0.009  ns/op
 * RNGBenchmark.measureSeaSlater64IntR        avgt    5   3.544 ± 0.058  ns/op
 * RNGBenchmark.measureSeaSlater64R           avgt    5   3.457 ± 0.075  ns/op
 * RNGBenchmark.measureSpiral                 avgt    5   3.471 ± 0.031  ns/op
 * RNGBenchmark.measureSpiralA                avgt    5   3.475 ± 0.025  ns/op
 * RNGBenchmark.measureSpiralB                avgt    5   3.159 ± 0.008  ns/op
 * RNGBenchmark.measureSpiralC                avgt    5   3.290 ± 0.011  ns/op
 * RNGBenchmark.measureSpiralD                avgt    5   3.203 ± 0.073  ns/op
 * RNGBenchmark.measureSpiralE                avgt    5   3.223 ± 0.010  ns/op
 * RNGBenchmark.measureSpiralF                avgt    5   3.001 ± 0.029  ns/op
 * RNGBenchmark.measureSpiralG                avgt    5   3.082 ± 0.062  ns/op
 * RNGBenchmark.measureSpiralH                avgt    5   3.169 ± 0.031  ns/op
 * RNGBenchmark.measureSpiralI                avgt    5   2.669 ± 0.034  ns/op
 * RNGBenchmark.measureSpiralInt              avgt    5   3.513 ± 0.050  ns/op
 * RNGBenchmark.measureSpiralIntR             avgt    5   4.234 ± 0.010  ns/op
 * RNGBenchmark.measureSpiralR                avgt    5   3.991 ± 0.037  ns/op
 * RNGBenchmark.measureStarfish32             avgt    5   4.449 ± 0.056  ns/op
 * RNGBenchmark.measureStarfish32Int          avgt    5   3.016 ± 0.017  ns/op
 * RNGBenchmark.measureStarfish32IntR         avgt    5   3.208 ± 0.014  ns/op
 * RNGBenchmark.measureStarfish32NextInt      avgt    5   2.997 ± 0.052  ns/op
 * RNGBenchmark.measureStarfish32R            avgt    5   5.013 ± 0.157  ns/op
 * RNGBenchmark.measureTangle                 avgt    5   2.572 ± 0.029  ns/op
 * RNGBenchmark.measureTangleA                avgt    5   2.582 ± 0.008  ns/op
 * RNGBenchmark.measureTangleB                avgt    5   2.734 ± 0.004  ns/op
 * RNGBenchmark.measureTangleC                avgt    5   2.762 ± 0.018  ns/op
 * RNGBenchmark.measureTangleD                avgt    5   2.838 ± 0.015  ns/op
 * RNGBenchmark.measureTangleInt              avgt    5   2.651 ± 0.008  ns/op
 * RNGBenchmark.measureTangleIntR             avgt    5   2.978 ± 0.039  ns/op
 * RNGBenchmark.measureTangleR                avgt    5   2.963 ± 0.009  ns/op
 * RNGBenchmark.measureThrust                 avgt    5   2.508 ± 0.024  ns/op
 * RNGBenchmark.measureThrustAlt              avgt    5   2.516 ± 0.012  ns/op
 * RNGBenchmark.measureThrustAlt32            avgt    5   4.363 ± 0.009  ns/op
 * RNGBenchmark.measureThrustAlt32Int         avgt    5   2.792 ± 0.009  ns/op
 * RNGBenchmark.measureThrustAlt32IntR        avgt    5   3.151 ± 0.020  ns/op
 * RNGBenchmark.measureThrustAlt32R           avgt    5   5.111 ± 0.150  ns/op
 * RNGBenchmark.measureThrustAltInt           avgt    5   2.522 ± 0.006  ns/op
 * RNGBenchmark.measureThrustAltIntR          avgt    5   2.811 ± 0.009  ns/op
 * RNGBenchmark.measureThrustAltR             avgt    5   2.823 ± 0.066  ns/op
 * RNGBenchmark.measureThrustInt              avgt    5   2.511 ± 0.010  ns/op
 * RNGBenchmark.measureThrustIntR             avgt    5   2.790 ± 0.038  ns/op
 * RNGBenchmark.measureThrustR                avgt    5   2.791 ± 0.011  ns/op
 * RNGBenchmark.measureThunder                avgt    5   2.653 ± 0.035  ns/op
 * RNGBenchmark.measureThunderInt             avgt    5   2.761 ± 0.022  ns/op
 * RNGBenchmark.measureThunderIntR            avgt    5   3.023 ± 0.015  ns/op
 * RNGBenchmark.measureThunderR               avgt    5   2.984 ± 0.015  ns/op
 * RNGBenchmark.measureVortex                 avgt    5   2.928 ± 0.003  ns/op
 * RNGBenchmark.measureVortexInt              avgt    5   3.026 ± 0.028  ns/op
 * RNGBenchmark.measureVortexIntR             avgt    5   3.401 ± 0.027  ns/op
 * RNGBenchmark.measureVortexR                avgt    5   3.342 ± 0.104  ns/op
 * RNGBenchmark.measureXoRo                   avgt    5   2.763 ± 0.011  ns/op
 * RNGBenchmark.measureXoRo32                 avgt    5   3.785 ± 0.007  ns/op
 * RNGBenchmark.measureXoRo32Int              avgt    5   2.770 ± 0.030  ns/op
 * RNGBenchmark.measureXoRo32IntR             avgt    5   3.114 ± 0.050  ns/op
 * RNGBenchmark.measureXoRo32R                avgt    5   4.409 ± 0.012  ns/op
 * RNGBenchmark.measureXoRoInt                avgt    5   2.881 ± 0.025  ns/op
 * RNGBenchmark.measureXoRoIntR               avgt    5   3.129 ± 0.026  ns/op
 * RNGBenchmark.measureXoRoR                  avgt    5   2.991 ± 0.007  ns/op
 * RNGBenchmark.measureXoshiroAra32           avgt    5   4.929 ± 0.190  ns/op
 * RNGBenchmark.measureXoshiroAra32Int        avgt    5   3.257 ± 0.024  ns/op
 * RNGBenchmark.measureXoshiroAra32IntR       avgt    5   3.675 ± 0.024  ns/op
 * RNGBenchmark.measureXoshiroAra32R          avgt    5   5.349 ± 0.062  ns/op
 * RNGBenchmark.measureXoshiroStarPhi32       avgt    5   5.117 ± 0.021  ns/op
 * RNGBenchmark.measureXoshiroStarPhi32Int    avgt    5   3.381 ± 0.009  ns/op
 * RNGBenchmark.measureXoshiroStarPhi32IntR   avgt    5   3.767 ± 0.012  ns/op
 * RNGBenchmark.measureXoshiroStarPhi32R      avgt    5   5.477 ± 0.022  ns/op
 * RNGBenchmark.measureXoshiroStarStar32      avgt    5   5.257 ± 0.070  ns/op
 * RNGBenchmark.measureXoshiroStarStar32Int   avgt    5   3.466 ± 0.046  ns/op
 * RNGBenchmark.measureXoshiroStarStar32IntR  avgt    5   3.836 ± 0.096  ns/op
 * RNGBenchmark.measureXoshiroStarStar32R     avgt    5   5.747 ± 0.016  ns/op
 * RNGBenchmark.measureXoshiroXara32          avgt    5   5.080 ± 0.014  ns/op
 * RNGBenchmark.measureXoshiroXara32Int       avgt    5   3.319 ± 0.011  ns/op
 * RNGBenchmark.measureXoshiroXara32IntR      avgt    5   3.748 ± 0.064  ns/op
 * RNGBenchmark.measureXoshiroXara32R         avgt    5   5.512 ± 0.149  ns/op
 * RNGBenchmark.measureZag32                  avgt    5   6.304 ± 0.107  ns/op
 * RNGBenchmark.measureZag32Int               avgt    5   3.366 ± 0.011  ns/op
 * RNGBenchmark.measureZag32IntR              avgt    5   3.875 ± 0.107  ns/op
 * RNGBenchmark.measureZag32R                 avgt    5   6.411 ± 0.103  ns/op
 * RNGBenchmark.measureZig32                  avgt    5   5.908 ± 0.084  ns/op
 * RNGBenchmark.measureZig32Int               avgt    5   3.498 ± 0.043  ns/op
 * RNGBenchmark.measureZig32IntR              avgt    5   4.031 ± 0.063  ns/op
 * RNGBenchmark.measureZig32R                 avgt    5   6.505 ± 0.056  ns/op
 * RNGBenchmark.measureZog32                  avgt    5   5.206 ± 0.076  ns/op
 * RNGBenchmark.measureZog32Int               avgt    5   3.216 ± 0.018  ns/op
 * RNGBenchmark.measureZog32IntR              avgt    5   3.693 ± 0.035  ns/op
 * RNGBenchmark.measureZog32R                 avgt    5   5.770 ± 0.020  ns/op
 * </pre>
 * <br>
 * The fastest generator depends on your target platform, but on a desktop or laptop using an OpenJDK-based Java
 * installation, Jab63RNG and MiniMover64RNG are virtually tied for first place. Neither is at all equidistributed; for
 * generators that are capable of producing all outputs with equal likelihood, LinnormRNG, MizuchiRNG, and QuixoticRNG
 * are all about the same, with DiverRNG probably a little faster (but it was added after this benchmark was run, so its
 * results wouldn't be from the same circumstances). DiverRNG and possibly QuixoticRNG are likely to have higher quality
 * than LinnormRNG and MizuchiRNG, since the last two fail one PractRand test after 16TB while at least DiverRNG does
 * not have that issue.
 * <br>
 * GWT-compatible generators need to work with an "int" type that isn't equivalent to Java's "int" and is closer to a
 * Java "double" that gets cast to an int when bitwise operations are used on it. This JS int is about 10x-20x faster to
 * do math operations on than GWT's "long" type, which is emulated using three JS numbers internally, but you need to be
 * vigilant about the possibility of exceeding the limits of Integer.MAX_VALUE and Integer.MIN_VALUE, since math won't
 * overflow, and about precision loss if you do exceed those limits severely, since JS numbers are floating-point. So,
 * you can't safely multiply by too large of an int (I limit my multipliers to 20 bits), you need to follow up normal
 * math with bitwise math to bring any overflowing numbers back to the 32-bit range, and you should avoid longs and math
 * on them whenever possible. The GWT-safe generators are in the bulk results above; the ones that are GWT-safe are (an
 * asterisk marks a generator that doesn't pass many statistical tests): Churro32RNG, Dizzy32RNG, Lathe32RNG,
 * Lobster32RNG, Molerat32RNG, Mover32RNG, Oriole32RNG, SeaSlater32RNG*, Starfish32RNG, XoRo32RNG*, XoshiroAra32RNG,
 * XoshiroStarPhi32RNG, XoshiroStarStar32RNG, XoshiroXara32RNG, Zag32RNG, Zig32RNG, and Zog32RNG. GWTRNG is a special
 * case because it uses Starfish32RNG's algorithm verbatim, but implements IRNG instead of just RandomnessSource and so
 * has some extra optimizations it can use when producing 32-bit values. Starfish32RNG and all of the Xoshiro-based
 * generators are probably the best choices for 32-bit generators, with Starfish having a smaller and possibly more
 * manageable state size, and Xoshiro variants having much longer periods.
 * <br>
 * You can benchmark most of these in GWT for yourself on
 * <a href="https://tommyettinger.github.io/SquidLib-Demos/bench/rng/">this SquidLib-Demos page</a>; comparing "runs"
 * where higher is better is a good way of estimating how fast a generator is. Each "run" is 10,000 generated numbers.
 * Lathe32RNG is by far the best on speed if you consider both desktop and GWT, but it can't generate all possible
 * "long" values, while XoshiroAra can generate all possible "long" values with equal frequency, and even all possible
 * pairs of "long" values (less one). XoshiroAra is also surprisingly fast compared to similar Xoshiro-based generators,
 * especially since it does just as well in PractRand testing. The equidistribution comment at the end of each line
 * refers to that specific method; calling {@code myOriole32RNG.next(32)} repeatedly will produce all ints with equal
 * frequency over the full period of that generator, ((2 to the 64) - 1) * (2 to the 32), but the same is not true of
 * calling {@code myOriole32RNG.nextLong()}, which would produce some longs more frequently than others (and probably
 * would not produce some longs at all). The Xoshiro-based generators have the best period and equidistribution
 * qualities, with XoshiroAra having the best performance (all of them pass 32TB of PractRand). The Xoroshiro-based
 * generators tend to be faster, but only Starfish has better equidistribution of the bunch (it can produce all longs
 * except one, while Lathe can't and Oriole won't with equal frequency). Starfish seems to have comparable quality and
 * speed relative to Oriole (excellent and pretty good, respectively), but improves on its distribution at the expense
 * of its period, and has a smaller state.
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
public class RNGBenchmark {

    private long state = 9000, stream = 9001, oddState = 9999L;

//    public long doThunder()
//    {
//        ThunderRNG rng = new ThunderRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunder() throws InterruptedException {
//        seed = 9000;
//        doThunder();
//    }
//
//    public int doThunderInt()
//    {
//        ThunderRNG rng = new ThunderRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderInt() throws InterruptedException {
//        iseed = 9000;
//        doThunderInt();
//    }
//    public long doThunderR()
//    {
//        RNG rng = new RNG(new ThunderRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderR() throws InterruptedException {
//        seed = 9000;
//        doThunderR();
//    }
//
//    public int doThunderIntR()
//    {
//        RNG rng = new RNG(new ThunderRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void measureThunderIntR() throws InterruptedException {
//        iseed = 9000;
//        doThunderIntR();
//    }

    private XoRoRNG XoRo = new XoRoRNG(9999L);
    private RNG XoRoR = new RNG(XoRo);
    @Benchmark
    public long measureXoRo()
    {
        return XoRo.nextLong();
    }

    @Benchmark
    public int measureXoRoInt()
    {
        return XoRo.next(32);
    }
    @Benchmark
    public long measureXoRoR()
    {
        return XoRoR.nextLong();
    }

    @Benchmark
    public int measureXoRoIntR()
    {
        return XoRoR.nextInt();
    }


    private Lathe64RNG Lathe64 = new Lathe64RNG(9999L);
    private RNG Lathe64R = new RNG(Lathe64);
    @Benchmark
    public long measureLathe64()
    {
        return Lathe64.nextLong();
    }

    @Benchmark
    public int measureLathe64Int()
    {
        return Lathe64.next(32);
    }
    @Benchmark
    public long measureLathe64R()
    {
        return Lathe64R.nextLong();
    }

    @Benchmark
    public int measureLathe64IntR()
    {
        return Lathe64R.nextInt();
    }

    /*
    public long doXar()
    {
        XarRNG rng = new XarRNG(seed);
        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXar() throws InterruptedException {
        seed = 9000;
        doXar();
    }

    public int doXarInt()
    {
        XarRNG rng = new XarRNG(iseed);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarInt() throws InterruptedException {
        iseed = 9000;
        doXarInt();
    }

    public long doXarR()
    {
        RNG rng = new RNG(new XarRNG(seed));
        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarR() throws InterruptedException {
        seed = 9000;
        doXarR();
    }

    public int doXarIntR()
    {
        RNG rng = new RNG(new XarRNG(iseed));
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a____measureXarIntR() throws InterruptedException {
        iseed = 9000;
        doXarIntR();
    }
    */

    private LongPeriodRNG LongPeriod = new LongPeriodRNG(9999L);
    private RNG LongPeriodR = new RNG(LongPeriod);
    @Benchmark
    public long measureLongPeriod()
    {
        return LongPeriod.nextLong();
    }

    @Benchmark
    public int measureLongPeriodInt()
    {
        return LongPeriod.next(32);
    }
    @Benchmark
    public long measureLongPeriodR()
    {
        return LongPeriodR.nextLong();
    }

    @Benchmark
    public int measureLongPeriodIntR()
    {
        return LongPeriodR.nextInt();
    }


    private LightRNG Light = new LightRNG(9999L);
    private RNG LightR = new RNG(Light);
    @Benchmark
    public long measureLight()
    {
        return Light.nextLong();
    }

    @Benchmark
    public int measureLightInt()
    {
        return Light.next(32);
    }
    @Benchmark
    public long measureLightR()
    {
        return LightR.nextLong();
    }

    @Benchmark
    public int measureLightIntR()
    {
        return LightR.nextInt();
    }
    
    private PaperweightRNG Paperweight = new PaperweightRNG(9999L);
    private RNG PaperweightR = new RNG(Paperweight);
    @Benchmark
    public long measurePaperweight()
    {
        return Paperweight.nextLong();
    }

    @Benchmark
    public int measurePaperweightInt()
    {
        return Paperweight.next(32);
    }
    @Benchmark
    public long measurePaperweightR()
    {
        return PaperweightR.nextLong();
    }

    @Benchmark
    public int measurePaperweightIntR()
    {
        return PaperweightR.nextInt();
    }
    
    private FlapRNG Flap = new FlapRNG(9999L);
    private RNG FlapR = new RNG(Flap);
    @Benchmark
    public long measureFlap()
    {
        return Flap.nextLong();
    }

    @Benchmark
    public int measureFlapInt()
    {
        return Flap.next(32);
    }
    @Benchmark
    public long measureFlapR()
    {
        return FlapR.nextLong();
    }

    @Benchmark
    public int measureFlapIntR()
    {
        return FlapR.nextInt();
    }

    private LapRNG Lap = new LapRNG(9999L);
    private RNG LapR = new RNG(Lap);
    @Benchmark
    public long measureLap()
    {
        return Lap.nextLong();
    }

    @Benchmark
    public int measureLapInt()
    {
        return Lap.next(32);
    }
    @Benchmark
    public long measureLapR()
    {
        return LapR.nextLong();
    }
    private ThunderRNG Thunder = new ThunderRNG(9999L);
    private RNG ThunderR = new RNG(Thunder);
    @Benchmark
    public long measureThunder()
    {
        return Thunder.nextLong();
    }

    @Benchmark
    public int measureThunderInt()
    {
        return Thunder.next(32);
    }
    @Benchmark
    public long measureThunderR()
    {
        return ThunderR.nextLong();
    }

    @Benchmark
    public int measureThunderIntR()
    {
        return ThunderR.nextInt();
    }


    @Benchmark
    // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public int measureLapIntR()
    {
        return LapR.nextInt();
    }

    private SeaSlater64RNG SeaSlater64 = new SeaSlater64RNG(9999L);
    private RNG SeaSlater64R = new RNG(SeaSlater64);
    @Benchmark
    public long measureSeaSlater64()
    {
        return SeaSlater64.nextLong();
    }

    @Benchmark
    public int measureSeaSlater64Int()
    {
        return SeaSlater64.next(32);
    }
    @Benchmark
    public long measureSeaSlater64R()
    {
        return SeaSlater64R.nextLong();
    }

    @Benchmark
    public int measureSeaSlater64IntR()
    {
        return SeaSlater64R.nextInt();
    }


    private ThrustRNG Thrust = new ThrustRNG(9999L);
    private RNG ThrustR = new RNG(Thrust);
    @Benchmark
    public long measureThrust()
    {
        return Thrust.nextLong();
    }

    @Benchmark
    public int measureThrustInt()
    {
        return Thrust.next(32);
    }
    @Benchmark
    public long measureThrustR()
    {
        return ThrustR.nextLong();
    }

    @Benchmark
    public int measureThrustIntR()
    {
        return ThrustR.nextInt();
    }

    //@Benchmark
    public long measureInlineThrust()
    {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ z >>> 26) * 0x2545F4914F6CDD1DL;
        return z ^ z >>> 28;
    }

    /*

    public long doThrust3()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong3();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust3() throws InterruptedException {
        seed = 9000;
        doThrust3();
    }

    public int doThrust3Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next3(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust3Int() throws InterruptedException {
        iseed = 9000;
        doThrust3Int();
    }

    public long doThrust2()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong2();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust2() throws InterruptedException {
        seed = 9000;
        doThrust2();
    }

    public int doThrust2Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next2(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureThrust2Int() throws InterruptedException {
        iseed = 9000;
        doThrust2Int();
    }
    public long doThrust4()
    {
        ThrustAltRNG rng = new ThrustAltRNG(seed|1L);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong4();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4() throws InterruptedException {
        seed = 9000;
        doThrust4();
    }

    public int doThrust4Int()
    {
        ThrustAltRNG rng = new ThrustAltRNG(iseed|1L);
        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next4(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void a______measureAltThrust4Int() throws InterruptedException {
        iseed = 9000;
        doThrust4Int();
    }
*/


    @Benchmark
    public long measureAltThrustDetermine() {
        return ThrustAltRNG.determine(state++);
    }
//
//
//    @Benchmark
//    public long measureDervishDetermine() {
//        return DervishRNG.determine(state++);
//    }
//

    @Benchmark
    public long measureLightDetermine() {
        return LightRNG.determine(state++);
    }
    @Benchmark
    public long measureLinnormDetermine() {
        return LinnormRNG.determine(state++);
    }

    @Benchmark
    public long measureDirkDetermine() {
        return DirkRNG.determine(state++);
    }
//    @Benchmark
//    public long measureMotorDetermine() {
//        return MotorRNG.determine(state++);
//    }

//
//    //@Benchmark
//    public long measureVortexDetermine() {
//        return VortexRNG.determine(state++);
//    }
//
//    //@Benchmark
//    public long measureVortexDetermineBare() {
//        return VortexRNG.determineBare(state += 0x6C8E9CF570932BD5L);
//    }
//
//    @Benchmark
//    public long measureAltThrustRandomize() {
//        return ThrustAltRNG.randomize(state += 0x6C8E9CF570932BD5L);
//    }
//    
//    @Benchmark
//    public long measureLinnormRandomize() { return LinnormRNG.randomize(state += 0x632BE59BD9B4E019L); }

    private ThrustAltRNG ThrustAlt = new ThrustAltRNG(9999L);
    private RNG ThrustAltR = new RNG(ThrustAlt);
    @Benchmark
    public long measureThrustAlt()
    {
        return ThrustAlt.nextLong();
    }

    @Benchmark
    public int measureThrustAltInt()
    {
        return ThrustAlt.next(32);
    }
    @Benchmark
    public long measureThrustAltR()
    {
        return ThrustAltR.nextLong();
    }

    @Benchmark
    public int measureThrustAltIntR()
    {
        return ThrustAltR.nextInt();
    }
//    @Benchmark
//    public long measureThrustAltInline()
//    {
//        final long s = (state += 0x6C8E9CF570932BD5L);
//        final long z = (s ^ (s >>> 25)) * (s | 0xA529L);
//        return z ^ (z >>> 22);
//    }
//    @Benchmark
//    public long measureInlineThrustAltOther()
//    {
//        long z = (state += 0x6C8E9CF570932BD5L);
//        z = (z ^ (z >>> 25)) * (z | 0xA529L);
//        return z ^ (z >>> 22);
//    }

    private Jab63RNG Jab63 = new Jab63RNG(9999L);
    private RNG Jab63R = new RNG(Jab63);
    @Benchmark
    public long measureJab63()
    {
        return Jab63.nextLong();
    }

    @Benchmark
    public int measureJab63Int()
    {
        return Jab63.next(32);
    }
    @Benchmark
    public long measureJab63R()
    {
        return Jab63R.nextLong();
    }

    @Benchmark
    public int measureJab63IntR()
    {
        return Jab63R.nextInt();
    }

//    @Benchmark
//    public long measureInlineJab63()
//    {
//        long z = (oddState += 0x3C6EF372FE94F82AL);
//        z *= (z ^ (z >>> 21));
//        return z - (z >>> 28);
//    }
//
//
//    @Benchmark
//    public long measureInlineVortex()
//    {
//        long z = (state += 0x6C8E9CF970932BD5L);
//        z = (z ^ z >>> 25) * 0x2545F4914F6CDD1DL;
//        z ^= ((z << 19) | (z >>> 45)) ^ ((z << 53) | (z >>> 11));
//        return z ^ (z >>> 25);
//    }

    private VortexRNG Vortex = new VortexRNG(9999L);
    private RNG VortexR = new RNG(Vortex);
    @Benchmark
    public long measureVortex()
    {
        return Vortex.nextLong();
    }

    @Benchmark
    public int measureVortexInt()
    {
        return Vortex.next(32);
    }
    @Benchmark
    public long measureVortexR()
    {
        return VortexR.nextLong();
    }

    @Benchmark
    public int measureVortexIntR()
    {
        return VortexR.nextInt();
    }

    private BasicRandom64 BasicRandom64 = new BasicRandom64(1L);
    private RNG BasicRandom64R = new RNG(BasicRandom64);
    @Benchmark
    public long measureBasicRandom64()
    {
        return BasicRandom64.nextLong();
    }

    @Benchmark
    public int measureBasicRandom64Int()
    {
        return BasicRandom64.next(32);
    }
    @Benchmark
    public long measureBasicRandom64R()
    {
        return BasicRandom64R.nextLong();
    }

    @Benchmark
    public int measureBasicRandom64IntR()
    {
        return BasicRandom64R.nextInt();
    }

    private BasicRandom32 BasicRandom32 = new BasicRandom32(1);
    private RNG BasicRandom32R = new RNG(BasicRandom32);
    @Benchmark
    public long measureBasicRandom32()
    {
        return BasicRandom32.nextLong();
    }

    @Benchmark
    public int measureBasicRandom32Int()
    {
        return BasicRandom32.next(32);
    }
    @Benchmark
    public long measureBasicRandom32R()
    {
        return BasicRandom32R.nextLong();
    }

    @Benchmark
    public int measureBasicRandom32IntR()
    {
        return BasicRandom32R.nextInt();
    }

    private MotorRNG Motor = new MotorRNG(9999L);
    private RNG MotorR = new RNG(Motor);
    @Benchmark
    public long measureMotor()
    {
        return Motor.nextLong();
    }

    @Benchmark
    public int measureMotorInt()
    {
        return Motor.next(32);
    }
    @Benchmark
    public long measureMotorR()
    {
        return MotorR.nextLong();
    }

    @Benchmark
    public int measureMotorIntR()
    {
        return MotorR.nextInt();
    }


    private MeshRNG Mesh = new MeshRNG(9999L);
    private RNG MeshR = new RNG(Mesh);
    @Benchmark
    public long measureMesh()
    {
        return Mesh.nextLong();
    }

    @Benchmark
    public int measureMeshInt()
    {
        return Mesh.next(32);
    }
    @Benchmark
    public long measureMeshR()
    {
        return MeshR.nextLong();
    }

    @Benchmark
    public int measureMeshIntR()
    {
        return MeshR.nextInt();
    }


    private SpiralRNG Spiral = new SpiralRNG(9999L);
    private RNG SpiralR = new RNG(Spiral);
    @Benchmark
    public long measureSpiral()
    {
        return Spiral.nextLong();
    }

    @Benchmark
    public int measureSpiralInt()
    {
        return Spiral.next(32);
    }
    @Benchmark
    public long measureSpiralR()
    {
        return SpiralR.nextLong();
    }

    @Benchmark
    public int measureSpiralIntR()
    {
        return SpiralR.nextInt();
    }

    private SpiralRNG spiralA = new SpiralRNG(9999L, 1337L),
            spiralB = new SpiralRNG(9999L, 1337L),
            spiralC = new SpiralRNG(9999L, 1337L),
            spiralD = new SpiralRNG(9999L, 1337L),
            spiralE = new SpiralRNG(9999L, 1337L),
            spiralF = new SpiralRNG(9999L, 1337L),
            spiralG = new SpiralRNG(9999L, 1337L),
            spiralH = new SpiralRNG(9999L, 1337L),
            spiralI = new SpiralRNG(9999L, 1337L);
    @Benchmark
    public long measureSpiralA()
    {
        return spiralA.nextLongOld();
    }
    @Benchmark
    public long measureSpiralB()
    {
        return spiralB.nextLongAlt();
    }
    @Benchmark
    public long measureSpiralC()
    {
        return spiralC.nextLongNew();
    }
    @Benchmark
    public long measureSpiralD()
    {
        return spiralD.nextLongAgain();
    }
    @Benchmark
    public long measureSpiralE()
    {
        return spiralE.nextLongAgain3();
    }
    @Benchmark
    public long measureSpiralF()
    {
        return spiralF.nextLongAgain4();
    }
    @Benchmark
    public long measureSpiralG()
    {
        return spiralG.nextLongAgain5();
    }
    @Benchmark
    public long measureSpiralH()
    {
        return spiralH.nextLongAgain6();
    }
    @Benchmark
    public long measureSpiralI()
    {
        return spiralI.nextLongAgain7();
    }

    private OrbitRNG Orbit = new OrbitRNG(9999L, 1337L);
    private RNG OrbitR = new RNG(Orbit);

    @Benchmark
    public long measureOrbit()
    {
        return Orbit.nextLong();
    }

    @Benchmark
    public int measureOrbitInt()
    {
        return Orbit.next(32);
    }
    @Benchmark
    public long measureOrbitR()
    {
        return OrbitR.nextLong();
    }

    @Benchmark
    public int measureOrbitIntR()
    {
        return OrbitR.nextInt();
    }

    private TangleRNG Tangle = new TangleRNG(9999L, 1337L);
    private RNG TangleR = new RNG(Tangle);

    @Benchmark
    public long measureTangle()
    {
        return Tangle.nextLong();
    }

    @Benchmark
    public int measureTangleInt()
    {
        return Tangle.next(32);
    }
    @Benchmark
    public long measureTangleR()
    {
        return TangleR.nextLong();
    }

    @Benchmark
    public int measureTangleIntR()
    {
        return TangleR.nextInt();
    }

    private OrbitRNG OrbitA = new OrbitRNG(9999L, 1337L),
            OrbitB = new OrbitRNG(9999L, 1337L),
            OrbitC = new OrbitRNG(9999L, 1337L),
            OrbitD = new OrbitRNG(9999L, 1337L),
            OrbitE = new OrbitRNG(9999L, 1337L),
            OrbitF = new OrbitRNG(9999L, 1337L),
            OrbitG = new OrbitRNG(9999L, 1337L),
            OrbitH = new OrbitRNG(9999L, 1337L),
            OrbitI = new OrbitRNG(9999L, 1337L),
            OrbitJ = new OrbitRNG(9999L, 1337L),
            OrbitK = new OrbitRNG(9999L, 1337L),
            OrbitL = new OrbitRNG(9999L, 1337L),
            OrbitM = new OrbitRNG(9999L, 1337L),
            OrbitN = new OrbitRNG(9999L, 1337L),
            OrbitO = new OrbitRNG(9999L, 1337L);
    @Benchmark
    public long measureOrbitA()
    {
        return OrbitA.nextLong1();
    }
    @Benchmark
    public long measureOrbitB()
    {
        return OrbitB.nextLong2();
    }
    @Benchmark
    public long measureOrbitC()
    {
        return OrbitC.nextLong3();
    }
    @Benchmark
    public long measureOrbitD()
    {
        return OrbitD.nextLong4();
    }
    @Benchmark
    public long measureOrbitE()
    {
        return OrbitE.nextLong5();
    }
    @Benchmark
    public long measureOrbitF()
    {
        return OrbitF.nextLong6();
    }
    @Benchmark
    public long measureOrbitG()
    {
        return OrbitG.nextLong7();
    }
    @Benchmark
    public long measureOrbitH()
    {
        return OrbitH.nextLong8();
    }
    @Benchmark
    public long measureOrbitI()
    {
        return OrbitI.nextLong9();
    }
    @Benchmark
    public long measureOrbitJ()
    {
        return OrbitJ.nextLong10();
    }
    @Benchmark
    public long measureOrbitK()
    {
        return OrbitK.nextLong11();
    }
    @Benchmark
    public long measureOrbitL()
    {
        return OrbitL.nextLong12();
    }
    @Benchmark
    public long measureOrbitM()
    {
        return OrbitM.nextLong13();
    }
    @Benchmark
    public long measureOrbitN()
    {
        return OrbitN.nextLong14();
    }
    @Benchmark
    public long measureOrbitO()
    {
        return OrbitO.nextLong15();
    }


    private TangleRNG TangleA = new TangleRNG(9999L, 1337L),
            TangleB = new TangleRNG(9999L, 1337L),
            TangleC = new TangleRNG(9999L, 1337L),
            TangleD = new TangleRNG(9999L, 1337L);
    @Benchmark
    public long measureTangleA()
    {
        return TangleA.nextLong1();
    }
    @Benchmark
    public long measureTangleB()
    {
        return TangleB.nextLong2();
    }
    @Benchmark
    public long measureTangleC()
    {
        return TangleC.nextLong3();
    }

    @Benchmark
    public long measureTangleD()
    {
        return TangleD.nextLong4();
    }


    private Mover32RNG Mover32 = new Mover32RNG(0);
    private RNG Mover32R = new RNG(Mover32);

    @Benchmark
    public long measureMover32()
    {
        return Mover32.nextLong();
    }

    @Benchmark
    public int measureMover32Int()
    {
        return Mover32.next(32);
    }
    @Benchmark
    public long measureMover32R()
    {
        return Mover32R.nextLong();
    }

    @Benchmark
    public int measureMover32IntR()
    {
        return Mover32R.nextInt();
    }

//    private Mover32RNG Mover32A = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32A()
//    {
//        return Mover32A.nextIntA();
//    }
//
//    private Mover32RNG Mover32B = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32B()
//    {
//        return Mover32B.nextIntB();
//    }
//    private Mover32RNG Mover32C = new Mover32RNG(0);
//    @Benchmark
//    public long measureMover32C()
//    {
//        return Mover32C.nextIntC();
//    }

    private Mover64RNG Mover64 = new Mover64RNG(0);
    private RNG Mover64R = new RNG(Mover64);

    @Benchmark
    public long measureMover64()
    {
        return Mover64.nextLong();
    }

    @Benchmark
    public int measureMover64Int()
    {
        return Mover64.next(32);
    }
    @Benchmark
    public long measureMover64R()
    {
        return Mover64R.nextLong();
    }

    @Benchmark
    public int measureMover64IntR()
    {
        return Mover64R.nextInt();
    }
    
    private Molerat32RNG Molerat32 = new Molerat32RNG(0);
    private RNG Molerat32R = new RNG(Molerat32);

    @Benchmark
    public long measureMolerat32()
    {
        return Molerat32.nextLong();
    }

    @Benchmark
    public int measureMolerat32Int()
    {
        return Molerat32.next(32);
    }
    @Benchmark
    public long measureMolerat32R()
    {
        return Molerat32R.nextLong();
    }

    @Benchmark
    public int measureMolerat32IntR()
    {
        return Molerat32R.nextInt();
    }
    private MiniMover64RNG MiniMover64 = new MiniMover64RNG(0);
    private RNG MiniMover64R = new RNG(MiniMover64);

    @Benchmark
    public long measureMiniMover64()
    {
        return MiniMover64.nextLong();
    }

    @Benchmark
    public int measureMiniMover64Int()
    {
        return MiniMover64.next(32);
    }
    @Benchmark
    public long measureMiniMover64R()
    {
        return MiniMover64R.nextLong();
    }

    @Benchmark
    public int measureMiniMover64IntR()
    {
        return MiniMover64R.nextInt();
    }

    private SFC64RNG SFC64 = new SFC64RNG(9999L);
    private RNG SFC64R = new RNG(SFC64);
    @Benchmark
    public long measureSFC64()
    {
        return SFC64.nextLong();
    }

    @Benchmark
    public int measureSFC64Int()
    {
        return SFC64.next(32);
    }
    @Benchmark
    public long measureSFC64R()
    {
        return SFC64R.nextLong();
    }

    @Benchmark
    public int measureSFC64IntR()
    {
        return SFC64R.nextInt();
    }
    
    private Overdrive64RNG Overdrive64 = new Overdrive64RNG(0);
    private RNG Overdrive64R = new RNG(Overdrive64);

    @Benchmark
    public long measureOverdrive64()
    {
        return Overdrive64.nextLong();
    }

    @Benchmark
    public int measureOverdrive64Int()
    {
        return Overdrive64.next(32);
    }
    @Benchmark
    public long measureOverdrive64R()
    {
        return Overdrive64R.nextLong();
    }

    @Benchmark
    public int measureOverdrive64IntR()
    {
        return Overdrive64R.nextInt();
    }

    private MoverCounter64RNG MoverCounter64 = new MoverCounter64RNG(9999L);
    private RNG MoverCounter64R = new RNG(MoverCounter64);
    @Benchmark
    public long measureMoverCounter64()
    {
        return MoverCounter64.nextLong();
    }

    @Benchmark
    public int measureMoverCounter64Int()
    {
        return MoverCounter64.next(32);
    }
    @Benchmark
    public long measureMoverCounter64R()
    {
        return MoverCounter64R.nextLong();
    }

    @Benchmark
    public int measureMoverCounter64IntR()
    {
        return MoverCounter64R.nextInt();
    }




    private DirkRNG Dirk = new DirkRNG(9999L);
    private RNG DirkR = new RNG(Dirk);
    @Benchmark
    public long measureDirk()
    {
        return Dirk.nextLong();
    }

    @Benchmark
    public int measureDirkInt()
    {
        return Dirk.next(32);
    }
    @Benchmark
    public long measureDirkR()
    {
        return DirkR.nextLong();
    }

    @Benchmark
    public int measureDirkIntR()
    {
        return DirkR.nextInt();
    }

//    private Overdrive64RNG Overdrive1 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive2 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive3 = new Overdrive64RNG(0);
//    private Overdrive64RNG Overdrive4 = new Overdrive64RNG(0);
//
//    @Benchmark
//    public long measureOverdrive1()
//    {
//        return Overdrive1.nextLong1();
//    }
//    @Benchmark
//    public long measureOverdrive2()
//    {
//        return Overdrive2.nextLong2();
//    }
//    @Benchmark
//    public long measureOverdrive3()
//    {
//        return Overdrive3.nextLong3();
//    }
//    @Benchmark
//    public long measureOverdrive4()
//    {
//        return Overdrive4.nextLong4();
//    }

    //    private Thrust32RNG Thrust32 = new Thrust32RNG(9999);
//    private RNG Thrust32R = new RNG(Thrust32);
//
//    @Benchmark
//    public long measureThrust32()
//    {
//        return Thrust32.nextLong();
//    }
//
//    @Benchmark
//    public int measureThrust32Int()
//    {
//        return Thrust32.next(32);
//    }
//    @Benchmark
//    public long measureThrust32R()
//    {
//        return Thrust32R.nextLong();
//    }
//
//    @Benchmark
//    public int measureThrust32IntR()
//    {
//        return Thrust32R.nextInt();
//    }
//
//
    private ThrustAlt32RNG ThrustAlt32 = new ThrustAlt32RNG(9999);
    private RNG ThrustAlt32R = new RNG(ThrustAlt32);

    @Benchmark
    public long measureThrustAlt32()
    {
        return ThrustAlt32.nextLong();
    }

    @Benchmark
    public int measureThrustAlt32Int()
    {
        return ThrustAlt32.next(32);
    }
    @Benchmark
    public long measureThrustAlt32R()
    {
        return ThrustAlt32R.nextLong();
    }

    @Benchmark
    public int measureThrustAlt32IntR()
    {
        return ThrustAlt32R.nextInt();
    }

    private Light32RNG Light32 = new Light32RNG(9999);
    private RNG Light32R = new RNG(Light32);

    @Benchmark
    public long measureLight32()
    {
        return Light32.nextLong();
    }

    @Benchmark
    public int measureLight32Int()
    {
        return Light32.next(32);
    }
    @Benchmark
    public long measureLight32R()
    {
        return Light32R.nextLong();
    }

    @Benchmark
    public int measureLight32IntR()
    {
        return Light32R.nextInt();
    }
    private Zig32RNG Zig32 = new Zig32RNG(9999L);
    private RNG Zig32R = new RNG(Zig32);
    @Benchmark
    public long measureZig32()
    {
        return Zig32.nextLong();
    }

    @Benchmark
    public int measureZig32Int()
    {
        return Zig32.next(32);
    }
    @Benchmark
    public long measureZig32R()
    {
        return Zig32R.nextLong();
    }

    @Benchmark
    public int measureZig32IntR()
    {
        return Zig32R.nextInt();
    }

    private Zag32RNG Zag32 = new Zag32RNG(9999L);
    private RNG Zag32R = new RNG(Zag32);
    @Benchmark
    public long measureZag32()
    {
        return Zag32.nextLong();
    }

    @Benchmark
    public int measureZag32Int()
    {
        return Zag32.next(32);
    }
    @Benchmark
    public long measureZag32R()
    {
        return Zag32R.nextLong();
    }

    @Benchmark
    public int measureZag32IntR()
    {
        return Zag32R.nextInt();
    }

    private Zog32RNG Zog32 = new Zog32RNG(9999L);
    private RNG Zog32R = new RNG(Zog32);
    @Benchmark
    public long measureZog32()
    {
        return Zog32.nextLong();
    }

    @Benchmark
    public int measureZog32Int()
    {
        return Zog32.next(32);
    }
    @Benchmark
    public long measureZog32R()
    {
        return Zog32R.nextLong();
    }

    @Benchmark
    public int measureZog32IntR()
    {
        return Zog32R.nextInt();
    }

    private XoRo32RNG XoRo32 = new XoRo32RNG(9999L);
    private RNG XoRo32R = new RNG(XoRo32);
    @Benchmark
    public long measureXoRo32()
    {
        return XoRo32.nextLong();
    }

    @Benchmark
    public int measureXoRo32Int()
    {
        return XoRo32.next(32);
    }
    @Benchmark
    public long measureXoRo32R()
    {
        return XoRo32R.nextLong();
    }

    @Benchmark
    public int measureXoRo32IntR()
    {
        return XoRo32R.nextInt();
    }



    private Oriole32RNG Oriole32 = new Oriole32RNG(9999, 999, 99);
    private RNG Oriole32R = new RNG(Oriole32);
    @Benchmark
    public long measureOriole32()
    {
        return Oriole32.nextLong();
    }

    @Benchmark
    public int measureOriole32Int()
    {
        return Oriole32.next(32);
    }
    @Benchmark
    public long measureOriole32R()
    {
        return Oriole32R.nextLong();
    }

    @Benchmark
    public int measureOriole32IntR()
    {
        return Oriole32R.nextInt();
    }

    private Lathe32RNG Lathe32 = new Lathe32RNG(9999, 999);
    private RNG Lathe32R = new RNG(Lathe32);
    @Benchmark
    public long measureLathe32()
    {
        return Lathe32.nextLong();
    }

    @Benchmark
    public int measureLathe32Int()
    {
        return Lathe32.next(32);
    }
    @Benchmark
    public long measureLathe32R()
    {
        return Lathe32R.nextLong();
    }

    @Benchmark
    public int measureLathe32IntR()
    {
        return Lathe32R.nextInt();
    }

    private Starfish32RNG Starfish32 = new Starfish32RNG(9999, 999);
    private RNG Starfish32R = new RNG(Starfish32);
    @Benchmark
    public long measureStarfish32()
    {
        return Starfish32.nextLong();
    }

    @Benchmark
    public int measureStarfish32Int()
    {
        return Starfish32.next(32);
    }

    @Benchmark
    public int measureStarfish32NextInt()
    {
        return Starfish32.nextInt();
    }
    @Benchmark
    public long measureStarfish32R()
    {
        return Starfish32R.nextLong();
    }

    @Benchmark
    public int measureStarfish32IntR()
    {
        return Starfish32R.nextInt();
    }


    private GWTRNG GWT = new GWTRNG(9999, 999);
    @Benchmark
    public long measureGWT()
    {
        return GWT.nextLong();
    }

    @Benchmark
    public int measureGWTInt()
    {
        return GWT.next(32);
    }

    @Benchmark
    public int measureGWTNextInt()
    {
        return GWT.nextInt();
    }

    private Otter32RNG Otter32 = new Otter32RNG(9999, 999);
    private RNG Otter32R = new RNG(Otter32);
    @Benchmark
    public long measureOtter32()
    {
        return Otter32.nextLong();
    }

    @Benchmark
    public int measureOtter32Int()
    {
        return Otter32.next(32);
    }
    @Benchmark
    public long measureOtter32R()
    {
        return Otter32R.nextLong();
    }

    @Benchmark
    public int measureOtter32IntR()
    {
        return Otter32R.nextInt();
    }


    private Lobster32RNG Lobster32 = new Lobster32RNG(9999, 999);
    private RNG Lobster32R = new RNG(Lobster32);
    @Benchmark
    public long measureLobster32()
    {
        return Lobster32.nextLong();
    }

    @Benchmark
    public int measureLobster32Int()
    {
        return Lobster32.next(32);
    }
    @Benchmark
    public long measureLobster32R()
    {
        return Lobster32R.nextLong();
    }

    @Benchmark
    public int measureLobster32IntR()
    {
        return Lobster32R.nextInt();
    }

    private SeaSlater32RNG SeaSlater32 = new SeaSlater32RNG(9999, 999);
    private RNG SeaSlater32R = new RNG(SeaSlater32);
    @Benchmark
    public long measureSeaSlater32()
    {
        return SeaSlater32.nextLong();
    }

    @Benchmark
    public int measureSeaSlater32Int()
    {
        return SeaSlater32.next(32);
    }
    @Benchmark
    public long measureSeaSlater32R()
    {
        return SeaSlater32R.nextLong();
    }

    @Benchmark
    public int measureSeaSlater32IntR()
    {
        return SeaSlater32R.nextInt();
    }


    private Churro32RNG Churro32 = new Churro32RNG(9999, 999, 99);
    private RNG Churro32R = new RNG(Churro32);
    @Benchmark
    public long measureChurro32()
    {
        return Churro32.nextLong();
    }

    @Benchmark
    public int measureChurro32Int()
    {
        return Churro32.next(32);
    }
    @Benchmark
    public long measureChurro32R()
    {
        return Churro32R.nextLong();
    }

    @Benchmark
    public int measureChurro32IntR()
    {
        return Churro32R.nextInt();
    }
    private Dizzy32RNG Dizzy32 = new Dizzy32RNG(9999, 999, 99);
    private RNG Dizzy32R = new RNG(Dizzy32);
    @Benchmark
    public long measureDizzy32()
    {
        return Dizzy32.nextLong();
    }

    @Benchmark
    public int measureDizzy32Int()
    {
        return Dizzy32.next(32);
    }
    @Benchmark
    public long measureDizzy32R()
    {
        return Dizzy32R.nextLong();
    }

    @Benchmark
    public int measureDizzy32IntR()
    {
        return Dizzy32R.nextInt();
    }

    @Benchmark
    public long measureDizzy32IntNative1()
    {
        return Dizzy32.nextInt();
    }
    @Benchmark
    public long measureDizzy32IntNative2()
    {
        return Dizzy32.nextInt2();
    }


    private XoshiroStarStar32RNG XoshiroStarStar32 = new XoshiroStarStar32RNG(9999);
    private RNG XoshiroStarStar32R = new RNG(XoshiroStarStar32);

    @Benchmark
    public long measureXoshiroStarStar32()
    {
        return XoshiroStarStar32.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarStar32Int()
    {
        return XoshiroStarStar32.next(32);
    }
    @Benchmark
    public long measureXoshiroStarStar32R()
    {
        return XoshiroStarStar32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarStar32IntR()
    {
        return XoshiroStarStar32R.nextInt();
    }


    private XoshiroStarPhi32RNG XoshiroStarPhi32 = new XoshiroStarPhi32RNG(9999);
    private RNG XoshiroStarPhi32R = new RNG(XoshiroStarPhi32);

    @Benchmark
    public long measureXoshiroStarPhi32()
    {
        return XoshiroStarPhi32.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarPhi32Int()
    {
        return XoshiroStarPhi32.next(32);
    }
    @Benchmark
    public long measureXoshiroStarPhi32R()
    {
        return XoshiroStarPhi32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroStarPhi32IntR()
    {
        return XoshiroStarPhi32R.nextInt();
    }

    private XoshiroXara32RNG XoshiroXara32 = new XoshiroXara32RNG(9999);
    private RNG XoshiroXara32R = new RNG(XoshiroXara32);

    @Benchmark
    public long measureXoshiroXara32()
    {
        return XoshiroXara32.nextLong();
    }

    @Benchmark
    public int measureXoshiroXara32Int()
    {
        return XoshiroXara32.next(32);
    }
    @Benchmark
    public long measureXoshiroXara32R()
    {
        return XoshiroXara32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroXara32IntR()
    {
        return XoshiroXara32R.nextInt();
    }

    private XoshiroAra32RNG XoshiroAra32 = new XoshiroAra32RNG(9999);
    private RNG XoshiroAra32R = new RNG(XoshiroAra32);

    @Benchmark
    public long measureXoshiroAra32()
    {
        return XoshiroAra32.nextLong();
    }

    @Benchmark
    public int measureXoshiroAra32Int()
    {
        return XoshiroAra32.next(32);
    }
    @Benchmark
    public long measureXoshiroAra32R()
    {
        return XoshiroAra32R.nextLong();
    }

    @Benchmark
    public int measureXoshiroAra32IntR()
    {
        return XoshiroAra32R.nextInt();
    }

    private DervishRNG Dervish = new DervishRNG(9999L);
    private RNG DervishR = new RNG(Dervish);
    @Benchmark
    public long measureDervish()
    {
        return Dervish.nextLong();
    }

    @Benchmark
    public int measureDervishInt()
    {
        return Dervish.next(32);
    }
    @Benchmark
    public long measureDervishR()
    {
        return DervishR.nextLong();
    }

    @Benchmark
    public int measureDervishIntR()
    {
        return DervishR.nextInt();
    }


    private LinnormRNG Linnorm = new LinnormRNG(9999L);
    private RNG LinnormR = new RNG(Linnorm);
    @Benchmark
    public long measureLinnorm()
    {
        return Linnorm.nextLong();
    }

    @Benchmark
    public int measureLinnormInt()
    {
        return Linnorm.next(32);
    }
    @Benchmark
    public long measureLinnormR()
    {
        return LinnormR.nextLong();
    }

    @Benchmark
    public int measureLinnormIntR()
    {
        return LinnormR.nextInt();
    }

    private MizuchiRNG Mizuchi = new MizuchiRNG(9999L);
    private RNG MizuchiR = new RNG(Mizuchi);
    @Benchmark
    public long measureMizuchi()
    {
        return Mizuchi.nextLong();
    }

    @Benchmark
    public int measureMizuchiInt()
    {
        return Mizuchi.next(32);
    }
    @Benchmark
    public long measureMizuchiR()
    {
        return MizuchiR.nextLong();
    }

    @Benchmark
    public int measureMizuchiIntR()
    {
        return MizuchiR.nextInt();
    }

    private QuixoticRNG Quixotic = new QuixoticRNG(9999L);
    private RNG QuixoticR = new RNG(Quixotic);
    @Benchmark
    public long measureQuixotic()
    {
        return Quixotic.nextLong();
    }

    @Benchmark
    public int measureQuixoticInt()
    {
        return Quixotic.next(32);
    }
    @Benchmark
    public long measureQuixoticR()
    {
        return QuixoticR.nextLong();
    }

    @Benchmark
    public int measureQuixoticIntR()
    {
        return QuixoticR.nextInt();
    }

    private DiverRNG Diver = new DiverRNG(9999L);
    private RNG DiverR = new RNG(Diver);
    @Benchmark
    public long measureDiver()
    {
        return Diver.nextLong();
    }

    @Benchmark
    public int measureDiverInt()
    {
        return Diver.next(32);
    }
    @Benchmark
    public long measureDiverR()
    {
        return DiverR.nextLong();
    }

    @Benchmark
    public int measureDiverIntR()
    {
        return DiverR.nextInt();
    }




    private IsaacRNG Isaac = new IsaacRNG(9999L);
    private RNG IsaacR = new RNG(Isaac);
    @Benchmark
    public long measureIsaac()
    {
        return Isaac.nextLong();
    }

    @Benchmark
    public long measureIsaacInt()
    {
        return Isaac.next(32);
    }
    @Benchmark
    public long measureIsaacR()
    {
        return IsaacR.nextLong();
    }

    @Benchmark
    public long measureIsaacIntR()
    {
        return IsaacR.nextInt();
    }

    private Isaac32RNG Isaac32 = new Isaac32RNG(9999L);
    private RNG Isaac32R = new RNG(Isaac32);
    @Benchmark
    public long measureIsaac32()
    {
        return Isaac32.nextLong();
    }

    @Benchmark
    public long measureIsaac32Int()
    {
        return Isaac32.next(32);
    }
    @Benchmark
    public long measureIsaac32R()
    {
        return Isaac32R.nextLong();
    }

    @Benchmark
    public long measureIsaac32IntR()
    {
        return Isaac32R.nextInt();
    }

    
    
    /*
    public long doJet()
    {
        JetRNG rng = new JetRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJet() {
        seed = 9000;
        doJet();
    }

    public int doJetInt()
    {
        JetRNG rng = new JetRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetInt() {
        iseed = 9000;
        doJetInt();
    }

    public long doJetR()
    {
        RNG rng = new RNG(new JetRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetR() {
        seed = 9000;
        doJetR();
    }

    public int doJetIntR()
    {
        RNG rng = new RNG(new JetRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureJetIntR() {
        iseed = 9000;
        doJetIntR();
    }

    public long doLunge32()
    {
        Lunge32RNG rng = new Lunge32RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32() {
        seed = 9000;
        doLunge32();
    }

    public int doLunge32Int()
    {
        Lunge32RNG rng = new Lunge32RNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32Int() {
        iseed = 9000;
        doLunge32Int();
    }

    public long doLunge32R()
    {
        RNG rng = new RNG(new Lunge32RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32R() {
        seed = 9000;
        doLunge32R();
    }

    public int doLunge32IntR()
    {
        RNG rng = new RNG(new Lunge32RNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureLunge32IntR() {
        iseed = 9000;
        doLunge32IntR();
    }


    @Benchmark
    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
    public void a________measureThrustAltDetermine() {
        seed = 9000;
        long state = 9000L;
        for (int i = 0; i < 1000000007; i++) {
            seed += ThrustAltRNG.determine(++state);
        }
    }

//    // Performs rather poorly, surprisingly. JIT needs method calls rather than inlined code, it looks like.
//    @Benchmark
//    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
//    public void a________measureDetermineBare() {
//        seed = 9000;
//        long running = seed, state = 9000L;
//        for (int i = 0; i < 1000000007; i++) {
//            seed += ((state = ((running += 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22));
//        }
//    }
    @Benchmark
    @Warmup(iterations = 10) @Measurement(iterations = 8) @Fork(1)
    public void a________measureRandomness() {
        seed = 9000;
        ThrustAltRNG rng = new ThrustAltRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
    }

    */






//    public long doVortex()
//    {
//        VortexRNG rng = new VortexRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortex() {
//        seed = 9000;
//        doVortex();
//    }
//
//    public int doVortexInt()
//    {
//        VortexRNG rng = new VortexRNG(iseed);
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexInt() {
//        iseed = 9000;
//        doVortexInt();
//    }
//    public long doVortexR()
//    {
//        RNG rng = new RNG(new VortexRNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexR() {
//        seed = 9000;
//        doVortexR();
//    }
//
//    public int doVortexIntR()
//    {
//        RNG rng = new RNG(new VortexRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a________measureVortexIntR() {
//        iseed = 9000;
//        doVortexIntR();
//    }



//    public long doSquirrel()
//    {
//        SquirrelRNG rng = new SquirrelRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrel() throws InterruptedException {
//        seed = 9000;
//        doSquirrel();
//    }
//
//    public int doSquirrelInt()
//    {
//        SquirrelRNG rng = new SquirrelRNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelInt() throws InterruptedException {
//        iseed = 9000;
//        doSquirrelInt();
//    }
//
//    public long doSquirrelR()
//    {
//        RNG rng = new RNG(new SquirrelRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelR() throws InterruptedException {
//        seed = 9000;
//        doSquirrelR();
//    }
//
//    public int doSquirrelIntR()
//    {
//        RNG rng = new RNG(new SquirrelRNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measureSquirrelIntR() throws InterruptedException {
//        iseed = 9000;
//        doSquirrelIntR();
//    }


//    public long doRule90()
//    {
//        Rule90RNG rng = new Rule90RNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90() throws InterruptedException {
//        seed = 9000;
//        doRule90();
//    }
//
//    public int doRule90Int()
//    {
//        Rule90RNG rng = new Rule90RNG(iseed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90Int() throws InterruptedException {
//        iseed = 9000;
//        doRule90Int();
//    }
//
//    public long doRule90R()
//    {
//        RNG rng = new RNG(new Rule90RNG(seed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90R() throws InterruptedException {
//        seed = 9000;
//        doRule90R();
//    }
//
//    public int doRule90IntR()
//    {
//        RNG rng = new RNG(new Rule90RNG(iseed));
//
//        for (int i = 0; i < 1000000007; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
//    public void a__measure90IntR() throws InterruptedException {
//        iseed = 9000;
//        doRule90IntR();
//    }


    
    /*
    public long doZap()
    {
        ZapRNG rng = new ZapRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }
    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZap() throws InterruptedException {
        seed = 9000;
        doZap();
    }

    public int doZapInt()
    {
        ZapRNG rng = new ZapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapInt() throws InterruptedException {
        iseed = 9000;
        doZapInt();
    }

    public long doZapR()
    {
        RNG rng = new RNG(new ZapRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapR() throws InterruptedException {
        seed = 9000;
        doZapR();
    }

    public int doZapIntR()
    {
        RNG rng = new RNG(new ZapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measureZapIntR() throws InterruptedException {
        iseed = 9000;
        doZapIntR();
    }




    public long doSlap()
    {
        SlapRNG rng = new SlapRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlap() throws InterruptedException {
        seed = 9000;
        doSlap();
    }

    public int doSlapInt()
    {
        SlapRNG rng = new SlapRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapInt() throws InterruptedException {
        iseed = 9000;
        doSlapInt();
    }

    public long doSlapR()
    {
        RNG rng = new RNG(new SlapRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapR() throws InterruptedException {
        seed = 9000;
        doSlapR();
    }

    public int doSlapIntR()
    {
        RNG rng = new RNG(new SlapRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void measureSlapIntR() throws InterruptedException {
        iseed = 9000;
        doSlapIntR();
    }

*/
    
    
    
    
    
    
    
    
/*
    public long doPlaceholder()
    {
        PlaceholderRNG rng = new PlaceholderRNG(seed);

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholder() throws InterruptedException {
        seed = 9000;
        doPlaceholder();
    }

    public int doPlaceholderInt()
    {
        PlaceholderRNG rng = new PlaceholderRNG(iseed);

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderInt() throws InterruptedException {
        iseed = 9000;
        doPlaceholderInt();
    }

    public long doPlaceholderR()
    {
        RNG rng = new RNG(new PlaceholderRNG(seed));

        for (int i = 0; i < 1000000007; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderR() throws InterruptedException {
        seed = 9000;
        doPlaceholderR();
    }

    public int doPlaceholderIntR()
    {
        RNG rng = new RNG(new PlaceholderRNG(iseed));

        for (int i = 0; i < 1000000007; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
     // @Warmup(iterations = 4) @Measurement(iterations = 4) @Fork(1)
    public void aa_measurePlaceholderIntR() throws InterruptedException {
        iseed = 9000;
        doPlaceholderIntR();
    }
*/

    private Random JDK = new Random(9999L);
    @Benchmark
    public long measureJDK()
    {
        return JDK.nextLong();
    }

    @Benchmark
    public int measureJDKInt()
    {
        return JDK.nextInt();
    }

    /*
mvn clean install
java -jar target/benchmarks.jar RNGBenchmark -wi 5 -i 5 -f 1 -gc true
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RNGBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
