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

import jdplus.toolkit.base.core.regsarima.regular.IModelComparator;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.arima.IArimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class AICcComparator implements IModelComparator {

    /**
     * Preference for the reference model
     * It means that the alternative model is accepted if
     * aicc(ref) > aicc(alternative) - aiccdiff
     * @param aiccDiff
     */
    public AICcComparator(double aiccDiff) {
        aicDiff = aiccDiff;
    }
    private final double aicDiff;

    @Override
    public <M extends IArimaModel> int compare(RegArimaEstimation<M> reference, RegArimaEstimation<M>[] models) {
        int imin = -1;
        double aicc = 0;
        for (int i = 0; i < models.length; ++i) {
            if (models[i] != null) {
                double aiccCur = models[i].statistics().getAICC();
                if (imin < 0 || aiccCur < aicc) {
                    aicc = aiccCur;
                    imin = i;
                }
            }
        }
        if (imin < 0) {
            return -1;
        }
        else if (reference == null) {
            return imin;
        }
        else {
            double aiccRef = reference.statistics().getAICC();
            return aicc < aiccRef - aicDiff ? imin : -1;
        }
    }

    /**
     * 
     * @param <M>
     * @param reference Model without the considered regression variables
     * @param alternative Model with the considered variables
     * @return if -1; take the model without the regression variables; if 0, take the model 
     * with the regression variable
     */
    @Override
    public <M extends IArimaModel> int compare(RegArimaEstimation<M> reference, RegArimaEstimation<M> alternative) {
        if (reference == null) {
            return 0;
        }
        else if (alternative == null) {
            return -1;
        }
        double aiccRef = reference.statistics().getAICC();
        double aicc = alternative.statistics().getAICC();
        return aicc < aiccRef - aicDiff ? 0 : -1;
    }
    
}
