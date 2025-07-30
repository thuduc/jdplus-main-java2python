/*
 * Copyright 2015 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
public interface IMultivariateSsfData {

    /**
     *
     * @param pos
     * @param v
     * @return
     */
    DoubleSeq get(int pos);
    /**
     *
     * @param pos
     * @param v
     * @return
     */
    double get(int pos, int v);

     /**
     *
     * @param pos
     * @param v
     * @return
     */
    boolean isMissing(int pos, int v);
    
    default boolean isConstraint(int pos, int v){
        return false;
    }

     /**
     *
     * @return
     */
    int getObsCount();
 
    int getVarsCount();
}
