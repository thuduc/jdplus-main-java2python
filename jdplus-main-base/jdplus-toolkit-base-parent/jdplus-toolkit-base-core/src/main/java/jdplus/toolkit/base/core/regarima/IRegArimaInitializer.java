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


package jdplus.toolkit.base.core.regarima;

import jdplus.toolkit.base.core.arima.IArimaModel;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.arima.estimation.IArimaMapping;

/**
 * Defines the generic interface for the initial estimation of a RegArima model
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Beta)
@FunctionalInterface
public interface IRegArimaInitializer<M extends IArimaModel>  {
    /**
     * Initializes a regarima model (first improvement of the parameters of the model)
     * @param regarima
     * @return
     */
    RegArimaModel<M> initialize(RegArimaModel<M> regarima, IArimaMapping<M> mapping);

}
