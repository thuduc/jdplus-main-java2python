/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.regular.IModelEstimator;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
abstract class ModelController {

    private IModelEstimator estimator;
    private RegSarimaModelling refmodelling;

    RegSarimaModelling getReferenceModel() {
        return refmodelling;
    }

    void setReferenceModel(RegSarimaModelling model) {
        refmodelling = model;
    }

    IModelEstimator getEstimator() {
        return estimator;
    }

    void setEstimator(IModelEstimator estimator) {
        this.estimator = estimator;
    }

    abstract ProcessingResult process(RegSarimaModelling modelling, TramoContext context);

    /**
     *
     * @param context
     * @return True if the mean is significant
     */
    protected boolean checkMean(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        RegArimaEstimation<SarimaModel> estimation = context.getEstimation();
        if (!desc.isMean()) {
            return true;
        }
        int nhp = desc.getArimaSpec().freeParametersCount();
        double ser = estimation.getConcentratedLikelihood().ser(0, nhp, true);
        return Math.abs(estimation.getConcentratedLikelihood().coefficient(0) / ser) >= 1.96;
    }

    protected boolean estimate(RegSarimaModelling context, boolean checkmean) {
        if (!estimator.estimate(context)) {
            return false;
        }
        if (checkmean) {
            if (!checkMean(context)) {
                context.getDescription().setMean(false);

                if (!estimator.estimate(context)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void transferInformation(RegSarimaModelling from, RegSarimaModelling to) {
        to.set(from.getDescription(),from.getEstimation());
        //        to.information.clear();
        //        to.information.copy(from.information);
    }

}
