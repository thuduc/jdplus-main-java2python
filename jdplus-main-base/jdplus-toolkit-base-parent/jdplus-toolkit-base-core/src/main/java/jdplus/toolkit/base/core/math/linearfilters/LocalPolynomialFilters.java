/*
 * Copyright 2023 National Bank of Belgium.
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
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.Householder2;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;

/**
 * The local polynomial filter is defined as follows: h is the number of lags
 * (-> length of the filter is 2*h+1) d is the order of the local polynomial ki
 * (local weight); we suppose that k(i) = k(-i) (symmetric filters; other
 * filters could be considered)
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {

    public IQuasiSymmetricFiltering of(LocalPolynomialFilterSpec spec) {
        return spec.isSymmetric() ? new SFilter(spec) : new Filter(spec);
    }

    public ISymmetricFiltering ofSymmetric(LocalPolynomialFilterSpec spec) {
        if (!spec.isSymmetric()) {
            throw new IllegalArgumentException();
        }
        return new SFilter(spec);
    }

    private static IntToDoubleFunction kernel(LocalPolynomialFilterSpec spec) {
        int len = spec.getFilterHorizon();
        return switch (spec.getKernel()) {
            case BiWeight ->
                DiscreteKernel.biweight(len);
            case TriWeight ->
                DiscreteKernel.triweight(len);
            case TriCube ->
                DiscreteKernel.tricube(len);
            case Uniform ->
                DiscreteKernel.uniform(len);
            case Triangular ->
                DiscreteKernel.triangular(len);
            case Epanechnikov ->
                DiscreteKernel.epanechnikov(len);
            case Henderson ->
                DiscreteKernel.henderson(len);
            case Trapezoidal ->
                DiscreteKernel.trapezoidal(len);
            default ->
                null;
        };
    }

    private static class SFilter implements ISymmetricFiltering {

        private final SymmetricFilter symmetricFilter;
        private final IFiniteFilter[] asymmetricFilters;

        private SFilter(LocalPolynomialFilterSpec spec) {
            int len = spec.getFilterHorizon();
            symmetricFilter = ofDefault(len, spec.getPolynomialDegree(), kernel(spec));
            asymmetricFilters = switch (spec.getAsymmetricFilters()) {
                case CutAndNormalize ->
                    AsymmetricFiltersFactory.cutAndNormalizeFilters(symmetricFilter);
                case MMSRE ->
                    AsymmetricFiltersFactory.mmsreFilters(symmetricFilter,
                    spec.getAsymmetricPolynomialDegree(), spec.getRightLinearModelCoefficients(), null,
                    spec.getPassBand(), spec.getTimelinessWeight());
                default ->
                    directAsymmetricFilters(len, spec.getPolynomialDegree(), kernel(spec));
            };
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, symmetricFilter, asymmetricFilters);
        }

        @Override
        public SymmetricFilter centralFilter() {
            return symmetricFilter;
        }

        @Override
        public IFiniteFilter[] endPointsFilters() {
            return asymmetricFilters;
        }
    }

    private static class Filter implements IQuasiSymmetricFiltering {

        private final SymmetricFilter symmetricFilter;
        private final IFiniteFilter[] leftAsymmetricFilters, rightAsymmetricFilters;

        private Filter(LocalPolynomialFilterSpec spec) {
            int len = spec.getFilterHorizon();
            symmetricFilter = ofDefault(len, spec.getPolynomialDegree(), kernel(spec));
            rightAsymmetricFilters = switch (spec.getAsymmetricFilters()) {
                case CutAndNormalize ->
                    AsymmetricFiltersFactory.cutAndNormalizeFilters(symmetricFilter);
                case MMSRE ->
                    AsymmetricFiltersFactory.mmsreFilters(symmetricFilter,
                    spec.getAsymmetricPolynomialDegree(), spec.getRightLinearModelCoefficients(), null,
                    spec.getPassBand(), spec.getTimelinessWeight());
                default ->
                    directAsymmetricFilters(len, spec.getPolynomialDegree(), kernel(spec));
            };
            leftAsymmetricFilters = ISymmetricFiltering.mirror(switch (spec.getAsymmetricFilters()) {
                case MMSRE ->
                    AsymmetricFiltersFactory.mmsreFilters(symmetricFilter,
                    spec.getAsymmetricPolynomialDegree(), spec.getLeftLinearModelCoefficients(), null,
                    spec.getPassBand(), spec.getTimelinessWeight());
                default ->
                    rightAsymmetricFilters;
            });
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, symmetricFilter, leftAsymmetricFilters, rightAsymmetricFilters);
        }

        @Override
        public SymmetricFilter centralFilter() {
            return symmetricFilter;
        }

        @Override
        public IFiniteFilter[] leftEndPointsFilters() {
            return leftAsymmetricFilters;
        }

        @Override
        public IFiniteFilter[] rightEndPointsFilters() {
            return rightAsymmetricFilters;
        }
    }

    /**
     *
     * @param h the number of lags (-> length of the filter is 2*h+1)
     * @param d d is the order of the local polynomial
     * @param k weight of y(t+i); we suppose that k(i) = k(-i) (symmetric
     * filters; other filters could be considered)
     * @return The corresponding filter
     */
    public SymmetricFilter of(final int h, final int d, final IntToDoubleFunction k) {
        return switch (d) {
            case 0, 1 ->
                of0_1(h, k);
            case 2, 3 ->
                of2_3(h, k);
            default ->
                ofDefault(h, d, k);
        };
    }

    /**
     * The filter is defined for the lags (-h, q)
     *
     * @param h Horizon of the (full) filter
     * @param q Asymmetric horizon (<h)
     * @param d Degree of the polynomial
     * @param k Kernel
     * @return
     */
    public FiniteFilter directAsymmetricFilter(final int h, final int q, final int d, final IntToDoubleFunction k) {
        // w = KpXp (Xp'Kp Xp)^-1 e1
        // (Xp'Kp Xp)^-1 e1 = u <-> (Xp'Kp Xp) u = e1
        FastMatrix xkx = FastMatrix.square(d + 1);
        for (int i = 0; i <= d; ++i) {
            xkx.set(i, i, S_hqd(h, q, 2 * i, k));
            for (int j = 0; j < i; ++j) {
                double x = S_hqd(h, q, i + j, k);
                if (x != 0) {
                    xkx.set(i, j, x);
                    xkx.set(j, i, x);
                }
            }
        }
        double[] u = new double[d + 1];
        u[0] = 1;
        LinearSystemSolver.robustSolver().solve(xkx, DataBlock.of(u));
        double[] w = new double[h + q + 1];
        w[h] = u[0] * k.applyAsDouble(0);
        for (int i = 1; i <= q; ++i) {
            double s = u[0];
            double l = 1;
            for (int j = 1; j <= d; ++j) {
                l *= i;
                s += l * u[j];
            }
            double wc = s * k.applyAsDouble(i);
            w[h + i] = wc;
        }
        for (int i = -1; i >= -h; --i) {
            double s = u[0];
            double l = 1;
            for (int j = 1; j <= d; ++j) {
                l *= i;
                s += l * u[j];
            }
            double wc = s * k.applyAsDouble(i);
            w[h + i] = wc;
        }
        return FiniteFilter.ofInternal(w, -h);
    }

    public FiniteFilter[] directAsymmetricFilters(int h, final int d, final IntToDoubleFunction k) {
        FiniteFilter[] ff = new FiniteFilter[h];
        for (int i = 0, j = h - 1; i < h; ++i, --j) {
            ff[i] = directAsymmetricFilter(h, j, d, k);
        }
        return ff;
    }

    private static SymmetricFilter of0_1(int h, IntToDoubleFunction k) {
        double[] w = new double[h + 1];
        double s0 = S_h0(h, k);
        for (int i = 0; i <= h; ++i) {
            w[i] = k.applyAsDouble(i) / s0;
        }
        return SymmetricFilter.ofInternal(w);
    }

    private static SymmetricFilter of2_3(int h, IntToDoubleFunction k) {
        double[] w = new double[h + 1];
        double s0 = 0, s2 = 0, s4 = 0;
        for (int i = 1; i <= h; ++i) {
            double j2 = i * i;
            double j4 = j2 * j2;
            double ki = k.applyAsDouble(i);
            s0 += ki;
            s2 += j2 * ki;
            s4 += j4 * ki;
        }
        s0 = k.applyAsDouble(0) + 2 * s0;
        s2 *= 2;
        s4 *= 2;

        double sr = s2 / s4;
        for (int i = 0; i <= h; ++i) {
            double n = 1 - i * i * sr, d = s0 - s2 * sr;
            w[i] = k.applyAsDouble(i) * n / d;
        }
        return SymmetricFilter.ofInternal(w);
    }

