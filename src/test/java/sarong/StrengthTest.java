package sarong;

/**
 * Created by Tommy Ettinger on 9/1/2016.
 */
public class StrengthTest {
    public static void main(String[] args)
    {
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
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double)0x10000000);
        }

        random = new ThunderRNG(++partA, partB);
        System.out.println(partA + "," + partB);
        bits = new long[64];
        curr = random.nextLong();
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
            System.out.printf("%02d : % .24f\n", i, 0.5 - bits[i] / (double)0x10000000);
        }
    }
}
