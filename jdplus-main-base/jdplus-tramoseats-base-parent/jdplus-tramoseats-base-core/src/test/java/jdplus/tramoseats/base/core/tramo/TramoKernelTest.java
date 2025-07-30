/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.timeseries.StaticTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.tramoseats.base.api.tramo.CalendarSpec;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec.AutoMethod;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.timeseries.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class TramoKernelTest {

    private static final double[] data, datamissing;
    public static final Calendar france, belgium;
    public static final ec.tstoolkit.timeseries.calendars.NationalCalendar ofrance, obelgium;

    static {
        data = Data.PROD.clone();
        datamissing = Data.PROD.clone();
        datamissing[2] = Double.NaN;
        datamissing[100] = Double.NaN;
        datamissing[101] = Double.NaN;
        datamissing[102] = Double.NaN;
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 14));
        holidays.add(new FixedDay(5, 8));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ARMISTICE);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);

        france = new Calendar(holidays.toArray(new Holiday[holidays.size()]), true);

        ofrance = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(13, Month.July));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(7, Month.May));
        ofrance.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.November));
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        ofrance.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        ofrance.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);

        holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 21));
        holidays.add(new FixedDay(1, 11));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);

        belgium = new Calendar(holidays.toArray(new Holiday[holidays.size()]));

        obelgium = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(20, Month.July));
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.January));
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);
    }

    public TramoKernelTest() {
    }

    @Test
    public void testProdMissing() {
        TramoKernel processor = TramoKernel.of(TramoSpec.TR5, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, datamissing);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println("JD3 with missing");
        System.out.println(rslt.getEstimation().getStatistics().getLogLikelihood());
    }

    @Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy with missing");
        System.out.println(rslt.estimation.getStatistics().logLikelihood);
    }

    @Test
    public void testProd() {
        DefaultProcessingLog log = new DefaultProcessingLog();
        TramoKernel processor = TramoKernel.of(TramoSpec.TRfull, null);
        TsPeriod start = TsPeriod.monthly(1992, 1);
        TsData s = TsData.ofInternal(start, Data.RETAIL_BOOKSTORES);
        RegSarimaModel rslt = processor.process(s, log);
        System.out.println("JD3");
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
        log.all().stream().forEach(z -> System.out.println(z));
    }

    @Test
    public void testProdLegacy() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1992, 0, Data.RETAIL_BOOKSTORES, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy");
        System.out.println(rslt.estimation.getStatistics().adjustedLogLikelihood);
    }

    @Test
    public void testInseeFull() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TRfull, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TRfull");
        System.out.println(n);
        assertTrue(n >= .9 * all.length);
    }

    @Test
    public void testXmFull() {
        TsData[] all = Data.xm();
        TramoSpec spec = TramoSpec.TRfull;
        ModellingContext context = new ModellingContext();
        context.getCalendars().set("belgium", belgium);

        RegressionSpec regSpec = spec.getRegression();
        CalendarSpec calSpec = regSpec.getCalendar();
        TradingDaysSpec tdSpec = TradingDaysSpec.automaticHolidays("belgium", LengthOfPeriodType.LeapYear, AutoMethod.FTEST, TradingDaysSpec.DEF_PFTD, false);
        spec = spec.toBuilder()
                .regression(regSpec.toBuilder()
                        .calendar(calSpec.toBuilder()
                                .tradingDays(tdSpec)
                                .build())
                        .build())
                .build();

        TramoKernel processor = TramoKernel.of(spec, context);

        ec.tstoolkit.algorithm.ProcessingContext ocontext = new ec.tstoolkit.algorithm.ProcessingContext();
        ocontext.getGregorianCalendars().set("belgium", new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium));
        ec.tstoolkit.modelling.arima.tramo.TramoSpecification ospec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        ospec.getRegression().getCalendar().getTradingDays().setHolidays("belgium");
        IPreprocessor oprocessor = ospec.build(ocontext);
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
            else{
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
            }
        }
        System.out.println(" XM:TRfull");
        System.out.println(n);
        assertTrue(n >= .9 * all.length);
    }

    @Test
    public void testInseeFullc() {
        TsData[] all = Data.insee();
        TramoSpec spec = TramoSpec.TRfull;
        ModellingContext context = new ModellingContext();
        context.getCalendars().set("france", france);

        RegressionSpec regSpec = spec.getRegression();
        CalendarSpec calSpec = regSpec.getCalendar();
        TradingDaysSpec tdSpec = TradingDaysSpec.automaticHolidays("france", LengthOfPeriodType.LeapYear, AutoMethod.FTEST, TradingDaysSpec.DEF_PFTD, false);
        spec = spec.toBuilder()
                .regression(regSpec.toBuilder()
                        .calendar(calSpec.toBuilder()
                                .tradingDays(tdSpec)
                                .build())
                        .build())
                .build();

        TramoKernel processor = TramoKernel.of(spec, context);

        ec.tstoolkit.algorithm.ProcessingContext ocontext = new ec.tstoolkit.algorithm.ProcessingContext();
        ocontext.getGregorianCalendars().set("france", new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(ofrance));
        ec.tstoolkit.modelling.arima.tramo.TramoSpecification ospec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        ospec.getRegression().getCalendar().getTradingDays().setHolidays("france");
        IPreprocessor oprocessor = ospec.build(ocontext);
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TRfullc");
        System.out.println(n);

// The old implementation was bugged. 
//        assertTrue(n > .6 * all.length);
    }

    @Test
    public void testInsee0() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR0, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR0.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR0");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee1() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR1, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR1.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR1");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee2() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR2, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR2.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR2");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee3() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR3, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR3.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR3");
        System.out.println(n);
        assertTrue(n >= .9 * all.length);
    }

    @Test
    public void testInsee4() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR4, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR4.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR4");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testInsee5() {
        TsData[] all = Data.insee();
        TramoKernel processor = TramoKernel.of(TramoSpec.TR5, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR5.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR5");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testRetail5() {
        TsData[] all = Data.retail_us();
        TramoKernel processor = TramoKernel.of(TramoSpec.TRfull, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            RegSarimaModel rslt = processor.process(all[i], null);
            TsPeriod start = all[i].getStart();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(all[i].getAnnualFrequency()), start.year(), start.annualPosition(), all[i].getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(s, null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(rslt.getStatistics().getAdjustedLogLikelihood());
//            System.out.print('\t');
//            System.out.println(orslt.estimation.getStatistics().adjustedLogLikelihood);
        }
        System.out.println("TR5-retail");
        System.out.println(n);
        assertTrue(n > .9 * all.length);
    }

    @Test
    public void testIPI() {
        TsData s = Data.SP_IPI_CN;
        TramoKernel processor = TramoKernel.of(TramoSpec.TRfull, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        RegSarimaModel rslt = processor.process(Data.SP_IPI_CN, null);
        TsPeriod start = s.getStart();
        ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(s.getAnnualFrequency()), start.year(), start.annualPosition(), s.getValues().toArray(), false);
        ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
        double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                - orslt.estimation.getStatistics().adjustedLogLikelihood;
        System.out.println(del);
    }

//    @Test
    public void testProdWald() {
        TramoSpec nspec = TramoSpec.TRfull;

        RegressionSpec regSpec = nspec.getRegression();
        CalendarSpec calSpec = regSpec.getCalendar();
        TradingDaysSpec tdSpec = TradingDaysSpec.automatic(LengthOfPeriodType.LeapYear, AutoMethod.WALD, TradingDaysSpec.DEF_PFTD, false);
        nspec = nspec.toBuilder()
                .regression(regSpec.toBuilder()
                        .calendar(calSpec.toBuilder()
                                .tradingDays(tdSpec)
                                .build())
                        .build())
                .build();

        TramoKernel processor = TramoKernel.of(nspec, null);
        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, data);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println("JD3 wald");
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
    }

