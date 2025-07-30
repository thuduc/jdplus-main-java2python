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
package jdplus.toolkit.base.core.stats;

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import jdplus.toolkit.base.api.math.Constants;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable(lazy = true)
public final class DescriptiveStatistics {

    public static boolean isSmall(double val) {
        return Math.abs(val) < Constants.getEpsilon();
    }

    private final double[] data;
    private final double sx, sxx;
    private final int nmissings;
    private final double skewness, kurtosis;
    private volatile double[] obs, sortedObs;

    public static DescriptiveStatistics of(DoubleSeq data) {
        return new DescriptiveStatistics(data.toArray());
    }

    public static DescriptiveStatistics ofInternal(double[] data) {
        return new DescriptiveStatistics(data);
    }

    private double[] obs() {
        double[] tmp = this.obs;
        if (tmp == null) {
            synchronized (this) {
                tmp = this.obs;
                if (tmp == null) {
                    if (nmissings == 0) {
                        tmp = data;
                    } else {
                        tmp = new double[data.length - nmissings];
                        for (int i = 0, j = 0; i < data.length; ++i) {
                            double x = data[i];
                            if (Double.isFinite(x)) {
                                tmp[j++] = x;
                            }
                        }
                    }
                    this.obs = tmp;
                }
            }
        }
        return obs;
    }

    private double[] sortObs() {
        double[] tmp = this.sortedObs;
        if (tmp == null) {
            double[] cobs = obs();
            synchronized (this) {
                tmp = this.sortedObs;
                if (tmp == null) {
                    tmp = cobs.clone();
                    Arrays.sort(tmp);
                    sortedObs = tmp;
                }
            }
        }
        return sortedObs;
    }

