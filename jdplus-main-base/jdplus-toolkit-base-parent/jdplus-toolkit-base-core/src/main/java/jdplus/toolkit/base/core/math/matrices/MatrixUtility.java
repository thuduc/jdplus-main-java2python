/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices;

import jdplus.toolkit.base.core.random.MersenneTwister;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class MatrixUtility {

    public void randomize(FastMatrix M, RandomNumberGenerator rng) {
        if (M.isEmpty()) {
            return;
        }
        if (rng == null) {
            rng = MersenneTwister.fromSystemNanoTime();
        }
        double[] x = M.getStorage();
        if (M.isFull()) {
            for (int i = 0; i < x.length; ++i) {
                x[i] = rng.nextDouble();
            }
            return;
        }
        int m = M.getRowsCount(), n = M.getColumnsCount(), start = M.getStartPosition(), lda = M.getColumnIncrement();
        int jmax = start + n * lda;
        for (int j = start; j < jmax; j += lda) {
            int imax = j + n;
            for (int i = j; i < imax; ++i) {
                x[i] = rng.nextDouble();
            }
        }
    }

}
