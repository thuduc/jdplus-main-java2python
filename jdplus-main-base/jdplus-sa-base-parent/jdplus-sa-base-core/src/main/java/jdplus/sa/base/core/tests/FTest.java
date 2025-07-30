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
package jdplus.sa.base.core.tests;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicContrasts;
import jdplus.toolkit.base.core.stats.linearmodel.JointTest;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.PeriodicContrastsFactory;

/**
 *
 * @author PALATEJ
 */
@BuilderPattern(StatisticalTest.class)
public class FTest {

    private SarimaOrders.Prespecified model = SarimaOrders.Prespecified.WN;
    private final DoubleSeq s;
    private final int period;
    private int ncycles;

    public FTest(DoubleSeq s, int period) {
        this.s = s;
        this.period = period;
    }

    public FTest model(SarimaOrders.Prespecified model) {
        this.model = model;
        return this;
    }

    public FTest ncycles(int n) {
        this.ncycles = n;
        return this;
    }

    public StatisticalTest build() {
        if (period <= 1) {
            throw new IllegalArgumentException("Invalid periodicity");
        }
        switch (model) {
            case AR:
                return processAr();
            case D1:
                return processDiff();
            case WN:
               return process();
             default:
         throw new UnsupportedOperationException("Not supported yet."); 
        }
    }

    private StatisticalTest process() {
        DoubleSeq y = ncycles == 0 ? s : s.drop(Math.max(0, s.length() - ncycles * period), 0);
        try {
            PeriodicContrasts dummies = new PeriodicContrasts(period);
            FastMatrix matrix = PeriodicContrastsFactory.matrix(dummies, y.length(), 0);
            double ybar = y.average();
            LinearModel reg = LinearModel.builder()
                    .y(y.fn(q -> q - ybar))
                    .addX(matrix)
                    .build();
            int nseas = dummies.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(0, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

    private StatisticalTest processAr() {
        DoubleSeq y = ncycles == 0 ? s : s.drop(Math.max(0, s.length() - ncycles * period - 1), 0);
        try {
            PeriodicContrasts dummies = new PeriodicContrasts(period);
            FastMatrix matrix = PeriodicContrastsFactory.matrix(dummies, y.length()-1, 0);
            LinearModel reg = LinearModel.builder()
                    .y(y.drop(1, 0))
                    .meanCorrection(true)
                    .addX(y.drop(0, 1))
                    .addX(matrix)
                    .build();
            int nseas = dummies.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(2, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

    private StatisticalTest processDiff() {
        DoubleSeq y = ncycles == 0 ? s : s.drop(Math.max(0, s.length() - ncycles * period - 1), 0);
        DoubleSeq dy = y.delta(1);
        try {
            PeriodicContrasts dummies = new PeriodicContrasts(period);
            FastMatrix matrix = PeriodicContrastsFactory.matrix(dummies, dy.length(), 0);
            double dybar = dy.average();
            LinearModel reg = LinearModel.builder()
                    .y(dy.fn(q -> q - dybar))
                    .addX(matrix)
                    .build();
            int nseas = dummies.dim();
            LeastSquaresResults lsr = Ols.compute(reg);
            return new JointTest(lsr.getLikelihood())
                    .variableSelection(0, nseas)
                    .blue()
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

}
