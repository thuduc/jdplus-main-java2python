/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.x13.base.core.x13.regarima;

import jdplus.x13.base.api.regarima.SingleOutlierSpec;
import jdplus.x13.base.api.regarima.OutlierSpec;
import jdplus.x13.base.api.regarima.AutoModelSpec;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.x13.base.api.regarima.TransformSpec;
import jdplus.x13.base.api.regarima.RegressionTestSpec;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.core.regarima.AICcComparator;
import jdplus.toolkit.base.core.regsarima.regular.RegressionVariablesTest;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.x13.base.core.x13.regarima.RegArimaKernel.AmiOptions;
import lombok.NonNull;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.x13.base.api.regarima.EasterSpec;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import java.util.List;
import jdplus.sa.base.core.regarima.AutomaticTradingDaysRegressionTest;
import jdplus.sa.base.core.regarima.LogLevelModule;
import jdplus.sa.base.core.regarima.AutomaticTradingDaysWaldTest;
import jdplus.sa.base.core.regarima.CalendarEffectsDetectionModule;
import jdplus.sa.base.core.regarima.EasterDetectionModule;

/**
 * The Tramo processing builder initializes the regarima processing, which
 * contains the initial model and the possible AMI modules. It starts from a
 * time series and from a Tramo specification. In a first step, we create the
 * initial model. In a second step, we define the processor itself
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
final class X13SpecDecoder {

    private final RegArimaKernel.Builder builder = RegArimaKernel.builder();

    X13SpecDecoder(@NonNull RegArimaSpec spec, ModellingContext context) {
        if (context == null) {
            context = ModellingContext.getActiveContext();
        }

        readTransformation(spec);
        if (spec.isUsingAutoModel()) {
            readAutoModel(spec);
        }
        readOutliers(spec);
        builder.modelBuilder(new X13ModelBuilder(spec, context));
        readRegression(spec, context);
        readAmiOptions(spec);
    }

    RegArimaKernel buildProcessor() {
        return builder.build();
    }

    private void readTransformation(final RegArimaSpec spec) {
        TransformSpec tspec = spec.getTransform();
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        if (tspec.getFunction() == TransformationType.Auto) {
            builder.logLevel(LogLevelModule.builder()
                    .aiccLogCorrection(tspec.getAicDiff())
                    .estimationPrecision(RegArimaKernel.AmiOptions.DEF_IEPS)
                    .preadjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriodType() : LengthOfPeriodType.None)
                    .outliersCorrection(tspec.isOutliersCorrection())
                    .build());
        }
    }

    private void readAutoModel(final RegArimaSpec spec) {
        AutoModelSpec amiSpec = spec.getAutoModel();
        DifferencingModule diff = DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(1 / amiSpec.getUb1())
                .ub2(1 / amiSpec.getUb2())
                .precision(RegArimaKernel.AmiOptions.DEF_IEPS)
                .build();
        ArmaModule arma = ArmaModule.builder()
                .balanced(amiSpec.isBalanced())
                .mixed(amiSpec.isMixed())
                .estimationPrecision(RegArimaKernel.AmiOptions.DEF_IEPS)
                .build();

        builder.autoModelling(new AutoModellingModule(diff, arma));
    }

    private void readOutliers(final RegArimaSpec spec) {
        OutlierSpec outliers = spec.getOutliers();
        if (!outliers.isUsed()) {
            return;
        }
        OutliersDetectionModule.Builder obuilder = OutliersDetectionModule.builder();
        List<SingleOutlierSpec> types = outliers.getTypes();
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i).getType()) {
                case AdditiveOutlier.CODE ->
                    obuilder.ao(true);
                case LevelShift.CODE ->
                    obuilder.ls(true);
                case TransitoryChange.CODE ->
                    obuilder.tc(true);
                case PeriodicOutlier.CODE ->
                    obuilder.so(true);
            }
        }
        builder.outliers(
                obuilder.span(outliers.getSpan())
                        .maxRound(outliers.getMaxIter())
                        .tcrate(outliers.getMonthlyTCRate())
                        .precision(RegArimaKernel.AmiOptions.DEF_IEPS)
                        .build());
    }

    private ITradingDaysVariable[] nestedtd(final RegArimaSpec spec, ModellingContext context) {
        return new ITradingDaysVariable[]{
            X13ModelBuilder.td(spec, DayClustering.TD2, context),
            X13ModelBuilder.td(spec, DayClustering.TD3, context),
            X13ModelBuilder.td(spec, DayClustering.TD4, context),
            X13ModelBuilder.td(spec, DayClustering.TD7, context)
        };
    }

    private ITradingDaysVariable[] alltd(final RegArimaSpec spec, ModellingContext context) {
        return new ITradingDaysVariable[]{
            X13ModelBuilder.td(spec, DayClustering.TD2c, context),
            X13ModelBuilder.td(spec, DayClustering.TD2, context),
            X13ModelBuilder.td(spec, DayClustering.TD3, context),
            X13ModelBuilder.td(spec, DayClustering.TD3c, context),
            X13ModelBuilder.td(spec, DayClustering.TD4, context),
            X13ModelBuilder.td(spec, DayClustering.TD7, context)
        };
    }

    private void readRegression(final RegArimaSpec spec, ModellingContext context) {
        TradingDaysSpec tdspec = spec.getRegression().getTradingDays();
        AICcComparator comparator = new AICcComparator(spec.getRegression().getAicDiff());
        if (tdspec.isAutomatic()) {
            switch (tdspec.getAutomaticMethod()) {
                case AIC:
                    builder.calendarTest(AutomaticTradingDaysRegressionTest.builder()
                            .leapYear(X13ModelBuilder.leapYear(tdspec))
                            .tradingDays(alltd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .aic()
                            .build());
                    break;
                case BIC:
                    builder.calendarTest(AutomaticTradingDaysRegressionTest.builder()
                            .leapYear(X13ModelBuilder.leapYear(tdspec))
                            .tradingDays(alltd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .bic()
                            .build());
                    break;
                case WALD:
                    builder.calendarTest(AutomaticTradingDaysWaldTest.builder()
                            .leapYear(X13ModelBuilder.leapYear(tdspec))
                            .tradingDays(nestedtd(spec, context))
                            .adjust(tdspec.isAutoAdjust())
                            .pmodel(tdspec.getAutoPvalue1())
                            .pconstraint(tdspec.getAutoPvalue2())
                            .build());
                    break;

            }
        } else if (tdspec.getRegressionTestType() != RegressionTestSpec.None) {
            CalendarEffectsDetectionModule cal = CalendarEffectsDetectionModule.builder()
                    .tradingDays(X13ModelBuilder.tradingDays(spec, context))
                    .leapYear(X13ModelBuilder.leapYear(tdspec))
                    .adjust(tdspec.isAutoAdjust() ? tdspec.getLengthOfPeriodType() : LengthOfPeriodType.None)
                    .modelComparator(comparator)
                    .build();
            builder.calendarTest(cal);
        }
        EasterSpec espec = spec.getRegression().getEaster();
        if (espec.getType() != EasterSpec.Type.Unused && espec.getTest() != RegressionTestSpec.None) {
            int[] w;
            if (espec.getTest() == RegressionTestSpec.Remove) {
                w = new int[]{espec.getDuration()};
            } else {
                w = new int[]{1, 8, 15};
            }
            IEasterVariable[] easters = new IEasterVariable[w.length];
            for (int i = 0; i < easters.length; ++i) {
                easters[i] = X13ModelBuilder.easter(espec.getType(), w[i]);
            }
            EasterDetectionModule e = EasterDetectionModule.builder()
                    .easters(easters)
                    .modelComparator(comparator)
                    .build();
            builder.easterTest(e);
        }

        RegressionVariablesTest.Builder rbuilder = RegressionVariablesTest.builder();
        if (tdspec.getRegressionTestType() != RegressionTestSpec.None) {
            rbuilder.tdTest(RegressionVariablesTest.CVAL, true);
        }
        if (espec.getType() != EasterSpec.Type.Unused && espec.getTest() != RegressionTestSpec.None) {
            rbuilder.movingHolidaysTest(RegressionVariablesTest.CVAL);
        }
        if ( spec.isUsingAutoModel() || spec.getRegression().getMean().isTest()) {
            rbuilder.meanTest(RegressionVariablesTest.CVAL);
        }
        builder.initialRegressionTest(rbuilder.build());
        if (spec.isUsingAutoModel() || spec.getRegression().getMean().isTest()) {
            rbuilder.meanTest(RegressionVariablesTest.TSIG);
        }
        builder.finalRegressionTest(rbuilder.build());

    }

    private void readAmiOptions(RegArimaSpec spec) {
        AutoModelSpec ami = spec.getAutoModel();
        builder.options(
                AmiOptions.builder()
                        .precision(spec.getEstimate().getTol())
                        .va(spec.getOutliers().getDefaultCriticalValue())
                        .reduceVa(ami.getPredcv())
                        .ljungBoxLimit(ami.getLjungBoxLimit())
                        .checkMu(spec.isUsingAutoModel()|| spec.getRegression().getMean().isTest())
                        .mixedModel(ami.isMixed())
                        .build());

    }

}
