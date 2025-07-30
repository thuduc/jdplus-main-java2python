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
package jdplus.toolkit.base.api.stats;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AutoCovariances {

    public final double SMALL = 1e-38;

    public double[] autoCovariancesWithZeroMean(DoubleSeq data, int maxLag) {
        return autoCovariances(data, 0, maxLag);
    }

    public double[] autoCovariances(DoubleSeq data, double mean, int maxLag) {
        double[] autoCovariance = new double[maxLag + 1];
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            autoCovariance[0] = variance(data, mean);
            for (int i = 1; i <= maxLag; ++i) {
                autoCovariance[i] = autoCovariance(data, mean, i);
            }
        } else {
            autoCovariance[0] = varianceNoMissing(data, mean);
            for (int i = 1; i <= maxLag; ++i) {
                autoCovariance[i] = autoCovarianceNoMissing(data, mean, i);
            }
        }
        return autoCovariance;
    }

    /**
     * Computes the covariance between two arrays of doubles, which are supposed
     * to have zero means; the arrays might contain missing values (Double.NaN);
     * those values are omitted in the computation of the covariance (and the
     * number of observations are adjusted).
     *
     * @param x The first array
     * @param y The second array
     * @param t The delay between the two arrays
     * @return The covariance; covariance = sum((x(i)*y(i+t)/n)
     */
    public double covarianceWithZeroMean(DoubleSeq x, DoubleSeq y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMean(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        int nm = 0;
        DoubleSeqCursor xr = x.cursor();
        DoubleSeqCursor yr = y.cursor();
        yr.moveTo(t);
        for (int i = 0; i < n; ++i) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += xcur * ycur;
            } else {
                ++nm;
            }
        }
        int m = x.length() - nm;
        if (m == 0) {
            return 0;
        }
        return v / m;
    }

    public double covarianceWithZeroMean(DoubleSeq x, DoubleSeq y) {
        return covarianceWithZeroMean(x, y, 0);
    }

    public double covarianceWithZeroMeanAndNoMissing(DoubleSeq x, DoubleSeq y, int t) {
        // x and y must have the same Length...
        if (t < 0) {
            return covarianceWithZeroMeanAndNoMissing(y, x, -t);
        }
        double v = 0;
        int n = x.length() - t;
        DoubleSeqCursor xr = x.cursor();
        DoubleSeqCursor yr = y.cursor();
        yr.moveTo(t);
        for (int i = 0; i < n; ++i) {
            v += xr.getAndNext() * yr.getAndNext();
        }
        return v / x.length();
    }

    public IntToDoubleFunction autoCorrelationFunction(DoubleSeq data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            final double var = variance(data, mean);
            return i -> var < SMALL ? 0 : autoCovariance(data, mean, i) / var;
        } else {
            final double var = varianceNoMissing(data, mean);
            return i -> var < SMALL ? 0 : autoCovarianceNoMissing(data, mean, i) / var;
        }
    }

    public IntToDoubleFunction autoCovarianceFunction(DoubleSeq data, double mean) {
        if (data.anyMatch(x -> !Double.isFinite(x))) {
            return i -> autoCovariance(data, mean, i);
        } else {
            return i -> autoCovarianceNoMissing(data, mean, i);
        }
    }

    /**
     * Computes the auto-covariance of a sample from a population with known
     * mean.=sum(lag:n-1)((x(t)-mu)(x(t-lag)-mu))/n
     *
     *
     * @param data The sample
     * @param mean Mean of the population
     * @param lag
     * @return
     */
    public double autoCovarianceNoMissing(DoubleSeq data, double mean, int lag) {
        int n = data.length() - lag;
        if (n <= 0) {
            return 0;
        }
        double v = 0;
        DoubleSeqCursor xr = data.cursor();
        DoubleSeqCursor yr = data.cursor();
        yr.moveTo(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
            v += (xcur - mean) * (ycur - mean);
        }
        return v / data.length();
    }

    /**
     * Computes the variance of a sample from a population with known mean
     *
     * =sum(0:n-1)((x(t)-mu)^2)/n
     *
     * @param data The sample
     * @param mean Mean of the population
     * @return
     */
    public double varianceNoMissing(DoubleSeq data, double mean) {
        int n = data.length();
        if (n == 0) {
            return 0;
        }
        double v = 0;
        DoubleSeqCursor xr = data.cursor();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            v += (xcur - mean) * (xcur - mean);
        }
        return v / n;
    }

    /**
     * Same as varianceNoMissing. The data can contain missing values
     *
     * @param data
     * @param mean
     * @return
     */
    public double variance(DoubleSeq data, double mean) {
        double v = 0;
        int n = data.length();
        int nm = 0;
        DoubleSeqCursor xr = data.cursor();
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            if (Double.isFinite(xcur)) {
                v += (xcur - mean) * (xcur - mean);
            } else {
                ++nm;
            }
        }
        int m = data.length() - nm;
        if (m == 0) {
            return 0;
        } else {
            return v / m;
        }
    }

    public double autoCovariance(DoubleSeq data, double mean, int lag) {
        double v = 0;
        int n = data.length() - lag;
        int nm = 0;
        DoubleSeqCursor xr = data.cursor();
        DoubleSeqCursor yr = data.cursor();
        yr.moveTo(lag);
        for (int j = 0; j < n; ++j) {
            double xcur = xr.getAndNext();
            double ycur = yr.getAndNext();
            if (Double.isFinite(xcur) && Double.isFinite(ycur)) {
                v += (xcur - mean) * (ycur - mean);
            } else {
                ++nm;
            }
        }
        int m = data.length() - nm;
        if (m == 0) {
            return 0;
        } else {
            return v / m;
        }
    }

    public double[] partialAutoCorrelations(IntToDoubleFunction acfn, int kmax) {
        double[] pac = new double[kmax];
        double[] tmp = new double[kmax];
        double[] coeff = new double[kmax];
        double[] ac = new double[kmax];
        for (int i = 0; i < kmax; ++i) {
            ac[i] = acfn.applyAsDouble(i + 1);
        }

        pac[0] = coeff[0] = ac[0]; // K = 1
        for (int K = 2; K <= kmax; ++K) {
            double n = 0, d = 0;
            for (int k = 1; k <= K - 1; ++k) {
                double x = coeff[k - 1];
                n += ac[K - k - 1] * x;
                d += ac[k - 1] * x;
            }
            pac[K - 1] = coeff[K - 1] = (ac[K - 1] - n) / (1 - d);

            for (int i = 0; i < K; ++i) {
                tmp[i] = coeff[i];
            }
            for (int j = 1; j <= K - 1; ++j) {
                coeff[j - 1] = tmp[j - 1] - tmp[K - 1] * tmp[K - j - 1];
            }
        }
        return pac;
    }

}
