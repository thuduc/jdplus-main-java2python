/*
 * Copyright 2017 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class TimeInvariantDynamics implements ISsfDynamics {

    public static class Innovations {

        private static Innovations of(int stateDim, ISsfDynamics sd) {
            int ne = sd.getInnovationsDim();
            FastMatrix V = FastMatrix.square(stateDim);
            sd.V(0, V);
            FastMatrix S = FastMatrix.make(stateDim, ne);
            sd.S(0, S);
            return new Innovations(V, S);
        }

        public Innovations(final FastMatrix V) {
            this.V = V;
            S = null;
        }

        public Innovations(final FastMatrix V, final FastMatrix S) {
            this.S = S;
            if (V == null && S != null) {
                this.V = SymmetricMatrix.XXt(S);
            } else {
                this.V = V;
            }
        }

        public final FastMatrix S, V;
    }

    private final FastMatrix T;
    private final FastMatrix V;
    private transient FastMatrix S;

    public TimeInvariantDynamics(FastMatrix T, Innovations E) {
        this.T = T;
        this.S = E.S;
        this.V = E.V;

    }

    public static TimeInvariantDynamics of(int stateDim, ISsfDynamics sd) {
        if (!sd.isTimeInvariant()) {
            return null;
        }
        FastMatrix t = FastMatrix.square(stateDim);
        sd.T(0, t);
        Innovations e = Innovations.of(stateDim, sd);
        if (e == null) {
            return null;
        }
        return new TimeInvariantDynamics(t, e);
    }

    private synchronized void checkS() {
        if (S == null) {
            S = V.deepClone();
            SymmetricMatrix.lcholesky(S);
        }
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return true;
    }

    @Override
    public int getInnovationsDim() {
        return S == null ? T.getColumnsCount() : S.getColumnsCount();
    }

    @Override
    public void V(int pos, FastMatrix qm) {
        qm.copy(V);
    }

    @Override
    public boolean hasInnovations(int pos) {
        return V != null;
    }

    @Override
    public void S(int pos, FastMatrix sm) {
        checkS();
        sm.copy(S);
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        checkS();
        x.addProduct(S.rowsIterator(), u);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        checkS();
        xs.product(x, S.columnsIterator());
    }

    @Override
    public void T(int pos, FastMatrix tr) {
        tr.copy(T);
    }

    @Override
    public void TM(int pos, FastMatrix tm) {
        DataBlock tx = DataBlock.make(T.getColumnsCount());
        DataBlockIterator cols = tm.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            tx.product(T.rowsIterator(), col);
            col.copy(tx);
        } ;
    }

    @Override
    public void TVT(int pos, FastMatrix tvt) {
        FastMatrix V = tvt.deepClone();
        SymmetricMatrix.XSXt(V, T, tvt);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        DataBlock tx = DataBlock.make(x.length());
        tx.product(T.rowsIterator(), x);
        x.copy(tx);
    }

    @Override
    public void XT(int pos, DataBlock x) {
        DataBlock tx = DataBlock.make(x.length());
        tx.product(x, T.columnsIterator());
        x.copy(tx);
    }

    @Override
    public void addV(int pos, FastMatrix p) {
        p.add(V);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("T:\r\n").append(T.toString(FMT)).append(System.lineSeparator());
        builder.append("V:\r\n").append(V.toString(FMT)).append(System.lineSeparator());
        return builder.toString();
    }
    
    private static final String FMT="0.#####";

}
