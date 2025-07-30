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
package jdplus.toolkit.base.core.data.analysis;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.Constants;
import jdplus.toolkit.base.api.stats.AutoCovariances;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public class SmoothedPeriodogram {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(SmoothedPeriodogram.class)
    public static class Builder {

        private WindowFunction win = WindowFunction.Tukey;
        private int winLen = 44;
        private int resolution = 0;
        private Taper taper = null;
        private DoubleSeq data;

        private Builder() {
        }

        /**
         *
         * @param data Input data
         * @return
         */
        public Builder data(DoubleSeq data) {
            this.data = data;
            return this;
        }

        /**
         *
         * @param taper The taper used in a first step. Null by default (no
         * tapering)
         * @return
         */
        public Builder taper(Taper taper) {
            this.taper = taper;
            return this;
        }

        /**
         *
         * @param win The window function used to smooth the autocorrelations
         * @return
         */
        public Builder windowFunction(WindowFunction win) {
            this.win = win;
            return this;
        }

        /**
         * Gives the length of the window. Only the autocorrelations belonging
         * to [-windowLength, +windowLength] will be taken into account.
         *
         * @param windowLength The length of the window function.
         * @return
         */
        public Builder windowLength(int windowLength) {
            this.winLen = windowLength;
            return this;
        }

        /**
         * Resolution of the periodogram: the spectrum is computed at the
         * frequencies 2*pi*k/resolution. If resolution is undefined, we take
         * half of the window length
         *
         * @param resolution Should be smaller than the windowLength
         * @return
         */
        public Builder resolution(int resolution) {
            this.resolution = resolution;
            return this;
        }

        public SmoothedPeriodogram build() {
            if (data == null) {
                throw new RuntimeException("Uninitialized data");
            }
            if (winLen >= data.length()) {
                throw new RuntimeException("Not enough data");
            }

            double[] x = data.toArray();
            // correct for mean
            double mean = DoublesMath.averageWithMissing(data);
            if (mean != 0) {
                for (int i = 0; i < x.length; ++i) {
                    x[i] -= mean;
                }
            }

            if (taper != null) {
                taper.process(x);
            }

            DoubleSeq datac = DoubleSeq.of(x);
            double[] ac = AutoCovariances.autoCovariancesWithZeroMean(datac, winLen - 1);

            double[] cwnd = win.discreteWindow(winLen);
            for (int i = 1; i < winLen; i++) {
                ac[i] *= cwnd[i] / ac[0];
            }
            ac[0] = 1;

            int res=resolution != 0 ? resolution : winLen;
            int nres = 1+res / 2;
            double[] p = new double[nres];
            for (int i = 0; i < nres; ++i) {
                p[i] = dft(ac, i * Constants.TWOPI / res);
            }
            return new SmoothedPeriodogram(p, res);
        }

        private double dft(double[] ac, double freq) {
            double p = 1;
            for (int j = 1; j < winLen; j++) {
                p += 2 * ac[j] * Math.cos(j * freq);
            }
            if (p < 0) {
                p = 0;
            }
            return p;
        }

    }

    private final double[] p;
    private final int resolution;

    private SmoothedPeriodogram(double[] p, int resolution) {
        this.p = p;
        this.resolution = resolution;
    }

    public double spectrumValueAtFrequency(double freq) {
        int ipos = (int) Math.round(freq * resolution / Constants.TWOPI);
        if (ipos == p.length) {
            ipos = p.length - 1;
        }
        if (ipos < 0 || ipos >= p.length) {
            return Double.NaN;
        } else {
            return p[ipos];
        }
    }

    public DoubleSeq spectrumValues() {
        return DoubleSeq.of(p);
    }
}
