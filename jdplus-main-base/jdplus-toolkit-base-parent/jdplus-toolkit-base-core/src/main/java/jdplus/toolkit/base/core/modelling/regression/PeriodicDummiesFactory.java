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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.PeriodicDummies;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 * The periodic contrasts are defined as follows:
 *
 * The contrasting period is by design the last period of the year. The
 * regression variables generated that way are linearly independent.
 *
 * @author Jean Palate
 */
public class PeriodicDummiesFactory implements RegressionVariableFactory<PeriodicDummies> {

    public static FastMatrix matrix(PeriodicDummies var, int length, int start) {
        int period = var.getPeriod();
        FastMatrix m = FastMatrix.make(length, period);
        int pstart = start % period;
        for (int i = 0; i < period; i++) {
            DataBlock x = m.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            x.extract(jstart, -1, period).set(1);
        }
        return m;
    }

    static PeriodicDummiesFactory FACTORY=new PeriodicDummiesFactory();

    private PeriodicDummiesFactory(){}

    @Override
    public boolean fill(PeriodicDummies var, TsPeriod start, FastMatrix buffer) {
        int period = var.getPeriod();
        TsPeriod refPeriod = start.withDate(var.getReference());
        long del = start.getId() - refPeriod.getId();
        int pstart = (int) del % period;
        for (int i = 0; i < period; i++) {
            DataBlock x = buffer.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(PeriodicDummies var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
