/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or � as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.base.core.seats;

import jdplus.toolkit.base.core.arima.ArimaException;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ucarima.WienerKolmogorovEstimators;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixWindow;
import jdplus.toolkit.base.core.math.matrices.decomposition.Gauss;
import jdplus.toolkit.base.core.math.matrices.decomposition.LUDecomposition;

/**
 * Estimation of the components of an UCARIMA model using a variant of the
 * Burman's algorithm.</br>This class is based on the program SEATS+ developed
 * by Gianluca Caporello and Agustin Maravall -with programming support from
 * Domingo Perez and Roberto Lopez- at the Bank of Spain, and on the program
 * SEATS, previously developed by Victor Gomez and Agustin Maravall.<br>It
 * corresponds more especially to a modified version of the routine
 * <i>ESTBUR</i>
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class BurmanEstimates {

    private static final double[] ONE = new double[]{1};

    public static class Builder {

        private int nf, nb;
        private DoubleSeq data;
        private UcarimaModel ucm;
        private boolean bmean;
        private int mcmp;
        private double ser = 1;

        public Builder forecastsCount(int nf) {
            this.nf = nf;
            return this;
        }

        public Builder backcastsCount(int nb) {
            this.nb = nb;
            return this;
        }

        public Builder data(DoubleSeq y) {
            this.data = y;
            return this;
        }

        public Builder mean(boolean mean) {
            this.bmean = mean;
            return this;
        }

        /**
         * Index of the component associated to the mean correction
         *
         * @param cmp
         * @return
         */
        public Builder meanComponent(int cmp) {
            this.mcmp = cmp;
            return this;
        }

        public Builder innovationStdev(double ser) {
            this.ser = ser;
            return this;
        }

        public Builder ucarimaModel(UcarimaModel ucm) {
            this.ucm = ucm;
            return this;
        }

        public BurmanEstimates build() {
            return new BurmanEstimates(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private BurmanEstimates(Builder builder) {
        this.data = builder.data;
        this.bmean = builder.bmean;
        this.mcmp = builder.mcmp;
        this.ucm = builder.ucm;
        this.ser = builder.ser;
        this.nfcasts = builder.nf;
        this.nbcasts = builder.nb;

        wk = new WienerKolmogorovEstimators(ucm);
        initModel();
        extendSeries();
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            calc(i);
        }
    }

    private final int nfcasts, nbcasts;
    private final DoubleSeq data;
    private final UcarimaModel ucm;
    private final boolean bmean;
    private final int mcmp;
    private final double ser;
    private final WienerKolmogorovEstimators wk;

    private double[] ar, ma;
    private double[][] g;
    private double mean, meanc;
    private double[] z;
    private DoubleSeq[] estimates, forecasts, backcasts;
    private DoubleSeq xbcasts, xfcasts;
    private LUDecomposition lu;
    private int nf, nbc, nfc;

    private void calc(final int cmp) {
        int n = data.length();

        double fmu = 0, bmu = 0;
        double mc = 0;
        if (mcmp == cmp) {
            // special cases: 
            // 1. trend constant (no model)
            // 2. no unit root in the AR polynomial of the trend. 
            // correction when the meancorrectedcmp doesn't contain unit
            if (isTrendConstant()) {
                // The trend is the average of the series. We remove it from the series
                double c = meanc;
                estimates[cmp] = DoubleSeq.onMapping(n, i -> c);
                forecasts[cmp] = DoubleSeq.onMapping(nfcasts, i -> c);
                backcasts[cmp] = DoubleSeq.onMapping(nbcasts, i -> c);
                return;
            }
            BackFilter sur = ucm.getComponent(cmp).getNonStationaryAr();
            if (mean != 0) {
                // no unit root
                if (sur.isIdentity()) {
                    mc = meanc;
                } else {
                    bmu = mean;
                    fmu = sur.get(0) * sur.get(sur.getDegree()) < 0 ? -mean : mean;
                }
            }
        }
        if (g[cmp] == null) {
            return;
        }

        // qstar is the order of the ma polynomial
        // pstar is the order of the ar polynomial
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;
        int rstar = qstar + pstar;
        double[] gcur = this.g[cmp];
        int gstar = gcur.length - 1;
        // g* = max(p*, q*). Could it be less ?
        // Also, nf-g*=q*; nfc >= nf
        // We want to estimate x1(t) = g(F)/Q(F) z(t) in [-nbc, n + nfc[ 
        // 1. Compute w1(t) = g(F) z(t) in [-nbc, n + q*[ (nf - g* = q*)  
        // 2. Estimate  x1(t) for t in [n+q*-p*, n+ 2* q*[ . See Burman's paper
        // 3. Compute the rest by recursion

        // //////////////////////////////////
        // 1. w1(t) = g(F) z(t) in [-nbc, n + q*[ 
        // rem: z in [-nbc, n+nfc[. 
        int start = 0, end = nbc + n + qstar;
        double[] w1 = new double[end];
        for (int i = start; i < end; ++i) {
            double s = gcur[0] * z[i];
            for (int k = 1; k <= gstar; ++k) {
                s += gcur[k] * z[i + k];
            }
            w1[i] = s;
        }

        // 2. Estimate  x1(t) for t in [n + q* - p*, n + 2* q*[
        // Q(F) x1(t) = w1(t) (t in n + q* - p*, n + q*) // p equations
        // P(B) x1(t) = 0 or m (t in n+q*, n + 2 * q*)  // q equations
        double[] ww = new double[rstar];
        for (int i = 0, j = nbc + n + qstar - pstar; i < pstar; ++i, ++j) {
            ww[i] = w1[j];
        }
        for (int i = pstar; i < ww.length; ++i) {
            ww[i] = fmu / 2;
        }
        lu.solve(DataBlock.of(ww));

        double[] x1 = new double[nbc + n + nfc];
        start = nbc + n + qstar - pstar;
        // Estimated xl
        for (int i = 0, j = start; i < rstar; ++i, ++j) {
            x1[j] = ww[i];
        }
        // backward iteration:  w = (m+MA(F))x1 <-> w(t)-m = MA(f)x1 
        end = 0;
        for (int i = start - 1; i >= end; --i) {
            double s = w1[i];
            for (int k = 1; k < ma.length; ++k) {
                s -= x1[i + k] * ma[k];
            }
            x1[i] = s / ma[0];
        }
        // forward iteration
        for (int i = nbc + n + 2 * qstar; i < x1.length; ++i) {
            double s = fmu/2;
            for (int j = 1; j <= pstar; ++j) {
                s -= ar[j] * x1[i - j];
            }
            x1[i] = s;
        }

        // symmetric computation for w2 = g(B) z(t)
        // 1. w2(t) = g(B) z(t) in [-q*, n + nfc[
        // we waste some memory to align the indices
        double[] w2 = new double[n + nbc + nfc];
        start = nbc - qstar;
        end = nbc + n + nfc;
        for (int i = start; i < end; ++i) {
            double s = gcur[0] * z[i];
            for (int k = 1; k <= gstar; ++k) {
                s += gcur[k] * z[i - k];
            }
            w2[i] = s;
        }

        // 2. Estimate  x2(t) for t in [-2 * q*, p* - q*[
        // Q(F) x1(t) = w1(t) (t in n + q* - p*, n + q*) // p equations
        // P(B) x1(t) = 0 or m (t in n+q*, n + 2 * q*)  // q equations
        ww = new double[rstar];
        for (int i = 0, j = nbc + pstar - qstar; i < pstar; ++i) {
            ww[i] = w2[--j];
        }
        for (int i = pstar; i < ww.length; ++i) {
            ww[i] = bmu / 2;
        }
        lu.solve(DataBlock.of(ww));
        // ww contains estimates of the signal for t= -2q* to p*-q* (in reverse order)
        double[] x2 = new double[n + nbc + nfc];
        start = nbc + pstar - qstar;
        for (int i = 0, j = start; i < ww.length; ++i) {
            x2[--j] = ww[i];
        }

        // forward recursion: Q(B) w = x2
        for (int i = start; i < x2.length; ++i) {
            double s = w2[i];
            for (int k = 1; k < ma.length; ++k) {
                s -= x2[i - k] * ma[k];
            }
            x2[i] = s / ma[0];
        }

        // backward iteration
        for (int i = nbc - 2 * qstar-1; i >= 0; --i) {
            double s = bmu/2;
            for (int j = 1; j <= pstar; ++j) {
                s -= ar[j] * x2[i + j];
            }
            x2[i] = s;
        }
        double[] rslt = new double[n + nfc + nbc];
        // x1, x2 defined in [-2*qstar, n + 2*qstar[
        // rslt define in ]-nbc, n+nbf[
        for (int i = 0; i < x1.length; ++i) {
            rslt[i] = x1[i] + x2[i];
        }
        estimates[cmp] = DoubleSeq.of(rslt, nbc, n);
        if (mc != 0) {
            for (int i = 0; i < rslt.length; ++i) {
                rslt[i] += mc;
            }
        }
        if (nfcasts > 0) {
            forecasts[cmp] = DoubleSeq.of(rslt, n + nbc, nfcasts);
        }
        if (nbcasts > 0) {
            backcasts[cmp] = DoubleSeq.of(rslt, nbc - nbcasts, nbcasts);
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq estimates(final int cmp, final boolean signal) {

        if (signal) {
            return estimates[cmp];
        } else {
            return data.fastOp(estimates[cmp], (a, b) -> a - b);
        }
    }

    /**
     *
     */
    private void extendSeries() {

        int q = ma.length - 1, p = ar.length - 1;
        nf = q > p ? 2 * q : p + q;
        nbc = Math.max(nf, nbcasts);
        nfc = Math.max(nf, nfcasts);
        ExactArimaForecasts fcasts = new ExactArimaForecasts();
        fcasts.prepare(wk.getUcarimaModel().getModel(), bmean);
        xfcasts = fcasts.forecasts(data, nfc);
        xbcasts = fcasts.backcasts(data, nbc);
        if (bmean) {
            mean = fcasts.getMean();
        } else {
            mean = 0;
        }
        int n = data.length();
        // z is the extended series with forecasts and backcasts
        z = new double[n + nbc+nfc];
        data.copyTo(z, nbc);

        xfcasts.copyTo(z, nbc + n);
        xbcasts.copyTo(z, 0);
        meanc = correctedMean();
        if (useMean()) {
            for (int i = 0; i < z.length; ++i) {
                z[i] -= meanc;
            }
        }
    }

    private double correctedMean() {
        IArimaModel arima = model();
        return mean / arima.getStationaryAr().asPolynomial().evaluateAt(1);
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq forecasts(final int cmp, final boolean signal) {
        if (signal) {
            return forecasts[cmp];
        } else {
            DoubleSeq xf = xfcasts.range(0, nfcasts);
            return forecasts[cmp].fastOp(xf, (a, b) -> a - b);
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq backcasts(final int cmp, final boolean signal) {
        if (signal) {
            return backcasts[cmp];
        } else {
            int nb = xbcasts.length();
            DoubleSeq xb = xbcasts.range(nb - nbcasts, nb);
            return backcasts[cmp].fastOp(xb, (a, b) -> a - b);
        }
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesBackcasts() {
        return xbcasts.drop(xbcasts.length() - nbcasts, 0);
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesForecasts() {
        return xfcasts.range(0, nfcasts);
    }

    /**
     *
     */
    private void initModel() {
        // cfr burman-wilson algorithm
        IArimaModel model = ucm.getModel();
        int ncmps = ucm.getComponentsCount();
        estimates = new DoubleSeq[ncmps];
        forecasts = new DoubleSeq[ncmps];
        backcasts = new DoubleSeq[ncmps];
        g = new double[ncmps][];

        Polynomial pma = model.getMa().asPolynomial();
        double v = model.getInnovationVariance();
        if (v != 1) {
            pma = pma.times(Math.sqrt(v));
        }
        Polynomial par = model.getAr().asPolynomial();
        for (int i = 0; i < ncmps; ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            if (!cmp.isNull()) {
                SymmetricFilter sma = cmp.symmetricMa();
                if (!sma.isNull()) {
                    BackFilter umar = model.getNonStationaryAr(), ucar = cmp.getNonStationaryAr();
                    BackFilter nar = umar.divide(ucar);
                    BackFilter smar = model.getStationaryAr(), scar = cmp.getStationaryAr();
                    BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
                    if (smp.simplify(smar, scar)) {
                        smar = smp.getLeft();
                        scar = smp.getRight();
                    }

                    BackFilter dar = scar;
                    nar = nar.times(smar);

                    BackFilter denom = new BackFilter(pma).times(dar);
                    SymmetricFilter c = sma.times(SymmetricFilter.convolutionOf(nar));
                    double mvar = model.getInnovationVariance();
                    if (mvar != 1) {
                        c = c.times(1 / mvar);
                    }
                    BackFilter gf = c.decompose(denom);
                    g[i] = gf.asPolynomial().toArray();
                } else {
                    g[i] = ONE;
                }
            }
        }
        ma = pma.toArray();
        ar = par.toArray();
        initSolver();
    }

    private boolean isTrendConstant() {
        return wk.getUcarimaModel().getComponent(mcmp).isNull();
    }

    private boolean useMean() {
        // we use the mean if there is a mean and if we don't use D1 correction
        // it happens when the model doesn't contain non stationary roots
        return bmean && model().getNonStationaryArOrder() == 0;
    }

    private IArimaModel model() {
        return wk.getUcarimaModel().getModel();
    }

    private void initSolver() {
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;

        FastMatrix M = FastMatrix.square(pstar + qstar);
        MatrixWindow top = M.top(0);
        FastMatrix M1 = top.vnext(pstar);
        for (int j = 0; j <= qstar; ++j) {
            M1.subDiagonal(j).set(ma[j]);
        }
        FastMatrix M2 = top.vnext(qstar);
        for (int j = 0; j <= pstar; ++j) {
            M2.subDiagonal(j).set(ar[pstar - j]);
        }
        lu = Gauss.decompose(M);
    }

    /**
     *
     * @return
     */
    public boolean isMeanCorrection() {
        return bmean;
    }

    /**
     *
     * @param cmp
     * @return
     */
    public DoubleSeq stdevEstimates(final int cmp) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull()) {
            return Doubles.EMPTY;
        } else {
            try {
                int n = (data.length() + 1) / 2;
                double[] err = wk.totalErrorVariance(cmp, true, 0, n);
                double[] e = new double[data.length()];
                for (int i = 0; i < err.length; ++i) {
                    double x = ser * Math.sqrt(err[i]);
                    e[i] = x;
                    e[e.length - i - 1] = x;
                }
                return DoubleSeq.of(e);
            } catch (ArimaException | MatrixException err) {
                return Doubles.EMPTY;
            }
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return null if no forecasts are requested
     */
    public DoubleSeq stdevForecasts(final int cmp, final boolean signal) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull() || nfcasts == 0) {
            return null;
        }
        try {

            double[] e = wk.totalErrorVariance(cmp, signal, -nfcasts, nfcasts);
            double[] err = new double[nfcasts];
            for (int i = 0; i < nfcasts; ++i) {
                err[i] = ser * Math.sqrt(e[nfcasts - 1 - i]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }
    }

    public DoubleSeq stdevBackcasts(final int cmp, final boolean signal) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull() || nbcasts == 0) {
            return null;
        }
        try {
            double[] e = wk.totalErrorVariance(cmp, signal, -nbcasts, nbcasts);
            double[] err = new double[nbcasts];
            for (int j = nbcasts - 1; j >= 0; --j) {
                err[j] = ser * Math.sqrt(e[j]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }

    }
}
