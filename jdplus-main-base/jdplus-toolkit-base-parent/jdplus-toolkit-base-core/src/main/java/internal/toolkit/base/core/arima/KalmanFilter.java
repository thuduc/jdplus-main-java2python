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
package internal.toolkit.base.core.arima;

import jdplus.toolkit.base.core.arima.ArimaException;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import static jdplus.toolkit.base.api.design.AlgorithmImplementation.Feature.Fast;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm = ArmaFilter.class, feature = Fast)
@ServiceProvider(ArmaFilter.class)
public class KalmanFilter implements ArmaFilter {

    private double[] C0, C, s;

    private final boolean multiUse;

    private double[] phi;

    private int dim, n, steadyPos;

    private double ldet = Double.NaN, h0, var;

    private static final double EPS = 1e-15;

    /**
     *
     */
    public KalmanFilter() {
        multiUse = false;
    }

    /**
     *
     * @param multiuse
     */
    public KalmanFilter(boolean multiuse) {
        multiUse = multiuse;
    }

    private void calcC() {

        double[] L = C0.clone();
        C = new double[dim * n];
        for (int i = 0; i < dim; ++i) {
            C[i] = L[i];
        }
        s = new double[n];
        double h = h0;

        DeterminantalTerm det = new DeterminantalTerm();
        int pos = 0, ccur = 0, cnext = dim, ilast = dim - 1;
        steadyPos = n - 1; // we don't need to update the last run
        do {
            if (Double.isNaN(h) || h < 0) {
                throw new ArimaException(ArimaException.INVALID);
            }
            det.add(h);
            s[pos] = Math.sqrt(h);
            if (pos < steadyPos) {
                double zl = L[0];
                double zlv = zl / h;
                double llast = tlast(L), clast = C[ccur + ilast];

                // C, L
                for (int i = 0, j = ccur, k = cnext; i < ilast; ++i, ++j, ++k) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[j] * zlv;
                        C[k] = C[j] - zlv * li;
                    } else {
                        L[i] = li;
                        C[k] = C[j];
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[cnext + ilast] = C[ccur + ilast] - zlv * llast;
                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                int k = 0;
                for (; k < L.length; ++k) {
                    if (Math.abs(L[k]) > EPS) {
                        break;
                    }
                }
                if (k == L.length) {
                    steadyPos = pos + 1;
                }
                ccur = cnext;
                cnext += dim;
            }
        } while (++pos < n);

        ldet = det.getLogDeterminant();
    }

