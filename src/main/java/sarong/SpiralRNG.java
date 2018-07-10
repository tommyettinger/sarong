package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * A 64-bit generator based on combining a linear feedback-shift register (LFSR) and a Weyl Sequence. Using the
 * LFSR as the stream, this has (2 to the 64) minus 1 possible streams instead of 1 stream for something like LightRNG.
 * Its period is (2 to the 128) minus (2 to the 64), with the stream constantly changing along with the state.
 * <br>
 * Created by Tommy Ettinger on 6/16/2018.
 */
public final class SpiralRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Can be any long value except 0.
     */
    private long stream;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public SpiralRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public SpiralRNG(long seed)
    {
        this(seed, seed == -1 ? -2 : ~seed);
    }

    /**
     *
     * @param seed any long
     * @param stream any long except 0 (0 will be treated as 1)
     */
    public SpiralRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream == 0 ? 1 : stream;
    }

    /**
     * Get the current internal state of this Spiral as a long.
     * This is not the full state; you also need {@link #getStream()}.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return state;
    }
    /**
     * Set the current internal state of this SpiralRNG with a long.
     * @param state any 64-bit long
     */
    public void setState(long state) {
        this.state = state;
    }
    /**
     * Get the current internal stream of this SpiralRNG as a long.
     * This is not the full state; you also need {@link #getState()}.
     *
     * @return the current internal stream of this object.
     */
    public long getStream() {
        return stream;
    }
    /**
     * Set the current internal stream of this SpiralRNG with a long.
     * @param stream any 64-bit long
     */
    public void setStream(long stream) {
        this.stream = stream == 0 ? 1 : stream;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
        final long x = (state += 0x9E3779B97F4A7C15L), y = stream,
                z = ((x ^ x >>> 27) + (y ^ y >>> 30)) * 0xAEF17502108EF2D9L;
        stream = y >>> 1 ^ (-(y & 1L) & 0xD800000000000000L);
        return (int)((z ^ z >>> 25) + y >>> (64 - bits));
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        final long x = (state += 0x9E3779B97F4A7C15L), y = stream,
                z = ((x ^ x >>> 27) + (y ^ y >>> 30)) * 0xAEF17502108EF2D9L;
        stream = y >>> 1 ^ (-(y & 1L) & 0xD800000000000000L);
        return (z ^ z >>> 25) + y;
    }
    
    public final long nextLongOld() {
        final long x = (state += 0x9E3779B97F4A7C15L), y = stream,
                z = ((x ^ x >>> 27) + (y ^ y >>> 30)) * 0xAEF17502108EF2D9L;
        stream = y >>> 1 ^ (-(y & 1L) & 0xD800000000000000L);
        return (z ^ z >>> 25) + y;
    }

    public final long nextLongAlt(){ 
        long z = (state += 0x632BE59BD9B4E019L);// (stateA += UINT64_C(0x9E3779B97F4A7C15));// (stateA = stateA * UINT64_C(0x41C64E6D) + UINT64_C(1));
        z = (z ^ z >>> 30) * ((stream = stream >>> 1 ^ (-(stream & 1L) & 0xD800000000000000L)) | 1L);
        return z ^ z >>> 25;
    }
    
    public final long nextLongNew(){
        final long y = -(stream & 1L);
        final long z = (state = state * 0x41C64E6DL + 1L) + (stream = stream >>> 1 ^ (y & 0xD800000000000000L));
        return (z ^ z >>> 27) + (y & 0x9E3779B97F4A7C15L);
    }

    public final long nextLongAgain()
    {
        long y = stream;
        y ^= y >>> 31;
        final long z = (state = state * 0x41C64E6DL + 1L) + (y ^= y << 25);
        return z + (stream = y ^ y >>> 37);
    }

    public final long nextLongAgain2()
    {
        stream ^= stream >>> 31;
        final long z = (state = state * 0x41C64E6DL + 1L) + (stream ^= stream << 25);
        return z + (stream ^= stream >>> 37);
    }

    public final long nextLongAgain3()
    {
        final long y = -(stream & 1L);
        final long z = (state = state * 0x41C64E6DL + 1L + (stream = stream >>> 1 ^ (y & 0xD800000000000000L)));
        return (z ^ z >>> 28) + (y ^ 0x9E3779B97F4A7C15L);
    }

