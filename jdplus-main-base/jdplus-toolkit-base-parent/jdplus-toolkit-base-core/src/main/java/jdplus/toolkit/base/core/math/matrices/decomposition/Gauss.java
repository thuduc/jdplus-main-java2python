/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.Constants;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class Gauss {

    public static LUDecomposition decompose(FastMatrix M) {
        return decompose(M, Constants.getEpsilon());
    }

    public LUDecomposition decompose(FastMatrix M, double eps) {
        int n = M.getColumnsCount();
        if (M.getRowsCount() != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        double[] lu = M.toArray();
        int[] piv = null;

        for (int k = 0, kn = 0; k < n; ++k, kn += n) {
            // Find pivot.
            int p = k;
            double pmax = Math.abs(lu[kn + k]);
            for (int i = k + 1; i < n; ++i) {
                double cur = Math.abs(lu[kn + i]);
                if (cur > pmax) {
                    p = i;
                    pmax = cur;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                if (piv == null) {
                    piv = new int[n];
                    for (int i = 0; i < n; ++i) {
                        piv[i] = i;
                    }
                }

                for (int j = 0, pj = p, kj = k; j < n; ++j, pj += n, kj += n) {
                    double tmp = lu[pj];
                    lu[pj] = lu[kj];
                    lu[kj] = tmp;
                }
                int t = piv[p];
                piv[p] = piv[k];
                piv[k] = t;
            }
            // Compute multipliers and eliminate k-th column.
            double kk = lu[kn + k];
            if (kk == 0) {
                throw new MatrixException(MatrixException.SINGULAR);
            }

            for (int i = k + 1; i < n; i++) {
                double alpha = lu[i + kn] /= kk;
                for (int j = k + 1, jn = j * n; j < n; j++, jn += n) {
                    lu[i + jn] -= alpha * lu[k + jn];
                }
            }
        }
        return new LUDecomposition(FastMatrix.builder(lu).nrows(n).ncolumns(n).build(), piv);
    }
}
