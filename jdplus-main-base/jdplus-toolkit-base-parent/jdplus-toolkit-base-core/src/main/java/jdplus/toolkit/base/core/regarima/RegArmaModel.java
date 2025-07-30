/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.regarima;

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.eco.EcoException;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import java.util.List;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * Linear model with stationary ARMA process
 * The regression variables correspond to 
 * 1. The missing values (additive outliers approach)
 * 2. The mean correction
 * 3. The other regression variables
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <M>
 */
@lombok.Value
public class RegArmaModel<M extends IArimaModel> {

    /**
     * Creates a new RegArma model from an existing one, with a new stationary ARMA model
     * @param <M>
     * @param model
     * @param newarma
     * @return 
     */
    public static <M extends IArimaModel> RegArmaModel<M> of(RegArmaModel<M> model, M newarma){
        return new RegArmaModel(model.y, newarma, model.missingCount, model.x);
    }

    static <M extends IArimaModel> RegArmaModel<M> of(RegArimaModel<M> regarima) {
        StationaryTransformation<M> st = (StationaryTransformation<M>) regarima.arima().stationaryTransformation();
        M arma = st.getStationaryModel();
        BackFilter ur = st.getUnitRoots();
        int d = ur.getDegree();
        int n = regarima.getObservationsCount();
        int ndy = n - d;
        if (ndy <= 0) {
            throw new EcoException(EcoException.NOT_ENOUGH_OBS);
        }

        DoubleSeq y = regarima.getY();
        boolean mean = regarima.isMean();
        List<DoubleSeq> x = regarima.getX();
        int[] missing = regarima.missing();
        int nx = regarima.getMissingValuesCount() + regarima.getVariablesCount();
        FastMatrix dx = FastMatrix.make(ndy, nx);
        double[] dy;
        // dy
        if (d > 0) {
            dy = new double[y.length() - d];
            ur.apply(y, DataBlock.of(dy));
        } else {
            dy = y.toArray();
        }
        // dx
        if (nx > 0) {
            DataBlockIterator cols = dx.columnsIterator();
            if (d > 0) {
                if (missing.length > 0) {
                    DoubleSeq coeff = ur.asPolynomial().coefficients().reverse();
                    for (int i = 0; i < missing.length; ++i) {
                        DataBlock col = cols.next();
                        if (missing[i] >= dy.length) {
                            col.range(missing[i] - d, dy.length).copy(coeff.drop(0, y.length() - missing[i]));
                        } else if (missing[i] >= d) {
                            col.range(missing[i] - d, missing[i] + 1).copy(coeff);
                        } else {
                            col.range(0, missing[i] + 1).copy(coeff.drop(d - missing[i], 0));
                        }
                    }
                }
                if (mean) {
                    cols.next().set(1);
                }
                for (DoubleSeq var : x) {
                    ur.apply(var, cols.next());
                }
            } else {
                for (int i = 0; i < missing.length; ++i) {
                    cols.next().set(missing[i], 1);
                }
                if (mean) {
                    cols.next().set(1);
                }
                for (DoubleSeq var : x) {
                    ur.apply(var, cols.next());
                }
            }
        }
        return new RegArmaModel<>(DoubleSeq.of(dy), arma, missing.length, dx);

    }

    /**
     * The differenced (interpolated) observations. Should not contain missing values
     * (handled by additive outliers)
     */
    @lombok.NonNull
    DoubleSeq y;
    /**
     * The stationary model
     */
    @lombok.NonNull
    M arma;
    /**
     * For information only. Number of missing observations (additive outliers at the beginning of x)
     */
    int missingCount;
    
    /**
     * The differenced regression variables. Contains successively
     * - the additive outliers corresponding to the missing values,
     * - the constant
     * - the other regression variables
     */
    FastMatrix x;
    
    /**
     * Gets the regression model.
     * It contains all the variables including the additive outliers corresponding to missing values
     * and the mean.
     * The order is
     * - AO for missing
     * - Mean
     * - Other regression variables
     * @return 
     */
    public LinearModel asLinearModel(){
        // the mean is in X. Don't repeat it in the linear model
        return new LinearModel(y.toArray(), false, FastMatrix.of(x));
    }
    
}
