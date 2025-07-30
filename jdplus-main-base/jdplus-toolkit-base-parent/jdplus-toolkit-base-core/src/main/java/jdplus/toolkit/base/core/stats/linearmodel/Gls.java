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
package jdplus.toolkit.base.core.stats.linearmodel;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.eco.EcoException;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Gls {

    public Gls() {
    }

    public LeastSquaresResults compute(LinearModel model, FastMatrix cov) {

        FastMatrix L = cov.deepClone();
        try {
            SymmetricMatrix.lcholesky(L);
        // yl = L^-1*y <-> L*yl = y
        DataBlock yl = DataBlock.of(model.getY());
        LowerTriangularMatrix.solveLx(L, yl);

        FastMatrix xl = model.variables();
        LowerTriangularMatrix.solveLX(L, xl);

        QRLeastSquaresSolution solution = QRLeastSquaresSolver.robustLeastSquares(yl, xl);
         FastMatrix bvar = solution.unscaledCovariance();
        return LeastSquaresResults.builder(yl, xl)
                .mean(model.isMeanCorrection())
                .estimation(solution.getB(), bvar)
                .ssq(solution.getSsqErr())
                .residuals(solution.getE())
                .logDeterminant(2 * LogSign.of(L.diagonal()).getValue())
                .build();
        } catch (Exception err) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
 }

}
