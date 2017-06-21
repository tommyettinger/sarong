package sarong;

import sarong.util.CrossHash;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 8/24/2016.
 */
public class Dumper {
    public static void blast(String filename, RNG[] r)
    {
        DataOutputStream dos = null;
        RNG rng = r[62];
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + filename + ".dat", false));

            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 0x20000; j++) {
                    dos.writeLong(rng.nextLong());
                }
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void blastInt(String filename, RNG[] r)
    {
        DataOutputStream dos = null;
        RNG rng = r[62];
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + filename + ".dat", false));

            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 0x40000; j++) {
                    dos.writeInt(rng.next(32));
                }
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void blastIntSpecial(String filename, BirdRNG rng)
    {
        DataOutputStream dos;
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + filename + ".dat", false));

            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 0x40000; j++) {
                    dos.writeInt(rng.nextInt());
                }
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static long[] seeds = new long[64];
    public static int[] iSeeds = new int[64];
    public static void main(String[] args)
    {
        seeds[0] = (iSeeds[0] = 0);
        seeds[1] = (iSeeds[1] = 3);
        seeds[2] = (iSeeds[2] = -1);
        seeds[3] = (iSeeds[3] = 31);
        seeds[4] = (iSeeds[4] = -31);
        seeds[5] = (iSeeds[5] = 1);
        seeds[6] = Long.MAX_VALUE;
        iSeeds[6] = Integer.MAX_VALUE;
        seeds[7] = Long.MIN_VALUE;
        iSeeds[7] = Integer.MIN_VALUE;
        for (int i = 8; i < 64; i++) {
            seeds[i] = CrossHash.Mist.predefined[(i & 15) + 2].hash64(seeds);
            iSeeds[i] = CrossHash.Mist.predefined[(i & 15) + 2].hash(seeds);
        }
        RNG[] rs = new RNG[64];
        /*
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new ThunderRNG(seeds[i], seeds[(i + 12) & 63]));
        }
        System.out.println(seeds[62]);
        blast("Thunder", rs);
        */
        /*
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new ZapRNG(LightRNG.determine(seeds[i])));
        }
        blast("Zap", rs);

        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new LapRNG(LightRNG.determine(seeds[i])));
        }
        blast("Lap", rs);
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new BeardRNG(LightRNG.determine(seeds[i])));
        }
        blast("Beard", rs);
        */
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new BirdRNG(LightRNG.determine(seeds[i])));
        }
        blastIntSpecial("Bird2", new BirdRNG(iSeeds));
        /*
        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new LightRNG(seeds[i]));
        }
        blastInt("Light", rs);

        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new HordeRNG(seeds[i]));
        }
        blast("Horde", rs);

        for (int i = 0; i < 64; i++) {
            rs[i] = new RNG(new FlapRNG(seeds[i]));
        }
        blast("Flap", rs);
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

        /*
        DataOutputStream dos = null;
        Random jre = new Random(seeds[62]);
        try {
            dos = new DataOutputStream(new FileOutputStream("target/JRE.dat", false));

            for (int i = 0; i < 256; i++) {
                for (int j = 0; j < 0x20000; j++) {
                    dos.writeInt(jre.nextInt());
                }
            }
            dos.flush();
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        DataOutputStream dos = null;
        Random jre = new Random(seeds[62]);
        /*
        try {
            dos = new DataOutputStream(new FileOutputStream("target/JRE.dat", false));

            for (int i = 0; i < 128; i++) {
                for (int j = 0; j < 0x20000; j++) {
                    dos.writeLong(jre.nextLong());
                }
            }
            dos.flush();
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
