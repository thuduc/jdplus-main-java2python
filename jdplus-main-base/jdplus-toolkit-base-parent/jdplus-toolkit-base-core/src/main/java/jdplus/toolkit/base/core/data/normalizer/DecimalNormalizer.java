/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jdplus.toolkit.base.core.data.normalizer;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import nbbrd.design.Development;


/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm=DataNormalizer.class)
@Development(status = Development.Status.Release)
public class DecimalNormalizer implements DataNormalizer {

    private static final double D_MAX = 10, D_MIN = 1e-1;

    private final double dmax_, dmin_;

    public DecimalNormalizer() {
        dmin_ = D_MIN;
        dmax_ = D_MAX;
    }

    /**
     * Scaling of toArray, except if all toArray (in abs) are in the range[dmin,
 dmax];
     *
     * @param dmin
     * @param dmax
     */
    public DecimalNormalizer(final double dmin, final double dmax) {
        this.dmin_ = dmin;
        this.dmax_ = dmax;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return dmax_;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return dmin_;
    }

    @Override
    public double normalize(DataBlock data) {
        int n = data.length();
        int i = data.indexOf((x) -> Double.isFinite(x));
        if (i == n) {
            return 1;
        }
        double ymax = data.get(i++), ymin = ymax;
        for (; i < n; ++i) {
            double ycur = Math.abs(data.get(i));
            if (Double.isFinite(ycur)) {
                ycur = Math.abs(ycur);
                if (ycur < ymin) {
                    ymin = ycur;
                } else if (ycur > ymax) {
                    ymax = ycur;
                }
            }
        }
        int k = 0;
        if (ymax < dmax_ && ymin > dmin_) {
            return 1;
        }
        while (ymin > 10) {
            --k;
            ymin /= 10;
        }
        while (ymax < 1e-1) {
            ++k;
            ymax *= 10;
        }
        if (k != 0) {
            double f = 1;
            for (i = 0; i < k; ++i) {
                f *= 10;
            }
            for (i = k; i < 0; ++i) {
                f /= 10;
            }
            final double c = f;
            data.apply((x) -> x * c);
            return c;

        } else {
            return 1;
        }
    }

}
