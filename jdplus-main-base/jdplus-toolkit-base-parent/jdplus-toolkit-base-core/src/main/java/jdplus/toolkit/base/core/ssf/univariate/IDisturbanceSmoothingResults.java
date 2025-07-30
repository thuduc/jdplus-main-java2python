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
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;


/**
 *
 * @author Jean Palate
 */
public interface IDisturbanceSmoothingResults {

    default double e(int pos){
        return 0;
    }
    
    default double eVar(int pos){
        return 0;
    }

    default DataBlock u(int pos) {
        return null;
    }

    default FastMatrix uVar(int pos) {
        return null;
    }
    
    void prepare(ISsf ssf, int start, int end);
    
    void saveSmoothedTransitionDisturbances(int pos, DataBlock u, FastMatrix uVar);
    
    void saveSmoothedMeasurementDisturbance(int pos, double e, double evar);
    
    void rescaleVariances(double factor);

}
