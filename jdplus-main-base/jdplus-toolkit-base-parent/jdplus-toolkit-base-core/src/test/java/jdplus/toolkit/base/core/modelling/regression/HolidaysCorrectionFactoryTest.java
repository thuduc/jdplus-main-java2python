/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import ec.tstoolkit.timeseries.Month;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class HolidaysCorrectionFactoryTest {
    
    public static final Calendar belgium;
    public static final ec.tstoolkit.timeseries.calendars.NationalCalendar obelgium;
    
    static {
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 21));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ARMISTICE);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);
        
        belgium = new Calendar(holidays.toArray(Holiday[]::new), true);
        
        obelgium = new ec.tstoolkit.timeseries.calendars.NationalCalendar();
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(20, Month.July));
        obelgium.add(new ec.tstoolkit.timeseries.calendars.FixedDay(10, Month.November));
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.AllSaintsDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Assumption);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.Christmas);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.MayDay);
        obelgium.add(ec.tstoolkit.timeseries.calendars.FixedDay.NewYear);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.Ascension);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.EasterMonday);
        obelgium.add(ec.tstoolkit.timeseries.calendars.EasterRelatedDay.PentecostMonday);
    }
    
    public HolidaysCorrectionFactoryTest() {
    }
    
    @Test    
    public void testRaw() {
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
        HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                .clustering(DayClustering.TD7)
                .corrector(corrector)
                .contrast(false)
                .build();
        FastMatrix td = Regression.matrix(TsDomain.of(TsPeriod.monthly(1980, 1), 60), var);
 //       System.out.println(td);
    }
    
    @Test
    public void testTD7_12() {
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
        HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                .clustering(DayClustering.TD7)
                .corrector(corrector)
                .build();
        FastMatrix td6 = Regression.matrix(TsDomain.of(TsPeriod.monthly(1980, 1), 60), var);
        ec.tstoolkit.timeseries.calendars.NationalCalendarProvider provider
                = new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium);
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovar
                = new ec.tstoolkit.timeseries.regression.GregorianCalendarVariables(provider, ec.tstoolkit.timeseries.calendars.TradingDaysType.TradingDays);
        ec.tstoolkit.maths.matrices.Matrix m = new ec.tstoolkit.maths.matrices.Matrix(60, 6);
        ovar.data(new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 60), m.columnList());
        double[] data = td6.getStorage();
        double[] odata = m.internalStorage();
        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], odata[i], 1e-12);
        }
    }
    
    @Test
    public void testTD2_12() {
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
        HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                .clustering(DayClustering.TD2)
                .corrector(corrector)
                .build();
        FastMatrix td2 = Regression.matrix(TsDomain.of(TsPeriod.monthly(1980, 1), 360), var);
        ec.tstoolkit.timeseries.calendars.NationalCalendarProvider provider
                = new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium);
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovar
                = new ec.tstoolkit.timeseries.regression.GregorianCalendarVariables(provider, ec.tstoolkit.timeseries.calendars.TradingDaysType.WorkingDays);
        ec.tstoolkit.maths.matrices.Matrix m = new ec.tstoolkit.maths.matrices.Matrix(360, 1);
        ovar.data(new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360), m.columnList());
        
        double[] data = td2.getStorage();
        double[] odata = m.internalStorage();
        
        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], odata[i], 1e-12);
        }
    }
    
    @Test
    public void testTD7_4() {
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
        HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                .clustering(DayClustering.TD7)
                .corrector(corrector)
                .build();
        FastMatrix td6 = Regression.matrix(TsDomain.of(TsPeriod.quarterly(1980, 1), 360), var);
        ec.tstoolkit.timeseries.calendars.NationalCalendarProvider provider
                = new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium);
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovar
                = new ec.tstoolkit.timeseries.regression.GregorianCalendarVariables(provider, ec.tstoolkit.timeseries.calendars.TradingDaysType.TradingDays);
        ec.tstoolkit.maths.matrices.Matrix m = new ec.tstoolkit.maths.matrices.Matrix(360, 6);
        ovar.data(new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 1980, 0, 360), m.columnList());
        double[] data = td6.getStorage();
        double[] odata = m.internalStorage();
        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], odata[i], 1e-12);
        }
    }
    
    @Test
    public void testTD2_4() {
        HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
        HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                .clustering(DayClustering.TD2)
                .corrector(corrector)
                .build();
        FastMatrix td2 = Regression.matrix(TsDomain.of(TsPeriod.quarterly(1980, 1), 360), var);
        ec.tstoolkit.timeseries.calendars.NationalCalendarProvider provider
                = new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium);
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovar
                = new ec.tstoolkit.timeseries.regression.GregorianCalendarVariables(provider, ec.tstoolkit.timeseries.calendars.TradingDaysType.WorkingDays);
        ec.tstoolkit.maths.matrices.Matrix m = new ec.tstoolkit.maths.matrices.Matrix(360, 1);
        ovar.data(new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 1980, 0, 360), m.columnList());
        
        double[] data = td2.getStorage();
        double[] odata = m.internalStorage();
        
        for (int i = 0; i < data.length; ++i) {
            assertEquals(data[i], odata[i], 1e-12);
        }
    }
    
    public static void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(belgium, DayOfWeek.SUNDAY);
            HolidaysCorrectedTradingDays var = HolidaysCorrectedTradingDays.builder()
                    .clustering(DayClustering.TD2)
                    .corrector(corrector)
                    .build();
            FastMatrix td2 = Regression.matrix(TsDomain.of(TsPeriod.monthly(1980, 1), 360), var);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New: " + (t1 - t0));
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            ec.tstoolkit.timeseries.calendars.NationalCalendarProvider provider
                    = new ec.tstoolkit.timeseries.calendars.NationalCalendarProvider(obelgium);
            ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovar
                    = new ec.tstoolkit.timeseries.regression.GregorianCalendarVariables(provider, ec.tstoolkit.timeseries.calendars.TradingDaysType.WorkingDays);
            ec.tstoolkit.maths.matrices.Matrix m = new ec.tstoolkit.maths.matrices.Matrix(360, 1);
            ovar.data(new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360), m.columnList());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old: " + (t1 - t0));
    }
    
    public static void main(String[] args) {
        stressTest();
    }
    
}
