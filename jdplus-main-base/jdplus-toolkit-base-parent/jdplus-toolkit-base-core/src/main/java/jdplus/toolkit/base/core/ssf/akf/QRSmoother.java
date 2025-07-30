/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class QRSmoother {

    public SmoothingOutput process(final ISsf ssf, final ISsfData data, boolean var, boolean rescale) {

        DefaultAugmentedFilteringResults fr = new DefaultAugmentedFilteringResults(true);
        fr.prepare(ssf, 0, data.length());
        AugmentedFilter filter = new AugmentedFilter();
        if (!filter.process(ssf, data, fr)) {
            return null;
        }
        // compute delta, psi
        int len = data.length();
        int n = data.getObsCount();
        int nd = ssf.getDiffuseDim();
        FastMatrix Xl = FastMatrix.make(n, nd);
        DataBlock yl = DataBlock.make(n);

        // fill the matrices
        for (int i = 0, j = 0; i < len; ++i) {
            if (!data.isMissing(i)) {
                double e = Math.sqrt(fr.errorVariance(i));
                yl.set(j, fr.error(i) / e);
                Xl.row(j).setAY(-1 / e, fr.E(i));
                ++j;
            }
        }
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.robustLeastSquares(yl, Xl);
        FastMatrix psi = ls.RtR();
        SymmetricMatrix.lcholesky(psi);
        DoubleSeq delta = ls.getB();
        double sig2 = ls.getSsqErr() / (n - nd);

        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(var);
        DefaultSmoothingResults rslts = var ? DefaultSmoothingResults.full() : DefaultSmoothingResults.light();
        rslts.prepare(ssf.getStateDim(), 0, len);
        smoother.process(ssf, len, fr, psi, delta, rslts);
        if (rescale) {
            rslts.rescaleVariances(sig2);
        }
        return new SmoothingOutput(fr, sig2, rslts);
    }
}
