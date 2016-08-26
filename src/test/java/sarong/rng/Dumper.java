package sarong.rng;

import sarong.util.CrossHash;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Tommy Ettinger on 8/24/2016.
 */
public class Dumper {
    public static void blast(String filename, RNG[] r)
    {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + filename + ".dat", false));

            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 0x20000; j++) {
                    dos.writeLong(r[63].nextLong());
                }
            }
            dos.flush();
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        long[] seeds = new long[64];
        seeds[0] = 0;
        seeds[1] = 3;
        seeds[2] = -1;
        seeds[3] = 31;
        seeds[4] = -31;
        seeds[5] = 1;
        seeds[6] = Long.MAX_VALUE;
        seeds[7] = Long.MIN_VALUE;
        for (int i = 8; i < 64; i++) {
            seeds[i] = CrossHash.hash(seeds);
        }
        RNG[] rs = new RNG[64];
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new ThunderRNG(seeds[i]));
        }
        blast("Thunder", rs);
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new LightRNG(seeds[i]));
        }
        /*
        blast("Light", rs);
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new XoRoRNG(seeds[i]));
        }
        blast("XoRo", rs);
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new PermutedRNG(seeds[i]));
        }
        blast("Permuted", rs);
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new LongPeriodRNG(seeds[i]));
        }
        blast("LongPeriod", rs);
        */
    }
}