//    @Test
    public void testProdWaldLegacy() {
        ec.tstoolkit.modelling.arima.tramo.TramoSpecification nspec = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.clone();
        nspec.getRegression().getCalendar().getTradingDays().setAutomaticMethod(ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec.AutoMethod.WaldTest);
        IPreprocessor processor = nspec.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        System.out.println("Legacy wald");
        System.out.println(rslt.estimation.getStatistics().adjustedLogLikelihood);
    }

    @Test
    public void testYearly() {
        TsData[] all = Data.insee();

        TramoKernel processor = TramoKernel.of(TramoSpec.TR0, null);
        IPreprocessor oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR0.build();
        int n = 0;
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            TsPeriod start = s.getStart();
            ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(s.getAnnualFrequency()), start.year(), start.annualPosition(), s.getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
        }
        assertTrue(n >= 48);
        processor = TramoKernel.of(TramoSpec.TR3, null);
        oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR3.build();
        n = 0;
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            TsPeriod start = s.getStart();
            ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(s.getAnnualFrequency()), start.year(), start.annualPosition(), s.getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
        }
        assertTrue(n >= 45);
        processor = TramoKernel.of(TramoSpec.TRfull, null);
        oprocessor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        n = 0;
        for (int i = 0; i < all.length; ++i) {
            TsData s = all[i].aggregate(TsUnit.P1Y, AggregationType.Average, true);
            TsPeriod start = s.getStart();
            ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(s.getAnnualFrequency()), start.year(), start.annualPosition(), s.getValues().toArray(), false);
            ec.tstoolkit.modelling.arima.PreprocessingModel orslt = oprocessor.process(os, null);
            RegSarimaModel rslt = processor.process(s, null);
            assertTrue(rslt != null);
            double del = rslt.getEstimation().getStatistics().getAdjustedLogLikelihood()
                    - orslt.estimation.getStatistics().adjustedLogLikelihood;
            if (Math.abs(del) < 1e-3) {
                ++n;
            }
        }
        assertTrue(n >= 45);
    }

    @Test
    public void testUser() {

        Random rnd = new Random(0);
        double[] z = new double[1200];
        for (int i = 0; i < z.length; ++i) {
            z[i] = rnd.nextDouble() - .5;
        }
        TsData trnd = TsData.ofInternal(TsPeriod.monthly(1960, 1), z);
        ModellingContext context = new ModellingContext();
        TsDataSuppliers suppliers = new TsDataSuppliers();
        suppliers.set("test", new StaticTsDataSupplier(trnd));
        context.getTsVariableManagers().set("vars", suppliers);

        TsPeriod start = TsPeriod.monthly(1967, 1);
        TsData s = TsData.ofInternal(start, data);
        
        TsContextVariable tv = new TsContextVariable("vars.test");
        Variable<TsContextVariable> var = Variable.<TsContextVariable>builder()
                .core(tv)
                .name("test")
                .coefficients(null)
                .build();
        TramoSpec nspec = TramoSpec.TRfull;
        RegressionSpec regSpec = nspec.getRegression();
        regSpec=regSpec.toBuilder()
                .userDefinedVariable(var)
                .build();

        nspec = nspec.toBuilder()
                .regression(regSpec)
                .build();

        TramoKernel processor = TramoKernel.of(nspec, context);
        RegSarimaModel rslt = processor.process(s, null);
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());
       
        var = Variable.<TsContextVariable>builder()
                .core(tv)
                .name("test")
                .coefficients(new Parameter[]{Parameter.fixed(-0.005)})
                .build();
        nspec = TramoSpec.TRfull;
        regSpec = nspec.getRegression();
        regSpec=regSpec.toBuilder()
                .userDefinedVariable(var)
                .build();

        nspec = nspec.toBuilder()
                .regression(regSpec)
                .build();

        processor = TramoKernel.of(nspec, context);
        rslt = processor.process(s, null);
        System.out.println(rslt.getEstimation().getStatistics().getAdjustedLogLikelihood());

    }

    public static void stressTestProd() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TR2.build();
            ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1982, 3, Data.ABS_RETAIL, true);
            ec.tstoolkit.modelling.arima.PreprocessingModel rslt = processor.process(s, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Legacy");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            TramoKernel processor = TramoKernel.of(TramoSpec.TR2, null);
            TsPeriod start = TsPeriod.monthly(1982,4);
            TsData s = TsData.ofInternal(start, Data.ABS_RETAIL);
            RegSarimaModel rslt = processor.process(s, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("JD3");
        System.out.println(t1 - t0);
    }

    public static void main(String[] arg) {
//        testInseeFull();
//        testInseeFullc();
        stressTestProd();
    }
}
