package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>
 * (specifically, an un-investigated generator mentioned in the code attached to the article), a lera^lera^lera with
 * three 32-bit states; it has high quality, passing PractRand's 32TB battery with at one anomaly, uses no
 * multiplication in its core operations, and is optimized for GWT. It has a period of somewhat under 2 to the 96,
 * 0xF8DB896393AF9CD31D8B69BA, which is roughly 2 to the 95.959176, and allows 2 to the 32 initial seeds. It is not
 * especially fast on desktop JVMs, but may do better on GWT in some browsers because it avoids multiplication. You may
 * want {@link Mover32RNG} for a faster "chaotic" generator that does well in 32-bit environments, though its period is
 * certainly shorter at roughly 2 to the 63.999691.
 * <br>
 * This seems to do well in PractRand testing (32 TB passed), but this is not a generator Overton tested. "Chaotic"
 * generators like this one tend to score well in PractRand, but it isn't clear if they will fail other tests (the
 * fairly-high period means that it is likely to produce most long values at some point, though possibly not all, and
 * definitely not with equal likelihood).
 * <br>
 * Its period is 0xF8DB896393AF9CD31D8B69BA for the largest cycle, which it always initializes into if
 * {@link #setState(int)} is used. setState() only allows 2 to the 32 starting states, but many more states are possible
 * if the generator runs for long enough. The generator has three similar parts, each updated without needing to read
 * from another part. Each is a 32-bit LERA generator, which adds together the current state left-shifted by a constant
 * amount and the current state rotated by another constant, and stores that as the next state. The particular constants
 * used here were found by exhaustively searching all possible combinations of left-shift added to a rotation; the three
 * selected LERA generators have period lengths that are coprime and are the longest three periods with that property.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it has 96 bits of state. It uses three generators
 * with different cycle lengths, and skips at most 65536 times into each generator's cycle independently when seeding.
 * It uses constants to store 128 known midpoints for each generator, which ensures it calculates an advance for each
 * generator at most 511 times. 
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and the "lera" type of generator
 * this uses, which Overton found and noted but hadn't yet investigated. Some species of mole-rats are especially social
 * animals and need to work in groups to thrive, much like how one or two lera generators aren't enough to pass tests,
 * but three will do quite well when combined.
 * <br>
 * Created by Tommy Ettinger on 11/23/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class Molerat32RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    private int stateA, stateB, stateC;
    public Molerat32RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Molerat32RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Molerat32RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Molerat32RNG(final int stateA, final int stateB, final int stateC)
    {
        this.stateA = stateA == 0 ? 1 : stateA;
        this.stateB = stateB == 0 ? 1 : stateB;
        this.stateC = stateC == 0 ? 1 : stateC;
    }

    private static final int[] startingA = {
            0x7E7A773E, 0x278C3D74, 0x3F1DE6F5, 0x0C581399, 0x414F083A, 0x74211C70, 0x42203026, 0x0724F7DB,
            0x42C95E0C, 0xAEC08514, 0x30FE969A, 0xDC76E210, 0x2695EE44, 0x1CEC3584, 0x58EB49F1, 0x5162DBA6,
            0x9A1ECDB2, 0x6CF2DCF3, 0xA52A2CD5, 0x78DD9006, 0x0E16D9BB, 0xEDCBE912, 0x4A262BE5, 0x1C6AE304,
            0xAB094E45, 0xB5258CA8, 0xAA45EBFA, 0x499F9D80, 0x52F48F2D, 0x0AD1CF16, 0x9276A067, 0x02F98701,
            0xAFCA8D8B, 0xE880DC36, 0x72868424, 0x58B91703, 0x6C596A1B, 0x5B612409, 0xA4C28B07, 0x9B683C0F,
            0xCAC56556, 0xC6329F6F, 0xFCE3D058, 0x41B790CA, 0xA441288B, 0xF0437135, 0x5B54B564, 0x2512DF84,
            0x755896B2, 0x59BF4FE6, 0xCAD40046, 0xAA5425D1, 0x3C95CE27, 0xC6F5B3D9, 0x9ED089C4, 0x2835D51F,
            0x31D1BB97, 0x64D44BC2, 0xADB98517, 0x1EFCF0BF, 0xA6E97356, 0x68EE8F8A, 0x6121C448, 0x63E4755D,
            0xB9F92D37, 0xC18BDDF7, 0x4649E686, 0xFE0DF92E, 0x1EFD5CEC, 0xE1838727, 0xF5C77CE5, 0x3291666E,
            0x07096AFC, 0x1B96EDEC, 0xD9464D89, 0xC433196D, 0x2678D3FB, 0x4F0A5B4F, 0xD2071036, 0x790F31D2,
            0xE6D16700, 0xF2EB1862, 0xA4C9417A, 0x319116D0, 0x853F9F2F, 0x4FB8C94E, 0xF5E26F09, 0x250996DC,
            0x9EBC8D9A, 0x245A4C99, 0xB555C221, 0x72F23EF0, 0x84BBE96D, 0xDB0A2348, 0xE63297B0, 0x04FBB001,
            0xD088B549, 0xA71D7895, 0x4C3B5598, 0x87B0B80F, 0x613DD55A, 0xFD5CABE0, 0xF43E0219, 0x3983FE43,
            0x8ABB4A1F, 0x2FC8556A, 0x3D167F14, 0x19CA2125, 0xB3DC2407, 0x8277A7B9, 0xD3228CE7, 0xA12CE097,
            0xB866E4B9, 0xB08E7C46, 0x56E44B12, 0x5922D76E, 0x6395D7AF, 0x52459BA9, 0x2F417A87, 0x7C61E1C9,
            0x2C90106C, 0x7CABE916, 0x1DBA3BAE, 0xCB9503C4, 0x6F14E967, 0x13FEAB39, 0x3A310CED, 0xD7423A2F,
    }, startingB = {
            0x90D7C575, 0xC8234ADB, 0xBCF5CC6A, 0x2786854F, 0x11D961DC, 0x89BFBF62, 0x02D30705, 0x01EB0E17,
            0x440A8BE4, 0xA2ABC105, 0x4F7F3723, 0xD9E7F475, 0x69EAC2D2, 0xD28CC26F, 0x51372436, 0xA19B19C3,
            0x73B52150, 0xFB8630B6, 0xC10B5FD1, 0x9F598DE7, 0x07DF4FEA, 0x822F5B8F, 0x6F66D5F8, 0x569C0D65,
            0x717E5F48, 0xFCAF67A7, 0x4DB18F99, 0x81A0C864, 0x97FA2990, 0xDFCDD00C, 0x097C1093, 0x5B4B08E1,
            0x38564F43, 0xBE4C4595, 0x9A158F54, 0x4FA96613, 0x133A5ACA, 0x0B4687B9, 0x1C78AB16, 0x9CF6B047,
            0x4CC61CC9, 0x5CA71348, 0x428C07A1, 0xD97DDFAC, 0x7B422BA5, 0xF100CA05, 0x07B7E271, 0xAB036BC7,
            0x43736C2B, 0x3373EF92, 0xA4017CB1, 0x82AD92F9, 0x85BEBEF7, 0x63C5B357, 0xACC98C93, 0x186B8003,
            0xD96FA7FA, 0x22A5B1DC, 0x6F965545, 0x4D14096B, 0x30CCBAF0, 0xDE58A0A7, 0x1BCBB0A5, 0xAF878228,
            0x5483DB7E, 0xF3E0C1E8, 0xD64D3ECB, 0xED5CD1BA, 0x92E0AC26, 0x467CF6CE, 0x31AFE688, 0x151A7D50,
            0x5AB0FE0D, 0x495FF933, 0x39E2FE6A, 0xB52C6EA3, 0xD8879EE8, 0x2CADB7E0, 0x621EAAC2, 0x716884F5,
            0xE9EF714A, 0x79E0C4E6, 0x880659B8, 0xB1964FC1, 0xA7899261, 0xB69EAB1A, 0x5D856906, 0xE5E8FA44,
            0x33D54582, 0x0C40C021, 0x933BF6BF, 0x88639094, 0xDA61A98B, 0xBB5F1211, 0xA8333A75, 0x99C5F20A,
            0x0823C2D7, 0x27394C15, 0x897C2CF6, 0xF12B7787, 0xE6EAA1F1, 0xC4876536, 0xE4F3BDCD, 0x92562433,
            0x4EE84624, 0xE89BEC88, 0x57D89B30, 0xC4CBAB76, 0x38DACE13, 0x886C4DD2, 0xC2B69665, 0xECC5B3E4,
            0x607F1FEC, 0xC498CAFF, 0xCE5CB3CE, 0x3CD0CCDF, 0xFF315E5A, 0xDCDD4968, 0x137427C6, 0x6FB2D50E,
            0x7338E7CD, 0xFC94439D, 0x6F3557F7, 0x3EDC3154, 0xFD576DA6, 0xC44AD5C7, 0xE8E88178, 0x34DE4D5A,
    }, startingC = {
            0x52CC878D, 0x4F1B13A2, 0xC3A42350, 0xC6E34D4E, 0x47161468, 0xC8B6B338, 0x6B7E278D, 0x6730BB1E,
            0xCD6E54C6, 0x71F94D24, 0x88543159, 0x8772668A, 0x72BBB3DD, 0xEBCDBCFE, 0xF9B9594B, 0x382FADB2,
            0x83E04D9F, 0x6E3E9ECC, 0x1CA5B60F, 0xE8701491, 0x8F881C35, 0x82889773, 0xFF49A3DD, 0x09736772,
            0x35051411, 0xC81938C6, 0x1D7A8447, 0x38699825, 0x389DAEFF, 0x8D6A9E48, 0x76266923, 0x7A5364BD,
            0x2C626D0A, 0x03FEF32F, 0x07BC6CDB, 0xAC8FEEBA, 0x146EC179, 0x71808FCE, 0x7EA044A7, 0xA669FA5C,
            0x3195791D, 0x721363D2, 0x68ED3340, 0x4054A402, 0x82FF33DC, 0xBD6D42FD, 0x2190916B, 0xB122BF9B,
            0x77CE02A4, 0xBBB02870, 0x3201F65D, 0x81839EE4, 0x0D276034, 0x1241DC88, 0x434AF4FE, 0xA2B53264,
            0xCE986D9E, 0xB64729D0, 0x37672F8A, 0xC3AE3AEC, 0xCC720AFC, 0x4F2A7F61, 0x9AA1B273, 0xCB022734,
            0xC5036E47, 0xA341BB1D, 0x737D966E, 0xF9891AE6, 0x5051BC0C, 0xAB424333, 0xB401D6D0, 0x6FF4C8E5,
            0x1E3547DB, 0x7EB85B90, 0x39DED179, 0xD74AD860, 0xCD6BDD8B, 0xAD3BE931, 0x4518795B, 0x12343B29,
            0x506F12EE, 0xD3058D31, 0xF6A5F567, 0xA15EF9E5, 0xA9230AD3, 0x3D9D0241, 0x0ED724A2, 0xD5888068,
            0xEA50F71B, 0x1F905E52, 0x5D2068A4, 0xB171D763, 0x1D612B8D, 0x2F333EBD, 0x0503454E, 0xE2089897,
            0xCB843788, 0x049C6535, 0xB3B04C17, 0x74859310, 0x0DE84BD0, 0xF4888A64, 0x96439BB8, 0x8054FC18,
            0x0C9EF5DF, 0x2F745EA5, 0x22FB9C1C, 0x42097C3B, 0x961266B3, 0xC898BDDC, 0xD1352D55, 0x6E35C940,
            0x58A97AEE, 0xECD14F5A, 0xC8D1DDE9, 0x667CAE9A, 0x50F5976F, 0x527E9ACA, 0xB356FCB0, 0xC751469A,
            0xF7618145, 0x092930BD, 0xFB26DCD8, 0xDE9A1D14, 0xB48C5650, 0x5F35DAB4, 0x26712B87, 0xAB0453E0,
    };


    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
        }
        stateB = startingB[s >>> 17 & 0x7F];
        for (int i = s >>> 8 & 0x1FF; i > 0; i--) {
            stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
        }
        stateC = startingC[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
        }
    }

    public final int nextInt()
    {
        stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
        stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
        stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
        return stateA ^ stateB ^ stateC;
    }
    @Override
    public final int next(final int bits)
    {
        stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
        stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
        stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
        return (stateA ^ stateB ^ stateC) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
        stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
        stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
        final long t = stateA ^ stateB ^ stateC;
        stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
        stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
        stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
        return t << 32 ^ stateA ^ stateB ^ stateC;
    }

    /**
     * Produces a copy of this Mover32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover32RNG
     */
    @Override
    public Molerat32RNG copy() {
        return new Molerat32RNG(stateA, stateB, stateC);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Molerat32RNG()},
     * {@link #Molerat32RNG(int)}, or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the "A" part of the state, an int
     */
    public int getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Molerat32RNG()},
     * {@link #Molerat32RNG(int)}, or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the "B" part of the state, an int
     */
    public int getStateB()
    {
        return stateB;
    }
    /**
     * Gets the "C" part of the state; if this generator was set with {@link #Molerat32RNG()},
     * {@link #Molerat32RNG(int)}, or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the "C" part of the state, an int
     */
    public int getStateC()
    {
        return stateC;
    }
    /**
     * Sets the "A" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int except 0, which this changes to 1
     */
    public void setStateA(final int stateA)
    {
        this.stateA = stateA == 0 ? 1 : stateA;
    }

    /**
     * Sets the "B" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateB any int except 0, which this changes to 1
     */
    public void setStateB(final int stateB)
    {
        this.stateB = stateB == 0 ? 1 : stateB;
    }
    /**
     * Sets the "C" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateC any int except 0, which this changes to 1
     */
    public void setStateC(final int stateC)
    {
        this.stateC = stateC == 0 ? 1 : stateC;
    }
    
    @Override
    public String toString() {
        return "Mover32RNG with stateA 0x" + StringKit.hex(stateA) + ", stateB 0x" + StringKit.hex(stateB)
                + ", and stateC 0x" + StringKit.hex(stateC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Molerat32RNG molerat32RNG = (Molerat32RNG) o;

        return stateA == molerat32RNG.stateA && stateB == molerat32RNG.stateB && stateC == molerat32RNG.stateC;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * stateA + stateB) + stateC | 0;
    }
    
//    public static void main(String[] args)
//    {
//        int stateA = 0x90D7C575, stateB = 0x7E7A773E, stateC = 0x52CC878D;
//        System.out.println("int[] startingA = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%08X, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateA = (stateA << 9) + (stateA << 8 | stateA >>> 24);
//            }
//        }
//        System.out.println("}, startingB = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%08X, ", stateB);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateB = (stateB << 5) + (stateB << 1 | stateB >>> 31);
//            }
//        }
//        System.out.println("}, startingC = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%08X, ", stateC);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateC = (stateC << 27) + (stateC << 20 | stateC >>> 12);
//            }
//        }
//        System.out.println("};");
//    }
}
