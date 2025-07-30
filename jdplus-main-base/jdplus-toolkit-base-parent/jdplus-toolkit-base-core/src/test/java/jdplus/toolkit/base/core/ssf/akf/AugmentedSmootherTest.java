/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class AugmentedSmootherTest {

    public AugmentedSmootherTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    public static void main(String[] arg) {
        SarimaOrders spec = SarimaOrders.m011(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(1, -.9)
                .build();
        Ssf ssf = Ssf.of(SsfArima.stateComponent(arima), SsfArima.defaultLoading());
        SsfData data = new SsfData(Data.NILE);
        int n = data.length();
        FastMatrix X = FastMatrix.make(n, 2);
        X.set(36, 0, 1);
        X.set(0, 1, 1);
        Ssf xssf = RegSsf.ssf(ssf, X);
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(true);
        DefaultSmoothingResults sd = DefaultSmoothingResults.full();
        sd.prepare(xssf.getStateDim(), 0, data.length());
        smoother.process(xssf, data, sd);
        double sig2 = smoother.getFilteringResults().var();

        for (int i = 0; i < n; ++i) {
//            System.out.print(sd.smoothation(i));
            System.out.print(sd.smoothation(i) / sd.smoothationVariance(i));
            System.out.print('\t');
            if (i != 36 && i != 0) {
                FastMatrix W = FastMatrix.make(n, 3);
                W.set(36, 0, 1);
                W.set(0, 1, 1);
                W.set(i, 2, 1);
                Ssf wssf = RegSsf.ssf(ssf, W);
                DefaultSmoothingResults sr = DkToolkit.smooth(wssf, data, false, true);
                double last = sr.a(0).getLast();
                System.out.print(last);
            }
                System.out.print('\t');
            System.out.print(sd.smoothation(i) * sd.smoothation(i) / sd.smoothationVariance(i) / sig2);
            System.out.print('\t');
            DataBlock R = DataBlock.of(sd.R(i));
            FastMatrix Rvar = sd.RVariance(i).deepClone();
            System.out.print(R.get(0) / Rvar.get(0, 0));
            System.out.print('\t');
            try {
                SymmetricMatrix.lcholesky(Rvar, 1e-9);
                LowerTriangularMatrix.solveLx(Rvar, R, 1e-9);
                System.out.println(R.ssq() / sig2);
            } catch (Exception err) {
                System.out.println("");
            }

        }

    }
}
