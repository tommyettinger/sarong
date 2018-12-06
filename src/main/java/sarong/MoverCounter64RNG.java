package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a CMR with a 64-bit state, that has its result multiplied by 127 and added to a running sum that is
 * returned. The period of the full generator depends on the period of the CMR and what the sum of all results of the
 * CMR is modulo 2 to the 64. Determining the CMR's period would require generating an unknown but probably very high
 * amount of random values until the generator cycles (more than 2 to the 42, less than 2 to the 64). The actual period
 * of the full generator is probably much higher than the CMR's period, unless the sum of all results of the CMR is 0
 * (then the actual period would be equal to the period of the CMR). It passes at least 16TB of PractRand 0.94 testing
 * with one minor anomaly (tests are ongoing). It probably won't pass many tests when the bits are reversed, so that is
 * something to be aware of. Unlike {@link MiniMover64RNG}, which this is based on, it can return 0, but like
 * MiniMover64RNG it cannot return the same number twice in a row. It is very, very fast, usually benchmarking in
 * second-place behind MiniMover64RNG (faster than {@link ThrustAltRNG} in third place).
 * <br>
 * The choice of constants for the multipliers and for the rotation needs to be carefully verified; earlier choices came
 * close to failing PractRand at 8TB (and were worsening, so were likely to fail at 16TB), but this set of constants has
 * higher quality in testing. For transparency, the constants used are the state multiplier 0x9E3779B9L, which is 2 to
 * the 32 divided by the golden ratio, a left rotation constant of 21, which was chosen because it is slightly smaller
 * than 1/3 of 64 (that seems to work well in a 64-bit CMR generator), and a multiplier applied to the CMR's output of
 * 127L or 0x7FL (which offers high-enough quality while still being optimized by the JIT compiler into a right shift
 * and a subtract operation, or possibly a more specialized instruction). MiniMover64RNG uses a much larger multiplier
 * on the output (0x41C64E6DL), but because this sums those multiplied outputs, it can get by with a smaller and faster
 * multiplier for that section.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. This generator, unlike its relatives that also use CMR generators, does not partially
 * skip into the CMR sequence, and instead relies on storing only 256 very-distant points in the CMR sequence, using the
 * bulk of a given seed to affect the counter instead, which has 2 to the 64 valid values that can be accessed without
 * any skipping required.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed. This generator has simpler seeding than {@link MiniMover64RNG} or
 * {@link Mover64RNG}, and has a higher known period (and probably a higher actual period) than either, plus is even
 * more robust in PractRand testing, but is sometimes slightly slower than MiniMover64RNG.
 * <br>
 * Created by Tommy Ettinger on 11/26/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class MoverCounter64RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    private long state, counter;
    public MoverCounter64RNG()
    {
        seed((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * The recommended constructor when you want a deterministic seed; all bits of state will be used in different ways.
     * @param seed the 64-bit long to use as a seed; it won't be used exactly for state or counter
     */
    public MoverCounter64RNG(final long seed)
    {
        seed(seed);
    }

    /**
     * Not advised for external use; prefer {@link #MoverCounter64RNG(long)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param state the state to use, exactly unless it is 0 (then this uses 1)
     * @param counter the counter to use, exactly
     */
    public MoverCounter64RNG(final long state, final long counter)
    {
        this.state = state == 0L ? 1L : state;
        this.counter = counter;
    }

    private static final long[] starting = {
            0x000000009E3779B9L, 0x58B2225F22F80E4BL, 0x0446DE23BB8CB353L, 0x19042A80ABA3A27FL, 0x8350AADA1DF4F932L, 0xB2947185EC6C48F8L, 0x6EE3397507333B50L, 0xA2802EAC6BC3F7FFL,
            0xA100890B88B3B3A5L, 0xFB712A13214385C3L, 0xE52631BCC180D74CL, 0x82FC2AED8EBFEC3BL, 0x54D75AD2D7D04627L, 0xD0D25B5F911DA084L, 0x07F46B557526F335L, 0x91A08CA4656129CDL,
            0xD79EBA35F4B52D3AL, 0x20645DC0C2D44217L, 0xDDD882582BF5E7B6L, 0x5427C7EF791E4E9BL, 0x08A950FD48DE5112L, 0x40FDFD3B052182FCL, 0x7488A26EA729BEC7L, 0xE00CAAB04AEAC3A1L,
            0xD256F44BB2F6F6EBL, 0xD381375ACD30BA4BL, 0x77BF33AEB2068384L, 0xD5F0CB2E8263B366L, 0xC1D12EA91BB93AB8L, 0xF71DD56FA5302124L, 0x4A9E7972DE17FE8CL, 0xA717E584C10806CBL,
            0x309F569022F743B5L, 0x26D71A9DE974372DL, 0x1A02C580384FB4C8L, 0xA37A84C50A138E93L, 0x1503FC0AC675422FL, 0x976228DC0F991A96L, 0xB24060AA7524EF1CL, 0xB876FD7EA0AA0D36L,
            0x6A500AB550746FD0L, 0x360B157F82CAA6E1L, 0xDA212A58211FC682L, 0x26805E6DE5734649L, 0x0D970D13FDA5F421L, 0x4D556F20CA4F24B6L, 0x37DECC131A90C947L, 0x15C4A69FF13D89B5L,
            0x8DDC1A5E8C4A4B56L, 0xF6277B58F81F5893L, 0x4670CBF39A00AD45L, 0xDE444A8F7F235E7DL, 0x3B64290ED00E1FE0L, 0x43DBA78BE42EFE5EL, 0x3C361A4CFFDAD281L, 0x8172A204456DC623L,
            0xC1F512E968BD8399L, 0xFB9B3746B6CAD183L, 0x06B002A94627F0C2L, 0x375592C9A3327411L, 0x592B3014172D0C79L, 0x4D0BB3B6EE0890B4L, 0x39C0DFAB7E1C165FL, 0x214777587C46949FL,
            0xCA5FB5B394E53B04L, 0xCDBB3AED60ACA022L, 0xB67DC0203B71735DL, 0x016F429A7CCBAFFBL, 0x5652E746B46ABE4CL, 0x0CF8896AEBC2ECC1L, 0xB4590D19D85AE89FL, 0x51AF6E54AFCA54BCL,
            0x6233D02F24C8E86EL, 0xF9B7F17E0536DBA4L, 0xAC85B93F77FB255FL, 0x68855B1B595D6218L, 0xC3F305008526BE29L, 0x30F43E214ECD9B68L, 0xA70ACEBC74EC38BEL, 0x4EF664A9E051A0FAL,
            0x5F5DBD583227D468L, 0x0BD7C1ADDF8445B9L, 0xDF4C37B60746F19DL, 0x843297E61DCE3AC9L, 0x4AB49CD05FCEB581L, 0xD163111C06931A44L, 0x22B5C36A451FD834L, 0xB5F35532FFD8B91AL,
            0xF63762017D8C5260L, 0x8BDAE902F884B480L, 0x764C84FE8A23C66BL, 0xEBD928B3BAF9861BL, 0x2F309A37F226E572L, 0x8CE7963A2A4D1506L, 0xA95FFF3EF2FFF01DL, 0x328FBA6615879639L,
            0x6E917F0F2BC5DADEL, 0x18F2C7872FC1F286L, 0xDD554844242766ABL, 0xDB5BA6C85B733032L, 0xC20C5B8460C43189L, 0x63E2684190B8B6B5L, 0x1ECF376F73B47996L, 0x1C8108181AF9B071L,
            0x13692D03A073E9CCL, 0x9167D912535C16A9L, 0x01892841D6C6DF0BL, 0x2B7C943EBEEDA745L, 0x0880CFEBE37DD3FDL, 0xF36B09A6D7747CD3L, 0x327ACBAA03726328L, 0x2CF74D1AB8894974L,
            0xCDB66C5D0303A944L, 0xAB7B842ED3FE1AB1L, 0x5E7B1ABE9C1C78ACL, 0xE416D3F171D0FF12L, 0x1B597A89B0C9FEE1L, 0x4E7BEC21AACB8131L, 0x78863E63B489AA71L, 0x19EF899236D0B5C5L,
            0xF8E9D85D9B6F064EL, 0xC599E84D32DD29FBL, 0xF5DE58954744D6A2L, 0xE43DAAA6170D041AL, 0x1D8C782644FB68EFL, 0xC03CFC5F4D6B0F52L, 0x435171A44A4A44A6L, 0x56E1DC7586195B69L,
            0xE6CE007F01BBE782L, 0xEB7AF83725C57534L, 0x597AB118A9A2BDBAL, 0x6D0F3FAAD2C10732L, 0xD4BD28795FC04DEBL, 0x3EA0D2B87473F50CL, 0x8EE0CB602DF142B0L, 0x09C436F3D7BC4696L,
            0x0998A6654BDD0634L, 0x05D5992C75B1676EL, 0x9228172784065CA6L, 0xE0FA902B48792F31L, 0x9F4CDF2BE8E4173AL, 0xB379C0D4B250B679L, 0x8C591ABBBCF768B5L, 0xB63EBAA2FE5B80ACL,
            0x1D8307CB9A450702L, 0x47BFB18D9F8C93ACL, 0x1E032B75DB852D82L, 0x436A0DD979E7F535L, 0x5785711FB9571100L, 0xFCECC785AE6FAC43L, 0x62F7A5EEC9D51184L, 0xE389A7C5B42E421CL,
            0xC8754AE45199C527L, 0x6B24BAABEE9D8AF3L, 0x1EC47D55F61448C0L, 0xAD6754A94D00A42FL, 0x7731E9C0ECE1A125L, 0xB257E1EE2987F428L, 0xEC425DC691B4CC8BL, 0x07CE5F18DE4BC138L,
            0xA594F97484860292L, 0x6E674BC336D4DA8CL, 0xD58B4896F10EF798L, 0x6968DF6702896CC1L, 0xD893C4BDAEDBEEFBL, 0xD1F8AB95DD912ABFL, 0x569382AD494824E9L, 0x4FCE2C209DA2E3AAL,
            0x2B43F14A77F8F600L, 0xB556588746E36D92L, 0x6B85BFB91EBCD229L, 0x98B4BB2A31909986L, 0xF592E54A80848697L, 0xB306E30DA4D2152AL, 0x18D981D1639F5B2AL, 0xA18631C09DF23091L,
            0x789572463DAEC2FFL, 0xAC8A51C24B3483ABL, 0x8BF6DB829CBD61E8L, 0x45BE5AE770C70294L, 0xA8ADF535A38E1C6DL, 0xD46679A244A60DD9L, 0x471D3E8C76F8786FL, 0x289D6F2D76A8333FL,
            0x4C26CF30168655DBL, 0x74593EBAB3739DA5L, 0x9CA324289A6F4A59L, 0xE71EB0C210C341E9L, 0x5D5ECEF248604580L, 0xDF2DD4DBD924BFD9L, 0x34667811E8296A64L, 0x878AE67F8263BE2AL,
            0x04D37DB685C346E2L, 0x24FA4D3FA6041672L, 0x97C61322F87E6C3EL, 0x263A84585C725404L, 0x4797B6B937C0DFF0L, 0xE80A115AA7C23E0AL, 0x9A68FCE1C7CC0DFFL, 0x594A2538955C3590L,
            0x0408C8C233B34017L, 0xDEE7FDC2F43115DEL, 0x15F4D0FFEA8AF0ABL, 0x5267EAF71FBE8394L, 0x3804484F33134A6BL, 0x6387F82862748DCBL, 0xBE1BFDBC2611FBF6L, 0xA499F29BF7ED597FL,
            0x5B67FDA1A26B1CA9L, 0x49FC48B4A36637D1L, 0x87B1D78ABFA0273CL, 0x94D699008C31A51AL, 0x78924B351BEA8CB5L, 0xB33AC196761A15BDL, 0x69888672B57D27F6L, 0x1E3A4CF21D6E1B1AL,
            0xEF8A2530CA139E90L, 0x84D76681890B1366L, 0xF40E431910EEAE56L, 0xC1C6B2E9F39D7CEBL, 0xE93A75B3C0AFF8B2L, 0x8F8878C612AD68FFL, 0x91FCEA165CA47DB6L, 0xB18CFBA542A844AFL,
            0x57DCB9EDDA6547EDL, 0x5249B2ED725F0D4BL, 0x6B7243414843C0B0L, 0x97446EDAD4EDBBBEL, 0x7642B57ED422A91BL, 0xF0C24B36B61505A6L, 0x34A0556BA9387608L, 0x20F5D1859483DB14L,
            0x3916495AF70D81A1L, 0x41069C8EE359139CL, 0x67D52CFBFD0E90C0L, 0x390A12B240796D32L, 0x974292A9F66173C5L, 0xBDA7604779DDC379L, 0xFD6C00ECA387A70FL, 0x5E23153385EEF927L,
            0x29C54260237C8C0AL, 0x5217265A8EC4356FL, 0xC2260384A799D8D4L, 0xB358BA7BC657EFFAL, 0xCA297CAD6C2CF565L, 0xFFF734C576EEF69BL, 0xFC81B75EB44482B3L, 0xBD2483D4DC8E9154L,
            0xC62D1F27EC6FAA2FL, 0x7985D0AFB7F6F228L, 0xBA51F64276F2ED5BL, 0x35BEE2AD70893ADEL, 0xA17CC97696BA3795L, 0x3719D36D5CA2971CL, 0x35F6BA7190FA237FL, 0x7A84F9D36CA581E8L,
    };

    /**
     * Seeds the counter using the lower 63 bits of s, and the state using both the upper 8 and lower 8 bits of s.
     * The state only has 256 possible starting values, chosen from very distant points in the CMR generator's period,
     * but all odd numbers are possible for the counter (this discards the sign bit by shifting s left and fills the LSB
     * with 1 always; the sign bit is used in calculating the state).
     * @param s a seed to fill the counter and state with usable values; all bits are used, though not directly
     */
    public final void seed(final long s) {
        counter = s;// << 1 | 1L;
        state = starting[(int) (s ^ s >>> 56) & 0xFF];
    }

    public final int nextInt()
    {
        return (int)(counter += (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 127L);
//        return (int)((state = (state << 21 | state >>> 43) * 0x9E3779B9L) * (counter += 0x9E3779B97F4A7AF6L));
    }
    @Override
    public final int next(final int bits)
    {
        return (int)(counter += (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 127L) >>> (32 - bits);
//        return (int)((state = (state << 21 | state >>> 43) * 0x9E3779B9L) * (counter += 0x9E3779B97F4A7AF6L)) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        return (counter += (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 127L);
//        return (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * (counter += 0x9E3779B97F4A7AF6L);
    }

    /**
     * Produces a copy of this MiniMover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this MiniMover64RNG
     */
    @Override
    public MoverCounter64RNG copy() {
        return new MoverCounter64RNG(state);
    }

    /**
     * Gets the CMR part of the state; if this generator was set with {@link #MoverCounter64RNG()} ()},
     * {@link #MoverCounter64RNG(long)}, or {@link #seed(long)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the state, a long
     */
    public long getState()
    {
        return state;
    }

    /**
     * Sets the CMR part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #seed(long)} to guarantee a good subcycle.
     * @param state any long; 0 will be treated as 1 instead
     */
    public void setState(final long state)
    {
        this.state = state == 0L ? 1L : state;
    }

    /**
     * Gets the counter part of the state, which can be any odd long.
     * @return the counter, a long
     */
    public long getCounter()
    {
        return counter;
    }

    /**
     * Sets the counter part of the state given any long; this will set the counter to always be odd but will otherwise
     * leave it untouched (it won't change if it was already odd).
     * @param counter the counter to use, a long; if this value is even it will be made odd by bitwise OR with 1L
     */
    public void setCounter(long counter)
    {
        this.counter = counter;// | 1L;
    }
    
    @Override
    public String toString() {
        return "MoverCounter64RNG with state 0x" + StringKit.hex(state) + "L and counter 0x" + StringKit.hex(counter) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoverCounter64RNG moverCounter64RNG = (MoverCounter64RNG) o;

        return state == moverCounter64RNG.state && counter == moverCounter64RNG.counter;
    }

    @Override
    public int hashCode() {
        return (int)((state ^ state >>> 32) * 31L ^ (counter ^ counter >>> 32));
    }

//    public static void main(String[] args)
//    {
//        long stateA = 0x9E3779B9L;
//        System.out.println("long[] starting = {");
//        for (int ctr = 0; ctr < 256; ctr++) {
//            System.out.printf("0x%016XL, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 0x7FFFFFFF; i++) {
//                stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L;
//            }
//        }
//        System.out.println("};");
//    }
}
