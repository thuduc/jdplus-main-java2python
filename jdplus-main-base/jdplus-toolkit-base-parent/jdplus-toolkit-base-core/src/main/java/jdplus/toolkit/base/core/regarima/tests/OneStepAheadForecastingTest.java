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
package jdplus.toolkit.base.core.regarima.tests;

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regarima.estimation.ConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.stats.tests.SampleMean;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;

/**
 *
 * @author Jean Palate
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OneStepAheadForecastingTest {

    // results
    private final DoubleSeq residuals;
    private final double meanIn, meanOut, varIn, varOut;
    private final int outSampleSize, inSampleSize;
    private final boolean mean;

    public static <M extends IArimaModel> OneStepAheadForecastingTest of(final RegArimaModel<M> regarima, final IRegArimaComputer<M> processor, final int nback) {
        try {
            RegArimaModel<M> model = linearize(regarima);
            DoubleSeq residuals = computeResiduals(model, processor, nback);
            if (residuals == null) {
                return null;
            }

            int n = residuals.length();
            if (n <= nback + 2) {
                return null;
            }
            DoubleSeq in = residuals.drop(0, nback);
            DoubleSeq out = residuals.range(in.length(), n);
            boolean mean = regarima.isMean();
            int inSampleSize = mean ? in.length() - 1 : in.length();
            double meanIn = in.sum() / in.length();
            double varIn = in.ssq() / inSampleSize;
            double meanOut = out.sum() / nback;
            double varOut = out.ssq() / nback;

            return new OneStepAheadForecastingTest(residuals, meanIn, meanOut, varIn, varOut, nback, inSampleSize, mean);
        } catch (Exception err) {
            return null;
        }
    }

    public int getOutOfSampleLength() {
        return outSampleSize;
    }

    public int getInSampleLength() {
        return residuals.length() - outSampleSize;
    }

    public StatisticalTest outOfSampleMeanTest() {
        return new SampleMean(meanOut, outSampleSize)
                .populationMean(0)
                .estimatedPopulationVariance(varIn, inSampleSize)
                .normalDistribution(true)
                .build();
    }

    public DoubleSeq getInSampleResiduals() {
        return residuals.drop(0, outSampleSize);
    }

    public double getInSampleMean() {
        return meanIn;
    }

    public double getOutOfSampleMean() {
        return meanOut;
    }

    public double getInSampleMeanSquaredError() {
        return Math.sqrt(varIn);
    }

    public double getOutOfSampleMeanSquaredError() {
        return Math.sqrt(varOut);
    }

    public DoubleSeq getOutOfSampleResiduals() {
        int n = residuals.length();
        return residuals.range(n - outSampleSize, n);
    }

    public StatisticalTest sameVarianceTest() {
        F f = new F(outSampleSize, inSampleSize);
        return TestsUtility.testOf(varOut / varIn, f, TestType.Upper);
    }

    public StatisticalTest inSampleMeanTest() {
        int n = residuals.length();
        int nsample = n - outSampleSize;
        return new SampleMean(meanIn, nsample)
                .populationMean(0)
                .estimatedPopulationVariance(varIn, inSampleSize)
                .normalDistribution(true)
                .build();
    }

    private static <M extends IArimaModel> RegArimaModel<M> linearize(RegArimaModel<M> regarima) {
        if (regarima.getVariablesCount() == 0) {
            return regarima;
        }

        ConcentratedLikelihoodWithMissing concentratedLikelihood = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
        DoubleSeq linearizedData = RegArimaUtility.linearizedData(regarima, concentratedLikelihood);

        return RegArimaModel.<M>builder()
                .y(linearizedData)
                .arima(regarima.arima())
                .meanCorrection(regarima.isMean())
                .build();
    }

    private static <M extends IArimaModel> RegArimaEstimation inSampleEstimate(RegArimaModel<M> regarima, final IRegArimaComputer<M> processor, int nback) {
        // shorten the model
        if (regarima.getObservationsCount() <= nback) {
            return null;
        }
        M arima = regarima.arima();
        RegArimaModel model = RegArimaModel.<M>builder()
                .y(regarima.getY().drop(0, nback))
                .arima(arima)
                .meanCorrection(regarima.isMean())
                .build();
        return processor.optimize(model, null);
    }

    private static <M extends IArimaModel> DoubleSeq computeResiduals(RegArimaModel<M> regarima, final IRegArimaComputer<M> processor, int nback) {
        try {
            RegArimaEstimation<M> est = inSampleEstimate(regarima, processor, nback);
            if (est == null) {
                return null;
            }
            DoubleSeq y = regarima.getY();
            if (regarima.isMean()) {
                DataBlock yc = DataBlock.of(regarima.getY());
                double[] m = RegArimaUtility.meanRegressionVariable(regarima.arima().getNonStationaryAr(), yc.length());
                yc.addAY(-est.getConcentratedLikelihood().coefficient(0), DataBlock.of(m));
                y = yc;
            }
            RegArimaModel model = RegArimaModel.<M>builder()
                    .y(y)
                    .arima(est.getModel().arima())
                    .build();
            return ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model).e();
        } catch (Exception err) {
            return null;
        }

    }
}
