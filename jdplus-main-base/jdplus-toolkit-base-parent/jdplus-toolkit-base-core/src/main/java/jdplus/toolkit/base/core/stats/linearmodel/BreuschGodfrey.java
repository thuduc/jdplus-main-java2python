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
package jdplus.toolkit.base.core.stats.linearmodel;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.dstats.Chi2;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class BreuschGodfrey {

    private int lag = 1;
    private final LeastSquaresResults lsr;
    private LeastSquaresResults lsr2;

    public BreuschGodfrey(LinearModel lm) {
        this(Ols.compute(lm));
    }

    public BreuschGodfrey(LeastSquaresResults lsr) {
        this.lsr = lsr;
    }

    public BreuschGodfrey lag(int lag) {
        this.lag = lag;
        return this;
    }

    public StatisticalTest build() {

        if (!lsr.isMean()) {
            throw new IllegalArgumentException("lm should contain a mean correction");
        }
        Matrix X = lsr.X();
        DoubleSeq u = lsr.residuals();

        // Be careful: X contains the mean correction
        int n = X.getRowsCount(), m = X.getColumnsCount();
        LinearModel.Builder builder = LinearModel.builder()
                .y(u.drop(lag, 0))
                .addX(X.extract(lag, n-lag, 1, m - 1))
                .meanCorrection(true);
        for (int i = 1; i <= lag; ++i) {
            builder.addX(u.drop(lag-i, i));
        }
        lsr2 = Ols.compute(builder.build());
        double val = lsr2.getR2()*(n-lag);
        Chi2 chi = new Chi2(lag);
        return TestsUtility.testOf(val, chi, TestType.Upper);
    }

    public LeastSquaresResults getLeastSquaresResults(){
        return lsr2;
    }
}
