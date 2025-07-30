/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.arima.estimation;

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.api.design.Algorithm;
import nbbrd.design.Development;
import nbbrd.service.ServiceDefinition;
import nbbrd.design.ThreadSafe;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */

@ThreadSafe
@Algorithm
@ServiceDefinition(singleton = true)
@Development(status = Development.Status.Beta)
public interface ArimaForecasts {
    
    /**
     * Initialises the forecasts routine
     * @param model The ARIMA model used for forecasting
     * @param mean Mean correction. The correction must be computed
     * @return True if the initialisation was successful, false otherwise
     */
    boolean prepare(final IArimaModel model, final boolean mean);
    
    /**
     * 
     * @param model
     * @param mean Mean correction (provided)
     * @return 
     */
    boolean prepare(final IArimaModel model, final double mean);
    /**
     * 
     * @param data The forecasted series
     * @param nforecasts The number of forecasts
     * @return The forecasts
     */
    DoubleSeq forecasts(DoubleSeq data, final int nforecasts);
    
    default DoubleSeq backcasts(DoubleSeq data, final int nbackcasts){
        return forecasts(data.reverse(), nbackcasts).reverse();
    }
    
    double getMean();
}
