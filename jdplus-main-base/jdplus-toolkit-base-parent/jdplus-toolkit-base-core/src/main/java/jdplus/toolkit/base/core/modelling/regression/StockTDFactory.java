/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.StockTradingDays;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarUtility;
import java.time.LocalDate;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 */
class StockTDFactory implements RegressionVariableFactory<StockTradingDays> {

     static StockTDFactory FACTORY=new StockTDFactory();

    private StockTDFactory(){}

    @Override
    public boolean fill(StockTradingDays var, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        int w = var.getW();
        TsPeriod cur = start;
        for (int i = 0; i < n; ++i) {
            LocalDate end = cur.end().toLocalDate();
            cur = cur.next();
            // first day after the current period
            if (w <= 0) {
                // 1 for monday, 7 for sunday
                int dw = end.getDayOfWeek().getValue();
                int g = (dw + w - 1) % 7;
                if (g < 0) {
                    g += 7;
                }
                if (g == 0) // Sunday
                {
                    buffer.row(i).set(-1);
                } else {
                    buffer.set(i, g - 1, 1);
                }
            } else {
                int month = end.getMonthValue() - 1;
                int year = end.getYear();
                if (month == 0) {
                    month = 12;
                    --year;
                }
                int day = w;
                if (day > 28) {
                    int wmax = CalendarUtility.getNumberOfDaysByMonth(year, month);
                    if (day > wmax) {
                        day = wmax;
                    }
                }
                LocalDate d = LocalDate.of(year, month, day);

                int g = d.getDayOfWeek().getValue();
                if (g == 7) // Sunday
                {
                    buffer.row(i).set(-1);
                } else {
                    buffer.set(i, g - 1, 1);
                }
            }
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(StockTradingDays var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

}
