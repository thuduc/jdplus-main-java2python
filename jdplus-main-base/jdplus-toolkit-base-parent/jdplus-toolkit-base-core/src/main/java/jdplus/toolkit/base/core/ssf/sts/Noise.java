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

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Loading;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Noise {
    
    public int dim(){
        return 1;
    }

    public StateComponent of(final double var) {
        return new StateComponent(new Initialization(var), new Dynamics(var));
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public ISsfLoading periodicLoading(final int period, final int startPos) {
        return Loading.circular(period, startPos);
    }

    static class Initialization implements ISsfInitialization {

        private final double var;

        Initialization(final double var) {
            this.var = var;
        }

        @Override
        public int getStateDim() {
            return 1;
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
            pf0.set(0, 0, var);
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final double var, e;

        Dynamics(final double var) {
            this.var = var;
            this.e = Math.sqrt(var);
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, var);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(0, 0, e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.set(0);
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
            v.set(0, 0, 0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, e * u.get(0));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.set(0, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(0) * e);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

}
