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
    public static long hash64(boolean[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(byte[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(short[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(char[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(int[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(long[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(float[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(double[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(char[] data, int start, int end) {
        if (data == null || start >= end)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = start; i < end && i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(CharSequence data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
    }

    public static long hash64(char[][] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(String[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(Iterable<String> data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (String datum : data) {
            result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static long hash64(String[]... data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
    }

    public static int hash(boolean[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(byte[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(short[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(char[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(int[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;

        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(long[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(float[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(double[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(char[] data, int start, int end) {
        if (data == null || start >= end)
            return 0;

        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = start; i < end && i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(CharSequence data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(char[][] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(String[] data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(Iterable<String> data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (String datum : data) {
            result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public static int hash(String[]... data) {
        if (data == null)
            return 0;
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
    }

    public interface IHasher extends Serializable {
        int hash(Object data);
    }

    private static class BooleanHasher implements IHasher {
        protected BooleanHasher() {
        }

        public int hash(Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher {
        protected ByteHasher() {
        }

        public int hash(Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher {
        protected ShortHasher() {
        }

        public int hash(Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher {
        protected CharHasher() {
        }

        public int hash(Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher {
        protected IntHasher() {
        }

        public int hash(Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher {
        protected LongHasher() {
        }

        public int hash(Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher {
        protected FloatHasher() {
        }

        public int hash(Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher {
        protected DoubleHasher() {
        }

        public int hash(Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher {
        protected Char2DHasher() {
        }

        public int hash(Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class StringHasher implements IHasher {
        protected StringHasher() {
        }

        public int hash(Object data) {
            return (data instanceof String) ? CrossHash.hash((String) data) : data.hashCode();
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher {
        protected StringArrayHasher() {
        }

        public int hash(Object data) {
            return (data instanceof String[]) ? CrossHash.hash((String[]) data) : data.hashCode();
        }
    }

    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class DefaultHasher implements IHasher {
        protected DefaultHasher() {
        }

        public int hash(Object data) {
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
     */
    public static class Storm implements Serializable
    {
        private static final long serialVersionUID = 2352426757973945149L;

        private transient long $alt;

        public Storm()
        {
            this(0L);
        }
        public Storm(long alteration)
        {
            $alt = (alteration + 0x9E3779B97F4A7C15L);
            $alt = ($alt ^ ($alt >>> 30)) * 0xBF58476D1CE4E5B9L;
            $alt = ($alt ^ ($alt >>> 27)) * 0x94D049BB133111EBL;
            $alt ^= ($alt >>> 31);
            $alt += (191 - Long.bitCount($alt));
        }

        public long hash64(boolean[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(byte[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(short[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(char[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(int[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(long[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(float[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(double[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(CharSequence data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length(); i++) {
                result ^= (~(result * spice) * bacon + (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(char[][] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(String[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(Iterable<String> data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (String datum : data) {
                result ^= (~(result * spice) * bacon + (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public long hash64(String[]... data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon;
        }

        public int hash(boolean[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(byte[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(short[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(char[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(int[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(long[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(float[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(double[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(CharSequence data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length(); i++) {
                result ^= (~(result * spice) * bacon + (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(char[][] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(String[] data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(Iterable<String> data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (String datum : data) {
                result ^= (~(result * spice) * bacon + (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

        public int hash(String[]... data) {
            if (data == null)
                return 0;
            long bacon = $alt, z = Long.rotateLeft(0x9E3779B97F4A7C15L, (int)(bacon & 63)), result = 0xA5A5A5A5A5A5A5A5L, spice = bacon << 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (~(result * spice) * bacon + (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL));
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58)) + bacon) ^ (result >>> 32));
        }

    }

}
