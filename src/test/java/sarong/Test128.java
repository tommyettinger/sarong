package sarong;

import org.huldra.math.BigInt;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by Tommy Ettinger on 6/17/2018.
 */
public class Test128 {
    public long mulhi(long x, long y) {
        final long xLow = x & 0xFFFFFFFFL;
        final long yLow = y & 0xFFFFFFFFL;
        x >>= 32;
        y >>= 32;
        final long z = (xLow * yLow >> 32);
        long t = x * yLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>= 32;
        return x * y + t + (tLow + xLow * y >> 32) - (z >> 63);
    }
    public long mulhi2(long x, long y)
    {
//        long x_high = x >> 32;
//        long x_low = x & 0xFFFFFFFFL;
//        long y_high = y >> 32;
//        long y_low = y & 0xFFFFFFFFL;
//        long z2 = x_low * y_low;
//        long t = x_high * y_low + (z2 >> 32);
//        long z1 = t & 0xFFFFFFFFL;
//        long z0 = t >> 32;
//        z1 += x_low * y_high;
//        return x_high * y_high + z0 + (z1 >> 32);
        final long xLow = x & 0xFFFFFFFFL;
        final long yLow = y & 0xFFFFFFFFL;
        x >>= 32;
        y >>= 32;
        final long z = (xLow * yLow >> 32);
        long t = x * yLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>= 32;
        return x * y + t + (tLow + xLow * y >> 32) - (z >> 63);
    }
    private final BigInt storage = new BigInt(0L);
    public long mulPrecise(long a, long b)
    {
        storage.assign(a);
        storage.mul(b);
        storage.shiftRight(64);
        return storage.longValue();
    }
    public long mulPrecise2(long a, long b)
    {
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).shiftRight(64).longValue();
    }
    @Test
    public void test128()
    {
        MizuchiRNG r1 = new MizuchiRNG(1234567890L, 987654321L), r2 = new MizuchiRNG(9876543210L, 123456789L);
        for (int i = 0; i < 8; i++) {
            r1.nextLong();
            r2.nextLong();
        }
        long a, b, x, y;
        for (int i = 0; i < 0x100; i++) {
            a = r1.nextLong();
            b = r2.nextLong();
            x = mulhi(a, b);
            y = mulPrecise(a, b);
            System.out.printf("On iteration %d, 0x%016X * 0x%016X (sum %016X) gave: 0x%016X and 0x%016X", i, a, b, a+b, x, y);
            if(x != y)
            {
                System.out.println(" :( by " + (y - x) + " ");
                //x = mulhi(a, b);
                //System.out.println(x);
            }
            else System.out.println();
        }
    }
    @Test
    public void testRNG()
    {
        RNG r = new RNG(new LinnormRNG(0x123456789ABCDEFL));
        for (int j = 0; j < 15; j++) {
            for (long i = 0x100000000L + j; i <= 0x30000000FL; i += 0x100000000L) {
                long limit = 5L;//oriole.nextInt();
                long result = r.nextLong(limit);
                System.out.printf("%016X %021d %016X %021d %b, ", result, result, limit, limit,Math.abs(limit) - Math.abs(result) >= 0 && (limit >> 63) == (result >> 63));
            }
            System.out.println();
        }

    }

}
