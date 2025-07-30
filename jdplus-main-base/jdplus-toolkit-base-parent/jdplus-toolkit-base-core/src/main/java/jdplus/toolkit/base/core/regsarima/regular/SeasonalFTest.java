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
package jdplus.toolkit.base.core.regsarima.regular;

import jdplus.toolkit.base.core.stats.linearmodel.JointTest;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicContrasts;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.GlsSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class SeasonalFTest {

    private RegArimaModel<SarimaModel> regarima;
    private RegArimaEstimation<SarimaModel> seasonalModel;
    private StatisticalTest ftest;
    private int nseas;

    public SeasonalFTest() {
    }

    public boolean test(TsData s) {
        clear();
        RegArimaModel.Builder builder = prepareSeasonalModel(s);
        addSeasonalDummies(builder, s.getDomain());
        if (!estimateModel()) {
            return false;
        }
        return computeStatistics();
    }

    public boolean test(ModelDescription m) {
        clear();
        RegArimaModel.Builder builder = prepareSeasonalModel(ModelDescription.copyOf(m));
        addSeasonalDummies(builder, m.getEstimationDomain());
        if (!estimateModel()) {
            return false;
        }
        return computeStatistics();
    }

    public RegArimaEstimation<SarimaModel> getEstimatedModel() {
        return seasonalModel;
    }

    public StatisticalTest getFTest() {
        return ftest;
    }

    private RegArimaModel.Builder prepareSeasonalModel(TsData input) {
        int period = input.getAnnualFrequency();
        SarimaOrders rspec = SarimaOrders.m011(period);
        SarimaModel arima = SarimaModel.builder(rspec).build();
        return RegArimaModel.<SarimaModel>builder()
                .y(input.getValues())
                .arima(arima)
                .meanCorrection(true);
    }

    private RegArimaModel.Builder prepareSeasonalModel(ModelDescription m) {
        SarimaOrders rspec = m.specification();
        rspec.setBd(0);
        rspec.setBp(0);
        rspec.setBq(0);
        m.setSpecification(rspec);
        return m.regarima().toBuilder()
                .meanCorrection(true);
    }

    private void addSeasonalDummies(RegArimaModel.Builder builder, TsDomain domain) {
        // makes seasonal dummies
        PeriodicContrasts dummies = new PeriodicContrasts(domain.getAnnualFrequency());
        FastMatrix x = Regression.matrix(domain, dummies);
        builder.addX(x);
        regarima = builder.build();
        nseas = dummies.dim();
    }

    private boolean estimateModel() {
        GlsSarimaComputer processor = GlsSarimaComputer.builder().build();
        seasonalModel = processor.process(regarima, null);
        return seasonalModel != null;
    }

    private boolean computeStatistics() {
        try {
            int nvars = regarima.getVariablesCount();
            int np = regarima.arima().orders().getParametersCount();
            ftest = new JointTest(seasonalModel.getConcentratedLikelihood())
                    .variableSelection(nvars - nseas, nseas)
                    .hyperParametersCount(np)
                    .build();
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private void clear() {
        regarima = null;
        seasonalModel = null;
        ftest = null;
    }

//    public boolean testAMI(TsData s) {
//        clear();
//        return searchSeasonalModel(s);
//    }
//
//    private boolean estimateContext(RegArimaModelling context) {
//        ModelDescription model = context.getDescription();
//        // force mean correction when the model is stationary
//        if (model.getArimaComponent().getDifferencingOrder() == 0) {
//            model.setMean(true);
//        }
//        context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
//        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
//        return context.estimation.compute(monitor, context.description.getArimaComponent().getFreeParametersCount());
//    }
//
//    private boolean searchSeasonalModel(TsData s) {
//        RegArimaModelling context = new RegArimaModelling();
//        ModelDescription model=new ModelDescription(s);
//        model.setAirline(false);
//        SeasonalDummies dummies = new SeasonalDummies(s.getAnnualFrequency());
//        nseas = dummies.getDim();
//        model.addVariable(new Variable(dummies, false) );
//        context.setDescription(model);
//       if (!estimateContext(context)) {
//            return false;
//        }
//        context.description.setSpecification(
//                new SarimaSpecification(context.description.getFrequency()));
//
//        DifferencingModule diff = new DifferencingModule();
//        diff.process(context);
//        context.estimation = null;
//        ArmaModule arma = new ArmaModule();
//        arma.setAcceptingWhiteNoise(true);
//        arma.process(context);
//        if (!estimateContext(context)) {
//            return false;
//        }
//        regarima = context.estimation.getRegArima();
//        seasonalModel = new RegArimaEstimation<>(regarima, context.estimation.getLikelihood());
//        return computeStatistics();
//    }
}
