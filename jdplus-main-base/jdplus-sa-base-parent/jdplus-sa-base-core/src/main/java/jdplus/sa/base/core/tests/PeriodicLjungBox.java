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
package jdplus.sa.base.core.tests;

import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.api.stats.AutoCovariances;
import jdplus.toolkit.base.api.stats.StatException;
import jdplus.toolkit.base.api.stats.TestType;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.ar.AutoRegressiveEstimation;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class PeriodicLjungBox {

    private int[] lags;
    private int nhp;
    private int sign;

    private final IntToDoubleFunction autoCorrelations;
    private final int n;

    public PeriodicLjungBox(DoubleSeq sample, int nar) {
        if (nar > 0) {
            AutoRegressiveEstimation burg = AutoRegressiveEstimation.burg();
            burg.estimate(sample, nar);
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(burg.residuals(), 0);
        } else {
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(sample, 0);
        }
        this.n = sample.length();
    }

    /**
     *
     * @param nhp
     * @return
     */
    public PeriodicLjungBox hyperParametersCount(int nhp) {
        this.nhp = nhp;
        return this;
    }

    public PeriodicLjungBox useNegativeAutocorrelations() {
        sign = -1;
        return this;
    }

    public PeriodicLjungBox usePositiveAutocorrelations() {
        sign = 1;
        return this;
    }

    public PeriodicLjungBox useAllAutocorrelations() {
        sign = 0;
        return this;
    }

    public PeriodicLjungBox lags(final int[] value) {
        lags = value;
        return this;
    }

    public PeriodicLjungBox lags(final double period, final int nperiods) {
        lags = new int[nperiods];
        for (int i = 1; i <= nperiods; ++i) {
            double ip = period * i + .5;
            lags[i - 1] = (int) ip;
        }
        return this;
    }

    public int[] getLags() {
        return lags;
    }

    public StatisticalTest build() {
        if (lags == null) {
            throw new StatException("Invalid lags in LjungBox test");
        }

        double res = 0.0;
        for (int i = 0; i < lags.length; i++) {
            double ai = autoCorrelations.applyAsDouble(lags[i]);
            if (sign == 0 || (sign == 1 && ai > 0) || (sign == -1 && ai < 0)) {
                res += ai * ai / (n - lags[i]);
            }
        }
        double val = res * n * (n + 2);
        Chi2 chi = new Chi2(lags.length > nhp ? lags.length : lags.length - nhp);
        return TestsUtility.testOf(val, chi, TestType.Upper);
    }
}
