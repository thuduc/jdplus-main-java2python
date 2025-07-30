/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.composite;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
class ComplexLoading implements ISsfLoading {

    private final ISsfLoading[] loadings;
    private final int[] dim;
    private final int[] start;
    private final DataBlock tmp;

    ComplexLoading(final int[] start, final int[] dim, final ISsfLoading[] ms) {
        this.loadings = ms;
        this.start = start;
        this.dim = dim;
        int n = ms.length;
        int tdim = 0;
        for (int i = 0; i < n; ++i) {
            if (dim[i] > tdim) {
                tdim = dim[i];
            }
        }
        tmp = DataBlock.make(tdim);
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < loadings.length; ++i) {
            if (!loadings[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        for (int i = 0; i < loadings.length; ++i) {
            loadings[i].Z(pos, z.extract(start[i], dim[i]));
        }
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        double x = 0;
        for (int i = 0; i < loadings.length; ++i) {
            x += loadings[i].ZX(pos, m.extract(start[i], dim[i]));
        }
        return x;
    }

    @Override
    public double ZVZ(int pos, FastMatrix v) {
        double x = 0;
        for (int i = 0; i < loadings.length; ++i) {
            FastMatrix D = v.extract(start[i], dim[i], start[i], dim[i]);
            x += loadings[i].ZVZ(pos, D);
            for (int j = i + 1; j < loadings.length; ++j) {
                DataBlock cur = tmp.range(0, dim[i]);
                FastMatrix C = v.extract(start[j], dim[j], start[i], dim[i]);
                loadings[j].ZM(pos, C, cur);
                x += 2 * loadings[i].ZX(pos, cur);
            }
        }
        return x;
    }

    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
        if (d == 0) {
            return;
        }
        for (int i = 0; i < loadings.length; ++i) {
            FastMatrix D = V.extract(start[i], dim[i], start[i], dim[i]);
            loadings[i].VpZdZ(pos, D, d);
            for (int j = i + 1; j < loadings.length; ++j) {
                if (dim[j] < dim[i]) {
                    DataBlock cur = tmp.range(0, dim[j]);
                    cur.set(0);
                    loadings[j].Z(pos, cur); // Zj
                    FastMatrix C = V.extract(start[i], dim[i], start[j], dim[j]);
                    DataBlockIterator cols = C.columnsIterator();
                    int k = 0;
                    while (cols.hasNext()) {
                        double zj = tmp.get(k++);
                        DataBlock n = cols.next();
                        if (zj != 0) {
                            loadings[i].XpZd(pos, n, d * zj);
                        }
                    }
                    FastMatrix CC = V.extract(start[j], dim[j], start[i], dim[i]);
                    CC.copyTranspose(C);
                } else {
                    DataBlock cur = tmp.range(0, dim[i]);
                    cur.set(0);
                    loadings[i].Z(pos, cur); // Zj
                    FastMatrix C = V.extract(start[j], dim[j], start[i], dim[i]);
                    DataBlockIterator cols = C.columnsIterator();
                    int k = 0;
                    while (cols.hasNext()) {
                        double zi = tmp.get(k++);
                        DataBlock n = cols.next();
                        if (zi != 0) {
                            loadings[j].XpZd(pos, n, d * zi);
                        }
                    }
                    FastMatrix CC = V.extract(start[i], dim[i], start[j], dim[j]);
                    CC.copyTranspose(C);
                }
            }
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        for (int i = 0; i < loadings.length; ++i) {
            loadings[i].XpZd(pos, x.extract(start[i], dim[i]), d);
        }
    }

}
