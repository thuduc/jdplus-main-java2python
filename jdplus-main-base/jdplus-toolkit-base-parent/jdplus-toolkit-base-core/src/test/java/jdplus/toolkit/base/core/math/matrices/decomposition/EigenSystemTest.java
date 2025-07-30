/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.api.math.Complex;
import ec.tstoolkit.random.JdkRNG;
import internal.toolkit.base.core.math.linearfilters.SymmetricFilterAlgorithms;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.ComplexUtility;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import static jdplus.toolkit.base.core.math.matrices.GeneralMatrix.AB;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class EigenSystemTest {

    public EigenSystemTest() {
    }

    @Test
    public void testVectori() {
        FastMatrix X = FastMatrix.make(20, 15);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        IEigenSystem eigen = EigenSystem.create(S, true);
        eigen.setComputingEigenVectors(true);
        eigen.compute();
        double[] v = eigen.getEigenVector(0);
        FastMatrix W = new FastMatrix(v, X.getColumnsCount(), 1);
        FastMatrix AB = AB(S, W);
        Complex v0 = eigen.getEigenValues()[0];
        W.addAY(-1 / v0.getRe(), AB);
        assertTrue(MatrixNorms.infinityNorm(W)<1e-9);
    }

    @Test
    public void testPolynomialRoots() {
        int K = 50;
        double[] p = new double[K];
        double k = 1.0 / K;
        for (int i = 0; i < K; ++i) {
            p[i] = 1 - k * i;
        }
        long t0 = System.currentTimeMillis();
        Polynomial P = Polynomial.of(p);
        SymmetricFilter S = SymmetricFilter.convolutionOf(P, 1);
        SymmetricFilter.Factorization fac = SymmetricFilterAlgorithms.robustFactorizer().factorize(S);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        int n = S.length() - 1, m = S.getLowerBound();
        FastMatrix M = FastMatrix.square(n);
        M.subDiagonal(1).set(1);
        DataBlock row = M.row(n - 1);
        IntToDoubleFunction weights = S.weights();
        double s0 = weights.applyAsDouble(S.getUpperBound());
        row.set(i -> -weights.applyAsDouble(m + i) / s0);

        IEigenSystem es = EigenSystem.create(M, false);
        Complex[] vals = es.getEigenValues();
        Complex[] nvals = new Complex[vals.length / 2];
        for (int i = 0, j = 0; i < vals.length; ++i) {
            if (vals[i].abs() > 1) {
                nvals[j++] = vals[i];
            }
        }
        ComplexUtility.lejaOrder(nvals);
        Polynomial Z = Polynomial.fromComplexRoots(nvals);
        Z = Z.times(1 / Z.get(0));
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(P.coefficients());
        System.out.println(fac.factor.asPolynomial().coefficients());
        System.out.println(Z.coefficients());
    }

}