//    @Deprecated
//    SymmetricFilter ofDefault2(int h, int d, IntToDoubleFunction k) {
//        // w = KX (X'K X)^-1 e1
//        // (X'K X)^-1 e1 = u <-> (X'K X) u = e1
//        FastMatrix xkx = FastMatrix.square(d + 1);
//        for (int i = 0; i <= d; ++i) {
//            xkx.set(i, i, S_hd(h, 2 * i, k));
//            for (int j = 0; j < i; ++j) {
//                double x = S_hd(h, i + j, k);
//                if (x != 0) {
//                    xkx.set(i, j, x);
//                    xkx.set(j, i, x);
//                }
//            }
//        }
//        double[] u = new double[d + 1];
//        u[0] = 1;
//        LinearSystemSolver.robustSolver().solve(xkx, DataBlock.of(u));
//        double[] w = new double[h + 1];
//        for (int i = 0; i <= h; ++i) {
//            double s = u[0];
//            double q = 1;
//            for (int j = 1; j <= d; ++j) {
//                q *= i;
//                s += q * u[j];
//            }
//            w[i] = s * k.applyAsDouble(i);
//        }
//        return SymmetricFilter.ofInternal(w);
//    }
    /**
     * Computes the symmetric filter of length h corresponding to a local
     * polynomial of degree d with a kernel k
     *
     * @param h The length of the filter (from -h to h)
     * @param d The degree of the local polynomial
     * @param k The kernel (a uniform kernel is used if k is null)
     * @return
     */
    public SymmetricFilter ofDefault(int h, int d, IntToDoubleFunction k) {
        double[] sk = new double[h + 1];
        if (k == null) {
            for (int i = 0; i < sk.length; ++i) {
                sk[i] = 1;
            }
        } else {
            for (int i = 0; i < sk.length; ++i) {
                double ki = k.applyAsDouble(i);
                if (ki > 0) {
                    sk[i] = Math.sqrt(ki);
                }
            }

        }
        FastMatrix Z = createZ(h, d);
        DataBlockIterator rows = Z.rowsIterator();
        int pos = -h;
        while (rows.hasNext()) {
            rows.next().mul(sk[Math.abs(pos++)]);
        }

        QRDecomposition qr = new Householder2().decompose(Z);
        double[] z = new double[Z.getRowsCount()];
        z[0] = 1;
        UpperTriangularMatrix.solvexU(qr.rawR(), DataBlock.of(z, 0, d + 1, 1));
        qr.applyQ(z);
        double[] w = new double[h + 1];
        for (int i = 0; i <= h; ++i) {
            w[i] = sk[i] * z[i + h];
        }
        return SymmetricFilter.ofInternal(w);
    }

    private double S_h0(int h, IntToDoubleFunction k) {
        double s = 0;
        for (int i = 1; i <= h; ++i) {
            s += k.applyAsDouble(i);
        }
        return 2 * s + k.applyAsDouble(0);
    }

