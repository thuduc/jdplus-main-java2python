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
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ucarima.estimation.McElroyEstimates;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MatrixEstimator implements IComponentsEstimator {

    private final int nfcasts, nbcasts;

    public MatrixEstimator(int nbcasts, int nfcasts) {
        this.nfcasts = nfcasts;
        this.nbcasts = nbcasts;
    }

    /**
     *
     * @param s
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        TsData s = model.getTransformedSeries();
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(DecompositionMode.Additive);
        ComponentType[] cmps = model.componentsType();
        UcarimaModel ucm = model.compactUcarimaModel(true, true);
        McElroyEstimates mc = new McElroyEstimates();
        mc.setForecastsCount(model.extrapolationCount(nfcasts));
        // TODO backcasts
        mc.setUcarimaModel(ucm);
        mc.setData(s.getValues());
        double ser = Math.sqrt(model.getInnovationVariance());
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            ComponentType type = cmps[i];
            double[] tmp = mc.getComponent(i);
            builder.add(TsData.ofInternal(s.getStart(), tmp), type);
            tmp = mc.stdevEstimates(i);
            for (int j = 0; j < tmp.length; ++j) {
                tmp[j] *= ser;
            }
            builder.add(TsData.ofInternal(s.getStart(), tmp), type, ComponentInformation.Stdev);
            tmp = mc.getForecasts(i);
            builder.add(TsData.ofInternal(s.getStart(), tmp), type, ComponentInformation.Forecast);
            tmp = mc.stdevForecasts(i);
            for (int j = 0; j < tmp.length; ++j) {
                tmp[j] *= ser;
            }
            builder.add(TsData.ofInternal(s.getStart(), tmp), type, ComponentInformation.StdevForecast);
        }
        builder.add(s, ComponentType.Series);
        return builder.build();
    }
}
