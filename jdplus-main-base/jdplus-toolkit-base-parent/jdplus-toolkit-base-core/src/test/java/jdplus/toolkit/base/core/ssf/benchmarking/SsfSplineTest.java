/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.benchmarking;

import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.splines.CubicSpline;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class SsfSplineTest {

    public SsfSplineTest() {
    }

    @Test
    public void testEquidistant() {
        double[] x = new double[]{-3, 20, -10, 5};
        double[] s = new double[25];
        for (int i = 0; i < s.length; ++i) {
            s[i] = Double.NaN;
        }
        for (int i = 0; i < x.length; ++i) {
            s[(i + 1) * 5] = x[i];
        }
        ISsf ssf = Ssf.of(SsfSpline.of(1), SsfSpline.defaultLoading());
        DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new SsfData(DoubleSeq.of(s)));
        DoubleSeq component = sr.item(0);

        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20}, x);

        for (int i = 0; i < 25; ++i) {
            assertEquals(component.get(i), fn.applyAsDouble(i), 1e-9);
        }
        DefaultSmoothingResults r = DkToolkit.smooth(ssf, new SsfData(DoubleSeq.of(s)), true, true);
        System.out.println(r.getComponentVariance(1));
    }

    public static void stressTest() {
        double[] x = new double[]{-3, 20, -10, 5, 6, 50, -10, 8, 9, 60, 100, 50};
        double[] s = new double[(x.length + 1) * 5];
        for (int i = 0; i < s.length; ++i) {
            s[i] = Double.NaN;
        }
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; ++i) {
            s[(i + 1) * 5] = x[i];
            z[i] = (i + 1) * 5;
        }
        int K = 100000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
        ISsf ssf = Ssf.of(SsfSpline.of(1), SsfSpline.defaultLoading());
            DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new SsfData(DoubleSeq.of(s)));
            DoubleSeq component = sr.item(0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleUnaryOperator fn = CubicSpline.of(z, x);

            for (int i = 0; i < s.length; ++i) {
                double q=fn.applyAsDouble(i);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
    
    public static void main(String[] args){
        stressTest();
    }
}
