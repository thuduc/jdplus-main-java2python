/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.core.math.matrices.decomposition;

import ec.tstoolkit.random.JdkRNG;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.CholeskyWithPivoting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class CholeskyWithPivotingTest {

    public CholeskyWithPivotingTest() {
    }

    @Test
    public void testlcholesky() {
        FastMatrix X = FastMatrix.make(100, 50);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        CholeskyWithPivoting pchol = new CholeskyWithPivoting();
        pchol.decompose(S, -1);
        FastMatrix T = pchol.getL();
//        System.out.println(T.diagonal());

        FastMatrix A = S.deepClone();
        SymmetricMatrix.lcholesky(S);
//        System.out.println(S.diagonal());

        LogSign lst = LogSign.of(T.diagonal());
        LogSign lss = LogSign.of(S.diagonal());

        assertEquals(lst.getValue(), lss.getValue(), 1e-9);

        int[] order = order(pchol.getPivot());
        FastMatrix Tc = MatrixFactory.select(T, order, null);

        FastMatrix D = SymmetricMatrix.XXt(Tc).minus(A);
        double nm = MatrixNorms.infinityNorm(D);
        assertEquals(nm,0,1e-9);

    }

    private int[] order(int[] p) {
        int[] o = new int[p.length];
        for (int i = 0; i < p.length; ++i) {
            o[p[i]] = i;
        }
        return o;
    }

    public static void main(String[] arg) {

        FastMatrix X = FastMatrix.make(150, 12);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        int K = 1000000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            FastMatrix T = S.deepClone();
            SymmetricMatrix.lcholesky(T);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            CholeskyWithPivoting pchol = new CholeskyWithPivoting();
            pchol.decompose(S, -1);
            pchol.getL();
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

}
