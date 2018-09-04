package sarong;

import org.roaringbitmap.longlong.Roaring64NavigableMap;

/**
 * Created by Tommy Ettinger on 1/12/2018.
 */
public class BirthdayTest {
    public static void main(String[] args)
    {
        Roaring64NavigableMap r = new Roaring64NavigableMap();
        ThrustAltRNG src = new ThrustAltRNG();
        for (int t = 0; t < 16; t++) {
            r.clear();
            src.skip(src.nextLong());
            System.out.printf("Seed used on try %d: 0x%016XL\n", t, src.state);
            for (int i = 0; i < 0x400000; i++) {
                r.addLong(src.nextLong());
            }
//            System.out.print("Step 1 done...");
//        long n;
//        for (int i = 0; i < 0x7ff00000; i++) {
//            if(r.contains(n = src.nextLong()))
//            {
//                System.out.println("BIRTHDAY COLLISION! " + n + " collided after " + (i + 0x100000) + " numbers.");
//                break;
//            }
//        }
            long card = r.getLongCardinality();
            System.out.printf("0x%05X / 0x400000 entered without collision, or %f / 1.0\n", card, card * 0x1p-22);
        }
        /*
        n = 1L;
        System.out.println("RANKS:");
        for (int i = 0; i < 64; i++, n <<= 1) {
            card = r.rankLong(n);
            System.out.printf("Values <= %016X: %016X, or %f of total possible\n", n, card, card / (double)n);
        }
        */

    }
}
