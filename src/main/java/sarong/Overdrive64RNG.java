package sarong;

import sarong.util.StringKit;

/**
 * Experimental performance adjustments to the type of algorithm used by {@link Mover64RNG}; the version in
 * Mover64RNG is no worse than this version and is probably faster.
 * <br>
 * Created by Tommy Ettinger on 9/13/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class Overdrive64RNG implements RandomnessSource {
    private long stateA, stateB;
    public Overdrive64RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Overdrive64RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Overdrive64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Overdrive64RNG(final long stateA, final long stateB)
    {
        this.stateA = stateA == 0L ? 1L : stateA;
        this.stateB = stateB == 0L ? 1L : stateB;
    }

    private static final long[] startingA = {
            0x0000000000000001L, 0xAB4B86FAC98F5946L, 0xE94BF30D8F93ADE4L, 0xE3E5A75074A90FB1L, 0xBABDFC5953B6AA53L, 0xA483200ADC30E766L, 0x3746FD6094127865L, 0x30BB80016211751DL,
            0x2C3EDFCEE477D6BCL, 0x21EB5D446136AEC4L, 0xEB3C9BF07A12F31EL, 0x40158B39CE5FA6F6L, 0xCEA8B2229183E410L, 0x951AA921E5DDFB46L, 0x281C93EBF836F734L, 0xABC02AF239EF3524L,
            0x64ACDFF29A81FE32L, 0x44FC22190D8DDE4FL, 0x0AB43934FDC52B67L, 0x85A0A0A6730100D7L, 0xD3028C2F8DC45483L, 0xE8483FEC7C8719C1L, 0xFBF4CE1AC25B6398L, 0x0DB586BF74A03FE7L,
            0x79624473D0CAAE28L, 0xDD5761788AC3A5E5L, 0xDB0475AC8FD1514CL, 0xF0E1D5FA65BE052BL, 0xA0D1CA984EC2AC8AL, 0x04DCB07EE5F5402FL, 0xC6DBABC10065A826L, 0x0DEE33AC2082A22DL,
            0xE681FB2371897F8FL, 0x69708CB507A814A3L, 0xF50CCDF7BCFF4447L, 0x0373259E44D308B9L, 0x716E060F8AB362A1L, 0xC40EA0A9BB7C755BL, 0xD690B800835E2145L, 0x53C524939BA7A280L,
            0x94444CA5CC6213A1L, 0xE7784562CDDCC324L, 0x7381164CC395341EL, 0x86F6BBA6775F6273L, 0x9F4D21442D0408C4L, 0xC2EC1847FE67329AL, 0xD482EB07BA9DA67BL, 0x4C81937B325676BBL,
            0xDAE00B6810189F36L, 0x4262EA453D235A0FL, 0xF9F7014479F1CA84L, 0x8C3D0B9038443412L, 0x33DA7ADCD08F9332L, 0xF99573ADF7D6751EL, 0x054AFEF725378095L, 0xAAB44A08C7898819L,
            0x641760C36A6B25E1L, 0xEFC6E83CF9B59C0DL, 0xEAB0667BD7EC8C00L, 0x5CC284C04BA870E1L, 0x3C134AC71E20AC8AL, 0x0D1F9C915AFE3200L, 0x84B793D0420932A1L, 0x2C170AEF1B849D75L,
            0x5CB11E977ABD8185L, 0xB704B61C7C6037EFL, 0xD91CC8A6B0AA641AL, 0xED25A71118692194L, 0x86DE3CAF2C17192AL, 0x2311F0646C89A0ACL, 0xE5BF708491D60613L, 0x32A48ED0FEB30DE8L,
            0xEE7896E9D373344EL, 0xA212A4341DDA222BL, 0x8BEE83C2F2AEC6E7L, 0x0FF6C26FD2F4F70CL, 0x04484D57BE78E15AL, 0x5186248532A87354L, 0xE1EB0614230FCA94L, 0x7BBD32331272D656L,
            0x3496359841D6002FL, 0xBF1078F67E2AE790L, 0xBE058EEFBAE81CD5L, 0x6662FF092C5CF915L, 0xE47696A5369C0600L, 0x3F51C509BAE169FAL, 0x611A4AF36C15F398L, 0x67518DA5F1609E74L,
            0x87293331F0C82AD5L, 0x18A6167E9AAAF7A3L, 0x091F161B501EFAA7L, 0xB5E8D6BA28A329CCL, 0xEC577D9F73B6D2C6L, 0xA656DB2060ED1ADAL, 0x3A2ADEF689374517L, 0x0CF85191C7B3659BL,
            0xA6C31F380971C5F0L, 0xAA2910D6FA0910BAL, 0x23DF640039F0E684L, 0x19EBC74C1A66484FL, 0xA13CB90148E6F5C0L, 0xE2534D11A096DE91L, 0x79E9662C275EBF89L, 0xCCDA30AA6EA8D298L,
            0xC9A83B312DD9DB29L, 0xCE20DB18EBB87DB4L, 0xE1F6F7370E6DA85FL, 0xB3E68F53DAC7A4E5L, 0x78CF23EDE8AE5949L, 0x64502235879E9309L, 0x7A7C9D9B883ECC2DL, 0xD9CD53FBD5A48D34L,
            0xDC2E43803924FCEEL, 0xC6705B105ED18232L, 0x7CACBE3C06E17A99L, 0x7562A88DAE049EDCL, 0x21B4F7F0F68578E6L, 0x2263C9450AAB36CFL, 0x30B26B25C24D890AL, 0x7A9EEB7219CF9C95L,
            0x0F2517A7E3ED4072L, 0xD4B3DCD448503233L, 0x7C3C98C1B005EF3BL, 0xD0D86FC1AD283FE3L, 0x728E9C2672057265L, 0xFD9AB263FA245D86L, 0x7B65CF1B9E7D5926L, 0x31B78649473EFF4EL,
    }, startingB = {
            0x0000000000000001L, 0x07293E6E09EC6368L, 0x96D969CADB4CD368L, 0x3FF86768F89EAEB3L, 0x2F9FAC39CC8E5CB7L, 0xF0ACF2D0542EE141L, 0x7BF403A079DCD087L, 0xDA68703F5EAB9409L,
            0xF887EDE8E8AD388BL, 0xB93108A12DD8DC5CL, 0x98676A8BE90BB48EL, 0x3C66E22B602A7007L, 0x69A56A92BAD39B5BL, 0x58857B966DDF07BCL, 0x3B6890E3EDB96D6EL, 0xF0363B595221C86DL,
            0x62EE3C3A7A528614L, 0xB0175247E00B4935L, 0xE70D810777ED42ACL, 0x275CE4F27473631AL, 0xB5DF57C4502967E9L, 0xB8EB0B9EC111C7FEL, 0xC28F3B422CA03689L, 0xD09DD3A8FEAB2DD1L,
            0x4E2C713B5A7A0FFCL, 0x9AFC4BE99ED5F1AAL, 0xA89BFD2F6C2E97AEL, 0xF8735B9A6DF5F258L, 0xB2F89E533D9B9897L, 0xD89711EA7777E671L, 0x9658217AF4F448CAL, 0xEE3F474204385F6BL,
            0x2B20D085EAB7ECC0L, 0xDF4FBDB5877EC70AL, 0xA27D970C88F1246BL, 0x88D0B336E63ADE23L, 0xC06AF42B0855C181L, 0x00E8B464987358DBL, 0xDA1DF8BA1E45586EL, 0x4C12347AB35D2F03L,
            0x752C4942F1095640L, 0x608BD5FC9E04FA0AL, 0xB253E48775CDD5E1L, 0x643E8401460AAA59L, 0xE248C00B3A622F06L, 0xB01AD54DFE588BF2L, 0x1D486285F47A99D0L, 0x4ACE70E9A24E7B42L,
            0xE498314246C2E894L, 0x67BB0785AEA67873L, 0xAB50922AD5171ABDL, 0x4BFA6DEE10549DC3L, 0x889BB7C03B745D65L, 0x705D68BC7379AEECL, 0x08BC6282C82C8B72L, 0xB967A84918604EDCL,
            0x17F2AE6E78487967L, 0x038874F2D394FB80L, 0x7F7A2F1A581C66D3L, 0x99977A67381F6F7FL, 0x6B62915A4927F8D3L, 0x4DE18BB59A3C182DL, 0x94E508A682455109L, 0x986BE18241462557L,
            0x0578DFAE00F8A0D6L, 0x29B22988B2264886L, 0xD552345E6E2A3125L, 0x5DB9E3195164C051L, 0x0E43BA334827D573L, 0x3AFAF8799E87209CL, 0xBC0E249E28B42DE9L, 0x022A07577137E25FL,
            0x7DEB553C69DAA1FFL, 0x4A69C3A72EF45E41L, 0xBEBAC3CF3B608398L, 0xEB5771FF214E2487L, 0x9FB5E8C5B36B4CC9L, 0xC09F95341A44B518L, 0x668BEA20B4AE0875L, 0x633E56557743D5CBL,
            0x60F91113C85EAAAAL, 0xB7FD377C14A36222L, 0xFCF5360544E39E14L, 0xC8201F79E019A016L, 0x9298BE81EFD5200FL, 0xBEB6A71A91068F67L, 0xB48125BFEFE20180L, 0xC470152566C3E1A0L,
            0x46646F5388059BA1L, 0x6B2EFA0363CEB524L, 0xC60186015E2573E1L, 0x514BF9772FA2ACCEL, 0x1C44DACDE62A44EDL, 0x0CC4356D150B5469L, 0xDF21F9DAE98D5C86L, 0xA22573A5D741ACECL,
            0x722CB87504029D8AL, 0x5727EA9D310F90F7L, 0x06D1E7DC6CF5C689L, 0x735BAEB75FDD9F85L, 0xEF96C3AF03785BEAL, 0xBE453FC733BEFA00L, 0xE27E2672BFCC1C44L, 0x541C5523E0FBB038L,
            0xA04B840944E17E54L, 0x313CA18B6537B063L, 0xC7B93061D18C2FFEL, 0xE1D991D2E4A8CD20L, 0x5BB21B4ED59FAE91L, 0x7DB82C96F57D18C5L, 0x9EEBA39CBD611F6EL, 0x093E9402ABCC23F7L,
            0x9A7637252A4475F7L, 0x0C8A522F0B70DB19L, 0x3532D24B07A4D08BL, 0x633C908FA64BB58AL, 0x16A3123AD6B3DD79L, 0x1169BB0D6BD6DEC5L, 0xDABFB787CED62E83L, 0x8F17A15C52A3B9BDL,
            0xA2F3FA0F0F5F6FDFL, 0x95DA83EA34697FEFL, 0xFE1541E512CBAC77L, 0xE68287CDEB9302A5L, 0xB928A0223B695207L, 0x3F9D05B291DE5A8AL, 0x5E28B275895A2C79L, 0x8E9BD22FBFD57A6CL,
    };
    
    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA *= 0x41C64E6BL;
            stateA = (stateA << 26 | stateA >>> 38);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB *= 0x9E3779B9L;
            stateB = (stateB << 37 | stateB >>> 27);
        }
    }

    public final int nextInt()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (int)((stateA = (a << 26 | a >>> 38)) + (stateB = (b << 37 | b >>> 27)));
    }
    @Override
    public final int next(final int bits)
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (int)((stateA = (a << 26 | a >>> 38)) + (stateB = (b << 37 | b >>> 27))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        //0x9E3779B97F4A7C15L
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (stateA = (a << 26 | a >>> 38)) ^ (stateB = (b << 37 | b >>> 27));
    }
    public final long nextLong2()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (stateA = (a << 23 | a >>> 41)) ^ (stateB = (b << 42 | b >>> 22));
    }

    public final long nextLong3()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (stateA = (a << 26 | a >>> 38)) ^ (stateB = (b << 37 | b >>> 27));
//        return (stateA = (a << 23 | a >>> 41)) ^ (stateB = (b << 42 | b >>> 22));
    }

    public final long nextLong4()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x7FFFFFFFL;
        return (stateA = (a << 23 | a >>> 41)) ^ (stateB = (b << 42 | b >>> 22));
    }

    /**
     * Produces a copy of this Overdrive64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Overdrive64RNG
     */
    @Override
    public Overdrive64RNG copy() {
        return new Overdrive64RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Overdrive64RNG()}, {@link #Overdrive64RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "A" part of the state, an int
     */
    public long getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state; if this generator was set with {@link #Overdrive64RNG()}, {@link #Overdrive64RNG(int)},
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
        return "Overdrive64RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Overdrive64RNG overdrive64RNG = (Overdrive64RNG) o;

        return stateA == overdrive64RNG.stateA && stateB == overdrive64RNG.stateB;
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
                stateA = (stateA << 26 | stateA >>> 38);
            }
        }
        System.out.println("}, startingB = {");
        for (int ctr = 0; ctr < 128; ctr++) {
            System.out.printf("0x%016XL, ", stateB);
            if((ctr & 7) == 7)
                System.out.println();
            for (int i = 0; i < 512; i++) {
//                stateB += 0x9E3779B97F4A7C15L;
                stateB *= 0x9E3779B9L;
                stateB = (stateB << 37 | stateB >>> 27);
            }
        }
        System.out.println("};");
    }
}