    private DescriptiveStatistics(double[] data) {
        this.data = data;
        double s_x = 0, s_xx = 0;
        int nm = 0;
        int n = data.length;
        for (int i = 0; i < n; i++) {
            double v = data[i];
            if (!Double.isFinite(v)) {
                ++nm;
            } else {
                s_x += v;
                s_xx += v * v;
            }
        }
        sx = s_x;
        sxx = s_xx;
        nmissings = nm;
        int nc = n - nmissings;
        double mu = sx / nc;
        double sxxc = 0;
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if (Double.isFinite(v)) {
                double e = v - mu;
                sxxc += e * e;
            }
        }
        double v = sxxc / nc, stdev = Math.sqrt(v);
        // skweness...
        double skew = 0.0;
        double kurt = 0.0;
        double stdev3 = v * stdev;
        for (int i = 0; i < n; i++) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                cur -= mu;
                double m3 = cur * cur * cur;
                skew += m3;
                kurt += m3 * cur;
            }
        }
        skewness = skew / (stdev3 * nc);
        kurtosis = kurt / (v * v * nc);
    }

    /**
     * Counts the number of elements between two doubles
     *
     * @param a Lower bound (included)
     * @param b Upper bound (excluded)
     * @return The number of observations in [a,b[
     */
    public int countBetween(double a, double b) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            int n = tmp.length;
            if (n == 0) {
                return 0;
            }
            int i0 = 0;
            while (i0 < n && tmp[i0] < a) {
                ++i0;
            }
            int i1 = i0;
            while (i1 < n && tmp[i1] < b) {
                ++i1;
            }
            return i1 - i0;
        } else {
            int n = data.length;
            int m = 0;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v >= a && v < b) {
                    m++;
                }
            }
            return m;
        }
    }

    /**
     *
     * @return
     */
    public double getAverage() {
        return sx / (data.length - nmissings);
    }

    /**
     *
     * @return
     */
    public int getDataCount() {
        return data.length;
    }

    /**
     * @return The kurtosis of the white noise toArray
     */
    public double getKurtosis() {
        return kurtosis;
    }

    // / <Summary>
    // / A Read Only property representing the maximum value in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMax() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[tmp.length - 1];
        } else {
            int n = data.length;
            double sent = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v > sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    // / <Summary>
    // / A Read Only property representing the median of the toArray in the
    // dataset.
    // / Defined as toArray[n/2+1] for count is odd and (toArray[n/2]+toArray[n/2+1])/2
    // fo even count
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMedian() {
        double[] tmp = sortObs();
        if (tmp.length == 0) {
            return Double.NaN;
        }
        boolean even = tmp.length % 2 == 0;
        if (even) {
            return ((tmp[tmp.length / 2 - 1] + tmp[tmp.length / 2]) / 2.0);
        } else {
            return tmp[tmp.length / 2];
        }
    }

    // / <Summary>
    // / A Read Only property representing the minimum value in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getMin() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0];
        } else {
            int n = data.length;
            double sent = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = data[i];
                if (Double.isFinite(v) && v < sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    /**
     *
     * @return
     */
    public int getMissingValuesCount() {
        return nmissings;
    }

    /**
     *
     * @return
     */
    public int getObservationsCount() {
        return data.length - nmissings;
    }

    /**
     *
     * @return
     */
    public double getSkewness() {
        return skewness;
    }

    /**
     *
     * @return
     */
    public double getStdev() {
        return Math.sqrt(getVar());
    }

    /**
     *
     * @param df
     * @return
     */
    public double getStdevDF(int df) {
        return Math.sqrt(getVarDF(df));
    }

    // / <Summary>
    // / A Read Only property representing the sum of the values in the dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getSum() {
        return sx;
    }

    // / <Summary>
    // / A Read Only property representing the sum of the squared values in the
    // dataset.
    // / </Summary>
    /**
     *
     * @return
     */
    public double getSumSquare() {
        return sxx;
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     *
     * @return
     */
    public double getVar() {
        return getVarDF(0);
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     *
     * @param df
     * @return
     */
    public double getVarDF(int df) {
        int n = data.length - nmissings;
        // Could lead to numerical instabilities
        //return (sxx - sx * sx / n) / (n - df);
        double sxxc = 0;
        double m = sx / n;
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if (Double.isFinite(v)) {
                double e = v - m;
                sxxc += e * e;
            }
        }
        return sxxc / (n - df);
    }

    /**
     * Root mean square error
     *
     * @return
     */
    public double getRmse() {
        int n = data.length - nmissings;
        return Math.sqrt(sxx / n);
    }

    // / <Summary>
    // / A Read Only property that returns true if at least one element in the
    // set is equal to zero
    // / </Summary>
    /**
     *
     * @return
     */
    public boolean hasZeroes() {
        double[] tmp = this.sortedObs;
        if (tmp != null && tmp.length > 0 && (tmp[0] > 0 || tmp[tmp.length - 1] < 0)) {
            return false;
        }
        return !allObservationsMatch(x -> x != 0);
    }

    /**
     *
     * @return
     */
    public boolean isConstant() {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            if (tmp.length == 0) {
                return true;
            }
            return tmp[0] == tmp[tmp.length - 1];
        } else {
            double sent = Double.NaN;
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                if (Double.isFinite(v)) {
                    if (Double.isNaN(sent)) {
                        sent = v;
                    } else if (v != sent) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public boolean isGreater(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp.length == 0 ? true : tmp[0] > val;
        } else {
            return allObservationsMatch(x -> x > val);
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public boolean isGreaterOrEqual(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp.length == 0 ? true : tmp[0] >= val;
        } else {
            return allObservationsMatch(x -> x >= val);
        }
    }

    /**
     *
     * @return
     */
    public boolean isNegative() {
        return isSmaller(0.0);
    }

    /**
     *
     * @return
     */
    public boolean isNegativeOrNull() {
        return isSmallerOrEqual(0.0);
    }

    /**
     *
     * @return
     */
    public boolean isPositive() {

        return isGreater(0.0);
    }

    /**
     *
     * @return
     */
    public boolean isPositiveOrNull() {
        return isGreaterOrEqual(0.0);
    }

    /**
     *
     * @param val
     * @return
     */
    public boolean isSmaller(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp.length == 0 ? true : tmp[tmp.length - 1] < val;
        } else {
            return allObservationsMatch(x -> x < val);
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public boolean isSmallerOrEqual(double val) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp.length == 0 ? true : tmp[tmp.length - 1] <= val;
        } else {
            return allObservationsMatch(x -> x <= val);
        }
    }

    /**
     *
     * @param eps
     * @return
     */
    public boolean isZero(double eps) {
        double[] tmp = this.sortedObs;
        if (tmp != null) {
            return tmp[0] >= -eps && tmp[tmp.length - 1] <= eps;
        } else {
            return allObservationsMatch(x -> Math.abs(x) <= eps);
        }
    }

    public boolean allObservationsMatch(DoublePredicate condition) {
        double[] tmp = this.obs;
        if (tmp != null) {
            for (int i = 0; i < tmp.length; ++i) {
                if (!condition.test(tmp[i])) {
                    return false;
                }
            }
            return true;
        } else {
            for (int i = 0; i < data.length; ++i) {
                double x = data[i];
                if (Double.isFinite(x) && !condition.test(x)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     *
     * @param partitions
     * @return
     */
    public double[] quantiles(int partitions) {
        double[] tmp = sortObs();
        int n = tmp.length;
        if (n == 0) {
            return null;
        }

        if (partitions < 2 || n / partitions < 1) {
            return null;
        }

        int np = partitions - 1;
        double[] res = new double[np];

        int ns = ((n - 1) / partitions);

        if (ns * partitions == n - 1) // pas d'arrondis
        {
            for (int i = 0; i < np; i++) {
                res[i] = tmp[(i + 1) * ns];
            }
        } else // quelques complications. On fait la moyenne pondérée entre les
        // deux valeurs les plus proches du point de partition
        {
            double ds = ((double) (n - 1)) / partitions;
            for (int i = 0; i < np; i++) {
                double dindex = (i + 1) * ds;
                int lo = (int) dindex;
                double dlo = dindex - lo;
                res[i] = tmp[lo] * (1 - dlo) + tmp[lo + 1] * dlo;
            }
        }

        return res;
    }

    /**
     *
     * @return
     */
    public DoubleSeq sortedObservations() {
        return DoubleSeq.of(sortObs());
    }

    /**
     *
     * @return
     */
    public DoubleSeq observations() {
        return DoubleSeq.of(obs());
    }

    public DoubleSeq data() {
        return DoubleSeq.of(data);
    }
}
