/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DataResults {

    double[] data;
    int start;
    int nused;

    /**
     *
     */
    public DataResults() {
    }

    /**
     *
     */
    public void clear() {
        data = null;
        nused = 0;
    }

    public void prepare(final int start, final int end) {
        this.start = start;
        data = new double[end - start];
        for (int i = 0; i < data.length; ++i) {
            data[i] = Double.NaN;
        }
    }

    public DataBlock all() {
        return DataBlock.of(data, 0, nused, 1);
    }

    /**
     * TODO: check : is it really what is needed ?
     * @param beg
     * @param n
     * @return 
     */
    public DataBlock extract(int beg, int n) {
        return DataBlock.of(data, beg, beg+n, 1);
    }
    /**
     *
     * @param t
     * @return
     */
    public double get(final int t) {
        if (data == null || t < start) {
            return Double.NaN;
        } else {
            return data[t - start];
        }
    }

    public boolean isMissing(final int t) {
        if (data == null || t < start) {
            return true;
        } else {
            return Double.isNaN(data[t - start]);
        }
    }
    public void save(final int t, final double x) {
        int st = t - start;
        if (st < 0) {
            return;
        }
        checkSize(st + 1);
        data[st] = x;
    }

    /**
     *
     * @return
     */
    public int getStartSaving() {
        return start;
    }

    /**
     *
     * @param p
     */
    public void setStartSaving(int p) {
        start = p;
        data = null;
    }

    public DoubleSeq asDoublesReader(boolean complete) {
        if (complete) {
            return DoubleSeq.onMapping(nused + start, i -> i < start ? Double.NaN : data[i - start]);
        } else {
            return DoubleSeq.onMapping(nused, i -> data[i]);
        }
    }

    private void checkSize(int size) {
        if (nused < size) {
            nused = size;
        }
        int cursize = data == null ? 0 : data.length;
        if (size > cursize) {
            int nsize = Math.max(DataBlockStorage.calcSize(size), cursize << 1);
            double[] tmp = new double[nsize];
            if (cursize > 0) {
                System.arraycopy(data, 0, tmp, 0, cursize);
            }
            for (int i = cursize; i < nsize; ++i) {
                tmp[i] = Double.NaN;
            }
            data = tmp;
        }
    }

    public void copyTo(double[] buffer, int start) {
        System.arraycopy(data, 0, buffer, start, nused);
    }

    public int getLength() {
        return nused;
    }

    public void rescale(double factor) {
        if (factor == 1) {
            return;
        }
        for (int i = 0; i < nused; ++i) {
            data[i] *= factor;
        }
    }
}
