package sarong;

import org.junit.Test;
import sarong.util.StringKit;

/**
 * Created by Tommy Ettinger on 9/1/2016.
 */
public class StrengthTest {
    //@Test
    public void testThunder() {
        ThunderRNG random = new ThunderRNG(); //0xABC7890456123DEFL
        long partA = random.getStatePartA(), partB = random.getStatePartB();
        System.out.println(partA + "," + partB);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x10000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x10000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x10000000);
        }
    }
    //@Test
    public void testPermuted()
    {
        PermutedRNG random = new PermutedRNG(); //0xABC7890456123DEFL
        long state = random.getState();
        System.out.println(state);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x10000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x10000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x10000000);
        }
    }
    //@Test
    public void testLight()
    {
        LightRNG random = new LightRNG(); //0xABC7890456123DEFL
        long state = random.getState();
        System.out.println(state);
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x10000000L; i++) {
            t = curr ^ (curr = random.nextLong());
            //t = random.nextLong();
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("Out of 0x10000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double) 0x10000000);
        }
    }

    @Test
    public void testLap()
    {
        LapRNG random = new LapRNG(); //0xABC7890456123DEFL
        System.out.println("LapRNG (testing nextLong): " + StringKit.hex(random.getState0()) + StringKit.hex(random.getState1()));
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextLong());
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("LAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void testZap()
    {
        ZapRNG random = new ZapRNG(); //0xABC7890456123DEFL
        System.out.println("ZapRNG (testing nextLong): " + StringKit.hex(random.getState0()) + StringKit.hex(random.getState1()));
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextLong());
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("ZAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }
    //@Test
    public void testLapInt()
    {
        LapRNG random = new LapRNG(); //0xABC7890456123DEFL
        System.out.println("LapRNG (testing nextInt): " + StringKit.hex(random.getState0()) + StringKit.hex(random.getState1()));
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("LAP INT: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }


    //@Test
    public void testFlap()
    {
        FlapRNG random = new FlapRNG(); //0xABC7890456123DEFL
        System.out.println(StringKit.hex(random.state0) + StringKit.hex(random.state1));
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("FLAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }


    @Test
    public void testSlap()
    {
        SlapRNG random = new SlapRNG(); //0xABC7890456123DEFL
        System.out.println(random);
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("SLAP: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }
    @Test
    public void testHorde()
    {
        HordeRNG random = new HordeRNG(new long[16]); //0xABC7890456123DEFL
        System.out.println("HordeRNG (testing nextLong): " +
                StringKit.hex(random.state)
                + " with " + StringKit.hex(random.choice));
        long[] bits = new long[64];
        long curr = random.nextLong(), t;
        int bi;
        for (long i = 0; i < 0x1000000L; i++) {
            /*t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
            */
            t = curr ^ (curr = random.nextLong());
            bi = 63;
            for (long b = 0x8000000000000000L; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("HORDE: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 64; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void testHerd()
    {
        HerdRNG random = new HerdRNG(new int[16]); //0xABC7890456123DEFL
        System.out.println("HerdRNG (testing nextInt): " +
                StringKit.hex(random.state)
                + " with " + StringKit.hex(random.choice));
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("HERD: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void testBird()
    {
        BirdRNG random = new BirdRNG(new int[32]); //0xABC7890456123DEFL
        System.out.println("BirdRNG (testing nextInt): " + random.toString());
        int[] bits = new int[32];
        int curr = random.nextInt(), t;
        int bi;
        for (int i = 0; i < 0x1000000; i++) {
            t = curr ^ (curr = random.nextInt());
            bi = 31;
            for (int b = 0x80000000; b != 0; b >>>= 1, bi--) {
                bits[bi] += (t & b) >>> bi;
            }
        }
        System.out.println("BIRD: Out of 0x1000000 random numbers,");
        //System.out.println("Out of 4,294,967,296 random numbers,");
        System.out.println("each bit changes this often relative to 0.5 probability...");
        for (int i = 0; i < 32; i++) {
            System.out.printf("%02d : % .24f %s\n", i, 0.5 - bits[i] / (double) 0x1000000, (Math.abs(0.5 - bits[i] / (double) 0x1000000) > 0.03) ? "!!!" : "");
        }
    }

    @Test
    public void dummyTest()
    {
        {
            BirdRNG r = new BirdRNG(0); //-1999262892926553691L
            System.out.println(r);
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(r.nextInt()));
            }
            r.setState(r.nextLong());
            System.out.println();
            System.out.println(r);
            System.out.println();

            int wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");

            for (long m = 1; m <= 0x100000104L; m++) {
                if (bonus == (bonus = r.nextInt()))
                {
                    if(bonus == r.nextInt())
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");
        }
        /*{
            int state = 1234560;
            System.out.println("STARTING LFSR AT: " + state);
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(state = LFSR.determinePositiveInt(state)));
            }
            state = 1234560;
            System.out.println();
            System.out.println();

            int wrap = (state = LFSR.determinePositiveInt(state)),
                    wrap2 = (state = LFSR.determinePositiveInt(state)),
                    wrap3 = (state = LFSR.determinePositiveInt(state)),
                    wrap4 = (state = LFSR.determinePositiveInt(state)),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");

            for (long m = 1; m <= 0x100000104L; m++) {
                if(state <= 0)
                    System.out.println("NOT CORRECT! " + m);
                if (bonus == (bonus = (state = LFSR.determinePositiveInt(state))))
                {
                    if(bonus == (state = LFSR.determinePositiveInt(state)))
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = (state = LFSR.determinePositiveInt(state))));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = (state = LFSR.determinePositiveInt(state))));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = (state = LFSR.determinePositiveInt(state))));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + state + "  with next 4 random values: "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  "
                    + (state = LFSR.determinePositiveInt(state)) + "  ");
        }*/
        /*{
            HordeRNG r = new HordeRNG(); //-1999262892926553691L
            System.out.println();
            System.out.println(StringKit.hex(r.state)
                    + " with " + StringKit.hex(r.choice));
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(r.nextInt()));
            }
            r.setState(r.nextLong());
            System.out.println();
            System.out.println(r);
            System.out.println();
            long wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
            for (long m = 1; m <= 0x100000104L; m++) {
                if (bonus == (bonus = r.nextInt()))
                {
                    if(bonus == r.nextInt())
                        System.out.println("BAD. " + StringKit.hex(m));
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");
        }*/
        /*{
            HerdRNG r = new HerdRNG(new int[16]); //-1999262892926553691L
            System.out.println();
            System.out.println(StringKit.hex(r.state)
                    + " with " + StringKit.hex(r.choice));
            System.out.println();
            for (int i = 0; i < 256; i++) {
                System.out.println(StringKit.hex(r.nextInt()));
            }
            r = new HerdRNG(new int[16]);
            System.out.println();
            System.out.println(r);
            System.out.println();
            long wrap = r.nextInt(), wrap2 = r.nextInt(), wrap3 = r.nextInt(), wrap4 = r.nextInt(),
                    bonus = 0, bonus2 = 0, bonus3 = 0, bonus4 = 0;
            System.out.println(wrap + "  " + wrap2 + "  " + wrap3 + "  " + wrap4 + "  ");
            for (long m = 1; m <= 0x100000104L; m++) {
                if (bonus == (bonus = r.nextInt()))
                {
                    if(bonus == r.nextInt())
                        System.out.println("BAD. Position " + StringKit.hex(m) + " and value " + bonus + ", r is " + r);
                }
                else {
                    if (wrap == bonus) {
                        System.out.println(StringKit.hex(m++) + ": " + bonus2 + ", " + (bonus2 = r.nextInt()));
                        if (wrap2 == bonus2) {
                            System.out.println(StringKit.hex(m++) + ": "  + bonus3 + ", " + (bonus3 = r.nextInt()));
                            if (wrap3 == bonus3) {
                                System.out.println(StringKit.hex(m++) + ": " + bonus4 + ", " +  (bonus4 = r.nextInt()));
                                if (wrap4 == bonus4) {

                                    System.out.println(StringKit.hex(m) + "!!!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("DONE! final r: " + r + "  with next 4 random values: "
                    + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  " + r.nextInt() + "  ");

        }*/

    }
}
