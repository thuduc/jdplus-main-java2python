/*
* Copyright 2013 National Bank of Belgium
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

package jdplus.toolkit.base.core.data.transformation;

import nbbrd.design.Development;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * Interface for transformation of a time series
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface DataTransformation {


    /**
     * Gives the converse transformation. Applying a transformation and its
     * converse should not change the initial series
     *
     * @return The converse transformation.
     */
    DataTransformation converse();

    /**
     * Transforms a time series.
     *
     * @param data The data being transformed.
     * @param logjacobian I/O parameter. The log of the Jacobian of this transformation
     * @return The transformed data. Null if the transformation was not successful
     */
    DoubleSeq transform(DoubleSeq data, LogJacobian logjacobian);
    
    double transform(double value);
}
