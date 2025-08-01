/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.Range;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.tramoseats.base.api.tramo.AutoModelSpec;
import jdplus.tramoseats.base.api.tramo.CalendarSpec;
import jdplus.tramoseats.base.api.tramo.EasterSpec;
import jdplus.tramoseats.base.api.tramo.OutlierSpec;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramo.TransformSpec;
import java.util.Arrays;
import java.util.Optional;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.tramoseats.base.api.tramo.MeanSpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.toolkit.base.api.timeseries.regression.JulianEasterVariable;

/**
 *
 * @author PALATEJ
 */
//@ServiceProvider(SaProcessingFactory.class)
public class TramoFactory /*implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults>*/ {

    private static final TramoFactory INSTANCE = new TramoFactory();

    public static TramoFactory getInstance() {
        return INSTANCE;
    }

    public TramoSpec generateSpec(TramoSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {
        TramoSpec.Builder builder = spec.toBuilder();
        update(spec.getTransform(), desc, builder);
        update(spec.getArima(), desc, builder);
        update(spec.getAutoModel(), desc, builder);
        update(spec.getOutliers(), desc, builder);
        update(spec.getRegression(), desc, builder);

        return builder.build();
    }

    public TramoSpec refreshSpec(TramoSpec currentSpec, TramoSpec domainSpec, EstimationPolicyType policy, TsDomain frozenDomain) {
        if (policy == EstimationPolicyType.None) {
            return currentSpec;
        }
        if (policy == EstimationPolicyType.Complete) {
            return domainSpec;
        }
        TramoSpec.Builder builder = currentSpec.toBuilder();
        switch (policy) {
            case Outliers_StochasticComponent -> {
                resetArima(currentSpec, domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case Outliers -> {
                clearArima(currentSpec, domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case LastOutliers -> {
                clearArima(currentSpec, domainSpec, builder);
                RegressionSpec rspec = removeOutliers(currentSpec, domainSpec, builder, frozenDomain);
                freeVariables(rspec, domainSpec, builder);
            }
            case FreeParameters -> {
                freeArima(currentSpec, domainSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case FixedAutoRegressiveParameters -> {
                fixAR(currentSpec, domainSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case FixedParameters -> {
                fixArima(currentSpec, domainSpec, builder);
                freeVariables(currentSpec.getRegression(), domainSpec, builder);
            }
            case Fixed, Current -> {
                fixArima(currentSpec, domainSpec, builder);
                fixVariables(currentSpec.getRegression(), domainSpec, builder, frozenDomain);
            }
            default -> {
                return currentSpec;
            }
        }
        return builder.build();
    }

    private void update(TransformSpec transform, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        if (transform.getFunction() == TransformationType.Auto) {
            TransformSpec ntransform = transform.toBuilder()
                    .function(rslts.isLogTransformation() ? TransformationType.Log : TransformationType.None)
                    .adjust(rslts.getLengthOfPeriodTransformation())
                    .build();
            builder.transform(ntransform);
        }
    }

    private void update(SarimaSpec arima, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        SarimaSpec nspec = rslts.getStochasticComponent();
        builder.arima(nspec);
    }

    private void update(AutoModelSpec ami, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        // Disable ami
        AutoModelSpec nami = ami.toBuilder()
                .enabled(false)
                .build();
        builder.autoModel(nami);
    }

    private void update(OutlierSpec outliers, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        if (outliers.isUsed()) {    // Disable outliers
            builder.outliers(
                    outliers.toBuilder()
                            .ao(false)
                            .ls(false)
                            .tc(false)
                            .so(false)
                            .build());
        }
    }

    private void update(RegressionSpec regression, GeneralLinearModel.Description<SarimaSpec> rslts, TramoSpec.Builder builder) {
        // The huge part. 
        RegressionSpec.Builder rbuilder = regression.toBuilder();
        // all the coefficients (fixed or free) of the variables have already been filled
        Variable[] variables = rslts.getVariables();
        updateMean(variables, rbuilder);
        update(regression.getCalendar(), variables, rbuilder);
        updateOutliers(variables, rbuilder);
        updateUserVariables(variables, rbuilder);
        builder.regression(rbuilder.build());
    }

    private void updateMean(Variable[] vars, RegressionSpec.Builder builder) {
        Optional<Variable> fc = Arrays.stream(vars)
                .filter(v -> v.getName().equals(TrendConstant.NAME)).findFirst();
        if (fc.isPresent()) {
            builder.mean(MeanSpec.mean(fc.orElseThrow().getCoefficient(0)));
        } else {
            builder.mean(MeanSpec.DEFAULT_UNUSED);
        }
    }

    private void updateOutliers(Variable[] vars, RegressionSpec.Builder builder) {
        builder.clearOutliers();
        Arrays.stream(vars)
                .filter(v -> ModellingUtility.isOutlier(v))
                .forEach(v -> builder.outlier(v.removeAttribute(ModellingUtility.AMI)));
    }

    private void updateUserVariables(Variable[] vars, RegressionSpec.Builder builder) {

        builder.clearInterventionVariables();
        Arrays.stream(vars)
                .filter(v -> v.getCore() instanceof InterventionVariable)
                .forEach(v -> builder.interventionVariable(v));
        builder.clearRamps();
        Arrays.stream(vars)
                .filter(v -> v.getCore() instanceof Ramp)
                .forEach(v -> builder.ramp(v));
        builder.clearUserDefinedVariables();
        Arrays.stream(vars)
                .filter(v -> ModellingUtility.isUser(v))
                .filter(v -> !(v.getCore() instanceof InterventionVariable))
                .filter(v -> !(v.getCore() instanceof Ramp))
                .map(v -> v.withCore(TsContextVariable.of(v.getCore())))
                .forEach(v -> builder.userDefinedVariable(v));
    }

    private void update(CalendarSpec cspec, Variable[] variables, RegressionSpec.Builder builder) {
        CalendarSpec.Builder cbuilder = CalendarSpec.builder();
        update(cspec.getTradingDays(), variables, cbuilder);
        update(cspec.getEaster(), variables, cbuilder);
        builder.calendar(cbuilder.build());
    }

    private void update(TradingDaysSpec tdspec, Variable[] vars, CalendarSpec.Builder builder) {
        // leap year
        Optional<Variable> flp = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isLengthOfPeriod(v)).findFirst();
        Optional<Variable> ftd = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isTradingDays(v)).findFirst();

        TradingDaysSpec ntdspec = TradingDaysSpec.none();

        LengthOfPeriodType lp = LengthOfPeriodType.None;
        Parameter clp = null;
        if (flp.isPresent()) {
            Variable v = flp.orElseThrow();
            lp = tdspec.getLengthOfPeriodType();
            clp = v.getCoefficient(0);
        }
        TradingDaysType td = TradingDaysType.NONE;
        Parameter[] ctd = null;
        if (ftd.isPresent()) {
            Variable v = ftd.orElseThrow();
            if (tdspec.isAutomatic()) {
                ITradingDaysVariable tdv = (ITradingDaysVariable) v.getCore();
                td = tdv.getTradingDaysType();
            } else {
                td = tdspec.getTradingDaysType();
            }
            ctd = v.getCoefficients();
        }

        if (ftd.isPresent() || flp.isPresent()) {
            if (tdspec.isStockTradingDays()) {
                int ntd = tdspec.getStockTradingDays();
                ntdspec = TradingDaysSpec.stockTradingDays(ntd, ctd);
            } else if (tdspec.isHolidays()) {
                ntdspec = TradingDaysSpec.holidays(tdspec.getHolidays(),
                        td, lp, ctd, clp);
            } else if (tdspec.isUserDefined()) {
                ntdspec = TradingDaysSpec.userDefined(tdspec.getUserVariables(), ctd);
            } else { //normal case
                ntdspec = TradingDaysSpec.td(td, lp, ctd, clp);
            }
        }
        builder.tradingDays(ntdspec);
    }

    private void update(EasterSpec espec, Variable[] vars, CalendarSpec.Builder builder) {
        // Search for an optional easter variable
        Optional<Variable> fe = Arrays.stream(vars)
                .filter(v -> ModellingUtility.isEaster(v)).findFirst();
        if (fe.isPresent()) {
            Variable ev = fe.orElseThrow();
            if (ev.getCore() instanceof EasterVariable evar) {
                espec = espec.toBuilder()
                        .test(false)
                        .duration(evar.getDuration())
                        .coefficient(ev.getCoefficient(0))
                        .build();
            } else if (ev.getCore() instanceof JulianEasterVariable evar) {
                espec = espec.toBuilder()
                        .test(false)
                        .duration(evar.getDuration())
                        .coefficient(ev.getCoefficient(0))
                        .julian(true)
                        .build();
            } else {
                espec = EasterSpec.none();
            }
        } else {
            espec = EasterSpec.none();
        }
        builder.easter(espec);
    }

    private void resetArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(domainSpec.getArima());
        builder.autoModel(domainSpec.getAutoModel());
    }

    private RegressionSpec removeOutliers(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder, TsDomain frozen) {
        OutlierSpec ospec = domainSpec.getOutliers();
        if (frozen != null) {
            ospec = ospec.toBuilder().span(TimeSelector.from(frozen.getEndPeriod().start())).build();
        }
        builder.outliers(ospec);

        // remove existing automatic outliers...
        List<Variable<IOutlier>> outliers = currentSpec.getRegression().getOutliers();
        List<Variable<IOutlier>> defoutliers = domainSpec.getRegression().getOutliers();

        RegressionSpec.Builder rbuilder = currentSpec.getRegression().toBuilder()
                .clearOutliers();
        // use frozen outliers and outliers specified in the domain spec (avoid doubles)
        defoutliers.forEach(outlier -> {
            rbuilder.outlier(outlier);
        });

        outliers.stream()
                .filter(outlier -> !belongsTo(outlier, defoutliers))
                .filter(outlier -> (frozen != null && frozen.contains(outlier.getCore().getPosition())))
                .forEachOrdered(outlier -> {
                    rbuilder.outlier(outlier);
                });
        return rbuilder.build();
    }

    private static boolean belongsTo(Variable<IOutlier> outlier, List<Variable<IOutlier>> defoutliers) {
        return defoutliers.stream()
                .filter(o -> o.getCore().getCode().equals(outlier.getCore().getCode()))
                .anyMatch(o -> o.getCore().getPosition().equals(outlier.getCore().getPosition()));
    }

    private void freeArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().freeParameters(domainSpec.isUsingAutoModel() ? null : domainSpec.getArima()));
    }

    private void clearArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().resetParameters(domainSpec.isUsingAutoModel() ? null : domainSpec.getArima()));
    }

    private void fixAR(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        SarimaSpec arima = currentSpec.getArima();
        Parameter[] phi = Parameter.fixParameters(arima.getPhi());
        Parameter[] bphi = Parameter.fixParameters(arima.getBphi());
        SarimaSpec.Builder abuilder = arima.toBuilder()
                .phi(phi)
                .bphi(bphi);
        if (domainSpec.isUsingAutoModel()) {
            abuilder.theta(Parameter.freeParameters(arima.getTheta()))
                    .btheta(Parameter.freeParameters(arima.getTheta()));
        } else {
            SarimaSpec refarima = domainSpec.getArima();
            abuilder.theta(Parameter.freeParameters(arima.getTheta(), refarima.getTheta()))
                    .btheta(Parameter.freeParameters(arima.getTheta(), refarima.getBtheta()));
        }
        builder.arima(abuilder.build());
    }

    private void fixArima(TramoSpec currentSpec, TramoSpec domainSpec, TramoSpec.Builder builder) {
        builder.arima(currentSpec.getArima().fixParameters());
    }

    private void freeVariables(RegressionSpec reg, TramoSpec domainSpec, TramoSpec.Builder builder) {
        RegressionSpec dreg = domainSpec.getRegression();
        RegressionSpec.Builder rbuilder = reg.toBuilder();
        MeanSpec mean = reg.getMean();
        if (mean.hasFixedCoefficient()) {
            if (!dreg.getMean().hasFixedCoefficient()) {
                mean = MeanSpec.mean(Parameter.initial(mean.getCoefficient().getValue()));
            }
        }

        List<Variable<InterventionVariable>> iv = reg.getInterventionVariables();
        List<Variable<InterventionVariable>> niv = new ArrayList<>();
        iv.forEach(v -> {
            niv.add(v.withCoefficients(freeCoefficients(v, dreg.getInterventionVariables())));
        });

        List<Variable<IOutlier>> o = reg.getOutliers();
        List<Variable<IOutlier>> no = new ArrayList<>();
        o.forEach(v -> {
            no.add(v.withCoefficients(freeCoefficients(v, dreg.getOutliers())));
        });

        List<Variable<Ramp>> r = reg.getRamps();
        List<Variable<Ramp>> nr = new ArrayList<>();
        r.forEach(v -> {
            nr.add(v.withCoefficients(freeCoefficients(v, dreg.getRamps())));
        });

        List<Variable<TsContextVariable>> u = reg.getUserDefinedVariables();
        List<Variable<TsContextVariable>> nu = new ArrayList<>();
        u.forEach(v -> {
            nu.add(v.withCoefficients(freeCoefficients(v, dreg.getUserDefinedVariables())));
        });

        EasterSpec easter = reg.getCalendar().getEaster();
        Parameter c = easter.getCoefficient();
        if (c != null && c.isFixed()) {
            Parameter dc = dreg.getCalendar().getEaster().getCoefficient();
            if (dc == null || !dc.isFixed()) {
                c = Parameter.initial(c.getValue());
                easter = easter.toBuilder()
                        .coefficient(c)
                        .build();
            }
        }
        TradingDaysSpec td = reg.getCalendar().getTradingDays();
        c = td.getLpCoefficient();
        Parameter[] tdc = td.getTdCoefficients();
        if (c != null || tdc != null) {
            if (c != null && c.isFixed()) {
                Parameter dc = dreg.getCalendar().getTradingDays().getLpCoefficient();
                if (dc == null || !dc.isFixed()) {
                    c = Parameter.initial(c.getValue());
                }
            }
            tdc = Parameter.freeParameters(tdc, dreg.getCalendar().getTradingDays().getTdCoefficients());
            td = td.withCoefficients(tdc, c);
        }

        builder.regression(rbuilder
                .mean(mean)
                .clearInterventionVariables().interventionVariables(niv)
                .clearOutliers().outliers(no)
                .clearRamps().ramps(nr)
                .clearUserDefinedVariables().userDefinedVariables(nu)
                .calendar(CalendarSpec.builder()
                        .easter(easter)
                        .tradingDays(td)
                        .build())
                .build());
    }

    private static <S extends ITsVariable> Parameter[] freeCoefficients(Variable<S> var, List<Variable<S>> ref) {
        Parameter[] c = var.getCoefficients();
        if (c == null) {
            return null;
        }
        Optional<Variable<S>> rvar = ref.stream().filter(v -> v.getName().equals(var.getName())).findFirst();
        if (rvar.isPresent()) {
            return Parameter.freeParameters(c, rvar.orElseThrow().getCoefficients());
        } else {
            return Parameter.freeParameters(c);
        }
    }

    private static Map<String, String> ao_attributes() {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(ModellingUtility.AMI, "tramo");
        attributes.put(SaVariable.REGEFFECT, ComponentType.Irregular.name());
        return attributes;
    }

    private static final Map<String, String> IV_AO = ao_attributes();

    private void fixVariables(RegressionSpec reg, TramoSpec domainSpec, TramoSpec.Builder builder, TsDomain frozenDomain) {
        RegressionSpec.Builder rbuilder = reg.toBuilder();
        MeanSpec mean = reg.getMean();
        if (mean.isDefined()) {
            mean = MeanSpec.mean(Parameter.fixed(mean.getCoefficient().getValue()));
        }

        List<Variable<InterventionVariable>> iv = reg.getInterventionVariables();
        List<Variable<InterventionVariable>> niv = new ArrayList<>();
        iv.forEach(v -> {
            String n = v.getName();
            if (!n.startsWith(EstimationPolicyType.IV_AO)) {
                niv.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
            } else {
                niv.add(v);
            }
        });
        if (frozenDomain != null) {
            // Current AO: Add IV (ao for the frozen period)
            for (int i = 0; i < frozenDomain.getLength(); ++i) {
                TsPeriod period = frozenDomain.get(i);
                LocalDateTime day = period.start();
                InterventionVariable ao = InterventionVariable.builder()
                        .sequence(Range.of(day, day))
                        .build();
                niv.add(Variable.<InterventionVariable>builder()
                        .name(EstimationPolicyType.IV_AO + period.getStartAsShortString())
                        .attributes(IV_AO)
                        .core(ao)
                        .build());
            }
        }

        List<Variable<IOutlier>> o = reg.getOutliers();
        List<Variable<IOutlier>> no = new ArrayList<>();
        o.forEach(v -> {
            no.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        List<Variable<Ramp>> r = reg.getRamps();
        List<Variable<Ramp>> nr = new ArrayList<>();
        r.forEach(v -> {
            nr.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        List<Variable<TsContextVariable>> u = reg.getUserDefinedVariables();
        List<Variable<TsContextVariable>> nu = new ArrayList<>();
        u.forEach(v -> {
            nu.add(v.withCoefficients(Parameter.fixParameters(v.getCoefficients())));
        });

        EasterSpec easter = reg.getCalendar().getEaster();
        Parameter c = easter.getCoefficient();
        if (c != null) {
            easter = easter.toBuilder()
                    .coefficient(Parameter.fixed(c.getValue()))
                    .build();
        }
        TradingDaysSpec td = reg.getCalendar().getTradingDays();
        c = td.getLpCoefficient();
        Parameter[] tdc = td.getTdCoefficients();
        if (c != null || tdc != null) {
            td = td.withCoefficients(Parameter.fixParameters(tdc), c == null ? null : Parameter.fixed(c.getValue()));
        }

        builder.regression(rbuilder
                .mean(mean)
                .clearInterventionVariables().interventionVariables(niv)
                .clearOutliers().outliers(no)
                .clearRamps().ramps(nr)
                .clearUserDefinedVariables().userDefinedVariables(nu)
                .calendar(CalendarSpec.builder()
                        .easter(easter)
                        .tradingDays(td)
                        .build())
                .build());

    }

}
