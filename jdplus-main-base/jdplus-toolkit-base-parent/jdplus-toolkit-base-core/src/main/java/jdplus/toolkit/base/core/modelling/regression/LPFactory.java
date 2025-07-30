/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.LengthOfPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarUtility;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 */
class LPFactory implements RegressionVariableFactory<LengthOfPeriod> {

    static LPFactory FACTORY=new LPFactory();

    private LPFactory(){}
    
    @Override
    public boolean fill(LengthOfPeriod var, TsPeriod start, FastMatrix buffer) {
        switch (var.getType()) {
            case LeapYear:
                lp(TsDomain.of(start, buffer.getRowsCount()), buffer.column(0));
                return true;
            case LengthOfPeriod:
                length(TsDomain.of(start, buffer.getRowsCount()), buffer.column(0));
                return true;
            default:
                return false;
        }
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(LengthOfPeriod var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private void lp(TsDomain domain, DataBlock buffer) {
        int freq = domain.getAnnualFrequency();
//        if (freq < 2) {
//            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
//        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int n = domain.getLength();
        int period = 0;
        if (freq == 12) {
            period = 1;
        }
        // position of the starting period in the year
        int pos = (start.start().getMonthValue() - 1) % freq;
        int idx = period - pos;
        if (idx < 0) {
            idx += freq;
        }
        // position of the first period containing 29/2
        int lppos = idx;
        int year = domain.get(idx).year();
        while (!CalendarUtility.isLeap(year)) {
            lppos += freq;
            ++year;
        }

        buffer.extract(idx, -1, freq).set(-.25);
        buffer.extract(lppos, -1, 4 * freq).set(.75);
    }

    private void length(TsDomain domain, DataBlock buffer) {
        int freq = domain.getAnnualFrequency();
//        if (freq < 2) {
//            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
//        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int[] ndays = CalendarUtility.daysCount(domain);
        final double m = 365.25 / freq;
        buffer.set(i -> ndays[i] - m);
    }
}
