package sarong;

import sarong.util.StringKit;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr with a 64-bit state, that has its result multiplied by a constant. Its period is unknown. The
 * period is at the very least 2 to the 38, since the cmr generator has been checked up to that period length without
 * running out of period.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It skips at most 65536 times
 * into its core generator's cycle when seeding. It uses constants to store 128 known midpoints for that
 * generator, which ensures it calculates an advance at most 511 times. There are 2 to the 32
 * possible starting states for this generator when using {@link #setState(int)}, but it is unknown if that method
 * actually puts the generator in the longest possible cycle, or just a sufficiently long one.
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
            0x0000000041C64E6BL, 0xEF4563DBF53BAFEBL, 0xC9473CEB940930F1L, 0xD005BC5361828BAAL, 0xFA26D82FE48E01F7L, 0xF364837DAF5B1665L, 0xB7C1B31ADD219790L, 0x051D187FB68CA207L,
            0x991687C566C20C69L, 0xAC4695A49B3946C9L, 0x4A7A9075CC3C7893L, 0xBB3D9B37E8C10AE3L, 0x375FCBABD788698EL, 0xD68E2F704BC94031L, 0x1711B04B27E212B7L, 0x828D35A6EF002365L,
            0x97A7894B55C6BE26L, 0x240E385EFBA8FA8BL, 0xC69C2B3811506884L, 0xA3A43140A2F5BCC2L, 0xE0A4E06471A54F53L, 0xD8083FC08D140615L, 0x23B3A4F7D613C2DCL, 0xFECD1327FA65F182L,
            0x7669C2654D3BD322L, 0xB1E302546CC2F3B7L, 0x4DE59563A71AA71FL, 0xC00D9E5F7F1592C3L, 0xCD525EBBEE5898EEL, 0x123B8C22AD497218L, 0x49517AE95A218A36L, 0x98B4301E16ABD51BL,
            0x1DEBD6974717B12DL, 0xC6DC07327CE1A182L, 0xA7A46144B93B06C2L, 0xE4724B4E4DA3CA65L, 0x1A033E1FAE65905DL, 0xBC36CE75A306C724L, 0x8C95690DB12AEAFFL, 0x46A4E4489885ED44L,
            0xC6D8E7BA62C72E5BL, 0x75D92C3EFD6102A0L, 0x46B31EF29FFF25CAL, 0xDE0EFE69276684DEL, 0x4721D67D543E4795L, 0xF127E3F3CE6F4485L, 0x1D20550E407582D4L, 0x219D04CAC8820A2BL,
            0x30E0D153CB92480EL, 0x8FE37BF67657F061L, 0x1D4AE8F0EF20C406L, 0x8D3D7DB6C69196B0L, 0xF14A1BB76968044FL, 0xFF7601F422327C04L, 0x5ABE5A6DF8579F67L, 0x6D40827D1CB439BDL,
            0xBD690447C7063DE0L, 0xC90158B1277A71F0L, 0x8BC66371575E5DAAL, 0xFCD3CCFB8261026CL, 0x9EFD68155A1AB99DL, 0xCCE0F1F7E0A7C593L, 0x4C6EA9F8D4CBC6C9L, 0xDFF56BAEB0E2B21DL,
            0xED30BF4BD50DB1B5L, 0x156438AAA396BF57L, 0x5F4A6C8463E7B5CFL, 0xB711FA310B981CCEL, 0xEFB2F1176429177EL, 0x5296D6B37C6DCE0DL, 0xF771DC501A5B414FL, 0x724AD00E5B0D67CCL,
            0x284FDBD72A9F128EL, 0xCDEBD4032E3E3FD3L, 0x12AC369973C8B4D0L, 0x9700AF4FAAF66DEAL, 0x2EDA40BFC22D073BL, 0xB008A44F3E1E31F1L, 0x57F1D981B9B50E5EL, 0xFEF3677844A09EC7L,
            0xDB47A0B27BF77DF3L, 0x0CB4C3B688BEB057L, 0xC9245362B0DD5010L, 0x1663D3102F7D7E64L, 0x1588D1E70E4FEC59L, 0x65F48845D0FC6926L, 0xDB759F0BEE52FE39L, 0x8F6138F380AFB411L,
            0x631E84C5BDDC3635L, 0xDB0E835883D533F3L, 0xCA37A28BDDEE441AL, 0xB38757850835989BL, 0x996F3BE0FF3A16D3L, 0x0E06EB8C69D79D07L, 0x72D691BD9744BC9BL, 0xBBD092004CBAE725L,
            0x7FDB07A2E7441983L, 0x7F1885B7B191BA2CL, 0x0ADD9FC3A3F4FDD8L, 0xFFAE7E1208AD17BEL, 0xAE4E2A78AC731CD3L, 0x8DE9DBBBA5E5516CL, 0x585BDE400F927D5DL, 0x76E6EF636DA3A391L,
            0xC181F6966C1CC2CAL, 0x3EA18F057776D247L, 0xAD19ED105057096AL, 0xB5751F6E9097CEFFL, 0x1D5673FAB6BD2AD1L, 0x6DBBC55F1E5FA934L, 0x84A0B604F28B40CEL, 0x9DE4A3BC3252D6DBL,
            0xC12B76EFFB817725L, 0x529FB77A0FE05D39L, 0x9FCF0CD7FD64D4FBL, 0x6614158F30DB33BDL, 0x04ACD499677DDC44L, 0xF774D044E9174271L, 0x19CEBC6A32905CDFL, 0x096EFF39BA4ABEFFL,
            0x78B1F5AA54DA2681L, 0xE5D0D955D1D29F92L, 0xDF90549CD7E09E17L, 0xF35A265E90E0F720L, 0x0162FC20830ED5D4L, 0x991F77F540CCC6E1L, 0xE93B7118E07C28EBL, 0x597203FCC7A8546BL,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA = (stateA << 42 | stateA >>> 22) * 0x41C64E6BL;
        }
    }

    public final int nextInt()
    {
        return (int)((stateA = (stateA << 42 | stateA >>> 22) * 0x41C64E6BL) * 0x41C64E6DL);
    }
    @Override
    public final int next(final int bits)
    {
        return (int)((stateA = (stateA << 42 | stateA >>> 22) * 0x41C64E6BL) * 0x41C64E6DL) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        return (stateA = (stateA << 42 | stateA >>> 22) * 0x41C64E6BL) * 0x41C64E6DL;
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
    
//    public static void main(String[] args)
//    {
//        long stateA = 0x41C64E6BL;
//        System.out.println("long[] startingA = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%016XL, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateA = (stateA << 42 | stateA >>> 22) * 0x41C64E6BL;
//            }
//        }
//        System.out.println("};");
//    }
}
