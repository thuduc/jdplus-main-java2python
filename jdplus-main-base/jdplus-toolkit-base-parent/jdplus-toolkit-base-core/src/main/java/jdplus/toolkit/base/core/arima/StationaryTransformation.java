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


package jdplus.toolkit.base.core.arima;

import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;


/**
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class StationaryTransformation<S extends ILinearProcess> {

    /**
     * Stationary model. Same class as the original model
     */
    private S stationaryModel;

    /**
     * Unit roots removed by means of a stationary transformation
     */
    private BackFilter unitRoots;
}
