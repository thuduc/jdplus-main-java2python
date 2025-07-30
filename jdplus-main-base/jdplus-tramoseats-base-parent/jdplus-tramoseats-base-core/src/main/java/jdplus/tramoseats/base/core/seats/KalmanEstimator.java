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
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ExtendedSsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class KalmanEstimator implements IComponentsEstimator {

    private final int nfcasts, nbcasts;

    public KalmanEstimator(int nbcasts, int nfcasts) {
        this.nfcasts = nfcasts;
        this.nbcasts = nbcasts;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public SeriesDecomposition decompose(SeatsModel model) {
        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(DecompositionMode.Additive);
        TsData s = model.getTransformedSeries();
        builder.add(s, ComponentType.Series);
        int n = s.length(), nf = model.extrapolationCount(nfcasts),
                nb = model.extrapolationCount(nbcasts);

        ComponentType[] cmps = model.componentsType();
        UcarimaModel ucm = model.compactUcarimaModel(true, true);

        CompositeSsf ssf = SsfUcarima.of(ucm);
        // compute KS
        ISsfData data = new ExtendedSsfData(new SsfData(s.getValues()), nb, nf);
        double mvar = model.getInnovationVariance();
        DefaultSmoothingResults srslts;
        if (mvar != 0) {
            // for using the same standard error (unbiased stdandard error, not ml)
            srslts = DkToolkit.sqrtSmooth(ssf, data, true, false);
            srslts.rescaleVariances(mvar);
        } else {
            srslts = DkToolkit.sqrtSmooth(ssf, data, true, true);
        }

        TsPeriod start = s.getStart(), bstart = start.plus(-nb), fstart = start.plus(n);
        int[] pos = ssf.componentsPosition();
        TsData cmp;
        int scmp = -1;
        for (int i = 0; i < pos.length; ++i) {
            ComponentType type = cmps[i];
            if (type == ComponentType.Seasonal) {
                scmp = i;
            }
            cmp = TsData.of(bstart, srslts.getComponent(pos[i]));
            if (nb > 0) {
                builder.add(cmp.range(0, nb), type, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(cmp.extract(nb + n, nf), type, ComponentInformation.Forecast);
            }
            builder.add(cmp.extract(nb, n), type);
            cmp = TsData.of(bstart, srslts.getComponentVariance(pos[i]).fn(x -> x <= 0 ? 0 : Math.sqrt(x)));
            if (nb > 0) {
                builder.add(cmp.range(0, nb), type, ComponentInformation.StdevBackcast);
            }
            if (nf > 0) {
                builder.add(cmp.extract(nb + n, nf), type, ComponentInformation.StdevForecast);
            }
            builder.add(cmp.extract(nb, n), type, ComponentInformation.Stdev);
            if (type == ComponentType.Seasonal) {
                // No missing values !
                builder.add(cmp.extract(nb, n), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            }
        }
        if (scmp < 0) {
            builder.add(s, ComponentType.SeasonallyAdjusted);
        }

        DataBlock z = DataBlock.make(ssf.getStateDim());
        ssf.measurement().loading().Z(0, z);
        if (nb > 0) {
            double[] a = new double[nb];
            double[] e = new double[nb];
            for (int i = 0; i < a.length; ++i) {
                a[i] = srslts.a(i).dot(z);
                e[i] = QuadraticForm.apply(srslts.P(i), z);
            }
            TsData sb = TsData.ofInternal(bstart, a);
            builder.add(sb, ComponentType.Series, ComponentInformation.Backcast);
            cmp = TsData.of(bstart, DoubleSeq.of(e).fn(x -> x <= 0 ? 0 : Math.sqrt(x)));
            builder.add(cmp, ComponentType.Series, ComponentInformation.StdevBackcast);
            if (scmp < 0) {  // SA == y
                builder.add(sb, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
        }
        if (nf > 0) {
            double[] a = new double[nf];
            double[] e = new double[nf];
            for (int i = 0, j = nb + n; i < a.length; ++i, ++j) {
                a[i] = srslts.a(j).dot(z);
                e[i] = QuadraticForm.apply(srslts.P(j), z);
            }
            TsData sf = TsData.ofInternal(fstart, a);
            builder.add(sf, ComponentType.Series, ComponentInformation.Forecast);
            cmp = TsData.of(fstart, DoubleSeq.of(e).fn(x -> x <= 0 ? 0 : Math.sqrt(x)));
            builder.add(cmp, ComponentType.Series, ComponentInformation.StdevForecast);
            if (scmp < 0) {  // SA == y
                builder.add(sf, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }
        // idem for SA

        if (scmp >= 0) {
            z.range(pos[scmp], scmp == pos.length - 1 ? ssf.getStateDim() : pos[scmp + 1]).set(0);
            TsData sa = TsData.of(bstart, srslts.zcomponent(z));
            builder.add(sa.range(nb, nb + n), ComponentType.SeasonallyAdjusted);
            if (nb > 0) {
                builder.add(sa.range(0, nb), ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
                double[] a = new double[nb];
                for (int i = 0; i < a.length; ++i) {
                    a[i] = QuadraticForm.apply(srslts.P(i), z);
                }
                cmp = TsData.of(bstart, DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x)));
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast);
            }
            if (nf > 0) {
                builder.add(sa.range(nb + n, nb + n + nf), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                double[] a = new double[nf];
                for (int i = 0, j = n + nb; i < a.length; ++i, ++j) {
                    a[i] = QuadraticForm.apply(srslts.P(j), z);
                }
                cmp = TsData.of(fstart, DoubleSeq.of(a).fn(x -> x <= 0 ? 0 : Math.sqrt(x)));
                builder.add(cmp, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        }
        return builder.build();
    }

}
