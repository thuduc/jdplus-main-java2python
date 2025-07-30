/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.dk;

import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.akf.AugmentedState;
import jdplus.toolkit.base.core.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.univariate.PredictionErrorDecomposition;

/**
 *
 * @author Jean Palate
 */
public class DiffusePredictionErrorDecomposition extends PredictionErrorDecomposition implements IDiffuseFilteringResults, IDiffuseSquareRootFilteringResults {

    private final DeterminantalTerm ddet = new DeterminantalTerm();
    private int nd, enddiffuse;

    public DiffusePredictionErrorDecomposition(boolean res) {
        super(res);
    }

    @Override
    public DiffuseLikelihood likelihood(boolean scalingfactor) {
        return DiffuseLikelihood.builder(nd + cumulator.getObsCount(), nd)
                .concentratedScalingFactor(scalingfactor)
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant())
                .diffuseCorrection(ddet.getLogDeterminant())
                .residuals(bres ? res : null).build();
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public void clear() {
        super.clear();
        ddet.clear();
        nd = 0;
        enddiffuse = 0;
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        double d = pe.getDiffuseVariance();
        if (d != 0) {
            if (pe.getStatus() == DiffuseUpdateInformation.Status.OBSERVATION) {
                ++nd;
                ddet.add(d);
            }
        } else {
            double e = pe.get();
            if (pe.getStatus() == DiffuseUpdateInformation.Status.OBSERVATION) {
                cumulator.add(e, pe.getVariance());
            }
            if (bres) {
                double sd = pe.getStandardDeviation();
                if (e == 0) {
                    res.set(t, 0);
                } else if (sd == 0) {
                    res.set(t, Double.NaN);
                } else {
                    res.set(t, e / sd);
                }
            }
        }
    }

    @Override
    public void save(final int pos, final DiffuseState state, final StateInfo info) {
    }

    @Override
    public void save(final int pos, final AugmentedState state, final StateInfo info) {
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }

}
