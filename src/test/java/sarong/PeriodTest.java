package sarong;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tommy Ettinger on 9/13/2018.
 */
public class PeriodTest {
    @Test
    public void checkPeriod32(){
        int stateA = 1, i = 0;
        //final int r = 13, m = 0x89A7;
        final int r = 25, m = 0xA5F152BF;
        for (; i != -1; i++) {
            if ((stateA = Integer.rotateLeft(stateA, 25) + 0xA5F152BF) == 1) {
                //if (i >>> 24 == 0xFF)
                System.out.printf("(state + 0x%08X, rotation %02d: 0x%08X\n", m, r, i);
                break;
            }
        }

    }

    @Test
    public void checkPeriod64() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                //state += state >>> 48;
                if ((
                        //state -= (m << 35 | m >>> 29)
//                        state = 0xC6BC279692B5CC8BL - (state << 45 | state >>> 19) // gets to 10438 iterations
                        state =   0xC6BC279692B5CC8BL - (state << 35 | state >>> 29) // gets past all 65536 iterations
                        //state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
        System.out.printf("(state = 0xC6BC279692B5CC8BL - (state << 45 | state >>> 19)): 0x%08X\n", i);
//        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }

    @Test
    public void checkPeriod64_cers() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                //state += state >>> 48;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        state = 0x5320B74ECA44ADADL - (state << 23 | state >>> 41)
                        //state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
        System.out.printf("(state = 0x5320B74ECA44ADADL - (state << 23 | state >>> 41)): 0x%08X\n", i);
//        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }
    @Test
    public void checkPeriod64_iadla() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                state += state >>> 47;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                        state += state << 16
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
//        System.out.printf("(state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)): 0x%08X\n", i);
        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }
    @Test
    public void checkPeriod64_cmr() {
        long i = 1L, state = 1L, m;
        OUTER:
        for (int outer = 0; outer < 0x10000; outer++) {
            for (int inner = 0x80000000; inner < 0; inner++) {
                //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                //m = state * 8997361904524633241L; //0xCE8B0105AF585193L
                state *= 0x41C64E6BL;
                if ((
                        //state -= (m << 35 | m >>> 29)
                        //state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)
                        state = (state << 28 | state >>> 36) // gets to at least 58320 iterations
                ) == 1L) {
                    break OUTER;
                }
                i++;
            }
            System.out.printf("0x80000000L * %03d completed, state is 0x%016X\n", (outer+1), state);
        }
//        System.out.printf("(state -= Long.rotateLeft(state * 8997361904524633241L, 35)): 0x%08X\n", i);
//        System.out.printf("(state = 0xC6BC279692B5CC83L - (state << 39 | state >>> 25)): 0x%08X\n", i);
        System.out.printf("(state += state >>> 48; state += state << 16): 0x%08X\n", i);
    }
    ///////// BEGIN subcycle finder code and period evaluator
    public static void main(String[] args)
//    @Test
//    public void testSubcycle32()
    {
        // multiplying
        // A refers to 0x9E377
        // A 10 0xC010AEB4
        // B refers to 0x64E6D
        // B 22 0x195B9108
        // all  0x04C194F3485D5A68

        // A=Integer.rotateLeft(A*0x9E377, 17) 0xF7F87D28
        // B=Integer.rotateLeft(A*0x64E6D, 14) 0xF023E25B 
        // all  0xE89BB7902049CD38


        // A11 B14 0xBBDA9763B6CA318D
        // A8  B14 0xC109F954C76CB09C
        // A17 B14 0xE89BB7902049CD38
//        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
//        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("0x%016X\n", result.longValue());
        // 0x9E37
        // rotation 27: 0xEE06F34D
        // 0x9E35
        // rotation 6 : 0xE1183C3A
        // rotation 19: 0xC4FCFC55
        // 0x9E3B
        // rotation 25: 0xE69313ED
        // 0xDE4D
        // rotation 3 : 0xF6C16607
        // rotation 23: 0xD23AD58D
        // rotation 29: 0xC56DC41F
        // 0x1337
        // rotation 7:  0xF41BD009
        // rotation 20: 0xF5846878
        // rotation 25: 0xF38658F9
        // 0xACED
        // rotation 28: 0xFC98CC08
        // rotation 31: 0xFA18CD57
        // 0xBA55
        // rotation 19: 0xFB059E43
        // 0xC6D5
        // rotation 05: 0xFFD78FD4
        // 0x5995
        // rotation 28: 0xFF4AB87D
        // rotation 02: 0xFF2AA5D5
        // 0xA3A9
        // rotation 09: 0xFF6B3AF7
        // 0xB9EF
        // rotation 23: 0xFFAEB037
        // 0x3D29
        // rotation 04: 0xFF6B92C5
        // 0x5FAB
        // rotation 09: 0xFF7E3277 // seems to be very composite
        // 0xCB7F
        // rotation 01: 0xFF7F28FE
        // 0x89A7
        // rotation 13: 0xFFFDBF50 // wow! note that this is a multiple of 16
        // 0xBCFD
        // rotation 17: 0xFFF43787 // second-highest yet, also an odd number
        // 0xA01B
        // rotation 28: 0xFFEDA0B5
        // 0xC2B9
        // rotation 16: 0xFFEA9001

        // slightly larger multiplications, GWT-safe
        //// 0xFFFFD5A9 0xFFFF687B 0xFFFF2E45 0xFFFF1928 0xFFFEE5E1 0xFFFEAB53 0xFFFE9CA7
        // (a = (a << 23 | a >>> 9) * 0x402AB) ^ (b = (b << 28 | b >>> 4) * 0x01621) ^
        // (c = (c << 24 | c >>> 8) * 0x808E9) ^ (d = (d << 29 | d >>> 3) * 0x8012D)
        // 0x402AB, rotation 23: 0xFFFFD5A9
        // 0x01621, rotation 28: 0xFFFF687B
        // 0x808E9, rotation 24: 0xFFFF2E45
        // 0x40809, rotation 05: 0xFFFF1928
        // 0xC134D, rotation 31: 0xFFFF04B2
        // 0x81411, rotation 09: 0xFFFEF47E
        // 0xC15DF, rotation 28: 0xFFFEE5E1
        // 0x8012D, rotation 29: 0xFFFEAB53
        // 0xC122D, rotation 19: 0xFFFE9CA7
        // 0x02323, rotation 24: 0xFFFE9916
        // 0x82245, rotation 02: 0xFFFE7A52
        // 0xC22C7, rotation 21: 0xFFFE6D98
        // 0x420DD, rotation 18: 0xFFFE3E93
        // 0x01F59, rotation 28: 0xFFFDFE3F
        // 0x01681, rotation 16: 0xFFFDEE49
        // 0x01ADD, rotation 21: 0xFFFDBC12
        // 0x41B85, rotation 13: 0xFFFD9F0D
        // 0x805D1, rotation 13: 0xFFFD966F
        // 0x0023F, rotation 21: 0xFFFD9550
        // 0xC14B7, rotation 26: 0xFFFD94BD
        // 0xC20AF, rotation 01: 0xFFFD7A4A
        // 0x80455, rotation 22: 0xFFFD7792
        // 0x40113, rotation 09: 0xFFFD71A1
        // 0x4062D, rotation 16: 0xFFFD43FA
        // 0x016CB, rotation 23: 0xFFFD3678
        // 0x41625, rotation 01: 0xFFFD23A9
        // 0x00D09, rotation 19: 0xFFFD015D


        // adding
        // 0x9E3779B9
        // rotation 2 : 0xFFCC8933
        // rotation 7 : 0xF715CEDF
        // rotation 25: 0xF715CEDF
        // rotation 30: 0xFFCC8933
        // 0x6C8E9CF5
        // rotation 6 : 0xF721971A
        // 0x41C64E6D
        // rotation 13: 0xFA312DBF
        // rotation 19: 0xFA312DBF
        // rotation 1 : 0xF945B8A7
        // rotation 31: 0xF945B8A7
        // 0xC3564E95
        // rotation 1 : 0xFA69E895 also 31
        // rotation 5 : 0xF2BF5E23 also 27
        // 0x76BAF5E3
        // rotation 14: 0xF4DDFC5A also 18
        // 0xA67943A3 
        // rotation 11: 0xF1044048 also 21
        // 0x6C96FEE7
        // rotation 2 : 0xF4098F0D
        // 0xA3014337
        // rotation 15: 0xF3700ABF also 17
        // 0x9E3759B9
        // rotation 1 : 0xFB6547A2 also 31
        // 0x6C8E9CF7
        // rotation 7 : 0xFF151D74 also 25
        // rotation 13: 0xFD468E2B also 19
        // rotation 6 : 0xF145A7EB also 26
        // 0xB531A935
        // rotation 13: 0xFF9E2F67 also 19
        // 0xC0EF50EB
        // rotation 07: 0xFFF8A98D also 25
        // 0x518DC14F
        // rotation 09: 0xFFABD755 also 23 // probably not prime
        // 0xA5F152BF
        // rotation 07: 0xFFB234B2 also 25
        // 0x8092D909
        // rotation 10: 0xFFA82F7C also 22
        // 0x73E2CCAB
        // rotation 09: 0xFF9DE8B1 also 23
        // 0x98EA52FD
        // rotation 07: 0xFFEBC6C7 also 25
        // 0x87194377
        // rotation 03: 0xFFF4D1C8 also 29
        // 0x9504583F
        // rotation 15: 0xFFFA683A also 17
        // 0x8F2554F7
        // rotation 11: 0xFFFA5B00 also 21
        // 0xC91ED343
        // rotation 10: 0xFFF8F071 also 22
        // 0x847A859B
        // rotation 15: 0xFFF8BBBC also 17
        // 0xC4DE9951
        // rotation 07: 0xFFFEEAA9 also 25
        // 0xAA78EDD7
        // rotation 01: 0xFFFF9C61 also 31
        // 0x929E7143
        // rotation 14: 0xFFFA8F94 also 18
        // stateB = rotate32(stateB + 0xB531A935, 13)
        // stateC = rotate32(stateC + 0xC0EF50EB, 7)

        // to get period of 0xFFFF9C61:
        // a = rotate32(a, 1) + 0xAA78EDD7
        // to get period of 0xFFFEEAA9:
        // b = rotate32(b, 25) + 0xC4DE9951


        // subtracting, rotating, and bitwise NOT:
        // 0xC68E9CF3
        // rotation 13: 0xFEF97E17, also 19 
        // 0xC68E9CB7
        // rotation 12: 0xFE3D7A2E

        // left xorshift
        // 5
        // rotation 15: 0xFFF7E000
        // 13
        // rotation 17: 0xFFFD8000

        // minus left shift, then xor
        // state - (state << 12) ^ 0xC68E9CB7, rotation 21: 0xFFD299CB
        // add xor
        // state + 0xC68E9CB7 ^ 0xDFF4ECB9, rotation 30: 0xFFDAEDF7
        // state + 0xC68E9CB7 ^ 0xB5402ED7, rotation 01: 0xFFE73631
        // state + 0xC68E9CB7 ^ 0xB2B386E5, rotation 24: 0xFFE29F5D
        // sub xor
        // state - 0x9E3779B9 ^ 0xE541440F, rotation 22: 0xFFFC9E3E


        // best power of two:
        // can get 63.999691 with: (period is 0xFFF1F6F18B2A1330)
        // multiplying A by 0x89A7 and rotating left by 13
        // multiplying B by 0xBCFD and rotating left by 17
        // can get 63.998159 with: (period is 0xFFAC703E2B6B1A30)
        // multiplying A by 0x89A7 and rotating left by 13
        // multiplying B by 0xB9EF and rotating left by 23
        // can get 63.998 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // xorshifting B left by 5 (B ^ B << 5) and rotating left by 15
        // can get 63.99 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // adding 0x6C8E9CF7 for B and rotating left by 7
        // can get 63.98 with:
        // adding 0x9E3779B9 for A and rotating left by 2
        // multiplying by 0xACED, NOTing, and rotating left by 28 for B
        // 0xFF6B3AF7L 0xFFAEB037L 0xFFD78FD4L

        // 0xFF42E24AF92DCD8C, 63.995831
        //BigInteger result = BigInteger.valueOf(0xFF6B3AF7L), tmp = BigInteger.valueOf(0xFFD78FD4L);

//        int stateA, i;
//        int r = 0x80001;
//        for (int c = 1; c <= 262144; c++) {
//            r += 2;
//            System.out.printf("%05d/262144, testing r = 0x%08X\n", c, r);
//            for (int j = 1; j < 32; j++) {
//                i = 0;
//                stateA = 1;
//                for (; ; i++) {
//                    if ((stateA = Integer.rotateLeft(stateA, j) * r) == 1) {
//                        if (i >>> 20 == 0xFFF)
//                            System.out.printf("state + 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
//                        break;
//                    }
//                }
//            }
//        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            executor.invokeAll(Arrays.asList(cmr32Runner(1), cmr32Runner(0x40001), cmr32Runner(0x80001), cmr32Runner(0xC0001)));
            executor.awaitTermination(10, TimeUnit.DAYS); // it won't take this long
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        int target;
//            for (int s = 1; s < 32; s++) {
//                for (int r = 1; r < 32; r++) {
//            for (int s : new int[]{5, 9, 27}) {
//                for (int r : new int[]{1, 8, 20}) {
//                    System.out.printf("testing (x << %d) + rotl(x, %d) ... ", s, r);
//                    stateA = 1;
//                    i = 0;
//                    for (int j = 0; j < 0x100000; j++) {
//                        stateA = (stateA << s) + Integer.rotateLeft(stateA, r);
//                    }
//                    target = stateA;
//                    System.out.printf("target is 0x%08X, ", target);
//                    for (; ; ) {
//                        if ((stateA = (stateA << s) + Integer.rotateLeft(stateA, r)) == target) {
//                            System.out.printf("period is 0x%08X\n", i);
//                            break;
//                        }
//                        if (++i == 0) {
//                            System.out.printf("cycled strangely, state ended on 0x%08X\n", stateA);
//                            break;
//                        }
//                    }
//                }
//            }


//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }         
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());

    }

