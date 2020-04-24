package sarong;

import sarong.util.StringKit;

import java.io.Serializable;

public final class Orbit32RNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 2L;

    private int stateA, stateB;

    public Orbit32RNG() {
        stateA = (int)((Math.random() - 0.5) * 0x1p32);
        stateB = (int)((Math.random() - 0.5) * 0x1p32);
    }
    public Orbit32RNG(final int seed) {
        setSeed(seed);
    }
    public Orbit32RNG(final long seed) {
        setState(seed);
    }
    public Orbit32RNG(final int stateA, final int stateB) {
        this.stateA = stateA;
        this.stateB = stateB;
    }
    @Override
    public final int next(int bits) {
        final int s = (stateA = 0 | stateA + 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB = 0 | stateB + ((s | -s) >> 31 & 0x9E3779BB)) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (x ^ x >>> 15) >>> (32 - bits);
    }

    public final int nextInt() {
        final int s = (stateA = 0 | stateA + 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB = 0 | stateB + ((s | -s) >> 31 & 0x9E3779BB)) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (x ^ x >>> 15);
    }
    
    public final int nextInt(final int bound) {
        final int s = (stateA = 0 | stateA + 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB = 0 | stateB + ((s | -s) >> 31 & 0x9E3779BB)) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (int) ((bound * ((x ^ x >>> 15) & 0xFFFFFFFFL)) >>> 32) & ~(bound >> 31);
    }

    @Override
    public final long nextLong() {
        int s = (stateA + 0xC1C64E6D | 0);
        final int s = (stateA = 0 | stateA + 0xC1C64E6D);
        int x = (s ^ s >>> 17) * ((stateB = 0 | stateB + ((s | -s) >> 31 & 0x9E3779BB)) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        final long high = (x ^ x >>> 15);
        s = (stateA = 0 | stateA + 0x838C9CDA);
        x = (s ^ s >>> 17) * ((stateB = 0 | stateB + ((s | -s) >> 31 & 0x9E3779BB)) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL);
    }

    @Override
    public Orbit32RNG copy() {
        return new Orbit32RNG(stateA, stateB);
    }

    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55 | 0, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13) | 0;
        z = seed + 0x8E9D5AAA | 0;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13) | 0;
    }
    
    public int getStateA()
    {
        return stateA;
    }
    public void setStateA(int stateA)
    {
        this.stateA = stateA;
    }
    public int getStateB()
    {
        return stateB;
    }
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
    }
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA;
        this.stateB = stateB;
    }
    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }
    @Override
    public void setState(long state) {
        stateA = (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public String toString() {
        return "Orbit32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Orbit32RNG orbit32RNG = (Orbit32RNG) o;

        return stateA == orbit32RNG.stateA && stateB == orbit32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }
}
