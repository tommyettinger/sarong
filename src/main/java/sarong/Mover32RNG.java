package sarong;

import sarong.util.StringKit;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr^cmr with two 32-bit states; this is the fastest 32-bit generator that still passes statistical
 * tests, plus it's optimized for GWT. It has a period of just under 2 to the 64, 0xFFF547E3D0AA1626, which is 2 to the
 * 63.999764, and allows 2 to the 32 initial seeds.
 * <br>
 * This seems to do well in PractRand testing (testing hasn't finished on this generator, but results up to 64GB have no
 * anomalies or failures), but this is not one of the exact generators Overton tested. "Chaotic" generators like this
 * one tend to score well in PractRand, but it isn't clear if they will fail other tests. As for speed, this is faster
 * than {@link Lathe32RNG} (which is also high-quality) and is also faster than {@link XoRo32RNG} (which is very fast
 * but has quality issues) and {@link ThrustAlt32RNG} (which has a very small period and probably isn't very useful).
 * A similar generator (One of Overton's CMR^CMR type) is even faster on desktop, but is slower than Lathe32RNG when
 * using GWT and viewing in Firefox or Chrome.
 * Its period is 0xFFF547E3D0AA1626 for the largest cycle, which it always initializes into if {@link #setState(int)} is
 * used. setState() only allows 2 to the 32 starting states, but less than 2 to the 64 states
 * are in the largest cycle, so using a long or two ints to set the state seems ill-advised.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It uses two generators with different cycle lengths, and skips at most 65536 times
 * into each generator's cycle independently when seeding. It uses constants to store 128 known midpoints for each
 * generator, which ensures it calculates an advance for each generator at most 511 times. 
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed.
 * <br>
 * Created by Tommy Ettinger on 8/6/2018.
 */
public final class Mover32RNG implements RandomnessSource {
    private int stateA, stateB;
    public Mover32RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Mover32RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Mover32RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Mover32RNG(final int stateA, final int stateB)
    {
        this.stateA = stateA == 0 ? 1 : stateA;
        this.stateB = stateB == 0 ? 1 : stateB;
    }

    private static final int[] startingA = {
            0x00000001, 0xE0F813FB, 0x3853A562, 0x416A00FB, 0x969733D0, 0x68FC5927, 0x99A19AAE, 0xC4C790CC,
            0x996A09D2, 0x6914B3DE, 0xB1A25EE1, 0xBCF1B09F, 0x442C6EF5, 0x475E34F6, 0xCC900DB2, 0x07E720B0,
            0x0058393C, 0xA8D71A86, 0xFFA9E99A, 0x2A123AD8, 0x162BA47D, 0x6993B504, 0xAFB932FD, 0xFAD8E139,
            0x77990180, 0x452C71B3, 0xF265CBCD, 0xC0F7B73D, 0x8A7F9F87, 0x845E4932, 0x8B5D1621, 0xBCAFA92A,
            0xD6D7A1EC, 0x8E2E18A7, 0x33E41E53, 0xDD326FE3, 0x2F05B4EA, 0xB8F6F3C7, 0x9E3DD946, 0x1F0394CB,
            0x7F0F299C, 0x61CB8AA1, 0xA9E05E22, 0xBBCD908F, 0xB070DD10, 0x0A2DDBB2, 0xCD0E44D6, 0x0D07CF67,
            0x6318736A, 0x74851D1A, 0x67403862, 0x7EAF0F09, 0xDCDFDA15, 0xAE41DED5, 0x6C2B2FB6, 0x6C2A7DA7,
            0x3A469C2E, 0x7B3EA36E, 0x858A16F4, 0x5FE35777, 0x21D4F6AF, 0x8547F196, 0x0D562186, 0x8F88EF25,
            0x38B8DB7C, 0x5C15CB9D, 0x75F3C219, 0xD0CC6A7B, 0xBBD4DA22, 0x38174193, 0xDEF01557, 0xF8B890C3,
            0x5CAE3E75, 0xE366F7B7, 0x10BB0CAB, 0xF82D9CB8, 0xB7D312CC, 0x69F60C1C, 0x512E23E0, 0xBFA77DBB,
            0x8CCC44D0, 0x4A486348, 0x233AC5DD, 0xEFF581CF, 0x51D5EA09, 0x6448830E, 0xFF0B9140, 0xA0F34887,
            0x92C0F3B8, 0xBBF60BDF, 0xCBE1A1A9, 0x31BF3301, 0xEC6E09A5, 0x87F67486, 0x272E487B, 0x3CEA124F,
            0x55282704, 0x6157B070, 0x6102419F, 0x58651391, 0x1C305E1A, 0xFE77CF87, 0x6C78B78B, 0xFF22C4C9,
            0x077E96D6, 0xF37FE662, 0x1B08A31A, 0x9C38F87C, 0x55C56A19, 0xE31AA568, 0x11E35EBE, 0xE7C81D24,
            0xE6C3D7E0, 0x87A9B50F, 0xB7D3547D, 0x34F0B5C9, 0xC4F93C05, 0xEAC59BB3, 0xDEA348B6, 0x23C56119,
            0xB76AE1A3, 0x0D8DA124, 0x4927CF73, 0x2580B158, 0x1F731C31, 0x9DEDEC30, 0x12BAFE34, 0x0B139973,
    }, startingB = {
            0x00000001, 0xFDE752F6, 0xC84AF5A3, 0xA87499BC, 0xD90270DE, 0xE90ACB23, 0x9257E51C, 0x9EE34DF2,
            0x6B6145AE, 0xEC75C190, 0xBCC18895, 0x32D10686, 0x317F5535, 0x5A97DDE9, 0x6A49F707, 0x4FD63148,
            0x31C884C3, 0xEE68C32E, 0xDECE7562, 0x989C6CA0, 0x449BAC70, 0xBFF6415E, 0xEB06F9A5, 0xABB8890F,
            0x859213D3, 0x7D9C5EB7, 0xCEFB7D21, 0x1054397A, 0x47133437, 0x39EA89DC, 0x57F3FA9B, 0xB7C9825D,
            0xBF5CF78E, 0x67AD1C2D, 0x841B6434, 0x5E82C45F, 0x97948021, 0xBF76909D, 0xF74020C5, 0x52F504F2,
            0x6B4466F5, 0x6742D957, 0x9FF19ABC, 0x57104213, 0x39B5F5D1, 0x730E17AB, 0x440F8E1B, 0x3B1D4A42,
            0x8ED24EBF, 0x7ECE6545, 0x710D59C2, 0x96E768D8, 0x3A33BFAF, 0x775A6C30, 0x5DE6E29C, 0x27B5C59D,
            0x24630AF2, 0x2F174DBC, 0xBF2D6A4C, 0xD6334C3B, 0xBE53EF43, 0xC1ECD43B, 0x6A60478E, 0x108F9B4A,
            0xA604530E, 0x4570407F, 0xCC6B423A, 0x47C0C0F9, 0xB09671A7, 0xE9A6BDFD, 0xABBD2751, 0x2524B64F,
            0x69B20A61, 0x0E696B30, 0x81633930, 0x0006ED93, 0x5C12F794, 0xE82602E0, 0xB1D5EDC2, 0xD31990D7,
            0xA9F6060E, 0x3C6BFA34, 0xD193C00F, 0x82D7DB5E, 0x82C49F3C, 0x7A771155, 0x8F0D9415, 0x8A17684D,
            0x2D77E8D6, 0x2913858E, 0xB533A466, 0x8129764F, 0x63162CD2, 0x5F2DD6F6, 0xBF8B497A, 0xB43BE06C,
            0x98654103, 0x8C28E2AD, 0xDF898920, 0x5D7AA02D, 0x402A4E1F, 0x31CAE12C, 0xA03FB63D, 0x45F0D48A,
            0xEFB636E4, 0x15BA997E, 0x03BE743D, 0x50C3829B, 0x1995789C, 0x9EB14174, 0x7E0FACE3, 0xEACE464A,
            0x8FD5E698, 0x921A2C9A, 0xBA254C0A, 0x946AD363, 0x380AAFC3, 0xCEA4C41D, 0x13789C1F, 0xD5F712C3,
            0x9599AECF, 0x03777BEA, 0xE27AD2AD, 0x17F9E31B, 0xA3AE7641, 0x4A607868, 0x7747EE23, 0x56EB9F40,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA = stateA - 0x9E3779B9 ^ 0xE541440F;
            stateA = (stateA << 22 | stateA >>> 10);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB += 0xC0EF50EB;
            stateB = (stateB << 7 | stateB >>> 25);
        }
    }

    public final int nextInt()
    {
        int y = stateA - 0x9E3779B9 ^ 0xE541440F;
        y = (stateA = (y << 22 | y >>> 10));
        final int x = stateB + 0xC0EF50EB;
        y ^= (stateB = (x << 7 | x >>> 25));
        y ^= y << 9;
        return y ^ y >>> 13;
    }
    @Override
    public final int next(final int bits)
    {
        int y = stateA - 0x9E3779B9 ^ 0xE541440F;
        y = (stateA = (y << 22 | y >>> 10));
        final int x = stateB + 0xC0EF50EB;
        y ^= (stateB = (x << 7 | x >>> 25));
        y ^= y << 9;
        return (y ^ y >> 13) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        int y = stateA - 0x9E3779B9 ^ 0xE541440F;
        y = (y << 22 | y >>> 10);
        int x = stateB + 0xC0EF50EB;
        int z = y ^ (x = (x << 7 | x >>> 25));
        z ^= z << 9;
        long t = z ^ z >>> 13;
        y = y - 0x9E3779B9 ^ 0xE541440F;
        stateA = (y = (y << 22 | y >>> 10));
        x += 0xC0EF50EB;
        z = y ^ (stateB = (x << 7 | x >>> 25));
        z ^= z << 9;
        return t << 32 ^ (z ^ z >>> 13);
    }

    /**
     * Produces a copy of this Mover32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover32RNG
     */
    @Override
    public Mover32RNG copy() {
        return new Mover32RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Mover32RNG()}, {@link #Mover32RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "A" part of the state, an int
     */
    public int getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state; if this generator was set with {@link #Mover32RNG()}, {@link #Mover32RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "B" part of the state, an int
     */
    public int getStateB()
    {
        return stateB;
    }
    /**
     * Sets the "A" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int
     */
    public void setStateA(final int stateA)
    {
        this.stateA = stateA;
    }

    /**
     * Sets the "B" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateB any int
     */
    public void setStateB(final int stateB)
    {
        this.stateB = stateB;
    }
    @Override
    public String toString() {
        return "Mover32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mover32RNG mover32RNG = (Mover32RNG) o;

        return stateA == mover32RNG.stateA && stateB == mover32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }

//    public static void main(String[] args)
//    {
//        // A 10 0xC010AEB4
//        // B 22 0x195B9108
//        // all  0x04C194F3485D5A68
//
//        // A 17 0xF7F87D28
//        // B 14 0xF023E25B 
//        // all  0xE89BB7902049CD38
//
//
//        // A11 B14 0xBBDA9763B6CA318D
//        // A8  B14 0xC109F954C76CB09C
//        // A17 B14 0xE89BB7902049CD38
////        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
////        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("0x%016X\n", result.longValue());
//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(stateA * 0x9E37, 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(stateA * 0x4E6D, 14)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }         
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());
//
//    }

//    public static void main(String[] args)
//    {
//        Mover32RNG m = new Mover32RNG();
//        System.out.println("int[] startingA = {");
//        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x00000200) {
//            m.setState(i);
//            System.out.printf("0x%08X, ", m.stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//        }
//        System.out.println("}, startingB = {");
//        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x02000000) {
//            m.setState(i);
//            System.out.printf("0x%08X, ", m.stateB);
//            if((ctr & 7) == 7)
//                System.out.println();
//        }
//        System.out.println("};");
//    }

//    public static void main(String[] args)
//    {
//        // A 10 0xC010AEB4
//        // B 22 0x195B9108
//        // all  0x04C194F3485D5A68
//
//        // A 17 0xF7F87D28
//        // B 14 0xF023E25B 
//        // all  0xE89BB7902049CD38
//
//
//        // A11 B14 0xBBDA9763B6CA318D
//        // A8  B14 0xC109F954C76CB09C
//        // A17 B14 0xE89BB7902049CD38
////        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
////        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("0x%016X\n", result.longValue());
//        // 0x9E37
//        // rotation 27: 0xEE06F34D
//        // 0x9E35
//        // rotation 6 : 0xE1183C3A
//        // rotation 19: 0xC4FCFC55
//        // 0x9E3B
//        // rotation 25: 0xE69313ED
//        // 0xDE4D
//        // rotation 3 : 0xF6C16607
//        // rotation 23: 0xD23AD58D
//        // rotation 29: 0xC56DC41F
//        // 0x1337
//        // rotation 7: 0xF41BD009
//        // rotation 20: 0xF5846878
//        // rotation 25: 0xF38658F9
//        // 0xACED
//        // rotation 28: 0xFC98CC08
//        // rotation 31: 0xFA18CD57
//        // 0xBA55
//        // rotation 19: 0xFB059E43
//        
//        // adding
//        // 0x9E3779B9
//        // rotation 2 : 0xFFCC8933
//        // rotation 7 : 0xF715CEDF
//        // rotation 25: 0xF715CEDF
//        // rotation 30: 0xFFCC8933
//        // 0x6C8E9CF5
//        // rotation 6 : 0xF721971A
//        // 0x41C64E6D
//        // rotation 13: 0xFA312DBF
//        // rotation 19: 0xFA312DBF
//        // rotation 1 : 0xF945B8A7
//        // rotation 31: 0xF945B8A7
//        // 0xC3564E95
//        // rotation 1 : 0xFA69E895 also 31
//        // rotation 5 : 0xF2BF5E23 also 27
//        // 0x76BAF5E3
//        // rotation 14: 0xF4DDFC5A also 18
//        // 0xA67943A3 
//        // rotation 11: 0xF1044048 also 21
//        // 0x6C96FEE7
//        // rotation 2 : 0xF4098F0D
//        // 0xA3014337
//        // rotation 15: 0xF3700ABF also 17
//        // 0x9E3759B9
//        // rotation 1 : 0xFB6547A2 also 31
//        // 0x6C8E9CF7
//        // rotation 7 : 0xFF151D74 also 25
//        // rotation 13: 0xFD468E2B also 19
//        // rotation 6 : 0xF145A7EB also 26
//        // 0xB531A935
//        // rotation 13: 0xFF9E2F67 also 19
//        // 0xC0EF50EB
//        // rotation 07: 0xFFF8A98D also 25
//        // 0x518DC14F
//        // rotation 09: 0xFFABD755 also 23 // probably not prime
//        // 0xA5F152BF
//        // rotation 07: 0xFFB234B2 also 27
//        // 0x8092D909
//        // rotation 10: 0xFFA82F7C also 22
//        // 0x73E2CCAB
//        // rotation 09: 0xFF9DE8B1 also 23
//        // stateB = rotate32(stateB + 0xB531A935, 13)
//        // stateC = rotate32(stateC + 0xC0EF50EB, 7)
//        
//        // subtracting, rotating, and bitwise NOT:
//        // 0xC68E9CF3
//        // rotation 13: 0xFEF97E17, also 19 
//        // 0xC68E9CB7
//        // rotation 12: 0xFE3D7A2E
//        
//        // left xorshift
//        // 5
//        // rotation 15: 0xFFF7E000
//        // 13
//        // rotation 17: 0xFFFD8000
//        
//        // minus left shift, then xor
//        // state - (state << 12) ^ 0xC68E9CB7, rotation 21: 0xFFD299CB
//        // add xor
//        // state + 0xC68E9CB7 ^ 0xDFF4ECB9, rotation 30: 0xFFDAEDF7
//        // state + 0xC68E9CB7 ^ 0xB5402ED7, rotation 01: 0xFFE73631
//        // state + 0xC68E9CB7 ^ 0xB2B386E5, rotation 24: 0xFFE29F5D
//        // sub xor
//        // state - 0x9E3779B9 ^ 0xE541440F, rotation 22: 0xFFFC9E3E
//        
//        
//        // best power of two:
//        // can get 63.998 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // xorshifting B left by 5 (B ^ B << 5) and rotating left by 15
//        // can get 63.99 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // adding 0x6C8E9CF7 for B and rotating left by 7
//        // can get 63.98 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // multiplying by 0xACED, NOTing, and rotating left by 28 for B
//        BigInteger result = BigInteger.valueOf(0xFFF8A98DL), tmp = BigInteger.valueOf(0xFFFC9E3EL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        tmp = BigInteger.valueOf(0xFFD299CBL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        tmp = BigInteger.valueOf(0xFFABD755L);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%s, %2.6f\n", result.toString(16), Math.log(result.doubleValue()) / Math.log(2));
//        int stateA = 1, i;
//        for (int c = 1; c <= 200; c++) {
//            final int r = Light32RNG.determine(5007 + c) | 0x80000001;
//            //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//            System.out.printf("%03d/200, testing r = 0x%08X\n", c, r);
//            for (int j = 1; j < 32; j++) {
//                i = 0;
//                for (; ; i++) {
//                    if ((stateA = Integer.rotateLeft(stateA + 0xC68E9CB7 ^ r, j)) == 1) {
//                        if (i >>> 24 == 0xFF)
//                            System.out.printf("(state + 0xC68E9CB7 ^ 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
//                        break;
//                    }
//                }
//            }
//        }
//
////        int stateA = 1, i = 0;
////        for (; ; i++) {
////            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
////            {
////                System.out.printf("0x%08X\n", i);
////                break;
////            }
////        }
////        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
////        i = 0;
////        for (; ; i++) {
////            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
////            {
////                System.out.printf("0x%08X\n", i);
////                break;
////            }
////        }         
////        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("\n0x%016X\n", result.longValue());
//
//    }
//    
////    public static void main(String[] args)
////    {
////        Mover32RNG m = new Mover32RNG();
////        System.out.println("int[] startingA = {");
////        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x00000200) {
////            m.setState(i);
////            System.out.printf("0x%08X, ", m.stateA);
////            if((ctr & 7) == 7)
////                System.out.println();
////        }
////        System.out.println("}, startingB = {");
////        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x02000000) {
////            m.setState(i);
////            System.out.printf("0x%08X, ", m.stateB);
////            if((ctr & 7) == 7)
////                System.out.println();
////        }
////        System.out.println("};");
////    }
}
