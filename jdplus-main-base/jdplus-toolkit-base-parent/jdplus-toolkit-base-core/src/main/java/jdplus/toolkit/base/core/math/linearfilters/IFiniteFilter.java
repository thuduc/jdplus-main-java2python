/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;

import java.util.Locale;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.math.Complex;
import java.util.Formatter;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFiniteFilter extends IFilter {

    /**
     * Length of the filter
     *
     * @return
     */
    default int length() {
        return getUpperBound() - getLowerBound() + 1;
    }

    @Override
    default boolean hasLowerBound() {
        return true;
    }

    @Override
    default boolean hasUpperBound() {
        return true;
    }

    // FiniteFilterDecomposition Decompose();
    /**
     * Lower bound of the filter (included)
     *
     * @return
     */
    int getLowerBound();

    /**
     * Upper bound of the filter (included)
     *
     * @return
     */
    int getUpperBound();

    /**
     * Weights of the filter; the function is defined for index ranging from the
     * lower bound to the upper bound (included)
     *
     * @return
     */
    IntToDoubleFunction weights();

    /**
     * Returns all the weights, from lbound to ubound
     *
     * @return
     */
    default double[] weightsToArray() {
        double[] w = new double[length()];
        IntToDoubleFunction weights = weights();
        for (int i = 0, j = getLowerBound(); i < w.length; ++i, ++j) {
            w[i] = weights.applyAsDouble(j);
        }
        return w;
    }

    @Override
    default Complex frequencyResponse(final double freq) {
        return FilterUtility.frequencyResponse(weights(), getLowerBound(), getUpperBound(), freq);
    }

    /**
     * If this filter is w(l)B^(-l)+...+w(u)F^u Its mirror is
     * w(-u)B^(u)+...+w(-l)F^(-l)
     *
     * @return A new filter is returned
     */
    IFiniteFilter mirror();

    /**
     * Applies the filter on the input
     *
     * @param in The input, which must have the same length as the filter
     * @return The product of the filter and of the input
     */
    default double apply(DoubleSeq in) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        double s = 0;
        DoubleSeqCursor cursor = in.cursor();
        for (int j = lb; j <= ub; ++j) {
            s += weights.applyAsDouble(j) * cursor.getAndNext();
        }
        return s;
    }

    /**
     * Applies the filter on the input y(t)
     *
     * @param in The buffer containing the input
     * @param pos The position of y(t) in the buffer
     * @param inc The increment between two successive inputs.
     * y(t+k)=buffer[pos+k*inc]
     * @return sum(w(k)* buffer[pos+k*inc]), k in [-lb, ub]
     */
    default double apply(double[] in, int pos, int inc) {
        IntToDoubleFunction weights = weights();
        int lb = getLowerBound(), ub = getUpperBound();
        double s = 0;
        if (inc == 1) {
            for (int k = lb, t = pos + lb; k <= ub; ++k, ++t) {
                s += in[t] * weights.applyAsDouble(k);
            }
        } else {
            for (int k = lb, t = pos + lb * inc; k <= ub; ++k, t += inc) {
                s += in[t] * weights.applyAsDouble(k);
            }
        }
        return s;
    }

    /**
     * Filters in and sets the result in out. More exactly, in contains x(-lb,
     * n+ub) and out contains y(0, n) with y(t) = f(t-lb,...,t+ub).
     *
     * @param in Input.
     * @param out Output
     */
    default void apply(DoubleSeq in, DoubleSeq.Mutable out) {
        double[] w = weightsToArray();
        int nz = 0, nw = w.length, nw2 = nw >> 1;
        boolean sparse = true;
        for (int i = 0; i < nw; ++i) {
            if (w[i] != 0 && ++nz > nw2) {
                sparse = false;
                break;
            }
        }
        if (sparse) {
            int len = in.length() - w.length + 1;
            out.setAY(w[0], in.range(0, len));
            for (int j = 1; j < nw; ++j) {
                if (w[j] != 0) {
                    out.addAY(w[j], in.range(j, len + j));
                }
            }
        } else {
            int n = out.length();

            DoubleSeqCursor.OnMutable cursor = out.cursor();
            DoubleSeqCursor icur = in.cursor();
            for (int i = 0; i < n; ++i) {
                icur.moveTo(i);
                double s = 0;
                for (int k = 0; k < nw; ++k) {
                    s += icur.getAndNext() * w[k];
                }
                cursor.setAndNext(s);
            }
        }
    }

    /**
     * Filters in and sets the result in out. More exactly, in contains x(-lb,
     * n+ub) and out contains y(0, n) with y(t) = f(t-lb,...,t+ub). Faster
     * variant of the generic "apply" method
     *
     * @param in Input. Should not be modified
     * @param out Output
     */
    default void apply(DataBlock in, DataBlock out) {
        double[] w = weightsToArray();
        double[] xin = in.getStorage();
        int start = in.getStartPosition(), inc = in.getIncrement();
        int n = out.length();
        double[] xout = out.getStorage();
        int ostart = out.getStartPosition(), oinc = out.getIncrement();
        if (inc == 1) {
            for (int i = 0, j = start, o = ostart; i < n; ++i, ++j, o += oinc) {
                double s = 0;
                for (int k = 0, t = j; k < w.length; ++k, ++t) {
                    s += xin[t] * w[k];
                }
                xout[o] = s;
            }
        } else {
            for (int i = 0, j = start, o = ostart; i < n; ++i, j += inc, o += oinc) {
                double s = 0;
                for (int k = 0, t = j; k < w.length; ++k, t += inc) {
                    s += xin[t] * w[k];
                }
                xout[o] = s;
            }
        }
    }

    default void apply2(DataBlock in, DataBlock out) {
        IntToDoubleFunction weights = weights();
        int l=getLowerBound(), u=getUpperBound(), n=out.length();
        for (int i=l, j=0; i<=u; ++i, ++j){
            double w = weights.applyAsDouble(i);
            if (w != 0){
                out.addAY(w, in.range(j, j+n));
            }
        }
    }

    static String toString(IFiniteFilter filter) {
        StringBuilder sb = new StringBuilder();
        String fmt = "%6g";
        boolean sign = false;
        IntToDoubleFunction weights = filter.weights();
        for (int i = filter.getLowerBound(); i <= filter.getUpperBound(); ++i) {
            double v = weights.applyAsDouble(i);
            double av = Math.abs(v);
            if (av >= 1e-6) {
                if (av > v) {
                    sb.append(" - ");
                } else if (sign) {
                    sb.append(" + ");
                }
                if ((av != 1) || (i == 0)) {
                    sb.append(new Formatter(Locale.ROOT).format(fmt, av).toString());
                }
                sign = true;
                if (i < 0) {
                    sb.append("(t-").append(-i).append(')');
                } else if (i > 0) {
                    sb.append("(t+").append(i).append(')');
                } else {
                    sb.append("(t)");
                }
            }
        }
        return sb.toString();
    }

    public static boolean equals(IFiniteFilter f1, IFiniteFilter f2, double eps) {
        int l = f1.getLowerBound(), u = f1.getUpperBound();
        if (l != f2.getLowerBound() || u != f2.getUpperBound()) {
            return false;
        }
        IntToDoubleFunction w = f1.weights(), fw = f2.weights();
        for (int i = l; i <= u; ++i) {
            if (Math.abs(w.applyAsDouble(i) - fw.applyAsDouble(i)) > eps) {
                return false;
            }
        }
        return true;
    }
}
