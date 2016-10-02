package sarong.util;

import java.io.Serializable;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * These use a relatively low number of simple steps for most of the hashing process,
 * but due to a more-elaborate finalization step it should have virtually no
 * correlation between input and output, with many bits potentially avalanching on
 * any change in the input array.
 * <br>
 * Created by Tommy Ettinger on 1/16/2016.
 * @author Tommy Ettinger
 */
public class CrossHash {
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
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
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

    public static int hash(final char[] data, int start, int end) {
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
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58))) ^ (result >>> 32));
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

    public interface IHasher extends Serializable {
        int hash(final Object data);
    }

    private static class BooleanHasher implements IHasher {
        protected BooleanHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher {
        protected ByteHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher {
        protected ShortHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher {
        protected CharHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher {
        protected IntHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher {
        protected LongHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher {
        protected FloatHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher {
        protected DoubleHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher {
        protected Char2DHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class StringHasher implements IHasher {
        protected StringHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher {
        protected StringArrayHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }
    }

    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class ObjectHasher implements IHasher {
        protected ObjectHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof Object[]) ? CrossHash.hash((Object[]) data) : data.hashCode();
        }
    }

    public static final IHasher objectHasher = new ObjectHasher();

    private static class DefaultHasher implements IHasher {
        protected DefaultHasher() {
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
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
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }
    }

}
