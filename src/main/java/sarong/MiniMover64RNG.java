package sarong;

import sarong.util.StringKit;

/**
 * The fastest generator in this library currently, and one of Mark Overton's subcycle generators from
 * <a href="http://www.drdobbs.com/tools/229625477">this article</a>, specifically a cmr with a 64-bit state, that has
 * its result multiplied by a constant. Its period is unknown, but is at the very least 2 to the 42, since the generator
 * passes PractRand after generating that many 64-bit integers (it passes with two minor anomalies, and none at the end,
 * the 32TB mark). It probably won't pass many tests when the bits are reversed, so that is something to be aware of.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It skips at most 65536 times into its core generator's cycle when seeding. It uses
 * constants to store 128 known midpoints for that generator, which ensures it calculates an advance at most 511 times.
 * There are 2 to the 16 possible starting states for this generator when using {@link #setState(int)}, but it is
 * unknown if that method actually puts the generator in the longest possible cycle, or just a sufficiently long one.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed.
 * <br>
 * Created by Tommy Ettinger on 9/13/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class MiniMover64RNG implements RandomnessSource {
    private long stateA, stateB;
    public MiniMover64RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public MiniMover64RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #MiniMover64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA the state to use, exactly unless it is 0 (then this uses 1)
     */
    public MiniMover64RNG(final long stateA)
    {
        this.stateA = stateA == 0L ? 1L : stateA;
    }

    private static final long[] startingA = {
            0x000000009E3779B9L, 0x039D4C0EC780FFC4L, 0x22BF0C8809124A51L, 0x419478016274934CL, 0x67EB9FB70D83B000L, 0x8CD9300DB62CECACL, 0x5EB38D35E65BCA91L, 0x26E82EB2B8035384L,
            0xD3D6D3164D13AB96L, 0x22F343450051C48FL, 0xA9AF0884FFD1E11FL, 0x05B96ECE5678E573L, 0x49B2CD2FE2C5A8D7L, 0x4D58EEBEA4096D34L, 0xD078ADD52D36FCD7L, 0x5E7B25100F8B9199L,
            0x367E6F4DFF334968L, 0xB6117240C6983624L, 0x7360D391EC35D779L, 0x3464E854FD0DB0DAL, 0xEDC7CB38DCEC6236L, 0x7561A6EAFD832509L, 0xA2EFF24EE359B58BL, 0xF55CC1EB3C9C0532L,
            0x1188CFF963D7712AL, 0x5D1840177E492F3CL, 0x9A958076F51B6375L, 0x182520A1E0B8802DL, 0x63F63F7A18C3D395L, 0x1CD3EA9E55797B87L, 0xDA272371A34E004AL, 0xADE9798773033626L,
            0xAB9E177279A07270L, 0xD445048F21FE6889L, 0x9F9E6FFEE5D49C65L, 0x76AA00E9CF42948FL, 0x5B94795BA31EF681L, 0x8E327E5C23C6278EL, 0xA7487525FC71C0EDL, 0xCB4553F5CC35D2FFL,
            0xAA8DAFF6105EEC75L, 0xFF8BFDBF2939F199L, 0x763AB30E339AB08AL, 0xFAFF3BF69D677B2CL, 0xFE09C3CCADAC55FCL, 0xE0C2763206467E19L, 0x95BCE42330B30253L, 0xA46AA3749F42AFADL,
            0x0730B18B71912C8BL, 0x675AD42A33C23833L, 0xF6538A72F3FAB5EFL, 0xC6D4D39FC5E67FCCL, 0xF46B4C5291B6C364L, 0xF09A18B4BF487325L, 0x3D2FCA863CA14A3DL, 0xEC81A78A94A738F2L,
            0xB94126C859447C88L, 0x1A0C6CC2AB5BE5D6L, 0x3E6217A40B51C914L, 0x85B2612BFD533328L, 0x45CACB43C44E5435L, 0xD83F5E91A3E6DC14L, 0x9F0ECC356A201778L, 0x7C6D7B8C2566BC1BL,
            0xE90BE053465D2259L, 0xC0770C8420804D8EL, 0xA9B28B647F3E1137L, 0xFB74B9506E69D300L, 0x084E188DA96E397AL, 0x5FB25AA5DCE4B43DL, 0x9660B29E3BCAE4E1L, 0x70D29772984ABB1FL,
            0x5297E1ADF851EDB7L, 0x2194198123CF7CD3L, 0xA3D3AC4EC9E40109L, 0x17835AF5D74E022BL, 0xCD501B51D005E7F8L, 0x46DFD73FCA620DDEL, 0x628183BE18CB5C8CL, 0x206FC522720EFE48L,
            0x821F722D1191758DL, 0x47E88E67E6FB64BBL, 0x799C46DCD00EF4A1L, 0x26F278866AD710A8L, 0x9FFD01EFEBC3AEF5L, 0xB797BA536EDDE98BL, 0xDF8D6B81F91E068DL, 0xCED943914A93E894L,
            0x2572E5A835E13634L, 0x650C74798C3F4372L, 0x136F741D2FFE947FL, 0xFF038810EDDCD880L, 0xBF1C4C3B2046F3B2L, 0xDB6B1712607AE0E3L, 0x985EE8EF7E88A3B4L, 0xC96E7CD1F9DD6BFFL,
            0x897083347494EA74L, 0x1AFE74C8344D347AL, 0x2CE6E347A0055876L, 0x88DB18C55AD2529FL, 0xBC1334454676A99BL, 0x18BE613EF297E1E5L, 0x5EC4983D09F91159L, 0xA1865969F348DEFDL,
            0x765AD392A65888B7L, 0x551AF361C50DBA42L, 0x2BD28F4EC5BD37FFL, 0xD29D8B7E8FC9B0F7L, 0xCDF2826B168D8299L, 0xA98B919E395EE6B3L, 0xFC95F4661A18505FL, 0x1191A025A0E2A52DL,
            0x4E5C4AC1875D7449L, 0xA9718CBD2CFA8291L, 0x1F1A31F33A4B6306L, 0xE195535A47B96813L, 0x5BA56ECA862113B2L, 0xFE5868AF147BED1DL, 0x6BAED11FA6AF9B8BL, 0x18E7116F90938968L,
            0x6A25411BD98049A1L, 0x84797CA9522F6A59L, 0x7A2D443415A0C237L, 0xFB05EA2734634E63L, 0xBDD08B877D56F644L, 0x9D0CF3D98D427405L, 0xCE60AA76A161E94AL, 0x9B89A8F54366238AL,
    };

    /**
     * Sets the state using 16 bits of the given int {@code s}. Although 65536 seeds are possible, this will only
     * generate a new state at most 511 times.
     * @param s only 16 bits are used (values 0 to 65535 inclusive will all have different results).
     */
    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L;
        }
    }

    public final int nextInt()
    {
        return (int)((stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L) * 0x41C64E6DL);
    }
    @Override
    public final int next(final int bits)
    {
        return (int)((stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L) * 0x41C64E6DL) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        return (stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L) * 0x41C64E6DL;
    }

    /**
     * Produces a copy of this MiniMover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover64RNG
     */
    @Override
    public MiniMover64RNG copy() {
        return new MiniMover64RNG(stateA);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #MiniMover64RNG()},
     * {@link #MiniMover64RNG(int)}, or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the state, a long
     */
    public long getStateA()
    {
        return stateA;
    }

    /**
     * Sets the "A" part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int
     */
    public void setStateA(final long stateA)
    {
        this.stateA = stateA == 0L ? 1L : stateA;
    }
    
    @Override
    public String toString() {
        return "Mover64RNG with state 0x" + StringKit.hex(stateA);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiniMover64RNG mover64RNG = (MiniMover64RNG) o;

        return stateA == mover64RNG.stateA;
    }

    @Override
    public int hashCode() {
        return (int)(stateA ^ stateA >>> 32);
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
    
///////// BEGIN subcycle finder code and period evaluator
//    public static void main(String[] args)
//    {
//        // multiplying
//        // A refers to 0x9E377
//        // A 10 0xC010AEB4
//        // B refers to 0x64E6D
//        // B 22 0x195B9108
//        // all  0x04C194F3485D5A68
//
//        // A=Integer.rotateLeft(A*0x9E377, 17) 0xF7F87D28
//        // B=Integer.rotateLeft(A*0x64E6D, 14) 0xF023E25B 
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
//        // 0xC6D5
//        // rotation 05: 0xFFD78FD4
//        // 0x5995
//        // rotation 28: 0xFF4AB87D
//        // rotation 02: 0xFF2AA5D5
//        // 0xA3A9
//        // rotation 09: 0xFF6B3AF7
//        // 0xB9EF
//        // rotation 23: 0xFFAEB037
//        // 0x3D29
//        // rotation 04: 0xFF6B92C5
//        // 0x5FAB
//        // rotation 09: 0xFF7E3277 // seems to be very composite
//        // 0xCB7F
//        // rotation 01: 0xFF7F28FE
//        // 0x89A7
//        // rotation 13: 0xFFFDBF50 // wow! note that this is a multiple of 16
//        // 0xBCFD
//        // rotation 17: 0xFFF43787 // second-highest yet, also an odd number
//        // 0xA01B
//        // rotation 28: 0xFFEDA0B5
//        // 0xC2B9
//        // rotation 16: 0xFFEA9001
//        
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
//        // can get 63.999691 with: (period is 0xFFF1F6F18B2A1330)
//        // multiplying A by 0x89A7 and rotating left by 13
//        // multiplying B by 0xBCFD and rotating left by 17
//        // can get 63.998159 with: (period is 0xFFAC703E2B6B1A30)
//        // multiplying A by 0x89A7 and rotating left by 13
//        // multiplying B by 0xB9EF and rotating left by 23
//        // can get 63.998 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // xorshifting B left by 5 (B ^ B << 5) and rotating left by 15
//        // can get 63.99 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // adding 0x6C8E9CF7 for B and rotating left by 7
//        // can get 63.98 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // multiplying by 0xACED, NOTing, and rotating left by 28 for B
//        // 0xFF6B3AF7L 0xFFAEB037L 0xFFD78FD4L
//        
//        // 0xFF42E24AF92DCD8C, 63.995831
//        //BigInteger result = BigInteger.valueOf(0xFF6B3AF7L), tmp = BigInteger.valueOf(0xFFD78FD4L);
//
//        BigInteger result = BigInteger.valueOf(0xFFFDBF50L), tmp = BigInteger.valueOf(0xFFF43787L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFEDA0B5L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
////        tmp = BigInteger.valueOf(0xFFABD755L);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
//        int stateA = 1, i;
//        LinnormRNG lin = new LinnormRNG();
//        System.out.println(lin.getState());
//        Random rand = new RNG(lin).asRandom();
//        for (int c = 1; c <= 200; c++) {
//            //final int r = (Light32RNG.determine(20007 + c) & 0xFFFF)|1;
//            final int r = BigInteger.probablePrime(20, rand).intValue();
//            //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//            System.out.printf("%03d/200, testing r = 0x%08X\n", c, r);
//            for (int j = 1; j < 32; j++) {
//                i = 0;
//                for (; ; i++) {
//                    if ((stateA = Integer.rotateLeft(stateA * r, j)) == 1) {
//                        if (i >>> 24 == 0xFF)
//                            System.out.printf("(state * 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
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
///////// END subcycle finder code and period evaluator
    
    public static void main(String[] args)
    {
        long stateA = 0x9E3779B9L;
        System.out.println("long[] startingA = {");
        for (int ctr = 0; ctr < 128; ctr++) {
            System.out.printf("0x%016XL, ", stateA);
            if((ctr & 7) == 7)
                System.out.println();
            for (int i = 0; i < 512; i++) {
                stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L;
            }
        }
        System.out.println("};");
    }
}
