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
package jdplus.toolkit.base.core.math.linearsystem;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.normalizer.SafeNormalizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.Gauss;
import jdplus.toolkit.base.core.math.matrices.decomposition.LUDecomposition;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.Constants;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class LULinearSystemSolver implements LinearSystemSolver {

    @BuilderPattern(LULinearSystemSolver.class)
    public static class Builder {

        private LUDecomposition.Decomposer decomposer = (M, e) -> Gauss.decompose(M, e);
        private double eps = Constants.getEpsilon();
        private boolean normalize = false;

        private Builder() {
        }

        public Builder decomposer(LUDecomposition.Decomposer decomposer) {
            this.decomposer = decomposer;
            return this;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public LULinearSystemSolver build() {
            return new LULinearSystemSolver(decomposer, eps, normalize);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final LUDecomposition.Decomposer decomposer;
    private final double eps;
    private final boolean normalize;

    private LULinearSystemSolver(LUDecomposition.Decomposer decomposer, double eps, boolean normalize) {
        this.decomposer = decomposer;
        this.eps = eps;
        this.normalize = normalize;
    }

    @Override
    public void solve(FastMatrix A, DataBlock b) {
        // we normalize b
        FastMatrix An;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn = new SafeNormalizer();
            int i = 0;
            while (rows.hasNext()) {
                double factor = sn.normalize(rows.next());
                b.mul(i++, factor);
            }
        } else {
            An = A;
        }
        LUDecomposition lu = decomposer.decompose(An, eps);
        lu.solve(b);
    }

    @Override
    public void solve(FastMatrix A, FastMatrix B) {
        FastMatrix An;
        double[] factor = null;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn = new SafeNormalizer();
            factor = new double[A.getRowsCount()];
            int i = 0;
            while (rows.hasNext()) {
                factor[i++] = sn.normalize(rows.next());
            }
        } else {
            An = A;
        }
        LUDecomposition lu = decomposer.decompose(An, eps);
        lu.solve(B);
        if (factor != null) {
            DataBlockIterator rows = B.rowsIterator();
            int r = 0;
            while (rows.hasNext()) {
                rows.next().div(factor[r++]);
            }
        }
    }
}
