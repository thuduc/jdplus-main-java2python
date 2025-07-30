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
package jdplus.toolkit.base.core.ucarima.estimation;

import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import internal.toolkit.base.core.arima.AnsleyFilter;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.RationalFunction;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;


/**
 * Estimation of the components of an UCARIMA model using the formulae proposed by McElroy.
 * <br><i>See McElroy T.S.(2008), Matrix formulae for non stationary ARIMA Signal Extraction, 
 <a href="http://www.census.gov/ts/papers/matform3.pdf"> http://www.census.gov/ts/papers/matform3.pdf
 */
@Development(status = Development.Status.Alpha)
public class McElroyEstimates {

    private UcarimaModel ucm_;
    private double[] data_;
    // (LL')=M
    // M^-1 * K'K =F
    private FastMatrix[] M_, F_, L_, K_, D_;
    private double[][] cmps_, fcmps_;
    private int nf_;
    private ArmaFilter[] filters_;

    private void clear() {
        M_ = null;
        F_ = null;
        L_ = null;
        K_ = null;
        cmps_ = null;
        filters_ = null;
        // forecasts
        D_ = null;
        fcmps_ = null;
    }

    /**
     *
     * @return
     */
    public UcarimaModel getUcarimaModel() {
        return ucm_;
    }

    public void setUcarimaModel(UcarimaModel ucm) {
        ucm_ = ucm;
        clear();
    }

    public void setData(DoubleSeq data) {
        data_=data.toArray();
        clear();
    }

    public void setData(double[] data) {
        data_ = data;
        clear();
    }

    public double[] getData() {
        return data_;
    }

    public int getForecastsCount() {
        return nf_;
    }

    public void setForecastsCount(int nf) {
        if (nf != nf_) {
            nf_ = nf;
            D_ = null;
            fcmps_ = null;
        }
    }

    public double[] getComponent(int cmp) {
        calc(cmp);
        return cmps_[cmp];
    }

    public double[] getForecasts(int cmp) {
        fcalc(cmp);
        return fcmps_[cmp];
    }

    public double[] getForecasts() {
        int n = ucm_.getComponentsCount();
        fcalc(n);
        return fcmps_[n];
    }

    public double[] stdevForecasts() {
        int n = ucm_.getComponentsCount();
        return stdevForecasts(n);
    }

