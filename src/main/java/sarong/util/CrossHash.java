package sarong.util;

import sarong.NumberTools;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * There's a large set of hash and hash64 methods in CrossHash (and the same set in
 * CrossHash.Falcon, CrossHash.Lightning, and CrossHash.Storm) that allow hashing for
 * primitive arrays Object arrays (which get the hashCode() of each object and use
 * that like an int in an int array), char arrays with specified ranges to hash
 * (ignoring chars outside the range), CharSequence objects (including Strings), and
 * arrays (or arrays of arrays) of CharSequence objects. Each hash() method returns
 * an int, while each hash64() method returns a long. In some cases, the hash
 * algorithm is optimized for 32-bit or 64-bit math, so hash() and hash64() return
 * unrelated numbers; in other cases the result of hash() is simply a truncated
 * version of hash64().
 * <br>
 * The main, generally-useful version of the hash functions is not in an inner class;
 * this algorithm is called Wisp. Wisp is the fastest of all the algorithms here and
 * seems to have no noticeable quality issues, so that's why it's the default. It uses
 * very, very few operations per element and has what would appear to be a simplistic
 * finalization step, but in practice it performs very well. Wisp may change in future
 * versions if improvements are found, but CrossHash's existing inner classes (Falcon,
 * Lightning, and Storm) probably won't change much, if at all.
 * <br>
 * There are three inner classes that emphasize other properties. {@link Lightning} is
 * a good stand-in for Wisp if Wisp has issues with certain data. Lightning uses a
 * relatively low number of simple steps for most of the hashing process, but due to
 * a more-elaborate finalization step it should have virtually no correlation between
 * input and output, with many bits potentially avalanching on any change in the input
 * array. It is somewhat slower than the hashing methods in Falcon (an inner class), but
 * has slightly better quality (if there is a difference, Lightning would have better
 * quality, but there might not be any measurable difference except on 32-bit hashes of
 * 64-bit elements, where Lightning doesn't have a significant flaw that Falcon
 * unfortunately does have). A possible problem with Lightning is that it always uses
 * 64-bit math, so platforms like GWT that emulate 64-bit longs will be slower to run
 * Lightning, and it also uses {@link Long#rotateLeft(long, int)}, which is only truly
 * fast if the JRE is the right kind and the processor is not-ancient.
 * <br>
 * {@link Falcon} is very similar to Lightning in many ways, but has removed some
 * safeguards to help its speed, and you shouldn't use it to get 32-bit hashes of
 * 64-bit items like longs or doubles. Since its speed still isn't as good as Wisp, it's
 * mostly here for backwards compatibility. Falcon has some statistical flaws when
 * reducing the bit depth of items to produce a hash with less bits than a single item,
 * but it should be on-par with the deeply-statistically-flawed
 * {@link java.util.Arrays#hashCode(long[])} in speed (slower than Wisp, though). You
 * may find Falcon to be reasonable if you only use hash64(), which does not have the
 * same flaws as hash(). However, Falcon is nothing like a cryptographic hash.
 * <br>
 * {@link Storm} is much closer to a cryptographic hash, and allows 64 bits of
 * "alteration" (a.k.a. salt) to perturb any patterns that might be apparent, and though
 * it shouldn't be used for any actually-secure applications, some of the other nice
 * qualities of having a salt may make it a good hash for certain applications. Wisp
 * is the fastest hash here, followed by Falcon, Lightning and then Storm. If a million
 * 32-bit hashes take 9.4 ms with Wisp, then they should take about 12.7 ms with Falcon,
 * about 14.8 ms with Lightning and 18.9 ms with Storm.
 * @author Tommy Ettinger
 */
public class CrossHash {
    public static long hash64(final boolean[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0xC6BC279692B5CC83L : 0xAEF17502108EF2D9L));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final byte[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final short[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final char[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final int[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    /**
     * The hashAlt and hash64Alt methods for floating-point number arrays have better visual hashing properties than
     * hash and hash64, but are somewhat slower on desktop. They may be drastically faster than hash and hash64 on
     * GWT, however, because they don't use {@link Double#doubleToLongBits(double)} or its equivalent for Floats,
     * and those methods have much more complex implementations on GWT than on desktop Java.
     * @param data a float array to hash
     * @return a 64-bit hash code of data
     */
    public static long hash64Alt(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * -0x1.39b4dce80194cp9)));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToLongBits(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    /**
     * The hashAlt and hash64Alt methods for floating-point number arrays have better visual hashing properties than
     * hash and hash64, but are somewhat slower on desktop. They may be drastically faster than hash and hash64 on
     * GWT, however, because they don't use {@link Double#doubleToLongBits(double)} or its equivalent for Floats,
     * and those methods have much more complex implementations on GWT than on desktop Java.
     * @param data a double array to hash
     * @return a 64-bit hash code of data
     */
    public static long hash64Alt(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * -0x1.39b4dce80194cp9)));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final char[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final long[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence[]... data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        for (CharSequence datum : data) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final List<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data.get(i)));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Object[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1L : o.hashCode()));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Object data) {
        if (data == null)
            return 0L;
        long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(), result = 0x9E3779B97F4A7C94L + a;
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static int hash(final boolean[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * (data[i] ? 0x789ABCDE : 0x62E2AC0D));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }


    public static int hash(final byte[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final short[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final char[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }
    public static int hash(final int[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
    }

    public static int hash(final float[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * NumberTools.floatToIntBits(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    /**
     * The hashAlt and hash64Alt methods for floating-point number arrays have better visual hashing properties than
     * hash and hash64, but are somewhat slower on desktop. They may be drastically faster than hash and hash64 on
     * GWT, however, because they don't use {@link Double#doubleToLongBits(double)} or its equivalent for Floats,
     * and those methods have much more complex implementations on GWT than on desktop Java.
     * @param data a float array to hash
     * @return a 32-bit hash code of data
     */
    public static int hashAlt(final float[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * ((int) (-0xD0E8.9D2D311E289Fp-25f * (t = data[i]) + t * -0x1.39b4dce80194cp9f)));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToLongBits(data[i]));
        }
        return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
    }

    /**
     * The hashAlt and hash64Alt methods for floating-point number arrays have better visual hashing properties than
     * hash and hash64, but are somewhat slower on desktop. They may be drastically faster than hash and hash64 on
     * GWT, however, because they don't use {@link Double#doubleToLongBits(double)} or its equivalent for Floats,
     * and those methods have much more complex implementations on GWT than on desktop Java.
     * @param data a double array to hash
     * @return a 32-bit hash code of data
     */
    public static int hashAlt(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * -0x1.39b4dce80194cp9)));
        }
        return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
    }

    public static int hash(final CharSequence data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data.charAt(i));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final char[][] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final long[][] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final CharSequence[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final CharSequence[]... data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        for (CharSequence datum : data) {
            result += (a ^= 0x85157AF5 * hash(datum));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final List<? extends CharSequence> data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data.get(i)));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final Object[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * ((o = data[i]) == null ? -1 : o.hashCode()));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hash(final Object data) {
        if (data == null)
            return 0;
        int a = 0x632BE5AB ^ 0x85157AF5 * data.hashCode(), result = 0x9E3779B9 + a;
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }


    public static class Lightning {
        public static long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final byte[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final long[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final Iterable<String> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static int hash(final boolean[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final byte[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;

            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final long[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final Iterable<String> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }
    }
    /**
     * An alternative hashing function that is slightly faster than the current default in
     * CrossHash but has issues with certain methods (namely, {@link Falcon#hash(long[])} and
     * {@link Falcon#hash(double[])} disregard the upper 32 bits, at the least, of any items
     * in their input arrays, though the hash64 variants don't have this issue).
     * In most cases Lightning is a safe alternative, and is only slightly slower. If statistical
     * quality or "salting" of the hash is particularly important, you should use {@link Storm}
     * with a variety of salts/alterations.
     * <br>
     * Created by Tommy Ettinger on 1/16/2016.
     */
    public static class Falcon {

        public static long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= (data[i] ? 0xC6BC279692B5CC83L : 0x789ABCDEFEDCBA98L) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= NumberTools.floatToIntBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= NumberTools.doubleToLongBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result += (z ^= data.charAt(i) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final Object[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result += (z ^= (o == null ? 0 : o.hashCode()) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }


        public static int hash(final boolean[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= (data[i] ? 0x9E3779B9 : 0x789ABCDEL) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        /**
         * Be aware that this disregards the most-significant 32 bits of each long
         * in data. Its use is discouraged, and if you need 32-bit hashes of long
         * arrays, you should use {@link CrossHash#hash(long[])} instead.
         * @param data an array of long; be aware that this disregards the upper 32 bits
         * @return a 32-bit int hash of data
         * @see CrossHash#hash(long[]) You should prefer the non-inner-class of CrossHash for this
         */
        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= NumberTools.floatToIntBits(data[i]) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }
        /**
         * Be aware that this disregards the most-significant 32 bits of the long
         * representation of each double in data. Its use is discouraged, and if you
         * need 32-bit hashes of double arrays, you should use
         * {@link CrossHash#hash(double[])} instead.
         * @param data an array of double; be aware that this disregards a significant amount of data
         * @return a 32-bit int hash of data
         * @see CrossHash#hash(double[]) You should prefer the non-inner-class of CrossHash for this
         */
        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= NumberTools.doubleToLongBits(data[i]) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length(); i++) {
                result += (z ^= data.charAt(i) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                result += (z ^= data[i] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            int z = 0x632BE5AB, result = 1;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result += (z ^= (o == null ? 0 : o.hashCode()) * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

    }

    /**
     * An interface that can be used to move the logic for the hashCode() and equals() methods from a class' methods to
     * an implementation of IHasher that certain collections in SquidLib can use. Primarily useful when the key type is
     * an array, which normally doesn't work as expected in Java hash-based collections, but can if the right collection
     * and IHasher are used.
     */
    public interface IHasher extends Serializable {
        /**
         * If data is a type that this IHasher can specifically hash, this method should use that specific hash; in
         * other situations, it should simply delegate to calling {@link Object#hashCode()} on data. The body of an
         * implementation of this method can be very small; for an IHasher that is meant for byte arrays, the body could
         * be: {@code return (data instanceof byte[]) ? CrossHash.Lightning.hash((byte[]) data) : data.hashCode();}
         *
         * @param data the Object to hash; this method should take any type but often has special behavior for one type
         * @return a 32-bit int hash code of data
         */
        int hash(final Object data);

        /**
         * Not all types you might want to use an IHasher on meaningfully implement .equals(), such as array types; in
         * these situations the areEqual method helps quickly check for equality by potentially having special logic for
         * the type this is meant to check. The body of implementations for this method can be fairly small; for byte
         * arrays, it looks like: {@code return left == right
         * || ((left instanceof byte[] && right instanceof byte[])
         * ? Arrays.equals((byte[]) left, (byte[]) right)
         * : (left != null && left.equals(right)));} , but for multidimensional arrays you should use the
         * {@link #equalityHelper(Object[], Object[], IHasher)} method with an IHasher for the inner arrays that are 1D
         * or otherwise already-hash-able, as can be seen in the body of the implementation for 2D char arrays, where
         * charHasher is an existing IHasher that handles 1D arrays:
         * {@code return left == right
         * || ((left instanceof char[][] && right instanceof char[][])
         * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
         * : (left != null && left.equals(right)));}
         *
         * @param left  allowed to be null; most implementations will have special behavior for one type
         * @param right allowed to be null; most implementations will have special behavior for one type
         * @return true if left is equal to right (preferably by value, but reference equality may sometimes be needed)
         */
        boolean areEqual(final Object left, final Object right);
    }

    /**
     * Not a general-purpose method; meant to ease implementation of {@link IHasher#areEqual(Object, Object)}
     * methods when the type being compared is a multi-dimensional array (which normally requires the heavyweight method
     * {@link Arrays#deepEquals(Object[], Object[])} or doing more work yourself; this reduces the work needed to
     * implement fixed-depth equality). As mentioned in the docs for {@link IHasher#areEqual(Object, Object)}, example
     * code that hashes 2D char arrays can be done using an IHasher for 1D char arrays called charHasher:
     * {@code return left == right
     * || ((left instanceof char[][] && right instanceof char[][])
     * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
     * : (left == right) || (left != null && left.equals(right)));}
     *
     * @param left
     * @param right
     * @param inner
     * @return
     */
    public static boolean equalityHelper(Object[] left, Object[] right, IHasher inner) {
        if (left == right)
            return true;
        if ((left == null) ^ (right == null))
            return false;
        for (int i = 0; i < left.length && i < right.length; i++) {
            if (!inner.areEqual(left[i], right[i]))
                return false;
        }
        return true;
    }

    private static class BooleanHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        BooleanHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof boolean[] && right instanceof boolean[]) ? Arrays.equals((boolean[]) left, (boolean[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ByteHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof byte[] && right instanceof byte[])
                    ? Arrays.equals((byte[]) left, (byte[]) right)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ShortHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof short[] && right instanceof short[]) ? Arrays.equals((short[]) left, (short[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        CharHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof char[] && right instanceof char[]) ? Arrays.equals((char[]) left, (char[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        IntHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof int[] && right instanceof int[]) ? Arrays.equals((int[]) left, (int[]) right) : Objects.equals(left, right);
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        LongHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof long[] && right instanceof long[]) ? Arrays.equals((long[]) left, (long[]) right) : Objects.equals(left, right);
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        FloatHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof float[] && right instanceof float[]) ? Arrays.equals((float[]) left, (float[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        DoubleHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof double[] && right instanceof double[]) ? Arrays.equals((double[]) left, (double[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Char2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof char[][] && right instanceof char[][])
                    ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class Int2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Int2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[][]) ? CrossHash.hash((int[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof int[][] && right instanceof int[][])
                    ? equalityHelper((int[][]) left, (int[][]) right, intHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher int2DHasher = new Int2DHasher();

    private static class Long2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Long2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[][]) ? CrossHash.hash((long[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof long[][] && right instanceof long[][])
                    ? equalityHelper((long[][]) left, (long[][]) right, longHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher long2DHasher = new Long2DHasher();

    private static class StringHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof CharSequence[] && right instanceof CharSequence[]) ? equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher) : Objects.equals(left, right));
        }
    }

    /**
     * Though the name suggests this only hashes String arrays, it can actually hash any CharSequence array as well.
     */
    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class ObjectArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ObjectArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof Object[]) ? CrossHash.hash((Object[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof Object[] && right instanceof Object[]) && Arrays.equals((Object[]) left, (Object[]) right) || Objects.equals(left, right));
        }
    }
    public static final IHasher objectArrayHasher = new ObjectArrayHasher();

    private static class DefaultHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        DefaultHasher() {
        }

        @Override
        public int hash(final Object data) {
            return data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }

    public static final IHasher defaultHasher = new DefaultHasher();

    private static class IdentityHasher implements IHasher, Serializable
    {
        private static final long serialVersionUID = 3L;
        IdentityHasher() { }

        @Override
        public int hash(Object data) {
            return System.identityHashCode(data);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right;
        }
    }
    public static final IHasher identityHasher = new IdentityHasher();

    private static class GeneralHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        GeneralHasher() {
        }

        @Override
        public int hash(final Object data) {
            return CrossHash.hash(data);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            if(left == right) return true;
            Class l = left.getClass(), r = right.getClass();
            if(l == r)
            {
                if(l.isArray())
                {
                    if(left instanceof int[]) return Arrays.equals((int[]) left, (int[]) right);
                    else if(left instanceof long[]) return Arrays.equals((long[]) left, (long[]) right);
                    else if(left instanceof char[]) return Arrays.equals((char[]) left, (char[]) right);
                    else if(left instanceof double[]) return Arrays.equals((double[]) left, (double[]) right);
                    else if(left instanceof boolean[]) return Arrays.equals((boolean[]) left, (boolean[]) right);
                    else if(left instanceof byte[]) return Arrays.equals((byte[]) left, (byte[]) right);
                    else if(left instanceof float[]) return Arrays.equals((float[]) left, (float[]) right);
                    else if(left instanceof short[]) return Arrays.equals((short[]) left, (short[]) right);
                    else if(left instanceof char[][]) return equalityHelper((char[][]) left, (char[][]) right, charHasher);
                    else if(left instanceof int[][]) return equalityHelper((int[][]) left, (int[][]) right, intHasher);
                    else if(left instanceof long[][]) return equalityHelper((long[][]) left, (long[][]) right, longHasher);
                    else if(left instanceof CharSequence[]) return equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher);
                    else if(left instanceof Object[]) return Arrays.equals((Object[]) left, (Object[]) right);
                }
                return Objects.equals(left, right);
            }
            return false;
        }
    }

    /**
     * This IHasher is the one you should use if you aren't totally certain what types will go in an OrderedMap's keys
     * or an OrderedSet's items, since it can handle mixes of elements.
     */
    public static final IHasher generalHasher = new GeneralHasher();

    /**
     * A whole cluster of Lightning-like hash functions that sacrifice a small degree of speed, but can be
     * constructed with a salt value that helps obscure what hashing function is actually being used.
     * <br>
     * The salt field is not serialized, so it is important that the same salt will be given by the
     * program when the same hash results are wanted for some inputs.
     * <br>
     * A group of 24 static, final, pre-initialized Storm members are present in this class, each with the
     * name of a letter in the Greek alphabet (this uses the convention on Wikipedia,
     * https://en.wikipedia.org/wiki/Greek_alphabet#Letters , where lambda is spelled with a 'b'). The whole
     * group of 24 pre-initialized members are also present in a static array called {@code predefined}.
     * These can be useful when, for example, you want to get multiple hashes of a single array or String
     * as part of cuckoo hashing or similar techniques that need multiple hashes for the same inputs.
     */
    public static class Storm implements Serializable
    {
        private static final long serialVersionUID = 2352426757973945149L;

        private transient long $alt;

        public Storm()
        {
            this(0L);
        }

        public Storm(final CharSequence alteration)
        {
            this(CrossHash.hash64(alteration));
        }
        public Storm(final long alteration)
        {
            $alt = (alteration + 0x9E3779B97F4A7C15L);
            $alt = ($alt ^ ($alt >>> 30)) * 0xBF58476D1CE4E5B9L;
            $alt = ($alt ^ ($alt >>> 27)) * 0x94D049BB133111EBL;
            $alt ^= ($alt >>> 31);
            $alt += (191 - Long.bitCount($alt));
        }

        public static final Storm alpha = new Storm("alpha"), beta = new Storm("beta"), gamma = new Storm("gamma"),
                delta = new Storm("delta"), epsilon = new Storm("epsilon"), zeta = new Storm("zeta"),
                eta = new Storm("eta"), theta = new Storm("theta"), iota = new Storm("iota"),
                kappa = new Storm("kappa"), lambda = new Storm("lambda"), mu = new Storm("mu"),
                nu = new Storm("nu"), xi = new Storm("xi"), omicron = new Storm("omicron"), pi = new Storm("pi"),
                rho = new Storm("rho"), sigma = new Storm("sigma"), tau = new Storm("tau"),
                upsilon = new Storm("upsilon"), phi = new Storm("phi"), chi = new Storm("chi"), psi = new Storm("psi"),
                omega = new Storm("omega");
        public static final Storm[] predefined = new Storm[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
                kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega};


        public long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = start; i < end && i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length();
            for (int i = 0; i < len; i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
        }

        public long hash64(final char[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final long[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final Iterable<String> data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            Object o;
            for (int i = 0; i < len; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }
        public int hash(final boolean[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = start; i < end && i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length();
            for (int i = 0; i < len; i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final long[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final Iterable<String> data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            Object o;
            for (int i = 0; i < len; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }
    }
    /**
     * A whole cluster of Wisp-like hash functions that sacrifice a small degree of speed, but can be built with up
     * to 128 bits of salt values that help to obscure what hashing function is actually being used. This class is
     * similar to Storm in how you can construct one (using a CharSequence, one long to use to produce a salt, or in
     * this class two longs to use to produce a salt), but differs from Storm by being somewhat faster, having many
     * more possible salt "states" when using the constructors that take two longs or a CharSequence, and also by using
     * 32-bit math when only 32-bit inputs and output are used (relevant for GWT with its slower 64-bit math).
     * The salt values are mostly a pair of longs, but for the hash() functions that don't take a long array or double
     * array, a different salt value is used, a pair of ints.
     * <br>
     * The salt fields are not serialized, so it is important that the same salt will be given by the
     * program when the same hash results are wanted for some inputs.
     * <br>
     * A group of 24 static, final, pre-initialized Mist members are present in this class, each with the
     * name of a letter in the Greek alphabet (this uses the convention on Wikipedia,
     * https://en.wikipedia.org/wiki/Greek_alphabet#Letters , where lambda is spelled with a 'b'). The whole
     * group of 24 pre-initialized members are also present in a static array called {@code predefined}.
     * These can be useful when, for example, you want to get multiple hashes of a single array or String
     * as part of cuckoo hashing or similar techniques that need multiple hashes for the same inputs.
     */
    public static final class Mist implements Serializable {
        private static final long serialVersionUID = -1275284837479983271L;

        private transient long $l1, $l2;

        private transient int $i1, $i2;

        public Mist() {
            this(0x1234567876543210L, 0xEDCBA98789ABCDEFL);
        }

        public Mist(final CharSequence alteration) {
            this(CrossHash.hash64(alteration), Falcon.hash64(alteration));
        }
        private static int permute(final long state)
        {
            int s = (int)state ^ 0xD0E89D2D;
            s = (s >>> 19 | s << 13);
            s ^= state >>> (5 + (state >>> 59));
            return ((s *= 277803737) >>> 22) ^ s;
        }

        @SuppressWarnings("NumericOverflow")
        public Mist(final long alteration) {
            $i1 = permute(alteration);
            $l1 = alteration + $i1;
            $l1 = ($l1 ^ ($l1 >>> 30)) * 0xBF58476D1CE4E5B9L;
            $l1 = ($l1 ^ ($l1 >>> 27)) * 0x94D049BB133111EBL;
            $l1 ^= $l1 >>> 31;

            $i2 = permute($l1 + 0x9E3779B97F4A7C15L);
            $l2 = alteration + 6 * 0x9E3779B97F4A7C15L;
            $l2 = ($l2 ^ ($l2 >>> 30)) * 0xBF58476D1CE4E5B9L;
            $l2 = ($l2 ^ ($l2 >>> 27)) * 0x94D049BB133111EBL;
            $l2 ^= $l2 >>> 31;
        }

        @SuppressWarnings("NumericOverflow")
        public Mist(final long alteration1, long alteration2) {
            $i1 = permute(alteration1);
            $l1 = alteration1 + $i1;
            $i2 = permute(alteration2 + $i1);
            $l2 = alteration2 + $i2;
        }

        /**
         * Alters all of the salt values in a pseudo-random way based on the previous salt value.
         * This will effectively make this Mist object a different, incompatible hashing functor.
         * Meant for use in Cuckoo Hashing, which can need the hash function to be updated or changed.
         * An alternative is to select a different Mist object from {@link #predefined}, or to simply
         * construct a new Mist with a different parameter or set of parameters.
         */
        @SuppressWarnings("NumericOverflow")
        public void randomize()
        {
            $i1 = permute($l2 + 3 * 0x9E3779B97F4A7C15L);
            $l1 = $l2 + $i1;
            $l1 = ($l1 ^ ($l1 >>> 30)) * 0xBF58476D1CE4E5B9L;
            $l1 = ($l1 ^ ($l1 >>> 27)) * 0x94D049BB133111EBL;
            $l1 ^= $l1 >>> 31;

            $i2 = permute($l1 + 5 * 0x9E3779B97F4A7C15L);
            $l2 = $l1 + 6 * 0x9E3779B97F4A7C15L;
            $l2 = ($l2 ^ ($l2 >>> 30)) * 0xBF58476D1CE4E5B9L;
            $l2 = ($l2 ^ ($l2 >>> 27)) * 0x94D049BB133111EBL;
            $l2 ^= $l2 >>> 31;

        }

        public static final Mist alpha = new Mist("alpha"), beta = new Mist("beta"), gamma = new Mist("gamma"),
                delta = new Mist("delta"), epsilon = new Mist("epsilon"), zeta = new Mist("zeta"),
                eta = new Mist("eta"), theta = new Mist("theta"), iota = new Mist("iota"),
                kappa = new Mist("kappa"), lambda = new Mist("lambda"), mu = new Mist("mu"),
                nu = new Mist("nu"), xi = new Mist("xi"), omicron = new Mist("omicron"), pi = new Mist("pi"),
                rho = new Mist("rho"), sigma = new Mist("sigma"), tau = new Mist("tau"),
                upsilon = new Mist("upsilon"), phi = new Mist("phi"), chi = new Mist("chi"), psi = new Mist("psi"),
                omega = new Mist("omega"),
                alpha_ = new Mist("ALPHA"), beta_ = new Mist("BETA"), gamma_ = new Mist("GAMMA"),
                delta_ = new Mist("DELTA"), epsilon_ = new Mist("EPSILON"), zeta_ = new Mist("ZETA"),
                eta_ = new Mist("ETA"), theta_ = new Mist("THETA"), iota_ = new Mist("IOTA"),
                kappa_ = new Mist("KAPPA"), lambda_ = new Mist("LAMBDA"), mu_ = new Mist("MU"),
                nu_ = new Mist("NU"), xi_ = new Mist("XI"), omicron_ = new Mist("OMICRON"), pi_ = new Mist("PI"),
                rho_ = new Mist("RHO"), sigma_ = new Mist("SIGMA"), tau_ = new Mist("TAU"),
                upsilon_ = new Mist("UPSILON"), phi_ = new Mist("PHI"), chi_ = new Mist("CHI"), psi_ = new Mist("PSI"),
                omega_ = new Mist("OMEGA");
        public static final Mist[] predefined = new Mist[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
                kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
                alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
                kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_};

        public long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0x9E3779B97F4A7C15L : 0x789ABCDEFEDCBA98L)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }


        public long hash64(final byte[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final short[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final int[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final long[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }


        public long hash64(final float[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToLongBits(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i += step) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            final int len = data.length();
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final long[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (CharSequence datum : data) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final Object[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1 : o.hashCode())) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public int hash(final boolean[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * (data[i] ? 0x789ABCDE : 0x62E2AC0D)) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final byte[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final short[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final char[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final int[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final long[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)((result = (result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
        }


        public int hash(final float[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * NumberTools.floatToIntBits(data[i])) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToLongBits(data[i])) ^ $l2 * a + $l1;
            }
            return (int)((result = (result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
        }

        public int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = start; i < end && i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = start; i < end && i < len; i += step) {
                result += (a ^= 0x85157AF5 * data[i]) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final CharSequence data) {
            if (data == null)
                return 0;
            final int len = data.length();
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data.charAt(i)) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final char[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash(data[i])) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final long[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash(data[i])) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash(data[i])) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final Iterable<? extends CharSequence> data) {

            if (data == null)
                return 0;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (CharSequence datum : data) {
                result += (a ^= 0x85157AF5 * hash(datum)) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash(data[i])) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final Object[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * ((o = data[i]) == null ? -1 : o.hashCode())) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }
        public int hash(final List<? extends CharSequence> data) {
            if (data == null)
                return 0;
            final int len = data.size();
            int result = 0x9E3779B9 + $i2, a = 0x632BE5AB;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash(data.get(i))) ^ $i2 * a + $i1;
            }
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

        public int hash(final Object data) {
            if (data == null)
                return 0;
            int a = 0x632BE5AB ^ 0x85157AF5 * data.hashCode(), result = 0x9E3779B9 + $i2 + (a ^ $i2 * a + $i1);
            return result * (a * $i1 | 1) ^ (result >>> 11 | result << 21);
        }

    }

}
