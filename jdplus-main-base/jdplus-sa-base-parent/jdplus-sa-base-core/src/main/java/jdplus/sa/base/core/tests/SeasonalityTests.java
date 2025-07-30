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

import jdplus.toolkit.base.core.data.analysis.WindowFunction;
import jdplus.toolkit.base.core.data.analysis.SmoothedPeriodogram;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.modelling.DifferencingResult;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTests {

    public static final int MSHORT = 80, SHORT = 60, SPEC_LENGTH = 120;

    /**
     * This test corresponds to the OverResidSeasTest function of TRAMO
     *
     * @param res The residuals of the model
     * @param period
     * @return
     */
    public static SeasonalityTests residualSeasonalityTest(DoubleSeq res, int period) {
        SeasonalityTests tests = new SeasonalityTests();
        tests.testResiduals(res, period);
        // compute the score
        StatisticalTest qs = tests.getQs();
        if (qs != null && qs.isSignificant(0.01)) // 9.21 at the 0.01 level
        {
            tests.score++;
        }
        StatisticalTest np = tests.getNonParametricTest();
        if (np != null && np.isSignificant(0.01)) // 24.725 at the 0.01 level (freq=12)
        {
            tests.score++;
        }
        int n = tests.getDifferencing().getDifferenced().length();
        if (n >= MSHORT || (period < 12 && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                tests.score++;
            }
        }
        return tests;
    }

    /**
     * @param s The original series
     * @param period The tested periodicity
     * @param diff The differencing order (-1 if it is automatically detected)
     * @param mean Mean correction of the differenced series
     * @param all Executes all the tests or stop when one of them is significant
     * @return
     */
    public static SeasonalityTests seasonalityTest(DoubleSeq s, int period, int diff, boolean mean, boolean all) {
        if (period <= 1) {
            throw new TsException(TsException.INVALID_FREQ);
        }
        SeasonalityTests tests = new SeasonalityTests();
        tests.test(s, period, diff, mean);
        // compute the score
        StatisticalTest qs = tests.getQs();
        if (qs != null && qs.isSignificant(0.01)) // 9.21 at the 0.01 level
        {
            tests.score++;
            if (!all) {
                return tests;
            }
        }
        StatisticalTest np = tests.getNonParametricTest();
        if (np != null && np.isSignificant(0.01)) // 24.725 at the 0.01 level (freq=12)
        {
            tests.score++;
            if (!all) {
                return tests;
            }
        }
        int n = tests.getDifferencing().getDifferenced().length();
        if (n >= MSHORT || (period < 12 && n >= SHORT)) {
            if (SpectralPeaks.hasSeasonalPeaks(tests.getSpectralPeaks())) {
                tests.score++;
                if (!all) {
                    return tests;
                }
            }
        }
        return tests;
    }

    /**
     *
     * @param input Original series
     * @param period
     * @param ndiff Differencing order
     * @param mean Mean correction (after differencing)
     */
    public void test(DoubleSeq input, int period, int ndiff, boolean mean) {
        delta = DifferencingResult.of(input, period, ndiff, mean);
        this.period = period;
        clear();
    }

    private void testResiduals(DoubleSeq res, int period) {
        delta = DifferencingResult.of(res, period, 0, false);
        this.period = period;
        clear();
    }

    private DifferencingResult delta;
    private SmoothedPeriodogram btSpectrum;
    private TukeySpectrumPeaksTest tpeaks;
    private AutoRegressiveSpectrumTest arpeaks;
    private SpectralPeaks[] peaks;
    private StatisticalTest nptest;
    private StatisticalTest qs, periodogram;
    private int score;
    private int period;

    public DifferencingResult getDifferencing() {
        return delta;
    }

    public int getPeriod() {
        return period;
    }

    // Lazy evaulation
    public StatisticalTest getNonParametricTest() {
        if (nptest == null) {
            Friedman friedman = new Friedman(delta.getDifferenced(), period);
            nptest = friedman.build();
        }
        return nptest;
    }

    public TukeySpectrumPeaksTest getTukeyPeaks() {
        if (tpeaks == null) {
            tpeaks = new TukeySpectrumPeaksTest();
            if (!tpeaks.test(delta.getDifferenced(), period)) {
                tpeaks = null;
            }
        }
        return tpeaks;
    }

    public SmoothedPeriodogram getSmoothedPeriodogram() {
        if (btSpectrum == null) {
            DoubleSeq d = delta.getDifferenced();
            int n = d.length();
            int wlen = 3 * n / 4 / period;
            if (wlen > 11) {
                wlen = 11;
            }
            btSpectrum = SmoothedPeriodogram.builder()
                    .data(d)
                    .windowFunction(WindowFunction.Tukey)
                    .windowLength(wlen * period)
                    .build();

        }
        return btSpectrum;
    }

    public AutoRegressiveSpectrumTest getArPeaks() {
        if (arpeaks == null) {
            arpeaks = new AutoRegressiveSpectrumTest();
            DoubleSeq dlast = delta.getDifferenced();
//            if (dlast.getLength()> SPEC_LENGTH){
//                dlast=dlast.drop(dlast.getLength()-SPEC_LENGTH, 0);
//            }
            if (!arpeaks.test(dlast, period)) {
                arpeaks = null;
            }
        }
        return arpeaks;
    }

    public SpectralPeaks[] getSpectralPeaks() {
        if (peaks == null) {
            AutoRegressiveSpectrumTest arPeaks = getArPeaks();
            TukeySpectrumPeaksTest tPeaks = getTukeyPeaks();
            if (arPeaks == null || tPeaks == null) {
                return null;
            }
            int[] a = arPeaks.seasonalPeaks(.90, .99);
            int[] t = tPeaks.seasonalPeaks(.90, .99);
            peaks = new SpectralPeaks[period / 2];
            for (int i = 0; i < peaks.length; ++i) {
                SpectralPeaks.AR ar = SpectralPeaks.AR.none;
                SpectralPeaks.Tukey tu = SpectralPeaks.Tukey.none;
                if (a != null) {
                    ar = SpectralPeaks.AR.fromInt(a[i]);
                }
                if (t != null) {
                    tu = SpectralPeaks.Tukey.fromInt(t[i]);
                }
                peaks[i] = new SpectralPeaks(ar, tu);
            }
        }
        return peaks;
    }

    public StatisticalTest getQs() {
        if (qs == null) {
            qs = new Qs(delta.getDifferenced(), period)
                    .autoCorrelationsCount(2)
                    .build();
        }
        return qs;
    }

    public StatisticalTest getPeriodogramTest() {
        if (periodogram == null) {
            PeriodogramTest test = new PeriodogramTest(delta.getDifferenced(), period);
            periodogram = test.buildF();
        }
        return periodogram;
    }

    public int getScore() {
        return score;
    }

    private void clear() {
        nptest = null;
        arpeaks = null;
        tpeaks = null;
        qs = null;
        periodogram = null;
        score = 0;
    }
}
