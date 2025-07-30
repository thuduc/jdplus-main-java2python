/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.core.regarima;

import jdplus.toolkit.base.core.regarima.estimation.ConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.regsarima.GlsSarimaComputer;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.arima.estimation.IArimaMapping;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class RegArimaToolkit {

    public RegArimaEstimation<SarimaModel> robustEstimation(RegArimaModel<SarimaModel> regarima, IArimaMapping<SarimaModel> mapping){
        return RegSarimaComputer.PROCESSOR.process(regarima, mapping);
    }

    public RegArimaEstimation<SarimaModel> fastEstimation(RegArimaModel<SarimaModel> regarima, IArimaMapping<SarimaModel> mapping){
        return GlsSarimaComputer.PROCESSOR.process(regarima, mapping);
    }
    
    public RegArimaEstimation<SarimaModel> concentratedLikelihood(RegArimaModel<SarimaModel> regarima){
        ConcentratedLikelihoodWithMissing cl = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
        return RegArimaEstimation.<SarimaModel>builder()
                .model(regarima)
                .concentratedLikelihood(cl)
                .build();
    }
}