//    private double S_h2(int h, IntToDoubleFunction k) {
//        double s = 0;
//        for (int i = 1; i <= h; ++i) {
//            double j = i * i;
//            s += j * k.applyAsDouble(i);
//        }
//        return 2 * s;
//    }
//
//    private double S_h4(int h, IntToDoubleFunction k) {
//        double s = 0;
//        for (int i = 1; i <= h; ++i) {
//            double j = i * i;
//            j *= j;
//            s += j * k.applyAsDouble(i);
//        }
//        return 2 * s;
//    }
//
//    private double S_hd(int h, int d, IntToDoubleFunction k) {
//        switch (d) {
//            case 0:
//                return S_h0(h, k);
//            case 2:
//                return S_h2(h, k);
//            case 4:
//                return S_h4(h, k);
//        }
//        if (d % 2 != 0) {
//            return 0;
//        }
//        int hd = d / 2;
//        double s = 0;
//        for (int i = 1; i <= h; ++i) {
//            double ii = i * i;
//            double j = ii;
//            for (int l = 2; l <= hd; ++l) {
//                j *= ii;
//            }
//            s += j * k.applyAsDouble(i);
//        }
//        return 2 * s;
//    }
//
    private double S_hqd(int h, int q, long d, IntToDoubleFunction k) {
        if (d == 0) {
            return S_hq0(h, q, k);
        }
        double s = 0;
        if (d % 2 == 0) {
            for (int i = 1; i <= h; ++i) {
                double j = i;
                for (int l = 1; l < d; ++l) {
                    j *= i;
                }
                if (i <= q) {
                    s += 2 * j * k.applyAsDouble(i);
                } else {
                    s += j * k.applyAsDouble(i);
                }
            }
        } else {
            for (int i = q + 1; i <= h; ++i) {
                double j = i;
                for (int l = 1; l < d; ++l) {
                    j *= i;
                }
                s -= j * k.applyAsDouble(i);
            }
        }
        return s;
    }

    private double S_hq0(int h, int q, IntToDoubleFunction k) {
        double s = k.applyAsDouble(0);
        for (int i = 1; i <= q; ++i) {
            s += 2 * k.applyAsDouble(i);
        }
        for (int i = q + 1; i <= h; ++i) {
            s += k.applyAsDouble(i);
        }
        return s;
    }

    /**
     * @param Z
     * @param d0 included
     * @param d1 included
     * @param l included (negative)
     * @param u included (positive)
     * @return
     */
    public FastMatrix z(FastMatrix Z, int l, int u, int d0, int d1) {
        int nh = Math.max(Math.abs(l), Math.abs(u));
        if (Z == null || Z.getRowsCount() / 2 < nh || Z.getColumnsCount() < d1 + 1) {
            Z = createZ(nh, d1);
        }
        return Z.extract(l + nh, u - l + 1, d0, d1 - d0 + 1);
    }

    public FastMatrix createZ(int h, int d) {
        FastMatrix M = FastMatrix.make(2 * h + 1, d + 1);
        M.column(0).set(1);
        if (d >= 1) {
            DataBlock c1 = M.column(1);
            c1.set(i -> i - h);
            for (int i = 2; i <= d; ++i) {
                M.column(i).set(c1, M.column(i - 1), (a, b) -> a * b);
            }
        }
        return M;
    }

}
