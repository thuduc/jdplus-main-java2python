/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.Complex;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FiniteFilterTest {

    static final FiniteFilter FILTER;
    static final DataBlock IN;

    static {
        FILTER = FiniteFilter.ofInternal(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8}, -9);
        IN = DataBlock.make(240);
        IN.set(i -> 1 / (1 + i));
    }

    public FiniteFilterTest() {
    }

    @Test
    public void testApply() {
        DataBlock out = DataBlock.make(IN.length() - FILTER.length() + 1);
        FILTER.apply(IN, out);

        DataBlock out2 = DataBlock.make(IN.length() - FILTER.length() + 1);
        FILTER.apply((DoubleSeq) IN, out2);

        assertTrue(out.distance(out2) < 1e-9);
    }

    @Test
    public void testFR() {
        Complex fr = FILTER.frequencyResponse(1.5);
        double g = FILTER.gainFunction().applyAsDouble(1.5);
        double p = FILTER.phaseFunction().applyAsDouble(1.5);
        assertTrue(fr.equals(Complex.polar(g, p), 1e-9));
    }

    public static void main(String[] arg) {
        FiniteFilter f1 = FiniteFilter.ofInternal(new double[]{.125, .25, .25, .25, .125}, -2);
        FiniteFilter f2 = FiniteFilter.ofInternal(new double[]{.25, .25, .25, .25}, -3);
        double[] w = new double[13];
        for (int i = 1; i < 12; ++i) {
            w[i] = 1.0 / 12;
        }
        w[0] = w[12] = 1.0 / 24;
        FiniteFilter f3 = FiniteFilter.ofInternal(w, -6);
        display(f1);
        display(f2);
        display(f3);
        display(HendersonFilters.ofLength(13));

        w = new double[25];
        w[0] = 1.0 / 3;
        w[12] = 1.0 / 3;
        w[24] = 1.0 / 3;
        FiniteFilter s1 = FiniteFilter.ofInternal(w, -12);
        display(s1);

        w = new double[10 * 12 + 1];
        for (int i = 24; i < 108; i += 12) {
            w[i] = 1.0 / 9;
        }
        w[0] = w[120] = 1.0 / 27;
        w[12] = w[108] = 2.0 / 27;
        FiniteFilter s9 = FiniteFilter.ofInternal(w, -60);
        display(s9);
        SymmetricFilter h11 = HendersonFilters.ofLength(11);
        double[] hw = h11.weightsToArray();
        for (int i=0; i<hw.length; ++i){
            w[12*i]=hw[i];
        }
        FiniteFilter s9h = FiniteFilter.ofInternal(w, -60);
        display(s9h);
    }

    private static void display(IFiniteFilter f) {
        double[] g = new double[101], p = new double[101];
        for (int i = 0; i <= 100; ++i) {
            double w = i * Math.PI / 100;
            Complex c = f.frequencyResponse(w);
            g[i] = c.abs();
            p[i] = c.arg();
        }
        System.out.println(DoubleSeq.of(g));
        System.out.println(DoubleSeq.of(p));
    }

    public static void stressTest() {
        int K = 1000000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlock out = DataBlock.make(IN.length() - FILTER.length() + 1);
            FILTER.apply(IN, out);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlock out = DataBlock.make(IN.length() - FILTER.length() + 1);
            FILTER.apply((DoubleSeq) IN, out);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
