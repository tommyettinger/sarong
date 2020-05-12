/*
 * Copyright 2015 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.hashing;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.openhft.hashing.LongHashFunction.NATIVE_LITTLE_ENDIAN;

/**
 * Adapted version of xxHash implementation from https://github.com/Cyan4973/xxHash.
 * This implementation provides endian-independent hash values, but it's slower on big-endian platforms.
 */
public class WaterHash {
    private static final WaterHash INSTANCE = new WaterHash();
    private static final WaterHash NATIVE_WATER = NATIVE_LITTLE_ENDIAN ?
        WaterHash.INSTANCE : BigEndian.INSTANCE;

    // Primes if treated as unsigned
    private static final long P1 = 0x9e3779b185ebca87L;
    private static final long P2 = 0xc2b2ae3d27d4eb4fL;
    private static final long P3 = 0x165667b19e3779f9L;
    private static final long P4 = 0x85ebca77c2b2ae63L;
    private static final long P5 = 0x27d4eb2f165667c5L;

    private WaterHash() {}

    <T> long fetch64(Access<T> access, T in, long off) {
        return access.getLong(in, off);
    }

    // long because of unsigned nature of original algorithm
    <T> long fetch32(Access<T> access, T in, long off) {
        return access.getUnsignedInt(in, off);
    }

    // int because of unsigned nature of original algorithm
    private <T> int fetch8(Access<T> access, T in, long off) {
        return access.getUnsignedByte(in, off);
    }

    long toLittleEndian(long v) {
        return v;
    }

    int toLittleEndian(int v) {
        return v;
    }

    short toLittleEndian(short v) {
        return v;
    }
    /**
     * Big constant 0.
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1.
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2.
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3.
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4.
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5.
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits).
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    private static long mum(final long a, final long b) {
        final long n = a * b;
        return n - (n >>> 32);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * <br>
     * This was changed so it distributes bits from both inputs a little better on July 6, 2019.
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    private static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    <T> long waterHash64(long seed, T input, Access<T> access, long off, long length) {
        long remaining = length;
        long hash = 0x1E98AE18CA351B28L ^ seed,// seed = b0 ^ b0 >>> 23 ^ b0 >>> 48 ^ b0 << 7 ^ b0 << 53,
                a = hash ^ b4, b = (hash << 17 | hash >>> 47) ^ b3,
                c = (hash << 31 | hash >>> 33) ^ b2, d = (hash << 47 | hash >>> 17) ^ b1;

        while (remaining >= 32){
            a = (fetch64(access, input, off +  0) ^ a) * b1; a = (a << 23 | a >>> 41) * b3;
            b = (fetch64(access, input, off +  8) ^ b) * b2; b = (b << 25 | b >>> 39) * b4;
            c = (fetch64(access, input, off + 16) ^ c) * b3; c = (c << 29 | c >>> 35) * b5;
            d = (fetch64(access, input, off + 24) ^ d) * b4; d = (d << 31 | d >>> 33) * b1;
            hash += a + b + c + d;
            off += 32;
            remaining -= 32;
        }
        hash += b5;

        switch ((int)(remaining >>> 3)) {
            case 1: hash = wow(hash, b1 ^ fetch64(access, input, off)); break;
            case 2: hash = wow(hash + fetch64(access, input, off), b2 + fetch64(access, input, off + 8)); break;
            case 3: hash = wow(hash + fetch64(access, input, off), b2 + fetch64(access, input, off+8)) ^ wow(hash + fetch64(access, input, off+16), hash ^ b3); break;
        }
        hash = (hash ^ hash << 16) * (length >>> 3 ^ b0 ^ hash >>> 32);
        return hash - (hash >>> 31) + (hash << 33);
    }

    private static long finalize(long hash) {
        hash ^= hash >>> 33;
        hash *= P2;
        hash ^= hash >>> 29;
        hash *= P3;
        hash ^= hash >>> 32;
        return hash;
    }

    private static class BigEndian extends WaterHash {
        private static final BigEndian INSTANCE = new BigEndian();

        private BigEndian() {}

        @Override
        <T> long fetch64(Access<T> access, T in, long off) {
            return Long.reverseBytes(super.fetch64(access, in, off));
        }

        @Override
        <T> long fetch32(Access<T> access, T in, long off) {
            return Primitives.unsignedInt(Integer.reverseBytes(access.getInt(in, off)));
        }

        // fetch8 is not overloaded, because endianness doesn't matter for single byte

        @Override
        long toLittleEndian(long v) {
            return Long.reverseBytes(v);
        }

        @Override
        int toLittleEndian(int v) {
            return Integer.reverseBytes(v);
        }

        @Override
        short toLittleEndian(short v) {
            return Short.reverseBytes(v);
        }
    }

    public static LongHashFunction water() {
        return AsLongHashFunction.SEEDLESS_INSTANCE;
    }

    static LongHashFunction asLongHashFunctionWithoutSeed() {
        return AsLongHashFunction.SEEDLESS_INSTANCE;
    }

    private static class AsLongHashFunction extends LongHashFunction {
        private static final long serialVersionUID = 0L;
        static final AsLongHashFunction SEEDLESS_INSTANCE = new AsLongHashFunction();
        private static final long VOID_HASH = WaterHash.finalize(P5);

        private Object readResolve() {
            return SEEDLESS_INSTANCE;
        }

        public long seed() {
            return 0L;
        }

        @Override
        public long hashLong(long input) {
            input = NATIVE_WATER.toLittleEndian(input);
            long hash = seed() + P5 + 8;
            input *= P2;
            input = Long.rotateLeft(input, 31);
            input *= P1;
            hash ^= input;
            hash = Long.rotateLeft(hash, 27) * P1 + P4;
            return WaterHash.finalize(hash);
        }

        @Override
        public long hashInt(int input) {
            input = NATIVE_WATER.toLittleEndian(input);
            long hash = seed() + P5 + 4;
            hash ^= Primitives.unsignedInt(input) * P1;
            hash = Long.rotateLeft(hash, 23) * P2 + P3;
            return WaterHash.finalize(hash);
        }

        @Override
        public long hashShort(short input) {
            input = NATIVE_WATER.toLittleEndian(input);
            long hash = seed() + P5 + 2;
            hash ^= Primitives.unsignedByte(input) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            hash ^= Primitives.unsignedByte(input >> 8) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            return WaterHash.finalize(hash);
        }

        @Override
        public long hashChar(char input) {
            return hashShort((short) input);
        }

        @Override
        public long hashByte(byte input) {
            long hash = seed() + P5 + 1;
            hash ^= Primitives.unsignedByte(input) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            return WaterHash.finalize(hash);
        }

        @Override
        public long hashVoid() {
            return VOID_HASH;
        }

        @Override
        public <T> long hash(T input, Access<T> access, long off, long len) {
            long seed = seed();
            if (access.byteOrder(input) == LITTLE_ENDIAN) {
                return WaterHash.INSTANCE.waterHash64(seed, input, access, off, len);
            } else {
                return BigEndian.INSTANCE.waterHash64(seed, input, access, off, len);
            }
        }
    }
}