    public double[] stdevForecasts(int cmp) {
        fcalc(cmp);
        FastMatrix m = D_[cmp];
        DataBlock var = m.diagonal();
        double[] e = new double[var.length()];
        var.copyTo(e, 0);
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i]);
        }
        return e;
    }

    public double[] stdevEstimates(final int cmp) {
        FastMatrix m = M(cmp);
        DataBlock var = m.diagonal();
        double[] e = new double[var.length()];
        var.copyTo(e, 0);
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i]);
        }
        return e;
    }

    public FastMatrix M(final int cmp) {
        calc(cmp);
        if (M_[cmp] == null) {
            FastMatrix L = L_[cmp];
            if (L == null) {
                return null;
            }
            // M = (L*L')^-1 or LL'M = I 
            // L X = I
            // X = L' M or M L = X'
            FastMatrix I = FastMatrix.identity(L.getColumnsCount());
            LowerTriangularMatrix.solveXL(L, I);
            LowerTriangularMatrix.solveLX(L, I);
            M_[cmp] = I;
        }
        return M_[cmp];
    }

    public FastMatrix F(final int cmp) {
        calc(cmp);
        if (F_[cmp] == null) {
            FastMatrix L = L_[cmp];
            FastMatrix K = K_[cmp];
            if (L == null || K == null) {
                return null;
            }
            // F = (LL')^-1 * K'K = L'^-1*L^-1*K'K

            // compute K'K
            FastMatrix KK = SymmetricMatrix.XtX(K);
            // compute X=L^-1*K'K
            // LX = K'K 
            LowerTriangularMatrix.solveLX(L, KK);
            // compute L'^-1 * X = F or L'F = X 
            LowerTriangularMatrix.solveLtX(L, KK);

            F_[cmp] = KK;

        }
        return F_[cmp];
    }

    private void calc(int cmp) {
        if (data_ == null || ucm_ == null) {
            return;
        }
        if (M_ == null) {
            int ncmps = ucm_.getComponentsCount();
            K_ = new FastMatrix[ncmps];
            L_ = new FastMatrix[ncmps];
            M_ = new FastMatrix[ncmps];
            F_ = new FastMatrix[ncmps];
            cmps_ = new double[ncmps][];
            filters_ = new ArmaFilter[ncmps + 1];
        } else if (cmps_[cmp] != null) {
            return;
        }
        // actual computation.
        ArimaModel signal = ucm_.getComponent(cmp);
        if (signal.isNull()) {
            return;
        }
        ArimaModel noise = ucm_.getComplement(cmp);

        // differencing matrices
        int n = data_.length;
        StationaryTransformation<ArimaModel> stS = signal.stationaryTransformation();
        StationaryTransformation<ArimaModel> stN = noise.stationaryTransformation();

        Polynomial ds = stS.getUnitRoots().asPolynomial();
        Polynomial dn = stN.getUnitRoots().asPolynomial();

        FastMatrix DS = FastMatrix.make(n - ds.degree(), n);
        FastMatrix DN = FastMatrix.make(n - dn.degree(), n);

        double[] c = ds.toArray();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DS.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }
        c = dn.toArray();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DN.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }

        AnsleyFilter S = new AnsleyFilter();
        S.prepare(stS.getStationaryModel(), n - ds.degree());
        filters_[cmp] = S;
        AnsleyFilter N = new AnsleyFilter();
        N.prepare(stN.getStationaryModel(), n - dn.degree());

        FastMatrix Q = FastMatrix.make(n, 2 * n - ds.degree() - dn.degree());
        for (int i = 0; i < n; ++i) {
            S.apply(DS.column(i), Q.row(n - i - 1).range(0, n - ds.degree()));
        }
        for (int i = 0; i < n; ++i) {
            N.apply(DN.column(i), Q.row(n - i - 1).drop(n - ds.degree(), 0));
        }
        K_[cmp] = Q.extract(0, n, n - ds.degree(), Q.getColumnsCount()-n + ds.degree()).deepClone();
        DataBlock yd = DataBlock.make(n - dn.degree());
        noise.getNonStationaryAr().apply(DataBlock.of(data_), yd);
        DataBlock yl = DataBlock.make(yd.length());
        N.apply(yd, yl);
        // compute K'n x yl. Don't forget: Q is arranged in reverse order !
        // should be improved to take into account the structure of K
        double[] z = new double[n];
        for (int i = 0, j = n - 1; i < n; ++i, --j) {
            z[i] = Q.row(j).drop(n - ds.degree(), 0).dot(yl);
        }
        // triangularize by means of Givens rotations
        ElementaryTransformations.fastGivensTriangularize(Q);
        FastMatrix L = Q.extract(0, n, 0, n).deepClone();
        LowerTriangularMatrix.solveLx(L, DataBlock.of(z));
        LowerTriangularMatrix.solvexL(L, DataBlock.of(z));
        L_[cmp] = L;
        cmps_[cmp] = z;
        // computes M^-1, 
        // F= M^-1, 
    }

    private ArmaFilter seriesFilter() {
        int n = data_.length;
        IArimaModel model = ucm_.getModel();
        StationaryTransformation stm = model.stationaryTransformation();
        AnsleyFilter F = new AnsleyFilter();
        F.prepare((IArimaModel) stm.getStationaryModel(), n - stm.getUnitRoots().getDegree());
        return F;
    }

    private void fcalc(int cmp) {
        int ncmps = ucm_.getComponentsCount();
        boolean fs = cmp == ncmps;
        if (!fs) {
            calc(cmp);
        }
        if (fcmps_ == null) {
            fcmps_ = new double[ncmps + 1][];
            D_ = new FastMatrix[ncmps + 1];
        } else if (fcmps_[cmp] != null) {
            return;
        }
        // computes D
        // actual computation.
        if (fs) {
            if (filters_ == null) {
                filters_ = new ArmaFilter[ncmps + 1];
            }
            if (filters_[cmp] == null) {
                filters_[cmp] = seriesFilter();
            }
        }
        int n = data_.length;
        IArimaModel signal = fs ? ucm_.getModel() : ucm_.getComponent(cmp);
        if (signal.isNull()) {
            return;
        }
        StationaryTransformation stS = signal.stationaryTransformation();

        Polynomial ds = stS.getUnitRoots().asPolynomial();

        FastMatrix DS = FastMatrix.make(n - ds.degree(), n);

        double[] c = ds.toArray();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DS.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }

        FastMatrix Q = FastMatrix.make(n - ds.degree(), n);
        for (int i = 0; i < n; ++i) {
            filters_[cmp].apply(DS.column(i), Q.column(i));
        }
        FastMatrix U = FastMatrix.make(n - ds.degree(), nf_);
        double[] acf = stS.getStationaryModel().getAutoCovarianceFunction().values(n - ds.degree() + nf_);
        for (int i = 0; i < nf_; ++i) {
            U.column(i).reverse().copyFrom(acf, i + 1);
        }
        FastMatrix V = FastMatrix.make(nf_, n - ds.degree());
        for (int i = 0; i < nf_; ++i) {
            if (!U.column(i).allMatch(x->Math.abs(x)<1.e-6)) {
                filters_[cmp].apply(U.column(i), V.row(i));
            }
        }
        FastMatrix W = GeneralMatrix.AB(V, Q);
        FastMatrix D;
        if (ds.degree() > 0) {
            D = FastMatrix.make(ds.degree() + nf_, n);

            D.subDiagonal(n - ds.degree()).set(1);
            D.extract(ds.degree(), D.getRowsCount(), 0, n).copy(W);
            FastMatrix S = FastMatrix.make(ds.degree() + nf_, ds.degree() + nf_);
            S.diagonal().set(1);
            for (int i = 1; i <= ds.degree(); ++i) {
                S.subDiagonal(-i).drop(ds.degree() - i, 0).set(ds.get(i));
            }
            LowerTriangularMatrix.solveLX(S, D);
            D = D.extract(ds.degree(), D.getRowsCount()-ds.degree(), 0, n).deepClone();
        } else {
            D = W;
        }
        DataBlock f = DataBlock.make(nf_);
        double[] data = fs ? data_ : getComponent(cmp);
        f.product(D.rowsIterator(), DataBlock.of(data));
        fcmps_[cmp] = f.getStorage();
        FastMatrix G = SymmetricMatrix.XXt(V);
        G.chs();
        G.diagonal().add(acf[0]);
        for (int i = 1; i < nf_; ++i) {
            G.subDiagonal(i).add(acf[i]);
            G.subDiagonal(-i).add(acf[i]);
        }

        if (ds.degree() > 0) {
            FastMatrix B = FastMatrix.square(nf_);
            RationalFunction rfe = RationalFunction.of(Polynomial.ONE, ds);
            double[] coeff = rfe.coefficients(nf_);
            for (int i = 0; i < nf_; ++i) {
                B.subDiagonal(-i).set(coeff[i]);
            }
            G = SymmetricMatrix.XSXt(G, B);
        }
        if (!fs) {
            FastMatrix m = M(cmp);
            m = SymmetricMatrix.XSXt(m, D);
            G.add(m);
        }
        D_[cmp] = G;
    }
}
