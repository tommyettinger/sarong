package sarong;

import sarong.util.StringKit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Work-in-progress.
 * <br>
 * Created by Tommy Ettinger on 9/24/2017.
 */
public class Lunge32RNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Can be any int value.
     */
    public int state;

    /**
     * Can be any int value except 0.
     */
    public int stream;
    /**
     * Creates a new generator seeded using Math.random.
     */
    public Lunge32RNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public Lunge32RNG(final int seed) {
        state = seed;
        stream = (seed ^ 0x9E377B5) * 0x31337CAB;
        if(stream == 0) stream = 1;
    }

    public Lunge32RNG(final int seed, final int stream) {
        state = seed;
        if(stream == 0) this.stream = 1;
        else this.stream = stream;
        
    }

    public Lunge32RNG(final long seed) {
        state = (int) (seed);
        stream = (int)(seed >>> 32);
        if(stream == 0) stream = 1;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You may want to avoid passing 0 for compatibility, though this implementation can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = (int) state;
    }

    public final int nextInt()
    {         
        stream ^= stream >>> 6;
        final int s = (state += 0x6C8E9CF5);
        final int z = (s ^ (s >>> 13)) * ((stream ^= stream << 1) | 1);
        return (z ^ z >>> 11) + (stream ^= stream >>> 11);

////        return (state += (state >> 13) + 0x5F356495) * 0x2C9277B5;
//        int z = (state += 0x7F4A7C15);
//        z = (z ^ z >>> 14) * (0x2C9277B5 + (z * 0x632BE5A6));
//        return (z ^ z >>> 14) * 0x5F356495;
////        int z = (state += 0x7F4A7C15);
////        z = (z ^ z >>> 14) * (z ^ z + 0x2C9277B5);
////        return (z ^ z >>> 13);
    }
    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(int bits) {
//        return (state += state ^ ((state >>> (state & 7) + 7) + 0x2C9277B5) * 0x5F356495) >>> (32 - bits);
        //return (state ^ (state += ((state >>> 13) + 0x5F356495) * 0x2C9277B5)) >>> (32 - bits);
        stream ^= stream >>> 6;
        final int s = (state += 0x6C8E9CF5);
        final int z = (s ^ (s >>> 13)) * ((stream ^= stream << 1) | 1);
        return (z ^ z >>> 11) + (stream ^= stream >>> 11) >>> (32 - bits);
//        return (z ^ z >>> 14) * 0x5F356495 >>> (32 - bits);
//        int z = (state += 0x7F4A7C15);
//        z = (z ^ z >>> 14) * (z ^ z + 0x2C9277B5);
//        return (z ^ z >>> 13) >>> (32 - bits);
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
//        return (state += (state >> 13) + 0x5F356495) * 0x2C9277B500000000L ^
//                (state += (state >> 13) + 0x5F356495) * 0x2C9277B5;
//        int x = state + 0x7F4A7C15, y = (state += 0xFE94F82A);
//        x = (x ^ x >>> 14) * (x ^ x + 0x2C9277B5);
//        y = (y ^ y >>> 14) * (y ^ y + 0x2C9277B5);
//        return (long) (x ^ x >>> 13) << 32 ^ (y ^ y >>> 13);

//        int x = state + 0x7F4A7C15, y = (state += 0xFE94F82A);
//        //0x5F356495
//        x = (x ^ x >>> 14) * (0x2C9277B5 + (x * 0x632BE5A6));
//        y = (y ^ y >>> 14) * (0x2C9277B5 + (y * 0x632BE5A6));
//        return (x ^ x >>> 14) * 0x5F35649500000000L ^ (y ^ y >>> 14) * 0x5F356495;
        stream ^= stream >>> 6;
        final int t = (state += 0xD91D39EA), s = (t - 0x6C8E9CF5);
        final int z = (s ^ (s >>> 13)) * ((stream ^= stream << 1) | 1);
        final long hi = (z ^ z >>> 11) + (stream ^= stream >>> 11);
        stream ^= stream >>> 6;
        final int lo = (t ^ (t >>> 13)) * ((stream ^= stream << 1) | 1);
        return ((lo ^ lo >>> 11) + (stream ^= stream >>> 11)) ^ hi << 32;
        // * 0x27BB2EE687B0B0FDL;
        //return ((state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> 28));

        //return (state = state * 0x59A2B8F555F5828FL % 0x7FFFFFFFFFFFFFE7L) ^ state << 2;
        //return (state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL);
        //return (state ^ (state += 0x2545F4914F6CDD1DL)) * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL;
        //return (state * 0x5851F42D4C957F2DL) + ((state += 0x14057B7EF767814FL) >> 28);
        //return (((state += 0x14057B7EF767814FL) >>> 28) * 0x5851F42D4C957F2DL + (state >>> 1));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Lunge32RNG copy() {
        return new Lunge32RNG(state, stream);
    }
    @Override
    public String toString() {
        return "Lunge32RNG with state 0x" + StringKit.hex(state) + " and stream 0x" + StringKit.hex(stream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lunge32RNG lungeRNG = (Lunge32RNG) o;

        return state == lungeRNG.state && stream == lungeRNG.stream;
    }

    @Override
    public int hashCode() {
        return state;
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x7F4A7C15} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static int determine(int state)
    {
        state = ((state *= 0x7F4A7C15) ^ state >>> 14) * (0x2C9277B5 + (state * 0x632BE5A6));
        return state ^ state >>> 12;
    }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(int state, final int bound)
    {
        state = ((state *= 0x7F4A7C15) ^ state >>> 14) * (0x2C9277B5 + (state * 0x632BE5A6));
        return (int)((bound * ((state ^ state >>> 14) * 0x5F356495 & 0x7FFFFFFFL)) >> 31);
    }
    public static void main0(String[] args)
    {
        byte state = 2, stream = 0;
        char[] counts = new char[256];
        for (int i = 0x0; i < 0x8000; i++) {
            /*
  					uint64_t y = stateB ^ stateB >> 31;
					const uint64_t z = (stateA = (stateA * UINT64_C(0x41C64E6D)) + UINT64_C(1)) + (y ^= y << 25);
					return (z ^ z >> 27u) + (stateB = y ^ y >> 37);
             */
//            y = -(stream & 1);
//            final byte z = (byte) ((state = (byte) (((state & 0xFF) * 0x65) + 1)) + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
//            counts[(z ^ z >>> 27) + (y & 0x95) & 0xFF]++;

//            Uint64 z = (stateB += INC2), s = (state += INCR);
//            z *= (s ^ z >> SHIFT_A);
//            return (z ^ (z + s >> SHIFT_B));

//            state += 0x95;
//            if(state == 0) 
//                stream += 0xD4;
//            else
//                stream += 0x6A;
//            final byte z = (byte)(((state ^ (stream&0xFF) >> 3)) * stream);
//            byte z;
//            if(stream == 0) // -0x91
//            {
//                z = (state *= 0x3D);
//                stream -= 107;
//            }
//            else 
//                z = (state = (byte)(state * 0x3D + (stream += -107)));
//            final byte z = (byte) (((stream += 1|(state += 0x95))|1) * (state ^ (state&0xFF) >> 3));
            if(stream != 0)
                state += 0xA7;
            final byte z = (byte) ((state ^ (state&0xFF) >> 3) * 0x3D + ((stream = (byte)(stream + 0x65 ^ 0x96))));
            counts[(z^(z&0xFF)>>2) & 0xFF]++;

//            stream ^= (stream & 0xFF) >> 3;
//            state += 0x95;
//            final byte z = (byte) ((state ^ (state&0xFF) >> 3) * ((stream ^= stream << 5) | 1));
//            counts[(z ^ (z&0xFF)>>2) + (stream ^= (stream & 0xFF) >> 4) & 0xFF]++;

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
    public static void main(String[] args)
    {
        /*
        cd target/classes
        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/SpiralRNG > spiral_asm.txt
         */
        int state;
        byte stateA = -120, stateB = 2, stateC = 2;
        char[] counts = new char[65536];
        for (int i = 0; i < 0xFFFF00; i++) {//0x7FFF80
            /*
  					uint64_t y = stateB ^ stateB >> 31;
					const uint64_t z = (stateA = (stateA * UINT64_C(0x41C64E6D)) + UINT64_C(1)) + (y ^= y << 25);
					return (z ^ z >> 27u) + (stateB = y ^ y >> 37);
             */
//            y = -(stream & 1);
//            final byte z = (byte) ((state = (byte) (((state & 0xFF) * 0x65) + 1)) + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
//            counts[(z ^ z >>> 27) + (y & 0x95) & 0xFF]++;
            //state += 0x9E75;
//            if(state == 0)
//                stream += 0x6B;
            //final short z = (short) ((state ^ (state&0xFFFF) >> 6) * ((stream += 0x649A)));
            //state = (short)(state + 0x6665 ^ 0x9376);
            byte s0 = stateA;
            byte s1 = (byte)((stateB ^ s0));
            byte s2 = (stateC = (byte)((stateC ^ 0x9D) * 3));
//            byte s2 = (byte)((stateC += 0x9D));
//            byte result = s1;
//            s1 ^= s0;
            stateA = (byte)((s0 << 4 | (s0&0xFF) >>> 4) ^ s1 ^ (s1 << 7)); // a, b
            stateB = (byte)(s1 << 3 | (s1&0xFF) >>> 5); // c
            //state = result << 8 & 0xFFFF;
            //state = ((result << 7 | (result&0xFF) >>> 1) + (stateC += 0x6D)) << 8 & 0xFFFF;
//            stateC+= 0x65;
//            s1 ^= (stateC);

//            stateC += 0x9D;
//            s1 = (byte)(stateA + stateB);
//            s1 ^= stateC;
//            stateC += 0x9D;
//            s1 += (byte)((s2 << 5 | (s2 & 0xFF) >>> 3) ^ (s2 << 2 | (s2 & 0xFF) >>> 6) ^ s2);
            s2 ^= (s2 & 0xFF) >>> 4;
            s1 += s2;
            s1 ^= (s1 & 0xFF) >>> 3;
//            s1 += ((s1 & 0xFF) << 4) + s2;
//            s1 ^= (s1 & 0xFF) >>> 3;

//            s1 = (byte)((s2 ^ (s2 & 0xFF) >>> 3) + s1);
//            s1 = (byte)((s1 << 5 | (s1 & 0xFF) >>> 3) + (s2 ^ (s2 & 0xFF) >>> 3));
//            s1 = (byte)((s2 << 5 | (s2 & 0xFF) >>> 3) + (s2 << 2 | (s2 & 0xFF) >>> 6) + s2);
//            s1 = (byte)((s2 << 5 | (s2 & 0xFF) >>> 3));
//            s1 = (byte)((s1 << 4 | (s1 & 0xFF) >>> 4));
//            s1 = (byte)((s1 << (stateC & 7) | (s1 & 0xFF) >>> (-stateC & 7)));
//            s1 += stateC;
//            s1 *= 0x9D;
//            s1 ^= (s1&0xFF) >>> 3;

            state = s1 << 8 & 0xFFFF;
//            state = (s1) << 8 & 0xFFFF;
//            result ^= (s0 << (stateC & 7) | (s0 & 0xFF) >>> (-stateC & 7));
//            s1 = (byte)((stateC & 0xFF) >>> 5); 
//            state = ((result << (s1 & 7) | (result & 0xFF) >>> (-s1 & 7))) << 8 & 0xFFFF;
//            state = ((stateC << (s1 & 7) | (stateC & 0xFF) >>> (-s1 & 7))) << 8 & 0xFFFF;
//            state = ((s0 << (stateC & 7) | (s0 & 0xFF) >>> (-stateC & 7)) ^ result) << 8 & 0xFFFF;
            s0 = stateA;
            s1 = (byte)((stateB ^ s0));
            s2 = (stateC = (byte)((stateC ^ 0x9D) * 3));
//            s2 = (byte)((stateC += 0x9D));
//            result = s1;
//            s1 ^= s0;
            stateA = (byte)((s0 << 4 | (s0&0xFF) >>> 4) ^ s1 ^ (s1 << 7)); // a, b
            stateB = (byte)(s1 << 3 | (s1&0xFF) >>> 5); // c
            //state |= result & 0xFF;
//            stateC+= 0x65;
//            s1 ^= (stateC);

//            stateC += 0x9D;
//            s1 = (byte)((s1 << (stateC & 7) | (s1 & 0xFF) >>> (-stateC & 7)));
//            s1 = (byte)(stateA + stateB);
//            s1 ^= stateC;
//            stateC += 0x9D;
            s2 ^= (s2 & 0xFF) >>> 4;
            s1 += s2;
            s1 ^= (s1 & 0xFF) >>> 3;
//            s1 += ((s1 & 0xFF) << 4) + s2;
//            s1 = (byte)((s2 ^ (s2 & 0xFF) >>> 3) + s1);

//            s1 = (byte)(s2 + (s1 ^ (s1 & 0xFF) >>> 3));
//            s1 ^= (s1 & 0xFF) >>> 3;
//            s1 = (byte)((s1 << 5 | (s1 & 0xFF) >>> 3) + (s2 ^ (s2 & 0xFF) >>> 3));
//            s1 += (byte)((s2 << 5 | (s2 & 0xFF) >>> 3) ^ (s2 << 2 | (s2 & 0xFF) >>> 6) ^ s2);
//            s1 = (byte)((s2 << 5 | (s2 & 0xFF) >>> 3) + (s2 << 2 | (s2 & 0xFF) >>> 6) + s2);
//            s1 = (byte)((s2 << 5 | (s2 & 0xFF) >>> 3) ^ (s2 << 2 | (s2 & 0xFF) >>> 6) ^ s2);
//            s1 ^= (byte)((s2 << 5 | (s2 & 0xFF) >>> 3));
//            s1 = (byte)((s2 << 5 | (s2 & 0xFF) >>> 3));

//            s1 = (byte)((s1 << 4 | (s1 & 0xFF) >>> 4) + stateC);
//            s1 = (byte)((s1 << 4 | (s1 & 0xFF) >>> 4));
//            s1 += stateC;
//            s1 *= 0x9D;
//            s1 ^= (s1&0xFF) >>> 3;
            state |= s1 & 0xFF;
//            state |= (s1 ^ (s1&0xFF) >>> 3 ^ stateC) & 0xFF;
            //state |= ((stateC << (s1 & 7) | (stateC & 0xFF) >>> (-s1 & 7))) & 0xFF;
//            result ^= (s0 << (stateC & 7) | (s0 & 0xFF) >>> (-stateC & 7));
//            s1 = (byte)((stateC & 0xFF) >>> 5);
//            state |= ((result << (s1 & 7) | (result & 0xFF) >>> (-s1 & 7))) & 0xFF;
            
//            state |= ((s0 << (stateC & 7) | (s0 & 0xFF) >>> (-stateC & 7)) ^ result) & 0xFF;
//            state |= ((s0 << (stateC & 7) | (s0 & 0xFF) >>> (-stateC & 7)) ^ result) & 0xFF;
            //state |= ((result << 7 | (result&0xFF) >>> 1) + (stateC += 0x6D)) & 0xFF;
            counts[state]++;

//            stream ^= (stream & 0xFF) >> 3;
//            state += 0x95;
//            final byte z = (byte) ((state ^ (state&0xFF) >> 3) * ((stream ^= stream << 5) | 1));
//            counts[(z ^ (z&0xFF)>>2) + (stream ^= (stream & 0xFF) >> 4) & 0xFF]++;

//            y = -(stream & 1);
//            final byte z = (state = (byte) (((state & 0xFF) * 0x65) + 0x7F + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8)))));
////            final byte z = (state += 1 + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
//            counts[(z ^ (z&0xFF) >>> 5) + (y >>> 24 ^ 0x95) & 0xFF]++;

            //uint64_t z = (stateA = (stateA ^ UINT64_C(0x6C8E9CF570932BD5)) * UINT64_C(0x5DA942042E4DD58B)) + (stateB = stateB >> 1u ^ (y & UINT64_C(0xD800000000000000)));
            //final byte z = (byte) ((state = (byte) (((state & 0xFF) ^ 0x65) * 0x5B)) + (stream = (byte)((stream & 0xFF) >>> 1 ^ (y & 0xB8))));
            //final byte z = (byte) (((state ^ state >>> 27) + (stream ^ stream >>> 30)) * 0xD9);
        }
        for (int i = 0; i < 65536; i++) {
            if(counts[i] >= 0x8000)
                System.out.printf("0x%04X occurs with frequency: %d\n", i, (int)counts[i]);
        }
        Arrays.sort(counts);
        char prev = counts[0];
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>(256);
        for (int i = 1, r = 1; i < 65536; i++) {
            if(prev == counts[i]){
                r++;
                if(i == 65535)
                    m.put((int)prev, r);
            }
            else
            {
                m.put((int)prev, r);
                r = 1;
                prev = counts[i];
            }
        }
        System.out.println(m);
   }

}