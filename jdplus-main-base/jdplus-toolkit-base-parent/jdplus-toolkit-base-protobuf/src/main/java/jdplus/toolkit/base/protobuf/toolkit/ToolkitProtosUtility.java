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
package jdplus.toolkit.base.protobuf.toolkit;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.data.Iterables;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.stats.OneWayAnova;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.stats.tests.NiidTests;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtos;
import lombok.NonNull;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsMoniker;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ToolkitProtosUtility {

    public ToolkitProtos.Date convert(LocalDate ld) {
        if (ld.equals(LocalDate.MIN)) {
            return ToolkitProtos.Date.newBuilder()
                    .setYear(1)
                    .setMonth(1)
                    .setDay(1)
                    .build();
        } else if (ld.equals(LocalDate.MAX)) {
            return ToolkitProtos.Date.newBuilder()
                    .setYear(9999)
                    .setMonth(12)
                    .setDay(31)
                    .build();
        } else {
            return ToolkitProtos.Date.newBuilder()
                    .setYear(ld.getYear())
                    .setMonth(ld.getMonthValue())
                    .setDay(ld.getDayOfMonth())
                    .build();
        }
    }

    public LocalDate convert(ToolkitProtos.Date d) {
        switch (d.getYear()) {
            case 0:
                throw new IllegalArgumentException("Date not correctly initialized");

            case 1:
                return LocalDate.MIN;
            case 9999:
                return LocalDate.MAX;
            default:
                return LocalDate.of(d.getYear(), d.getMonth(), d.getDay());
        }
    }

    public TimeSelector convert(ToolkitProtos.TimeSelector sel) {
        switch (sel.getType()) {
            case SPAN_ALL:
                return TimeSelector.all();
            case SPAN_FROM: {
                LocalDate ld = convert(sel.getD0());
                return TimeSelector.from(ld.atStartOfDay());
            }
            case SPAN_TO: {
                LocalDate ld = convert(sel.getD1());
                return TimeSelector.to(ld.atStartOfDay());
            }
            case SPAN_BETWEEN: {
                LocalDate ld0 = convert(sel.getD0());
                LocalDate ld1 = convert(sel.getD1());
                return TimeSelector.between(ld0.atStartOfDay(), ld1.atStartOfDay());
            }
            case SPAN_FIRST: {
                int n0 = sel.getN0();
                return TimeSelector.first(n0);
            }
            case SPAN_LAST: {
                int n1 = sel.getN1();
                return TimeSelector.last(n1);
            }
            case SPAN_EXCLUDING: {
                int n0 = sel.getN0(), n1 = sel.getN1();
                return TimeSelector.excluding(n0, n1);
            }
            default:
                return TimeSelector.none();
        }
    }

    public void fill(TimeSelector sel, ToolkitProtos.TimeSelector.Builder builder) {
        switch (sel.getType()) {
            case All:
                builder.setType(ToolkitProtos.SelectionType.SPAN_ALL);
                break;
            case From:
                builder.setType(ToolkitProtos.SelectionType.SPAN_FROM)
                        .setD0(convert(sel.getD0().toLocalDate()));
                break;
            case To:
                builder.setType(ToolkitProtos.SelectionType.SPAN_TO)
                        .setD1(convert(sel.getD1().toLocalDate()));
                break;
            case Between:
                builder.setType(ToolkitProtos.SelectionType.SPAN_BETWEEN)
                        .setD0(convert(sel.getD0().toLocalDate()))
                        .setD1(convert(sel.getD1().toLocalDate()));
                break;
            case First:
                builder.setType(ToolkitProtos.SelectionType.SPAN_FIRST)
                        .setN0(sel.getN0());
                break;
            case Last:
                builder.setType(ToolkitProtos.SelectionType.SPAN_LAST)
                        .setN1(sel.getN1());
                break;
            case Excluding:
                builder.setType(ToolkitProtos.SelectionType.SPAN_EXCLUDING)
                        .setN0(sel.getN0())
                        .setN1(sel.getN1());
                break;

            default:
                builder.setType(ToolkitProtos.SelectionType.SPAN_NONE);
        }
    }

    public ToolkitProtos.TimeSelector convert(TimeSelector sel) {
        ToolkitProtos.TimeSelector.Builder builder = ToolkitProtos.TimeSelector.newBuilder();
        fill(sel, builder);
        return builder.build();
    }

    public ParameterType convert(ToolkitProtos.ParameterType t) {
        switch (t) {
            case PARAMETER_FIXED:
                return ParameterType.Fixed;
            case PARAMETER_INITIAL:
                return ParameterType.Initial;
            case PARAMETER_ESTIMATED:
                return ParameterType.Estimated;
            case PARAMETER_UNDEFINED:
                return ParameterType.Undefined;
            default:
                return null;
        }
    }

    public ToolkitProtos.ParameterType convert(@NonNull ParameterType t) {
        switch (t) {
            case Fixed:
                return ToolkitProtos.ParameterType.PARAMETER_FIXED;
            case Initial:
                return ToolkitProtos.ParameterType.PARAMETER_INITIAL;
            case Estimated:
                return ToolkitProtos.ParameterType.PARAMETER_ESTIMATED;
            default:
                return ToolkitProtos.ParameterType.PARAMETER_UNDEFINED;
        }
    }

    public Parameter convert(ToolkitProtos.Parameter p) {
        switch (p.getType()) {
            case PARAMETER_FIXED:
                return Parameter.fixed(p.getValue());
            case PARAMETER_INITIAL:
                return Parameter.initial(p.getValue());
            case PARAMETER_ESTIMATED:
                return Parameter.estimated(p.getValue());
            case PARAMETER_UNDEFINED:
                return Parameter.undefined();
            default: // UNUSED
                return null;
        }
    }

    public ToolkitProtos.Parameter convert(Parameter p) {
        if (p == null) {
            return ToolkitProtos.Parameter.getDefaultInstance();
        }
        return ToolkitProtos.Parameter.newBuilder()
                .setType(convert(p.getType()))
                .setValue(p.getValue())
                .build();
    }

    public List<ToolkitProtos.Parameter> convert(Parameter[] p) {
        if (p == null || p.length == 0) {
            return Collections.emptyList();
        }
        ArrayList<ToolkitProtos.Parameter> list = new ArrayList<>();
        for (int i = 0; i < p.length; ++i) {
            list.add(convert(p[i]));
        }
        return list;
    }

    public ToolkitProtos.ParametersEstimation convert(@NonNull ParametersEstimation p) {

        ToolkitProtos.ParametersEstimation.Builder builder = ToolkitProtos.ParametersEstimation.newBuilder()
                .addAllValue(Iterables.of(p.getValues()))
                .addAllScore(Iterables.of(p.getScores()))
                .setCovariance(convert(p.getCovariance()));
        String description = p.getDescription();
        if (description != null) {
            builder.setDescription(description);
        }
        return builder.build();
    }

    public ToolkitProtos.Parameter convert(@NonNull Parameter p, String description) {
        return ToolkitProtos.Parameter.newBuilder()
                .setType(convert(p.getType()))
                .setValue(p.getValue())
                .setDescription(description)
                .build();
    }

    public Parameter[] convert(List<ToolkitProtos.Parameter> p) {
        int n = p.size();
        if (n == 0) {
            return null;
        } else {
            Parameter[] np = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                Parameter c = convert(p.get(i));
                if (c == null)
                    np[i]=Parameter.undefined();
                else 
                    np[i]=c;
            }
            return np;
        }
    }

    public TsData convert(ToolkitProtos.TsData s) {
        int p = s.getAnnualFrequency(), y = s.getStartYear(), m = s.getStartPeriod();
        int n = s.getValuesCount();
        double[] data = new double[n];
        for (int i = 0; i < n; ++i) {
            data[i] = s.getValues(i);
        }
        return ts(p, y, m, data);
    }

    public ToolkitProtos.TsData convert(TsData s) {
        if (s == null || s.isEmpty()) {
            return ToolkitProtos.TsData.getDefaultInstance();
        }

        TsPeriod start = s.getStart();
        return ToolkitProtos.TsData.newBuilder()
                .setAnnualFrequency(s.getAnnualFrequency())
                .setStartYear(start.year())
                .setStartPeriod(start.annualPosition() + 1)
                .addAllValues(Iterables.of(s.getValues()))
                .build();
    }

    public ToolkitProtos.TsData convert(TsData s, String name) {
        if (s == null || s.isEmpty()) {
            return ToolkitProtos.TsData.getDefaultInstance()
                    .toBuilder().setName(name)
                    .build();
        }

        TsPeriod start = s.getStart();
        return ToolkitProtos.TsData.newBuilder()
                .setAnnualFrequency(s.getAnnualFrequency())
                .setStartYear(start.year())
                .setStartPeriod(start.annualPosition() + 1)
                .addAllValues(Iterables.of(s.getValues()))
                .setName(name)
                .build();
    }

   public ToolkitProtos.TsMoniker convert(TsMoniker moniker) {
        return ToolkitProtos.TsMoniker.newBuilder()
                .setSource(moniker.getSource())
                .setId(moniker.getId())
                .build();
    }

    public TsMoniker convert(ToolkitProtos.TsMoniker moniker) {
        return TsMoniker.of(moniker.getSource(), moniker.getId());
    }

    public ToolkitProtos.Ts convert(Ts s) {
        if (s == null) {
            return ToolkitProtos.Ts.getDefaultInstance();
        }

        return ToolkitProtos.Ts.newBuilder()
                .setName(s.getName())
                .setMoniker(convert(s.getMoniker()))
                .setData(convert(s.getData()))
                .putAllMetadata(s.getMeta())
                .build();
    }

    public ToolkitProtos.TsCollection convert(TsCollection s) {
        if (s == null) {
            return ToolkitProtos.TsCollection.getDefaultInstance();
        }

        return ToolkitProtos.TsCollection.newBuilder()
                .setName(s.getName())
                .setMoniker(convert(s.getMoniker()))
                .putAllMetadata(s.getMeta())
                .addAllSeries(s.getItems().stream().map(t->convert(t)).toList())
                .build();
    }

    public Ts convert(ToolkitProtos.Ts s) {
        if (s == null) {
            return null;
        }
        
        return Ts.builder()
                .name(s.getName())
                .moniker(convert(s.getMoniker()))
                .data(convert(s.getData()))
                .meta(s.getMetadataMap())
                .build();
    }

   public TsCollection convert(ToolkitProtos.TsCollection s) {
        if (s == null) {
            return null;
        }

        return TsCollection.builder()
                .name(s.getName())
                .moniker(convert(s.getMoniker()))
                .meta(s.getMetadataMap())
                .items(s.getSeriesList().stream().map(t->convert(t)).toList())
                .build();
    }

    public ToolkitProtos.Matrix convert(Matrix m) {
        if (m == null || m.isEmpty()) {
            return ToolkitProtos.Matrix.getDefaultInstance();
        }
        return ToolkitProtos.Matrix.newBuilder()
                .setNrows(m.getRowsCount())
                .setNcols(m.getColumnsCount())
                .addAllValues(Iterables.of(m.toArray()))
                .build();
    }

    public ToolkitProtos.LikelihoodStatistics convert(LikelihoodStatistics ls) {
        return ToolkitProtos.LikelihoodStatistics.newBuilder()
                .setNobs(ls.getObservationsCount())
                .setNeffectiveobs(ls.getEffectiveObservationsCount())
                .setNparams(ls.getEstimatedParametersCount())
                .setDegreesOfFreedom(ls.getEffectiveObservationsCount() - ls.getEstimatedParametersCount())
                .setLogLikelihood(ls.getLogLikelihood())
                .setAdjustedLogLikelihood(ls.getAdjustedLogLikelihood())
                .setAic(ls.getAIC())
                .setAicc(ls.getAICC())
                .setBic(ls.getBIC())
                .setBicc(ls.getBICC())
                .setBic2(ls.getBIC2())
                .setHannanQuinn(ls.getHannanQuinn())
                .setSsq(ls.getSsqErr())
                .build();

    }

    public ToolkitProtos.DiffuseLikelihoodStatistics convert(DiffuseLikelihoodStatistics ls) {
        return ToolkitProtos.DiffuseLikelihoodStatistics.newBuilder()
                .setNobs(ls.getObservationsCount())
                .setNdiffuse(ls.getDiffuseCount())
                .setNparams(ls.getEstimatedParametersCount())
                .setDegreesOfFreedom(ls.getObservationsCount() - ls.getDiffuseCount() - ls.getEstimatedParametersCount())
                .setLogLikelihood(ls.getLogLikelihood())
                .setAdjustedLogLikelihood(ls.getAdjustedLogLikelihood())
                .setAic(ls.aic())
                .setAicc(ls.aicc())
                .setBic(ls.bic())
                .setSsq(ls.getSsqErr())
                .setLdet(ls.getLogDeterminant())
                .setDcorrection(ls.getDiffuseCorrection())
                .build();
    }

    public ModellingProtos.ArimaModel convert(IArimaModel arima, String name) {
        if (arima == null) {
            return ModellingProtos.ArimaModel.getDefaultInstance();
        }
        return ModellingProtos.ArimaModel.newBuilder()
                .addAllAr(Iterables.of(arima.getStationaryAr().asPolynomial().coefficients()))
                .addAllDelta(Iterables.of(arima.getNonStationaryAr().asPolynomial().coefficients()))
                .addAllMa(Iterables.of(arima.getMa().asPolynomial().coefficients()))
                .setInnovationVariance(arima.getInnovationVariance())
                .setName(name).build();
    }

    public ToolkitProtos.StatisticalTest convert(StatisticalTest test) {
        if (test == null) {
            return ToolkitProtos.StatisticalTest.getDefaultInstance();
        } else {
            return ToolkitProtos.StatisticalTest.newBuilder()
                    .setValue(test.getValue())
                    .setPvalue(test.getPvalue())
                    .setDescription(test.getDescription())
                    .build();
        }
    }

    public ToolkitProtos.OneWayAnova convert(OneWayAnova anova) {
        return ToolkitProtos.OneWayAnova.newBuilder()
                .setSSM(anova.getSsm())
                .setDfm(anova.getDfm())
                .setSSR(anova.getSsr())
                .setDfr(anova.getDfr())
                .build();
    }

    public ToolkitProtos.NIIDTests convert(NiidTests tests) {
        if (tests == null) {
            return ToolkitProtos.NIIDTests.getDefaultInstance();
        } else {
            return ToolkitProtos.NIIDTests.newBuilder()
                    .setMean(convert(tests.meanTest()))
                    .setSkewness(convert(tests.skewness()))
                    .setKurtosis(convert(tests.kurtosis()))
                    .setDoornikHansen(convert(tests.normalityTest()))
                    .setBoxPierce(convert(tests.boxPierce()))
                    .setLjungBox(convert(tests.ljungBox()))
                    .setSeasonalBoxPierce(convert(tests.seasonalBoxPierce()))
                    .setSeasonalLjungBox(convert(tests.seasonalLjungBox()))
                    .setRunsNumber(convert(tests.runsNumber()))
                    .setRunsLength(convert(tests.runsLength()))
                    .setUpDownRunsNumber(convert(tests.upAndDownRunsNumbber()))
                    .setUpDownRunsLength(convert(tests.upAndDownRunsLength()))
                    .setBoxPierceOnSquares(convert(tests.boxPierceOnSquare()))
                    .setLjungBoxOnSquares(convert(tests.ljungBoxOnSquare()))
                    .build();
        }
    }

    private final Parameter[] EMPTY_P = new Parameter[0];

    TsData ts(int freq, int year, int start, double[] data) {
        switch (freq) {
            case 1:
                return TsData.ofInternal(TsPeriod.yearly(year), data);
            case 12:
                return TsData.ofInternal(TsPeriod.monthly(year, start), data);
            default:
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data);
        }
    }
}
