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


package jdplus.tramoseats.base.core.seats;

import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultModelEstimator implements IModelEstimator {
    
    private final IModelValidator validator;
    private final IRegArimaComputer<SarimaModel> processor;
    
    public DefaultModelEstimator(IModelValidator validator, IRegArimaComputer<SarimaModel> processor){
        this.validator=validator;
        if (processor == null)
            this.processor=RegSarimaComputer.PROCESSOR;
        else
            this.processor=processor;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public boolean estimate(SeatsModel model) {

	RegArimaEstimation<SarimaModel> estimation = processor.process(model
		.asRegarima(), null);
	if (estimation == null)
	    return false;

        SarimaModel arima=estimation.getModel().arima();
        if (validator != null && !validator.validate(arima))
            arima=validator.getNewModel();
	model.setCurrentModel(arima);
        LikelihoodStatistics stat = estimation.statistics();
        model.setInnovationVariance(stat.getSsqErr()/(stat.getEffectiveObservationsCount()-stat.getEstimatedParametersCount()));
	return true;
    }

}
