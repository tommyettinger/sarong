package sarong.util;

import java.io.Serializable;

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
        return result * (a << 1 | 1);
    }

    public static long hash64(final byte[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final short[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final char[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final int[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * Float.floatToIntBits(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * Double.doubleToLongBits(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final CharSequence data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = Math.min(end, data.length);
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final char[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final long[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final CharSequence[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static long hash64(final CharSequence[]... data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a << 1 | 1);
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
        return result * (a << 1 | 1);
    }

    public static int hash(final boolean[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * (data[i] ? 0x789ABCDE : 0x62E2AC0D));
        }
        return result * (a << 1 | 1);
    }


    public static int hash(final byte[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final short[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final char[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a << 1 | 1);
    }
    public static int hash(final int[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)((result *=  (a << 1 | 1)) ^ (result >>> 32));
    }

    public static int hash(final float[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * Float.floatToIntBits(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * Double.doubleToLongBits(data[i]));
        }
        return (int)((result *=  (a << 1 | 1)) ^ (result >>> 32));
    }

    public static int hash(final CharSequence data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * data.charAt(i));
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = Math.min(end, data.length);
        for (int i = start; i < len; i++) {
            result += (a ^= 0x85157AF5 * data[i]);
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final char[][] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final long[][] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final CharSequence[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a << 1 | 1);
    }

    public static int hash(final CharSequence[]... data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * hash(data[i]));
        }
        return result * (a << 1 | 1);
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
        return result * (a << 1 | 1);
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
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
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
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
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
                result += (z ^= Float.floatToIntBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result ^ ((z ^ result) >>> 16) * 0x9E3779B97F4A7C15L;
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= Double.doubleToLongBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
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
                result += (z ^= Float.floatToIntBits(data[i]) * 0x85157AF5) + 0x62E2AC0D;
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
                result += (z ^= Double.doubleToLongBits(data[i]) * 0x85157AF5) + 0x62E2AC0D;
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

    public interface IHasher extends Serializable {
        int hash(final Object data);
    }

    private static class BooleanHasher implements IHasher {
        BooleanHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher {
        ByteHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher {
        ShortHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher {
        CharHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher {
        IntHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher {
        LongHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher {
        FloatHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher {
        DoubleHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher {
        Char2DHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class StringHasher implements IHasher {
        StringHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher {
        StringArrayHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }
    }

    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class ObjectHasher implements IHasher {
        ObjectHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof Object[]) ? CrossHash.hash((Object[]) data) : data.hashCode();
        }
    }

    public static final IHasher objectHasher = new ObjectHasher();

    private static class DefaultHasher implements IHasher {
        DefaultHasher() {
        }

        public int hash(final Object data) {
            return data.hashCode();
        }
    }
    public static final IHasher defaultHasher = new DefaultHasher();
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
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
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
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L, len = data.length; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
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
}
