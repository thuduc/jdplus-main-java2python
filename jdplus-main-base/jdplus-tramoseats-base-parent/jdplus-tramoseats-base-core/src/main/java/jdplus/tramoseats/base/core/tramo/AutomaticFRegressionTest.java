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

import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;

/**
 *
 * @author gianluca
 */
public class AutomaticFRegressionTest implements IRegressionModule {

    public static final double DEF_TMEAN = 1.96, DEF_TLP = 2, DEF_TEASTER = 2.2, DEF_FPVAL = 0.01;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticFRegressionTest.class)
    public static class Builder {

        private ITradingDaysVariable td, wd;
        private ILengthOfPeriodVariable lp;
        private IEasterVariable easter;
        private double tmean = DEF_TMEAN, tlp = DEF_TLP, teaster = DEF_TEASTER;
        private double fpvalue = DEF_FPVAL;
        private boolean testMean = true;
        private double precision = 1e-5;
        private boolean adjust = false;

        public Builder tradingDays(ITradingDaysVariable td) {
            this.td = td;
            return this;
        }

        public Builder workingDays(ITradingDaysVariable wd) {
            this.wd = wd;
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
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

        public Builder lpThreshold(double tlp) {
            this.tlp = tlp;
            return this;
        }

        public Builder fPValue(double f) {
            this.fpvalue = f;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public AutomaticFRegressionTest build() {
            return new AutomaticFRegressionTest(this);
        }
    }

    private final ITradingDaysVariable td, wd;
    private final ILengthOfPeriodVariable lp;
    private final boolean adjust;
    private final IEasterVariable easter;
    private final double tmean, teaster, tlp;
    private final double fpvalue;
    private final boolean testMean;
    private final double precision;

    private AutomaticFRegressionTest(Builder builder) {
        this.td = builder.td;
        this.wd = builder.wd;
        this.lp = builder.lp;
        this.easter = builder.easter;
        this.fpvalue = builder.fpvalue;
        this.tmean = builder.tmean;
        this.teaster = builder.teaster;
        this.tlp = builder.tlp;
        this.testMean = builder.testMean;
        this.precision = builder.precision;
        this.adjust = builder.adjust;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {
        try {
            ModelDescription current = context.getDescription();
//      First case TD=0 or Just test EE
            ModelDescription test0 = createTestModel(context, null, null);
            IRegArimaComputer processor = RegArimaUtility.processor(true, precision);
            RegArimaEstimation regarima0 = processor.process(test0.regarima(), test0.mapping());
            ConcentratedLikelihoodWithMissing ll0 = regarima0.getConcentratedLikelihood();
            int nhp = test0.getArimaSpec().freeParametersCount();
            double SS0 = ll0.ssq();

            if (td == null) {
                return update(current, test0, null, ll0, nhp);
            }

            //      Second case TD=TradindDay only
            ModelDescription test6 = createTestModel(context, td, null);
            RegArimaEstimation regarima6 = processor.process(test6.regarima(), test6.mapping());
            ConcentratedLikelihoodWithMissing ll6 = regarima6.getConcentratedLikelihood();
            double SS6 = ll6.ssq(), SSmc6 = SS6 / (ll6.degreesOfFreedom() - nhp);
            double Ftd = (SS0 - SS6) / (SSmc6 * 6);
            double pFtd6 = 0.0;
            if (Ftd >= 0) {
                F f0 = new F(6, ll6.degreesOfFreedom() - nhp);
                pFtd6 = f0.getProbability(Ftd, ProbabilityType.Lower);
            }

//      Third case TD=WorkingDay only
            ModelDescription test1 = createTestModel(context, wd, null);
            RegArimaEstimation regarima1 = processor.process(test1.regarima(), test1.mapping());
            ConcentratedLikelihoodWithMissing ll1 = regarima1.getConcentratedLikelihood();
            double SS1 = ll1.ssq(), SSmc1 = SS1 / (ll1.degreesOfFreedom() - nhp);
            Ftd = (SS0 - SS1) / SSmc1;
            double pFtd1 = 0.0;
            if (Ftd >= 0) {
                F f1 = new F(1, ll1.degreesOfFreedom() - nhp);
                pFtd1 = f1.getProbability(Ftd, ProbabilityType.Lower);
            }

// Check over the 3 cases        
            if ((pFtd6 > pFtd1) && (pFtd6 > 1 - fpvalue)) {
                // add leap year
                ModelDescription all = createTestModel(context, td, lp);
                RegArimaEstimation regarima = processor.process(all.regarima(), all.mapping());
                return update(current, all, td, regarima.getConcentratedLikelihood(), nhp);
            } else if (pFtd1 < 1 - fpvalue) {
                return update(current, test0, null, ll0, nhp);
            } else {
                // add leap year
                ModelDescription all = createTestModel(context, wd, lp);
                RegArimaEstimation regarima = processor.process(all.regarima(), all.mapping());
                return update(current, all, wd, regarima.getConcentratedLikelihood(), nhp);
            }
        } catch (Exception err) {
            return ProcessingResult.Failed;
        }
    }

    private ModelDescription createTestModel(RegSarimaModelling context, ITradingDaysVariable td, ILengthOfPeriodVariable lp) {
        ModelDescription tmp = ModelDescription.copyOf(context.getDescription());
        if (td != null) {
            tmp.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
            if (lp != null) {
                tmp.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
            }
        }
        if (easter != null) {
            tmp.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
        }
        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ConcentratedLikelihoodWithMissing ll, int nhp) {
        boolean changed = false;
        boolean preadjustment = adjust && current.isLogTransformation();
        if (aTd != null) {
            current.addVariable(Variable.variable("td", aTd, TramoModelBuilder.calendarAMI));
        }
        if (testMean) {
            boolean mean = Math.abs(ll.tstat(0, nhp, true)) > tmean;
            if (mean != current.isMean()) {
                current.setMean(mean);
                changed = true;
            }
        }
        if (aTd != null && lp != null) {
            int pos = test.findPosition(lp);
            double tstat = pos >= 0 ? ll.tstat(pos, nhp, true) : 0;
            if (Math.abs(tstat) > tlp) {
                if (preadjustment && tstat > 0) {
                    current.setPreadjustment(lp.getType());
                } else {
                    current.addVariable(Variable.variable("lp", lp, TramoModelBuilder.calendarAMI));
                }
                changed = true;
            }
        }
        if (easter != null) {
            int pos = test.findPosition(easter);
            if (pos >= 0 && Math.abs(ll.tstat(pos, nhp, true)) > teaster) {
                current.addVariable(Variable.variable("easter", easter, TramoModelBuilder.calendarAMI));
                changed = true;
            }
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
