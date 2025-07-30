/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.advanced.algebra;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Exploratory)
public interface RealHilbertSpace<T> extends RealVectorSpace<T>{
    
    double dot(T a, T b);
    
    default double norm(T a){
        return Math.sqrt(dot(a,a));
    }
    
    default double distance(T a, T b){
        return norm(minus(a,b));
    }
}
