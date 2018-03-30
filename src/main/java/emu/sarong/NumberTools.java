package squidpony.squidmath;

import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 */
public class NumberTools {

    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wda = Float64ArrayNative.create(wba.buffer(), 0, 1);

    public static long doubleToLongBits(final double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static long doubleToRawLongBits(final double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static double longBitsToDouble(final long bits) {
        wia.set(1, (int)(bits >>> 32));
        wia.set(0, (int)(bits & 0xFFFFFFFF));
        return wda.get(0);
    }

    public static int doubleToLowIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(0);
    }
    public static int doubleToHighIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(1);
    }
    public static int doubleToMixedIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(0) ^ wia.get(1);
    }

    public static double setExponent(final double value, final int exponentBits)
    {
        wda.set(0, value);
        wia.set(1, (wia.get(1) & 0xfffff) | exponentBits << 20);
        return wda.get(0);
    }

    public static double bounce(final double value)
    {
        wda.set(0, value);
        final int s = wia.get(1) & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, wia.get(0) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static float bounce(final float value)
    {
        wfa.set(0, value);
        final int s = wia.get(0) & 0x007fffff, flip = -((s & 0x00400000)>>22);
        wia.set(0, ((s ^ flip) & 0x007fffff) | 0x40800000);
        return wfa.get(0) - 5f;
    }

    public static double bounce(final long value)
    {
        final int s = (int)(value>>>32&0xfffff), flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, ((int)(value & 0xFFFFFFFF)) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double bounce(final int valueLow, final int valueHigh)
    {
        final int s = valueHigh & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, valueLow ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double zigzag(final double value)
    {
        wda.set(0, value + (value < 0.0 ? -2.0 : 2.0));
        final int s = wia.get(1), m = (s >>> 20 & 0x7FF) - 0x400, sm = s << m, flip = -((sm & 0x80000)>>19);
        wia.set(1, ((sm ^ flip) & 0xFFFFF) | 0x40100000);
        wia.set(0, wia.get(0) ^ flip);
        return (wda.get(0) - 5.0);
    }

    public static float zigzag(final float value)
    {
        wfa.set(0, value + (value < 0f ? -2f : 2f));
        final int s = wia.get(0), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        wia.set(0, ((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40800000);
        return wfa.get(0) - 5f;
    }

    public static double sway(final double value)
    {
        wda.set(0, value + (value < 0.0 ? -2.0 : 2.0));
        final int s = wia.get(1), m = (s >>> 20 & 0x7FF) - 0x400, sm = s << m, flip = -((sm & 0x80000)>>19);
        wia.set(1, ((sm ^ flip) & 0xFFFFF) | 0x40000000);
        wia.set(0, wia.get(0) ^ flip);
        final double a = wda.get(0) - 2.0;
        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0) * 2.0 - 1.0;
    }

    public static float sway(final float value)
    {
        wfa.set(0, value + (value < 0f ? -2f : 2f));
        final int s = wia.get(0), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        wia.set(0, ((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000);
        final float a = wfa.get(0) - 2f;
        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
    }

    public static float swayTight(final float value)
    {
        wfa.set(0, value + (value < 0f ? -2f : 2f));
        final int s = wia.get(0), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        wia.set(0, ((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000);
        final float a = wfa.get(0) - 2f;
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }

    public static double swayTight(final double value)
    {
        wda.set(0, value + (value < 0.0 ? -2.0 : 2.0));
        final int s = wia.get(1), m = (s >>> 20 & 0x7FF) - 0x400, sm = s << m, flip = -((sm & 0x80000)>>19);
        wia.set(1, ((sm ^ flip) & 0xFFFFF) | 0x40000000);
        wia.set(0, wia.get(0) ^ flip);
        final double a = wda.get(0) - 2.0;
        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0);
    }

    public static int floatToIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }
    public static int floatToRawIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }

    public static float intBitsToFloat(final int bits) {
        wia.set(0, bits);
        return wfa.get(0);
    }

    public static byte getSelectedByte(final double value, final int whichByte)
    {
        wda.set(0, value);
        return wba.get(whichByte & 7);
    }

    public static double setSelectedByte(final double value, final int whichByte, final byte newValue)
    {
        wda.set(0, value);
        wba.set(whichByte & 7, newValue);
        return wda.get(0);
    }

    public static byte getSelectedByte(final float value, final int whichByte)
    {
        wfa.set(0, value);
        return wba.get(whichByte & 3);
    }

    public static float setSelectedByte(final float value, final int whichByte, final byte newValue)
    {
        wfa.set(0, value);
        wba.set(whichByte & 3, newValue);
        return wfa.get(0);
    }

    public static long splitMix64(long state) {
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static double randomDouble(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public static float randomFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) & 0xFFFFFF) * 0x1p-24f;
    }

    public static float randomSignedFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) >> 39) * 0x1p-24f;
    }

    public static float randomFloatCurved(long seed)
    {
        return formCurvedFloat(((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)));
    }

    public static float formFloat(final int seed)
    {
        wia.set(0, (seed & 0x7FFFFF) | 0x3f800000);
        return wfa.get(0) - 1f;
    }
    public static float formSignedFloat(final int seed)
    {
        wia.set(0, (seed & 0x7FFFFF) | 0x40000000);
        return wfa.get(0) - 3f;
    }

    public static double formDouble(final long seed)
    {
        wia.set(1, (int)(seed >>> 32 & 0xFFFFF) | 0x3ff00000);
        wia.set(0, (int)(seed & 0xFFFFFFFF));
        return wda.get(0) - 1.0;
    }

    public static double formSignedDouble(final long seed)
    {
        wia.set(1, (int)(seed >>> 32 & 0xFFFFF) | 0x40000000);
        wia.set(0, (int)(seed & 0xFFFFFFFF));
        return wda.get(0) - 3.0;
    }
    public static double formCurvedDouble(long start) {
        return    longBitsToDouble((start >>> 12) | 0x3fe0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L);
    }
    public static double formCurvedDoubleTight(long start) {
        return  0.5
                + longBitsToDouble((start >>> 12) | 0x3fd0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L);
    }
    public static float formCurvedFloat(final long start) {
        return    intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                - intBitsToFloat(((int)(start ^ ~start >>> 20) & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((int) (~start ^ start >>> 30) & 0x007FFFFF) | 0x3F000000)
                ;
    }
    public static float formCurvedFloat(final int start1, final int start2) {
        return    intBitsToFloat(start1 >>> 9 | 0x3F000000)
                + intBitsToFloat((~start1 & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(start2 >>> 9 | 0x3F000000)
                - intBitsToFloat((~start2 & 0x007FFFFF) | 0x3F000000)
                ;
    }
    public static float formCurvedFloat(final int start) {
        return    intBitsToFloat(start >>> 9 | 0x3F000000)
                + intBitsToFloat((start & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((start << 18 & 0x007FFFFF) ^ ~start >>> 14) | 0x3F000000)
                - intBitsToFloat(((start << 13 & 0x007FFFFF) ^ ~start >>> 19) | 0x3F000000)
                ;
    }
    public static int lowestOneBit(int num)
    {
        return num & ~(num - 1);
    }

    public static long lowestOneBit(long num)
    {
        return num & ~(num - 1L);
    }

    public static double sin(double radians)
    {
        radians = radians * 0.6366197723675814;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    public static double cos(double radians)
    {
        radians = radians * 0.6366197723675814 + 1.0;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    public static float sin(float radians)
    {
        radians = radians * 0.6366197723675814f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    public static float cos(float radians)
    {
        radians = radians * 0.6366197723675814f + 1f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }
    /**
     * Rather rough approximation of the frequently-used trigonometric method atan2, meant for speed rather than high
     * precision. Maximum error is below 0.07 radians, though most angles apparently have a much lower average error.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in radians.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a double
     */
    public static double atan2(double y, double x) {
        if(y == 0.0)
        {
            return x < 0 ? 3.141592653589793 : 0.0;
        }
        else if(y < 0.0)
        {
            if (x >= 0.0) {
                return 0.7853981633974483 - 0.7853981633974483 * ((x + y) / (x - y));
            } else {
                return 2.3561944901923453 - 0.7853981633974483 * ((x - y) / (-y - x));
            }
        }
        else
        {
            if (x >= 0.0) {
                return 0.7853981633974483 - 0.7853981633974483 * ((x - y) / (x + y));
            } else {
                return 2.3561944901923453 - 0.7853981633974483 * ((x + y) / (y - x));
            }

        }
    }
    /**
     * Rather rough approximation of the frequently-used trigonometric method atan2, meant for speed rather than high
     * precision. Maximum error is below 0.07 radians, though most angles apparently have a much lower average error.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in radians.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a float
     */
    public static float atan2(float y, float x) {
        if(y == 0f)
        {
            return x < 0f ? 3.141592653589793f : 0.0f;
        }
        else if(y < 0.0f)
        {
            if (x >= 0.0f) {
                return 0.7853981633974483f - 0.7853981633974483f * ((x + y) / (x - y));
            } else {
                return 2.3561944901923453f - 0.7853981633974483f * ((x - y) / (-y - x));
            }
        }
        else
        {
            if (x >= 0.0f) {
                return 0.7853981633974483f - 0.7853981633974483f * ((x - y) / (x + y));
            } else {
                return 2.3561944901923453f - 0.7853981633974483f * ((x + y) / (y - x));
            }

        }
    }

}