    private void calcdet() {
        DeterminantalTerm det = new DeterminantalTerm();
        double[] C = C0.clone();
        double[] L = C.clone();
        double h = h0;

        // iteration
        int pos = 0, ilast = dim - 1;
        steadyPos = n;
        do {
            if (Double.isNaN(h) || h < 0) {
                throw new ArimaException(ArimaException.INVALID);
            }
            det.add(h);
            // filter x if any

            if (pos < steadyPos) {
                double zl = L[0];
                double zlv = zl / h;

                double llast = tlast(L), clast = C[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[i] * zlv;
                        C[i] -= zlv * li;
                    } else {
                        L[i] = li;
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[ilast] -= zlv * llast;

                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                int k = 0;
                for (; k < L.length; ++k) {
                    if (Math.abs(L[k]) > EPS) {
                        break;
                    }
                }
                if (k == L.length) {
                    steadyPos = pos + 1;
                }
            } else if (h == 1) {
                break;
            }
        } while (++pos < n);

        ldet = det.getLogDeterminant();
    }

    /**
     *
     * @param y
     * @param outrc
     */
    @Override
    public void apply(DoubleSeq y, DataBlock outrc) {
        if (multiUse) {
            mfilter(y, outrc);
        } else {
            sfilter(y, outrc);
        }
    }

    @Override
    public double getLogDeterminant() {
        if (Double.isNaN(ldet)) {
            calcdet();
        }
        return ldet;
    }

    @Override
    public int prepare(final IArimaModel model, int length) {
        var = model.getInnovationVariance();
        ldet = Double.NaN;
        phi = model.getAr().asPolynomial().toArray();
        dim = Math.max(model.getArOrder(), model.getMaOrder() + 1);
        C0 = model.getAutoCovarianceFunction().values(dim);
        h0 = C0[0];
        n = length;
        tx(C0);

        if (multiUse) {
            calcC();
        }
        return length;
    }

    private void mfilter(DoubleSeq y, DataBlock yf) {

        double[] a = new double[dim];
        // iteration
        DoubleSeqCursor yreader = y.cursor();
        DoubleSeqCursor.OnMutable yfwriter = yf.cursor();
        int pos = 0, cpos = 0, ilast = dim - 1;
        boolean started = false;
        do {
            double z = yreader.getAndNext();
            if (z != 0) {
                started = true;
            }
            double s = this.s[pos];
            double e = (z - a[0]) / s;
            yfwriter.setAndNext(e);
            // update state
            if (started) {
                double la = tlast(a);
                // update the state
                if (e != 0) {
                    double v = e / s;
                    for (int i = 0, j = cpos; i < ilast; ++i, ++j) {
                        a[i] = a[i + 1] + C[j] * v;
                    }
                    a[ilast] = la + C[cpos + ilast] * v;
                } else {
                    for (int i = 0; i < ilast; ++i) {
                        a[i] = a[i + 1];
                    }
                    a[ilast] = la;
                }
            }
            // filter x if any
            if (pos < steadyPos) {
                cpos += dim;
            }

        } while (++pos < n);
    }

    private void sfilter(DoubleSeq y, DataBlock outrc) {
        DeterminantalTerm det = new DeterminantalTerm();
        double[] C = C0.clone();
        double[] L = C.clone();
        double h = h0;

        double[] a = new double[dim];
        double[] yf = new double[n];
        // iteration
        int pos = 0, ilast = dim - 1;
        steadyPos = n - 1; // we don't need to update the last run
        do {
            if (Double.isNaN(h) || h < 0) {
                throw new ArimaException(ArimaException.INVALID);
            }
            // filter y
            det.add(h);
            double s = Math.sqrt(h);
            double e = y.get(pos) - a[0];
            yf[pos] = e / s;
            double la = tlast(a);
            // update the state
            if (e != 0) {
                double v = e / h;
                for (int i = 0; i < ilast; ++i) {
                    a[i] = a[i + 1] + C[i] * v;
                }
                a[ilast] = la + C[ilast] * v;
            } else {
                for (int i = 0; i < ilast; ++i) {
                    a[i] = a[i + 1];
                }
                a[ilast] = la;
            }
            // next
            if (pos < steadyPos) {
                double zl = L[0];
                double zlv = zl / h;
                double llast = tlast(L), clast = C[ilast];

                // C, L
                for (int i = 0; i < ilast; ++i) {
                    double li = L[i + 1];
                    if (zlv != 0) {
                        L[i] = li - C[i] * zlv;
                        C[i] -= zlv * li;
                    } else {
                        L[i] = li;
                    }
                }

                L[ilast] = llast - zlv * clast;
                C[ilast] -= zlv * llast;
                h -= zl * zlv;
                if (h < var) {
                    h = var;
                }
                int k = 0;
                for (; k < L.length; ++k) {
                    if (Math.abs(L[k]) > EPS) {
                        break;
                    }
                }
                if (k == L.length) {
                    steadyPos = pos + 1;
                }
            }

            // filter x if any
        } while (++pos < n);

        ldet = det.getLogDeterminant();
        outrc.copy(DataBlock.of(yf));

    }

    private double tlast(final double[] x) {
        double last = 0;
        for (int i = 1; i < phi.length; ++i) {
            last -= phi[i] * x[dim - i];
        }
        return last;
    }

    private void tx(final double[] x) {
        double last = 0;
        for (int i = 1; i < phi.length; ++i) {
            last -= phi[i] * x[dim - i];
        }
        for (int i = 1; i < dim; ++i) {
            x[i - 1] = x[i];
        }
        x[dim - 1] = last;

    }
}
