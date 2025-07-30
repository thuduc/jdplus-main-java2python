/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.arima;

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataWindow;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.RationalFunction;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.ckms.CkmsDiffuseInitializer;
import jdplus.toolkit.base.core.ssf.ckms.CkmsFilter;
import jdplus.toolkit.base.core.ssf.ckms.CkmsState;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfArima {
    
    public int dim(IArimaModel arima){
       return Math.max(arima.getArOrder(), arima.getMaOrder() + 1);
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }
    
    public StateComponent differencingSsf(int d, double var){
        if (d<=0)
            return null;
        if (d == 1)
            return Rw.of(var, false);
        ArimaModel model=new ArimaModel(BackFilter.ONE, new BackFilter(UnitRoots.D(1, d)), BackFilter.ONE, var);
        return ofNonStationary(model);
    }

    public StateComponent stateComponent(IArimaModel arima) {
        if (arima.isStationary()) {
            return ofStationary(arima);
        } else {
            return ofNonStationary(arima);
        }
    }

    public Ssf ssf(IArimaModel arima) {
        return Ssf.of(stateComponent(arima), defaultLoading());
    }

    public CkmsFilter.IFastFilterInitializer fastInitializer(IArimaModel arima) {
        return (CkmsState state, UpdateInformation upd, ISsf ssf, ISsfData data) -> {
            if (arima.isStationary()) {
                return stInitialize(state, upd, arima, ssf, data);
            } else {
                return dInitialize(state, upd, arima, ssf, data);
            }
        };
    }

    private int stInitialize(CkmsState state, UpdateInformation upd, IArimaModel arima, ISsf ssf, ISsfData data) {
        int n = ssf.getStateDim();
        double[] values = arima.getAutoCovarianceFunction().values(n);
        DataBlock M = upd.M(), L = state.l();
        upd.M().copyFrom(values, 0);
        L.copy(M);
        ssf.dynamics().TX(0, L);
        upd.setVariance(values[0]);
        return 0;
    }

    private int dInitialize(CkmsState state, UpdateInformation upd, IArimaModel arima, ISsf ssf, ISsfData data) {
        return new CkmsDiffuseInitializer(diffuseInitializer(arima)).initializeFilter(state, upd, ssf, data);
    }

    private OrdinaryFilter.Initializer diffuseInitializer(IArimaModel arima) {
        return (State state, ISsf ssf, ISsfData data) -> {
            ArimaInitialization initialization = (ArimaInitialization) ssf.initialization();
            int nr = ssf.getStateDim(), nd = initialization.getDiffuseDim();
            FastMatrix A = FastMatrix.make(nr + nd, nd);
            double[] dif = arima.getNonStationaryAr().asPolynomial().toArray();
            for (int j = 0; j < nd; ++j) {
                A.set(j, j, 1);
                for (int i = nd; i < nd + nr; ++i) {
                    double c = 0;
                    for (int k = 1; k <= nd; ++k) {
                        c -= dif[k] * A.get(i - k, j);
                    }
                    A.set(i, j, c);
                }
            }

            for (int i = 0; i < nr; ++i) {
                double c = 0;
                for (int j = 0; j < nd; ++j) {
                    c += A.get(i + nd, j) * data.get(j);
                }
                state.a().set(i, c);
            }
            FastMatrix stV = FastMatrix.square(nr);
            ArimaInitialization.stVar(stV, initialization.stpsi, initialization.stacgf, initialization.data.var);
            FastMatrix K = FastMatrix.square(nr);
            ArimaInitialization.sigma(K, initialization.dif);
            SymmetricMatrix.XSXt(stV, K, state.P());
            return nd;
        };
    }

    private StateComponent ofStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ArmaInitialization initialization = new ArmaInitialization(arima);
        ISsfDynamics dynamics = new ArimaDynamics(initialization.data);
        return new StateComponent(initialization, dynamics);
    }

    private static StateComponent ofNonStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ArimaInitialization initialization = new ArimaInitialization(arima);
        ISsfDynamics dynamics = new ArimaDynamics(initialization.data);
        return new StateComponent(initialization, dynamics);
    }

    static class ArmaInitialization implements ISsfInitialization {

        final ArimaData data;
        private final DataBlock acgf;
        private final FastMatrix P0;

        ArmaInitialization(IArimaModel arima) {
            data = new ArimaData(arima);
            acgf = DataBlock.of(arima.getAutoCovarianceFunction().values(data.dim));
            P0 = p0(data.var, acgf, data.psi);
        }

        static FastMatrix v(double var, DataBlock psi) {
            FastMatrix v = SymmetricMatrix.xxt(psi);
            v.mul(var);
            return v;
        }

        private static FastMatrix p0(double var, final DataBlock acgf, final DataBlock psi) {
            int dim = acgf.length();
            FastMatrix P = FastMatrix.square(dim);
            P.column(0).copy(acgf);
            for (int j = 0; j < dim - 1; ++j) {
                double psij = psi.get(j);
                P.set(j + 1, j + 1, P.get(j, j) - psij * psij * var);
                for (int k = 0; k < j; ++k) {
                    P.set(j + 1, k + 1, P.get(j, k) - psij * psi.get(k) * var);
                }
            }
            SymmetricMatrix.fromLower(P);
            return P;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            pf0.copy(P0);
        }

        @Override
        public void Pi0(FastMatrix pi0) {
        }

        @Override
        public int getStateDim() {
            return data.dim;
        }
    }

    static class ArimaInitialization implements ISsfInitialization {

        final ArimaData data;
        final double[] dif;
        private final DataBlock stpsi, stacgf;
        private final FastMatrix P0;

        ArimaInitialization(IArimaModel arima) {
            data = new ArimaData(arima);
            //
            StationaryTransformation<IArimaModel> starima = arima.stationaryTransformation();
            dif = starima.getUnitRoots().asPolynomial().toArray();
            stacgf = DataBlock.of(starima.getStationaryModel().getAutoCovarianceFunction().values(data.dim));
            RationalFunction rf = starima.getStationaryModel().getPsiWeights().getRationalFunction();
            stpsi = DataBlock.of(rf.coefficients(data.dim));
            FastMatrix stvar = ArmaInitialization.p0(data.var, stacgf, stpsi);
            FastMatrix L = FastMatrix.square(data.dim);
            sigma(L, dif);
            P0 = SymmetricMatrix.XSXt(stvar, L);

        }

        /**
         * Computes B =
         *
         * @param b B
         * @param d The coefficients of the differencing polynomial
         */
        static void B0(final FastMatrix b, final double[] d) {
            int nd = d.length - 1;
            if (nd == 0) {
                return;
            }
            int nr = b.getRowsCount();
            b.diagonal().set(1);
            if (nd == nr) {
                return;
            }

            DataBlock D = DataBlock.of(d, d.length - 1, 0, -1);
            for (int i = 0; i < nd; ++i) {
                DataBlock C = b.column(i);
                DataWindow R = C.window(0, nd);
                C.set(nd, -R.get().dot(D));
                for (int k = nd + 1; k < nr; ++k) {
                    C.set(k, -R.move(1).dot(D));
                }
            }
        }

        /**
         *
         * @param X
         * @param dif
         */
        static void sigma(final FastMatrix X, final double[] dif) {
            int n = X.getRowsCount();
            double[] lambda = RationalFunction.of(Polynomial.ONE, Polynomial.of(dif)).coefficients(n);

            for (int j = 0; j < n; ++j) {
                for (int k = 0; k <= j; ++k) {
                    X.set(j, k, lambda[j - k]);
                }
            }
        }

        /**
         *
         * @param stV
         * @param stpsi
         * @param stacgf
         * @param var
         */
        static void stVar(final FastMatrix stV, final DataBlock stpsi,
                final DataBlock stacgf, final double var) {
            int n = stV.getRowsCount();

            stV.column(0).copy(stacgf);

            for (int j = 0; j < n - 1; ++j) {
                double stpsij = stpsi.get(j);
                stV.set(j + 1, j + 1, stV.get(j, j) - stpsij * stpsij * var);
                for (int k = 0; k < j; ++k) {
                    stV.set(j + 1, k + 1, stV.get(j, k) - stpsij * stpsi.get(k) * var);
                }
            }

            SymmetricMatrix.fromLower(stV);
        }

        @Override
        public boolean isDiffuse() {
            return dif.length > 1;
        }

        @Override
        public int getDiffuseDim() {
            return dif.length - 1;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            int d = dif.length - 1;
            if (d == 0) {
                return;
            }
            B0(b, dif);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            pf0.copy(P0);
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            FastMatrix B = FastMatrix.make(data.dim, dif.length - 1);
            B0(B, dif);
            SymmetricMatrix.XXt(B, pi0);
        }

        @Override
        public int getStateDim() {
            return data.dim;
        }
    }

    static class ArimaData {

        final int dim;
        final double var, se;
        final double[] phi;
        final DataBlock psi;

        ArimaData(IArimaModel arima) {
            var = arima.getInnovationVariance();
            Polynomial ar = arima.getAr().asPolynomial();
            Polynomial ma = arima.getMa().asPolynomial();
            phi = ar.toArray();
            dim = Math.max(ar.degree(), ma.degree() + 1);
            psi = DataBlock.of(RationalFunction.of(ma, ar).coefficients(dim));
            se = Math.sqrt(var);
        }

    }

    static class ArimaDynamics implements ISsfDynamics {

        private final ArimaData data;
        private final DataBlock z;
        private final FastMatrix V;

        public ArimaDynamics(ArimaData data) {
            this.data = data;
            z = DataBlock.make(data.dim);
            V = ArmaInitialization.v(data.var, data.psi);
        }

        /**
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(final int pos, final FastMatrix tr) {
            T(tr);
        }

        /**
         *
         * @param tr
         */
        public void T(final FastMatrix tr) {
            tr.set(0);
            for (int i = 1; i < data.dim; ++i) {
                tr.set(i - 1, i, 1);
            }
            for (int i = 1; i < data.phi.length; ++i) {
                tr.set(data.dim - 1, data.dim - i, -data.phi[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final FastMatrix vm) {
            if (data.phi.length == 1) {
                vm.upLeftShift(1);
                vm.column(data.dim - 1).set(0);
                vm.row(data.dim - 1).set(0);
            } else {
                z.set(0);
                DataBlockIterator cols = vm.reverseColumnsIterator();
                for (int i = 1; i < data.phi.length; ++i) {
                    z.addAY(-data.phi[i], cols.next());
                }
                TX(pos, z);
                vm.upLeftShift(1);
                vm.column(data.dim - 1).copy(z);
                vm.row(data.dim - 1).copy(z);
            }

        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            double tx = 0;
            if (data.phi.length > 1) {
                DoubleSeqCursor reader = x.reverseReader();
                for (int i = 1; i < data.phi.length; ++i) {
                    tx -= data.phi[i] * reader.getAndNext();
                }
            }
            x.bshift(1);
            x.set(data.dim - 1, tx);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            double last = -x.get(data.dim - 1);
            x.fshift(1);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 1, j = data.dim - 1; i < data.phi.length; ++i, --j) {
                    if (data.phi[i] != 0) {
                        x.add(j, last * data.phi[i]);
                    }
                }
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.copy(V);
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.column(0).copy(data.psi);
            if (data.se != 1) {
                sm.mul(data.se);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(V);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock sx) {
            double a = x.dot(data.psi) * data.se;
            sx.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            double a = u.get(0) * data.se;
            x.addAY(a, data.psi);
        }

    }

}
