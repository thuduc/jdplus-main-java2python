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
package jdplus.toolkit.base.core.sarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import java.util.Arrays;
import jdplus.toolkit.base.core.arima.AbstractArimaModel;
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import nbbrd.design.Immutable;

/**
 * Box-Jenkins seasonal arima model AR(B)* SAR(B)*D(B)*SD(B) y(t) =
 * MA(B)*SMA(B)e(t), e~N(0, var) AR(B) = 1+a(1)B+...+a(p)B^p, regular
 * auto-regressive polynomial SAR(B) = 1+b(1)B^s+...+b(bp)B^s*bp, seasonal
 * auto-regressive polynomial D(B) = 1+e(1)B+...+e(d)B^d, regula differencing
 * polynomial SD(B) = 1+f(1)B^s+...+f(bd)B^s*bd, seasonal differencing
 * polynomial MA(B) = 1+c(1)B+...+c(q)B^q, regular moving average polynomial
 * SMA(B) = 1+d(1)B^s+...+d(bq)B^s*bq, seasonal moving average polynomial
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class SarimaModel extends AbstractArimaModel {

    @BuilderPattern(SarimaModel.class)
    public static class Builder {

        private final int s;
        private int d, bd;
        private double[] phi, bphi, th, bth;
        private boolean adjust = false;

        public static final double AR = -.1, MA = -.2;

        private Builder(int period) {
            this.s = period;
            phi = Doubles.EMPTYARRAY;
            bphi = Doubles.EMPTYARRAY;
            th = Doubles.EMPTYARRAY;
            bth = Doubles.EMPTYARRAY;
        }

        private Builder(SarimaOrders spec) {
            s = spec.getPeriod();
            d = spec.getD();
            bd = spec.getBd();
            phi = (spec.getP() > 0) ? new double[spec.getP()] : Doubles.EMPTYARRAY;
            bphi = (spec.getBp() > 0) ? new double[spec.getBp()] : Doubles.EMPTYARRAY;
            th = (spec.getQ() > 0) ? new double[spec.getQ()] : Doubles.EMPTYARRAY;
            bth = (spec.getBq() > 0) ? new double[spec.getBq()] : Doubles.EMPTYARRAY;
        }

        private Builder(SarimaSpec spec) {
            s = spec.getPeriod();
            d = spec.getD();
            bd = spec.getBd();

            phi = Parameter.values(spec.getPhi(), AR);
            bphi = Parameter.values(spec.getBphi(), AR);
            th = Parameter.values(spec.getTheta(), MA);
            bth = Parameter.values(spec.getBtheta(), MA);
        }

        private Builder(SarmaOrders spec) {
            s = spec.getPeriod();
            d = 0;
            bd = 0;
            phi = (spec.getP() > 0) ? new double[spec.getP()] : Doubles.EMPTYARRAY;
            bphi = (spec.getBp() > 0) ? new double[spec.getBp()] : Doubles.EMPTYARRAY;
            th = (spec.getQ() > 0) ? new double[spec.getQ()] : Doubles.EMPTYARRAY;
            bth = (spec.getBq() > 0) ? new double[spec.getBq()] : Doubles.EMPTYARRAY;
        }

        private double[] clone(double[] c) {
            if (c.length == 0) {
                return Doubles.EMPTYARRAY;
            } else {
                return c.clone();
            }
        }

        private Builder(SarimaModel model) {
            s = model.s;
            d = model.d;
            bd = model.bd;
            phi = clone(model.phi);
            bphi = clone(model.bphi);
            th = clone(model.th);
            bth = clone(model.bth);
        }

        public Builder adjustOrders(boolean adjust) {
            this.adjust = adjust;
            return this;
        }

        public Builder setDefault() {
            return setDefault(-0.1, -0.2);
        }

        public Builder setDefault(double ar, double ma) {
            for (int i = 0; i < phi.length; ++i) {
                phi[i] = ar;
            }
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i] = ar;
            }
            for (int i = 0; i < th.length; ++i) {
                th[i] = ma;
            }
            for (int i = 0; i < bth.length; ++i) {
                bth[i] = ma;
            }
            return this;
        }

        public Builder parameters(DoubleSeq p) {
            DoubleSeqCursor reader = p.cursor();
            for (int i = 0; i < phi.length; ++i) {
                phi[i] = reader.getAndNext();
            }
            for (int i = 0; i < bphi.length; ++i) {
                bphi[i] = reader.getAndNext();
            }
            for (int i = 0; i < th.length; ++i) {
                th[i] = reader.getAndNext();
            }
            for (int i = 0; i < bth.length; ++i) {
                bth[i] = reader.getAndNext();
            }
            return this;
        }

        public Builder phi(int lag, double val) {
            phi[lag - 1] = val;
            return this;
        }

        public Builder bphi(int lag, double val) {
            bphi[lag - 1] = val;
            return this;
        }

        public Builder theta(int lag, double val) {
            th[lag - 1] = val;
            return this;
        }

        public Builder btheta(int lag, double val) {
            bth[lag - 1] = val;
            return this;
        }

        public Builder phi(double... val) {
            if (val == null) {
                phi = Doubles.EMPTYARRAY;
            } else if (val.length == phi.length) {
                System.arraycopy(val, 0, phi, 0, phi.length);
            } else {
                phi = val.clone();
            }
            return this;
        }

        public Builder bphi(double... val) {
            if (val == null) {
                bphi = Doubles.EMPTYARRAY;
            } else if (val.length == bphi.length) {
                System.arraycopy(val, 0, bphi, 0, bphi.length);
            } else {
                bphi = val.clone();
            }
            return this;
        }

        public Builder theta(double... val) {
            if (val == null) {
                th = Doubles.EMPTYARRAY;
            } else if (val.length == th.length) {
                System.arraycopy(val, 0, th, 0, th.length);
            } else {
                th = val.clone();
            }
            return this;
        }

        public Builder btheta(double... val) {
            if (val == null) {
                bth = Doubles.EMPTYARRAY;
            } else if (val.length == bth.length) {
                System.arraycopy(val, 0, bth, 0, bth.length);
            } else {
                bth = val.clone();
            }
            return this;
        }

        public Builder differencing(int d, int bd) {
            this.d = d;
            this.bd = bd;
            return this;
        }

        private void adjust() {
            double[] nphi = adjust(phi);
            if (nphi != null) {
                phi = nphi;
            }
            double[] nbphi = adjust(bphi);
            if (nbphi != null) {
                bphi = nbphi;
            }
            double[] nth = adjust(th);
            if (nth != null) {
                th = nth;
            }
            double[] nbth = adjust(bth);
            if (nbth != null) {
                bth = nbth;
            }
        }

        private double[] adjust(double[] p) {
            int l = p.length;
            for (int i = l - 1; i >= 0; --i) {
                if (Math.abs(p[i]) < SMALL) {
                    --l;
                } else {
                    break;
                }
            }
            if (l != p.length) {
                double[] np = new double[l];
                if (l > 0) {
                    System.arraycopy(p, 0, np, 0, l);
                }
                return np;
            } else {
                return null;
            }
        }

        public SarimaModel build() {
            if (adjust) {
                adjust();
            }
            return new SarimaModel(this);
        }
    }

    public static Builder builder(SarimaOrders spec) {
        return new Builder(spec);
    }

    public static Builder builder(SarmaOrders spec) {
        return new Builder(spec);
    }

    public static Builder builder(SarimaSpec spec) {
        return new Builder(spec);
    }

    public static Builder builder(int period) {
        return new Builder(period);
    }

    public static final double SMALL = 1e-6;

    private final int s;
    private final int d, bd;
    private final double[] phi, bphi, th, bth;

    /**
     *
     */
    private SarimaModel(Builder builder) {
        this.s = builder.s;
        this.d = builder.d;
        this.bd = builder.bd;
        this.phi = builder.phi;
        this.bphi = builder.bphi;
        this.th = builder.th;
        this.bth = builder.bth;
    }

    /**
     * Gets all the parameters in the following order:
     * phi, bphi, theta, btheta
     *
     * @return
     */
    public DoubleSeq parameters() {
        double[] p = new double[phi.length + bphi.length + th.length + bth.length];
        int pos = 0;
        if (phi.length > 0) {
            System.arraycopy(phi, 0, p, pos, phi.length);
            pos += phi.length;
        }
        if (bphi.length > 0) {
            System.arraycopy(bphi, 0, p, pos, bphi.length);
            pos += bphi.length;
        }
        if (th.length > 0) {
            System.arraycopy(th, 0, p, pos, th.length);
            pos += th.length;
        }
        if (bth.length > 0) {
            System.arraycopy(bth, 0, p, pos, bth.length);
        }
        return DoubleSeq.of(p);
    }

    /**
     *
     * @param lag
     * @return
     */
    public double phi(final int lag) {
        return phi[lag - 1];
    }

    private DoubleSeq clone(double[] x) {
        return x.length == 0 ? DoubleSeq.empty() : DoubleSeq.of(x.clone());
    }

    public DoubleSeq getPhi() {
        return clone(phi);
    }

    /**
     *
     * @param lag
     * @return
     */
    public double bphi(final int lag) {
        return bphi[lag - 1];
    }

    public DoubleSeq getBphi() {
        return clone(bphi);
    }

    /**
     *
     * @param lag
     * @return
     */
    public double theta(final int lag) {
        return th[lag - 1];
    }

    public DoubleSeq getTheta() {
        return clone(th);
    }

    /**
     *
     * @param lag
     * @return
     */
    public double btheta(final int lag) {
        return bth[lag - 1];
    }

    public DoubleSeq getBtheta() {
        return clone(bth);
    }

    @Override
    public BackFilter getAr() {
        BackFilter df = SarimaUtility.differencingFilter(s, d, bd);
        BackFilter st = getStationaryAr();
        return df.times(st);
    }

    /**
     *
     * @return
     */
    @Override
    public int getArOrder() {
        int n = d + phi.length;
        if (s != 1) {
            n += s * (bd + bphi.length);
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getDifferencingOrder() {
        int n = d;
        if (s > 1) {
            n += s * bd;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getPeriod() {
        return s;
    }

    /**
     *
     * @return
     */
    @Override
    public double getInnovationVariance() {
        return 1;
    }

    @Override
    public BackFilter getMa() {
        Polynomial pr = getRegularMA();
        Polynomial ps = seasonalMA();
        return new BackFilter(pr.times(ps, false));
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaOrder() {
        int n = th.length;
        if (s != 1) {
            n += s * bth.length;
        }
        return n;
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter getNonStationaryAr() {
        return SarimaUtility.differencingFilter(s, d, bd);
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryArOrder() {
        return getDifferencingOrder();
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return phi.length + bphi.length + th.length + bth.length;
    }

    /**
     *
     * @return
     */
    public Polynomial getRegularAR() {
        int n = phi.length;
        double[] p = new double[1 + n];
        p[0] = 1;
        if (n > 0) {
            System.arraycopy(phi, 0, p, 1, n);
        }
        return Polynomial.ofInternal(p);
    }

    /**
     *
     * @return
     */
    public int getD() {
        return d;
    }

    public int getP() {
        return phi.length;
    }

    public int getQ() {
        return th.length;
    }

    /**
     *
     * @return
     */
    public Polynomial getRegularMA() {
        int n = th.length;
        double[] p = new double[1 + n];
        p[0] = 1;
        if (n > 0) {
            System.arraycopy(th, 0, p, 1, n);
        }
        return Polynomial.ofInternal(p);
    }

    /**
     *
     * @return
     */
    public Polynomial getSeasonalAR() {
        int n = bphi.length;
        double[] p = new double[1 + n];
        p[0] = 1;
        if (n > 0) {
            System.arraycopy(bphi, 0, p, 1, n);
        }
        return Polynomial.ofInternal(p);
    }

    private Polynomial seasonalAR() {
        if (bphi.length == 0) {
            return Polynomial.ONE;
        }
        if (bphi.length == 1) {
            return Polynomial.factor(-bphi[0], s);
        } else {
            double[] p = new double[bphi.length * s + 1];
            p[0] = 1;
            for (int i = s, j = 0; i < bphi.length; i += s, ++j) {
                p[i] = bphi[j];
            }
            return Polynomial.ofInternal(p);
        }
    }

    private Polynomial seasonalMA() {
        if (bth.length == 0) {
            return Polynomial.ONE;
        }
        if (bth.length == 1) {
            return Polynomial.factor(-bth[0], s);
        } else {
            double[] p = new double[bth.length * s + 1];
            p[0] = 1;
            for (int i = s, j = 0; i < bth.length; i += s, ++j) {
                p[i] = bth[j];
            }
            return Polynomial.ofInternal(p);
        }
    }

    /**
     *
     * @return
     */
    public int getBd() {
        return bd;
    }

    public int getBp() {
        return bphi.length;
    }

    public int getBq() {
        return bth.length;
    }

    /**
     *
     * @return
     */
    public Polynomial getSeasonalMA() {
        int n = bth.length;
        double[] p = new double[1 + n];
        p[0] = 1;
        if (n > 0) {
            System.arraycopy(bth, 0, p, 1, n);
        }
        return Polynomial.ofInternal(p);
    }

    /**
     *
     * @return
     */
    public SarimaOrders orders() {
        SarimaOrders spec = new SarimaOrders(s);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(phi.length);
        spec.setBp(bphi.length);
        spec.setQ(th.length);
        spec.setBq(bth.length);
        return spec;
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter getStationaryAr() {
        Polynomial pr = getRegularAR();
        Polynomial ps = seasonalAR();
        return new BackFilter(pr.times(ps, true));
    }

    /**
     *
     * @return
     */
    @Override
    public int getStationaryArOrder() {
        int n = phi.length;
        if (s != 1) {
            n += s * bphi.length;
        }
        return n;
    }

    @Override
    public boolean isInvertible() {
        return FilterUtility.checkStability(DoubleSeq.of(th))
                && FilterUtility.checkStability(DoubleSeq.of(bth));
    }

    @Override
    public boolean isNull() {
        return false;
    }

    /**
     *
     * @param checkMA
     * @return
     */
    public boolean isStable(boolean checkMA) {
        int pos = 0;
        if (!FilterUtility.checkStability(DataBlock.of(phi))) {
            return false;
        }
        if (!FilterUtility.checkStability(DataBlock.of(bphi))) {
            return false;
        }
        if (checkMA) {
            if (!FilterUtility.checkStability(DataBlock.of(th))) {
                return false;
            }
            if (!FilterUtility.checkStability(DataBlock.of(bth))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isStationary() {
        return d == 0 && bd == 0;
    }

    /**
     *
     * @return
     */
    public boolean isWhiteNoise() {
        return getParametersCount() == 0 && d == 0 && bd == 0;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     *
     * @return
     */
    @Override
    public StationaryTransformation<SarimaModel> stationaryTransformation() {
        if (isStationary()) {
            return new StationaryTransformation(this, BackFilter.ONE);
        } else {
            BackFilter ur = getNonStationaryAr();
            Builder builder = toBuilder();
            builder.differencing(0, 0);
            return new StationaryTransformation(builder.build(), ur);
        }
    }

    public boolean isAirline(boolean seas) {
        if (seas) {
            return phi.length == 0 && bphi.length == 0 && d == 1 && bd == 1
                    && th.length == 1 && bth.length == 1;
        } else {
            return phi.length == 0 && bphi.length == 0 && bd == 0 && bth.length == 0
                    && d == 1 && th.length == 1;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SarimaModel) {
            SarimaModel m = (SarimaModel) other;
            return s == m.s && d == m.d && bd == m.bd
                    && Arrays.equals(phi, m.phi)
                    && Arrays.equals(bphi, m.bphi)
                    && Arrays.equals(th, m.th)
                    && Arrays.equals(bth, m.bth);

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.s;
        hash = 43 * hash + this.d;
        hash = 43 * hash + this.bd;
        hash = 43 * hash + Arrays.hashCode(this.phi);
        hash = 43 * hash + Arrays.hashCode(this.bphi);
        hash = 43 * hash + Arrays.hashCode(this.th);
        hash = 43 * hash + Arrays.hashCode(this.bth);
        return hash;
    }

}
