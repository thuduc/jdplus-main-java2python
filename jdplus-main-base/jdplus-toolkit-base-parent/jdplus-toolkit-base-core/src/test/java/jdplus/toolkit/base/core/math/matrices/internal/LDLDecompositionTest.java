/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.internal;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.math.matrices.decomposition.LDLDecomposition;

/**
 *
 * @author palatej
 */
public class LDLDecompositionTest {

    static FastMatrix M(int n) {
        FastMatrix M = FastMatrix.square(n);
        Random rnd = new Random(0);
        M.set((i, j) -> i == j ? 1.0 : (i < j ? 0.0 : rnd.nextDouble()));
        return M;
    }

    @Test
    public void LDLDecompositionTest() {

        for (int n = 3; n < 50; ++n) {
            double[] d = new double[n];
            FastMatrix L = M(n).deepClone();
            for (int i = 0; i < n; ++i) {
                d[i] = i - n / 2;
                if (d[i] == 0) {
                    L.column(i).range(i + 1, n).set(0);
                }
            }
            FastMatrix D = FastMatrix.diagonal(DoubleSeq.of(d));
            FastMatrix S = SymmetricMatrix.XSXt(D, L);
            LDLDecomposition ldl = new LDLDecomposition();
            ldl.decompose(S, 1e-9);
            FastMatrix del = ldl.L().minus(L);
            assertTrue(MatrixNorms.frobeniusNorm(del) < 1e-9);
        }
    }

    @Test
    public void LDLDecompositionTest2() {

        for (int n = 3; n < 50; ++n) {
            double[] d = new double[n];
            FastMatrix L = M(n).deepClone();
            for (int i = 0; i < n; ++i) {
                d[i] = i % 3 == 0 ? 0 : i - n / 2;
                if (d[i] == 0) {
                    L.column(i).range(i + 1, n).set(0);
                }
            }
            FastMatrix D = FastMatrix.diagonal(DoubleSeq.of(d));
            FastMatrix S = SymmetricMatrix.XSXt(D, L);
            LDLDecomposition ldl = new LDLDecomposition();
            ldl.decompose(S, 1e-9);
            FastMatrix del = ldl.L().minus(L);
            assertTrue(MatrixNorms.frobeniusNorm(del) < 1e-9);
        }
    }

}
