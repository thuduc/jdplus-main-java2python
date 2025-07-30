/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.basic;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class Loading {

    public static ISsfLoading optimize(ISsfLoading l, int len) {
        if (!l.isTimeInvariant()) {
            return l;
        }
        DataBlock z = DataBlock.make(len);
        l.Z(0, z);
        IntList c = new IntList();
        DoubleSeqCursor reader = z.cursor();
        double cur = 0;
        boolean same = true;
        int n0 = 0;
        for (int k = 0; k < len; ++k) {
            double x = reader.getAndNext();
            if (x == 0) {
                ++n0;
            } else if (same) {
                if (cur == 0) {
                    cur = x;
                    c.add(k);
                } else if (cur == x) {
                    c.add(k);
                } else {
                    same = false;
                }
            }
        }
        if (same == false) {
            if (n0 < 2 * len / 3) {
                return new TimeInvariantLoading(z);
            } else {
                return l;
            }
        } else if (c.size() == 1) {
            return cur == 1 ? fromPosition(c.get(0)) : from(c.get(0), cur);
        } else {
            int[] pos = c.toArray();
            return cur == 0 ? fromPositions(pos) : rescale(fromPositions(pos), cur);
        }

    }

    public static ISsfLoading fromPosition(final int mpos) {
        return mpos == 0 ? Loading1.L0 : new Loading1(mpos);
    }

    public static ISsfLoading from(final int mpos, final double b) {
        return b == 1 ? fromPosition(mpos) : new Loading1l(mpos, b);
    }

    public static ISsfLoading sum() {
        return new SumLoading();
    }

    public static ISsfLoading createPartialSum(final int n) {
        return new PartialSumLoading(n);
    }

    public static ISsfLoading fromPositions(final int[] mpos) {
        return new Loading2(mpos);
    }

    public static ISsfLoading from(final int[] mpos, final double[] w) {
        return new Loading3(mpos, w);
    }

    public static ISsfLoading circular(final int period) {
        return new CircularLoading(period, 0);
    }

    public static ISsfLoading circular(final int period, final int pstart) {
        return new CircularLoading(period, pstart);
    }

    public static ISsfLoading cyclical(final int period) {
        return new CyclicalLoading(period, 0);
    }

    public static ISsfLoading cyclical(final int period, final int pstart) {
        return new CyclicalLoading(period, pstart);
    }

    public static ISsfLoading periodic(final int period, final int start) {
        return new PeriodicLoading(period, start);
    }

    public static ISsfLoading regression(final FastMatrix X) {
        return new RegressionLoading(X);
    }

    public static ISsfLoading regression(final DoubleSeq x) {
        return new SingleRegressionLoading(x);
    }

    public static ISsfLoading rescale(ISsfLoading loading, double s) {
        if (s == 1) {
            return loading;
        } else {
            return new MLoading(loading, s);
        }
    }

    public static ISsfLoading rescale(ISsfLoading loading, double[] s) {
        if (s.length == 1) {
            return rescale(loading, s[0]);
        } else {
            return new ALoading(loading, s);
        }
    }

    private static class RegressionLoading implements ISsfLoading {

        private final FastMatrix data;

        private RegressionLoading(final FastMatrix data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.copy(data.row(pos));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            return x.dot(data.row(pos));
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return QuadraticForm.apply(V, data.row(pos));
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            V.addXaXt(d, data.row(pos));
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.addAY(d, data.row(pos));
        }

    }

    private static class SingleRegressionLoading implements ISsfLoading {

        private final double[] data;

        private SingleRegressionLoading(final DoubleSeq data) {
            this.data = data.toArray();
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        double reg(int pos) {
            return pos < data.length ? data[pos] : 0;
        }

        double reg2(int pos) {
            return pos < data.length ? data[pos]*data[pos] : 0;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(0, reg(pos));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            return x.get(0)*reg(pos);
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return V.get(0, 0)*reg2(pos);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            V.add(0,0,d*reg2(pos));
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(0, d*reg(pos));
        }

    }

    private static class SumLoading implements ISsfLoading {

        SumLoading() {
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.sum();
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.set(i->m.column(i).sum());
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return V.sum();
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            V.add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(1);
        }

    }

    private static class PartialSumLoading implements ISsfLoading {

        private final int cdim;

        PartialSumLoading(int n) {
            this.cdim = n;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.extract(0, cdim).sum();
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.set(m.row(0), m.row(1), (x, y) -> x + y);
            for (int r = 2; r < cdim; ++r) {
                zm.add(m.row(r));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return V.extract(0, cdim, 0, cdim).sum();
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            V.extract(0, cdim, 0, cdim).add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.extract(0, cdim).add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.extract(0, cdim).set(1);
        }

    }

    private static class Loading1 implements ISsfLoading {

        static Loading1 L0 = new Loading1(0);

        private final int mpos;

        Loading1(int mpos) {
            this.mpos = mpos;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.get(mpos);
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.copy(m.row(mpos));
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return V.get(mpos, mpos);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            V.add(mpos, mpos, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(mpos, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(mpos, 1);
        }

    }

    private static class Loading1l implements ISsfLoading {

        private final int mpos;
        private final double b, b2;

        Loading1l(int mpos, double b) {
            this.mpos = mpos;
            this.b = b;
            this.b2 = b * b;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return b * m.get(mpos);
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.setAY(b, m.row(mpos));

        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return b2 * V.get(mpos, mpos);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            V.add(mpos, mpos, d * b2);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(mpos, d * b);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(mpos, b);
        }

    }

    static class Loading2 implements ISsfLoading {

        private final int[] mpos;

        Loading2(int[] mpos) {
            this.mpos = mpos;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            int n = mpos.length;
            double d = m.get(mpos[0]);
            for (int i = 1; i < n; ++i) {
                d += m.get(mpos[i]);
            }
            return d;
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.copy(m.row(mpos[0]));
            for (int i = 1; i < mpos.length; ++i) {
                zm.add(m.row(mpos[i]));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            double d = 0;
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                d += V.get(mpos[i], mpos[i]);
                for (int j = 0; j < i; ++j) {
                    d += 2 * V.get(mpos[i], mpos[j]);
                }
            }
            return d;
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    V.add(mpos[i], mpos[j], d);
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                x.add(mpos[i], d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                z.set(mpos[i], 1);
            }
        }

    }

    static class Loading3 implements ISsfLoading {

        private final int[] mpos;
        private final double[] w;

        Loading3(int[] mpos, double[] w) {
            this.mpos = mpos;
            this.w = w;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            int n = mpos.length;
            double d = 0;
            for (int i = 0; i < n; ++i) {
                d += w[i] * m.get(mpos[i]);
            }
            return d;
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            zm.setAY(w[0], m.row(mpos[0]));
            for (int i = 1; i < mpos.length; ++i) {
                zm.addAY(w[i], m.row(mpos[i]));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            double d = 0;
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                double wi = w[i];
                d += V.get(mpos[i], mpos[i]) * wi * wi;
                for (int j = 0; j < i; ++j) {
                    double wj = w[j];
                    d += 2 * V.get(mpos[i], mpos[j]) * wi * wj;
                }
            }
            return d;
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                double wi = w[i];
                for (int j = 0; j < n; ++j) {
                    double wj = w[j];
                    V.add(mpos[i], mpos[j], d * wi * wj);
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                x.add(mpos[i], d * w[i]);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                z.set(mpos[i], w[i]);
            }
        }

    }

    static class CircularLoading implements ISsfLoading {

        private final int period, start;

        public CircularLoading(int period, int start) {
            this.period = period;
            this.start = start;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                z.set(-1);
            } else {
                z.set(spos, 1);
            }
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                return -x.sum();
            } else {
                return x.get(spos);
            }
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                for (int i = 0; i < x.length(); ++i) {
                    x.set(i, -m.column(i).sum());
                }
            } else {
                x.copy(m.row(spos));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                return vm.sum();
            } else {
                return vm.get(spos, spos);
            }
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                vm.add(d);
            } else {
                vm.add(spos, spos, d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                x.add(-d);
            } else {
                x.add(spos, d);
            }
        }
    }

    static class CyclicalLoading implements ISsfLoading {

        private final int period, start;

        public CyclicalLoading(int period, int start) {
            this.period = period;
            this.start = start;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int spos = (start + pos) % period;
            z.set(spos, 1);
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int spos = (start + pos) % period;
            return x.get(spos);
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            int spos = (start + pos) % period;
            x.copy(m.row(spos));
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            int spos = (start + pos) % period;
            return vm.get(spos, spos);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            vm.add(spos, spos, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            x.add(spos, d);
        }

    }

    static class PeriodicLoading implements ISsfLoading {

        private final int period, pos;

        public PeriodicLoading(int period, int pos) {
            this.period = period;
            this.pos = pos;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int t, DataBlock z) {
            if (t % period == pos) {
                z.set(0, 1);
            }
        }

        @Override
        public double ZX(int t, DataBlock x) {
            if (t % period == pos) {
                return x.get(0);
            } else {
                return 0;
            }
        }

        @Override
        public void ZM(int t, FastMatrix m, DataBlock x) {
            if (t % period == pos) {
                x.copy(m.row(0));
            } else {
                x.set(0);
            }
        }

        @Override
        public double ZVZ(int t, FastMatrix vm) {
            if (t % period == pos) {
                return vm.get(0, 0);
            } else {
                return 0;
            }
        }

        @Override
        public void VpZdZ(int t, FastMatrix vm, double d) {
            if (d == 0) {
                return;
            }
            if (t % period == pos) {
                vm.add(0, 0, d);
            }
        }

        @Override
        public void XpZd(int t, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            if (t % period == pos) {
                x.add(0, d);
            }
        }
    }

    private static class MLoading implements ISsfLoading {

        private final ISsfLoading loading;
        private final double s, s2;

        private MLoading(ISsfLoading loading, final double s) {
            this.loading = loading;
            this.s = s;
            this.s2 = s * s;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            loading.Z(pos, z);
            z.mul(s);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return s * loading.ZX(pos, m);
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return s2 * loading.ZVZ(pos, V);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            loading.VpZdZ(pos, V, d * s2);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            loading.XpZd(pos, x, d * s);
        }

        @Override
        public boolean isTimeInvariant() {
            return loading.isTimeInvariant();
        }

    }

    private static class ALoading implements ISsfLoading {

        private final ISsfLoading loading;
        private final double[] s;

        private ALoading(ISsfLoading loading, final double[] s) {
            this.loading = loading;
            this.s = s;
        }

        private double l(int pos) {
            return pos < s.length ? s[pos] : s[s.length - 1];
        }

        private double l2(int pos) {
            double z = l(pos);
            return z * z;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            loading.Z(pos, z);
            z.mul(l(pos));
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return l(pos) * loading.ZX(pos, m);
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return l2(pos) * loading.ZVZ(pos, V);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            loading.VpZdZ(pos, V, d * l2(pos));
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            loading.XpZd(pos, x, d * l(pos));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }
}
