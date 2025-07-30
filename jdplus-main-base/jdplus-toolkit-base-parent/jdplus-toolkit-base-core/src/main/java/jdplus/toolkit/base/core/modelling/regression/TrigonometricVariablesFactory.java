/*
 * Copyright 2016 National Bank of Belgium
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

import jdplus.toolkit.base.api.timeseries.regression.TrigonometricVariables;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 * Computes trigonometric variables: sin(wt), cos(wt) at given frequencies if w
 * = pi, sin(wt) is omitted t = 0 for 1/1/70
 *
 * @author Jean Palate
 */
public class TrigonometricVariablesFactory implements RegressionVariableFactory<TrigonometricVariables> {

    public static FastMatrix matrix(TrigonometricVariables var, int length, int start) {
        double[] freq = var.getFrequencies();
        FastMatrix m = FastMatrix.make(length, var.dim());
        int nlast = freq.length - 1;
        if (freq[nlast] != 1) {
            ++nlast;
        }
        for (int i = 0; i < nlast; ++i) {
            double w = freq[i] * Math.PI;
            DataBlock c = m.column(2 * i);
            c.set(k -> Math.cos(w * (k + start)));
            DataBlock s = m.column(2 * i + 1);
            s.set(k -> Math.sin(w * (k + start)));
        }
        if (nlast < freq.length) { // PI
            DataBlock c = m.column(2 * nlast);
            c.set(k -> (k + start) % 2 == 0 ? 1 : -1);
        }
        return m;
    }

    static TrigonometricVariablesFactory FACTORY=new TrigonometricVariablesFactory();

    private TrigonometricVariablesFactory(){}

    @Override
    public boolean fill(TrigonometricVariables var, TsPeriod start, FastMatrix buffer) {
        TsPeriod refPeriod = start.withDate(var.getReference());
        long istart = start.getId() - refPeriod.getId();
        double[] freq = var.getFrequencies();
        int nlast = freq.length - 1;
        if (freq[nlast] != 1) {
            ++nlast;
        }
        for (int i = 0; i < nlast; ++i) {
            double w = freq[i] * Math.PI;
            DataBlock c = buffer.column(2 * i);
            c.set(k -> Math.cos(w * (k + istart)));
            DataBlock s = buffer.column(2 * i + 1);
            s.set(k -> Math.sin(w * (k + istart)));
        }
        if (nlast < freq.length) { // PI
            DataBlock c = buffer.column(2 * nlast);
            c.set(k -> (k + istart) % 2 == 0 ? 1 : -1);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(TrigonometricVariables var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
