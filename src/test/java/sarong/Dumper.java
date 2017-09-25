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
    public static void blast(String filename, RandomnessSource rng)
    {
        DataOutputStream dos = null;
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
    public static void blastInt(String filename, RandomnessSource rng)
    {
        DataOutputStream dos = null;
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
    public static void blastIntSpecial(String filename, BardRNG rng)
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
        RNG rng = new RNG();
        for (int i = 0; i < 8; i++) {
            iSeeds[i] = rng.nextInt();
            seeds[i] = rng.nextLong();
        }
//        seeds[0] = (iSeeds[0] = 0);
//        seeds[1] = (iSeeds[1] = 3);
//        seeds[2] = (iSeeds[2] = -1);
//        seeds[3] = (iSeeds[3] = 31);
//        seeds[4] = (iSeeds[4] = -31);
//        seeds[5] = (iSeeds[5] = 1);
//        seeds[6] = Long.MAX_VALUE;
//        iSeeds[6] = Integer.MAX_VALUE;
//        seeds[7] = Long.MIN_VALUE;
//        iSeeds[7] = Integer.MIN_VALUE;
        for (int i = 8; i < 64; i++) {
            seeds[i] = CrossHash.Mist.predefined[(i & 15) + 2].hash64(seeds);
            iSeeds[i] = CrossHash.Mist.predefined[(i & 15) + 2].hash(seeds);
        }
        System.out.println("Seed used (long): " + seeds[62]);
        System.out.println("Seed used (int): " + iSeeds[62]);
        /*
        blast("Thunder", new ThunderRNG(seeds[62]));
        blast("Zap", new ZapRNG(seeds[62]));
        blast("Lap", new LapRNG(seeds[62]));
        blast("Beard", new BeardRNG(seeds[62]));
        */
        //blastInt("Bird", new BirdRNG(iSeeds));
        //blastInt("Light32", new Light32RNG(iSeeds[62], iSeeds[63]));
        //blastInt("Bard", new BardRNG(iSeeds));
        //blast("Thrust", new ThrustRNG(seeds[62]));
        //blastInt("Thrust32", new Thrust32RNG(iSeeds[62]));
        //blastInt("Lunge32", new Lunge32RNG(iSeeds[62]));
        blastInt("Jet", new JetRNG(iSeeds[62]));
        /*
        blastInt("Light", new LightRNG(seeds[62]));

        blast("Horde", new HordeRNG(seeds[62]));

        blast("Flap", new FlapRNG(seeds[62]));
        blast("XoRo", new XoRoRNG(seeds[62]));
        blast("Permuted", new PermutedRNG(seeds[62]));
        blast("LongPeriod", new LongPeriodRNG(seeds[62]));
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
