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
package jdplus.toolkit.base.core.ssf.basic;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantLoading implements ISsfLoading {

    private final DataBlock Z;

    public static TimeInvariantLoading of(int dim, ISsfLoading loading) {
        if (!loading.isTimeInvariant()) {
            return null;
        }
        DataBlock Z = DataBlock.make(dim);
        loading.Z(0, Z);
        return new TimeInvariantLoading(Z);
    }

    public TimeInvariantLoading(DataBlock Z) {
        this.Z = Z;
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        z.copy(Z);
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        return Z.dot(m);
    }

    @Override
    public double ZVZ(int pos, FastMatrix V) {
        DataBlock zv = DataBlock.make(V.getColumnsCount());
        zv.product(Z, V.columnsIterator());
        return zv.dot(Z);
    }

    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0)
                return;
        DataBlockIterator cols = V.columnsIterator();
        DoubleSeqCursor z=Z.cursor();
        while (cols.hasNext()) {
            cols.next().addAY(d * z.getAndNext(), Z);
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        x.addAY(d, Z);
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("Z:\r\n").append(Z.toString(FMT)).append("\r\n");
//        builder.append("H:\r\n").append(new DecimalFormat(FMT).format(var)).append("\r\n\r\n");
        return builder.toString();
    }
    private static final String FMT="0.#####";

}
