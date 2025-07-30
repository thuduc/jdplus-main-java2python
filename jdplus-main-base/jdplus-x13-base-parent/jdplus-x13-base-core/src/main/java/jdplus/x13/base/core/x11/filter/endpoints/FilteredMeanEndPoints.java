/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.filter.endpoints;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Nina Gonschorreck
 */
public class FilteredMeanEndPoints implements IEndPointsProcessor {

    private SymmetricFilter filter;

    /**
     *
     * @param filter
     */
    public FilteredMeanEndPoints(SymmetricFilter filter) {
        this.filter = filter;
    }

    @Override
    public void process(DoubleSeq in, DataBlock out) {
        int len = filter.length() / 2;
        int n = in.length();

        // expand the block...
        double[] tmp = new double[n + 2 * len];
        // copy the input
        in.copyTo(tmp, len);

        // computes the means
        DoubleSeq rbeg = in.range(0, len);
        DoubleSeq rend = in.range(n - len, n);

        double beg = rbeg.average();
        double end = rend.average();
        // fill the first/last items
        for (int i = 0, j = n + len; i < len; ++i, ++j) {
            tmp[i] = beg;
            tmp[j] = end;
        }
        filter.apply(DataBlock.of(tmp), out);
    }
}
