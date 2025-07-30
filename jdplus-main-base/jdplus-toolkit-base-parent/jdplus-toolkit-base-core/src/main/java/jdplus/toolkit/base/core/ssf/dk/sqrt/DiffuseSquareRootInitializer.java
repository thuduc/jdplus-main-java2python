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
package jdplus.toolkit.base.core.ssf.dk.sqrt;

import jdplus.toolkit.base.api.math.Constants;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.ssf.akf.AugmentedState;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.dk.DiffuseUpdateInformation;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 * Mixed algorithm based on the diffuse initializer copyOf Durbin-Koopman and on
 * the (square root) array filter copyOf Kailath for the diffuse part. That
 * solution provides a much more stable estimate copyOf the diffuse part.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DiffuseSquareRootInitializer implements OrdinaryFilter.Initializer {

    public interface Transformation {

        void transform(DataBlock row, FastMatrix A);
    }

    private Transformation fn = (DataBlock row, FastMatrix A) -> ElementaryTransformations.fastRowGivens(row, A);
    private final IDiffuseSquareRootFilteringResults results;
    private AugmentedState astate;
    private DiffuseUpdateInformation updateInfo;
    private ISsf ssf;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private ISsfData data;
    private int t, endpos;
    private DataBlock Z;
    private double scale;

    public DiffuseSquareRootInitializer() {
        this.results = null;
    }

    /**
     *
     * @param results
     */
    public DiffuseSquareRootInitializer(IDiffuseSquareRootFilteringResults results) {
        this.results = results;
    }

    /**
     * @return the fn
     */
    public Transformation getTransformation() {
        return fn;
    }

    /**
     * @param fn the fn to set
     */
    public void setTransformation(Transformation fn) {
        this.fn = fn;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param state
     * @return
     */
    @Override
    public int initializeFilter(final State state, final ISsf ssf, final ISsfData data) {
        if (!ssf.initialization().isDiffuse()) {
            ssf.initialization().a0(state.a());
            ssf.initialization().Pf0(state.P());
            return 0;
        }
        this.ssf = ssf;
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        this.data = data;
        this.scale = data.scale();
        t = 0;
        int end = data.length();
        if (!initState()) {
            return -1;
        }
        while (t < end) {
            // astate contains a(t|t-1), P(t|t-1)
            if (results != null) {
                results.save(t, astate, StateInfo.Forecast);
            }
            if (error(t)) {
                // pe contains e(t), f(t), C(t), Ci(t)
                if (results != null) {
                    results.save(t, updateInfo);
                }
                update();
            } else if (results != null) {
                results.save(t, updateInfo);
            }
            if (results != null) {
                results.save(t, astate, StateInfo.Concurrent);
            }
            if (!astate.isDiffuse()) {
                break;
            }
            // astate contains now a(t+1|t), P(t+1|t), B(t+1)
            astate.next(t++, dynamics);
        }
        if (t < end) {
            state.P().copy(this.astate.P());
            state.a().copy(this.astate.a());
            state.next(t++, dynamics);
        } else {
            throw new SsfException("Diffuse initialization failed");
        }

        if (results != null) {
            results.close(t);
        }
        endpos = t;
        return t;
    }

    public int getEndDiffusePos() {
        return endpos;
    }

    private boolean initState() {
        ISsfInitialization initialization = ssf.initialization();
        int r = initialization.getStateDim();
        astate = AugmentedState.of(ssf);
        if (astate == null) {
            return false;
        }
        updateInfo = new DiffuseUpdateInformation(r);
        Z = DataBlock.make(astate.getDiffuseDim());
        initialization.diffuseConstraints(constraints());
        return true;
    }

    /**
     * Computes P(t|t), a(t|t)
     */
    private void update() {
        if (updateInfo.isDiffuse()) {
            update1();
        } else {
            update0();
        }
    }

    private void update0() {
        double f = updateInfo.getVariance();
        if (f != 0) {
            double e = updateInfo.get();
            DataBlock C = updateInfo.M();
            FastMatrix P = astate.P();
            P.addXaXt(-1 / f, C);
            // state
            // a0 = a0 + f1*Mi*v0.
            double c = e / f;
            astate.a().addAY(c, C);
        }
    }

    private void update1() {
        double f = updateInfo.getVariance();
//        if (f == 0) {
//            return;
//        }
        double fi = updateInfo.getDiffuseVariance(), e = updateInfo.get();
        DataBlock C = updateInfo.M(), Ci = updateInfo.Mi();
        // P = T P T' - 1/f*(TMf)(TMf)'+RQR'+f*(TMf/f-TMi/fi)(TMf/f-TMi/fi)'
        if (f != 0) {
            astate.P().addXaXt(-1 / f, C);
            DataBlock tmp = DataBlock.of(C);
            tmp.addAY(-f / fi, Ci);
            astate.P().addXaXt(1 / f, tmp);
        }

        // a0 = a0 + f1*Mi*v0. Reuse Mf as temporary buffer
        astate.a().addAY(e / fi, Ci);
    }

    /**
     * Computes e(t)=y(t)-Z(t)a(t|t-1) f(t)=Z(t)P(t|t-1)Z'(t)+h(t)
     * C(t)=Z(t)P(t|t-1) Ci(t) by array algorithm
     *
     * @return true if y non missing
     */
    private boolean error(int t) {
        // calc f and fi
        // fi = Z Pi Z' , f = Z P Z' + H
        preArray();
        DataBlock z = zconstraints();
        double fi = z.ssq();
        if (fi < State.ZERO) {
            fi = 0;
        }
        updateInfo.setDiffuseVariance(fi);

        double f = loading.ZVZ(t, astate.P());
        if (error != null) {
            f += error.at(t);
        }
        if (Math.abs(f) < State.ZERO) {
            f = 0;
        }

        updateInfo.setVariance(f);
        double y = data.get(t);
        if (Double.isNaN(y)) {
            updateInfo.setMissing();
            return false;
        } else {
            double e = y - loading.ZX(t, astate.a());
            if (Math.abs(e) < scale * State.ZERO) {
                e = 0;
            }
            if (fi == 0 && f == 0 && e != 0) {
                throw new SsfException(SsfException.INCONSISTENT);
            }
            updateInfo.set(e, data.isConstraint(t));
        }

        DataBlock C = updateInfo.M();
        loading.ZM(t, astate.P(), C);
        if (updateInfo.isDiffuse()) {
            FastMatrix B = constraints();
            fn.transform(z, B);
            updateInfo.Mi().setAY(z.get(0), B.column(0));
            updateInfo.Mi().apply(x -> Math.abs(x) < State.ZERO ? 0 : x);
            // move right
            astate.dropDiffuseConstraint();
        }
        return true;
    }

    // Array routines
    //     |R Z*X|
    // X = |     | 
    //     |0   X|
    // XX' = |RR'+ZXX'Z' ZXX'| = |AA'     AB'|
    //       |XX'Z'      XX' | = |BA' BB'+CC'|
    // A = Fi^1/2
    // B = Ci * Fi^-1/2
    // C = X(t+1)
    private void preArray() {
        DataBlock zconstraints = zconstraints();
        zconstraints.set(0);
        FastMatrix A = constraints();
        loading.ZM(t, A, zconstraints);
        //dynamics.TM(pos, A);
    }

    private FastMatrix constraints() {
        return astate.B();
    }

    private DataBlock zconstraints() {
        return Z.range(0, astate.getDiffuseDim());
    }

//    private void checkDiffuse() {
//        SubMatrix C = constraints();
//        for (int c = ndiffuse_ - 1; c >= 0; --c) {
//            if (C.column(c).nrm2() < State.ZERO) {
//                --ndiffuse_;
//            } else {
//                break;
//            }
//        }
//    }
}
