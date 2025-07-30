/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.data.Range;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import java.time.LocalDateTime;
import java.util.List;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.RationalFunction;

/**
 *
 * @author palatej
 */
class IVFactory implements RegressionVariableFactory<InterventionVariable> {

    static IVFactory FACTORY = new IVFactory();

    private IVFactory() {
    }

    @Override
    public boolean fill(InterventionVariable var, TsPeriod start, FastMatrix buffer) {
        int dcount = buffer.getRowsCount();
        List<Range<LocalDateTime>> seqs = var.getSequences();
        if (seqs.isEmpty()) {
            return true;
        }
        // first, generates the 0/1
        LocalDateTime t0 = LocalDateTime.MAX, t1 = LocalDateTime.MIN;
        // search the Start / End of the sequences

        for (Range<LocalDateTime> seq :seqs) {
            if (t0.isAfter(seq.start())) {
                t0 = seq.start();
            }
            if (t1.isBefore(seq.end())) {
                t1 = seq.end();
            }
        }

        // period of estimation : Start->domain[last]
        TsPeriod pstart = start.withDate(t0), pend = start.withDate(t1);
        int n = dcount - start.until(pstart);
        if (n < 0) {
            return true;
        }

        double[] tmp = new double[n];
        for (Range<LocalDateTime> seq :seqs) {
            TsPeriod curstart = start.withDate(seq.start()),
                    curend = start.withDate(seq.end());

            int istart = pstart.until(curstart);
            int iend = 1 + pstart.until(curend);
            if (iend > n) {
                iend = n;
            }
            for (int j = istart; j < iend; ++j) {
                tmp[j] += 1;
            }
        }

        double delta=var.getDelta(), deltas=var.getDeltaSeasonal();
        int freq=start.getUnit().getAnnualFrequency();
        if (delta != 0 || deltas != 0) {
            // construct the filter
            Polynomial num = Polynomial.ONE;
            Polynomial d = delta != 0 ? Polynomial.valueOf(1, -delta): Polynomial.ONE;
            if (freq != 1 && deltas != 0) {
                double[] ds = new double[freq+1];
                ds[0] = 1;
                ds[freq] = -deltas;
                d = d.times(Polynomial.of(ds));
            }
            RationalFunction rf = RationalFunction.of(num, d);
            double[] w = rf.coefficients(n);

            // apply the filter
            double[] ftmp = new double[n];
            for (int i = 0; i < ftmp.length; ++i) {
                if (tmp[i] != 0) {
                    for (int j = 0; j < ftmp.length - i; ++j) {
                        ftmp[i + j] += tmp[i] * w[j];
                    }
                }
            }
            tmp = ftmp;
        }
        // copy in rslt
        int di = start.until(pstart);
        if (di > 0) {
            buffer.column(0).drop(di, 0).copyFrom(tmp, 0);
        } else {
            buffer.column(0).copyFrom(tmp, -di);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(InterventionVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
