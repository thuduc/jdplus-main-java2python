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
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 * Efficient storage of state arrays
 * @author Jean Palate
 */
public class StateStorage implements IStateResults {

    private final DataBlockResults A;
    private final MatrixResults P;
    private final StateInfo info;

    protected StateStorage(final StateInfo info, final boolean cov) {
        A = new DataBlockResults();
        P = cov ? new MatrixResults() : null;
        this.info = info;
    }

    public static StateStorage full(final StateInfo info) {
        return new StateStorage(info, true);
    }

    public static StateStorage light(final StateInfo info) {
        return new StateStorage(info, false);
    }
    
    public boolean hasVariances(){
        return P != null;
    }
    
    public int size(){
        return A.getCurrentSize();
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
        if (info != this.info) {
            return;
        }
        A.save(t, state.a());

        if (P != null) {
            P.save(t, state.P());
        }
    }

    public void save(final int t, final DataBlock a, final FastMatrix p) {
        if (info != this.info) {
            return;
        }
        A.save(t, a);

        if (P != null) {
            P.save(t, p);
        }
    }

    public DoubleSeq getComponent(int pos) {
        return A.item(pos);
    }

    public DoubleSeq zcomponent(DoubleSeq z) {
        double[] a=new double[this.size()];
        for (int i=0; i<a.length; ++i)
            a[i]=A.datablock(i).dot(z);
        return DoubleSeq.of(a);
    }

    public DoubleSeq zvariance(DoubleSeq z) {
        DataBlock b=DataBlock.of(z);
        double[] a=new double[this.size()];
        for (int i=0; i<a.length; ++i)
            a[i]= QuadraticForm.apply(P.matrix(i), b);
        return DoubleSeq.of(a);
    }

    public DoubleSeq getComponentVariance(int pos) {
        return P.item(pos, pos);
    }

    public DataBlock a(int pos) {
        return A.datablock(pos);
    }

    public FastMatrix P(int pos) {
        return P == null ? null : P.matrix(pos);
    }
    
    public DataBlock item(int i){
        return A.item(i);
    }

    public DataBlock var(int i) {
        return P == null ? DataBlock.EMPTY : P.item(i, i);
    }

    public DataBlock covar(int i, int j) {
        return P == null ? DataBlock.EMPTY : P.item(i, j);
    }

    public int getStart() {
        return A.getStartSaving();
    }

    public void prepare(int dim, int start, int end) {
        A.prepare(dim, start, end);

        if (P != null) {
            P.prepare(dim, start, end);
        }
    }

    public void rescaleVariances(double factor) {
        if (P != null) {
            P.rescale(factor);
        }
    }

    public void clear() {
        A.clear();
        if (P != null) {
            P.clear();
        }
    }
}
