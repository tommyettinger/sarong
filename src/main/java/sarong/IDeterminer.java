package sarong;

/**
 * Implementations of this provide an RNG algorithm that does not store its own state, instead accepting a state as a
 * parameter and expecting it to change on repeated calls.
 * <br>
 * Created by Tommy Ettinger on 11/1/2017.
 */
public interface IDeterminer {
    /**
     * Given a state that is expected to change between calls, this gets a deterministic pseudo-random number calculated
     * from state; the result will be the same if state is ever the same on two calls. Implementations should handle
     * the change of state as internally as possible, so this can be called with {@code determine(++state)} to go
     * forwards or {@code determine(--state)} to go backwards.
     * @param state a long that should change between calls, with the best implementations working if state changes by 1
     * @return a deterministic pseudo-random long calculated from state
     */
    long determine(long state);

    /**
     * An abstract expansion of IDeterminer's API to include methods that get int values under a specific bound.
     */
    abstract class ADeterminer implements IDeterminer
    {
        /**
         * Uses this class' {@link #determine(long)} method to get a pseudo-random long from state, and then uses that
         * to get a reasonably-well-distributed int between 0 (inclusive) and bound (exclusive). The bound can be
         * positive or negative.
         * @param state a long that should change between calls, with the best implementations working if state changes by 1
         * @param bound an int that will be the outer exclusive bound for the result; can be positive or negative
         * @return a deterministic pseudo-random int between 0 (inclusive) and bound (exclusive), calculated from state
         */
        int determineBounded(long state, int bound)
        {
            return (int)((bound * (determine(state) & 0xFFFFFFFFL)) >> 32);
        }

        /**
         * Uses this class' {@link #determine(long)} method to get a pseudo-random long from state, then gets up to 32
         * bits from its upper half, with the actual quantity specified by {@code bits}.
         * @param state a long that should change between calls, with the best implementations working if state changes by 1
         * @param bits the amount of bits to get, from 1 to 32
         * @return a pseudo-random int with up to {@code bits} bits
         */
        int determineBits(long state, int bits)
        {
            return (int) (determine(state) >>> (64 - bits));
        }
    }
    class SplitMix64 extends ADeterminer
    {
        @Override
        public long determine(long state) {
            state = (((state *= 0x9E3779B97F4A7C15L) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
            state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
            return state ^ (state >>> 31);
        }
    }
    class Thrust extends ADeterminer
    {
        @Override
        public long determine(long state) {
            state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 26) * 0x2545F4914F6CDD1DL;
            return state ^ state >>> 28;
        }
    }
    class ThrustAlt extends ADeterminer
    {
        @Override
        public long determine(long state) {
            state = ((state *= 0x6A5D39EAE12657A9L) ^ (state >>> 25)) * (state | 0xA529L);
            return state ^ (state >>> 23);
        }
    }
    class ThrustAltOld extends ADeterminer
    {
        @Override
        public long determine(long state) {
            state = (state *= 0xA99635D5B8597AE5L) ^ (((state + 0xA99635D5B8597AE5L) ^ state >>> 23) * 0xAD5DE9A61A9C3D95L);
            return state ^ (state >>> 29);
        }
    }
}
