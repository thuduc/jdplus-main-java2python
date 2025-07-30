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
package jdplus.toolkit.base.core.ssf.sts;

import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalComponent {

    private FastMatrix tsvar(int freq) {
        int n = freq - 1;
        FastMatrix M = FastMatrix.make(n, freq);
        M.diagonal().set(1);
        M.column(n).set(-1);
        FastMatrix O = SymmetricMatrix.XXt(M);
        LinearSystemSolver.robustSolver().solve(O, M);
        FastMatrix H = FastMatrix.make(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }
        FastMatrix QH = GeneralMatrix.AB(M, H);
        FastMatrix Z = SymmetricMatrix.XXt(QH);
        Z.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);

        return Z;
    }

    /**
     *
     * @param freq
     * @return
     */
    private synchronized FastMatrix tsVar(int freq) {
        switch (freq) {
            case 12:
                if (VTS12 == null) {
                    VTS12 = tsvar(12);
                }
                return VTS12.deepClone();
            case 4:
                if (VTS4 == null) {
                    VTS4 = tsvar(4);
                }
                return VTS4.deepClone();
            case 2:
                if (VTS2 == null) {
                    VTS2 = tsvar(2);
                }
                return VTS2.deepClone();
            case 3:
                if (VTS3 == null) {
                    VTS3 = tsvar(3);
                }
                return VTS3.deepClone();
            case 6:
                if (VTS6 == null) {
                    VTS6 = tsvar(6);
                }
                return VTS6.deepClone();
            default:
                return tsvar(freq);
        }
    }

    private FastMatrix hsvar(int freq) {
        FastMatrix m = FastMatrix.square(freq - 1);
        m.set(-1.0 / freq);
        m.diagonal().add(1);
        return m;
    }

    private synchronized FastMatrix hslVar(int freq) {
        switch (freq) {
            case 12:
                if (LHS12 == null) {
                    LHS12 = hsvar(12);
                    SymmetricMatrix.lcholesky(LHS12);
                }
                return LHS12.deepClone();
            case 4:
                if (LHS4 == null) {
                    LHS4 = hsvar(4);
                    SymmetricMatrix.lcholesky(LHS4);
                }
                return LHS4.deepClone();
            case 2:
                if (LHS2 == null) {
                    LHS2 = hsvar(2);
                    SymmetricMatrix.lcholesky(LHS2);
                }
                return LHS2.deepClone();
            case 3:
                if (LHS3 == null) {
                    LHS3 = hsvar(3);
                    SymmetricMatrix.lcholesky(LHS3);
                }
                return LHS3.deepClone();
            case 6:
                if (LHS6 == null) {
                    LHS6 = hsvar(6);
                    SymmetricMatrix.lcholesky(LHS6);
                }
                return LHS6.deepClone();
            default:
                FastMatrix lhs = hsvar(freq);
                SymmetricMatrix.lcholesky(lhs);
                return lhs;
        }
    }

    public FastMatrix tsVar(SeasonalModel seasModel, final int freq) {
        if (seasModel == SeasonalModel.Trigonometric) {
            return tsVar(freq);
        } else {
            int n = freq - 1;
            FastMatrix Q = FastMatrix.square(n);
            if (null != seasModel) // Dummy
            {
                switch (seasModel) {
                    case Dummy:
                        Q.set(n - 1, n - 1, 1);
                        break;
                    case Crude:
                        Q.set(1);
                        //Q.set(0, 0, freq * var);
                        break;
                    case HarrisonStevens:
                        double v = 1.0 / freq;
                        Q.set(-v);
                        Q.diagonal().add(1);
                        break;
                    default:
                        break;
                }
            }
            return Q;
        }
    }

    private synchronized FastMatrix tslVar(int freq) {
        switch (freq) {
            case 12:
                if (LVTS12 == null) {
                    LVTS12 = tsvar(12);
                    SymmetricMatrix.lcholesky(LVTS12);
                    LVTS12.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS12.deepClone();
            case 4:
                if (LVTS4 == null) {
                    LVTS4 = tsvar(4);
                    LVTS4.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                    SymmetricMatrix.lcholesky(LVTS4);
                }
                return LVTS4.deepClone();
            case 2:
                if (LVTS2 == null) {
                    LVTS2 = tsvar(2);
                    LVTS2.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                    SymmetricMatrix.lcholesky(LVTS2);
                }
                return LVTS2.deepClone();
            case 3:
                if (LVTS3 == null) {
                    LVTS3 = tsvar(3);
                    SymmetricMatrix.lcholesky(LVTS3);
                    LVTS3.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS3.deepClone();
            case 6:
                if (LVTS6 == null) {
                    LVTS6 = tsvar(6);
                    SymmetricMatrix.lcholesky(LVTS6);
                    LVTS6.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS6.deepClone();
            default:
                FastMatrix var = tsvar(freq);
                SymmetricMatrix.lcholesky(var);
                var.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                return var;
        }
    }

    public FastMatrix tslVar(SeasonalModel seasModel, final int freq) {
        switch (seasModel) {
            case Trigonometric:
                return tslVar(freq);
            case HarrisonStevens:
                return hslVar(freq);
            default:
                int n = freq - 1;
                FastMatrix Q = FastMatrix.square(n);
                switch (seasModel) {
                    case Dummy:
                        Q.set(n - 1, n - 1, 1);
                        break;
                    case Crude:
                        Q.set(1);
                        //Q.set(0, 0, freq * var);
                        break;
                    default:
                        break;
                }
                return Q;
        }
    }

    private static FastMatrix VTS2, VTS3, VTS4, VTS6, VTS12;
    private static FastMatrix LVTS2, LVTS3, LVTS4, LVTS6, LVTS12, LHS2, LHS3, LHS4, LHS6, LHS12;

    public StateComponent of(final SeasonalModel model, final int period, final double seasVar) {
        SeasonalModel cmodel = seasVar == 0 ? SeasonalModel.Fixed : model;
        Data data = new Data(cmodel, seasVar, period);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public StateComponent harrisonStevens(final int period, final double v) {
        HarrisonStevensData data = new HarrisonStevensData(period, v);
        return new StateComponent(new HarrisonStevensInitialization(data),
                new HarrisonStevensDynamics(data));
    }

    public ISsfLoading harrisonStevensLoading(final int period) {
        return Loading.circular(period);
    }

    public StateComponent harrisonStevens(final double[] var) {
        HarrisonStevensData data = new HarrisonStevensData(var);
        return new StateComponent(new HarrisonStevensInitialization(data),
                new HarrisonStevensDynamics(data));
    }

    static class Data {

        private final SeasonalModel seasModel;
        private final double seasVar;
        private final int period;
        private final FastMatrix tsvar, lvar;

        Data(final SeasonalModel model, final double seasVar, final int period) {
            this.seasVar = seasVar;
            this.seasModel = model;
            this.period = period;
            if (seasVar > 0) {
                tsvar = tsVar(seasModel, period);
                tsvar.mul(seasVar);
                if (model != SeasonalModel.Crude && model != SeasonalModel.Dummy) {
                    lvar = tslVar(seasModel, period);
                    lvar.mul(std());
                } else {
                    lvar = null;
                }
            } else {
                tsvar = null;
                lvar = null;
            }
        }

        final double std() {
            if (seasVar == 0 || seasVar == 1) {
                return seasVar;
            } else {
                return Math.sqrt(seasVar);
            }
        }
    }

    static class Initialization implements ISsfInitialization {

        private final Data data;

        public Initialization(final Data data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return data.period - 1;
        }

        @Override
        public boolean isDiffuse() {
            return data.seasVar >= 0;
        }

        @Override
        public int getDiffuseDim() {
            return data.period - 1;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(FastMatrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix p) {
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    p.set(0, 0, data.seasVar);
                } else {
                    p.copy(data.tsvar);
                }
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data data;

        public Dynamics(final Data data) {
            this.data = data;
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
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy
                        || data.seasModel == SeasonalModel.Crude) {
                    return 1;
                } else {
                    return data.period - 1;
                }
            } else {
                return 0;
            }
        }

        @Override
        public void V(int pos, FastMatrix v) {
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    v.set(0, 0, data.seasVar);
                } else {
                    v.copy(data.tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.seasModel != SeasonalModel.Fixed;
        }

        @Override
        public void S(int pos, FastMatrix s) {
            if (null != data.seasModel) {
                switch (data.seasModel) {
                    case Crude:
                        s.set(data.std());
                        break;
                    case Dummy:
                        s.set(data.period - 2, data.period - 2, data.std());
                        break;
                    default:
                        s.copy(data.lvar);
                        break;
                }
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            switch (data.seasModel) {
                case Crude:
                    x.add(data.std() * u.get(0));
                    break;
                case Dummy:
                    x.add(0, data.std() * u.get(0));
                    break;
                default:
                    x.addProduct(data.lvar.rowsIterator(), u);
                    break;
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            switch (data.seasModel) {
                case Crude:
                    xs.set(0, data.std() * x.sum());
                    break;
                case Dummy:
                    xs.set(0, data.std() * x.get(data.period - 2));
                    break;
                default:
                    xs.product(x, data.lvar.columnsIterator());
                    break;
            }
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            if (data.seasVar >= 0) {
                tr.row(data.period - 2).set(-1);
                tr.subDiagonal(1).set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.fshiftAndNegSum();
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int imax = data.period - 2;
            double xs = x.get(0);
            for (int i = 0; i < imax; ++i) {
                x.set(i, x.get(i + 1) - xs);
            }
            x.set(imax, -xs);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            switch (data.seasModel) {
                case Fixed:
                    return;
                case Dummy:
                    p.add(data.period - 2, data.period - 2, data.seasVar);
                    break;
                case Crude:
                    p.add(data.seasVar);
                    break;
                default:
                    p.add(data.tsvar);
                    break;
            }
        }
    }

    public static class HarrisonStevensData {

        private final int period;
        private final double[] var;
        private final FastMatrix V;

        public HarrisonStevensData(final int period, final double v) {
            this.period = period;
            var = null;
            V = FastMatrix.square(period - 1);
            V.set(-1.0 / period);
            V.diagonal().add(1);
            V.mul(v);
        }

        public HarrisonStevensData(final double[] var) {
            period = var.length;
            this.var = var.clone();
            DataBlock xvar = DataBlock.of(var);
            V = FastMatrix.square(period - 1);
            double mvar = xvar.sum() / (period * period);
            double dp = 2.0 / period;
            for (int i = 0; i < period - 1; ++i) {
                V.set(i, i, var[i] * (1 - dp) + mvar);
                for (int j = 0; j < i; ++j) {
                    V.set(i, j, mvar - (var[i] + var[j]) / period);
                }
            }
            SymmetricMatrix.fromLower(V);
        }

        public double[] getVariances() {
            return var;
        }
    }

    public static class HarrisonStevensInitialization implements ISsfInitialization {

        private final HarrisonStevensData data;

        public HarrisonStevensInitialization(final HarrisonStevensData data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return data.period - 1;
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return data.period - 1;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix p) {
            if (data.V != null) {
                p.copy(data.V);
            }
        }

    }

    public static class HarrisonStevensDynamics implements ISsfDynamics {

        private final HarrisonStevensData data;

        public HarrisonStevensDynamics(final HarrisonStevensData data) {
            this.data = data;
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
            return data.period - 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            if (data.V != null) {
                qm.copy(data.V);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.V != null;
        }

        @Override
        public void S(int pos, FastMatrix s) {
            //TODO
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            //TODO
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            //TODO
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(data.V);
        }

    }
}