    private static Callable<Integer> cmr32Runner(final int start)
    {
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int stateA, i;
                int r = start;
                for (int c = 1; c <= 131072; c++) {
                    r += 2;
                    System.out.printf("%05d/131072, testing r = 0x%08X\n", c, r);
                    for (int j = 1; j < 32; j++) {
                        i = 0;
                        stateA = 1;
                        for (; ; i++) {
                            if ((stateA = Integer.rotateLeft(stateA, j) * r) == 1) {
                                if (i >>> 20 == 0xFFF)
                                    System.out.printf("state * 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
                                break;
                            }
                        }
                    }
                }
                return r;
            }
        };
    }

    /////// END subcycle finder code and period evaluator
    @Test
    public void showCombined()
    {
        //// 0xFFFFD5A9 0xFFFF687B 0xFFFF2E45 0xFFFF1928 0xFFFEE5E1 0xFFFEAB53 0xFFFE9CA7
        // add 0xFFFF9C61L 0xFFFEEAA9L
        // mul 0xFFFDBF50L 0xFFF43787L 0xFFFD3B83L 0xFFF60EDDL : 127.999411
        BigInteger result = BigInteger.valueOf(0xFFFF9C61L), tmp = BigInteger.valueOf(0xFFFEEAA9L); // 5/1, 6/-4
//        BigInteger result = BigInteger.valueOf(0xFF8F603FL), tmp = BigInteger.valueOf(0xFD6D7E76L); // 5/1, 9/8 
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFF2E45L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFEAB53L);
//        tmp = BigInteger.valueOf(0xFDD16277L); // 27/-14
//        tmp = BigInteger.valueOf(0xFBD0F379L); // 27/20
        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        BigInteger result = BigInteger.valueOf(0xFFFDBF50L), tmp = BigInteger.valueOf(0xFFFD3B83L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFFD3B83L); //mul 0xFFEDA0B5L //add 0xFFF8A98DL
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFF60EDDL); //add 0xFFF8A98DL
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
//        tmp = BigInteger.valueOf(0xFFABD755L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
    }
    @Test
    public void testSubcycle64()
    {
//        long i;
//        long state = 1L;
//        LinnormRNG lin = new LinnormRNG(2L);//3676505223501568873L
//        System.out.println(lin.getState());
        //Random rand = new RNG(lin).asRandom();
        //for (int c = 1; c <= 200; c++) {
            //final int r = lin.nextInt()|1;
//        final long r = 0x7FFFFFFFL;//0x41C64E6BL;//0x41C64E6D;
//            //final int r = BigInteger.probablePrime(32, rand).intValue();
//            //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//        //System.out.printf("%03d/200, testing r = 0x%08X\n", c, r);
//        System.out.printf("testing r = 0x%08X\n", r);
//        for (int j = 3; j < 64; j++) {
//            i = 0L;
//            state = 1L;
//            OUTER:
//            for (; i < 0x40000000000L; ) {
//                for (int k = 0x80000000; k < 0; k++) {
//                    if ((state = (state << j | state >>> -j) * 0x7FFFFFFFL) == 1L) {
//                        //if (i > 0x100000000L)
//                        System.out.printf("state * 0x%08X, rotation %02d: 0x%016X\n", r, j, i);
//                        break OUTER;
//                    }
//                    i++;
//                }
//                System.out.printf("Period is at least 0x%016X\n", i);
//            }
//            System.out.printf("state * 0x%08X, rotation %02d: 0x%016X\n", r, j, i);
//        }
        //}

//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());
        long stateA, i, target;
//        for (int c = 1; c <= 200; c++) {
        //final int r = (Light32RNG.determine(20007 + c) & 0xFFFF)|1;
//            final int r = BigInteger.probablePrime(31, rand).intValue();
        //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//            for (int s = 1; s < 32; s++) {
//                for (int r = 1; r < 32; r++) {
        for (int s : new int[]{5, 9, 27}) {
            for (int r : new int[]{1, 8, 20}) {
                System.out.printf("testing (x << %d) + rotl(x, %d) ...\n", s, r);
                stateA = 1L;
                i = 0L;
                for (int j = 0; j < 0x100000; j++) {
                    stateA = (stateA << s) + Long.rotateLeft(stateA, r);
                }
                target = stateA;
                System.out.printf("target is 0x%08X,\n", target);
                for (; ; ) {
                    if ((stateA = (stateA << s) + Long.rotateLeft(stateA, r)) == target) {
                        System.out.printf("period is 0x%08X\n", i);
                        break;
                    }
                    if (++i == 0) {
                        System.out.printf("cycled strangely, state ended on 0x%08X\n", stateA);
                        break;
                    }
                    else if((i & 0xFFFFFFFFL) == 0L)
                    {
                        System.out.printf("Currently at 0x%016X\n", i);
                    }
                }
            }
        }
    }
    // MiniMover64RNG good-period range finder
    //public static void main(String[] args)
    @Test
    public void miniRangeFinder()
    {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            executor.invokeAll(Arrays.asList(runner(0), runner(0x800000), runner(0x1000000), runner(0x1800000)));
            executor.awaitTermination(10, TimeUnit.DAYS); // it won't take this long
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static Callable<Long> runner(final long start)
    {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long state, ctr = start;
                for (int i = 0; i < 0x80; i++) {
                    for (int j = 0; j < 0x10000; j++) {
                        state = ++ctr;
                        for (int p = 0; p < 0x100000; p++) {
//                    state = (state << 21 | state >>> 43) * 0x9E3779B9L;
                            state = (state << 29 | state >>> 35) * 0xAC564B05L;
                            if (state == ctr) {
                                System.out.println(ctr + " is NOT OKAY, period cycles at " + p);
                                throw new InterruptedException("period cycles at " + p);
                            }
                        }
                        //System.out.println("Seed of " + i + " is okay");
                    }
                    System.out.println("Successfully checked seeds " + (ctr - 0x10000) + " to " + ctr);
                }
                return ctr;
            }
        };
    }
}
