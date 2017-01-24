package sarong.util;

/**
 * Various utility functions for making toString implementations easier.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {
    private static final String mask64 = "0000000000000000000000000000000000000000000000000000000000000000",
            mask32 = "00000000000000000000000000000000",
            mask16 = "0000000000000000",
            mask8 = "00000000";

    public static String hex(long number) {
        String h = Long.toHexString(number);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String hex(int number) {
        String h = Integer.toHexString(number);
        return mask8.substring(0, 8 - h.length()) + h;
    }

    public static String hex(short number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask8.substring(4, 8 - h.length()) + h;
    }

    public static String hex(char number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask8.substring(4, 8 - h.length()) + h;
    }

    public static String hex(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(6, 8 - h.length()) + h;
    }

    public static String hex(long[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 4);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(int[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 3);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(short[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(char[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(byte[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 1);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String bin(long number) {
        String h = Long.toBinaryString(number);
        return mask64.substring(0, 64 - h.length()) + h;
    }

    public static String bin(int number) {
        String h = Integer.toBinaryString(number);
        return mask32.substring(0, 32 - h.length()) + h;
    }

    public static String bin(short number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(char number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(0, 8 - h.length()) + h;
    }
    private static final int[] hexCodes = new int[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0,
                    0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 10, 11, 12, 13, 14, 15};

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) and returns the long they represent,
     * reading at most 16 characters and returning the result if valid or 0 otherwise. This does not represent negative
     * numbers except as they are printed by such methods as String.format given a %x in the formatting string, or this
     * class' {@link #hex(long)} method; that is, if the first char of a 16-char CharSequence is a hex digit 8 or
     * higher, then the whole number represents a negative number, using two's complement and so on. This means
     * "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only hex digits (no 0x at the start)
     * @return the long that cs represents
     */
    public static long longFromHex(CharSequence cs) {
        return longFromHex(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) and returns the long they represent,
     * reading at most 16 characters and returning the result if valid or 0 otherwise. This does not represent negative
     * numbers except as they are printed by such methods as String.format given a %x in the formatting string, or this
     * class' {@link #hex(long)} method; that is, if the first char of a 16-char CharSequence is a hex digit 8 or
     * higher, then the whole number represents a negative number, using two's complement and so on. This means
     * "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only hex digits (no 0x at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c > 102)
            return 0;
        long data = hexCodes[c];
        for (int i = start+1; i < end && i < 16; i++) {
            if((c = cs.charAt(i)) > 102)
                return 0;
            data <<= 4;
            data |= hexCodes[c];
        }
        return data;
    }
    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs)
    {
        return longFromBin(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 64 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c < '0' || c > '1')
            return 0;
        long data = hexCodes[c];
        for (int i = start+1; i < end && i < 64; i++) {
            if((c = cs.charAt(i)) < '0' || c > '1')
                return 0;
            data <<= 1;
            data |= c - '0';
        }
        return data;
    }

    public static String hexHash(boolean... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(byte... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(short... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(char... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(int... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(long... array) {
        return hex(CrossHash.hash64(array));
    }
}
