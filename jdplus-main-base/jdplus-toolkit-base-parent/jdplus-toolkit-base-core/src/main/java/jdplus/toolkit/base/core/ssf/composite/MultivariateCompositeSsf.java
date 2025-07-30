/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.composite;

import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfErrors;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfMeasurements;
import jdplus.toolkit.base.core.ssf.multivariate.MultivariateSsf;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.basic.Measurements;
import jdplus.toolkit.base.core.ssf.basic.MeasurementsError;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;

/**
 *
 * @author palatej
 */
public class MultivariateCompositeSsf extends MultivariateSsf {

    private final int[] pos;
    private int[] dim;

    private MultivariateCompositeSsf(int[] pos, int[] dim, ISsfInitialization initializer, ISsfDynamics dynamics, ISsfMeasurements measurements) {
        super(initializer, dynamics, measurements);
        this.pos = pos;
        this.dim = dim;
    }

    public ISsf asSsf() {
//        ISsfMeasurements ms = measurements();
//        if (ms.getCount() == 1) {
//            ISsfLoading loading = ms.loading(0);
//            ISsfError err = null;
//            ISsfErrors errors = ms.errors();
//            if (errors != null) {
//                FastMatrix tmp = FastMatrix.make(1, 1);
//                if (errors.isTimeInvariant()) {
//                    errors.H(0, tmp);
//                    err = MeasurementError.of(tmp.get(0, 0));
//                } else {
//                    err = MeasurementError.of(
//                            i -> {
//                                errors.H(i, tmp);
//                                return tmp.get(0, 0);
//                            });
//                }
//            }
//            return new CompositeSsf(pos, dim, this.initialization(), this.dynamics(), new Measurement(loading, err));
//        } else {
            return M2uAdapter.of(this);
//        }
    }

    public int[] componentsPosition() {
        return pos.clone();
    }

    public int[] componentsDimension() {
        return dim.clone();
    }

    public static Builder builder() {
        return new Builder();
    }

    @lombok.Value
    public static class Item {

        private String component;
        private double coefficient;
        private ISsfLoading loading;

        public Item(String component, double coefficient, ISsfLoading loading) {
            this.component = component;
            this.coefficient = coefficient;
            this.loading = loading;
        }
    }

    @lombok.Value
    public static class Equation {

        private final List<Item> items = new ArrayList<>();
        private double measurementError;

        public void add(Item item) {
            this.items.add(item);
        }
    }

    public static class Builder {

        private final List<StateComponent> components = new ArrayList<>();
        private final List<ISsfLoading> defLoadings = new ArrayList<>();
        private final List<String> names = new ArrayList<>();
        private final List<Equation> equations = new ArrayList<>();
        private ISsfErrors measurementsError;

        public Builder add(String name, StateComponent cmp, ISsfLoading loading) {
            components.add(new StateComponent(cmp.initialization(), cmp.dynamics()));
            defLoadings.add(loading);
            names.add(name);
            return this;
        }

        public Builder add(Equation equation) {
            equations.add(equation);
            return this;
        }

        public Builder measurementError(ISsfErrors measurementsError) {
            this.measurementsError = measurementsError;
            return this;
        }

        public MultivariateCompositeSsf build() {
            if (components.isEmpty() || equations.isEmpty()) {
                return null;
            }
            // build dim / pos
            int n = components.size();
            int neq = equations.size();
            int[] dim = new int[n];
            int[] pos = new int[n];
            ISsfInitialization[] i = new ISsfInitialization[n];
            ISsfDynamics[] d = new ISsfDynamics[n];
            int cpos = 0;
            for (int j = 0; j < n; ++j) {
                StateComponent cur = components.get(j);
                i[j] = cur.initialization();
                d[j] = cur.dynamics();
                pos[j] = cpos;
                dim[j] = i[j].getStateDim();
                cpos += dim[j];
            }
            ISsfErrors errors = measurementsError;
            if (errors == null) {
                errors = MeasurementsError.of(DoubleSeq.onMapping(neq, k -> equations.get(k).getMeasurementError()));
            }
            // creates the equations
            ISsfLoading[] loadings = new ISsfLoading[neq];
            for (int j = 0; j < neq; ++j) {
                loadings[j] = Loading.optimize(loadingOf(equations.get(j), pos, dim), cpos);
            }

            return new MultivariateCompositeSsf(pos, dim, new CompositeInitialization(dim, i),
                    new CompositeDynamics(dim, d),
                    Measurements.of(loadings, errors));
        }

        private ISsfLoading loadingOf(Equation eq, int[] pos, int[] dim) {
            int[] c = cmpOf(eq);
            int[] npos = new int[c.length];
            int[] ndim = new int[c.length];
            ISsfLoading[] loadings = new ISsfLoading[c.length];
            for (int j = 0; j < c.length; ++j) {
                Item item = eq.items.get(j);
                ISsfLoading curloading = item.loading;
                if (curloading == null) // use default loading
                {
                    curloading = defLoadings.get(c[j]);
                }
                loadings[j] = Loading.rescale(curloading, item.coefficient);
                npos[j] = pos[c[j]];
                ndim[j] = dim[c[j]];
            }
            return new ComplexLoading(npos, ndim, loadings);
        }

        private int[] cmpOf(Equation eq) {
            int[] c = new int[eq.items.size()];
            for (int i = 0; i < c.length; ++i) {
                c[i] = cmp(eq.items.get(i).component);
            }
            return c;
        }

        private int cmp(String name) {
            int c = names.indexOf(name);
            if (c < 0) {
                throw new SsfException(SsfException.MODEL);
            }
            return c;
        }
    }

}
