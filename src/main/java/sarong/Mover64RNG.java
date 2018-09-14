package sarong;

import sarong.util.StringKit;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr^cmr with two 64-bit states.
 * <br>
 * This seems to do well in PractRand testing, but this is not one of the exact generators Overton tested. "Chaotic"
 * generators like this one tend to score well in PractRand, but it isn't clear if they will fail other tests (in
 * particular, they probably can't generate all possible long values, and maybe can't generate some ints).
 * <br>
 * Its period is unknown, but almost certainly higher than 2 to the 64. The generator has two similar parts, each
 * updated without needing to read from the other part. Each is a 64-bit CMR generator, which multiplies a state by a
 * constant, rotates by another constant, and stores that as the next state. The multiplier constants used here were
 * chosen arbitrarily, since almost all odd-number multipliers produce a period for a CMR generator that is too long to
 * iterate through and compare. One multiplier is close to the LCG multiplier used in PractRand; the other is very close
 * to the golden ratio times 2 to the 64. Better multipliers are almost guaranteed to exist, but finding them would be a
 * challenge. The rotation constants were chosen so they were sufficiently different, which seems to help quality.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It uses two generators with different cycle lengths, and skips at most 65536 times
 * into each generator's cycle independently when seeding. It uses constants to store 128 known midpoints for each
 * generator, which ensures it calculates an advance for each generator at most 511 times. 
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed.
 * <br>
 * Created by Tommy Ettinger on 9/13/2018.
 */
public final class Mover64RNG implements RandomnessSource {
    private long stateA, stateB;
    public Mover64RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Mover64RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Mover64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Mover64RNG(final long stateA, final long stateB)
    {
        this.stateA = stateA == 0L ? 1L : stateA;
        this.stateB = stateB == 0L ? 1L : stateB;
    }

