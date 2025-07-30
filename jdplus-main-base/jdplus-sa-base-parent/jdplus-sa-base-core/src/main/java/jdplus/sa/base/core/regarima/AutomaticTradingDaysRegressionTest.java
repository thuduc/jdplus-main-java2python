/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.sa.base.core.regarima;

import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regsarima.regular.TradingDaysRegressionComparator;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class AutomaticTradingDaysRegressionTest implements IRegressionModule {

    public static final double DEF_TLP = 2;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(AutomaticTradingDaysRegressionTest.class)
    public static class Builder {

        /**
         * Increasing complexity
         */
        private ITradingDaysVariable td[];
        private ILengthOfPeriodVariable lp;
        private double tlp = DEF_TLP;
        private boolean aic = true;
        private double precision = 1e-3;
        private boolean adjust = true;

        public Builder tradingDays(ITradingDaysVariable[] td) {
            this.td = td.clone();
            return this;
        }

        public Builder leapYear(ILengthOfPeriodVariable lp) {
            this.lp = lp;
            return this;
        }

        public Builder lpThreshold(double tlp) {
            this.tlp = tlp;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder aic() {
            aic = true;
            return this;
        }

        public Builder bic() {
            aic = false;
            return this;
        }

        /**
         * Indicates if the lp effect can/must be handled as pre-adjustment
         * @param adjust
         * @return 
         */
        public Builder adjust(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

        public AutomaticTradingDaysRegressionTest build() {
            return new AutomaticTradingDaysRegressionTest(this);
        }
    }

    private final ITradingDaysVariable[] td;
    private final ILengthOfPeriodVariable lp;
    private final double tlp;
    private final double precision;
    private final boolean aic;
    private final boolean adjust;

    private AutomaticTradingDaysRegressionTest(Builder builder) {
        this.td = builder.td;
        this.lp = builder.lp;
        this.precision = builder.precision;
        this.aic = builder.aic;
        this.adjust = builder.adjust;
        this.tlp = builder.tlp;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {

        // first step: test all trading days
        ModelDescription current = context.getDescription();

        RegArimaEstimation<SarimaModel>[] estimations = TradingDaysRegressionComparator.test(current, td, lp, precision);
        int best = aic ? TradingDaysRegressionComparator.bestModel(estimations, TradingDaysRegressionComparator.aiccComparator())
                : TradingDaysRegressionComparator.bestModel(estimations, TradingDaysRegressionComparator.bicComparator());

        ITradingDaysVariable tdsel = best < 2 ? null : td[best - 2];
        ILengthOfPeriodVariable lpsel = best < 1 ? null : lp;
        IRegArimaComputer processor = RegArimaUtility.processor(true, precision);
        ModelDescription model = createTestModel(context, tdsel, lpsel);
        RegArimaEstimation<SarimaModel> regarima = processor.process(model.regarima(), model.mapping());
        int nhp = current.getArimaSpec().freeParametersCount();
        return update(current, model, tdsel, lpsel, regarima.getConcentratedLikelihood(), nhp);
    }

    private ModelDescription createTestModel(RegSarimaModelling context, ITradingDaysVariable td, ILengthOfPeriodVariable lp) {
        ModelDescription tmp = ModelDescription.copyOf(context.getDescription());
        tmp.setAirline(true);
        tmp.setMean(true);
        if (td != null) {
            tmp.addVariable(Variable.variable("td", td,  ModelBuilder.calendarAMI));
        }
        if (lp != null) {
            tmp.addVariable(Variable.variable("lp", lp, ModelBuilder.calendarAMI));
        }

        return tmp;
    }

    private ProcessingResult update(ModelDescription current, ModelDescription test, ITradingDaysVariable aTd, ILengthOfPeriodVariable aLp, ConcentratedLikelihoodWithMissing ll, int nhp) {
        boolean changed = false;
        boolean preadjustment=adjust && current.isLogTransformation();
        Variable var = current.variable("td");
        if (aTd != null) {
            if (var != null) {
                if (!var.getCore().equals(aTd)) {
                    current.remove("td");
                    current.addVariable(Variable.variable("td", aTd, ModelBuilder.calendarAMI));
                    changed = true;
                }
            } else {
                current.addVariable(Variable.variable("td", aTd, ModelBuilder.calendarAMI));
                changed = true;
            }

        } else if (var != null) {
            current.remove("td");
            changed = true;
        }

        var = current.variable("lp");
        if (aLp != null) {
            int pos = test.findPosition(lp);
            double tstat = ll.tstat(pos, nhp, true);
            if (Math.abs(tstat) > tlp) {
                if (var == null) {
                    if (preadjustment && tstat > 0) {
                        if (!current.isAdjusted()) {
                            current.setPreadjustment(aLp.getType());
                            return ProcessingResult.Changed;
                        }
                    } else {
                        current.addVariable(Variable.variable("lp", lp, ModelBuilder.calendarAMI));
                        return ProcessingResult.Changed;
                    }
                } else if (preadjustment && tstat > 0) {
                    current.setPreadjustment(aLp.getType());
                    current.remove("lp");
                    return ProcessingResult.Changed;
                } else {
                    return ProcessingResult.Unchanged;
                }
            }
        }
        if (var != null) {
            current.remove("lp");
            changed = true;
        } else if (current.isAdjusted() && preadjustment) {
            current.setPreadjustment(LengthOfPeriodType.None);
            changed = true;
        }
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