//    public final long nextLongAgain4()
//    {
//        final long y = -(stream & 1L);
//        final long z = (state += 0x9E3779B97F4A7C15L + (stream = stream >>> 1 ^ (y & 0xD800000000000000L))) * 0x41C64E6DL;
//        return (z ^ z >>> 28) + (y ^ 0x6C8E9CF570932BD5L);
//    }

    public final long nextLongAgain4()
    {
        final long s = (state += 0x6C8E9CF570932BD5L);
        final long z = (s ^ (s >>> 25)) * 
                //((stream += 0x9E3779B97F4A7C15L << 1L + ((t | (-t)) >> 31)) | 0xA529L);
                ((stream += (stream == -0x9E3779B97F4A7C15L) ? 0x3C6EF372FE94F82AL : 0x9E3779B97F4A7C15L) | 0xA529L);
        return (z ^ z >>> 22);
    }

    public final long nextLongAgain5()
    {
//        final long s = (state += 0x6C8E9CF570932BD5L);
//        final long z = (s ^ (s >>> 25)) * ((stream += 0x3C6EF372FE94F82AL >>> ((s | (-s)) >>> 31)) | 0xA529L);
//        return (z ^ z >>> 22);
        stream += 0x9E3779B97F4A7C15L;
        if(stream == 0L)
            stream = 0x9E3779B97F4A7C15L;
        final long y = stream ^ stream >>> 28;
        //y ^= y >>> 28;
        final long z = (state = (state ^ 0x9E3779B97F4A7C15L) * 0x41C64E6BL + y);
        return (z ^ z >>> 28) + y;

    }
    public final long nextLongAgain6()
    {
        stream ^= stream >>> 21;
        final long z = (state = (state ^ 0x9E3779B97F4A7C15L) * 0x41C64E6BL + (stream ^= stream << 9));
        return (z ^ z >>> 28) + (stream ^= stream >>> 29);
    }
    public final long nextLongAgain7()
    {
        final long s = (state += 0x6C8E9CF570932BD5L);
        final long z = (s ^ (s >>> 25)) * ((stream += 0x9E3779B97F4A7C15L) | 0xA529L);
        return (z ^ z >>> 22);
    }
    
    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public SpiralRNG copy() {
        return new SpiralRNG(state, stream);
    }
    @Override
    public String toString() {
        return "SpiralRNG on stream 0x" + StringKit.hex(stream) + "L with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpiralRNG spiralRNG = (SpiralRNG) o;

        return state == spiralRNG.state && stream == spiralRNG.stream;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32) + 31 * (stream ^ stream >>> 32));
    }
    public static void main(String[] args)
    {
        /*
        cd target/classes
        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/SpiralRNG > spiral_asm.txt
         */
        byte state = 0, stream = 1;
        int y = 0;
        char[] counts = new char[256];
        for (int i = 0; i < 0xFF00; i++) {
            /*
  					uint64_t y = stateB ^ stateB >> 31;
					const uint64_t z = (stateA = (stateA * UINT64_C(0x41C64E6D)) + UINT64_C(1)) + (y ^= y << 25);
					return (z ^ z >> 27u) + (stateB = y ^ y >> 37);
             */
//            y = -(stream & 1);
//            final byte z = (byte) ((state = (byte) (((state & 0xFF) * 0x65) + 1)) + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
//            counts[(z ^ z >>> 27) + (y & 0x95) & 0xFF]++;

            stream ^= (stream & 0xFF) >> 3;
            final byte z = (state = (byte) (((state & 0xFF) ^ 0x65) * 0x93 + (stream ^= stream << 5)));
            counts[z + (stream ^= (stream & 0xFF) >> 4) & 0xFF]++;
//            y = -(stream & 1);
//            final byte z = (state = (byte) (((state & 0xFF) * 0x65) + 0x7F + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8)))));
////            final byte z = (state += 1 + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
//            counts[(z ^ (z&0xFF) >>> 5) + (y >>> 24 ^ 0x95) & 0xFF]++;
            
            //uint64_t z = (stateA = (stateA ^ UINT64_C(0x6C8E9CF570932BD5)) * UINT64_C(0x5DA942042E4DD58B)) + (stateB = stateB >> 1u ^ (y & UINT64_C(0xD800000000000000)));
            //final byte z = (byte) ((state = (byte) (((state & 0xFF) ^ 0x65) * 0x5B)) + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
            //final byte z = (byte) (((state ^ state >>> 27) + (stream ^ stream >>> 30)) * 0xD9);
        }
        int b = -1;
        for (int i = 0; i < 32; i++) {
            System.out.printf("%03d: %04X  %03d: %04X  %03d: %04X  %03d: %04X  %03d: %04X  %03d: %04X  %03d: %04X  %03d: %04X\n",
                    ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF,
                    ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF, ++b, counts[b] & 0xFFFF);
        }
    }
}
