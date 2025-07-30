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
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.core.regsarima.regular.TRegressionTest;
import jdplus.toolkit.base.core.regarima.FRegressionTest;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionTest;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;

/**
 * This module test for the presence of td, easter and mean in the initial model
 * (after log/level test) On entry, the model only contains pre-specified
 * regression variables On exit, it can also contain td, lp, easter and mean
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultRegressionTest implements IRegressionModule {

    public static final double CVAL = 1.96;
    public static final double T0 = 2, T1 = 2.6;
    public static final double T2 = 2.2;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(DefaultRegressionTest.class)
    public static class Builder {

        private ITradingDaysVariable td;
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private double tmean = CVAL, teaster = CVAL;
        private double twd = T2, t0td = T0, t1td = T1;
        private double fpvalue = 0.05;
        private double precision = 1e-5;
        private boolean adjust = false;

        private boolean joinTest = false;
        private boolean testMean = true;

        public Builder tradingDays(ITradingDaysVariable td) {
            this.td = td;
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
            return this;
        }

        public Builder easter(IEasterVariable easter) {
            this.easter = easter;
            return this;
        }

        public Builder meanThreshold(double tmean) {
            this.tmean = tmean;
            return this;
        }

        public Builder easterThreshold(double teaster) {
            this.teaster = teaster;
            return this;
        }

        public Builder wdThreshold(double t) {
            this.twd = t;
            return this;
        }

        public Builder tdThreshold0(double t) {
            this.t0td = t;
            return this;
        }

        public Builder tdThreshold1(double t) {
            this.t1td = t;
            return this;
        }

        public Builder fPValue(double f) {
            this.fpvalue = f;
            return this;
        }

        public Builder useJoinTest(boolean join) {
            this.joinTest = join;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public DefaultRegressionTest build() {
            return new DefaultRegressionTest(this);
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        /**
         * Indicates if the lp effect can/must be handled as pre-adjustment
         *
         * @param adjust
         * @return
         */
        public Builder adjust(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

    }

    private final ITradingDaysVariable td;
    private final ILengthOfPeriodVariable lp;
    private final IEasterVariable easter;
    private final IRegressionTest tdTest, wdTest, lpTest, mhTest, meanTest;
    private final double precision;
    private final boolean adjust;

//    private IRegressionTest tdTest_, wdTest_, lpTest_, mhTest_, meanTest_;
    private DefaultRegressionTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
        this.easter = builder.easter;
        tdTest = builder.joinTest ? new FRegressionTest(.05) : new TRegressionTest(builder.t0td, builder.t1td);
        wdTest = new TRegressionTest(builder.twd);
        lpTest = new TRegressionTest(builder.t0td);
        mhTest = new TRegressionTest(builder.teaster);
        meanTest = builder.testMean ? new TRegressionTest(builder.tmean) : null;
        precision = builder.precision;
        adjust = builder.adjust;
    }

    private ModelDescription createTestModel(RegSarimaModelling current) {
        ModelDescription model = ModelDescription.copyOf(current.getDescription());
        // add td, lp and easter
        if (td != null) {
            model.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
        }
        if (lp != null) {
            model.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
        }
        if (easter != null) {
            model.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
        }
        return model;
    }

    @Override
    public ProcessingResult test(final RegSarimaModelling context) {
        if (td == null && lp == null && easter == null && meanTest == null) {
            return ProcessingResult.Unprocessed;
        }
        // estimate the model.
        ModelDescription currentModel = context.getDescription();
        ModelDescription tmpModel = createTestModel(context);
        boolean changed = false;
        RegArimaModel<SarimaModel> regarima = tmpModel.regarima();
        IRegArimaComputer<SarimaModel> processor = RegArimaUtility.processor(true, precision);
        RegArimaEstimation<SarimaModel> rslt = processor.process(regarima, currentModel.mapping());
        ConcentratedLikelihoodWithMissing ll = rslt.getConcentratedLikelihood();

        int nhp = tmpModel.getArimaSpec().freeParametersCount();
        // td
        boolean usetd = false;
        if (td != null) {
            Variable variable = tmpModel.variable(td);
            if (variable != null && ModellingUtility.isAutomaticallyIdentified(variable)) {
                int pos = tmpModel.findPosition(variable.getCore());
                int dim = variable.getCore().dim();
                IRegressionTest test = dim == 1 ? wdTest : tdTest;
                if (test.accept(ll, nhp, pos, dim)) {
                    usetd = true;
                    currentModel.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
                    changed = true;
                }
            }
        }
        if (lp != null) {
            Variable variable = tmpModel.variable(lp);
            if (variable != null && ModellingUtility.isAutomaticallyIdentified(variable)) {
                int pos = tmpModel.findPosition(variable.getCore());
                if (usetd && lpTest.accept(ll, nhp, pos, 1)) {
                    if (adjust && tmpModel.isLogTransformation() && ll.coefficient(pos) > 0) {
                        currentModel.setPreadjustment(lp.getType());
                    } else {
                        currentModel.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
                    }
                    changed = true;
                }
            }
        }

        if (easter != null) {
            Variable variable = tmpModel.variable(easter);
            if (variable != null && ModellingUtility.isAutomaticallyIdentified(variable)) {
                int pos = tmpModel.findPosition(variable.getCore());
                if (mhTest.accept(ll, nhp, pos, 1)) {
                    currentModel.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
                    changed = true;
                }
            }
        }
        if (meanTest != null && regarima.isMean() && !meanTest.accept(ll, nhp, 0, 1)) {
            currentModel.setMean(false);
            changed = true;
        }

        if (changed) {
            context.clearEstimation();
        }

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

}
