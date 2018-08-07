package sarong;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr-cmr with two 32-bit states; this is the fastest 32-bit generator that still passes statistical
 * tests. It has a period that is likely to be at most 2 to the 62, and is known to be at least 2 to the 33 for some 
 * seeds. Not all seeds have equivalent period lengths, and some may be very small; I don't have a good idea for how to
 * guarantee optimal cycles (Bob Jenkins has found ways for his JSF generators, so there could be some pointers there).
 * This seems to do well in PractRand testing, though the test hasn't run for very long yet, but at least some variants
 * on cmr-cmr pass BigCrush according to Overton. "Chaotic" generators like this one tend to score well in PractRand,
 * but it isn't clear if they will fail other tests. As for speed, this is faster than {@link Lathe32RNG} (which is also
 * high-quality) and is also faster than {@link XoRo32RNG} (which is very fast but has quality issues) and
 * {@link ThrustAlt32RNG} (which has a very small period and probably isn't very useful). Its period is shorter than
 * Lathe32RNG for certain, but not necessarily by very much (1/4 the total period seems possible, at 2 to the 62).
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it may need to edit states that would be assigned to
 * it if they would put the generator in a short-period subcycle. It does not change the states currently.
 * <br>
 * Created by Tommy Ettinger on 8/6/2018.
 */
public final class Mover32RNG implements RandomnessSource {
    private int stateA, stateB;
    public Mover32RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Mover32RNG(int stateA, int stateB)
    {
        setState(stateA, stateB);
    }

    public final void setState(final int a, final int b) {
        stateA = a == 0 ? 42 : a;
        stateB = b == 0 ? -1 : b;
    }

    public final int nextInt()
    {
        return (stateA = Integer.rotateLeft(stateA * 0x9E37, 10))
                + (stateB = Integer.rotateLeft(stateB * 0x4E6D, 22));
    }
    @Override
    public final int next(final int bits)
    {
        return ((stateA = Integer.rotateLeft(stateA * 0x9E37, 10))
                + (stateB = Integer.rotateLeft(stateB * 0x4E6D, 22))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        final long t = (stateA = Integer.rotateLeft(stateA * 0x9E37, 10))
                + (stateB = Integer.rotateLeft(stateB * 0x4E6D, 22));
        return t << 32 ^ (stateA = Integer.rotateLeft(stateA * 0x9E37, 10))
                + (stateB = Integer.rotateLeft(stateB * 0x4E6D, 22));
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
}