    private static final long[] startingA = {
            0x0000000000000001L, 0x13648FBE2EEA1D43L, 0x8519AA5F58ABEA7BL, 0xAB5F39C7BCCE6B7BL, 0x5920D72FD8E767EEL, 0xD6D2BF560B4F27A2L, 0x5FC69CF0D6E3988EL, 0x8AD9DA62A8D55F45L,
            0x4932E1B70765F664L, 0xF9EFA986048518EAL, 0x8FBEF24E98962D80L, 0x7F0EA66EBD399457L, 0xA2D3D9FB25C32227L, 0xF9A28F4270F7EA88L, 0x411A09457C23731BL, 0x687BA31E648A2F6EL,
            0xCB2C76E554E2924FL, 0x5DCB31129339FCB0L, 0x10405FC17347023CL, 0x7C41E02D43FB1A4FL, 0x7B83FB5E07206815L, 0x10F3598A9AB08B43L, 0x47F6D61340D2A799L, 0x55CE53E14F2EC379L,
            0x12086C91F38D43BEL, 0xE4D1CFC6CA642F5EL, 0x75E1EBBA93A95CB2L, 0x6D60802C494AB3AAL, 0x2E6B1F9D18D29864L, 0x4B6572CD6AFFBB94L, 0xA84C33DAD6523A2BL, 0x1AB5B6DAA48E1979L,
            0x5A46E58B326F0155L, 0xCCC2E49EF97CB0F7L, 0x2A4A61F2A2867632L, 0x7BC39BEA33CB0134L, 0xFF9FC824B9BB1643L, 0xE6C8E580745B826EL, 0x85534A3D0AF6BD00L, 0x42423B139426DA44L,
            0xFCF35CB39470DB7DL, 0x8838EF7D89FD5E75L, 0x332C2C8F9428F116L, 0xDE7ADCB6F9094020L, 0xD52A788B7A38BACDL, 0xA9F25970B7DF4C41L, 0xF1E17BA10A9B3223L, 0xE55C8215D19867DBL,
            0xF35066C1A594D55CL, 0xE1D02F2DFA4E95A3L, 0x8368DD854F21AA89L, 0xEE463DE0B3B040FFL, 0x8809BC3D8767126AL, 0x9888B4CA0604F87CL, 0x6B23745E40C36790L, 0x5AD100BFA4EAA440L,
            0xDDC42968F06BD5D4L, 0x0EB58AD5650FCC99L, 0xAF93EA5B745BB306L, 0x3DF9A703BA92D681L, 0xDB1FCA728CDCA1F4L, 0xC6181560563DCDF4L, 0x7D95DC5ADB730E73L, 0x5A9D65E23F4BCCB2L,
            0xD81B4E22820410C1L, 0x74A29B95F7DAA62AL, 0xDC360353D5F6474AL, 0x51E7DAA6A40260F2L, 0x4E2EA639DF3520D3L, 0xB491ADF64736A776L, 0xD98566027CFC2548L, 0x9C756684019C1012L,
            0xA1FEAB236BA5EC1CL, 0x3CD88B16379D7D08L, 0x707DA045C241B886L, 0xC6DB99EF129C1807L, 0x90D24EE9243A161DL, 0xA2464D58B7C421DBL, 0xA14785E5E679B721L, 0x523FB995A2B2DDA0L,
            0xDC3FB72960467BB2L, 0xE230C7DA702BAAB3L, 0xF32719B80F2003A1L, 0x0B474378EEE5DAEEL, 0xD77FE075835C9C62L, 0x7490BD791DF171D7L, 0x01615CA67556C073L, 0x40207D2EAA38EDECL,
            0xBDBBBF37FE31EABFL, 0xAAF6E3890ADC8902L, 0xFE2C98A350D85E8FL, 0x6EA500DFF5A0F494L, 0x2AD44DAC8806BDA4L, 0x371FF7ADC61984C4L, 0x2D67C3D55315790CL, 0xA47F164AB29EC5AAL,
            0xBC2C63A2A5650ECAL, 0x0B2512F15EC9FDF0L, 0x518B95A30A2E3B29L, 0x58A75361DA739430L, 0x16870A3A56632EA8L, 0xE91D9F763A910910L, 0xCE08AE3825B3B9DFL, 0xCB50BE792BB0334FL,
            0x1D8C8126FDB0E2C0L, 0x68B10E1C266B75F0L, 0xB029BCCA574BFAF4L, 0x02A9CF13E42FE100L, 0xDAAB87A6354947CEL, 0x38EDB52D09436F40L, 0x5E1ED2DB4A032BB3L, 0x732406706848B79CL,
            0xC2C6F78E9FD659D5L, 0x4F46EA23B2A59D6FL, 0x7412D694FEA63995L, 0x2AD486306C8AC739L, 0x4C5FF9E964284F55L, 0x5D08EDE497C62505L, 0x22C72DDC681405FCL, 0x43B57E9A9CA4CDD1L,
            0x8E09990345DFF5BAL, 0x230C6482B198C30DL, 0xE1D797CA1B480D65L, 0x0C5F958F3ACC39BBL, 0xDB6CA881AB431D02L, 0x61574A31B78B2E0EL, 0x1E30FA40D2F90D92L, 0x45D0CC5C6FDA8AEEL,
    }, startingB = {
            0x0000000000000001L, 0x455E0E109C65E5A6L, 0xEA6F8F04B363C882L, 0xA8D4871827A3D3CDL, 0xDFE1AA36B29428EBL, 0x24EDEB911B1B54BFL, 0xBEA13C7B2AD329EEL, 0xCDC764BDCB0378E7L,
            0x279BD0A81B87C66AL, 0xFD113349159EB2F1L, 0x8AC708AC16866222L, 0xF103F79E075B8B54L, 0xC3FD4F2FBCE83B09L, 0xF0A5210661FB395FL, 0x003420A7D182336EL, 0xBACD1E99584649EAL,
            0x61A892E4D85E682EL, 0x1BE2DADB1A7FEA69L, 0xE29A7A12A19E5904L, 0xF40C3CEA5D0D9124L, 0x29194A004AA9B7EFL, 0x3E8984574BEFA523L, 0x0561EFA21874C8E4L, 0xC6561E29E84FE115L,
            0xE5C03A1C7C376450L, 0xB36146ABD08D33BBL, 0xB7548D3B1DDBDD04L, 0x9C76C4DACF3BA742L, 0xABF44840E3CDBD62L, 0xDE36A40D6B3AB608L, 0xB2420BB008CC3B63L, 0xCC2628370888FFA0L,
            0xEBDBCDA6649A717FL, 0xB257532148F2B6ACL, 0xF102ED1796C40366L, 0x8AFC455AB7D634CBL, 0xD85B4DC465D2CB60L, 0x6A9DECC61F2E06F9L, 0x56F540C3C5ED5A6CL, 0xA3299800824B1431L,
            0x397889AD3BAB24EBL, 0x9540948FE3434A59L, 0x4A1DE35BAC4F1666L, 0xC1E2B74BB6C0D8ADL, 0x7E5436FCBDCB5B71L, 0xA6788C252D3FA9AFL, 0xD187F9304317D9FDL, 0xC342F39AC826DCA7L,
            0x4B26B0F851F2CA1FL, 0x019838CAF8B1D0ACL, 0x27313A0568002621L, 0xF2D57756F296DA76L, 0x9DE273D38F81D2CCL, 0x93F5965A7A03C939L, 0x7904CD65E3E87604L, 0x058729A0EFFA2E21L,
            0xD99364D8FDA45655L, 0xEC6E2993A729EC95L, 0x7BA14324C0397BA5L, 0xF1A7B6945B1341A6L, 0x15C249CB81FDB967L, 0xD727098CB28C09EDL, 0xE82E98BD98479F13L, 0x403EF674EDCC14DFL,
            0x90BC3BF2519F7811L, 0xD3DF13D2D2FB810BL, 0xB79BC3C57BBD1787L, 0xA2F77E657B1260A4L, 0xE7CFD7F878D0BA98L, 0x7B19691E0EB94011L, 0xD68E3F0995605AD5L, 0xEEA57DF59BEE051DL,
            0xC29E0AC4FF25FE24L, 0x8325EA1E66BE52CDL, 0x9DB8B6958239896DL, 0x0717A3E610B293FEL, 0x496B7954B646B98AL, 0x732F2581700A4189L, 0x213CD01ED01FC5D5L, 0x6ADFF9DE4403408EL,
            0x05390C907E574558L, 0xE3E399196E282167L, 0x4866D3842FC94CD9L, 0x5D09493B20CC4B37L, 0xFD7B1BDE42B449D1L, 0xEEBFEB2B45A5E232L, 0xC650AFC2D9D3E673L, 0x45DB07A2956ADDD8L,
            0x57064975A144DB8DL, 0xADB0185B4FE803DCL, 0x724F3400AD070184L, 0xC76820BC8592A42AL, 0xC48B37945C483322L, 0x1FA560F4ABBD89CDL, 0x66CEAAA5F9DC0547L, 0x7F9B0EADC133F35FL,
            0x57550759086478D9L, 0x09733BA3DD99CA71L, 0x368AA7716FA73C8DL, 0xC9EC5919FC3A7986L, 0x393444DBBDC22896L, 0x27CD96264EE89EF5L, 0x6D69BEE759072BDCL, 0xA17B5598B2F84070L,
            0x2A25740C352995A9L, 0xC6E3DDBD4E9BA68AL, 0xBA0C72348F13C037L, 0xF92B9565DF441DEBL, 0xF4DC9550EB3D1FF2L, 0x63AEAD81EACCD91CL, 0x6C33B52BD273E875L, 0x94F1EAF8BD2A68C2L,
            0xE108685A36A8E258L, 0x6C244244C12B241AL, 0xE689E3DC9D7D0610L, 0xFF6E5638B5A5B426L, 0x5AC11CB7F0D8DEAFL, 0xDEC5A4466C79CECAL, 0xC2A1567E06385BEDL, 0xBE8CE0268485F93DL,
            0xE418ACC9398291E0L, 0x14621413FE5F97CDL, 0xC9649FEBC1C311D3L, 0x47A0337F14523B8CL, 0xB345F309CC27BCC5L, 0x0D466C7F77971649L, 0x04B2267678FBFBFDL, 0x10ACC2FC15FFD21DL,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA *= 0x41C64E6BL;
            stateA = (stateA << 18 | stateA >>> 46);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB *= 0x9E3779B97F4A7C15L;
            stateB = (stateB << 47 | stateB >>> 17);
        }
    }

    public final int nextInt()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B97F4A7C15L;
        return (int)((stateA = (a << 18 | a >>> 46)) ^ (stateB = (b << 47 | b >>> 17)));
    }
    @Override
    public final int next(final int bits)
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B97F4A7C15L;
        return (int)((stateA = (a << 18 | a >>> 46)) ^ (stateB = (b << 47 | b >>> 17))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B97F4A7C15L;
        return (stateA = (a << 18 | a >>> 46)) ^ (stateB = (b << 47 | b >>> 17));
    }

    /**
     * Produces a copy of this Mover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover64RNG
     */
    @Override
    public Mover64RNG copy() {
        return new Mover64RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Mover64RNG()}, {@link #Mover64RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "A" part of the state, an int
     */
    public long getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state; if this generator was set with {@link #Mover64RNG()}, {@link #Mover64RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "B" part of the state, an int
     */
    public long getStateB()
    {
        return stateB;
    }
    /**
     * Sets the "A" part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int
     */
    public void setStateA(final long stateA)
    {
        this.stateA = stateA;
    }

    /**
     * Sets the "B" part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateB any int
     */
    public void setStateB(final long stateB)
    {
        this.stateB = stateB;
    }
    
    @Override
    public String toString() {
        return "Mover64RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mover64RNG mover64RNG = (Mover64RNG) o;

        return stateA == mover64RNG.stateA && stateB == mover64RNG.stateB;
    }

    @Override
    public int hashCode() { 
        long h = 31L * stateA + stateB;
        return (int)(h ^ h >>> 32);
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
        long stateA = 1, stateB = 1;
        System.out.println("long[] startingA = {");
        for (int ctr = 0; ctr < 128; ctr++) {
            System.out.printf("0x%016XL, ", stateA);
            if((ctr & 7) == 7)
                System.out.println();
            for (int i = 0; i < 512; i++) {
                stateA *= 0x41C64E6BL;
                stateA = (stateA << 18 | stateA >>> 46);
            }
        }
        System.out.println("}, startingB = {");
        for (int ctr = 0; ctr < 128; ctr++) {
            System.out.printf("0x%016XL, ", stateB);
            if((ctr & 7) == 7)
                System.out.println();
            for (int i = 0; i < 512; i++) {
                stateB *= 0x9E3779B97F4A7C15L;
                stateB = (stateB << 47 | stateB >>> 17);
            }
        }
        System.out.println("};");
    }
}
