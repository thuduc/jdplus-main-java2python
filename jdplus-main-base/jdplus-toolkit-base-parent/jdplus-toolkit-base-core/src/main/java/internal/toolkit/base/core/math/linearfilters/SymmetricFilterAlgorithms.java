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
package internal.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFilterAlgorithms {

    public static SymmetricFilter.Decomposer decomposer(LinearSystemSolver solver) {
        return (SymmetricFilter filter, final BackFilter Q) -> {
            if (Q.length() == 1) {
                double[] data = filter.coefficientsAsPolynomial().toArray();
                data[0] /= 2;
                double q0 = Math.abs(Q.get(0));
                if (q0 != 1) {
                    for (int i = 0; i < data.length; ++i) {
                        data[i] /= q0;
                    }
                }
                return BackFilter.ofInternal(data);
            }

            Polynomial q = Q.asPolynomial();
            Polynomial c = filter.coefficientsAsPolynomial();

            int nq = q.degree();
            int nc = c.degree();
            int r = nq > nc ? nq : nc;

            FastMatrix a = FastMatrix.square(r + 1);
            double[] mc = new double[r + 1];
            for (int i = 0; i <= r; ++i) {
                mc[r - i] = i <= nc ? c.get(i) : 0;
                for (int j = 0; j <= i; ++j) {
                    if (i - j <= nq) {
                        double a1 = q.get(i - j);
                        a.add(i, j, a1);
                    }
                    if (r - i + j <= nq) {
                        double a2 = q.get(r - i + j);
                        a.add(i, r - j, a2);
                    }
                }
            }
            DataBlock g = DataBlock.of(mc);
            if (solver == null) {
                LinearSystemSolver.robustSolver().solve(a, g);
            } else {
                solver.solve(a, g);
            }
            return BackFilter.ofInternal(g.reverse().toArray());
        };
    }

    public static SymmetricFilter.Factorizer fastFactorizer() {
        return (SymmetricFilter filter) -> {
            SymmetricFrequencyResponseDecomposer decomposer = new SymmetricFrequencyResponseDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    public static SymmetricFilter.Factorizer robustFactorizer() {
        return (SymmetricFilter filter) -> {
            RobustSymmetricFrequencyResponseDecomposer decomposer = new RobustSymmetricFrequencyResponseDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    public static SymmetricFilter.Factorizer evFactorizer() {
        return (SymmetricFilter filter) -> {
            EigenValuesDecomposer decomposer = new EigenValuesDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    public static SymmetricFilter.Factorizer fastEvFactorizer() {
        return (SymmetricFilter filter) -> {
            FastEigenValuesDecomposer decomposer = new FastEigenValuesDecomposer();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    public static SymmetricFilter.Factorizer evFactorizer2() {
        return (SymmetricFilter filter) -> {
            EigenValuesDecomposer2 decomposer = new EigenValuesDecomposer2();
            if (!decomposer.decompose(filter)) {
                return null;
            } else {
                return new SymmetricFilter.Factorization(decomposer.getBFilter(), decomposer.getFactor());
            }
        };
    }

    private static final int ROBUST_LIMIT = 5;

    public static SymmetricFilter.Factorizer factorizer() {
        return filter -> {
            SymmetricFilter.Factorization fac = evFactorizer2().factorize(filter);
            if (fac == null) {
                fac = evFactorizer().factorize(filter);
                if (fac == null) {
                    fac = fastFactorizer().factorize(filter);
                    if (fac == null) {
                        fac = robustFactorizer().factorize(filter);
                    }
                }
            }
            return fac;
        };
    }
}
