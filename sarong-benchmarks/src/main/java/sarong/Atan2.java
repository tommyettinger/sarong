package sarong;

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * Most of this benchmark is fixed up from <a href="http://www.java-gaming.org/topics/fast-atan2-again/38409/view.html">This Java-Gaming post</a>.
 * The benchmark has been modified to choose x and y from a large array of random floats or doubles, varying in sign.
 * This thrashes branch prediction, causing the DSP code (as an example) to go from roughly 6 ns (when sign is always
 * positive for x and y) to 16 ns (with varied signs for each).
 * <pre>
 *                       [-----------Speed--------------]  [-----Quality-----]
 * Benchmark             Mode  Cnt   Score   Error  Units  Average  Worst-Case
 * Atan2.apache          avgt    4  75.494 ± 2.209  ns/op  0.00000  0.00000
 * Atan2.baselineDouble  avgt    4   4.210 ± 0.220  ns/op  
 * Atan2.baselineFloat   avgt    4   4.264 ± 0.080  ns/op  
 * Atan2.diamond         avgt    4  16.643 ± 0.658  ns/op  0.04318  0.07112
 * Atan2.dspAccurate     avgt    4  16.836 ± 1.016  ns/op  0.00344  0.01015
 * Atan2.dspFast         avgt    4  16.704 ± 1.418  ns/op  0.04318  0.07111
 * Atan2.dspOpt          avgt    4  16.736 ± 0.094  ns/op  0.04318  0.07111
 * Atan2.gdx             avgt    4  19.660 ± 0.656  ns/op  0.00231  0.00488
 * Atan2.icecore         avgt    4  21.107 ± 0.357  ns/op  0.00038  0.00098
 * Atan2.kappa           avgt    4  19.482 ± 0.617  ns/op  0.00224  0.00468
 * Atan2.math            avgt    4  81.711 ± 1.487  ns/op  0.00000  0.00000
 * Atan2.riven           avgt    4  22.850 ± 0.673  ns/op  0.00290  0.00787
 * Atan2.squid           avgt    4  22.150 ± 1.042  ns/op  0.00007  0.00020
 * Atan2.squidRough      avgt    4  21.261 ± 0.967  ns/op  0.00237  0.00376
 * </pre>
 * Apache's FastMath is 100% accurate to Math.atan2(), but is barely any faster, so it doesn't really compare here.
 * Squid has (by far) the closest approximation that is still significantly faster than Math.atan2().
 * Icecore is slightly faster than Squid and has the second-best quality, but it uses a lookup table.
 * Diamond and DspFast have way too much error to be useful in the general case, but are very fast.
 * Kappa is the fastest without horrible quality, but it's still just 18% faster than Squid (accounting for baseline).
 * Gdx is slightly slower and lower-quality than Kappa.
 * Riven should not be used.
 * SquidRough should probably not be used either.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 4)
@Measurement(iterations = 4)
public class Atan2 {

    ///////////////////////////////////////
    // Benchmark Settings
    ///////////////////////////////////////

    private static final double LOW_D = -3;
    private static final double HIGH_D = 3;
    private static final double INC_D = 0.001D;
    private static final float LOW_F = (float) LOW_D;
    private static final float HIGH_F = (float) HIGH_D;
    private static final float INC_F = (float) INC_D;
    private static final double[] inputs = new double[65536];
    private final float[] floatInputs = new float[65536];
    private short counterA = -0x8000, counterB = -0x4000;
    {
        for (int i = 0; i < 65536; i++) {
            floatInputs[i] = (float) (inputs[i] =
                    (LinnormRNG.determine(i) >> 11) * 0x1p-40);
        }
    }

    public static Options buildOptions() {
        return new OptionsBuilder()
                .include(Atan2Benchmark.class.getSimpleName())
                .warmupTime(new TimeValue(2, TimeUnit.SECONDS))
                .warmupIterations(4)
                .measurementTime(new TimeValue(2, TimeUnit.SECONDS))
                .measurementIterations(4)
                .forks(1)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .threads(1)
                .build();
    }

    public static class Atan2Benchmark {
        private String name;

        public Atan2Benchmark() {
            name = "Empty";
        }

        Atan2Benchmark(String name) {
            this.name = name;
        }

