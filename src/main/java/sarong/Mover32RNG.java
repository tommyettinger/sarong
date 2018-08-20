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
            0x00000001, 0xB6E16604, 0x84A91149, 0x67AC52C8, 0xBEF3BB8C, 0x23CE3E07, 0x6EDBB8A1, 0x274276DF,
            0xE66E7F42, 0x782250C3, 0x284F0D35, 0xB3903E44, 0x19A162D2, 0x7DCB5533, 0x31146CDF, 0x3666EE93,
            0x4CFFC6E9, 0xE9AF02E0, 0xC0D678CA, 0x82B174C0, 0xBF6F25BA, 0x6B37ADAB, 0xA8FE16E5, 0xB7B6C86B,
            0x3C6C3DC6, 0xA9CB1833, 0xEAA1A2D7, 0x5B12EB3D, 0xFB6229D1, 0x364EA5EF, 0x582F63D5, 0x0CB374F5,
            0x8B161A5E, 0xE759784A, 0x203788FA, 0xAD6791DB, 0xFE4E70F1, 0xB5E14DA3, 0x849610A5, 0xFFFB00FF,
            0x7D28539A, 0xD0B18B9E, 0x45F2E945, 0x9346659F, 0x02E0C263, 0xFA53EEFE, 0x366BC4B5, 0xBAA06D47,
            0x714EAB99, 0xA77278AE, 0xC7536981, 0xD2D5B2EF, 0x28C69EF7, 0x3B6B15DB, 0xAC81216F, 0xC6D50F66,
            0xBFCE5018, 0x43A36D98, 0x0C111B51, 0x4AE6EAA0, 0x960F00DE, 0x60143353, 0x61FEE9CD, 0x4B0CB168,
            0x91695609, 0x9C10C765, 0x29A4343B, 0xACABAB21, 0x4374163D, 0xC3BAA736, 0x918E3147, 0xC1DAB2FB,
            0x20355E4D, 0xA0D531FF, 0x516DF23D, 0x0F41D121, 0xAF38E8F6, 0xAE866375, 0x764AAA2B, 0xA63AE93A,
            0x35B9C0C5, 0x32DCDB6A, 0xA61561D0, 0x52518525, 0x7115E9B9, 0x27B34AD3, 0x8DBBB84F, 0x0F9AEF15,
            0x199EDEDD, 0xBA5A4993, 0x3CA7D786, 0xDD2C6E48, 0x90BEA6C9, 0xAA34E309, 0xE02FB459, 0x167FCA38,
            0xA8653EE4, 0x9FCF090E, 0xCB2B47F9, 0x9A3909E5, 0x75B0F986, 0x557B897B, 0x0873C70D, 0xCAF3824B,
            0x14F63600, 0xF00EF48B, 0x337CED22, 0xBE2A3E1D, 0x5939AF06, 0x72755544, 0xBEA17CD7, 0x0767E32B,
            0x05D4FB25, 0x560A74AB, 0x58332A3A, 0x309B106B, 0x0DDF9FEF, 0xBF20AB12, 0x4900C5B9, 0xBBF4A294,
            0xB9A4B25F, 0x0E0F882B, 0xB9AB5606, 0xE4DE86BC, 0xDD026D62, 0xEBB4B162, 0xE3CA2222, 0xEA5A01C5,
    }, startingB = {
            0x00000001, 0xA9767029, 0xC36D2FFF, 0x8BC8A46F, 0x3C586BE9, 0x654028F8, 0x3BC36ED8, 0xBCAD2EE5,
            0x12DD2D5C, 0x99D4A55B, 0xBA00D3C0, 0x4F85CF46, 0x2EDE74A5, 0xAD771823, 0x8ECC54D8, 0x2EB0955F,
            0x5C1BBA00, 0x9DB7CE62, 0x2A76D204, 0x6DC1C43E, 0xFF4E0E96, 0xE45728BB, 0x4103F12C, 0xD8E3D609,
            0x58F2587B, 0x4B169E3D, 0x00DDD04F, 0x3721B154, 0xACF777A2, 0x9715D29E, 0x46E45724, 0x299145D6,
            0x6FDD75D4, 0x572A304B, 0x17519541, 0xC59D50A6, 0x9BE5938D, 0x4AA90B25, 0x01626ACC, 0xDA9A024D,
            0x47F60DBD, 0x85BA1183, 0xF553788D, 0x1D642674, 0x50B506A0, 0x9EAC6A04, 0x3F7BFCB9, 0x5C32A24D,
            0x66B3A7AE, 0x1E0D0B7C, 0x3186148B, 0x0461A847, 0xE193E7FA, 0xB5CBB459, 0xFC7D8604, 0xF9F9C493,
            0xE038620A, 0xFFEDAC2F, 0x38FC87CD, 0x8A0E062B, 0x0EAA198F, 0xED6CBC65, 0x66A73D25, 0x5C3D77AD,
            0xCA32D8F4, 0x30D44109, 0x55AC56D7, 0x26784CD2, 0x8E95392F, 0x609DA1AB, 0xC01CBA2D, 0x36A594F5,
            0x65463BB0, 0xEC147FD9, 0xFCB3D73C, 0x13BC191E, 0x36E408C7, 0x550A1050, 0x118BCBCE, 0xF18CEFF0,
            0x781F50BC, 0xEB306A3F, 0x522147AC, 0x43EF7770, 0x48C7FD2B, 0x04965BE3, 0x557720E4, 0xBA355404,
            0x07884E1D, 0x85AB54AA, 0x1197FDFD, 0x635DEDE7, 0xBE49761B, 0xF7FA516C, 0xF1854433, 0x56133FE6,
            0x9F5F8EEF, 0x40B02A4F, 0x4E8B296F, 0xBC197E3B, 0xE896BA7F, 0x6BC0187A, 0x47FCADD1, 0xA594B585,
            0xA6517A0D, 0x45C47256, 0x8877ADD5, 0xAC8C32A7, 0x2376C425, 0x5427F940, 0xC3332A3E, 0xB3358CCA,
            0x8E6B3EDA, 0x33F7BF4F, 0x32A3294C, 0xE18EE95E, 0xAE8908E9, 0x79B012AB, 0xCEDEE0E9, 0xA3EB8638,
            0xA38C2A1A, 0xB60F4A3A, 0xFF670EC7, 0x8E1019AA, 0x9112227D, 0xDBC81559, 0x34CDFF4E, 0x5D357F7C,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA *= 0xACED;
            stateA = (stateA << 28 | stateA >>> 4);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB *= 0xBA55;
            stateB = (stateB << 19 | stateB >>> 13);
        }
    }

    public final int nextInt()
    {
        int y = stateA * 0xACED;
        y = (stateA = (y << 28 | y >>> 4));
        final int x = stateB * 0xBA55;
        return y ^ (stateB = (x << 19 | x >>> 13));
    }
    @Override
    public final int next(final int bits)
    {
        int y = stateA * 0xACED;
        y = (stateA = (y << 28 | y >>> 4));
        final int x = stateB * 0xBA55;
        return (y ^ (stateB = (x << 19 | x >>> 13))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        int y = stateA * 0xACED;
        y = (y << 28 | y >>> 4);
        int x = stateB * 0xBA55;
        long t = y ^ (x = (x << 19 | x >>> 13));
        y *= 0xACED;
        stateA = (y = (y << 28 | y >>> 4));
        x *= 0xBA55;
        return t << 32 ^ (y ^ (stateB = (x << 19 | x >>> 13)));
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
//        BigInteger result = BigInteger.valueOf(0xFB059E43L), tmp = BigInteger.valueOf(0xFC98CC08L);
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

    public static void main(String[] args)
    {
        Mover32RNG m = new Mover32RNG();
        System.out.println("int[] startingA = {");
        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x00000200) {
            m.setState(i);
            System.out.printf("0x%08X, ", m.stateA);
            if((ctr & 7) == 7)
                System.out.println();
        }
        System.out.println("}, startingB = {");
        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x02000000) {
            m.setState(i);
            System.out.printf("0x%08X, ", m.stateB);
            if((ctr & 7) == 7)
                System.out.println();
        }
        System.out.println("};");
    }
}
