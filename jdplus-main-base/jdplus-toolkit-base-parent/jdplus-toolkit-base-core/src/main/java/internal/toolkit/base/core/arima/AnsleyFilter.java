/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or as soon they will be approved 
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
package internal.toolkit.base.core.arima;

import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm=ArmaFilter.class)
@ServiceProvider(ArmaFilter.class)
public class AnsleyFilter implements ArmaFilter {

    private FastMatrix L;
    private double[] ar, ma;
    private double var;
    private int n;

    /**
     *
     * @param y
     * @return
     */
    public double[] filter(DoubleSeq y) {
        double[] e = y.toArray();
        int p = ar.length-1;
        int q = ma.length-1;
        if (p == 0 && q == 0) {
            if (var != 1) {
                double std = Math.sqrt(var);
                for (int i = 0; i < e.length; ++i) {
                    e[i] /= std;
                }
            }
            return e;
        }
        if (p > 0) {
            for (int i = e.length - 1; i >= p; --i) {
                double s = 0;
                for (int j = 1; j <= p; ++j) {
                    s += ar[j] * e[i - j];
                }
                e[i] += s;
            }
        }

        rsolve(e);
        return e;
    }

    /**
     *
     * @param y
     * @param yf
     */
    @Override
    public void apply(DoubleSeq y, DataBlock yf) {
        double[] e = filter(y);
        yf.copyFrom(e, 0);
    }

    @Override
    public double getLogDeterminant() {
        if (L == null) {
            return n*Math.log(var);
        } else {
            DataBlock diag = L.row(0);
            return 2 * LogSign.of(diag).getValue();
        }
    }

    @Override
    public int prepare(final IArimaModel arima, int n) {
        this.n = n;
        L = null;
        ar = arima.getAr().asPolynomial().toArray();
        var = arima.getInnovationVariance();
        ma = arima.getMa().asPolynomial().toArray();
        int p = ar.length-1, q = ma.length-1;
        if (p == 0 && q == 0) {
            return n;
        }
        int r = Math.max(p, q + 1);
        double[] cov = null, dcov = null;
        if (p > 0) {
            cov = arima.getAutoCovarianceFunction().values(r);
            double[] psi = arima.getPsiWeights().getWeights(q);
            dcov = new double[r];
            for (int i = 1; i <= q; ++i) {
                double v = ma[i];
                for (int j = i + 1; j <= q; ++j) {
                    v += ma[j] * psi[j - i];
                }
                dcov[i] = v * var;
            }
        }

        IntToDoubleFunction sma = SymmetricFilter.convolutionOf(arima.getMa(), var).weights();

        L = FastMatrix.make(r, n);
        // complete the matrix
        // if (i >= j) m(i, j) = lband[i-j, j]; if i-j >= r, m(i, j) =0
        // if (i < j) m(i, j) = lband(j-i, i)

        DataBlockIterator cols = L.columnsIterator();
        for (int j = 0; j < p; ++j) {
            DataBlock col = cols.next();
            for (int i = 0; i < p - j; ++i) {
                col.set(i, cov[i]);
            }
            for (int i = p - j; i < r; ++i) {
                col.set(i, dcov[i]);
            }
        }

        FastMatrix M = L.extract(0, q + 1, p, n-p);
        DataBlockIterator rows = M.rowsIterator();

        int pos=0;
        while (rows.hasNext()) {
            double s=sma.applyAsDouble(pos++);
            DataBlock row = rows.next();
            if ( s!= 0) {
                row.set(s);
            }
        } 

        lcholesky();
        return n;
    }

    private void lcholesky() {
        int r = L.getRowsCount();
        double[] data = L.getStorage();
        if (r == 1) {
            for (int i = 0; i < data.length; ++i) {
                if (data[i] <= 0) {
                    throw new MatrixException(MatrixException.CHOLESKY);
                }
                data[i] = Math.sqrt(data[i]);
            }
        } else {
            // The diagonal item is the first row !
            int dr = r - 1, drr = dr * dr;
            for (int i = 0, idiag = 0; i < n; ++i, idiag += r) {
                // compute aii;
                double aii = data[idiag];
                int rmin = idiag - drr;
                if (rmin < 0) {
                    rmin = 0;
                }
                int rcur = idiag - dr;
                while (rcur >= rmin) {
                    double x = data[rcur];
                    if (x != 0) {
                        aii -= x * x;
                    }
                    rcur -= dr;
                }
                if (aii <= 0) {
                    throw new MatrixException(MatrixException.CHOLESKY);
                }
                aii = Math.sqrt(aii);
                data[idiag] = aii;

                // compute elements i+1 : n of column i
                rcur = idiag - dr;
                int k = i + r - 1;
                while (rcur >= rmin) {
                    double x = data[rcur];
                    if (x != 0) {
                        int q = Math.min(k, n) - i;
                        for (int iy = idiag + 1, ia = rcur + 1; ia < rcur + q; ++ia, ++iy) {
                            data[iy] -= x * data[ia];
                        }
                    }
                    rcur -= dr;
                    --k;
                }
                int ymax = r * (i + 1);
                for (int iy = idiag + 1; iy < ymax; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
    }

    // / <summary>
    // / Lx=b or L^-1 * b = x
    // / </summary>
    // / <param name="b"> On entry: b, on exit: x</param>
    /**
     *
     * @param b
     */
    private void rsolve(double[] b) {
        int r = L.getRowsCount();

        double[] data = L.getStorage();

        int nb = b.length;

        int i = 0;
        while (i < nb && b[i] == 0) {
            ++i;
        }
        for (int idx = i * r; i < nb; ++i, idx += r) {
            double t = b[i] / data[idx];

            int jmax = Math.min(r, n - i);
            for (int j = 1, k = idx + 1; j < jmax; ++j, ++k) {
                b[i + j] -= t * data[k];
            }
            b[i] = t;
        }
    }

    public FastMatrix getCholeskyFactor() {
        if (L == null) {
            FastMatrix l = FastMatrix.make(1, n);
            l.set(Math.sqrt(var));
            return l;
        } else {
            return L;
        }
    }

}
