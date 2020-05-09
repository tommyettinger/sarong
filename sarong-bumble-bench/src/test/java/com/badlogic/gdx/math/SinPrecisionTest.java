package com.badlogic.gdx.math;

import sarong.NumberTools;

/**
 * Created by Tommy Ettinger on 10/9/2019.
 */
public class SinPrecisionTest {
    public static void main(String[] args) {
        long expo = 1;
        for (int i = 0; i < 16; i++) {
            expo *= 5;
            float f = expo;
            double d = expo;
            System.out.println("Math.sin       (" + d + "D): " + Math.sin(d));
            System.out.println("NumberTools.sin(" + d + "D): " + NumberTools.sin(d));
            System.out.println("MathUtils.sin  (" + f + "F): " + MathUtils.sin(f));
            System.out.println("NumberTools.sin(" + f + "F): " + NumberTools.sin(f));
            f %= MathUtils.PI2;
            d %= Math.PI * 2.0;
            System.out.println("Bounded:");
            System.out.println("NumberTools.sin(" + d + "D): " + NumberTools.sin(d));
            System.out.println("MathUtils.sin  (" + f + "F): " + MathUtils.sin(f));
            System.out.println("NumberTools.sin(" + f + "F): " + NumberTools.sin(f));
            System.out.println();
            expo *= 2;
            f = expo;
            d = expo;
            System.out.println("Math.sin       (" + d + "D): " + Math.sin(d));
            System.out.println("NumberTools.sin(" + d + "D): " + NumberTools.sin(d));
            System.out.println("MathUtils.sin  (" + f + "F): " + MathUtils.sin(f));
            System.out.println("NumberTools.sin(" + f + "F): " + NumberTools.sin(f));
            f %= MathUtils.PI2;
            d %= Math.PI * 2.0;
            System.out.println("Bounded:");
            System.out.println("NumberTools.sin(" + d + "D): " + NumberTools.sin(d));
            System.out.println("MathUtils.sin  (" + f + "F): " + MathUtils.sin(f));
            System.out.println("NumberTools.sin(" + f + "F): " + NumberTools.sin(f));
            System.out.println();
        }
        float prev = MathUtils.sin(500000f);
        for (int i = 500001; i < 1000000; i++) {
            if(prev == (prev = MathUtils.sin(i)))
            {
                System.out.println("MathUtils.sin() breaks when called with arguments of at least " + i);
                System.out.println("Value there is " + prev);
                break;
            }
        }
    }
}