        public float test(float y, float x) {
            return 1f;
        }

        private void printError(float low, float high, float inc) {
            double total = 0.0;
            double largestError = 0.0;
            int count = 0;
            for (float y = low; y <= high; y += inc) {
                for (float x = low; x <= high; x += inc) {
                    double result;

                    result = Math.abs(Math.atan2(y, x) - test(y, x));
                    total += result;
                    largestError = result > largestError ? result : largestError;

                    count++;
                }
            }
            System.out.printf("%15s: Average Error %.5f / Largest Error %.5f\n", name, total / count, largestError);

        }
    }
    ///////////////////////////////////////
    // Accuracy
    ///////////////////////////////////////

    public static void main(String[] args) {
        Atan2Benchmark[] benchmarks = new Atan2Benchmark[]{
                new Apache(),
                new Default(),
                new Diamond(),
                new DSPAccurate(),
                new DSPFast(),
                new DSPOpt(),
                new Gdx(),
                new Icecore(),
                new Kappa(),
                new Riven(),
                new Squid(),
                new SquidRough(),
        };

        System.out.println("A lower average means higher accuracy.");
        for (Atan2Benchmark benchmark : benchmarks) {
            benchmark.printError(LOW_F, HIGH_F, INC_F);
        }
    }

    ///////////////////////////////////////
    // Baseline
    ///////////////////////////////////////

    @Benchmark
    public double baselineDouble()
    {
        return inputs[counterA++ & 0xFFFF] + inputs[counterB++ & 0xFFFF];
    }

    @Benchmark
    public float baselineFloat()
    {
        return floatInputs[counterA++ & 0xFFFF] + floatInputs[counterB++ & 0xFFFF];
    }

