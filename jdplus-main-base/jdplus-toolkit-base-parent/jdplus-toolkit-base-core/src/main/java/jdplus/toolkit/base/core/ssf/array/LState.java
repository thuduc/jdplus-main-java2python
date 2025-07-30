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
package jdplus.toolkit.base.core.ssf.array;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfState;
import jdplus.toolkit.base.core.ssf.State;


/**
 * Represents a gaussian vector, with its mean and covariance matrix.
 * The way information must be interpreted is given by the state info.
 * This is similar to the NRV (normal random vector) of Snyder/Forbes (apart from 
 * the additional info)
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LState {
    
    /**
     * a is the state vector. a(t|t-1)
     */
    public final DataBlock a;

    /**
     * L is the Cholesky factor of the covariance of the state vector (P(t|t-1)). 
     */
    public final FastMatrix L;


    /**
     * @param L Initial Cholesky factor
     */
    public LState(final FastMatrix L) {
        a = DataBlock.make(L.getRowsCount());
        this.L = L;
    }

    public static LState of(ISsfState ssf) {
        FastMatrix L=FastMatrix.square(ssf.getStateDim());
        ssf.initialization().Pf0(L);
        SymmetricMatrix.lcholesky(L, State.ZERO);
        LowerTriangularMatrix.toLower(L);
        LState state = new LState(L);
        ssf.initialization().a0(state.a);
        return state;
    }
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("mean:\r\n").append(a).append("\r\n");
        builder.append("Cholesky factor of the covariance:\r\n").append(L);
        return builder.toString();
   }
    
}
