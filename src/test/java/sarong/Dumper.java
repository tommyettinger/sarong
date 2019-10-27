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

            for (int j = 0; j < 0x2000000; j++) {
                dos.writeLong(rng.nextLong());
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void scatterBlast(String prefix)
    {
        DataOutputStream dos;
        // 0xD0E89D2D 0x85157AF5 0x62E2AC0D 0xAC564B05
        /*
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_16_16.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x1000000; j++, state += 0xAC564B05) {
                z = (state ^ state >>> 16) * 0x2C9277B5;
                dos.writeInt(z ^ z >>> 16);
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_16_15.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x1000000; j++, state += 0xAC564B05) {
                z = (state ^ state >>> 16) * 0x2C9277B5;
                dos.writeInt(z ^ z >>> 15);
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        // 0x01ED0675
        // state += 0xAC564B05
        /*
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_14_14_2C9_5F3.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x4000000; j++, state += 0x7F4A7C15) {
                z = (state ^ state >>> 14) * 0x2C9277B5;
                dos.writeInt((z ^ z >>> 14) * ((z >>> 9 ^ z << 17) * 0x5F356498 | 5));
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_14_14_5F3_2C9.dat", false));
            int state = -1109006589, z, state2 = 879346589;
            for (int j = 0; j < 0x4000000; j++, state += 0x7F4A7C15) {
                z = (state ^ state >>> 14) * 0x5F356495;
                dos.writeInt(state2 += (z ^ z >>> 14) * (state2 << 2 | 5));// + 0x2C9277B5);
                //dos.writeInt(state2 += (z ^ z >>> 14) * (state2 << 2 | 5) + 0x2C9277B5);
                //dos.writeInt((z ^ z >>> 14) * ~((z >>> 12 | z << 20) * 0x632BE5A6) + 0x2C9277B5);
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_14_14_01E_5F3.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x4000000; j++, state += 0x7F4A7C15) {
                z = (state ^ state >>> 14) * 0x01ED0675 + 0x62E2AC0D;
                dos.writeInt((z ^ z >>> 14) * ((z >>> 9 ^ z << 17) * 0x5F356498 | 5));
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        /*
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_14_14_01E_2C9.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x1000000; j++, state += 0xAC564B05) {
                z = (state ^ state >>> 14) * 0x01ED0675;// + 0x7F4A7C15;
                dos.writeInt( (z ^ 0x34ED9DE5 * (z >>> 7) ^ (z >>> 14)) * 0x2C9277B5);
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + prefix + "_14_14_01E_2C2.dat", false));
            int state = -1109006589, z;
            for (int j = 0; j < 0x1000000; j++, state += 0xAC564B05) {
                z = (state ^ state >>> 14) * 0x01ED0675;// + 0x7F4A7C15;
                dos.writeInt((z ^ 0x34ED9DE5 * (z >>> 7) ^ (z >>> 14)) * 0x2C2C57ED);
            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/



    }

    public static void blastInt(String filename, RandomnessSource rng)
    {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream("target/" + filename + ".dat", false));

            for (int j = 0; j < 0x4000000; j++) {
                dos.writeInt(rng.next(32));
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
        //blastInt("Squirrel", new SquirrelRNG(iSeeds[62]));
        blast("Lunge32", new Lunge32RNG(iSeeds[62]));
        //blastInt("Herd", new HerdRNG(iSeeds[62]));
        //blastInt("Jet", new JetRNG(iSeeds[62]));
        //blast("Thrust", new ThrustRNG(seeds[62]));
        //scatterBlast("Thrust32");
        //blast("Rule90", new Rule90RNG(seeds[62]));
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