    @Benchmark
    public double math()
    {
        return Math.atan2(inputs[counterA++ & 0xFFFF], inputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public double apache()
    {
        return FastMath.atan2(inputs[counterA++ & 0xFFFF], inputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float riven()
    {
        return Riven.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float dspFast()
    {
        return DSPFast.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float dspOpt()
    {
        return NumberTools.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float dspAccurate()
    {
        return DSPAccurate.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }
    @Benchmark
    public float kappa()
    {
        return Kappa.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float icecore()
    {
        return Icecore.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float diamond()
    {
        return Diamond.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float squidRough()
    {
        return SquidRough.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }

    @Benchmark
    public float squid()
    {
        return Squid.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }
    
    @Benchmark
    public float gdx()
    {
        return Gdx.atan2(floatInputs[counterA++ & 0xFFFF], floatInputs[counterB++ & 0xFFFF]);
    }


//    @Benchmark
//    public double baseline() {
//        double sum = 0;
//        for (double x = LOW_D; x < HIGH_D; x += INC_D) {
//            for (double y = LOW_D; y < HIGH_D; y += INC_D) {
//                sum += x + y;
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public double atan2_default() {
//        double sum = 0;
//        for (double y = LOW_D; y < HIGH_D; y += INC_D) {
//            for (double x = LOW_D; x < HIGH_D; x += INC_D) {
//                sum += Math.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public double atan2_apache() {
//        double sum = 0;
//        for (double y = LOW_D; y < HIGH_D; y += INC_D) {
//            for (double x = LOW_D; x < HIGH_D; x += INC_D) {
//                sum += FastMath.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public float atan2_riven() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += Riven.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public float atan2_dsp_fast() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += DSPFast.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//
//    @Benchmark
//    public float atan2_dsp_accurate() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += DSPAccurate.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//
//    @Benchmark
//    public float atan2_kappa() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += Kappa.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public float atan2_icecore() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += Icecore.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//
//    @Benchmark
//    public float atan2_diamond() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += Diamond.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//
//    @Benchmark
//    public float atan2_squid() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += SquidRough.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//
//    @Benchmark
//    public float atan2_gdx() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += MathUtils.atan2(y, x);
//            }
//        }
//        return sum;
//    }
//    @Benchmark
//    public float atan2_squid2() {
//        float sum = 0;
//        for (float y = LOW_F; y < HIGH_F; y += INC_F) {
//            for (float x = LOW_F; x < HIGH_F; x += INC_F) {
//                sum += Squid.atan2(y, x);
//            }
//        }
//        return sum;
//    }




    ///////////////////////////////////////
    // Default atan2
    ///////////////////////////////////////

    public final static class Default extends Atan2Benchmark {

        Default() {
            super("Default");
        }

        @Override
        public float test(float y, float x) {
            return (float) Math.atan2(y, x);
        }
    }
    
    ///////////////////////////////////////
    // Apache's FastMath.atan2 ( http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/util/FastMath.html )
    ///////////////////////////////////////

    public final static class Apache extends Atan2Benchmark {

        Apache() {
            super("Apache");
        }

        @Override
        public float test(float y, float x) {
            return (float) FastMath.atan2(y, x);
        }
    }

    ///////////////////////////////////////
    // Riven's atan2 ( http://www.java-gaming.org/index.php?topic=14647.0 )
    ///////////////////////////////////////

    public final static class Riven extends Atan2Benchmark {

        Riven() {
            super("Riven");
        }

        @Override
        public float test(float y, float x) {
            return Riven.atan2(y, x);
        }

        private static final int ATAN2_BITS = 7;

        private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
        private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
        private static final int ATAN2_COUNT = ATAN2_MASK + 1;
        private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

        private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

        private static final float[] atan2 = new float[ATAN2_COUNT];

        static {
            for (int i = 0; i < ATAN2_DIM; i++) {
                for (int j = 0; j < ATAN2_DIM; j++) {
                    float x0 = (float) i / ATAN2_DIM;
                    float y0 = (float) j / ATAN2_DIM;
                    atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
                }
            }
        }

        static float atan2(float y, float x) {
            float add, mul;

            if (x < 0.0f) {
                if (y < 0.0f) {
                    x = -x;
                    y = -y;

                    mul = 1.0f;
                } else {
                    x = -x;
                    mul = -1.0f;
                }
                add = -3.141592653f;
            } else {
                if (y < 0.0f) {
                    y = -y;
                    mul = -1.0f;
                } else {
                    mul = 1.0f;
                }
                add = 0.0f;
            }

            float invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

            int xi = (int) (x * invDiv);
            int yi = (int) (y * invDiv);

            return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
        }
    }

    ///////////////////////////////////////
    // DSP's atan2 ( http://dspguru.com/dsp/tricks/fixed-point-atan2-with-self-normalization )
    ///////////////////////////////////////

    public final static class DSPFast extends Atan2Benchmark {

        DSPFast() {
            super("DSPFast");
        }

        @Override
        public float test(float y, float x) {
            return DSPFast.atan2(y, x);
        }

        private static final float PI_2 = 1.5707963F; // Math.PI / 2
        private static final float PI_4 = 0.7853982F; // Math.PI / 4
        private static final float PI_3_4 = 2.3561945F; // (Math.PI / 4) * 3
        private static final float MINUS_PI_2 = -1.5707963F; // Math.PI / -2

        static float atan2(float y, float x) {
            float r;
            float abs_y = Float.intBitsToFloat(Float.floatToRawIntBits(y) << 1 >>> 1);
            if (x == 0.0F) {
                if (y > 0.0F) {
                    return PI_2;
                }
                if (y == 0.0F) {
                    return 0.0f;
                }
                return MINUS_PI_2;
            } else if (x > 0) {
                r = (x - abs_y) / (x + abs_y);
                r = PI_4 - PI_4 * r;
            } else {
                r = (x + abs_y) / (abs_y - x);
                r = PI_3_4 - PI_4 * r;
            }
            return y < 0 ? -r : r;
        }
    }

    public final static class DSPAccurate extends Atan2Benchmark {

        DSPAccurate() {
            super("DSPAccurate");
        }

        @Override
        public float test(float y, float x) {
            return DSPAccurate.atan2(y, x);
        }

        private static final float PI_2 = 1.5707963F; // Math.PI / 2
        private static final float PI_4 = 0.7853982F; // Math.PI / 4
        private static final float PI_3_4 = 2.3561945F; // (Math.PI / 4) * 3
        private static final float MINUS_PI_2 = -1.5707963F; // Math.PI / -2

        static float atan2(float y, float x) {
            float r;
            float c;
            float abs_y = Float.intBitsToFloat(Float.floatToRawIntBits(y) << 1 >>> 1);
            if (x == 0.0F) {
                if (y > 0.0F) {
                    return PI_2;
                }
                if (y == 0.0F) {
                    return 0.0f;
                }
                return MINUS_PI_2;
            } else if (x > 0) {
                r = (x - abs_y) / (x + abs_y);
                c = PI_4;
            } else {
                r = (x + abs_y) / (abs_y - x);
                c = PI_3_4;
            }
            r = 0.1963F * r * r * r - 0.9817F * r + c;
            return y < 0 ? -r : r;
        }
    }
    public final static class DSPOpt extends Atan2Benchmark {

        DSPOpt() {
            super("DSPOpt");
        }

        @Override
        public float test(float y, float x) {
            return NumberTools.atan2(y, x);
        }
    }

    ///////////////////////////////////////
    // kappa's atan2 ( http://www.java-gaming.org/topics/extremely-fast-atan2/36467/msg/346112/view.html#msg346112 )
    ///////////////////////////////////////

    public final static class Kappa extends Atan2Benchmark {

        Kappa() {
            super("Kappa");
        }

        @Override
        public float test(float y, float x) {
            return Kappa.atan2(y, x);
        }

        private static final float PI = 3.1415927f;
        private static final float PI_2 = PI / 2f;
        private static final float MINUS_PI_2 = -PI_2;
        private static final float C_M = 0.2808722f;
        private static final float C_A = 0.2808722f;

        static float atan2(float y, float x) {
            if (x == 0.0f) {
                if (y > 0.0f) {
                    return PI_2;
                }
                if (y == 0.0f) {
                    return 0.0f;
                }
                return MINUS_PI_2;
            }

            final float atan;
            final float z = y / x;
            if (Math.abs(z) < 1.0f) {
                atan = z / (z * z * C_M + 1.0f);
                if (x < 0.0f) {
                    return (y < 0.0f) ? atan - PI : atan + PI;
                }
                return atan;
            } else {
                atan = PI_2 - z / (z * z + C_A);
                return (y < 0.0f) ? atan - PI : atan;
            }
        }
    }

    ///////////////////////////////////////
    // Icecore's atan2 ( http://www.java-gaming.org/topics/extremely-fast-atan2/36467/msg/346145/view.html#msg346145 )
    ///////////////////////////////////////

    public final static class Icecore extends Atan2Benchmark {

        Icecore() {
            super("Icecore");
        }

        @Override
        public float test(float y, float x) {
            return Icecore.atan2(y, x);
        }

        private static final float PI = (float) Math.PI;
        private static final float PI_2 = PI / 2;
        private static final float PI_NEG_2 = -PI_2;
        private static final int SIZE = 1024;
        private static final float ATAN2[];

        static {
            final int Size_Ar = SIZE + 1;
            ATAN2 = new float[Size_Ar];
            for (int i = 0; i <= SIZE; i++) {
                double d = (double) i / SIZE;
                double x = 1;
                double y = x * d;
                ATAN2[i] = (float) Math.atan2(y, x);
            }
        }

        static float atan2(float y, float x) {
            if (y < 0) {
                if (x < 0) {
                    // (y < x) because == (-y > -x)
                    if (y < x) {
                        return PI_NEG_2 - ATAN2[(int) (x / y * SIZE)];
                    } else {
                        return ATAN2[(int) (y / x * SIZE)] - PI;
                    }
                } else {
                    y = -y;
                    if (y > x) {
                        return ATAN2[(int) (x / y * SIZE)] - PI_2;
                    } else {
                        return -ATAN2[(int) (y / x * SIZE)];
                    }
                }
            } else {
                if (x < 0) {
                    x = -x;
                    if (y > x) {
                        return PI_2 + ATAN2[(int) (x / y * SIZE)];
                    } else {
                        return PI - ATAN2[(int) (y / x * SIZE)];
                    }
                } else {
                    if (y > x) {
                        return PI_2 - ATAN2[(int) (x / y * SIZE)];
                    } else {
                        return ATAN2[(int) (y / x * SIZE)];
                    }
                }
            }
        }
    }

    ///////////////////////////////////////
    // Diamond's atan2 ( https://stackoverflow.com/questions/1427422/cheap-algorithm-to-find-measure-of-angle-between-vectors/14675998#14675998 )
    ///////////////////////////////////////

    public final static class Diamond extends Atan2Benchmark {

        Diamond() {
            super("Diamond");
        }

        @Override
        public float test(float y, float x) {
            return Diamond.atan2(y, x);
        }

        private static final float PI_2 = 1.5707963F; // Math.PI / 2

        static float atan2(float y, float x) {
            float angle;
            if (y == 0f && x >= 0f) {
                return 0;
            } else if (y >= 0f) {
                if (x >= 0f) {
                    angle = y / (x + y);
                } else {
                    angle = 1f - x / (-x + y);
                }
            } else {
                if (x < 0f) {
                    angle = -2f + y / (x + y);
                } else {
                    angle = -1f + x / (x - y);
                }
            }
            return angle * PI_2;
        }
    }

    /**
     * SquidLib's NumberTools.atan2Rough() method.
     * Less-precise but somewhat faster approximation of the frequently-used trigonometric method atan2, with
     * worse average and max error than NumberTools.atan2(float, float) but better error all-around than the old
     * implementation of atan2() in SquidLib. Should be up to twice as fast as NumberTools.atan2(float, float).
     * Should be fine for things at coarse-grid-level precision, like cells in a dungeon map, but less fitting for tasks
     * like map projections that operate on finer grids.
     * <br>
     * Credit to Sreeraman Rajan, Sichun Wang, Robert Inkol, and Alain Joyal in
     * <a href="https://www.researchgate.net/publication/3321724_Streamlining_Digital_Signal_Processing_A_Tricks_of_the_Trade_Guidebook_Second_Edition">this DSP article</a>.
     */
    public final static class SquidRough extends Atan2Benchmark
    {
        SquidRough() {
            super("SquidRough");
        }

        @Override
        public float test(float y, float x) {
            return SquidRough.atan2(y, x);
        }

        static float atan2(final float y, final float x)
        {
            if(y == 0f && x >= 0f) return 0f;
            final float ax = Math.abs(x), ay = Math.abs(y);
            if(ax < ay)
            {
                final float a = ax / ay,
                        r = 1.57079637f - (a * (0.7853981633974483f + 0.273f * (1f - a)));
                return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
            }
            else {
                final float a = ay / ax,
                        r = (a * (0.7853981633974483f + 0.273f * (1f - a)));
                return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
            }

        }

    }

    /**
     * SquidLib's NumberTools.atan2() method.
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than LibGDX's atan2
     * approximation. Maximum error is below 0.001 radians.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in radians.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to LibGDX'
     * MathUtils approximation of the same method; MathUtils seems to have worse average error, though.
     * Credit to StackExchange user njuffa, who gave
     * <a href="https://math.stackexchange.com/a/1105038">this useful answer</a>.
     */
    public final static class Squid extends Atan2Benchmark
    {
        Squid() {
            super("Squid");
        }

        @Override
        public float test(float y, float x) {
            return Squid.atan2(y, x);
        }

        static float atan2(final float y, final float x)
        {
            if(y == 0f && x >= 0f) return 0f;
            final float ax = Math.abs(x), ay = Math.abs(y);
            if(ax < ay)
            {
                final float a = ax / ay, s = a * a,
                        r = 1.57079637f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
                return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
            }
            else {
                final float a = ay / ax, s = a * a,
                        r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
                return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
            }
        }

    }

    public final static class Gdx extends Atan2Benchmark
    {
        static final float PI = 3.1415927f;

        Gdx() {
            super("Gdx");
        }

        @Override
        public float test(float y, float x) {
            return Gdx.atan2(y, x);
        }
        /** Returns atan2 in radians, faster but less accurate than Math.atan2. Average error of 0.00231 radians (0.1323 degrees),
         * largest error of 0.00488 radians (0.2796 degrees). */
        static float atan2 (float y, float x) {
            if (x == 0f) {
                if (y > 0f) return PI / 2;
                if (y == 0f) return 0f;
                return -PI / 2;
            }
            final float atan, z = y / x;
            if (Math.abs(z) < 1f) {
                atan = z / (1f + 0.28f * z * z);
                if (x < 0f) return atan + (y < 0f ? -PI : PI);
                return atan;
            }
            atan = PI / 2 - z / (z * z + 0.28f);
            return y < 0f ? atan - PI : atan;
        }

    }

}