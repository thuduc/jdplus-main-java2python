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
package jdplus.toolkit.base.core.stats.tests;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.dstats.T;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 * Test the mean of a sample. H0: mean(sample) == mean(population), H1:
 * mean(sample) != mean(population)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class SampleMean {

    public static final double SMALL = 1e-38;

    private final double sampleMean;
    private final int sampleSize;
    private double populationMean, populationVariance;
    private int sampleSizeForVariance;
    private boolean normalPopulation;

    public SampleMean(final double sampleMean, final int sampleSize) {
        this.sampleMean = sampleMean;
        this.sampleSize = sampleSize;
    }

    public SampleMean populationMean(double value) {
        this.populationMean = value;
        return this;
    }

    public SampleMean populationVariance(double value) {
        this.populationVariance = value;
        this.sampleSizeForVariance = 0;
        return this;
    }

    public SampleMean normalDistribution(boolean value) {
        this.normalPopulation = value;
        return this;
    }

    /**
     *
     * @param value
     * @param sampleSize The sample size is the size of the sample for
     * estimating the variance, which could be different of the size used for
     * the mean
     *
     * @return
     */
    public SampleMean estimatedPopulationVariance(double value, int sampleSize) {
        this.populationVariance = value;
        this.sampleSizeForVariance = sampleSize;
        return this;
    }

    /**
     * @return Normal distribution if the variance is known, T(n-1) if the
     * variance is estimated using the sample.
     */
    public StatisticalTest build() {
        if (this.populationVariance == 0) {
            throw new java.lang.IllegalStateException("undefined population variance");
        }
        double val = (sampleMean - populationMean) / Math.sqrt(populationVariance / sampleSize);
        if (sampleSizeForVariance > 0) {
            return TestsUtility.testOf(val, new T(sampleSizeForVariance - 1), TestType.TwoSided);
        } else {
            return TestsUtility.testOf(val, new Normal(), TestType.TwoSided);
        }
    }

}
