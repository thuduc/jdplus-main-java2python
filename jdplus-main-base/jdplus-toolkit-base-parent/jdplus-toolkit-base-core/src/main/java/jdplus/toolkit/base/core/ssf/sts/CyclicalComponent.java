/*
 * Copyright 2016-2017 National Bank of Belgium
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

import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CyclicalComponent {
    
    public int dim(){
        return 2;
    }

    public StateComponent stateComponent(final double dumpingFactor, final double period, final double cvar) {
        Data data = new Data(dumpingFactor, period, cvar);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }
    
    public ISsfLoading defaultLoading(){
        return Loading.fromPosition(0);
    }
    
    static class Data {

        private final double var;
        private final double cdump, cperiod;

        public Data(double cyclicaldumpingfactor, double cyclicalperiod, double var) {
            this.var = var;
            cperiod = cyclicalperiod;
            cdump = cyclicaldumpingfactor;
        }
    }

    static class Initialization implements ISsfInitialization {

        final Data data;

        Initialization(Data data) {
            this.data = data;
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
        public int getStateDim() {
            return 2;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix p) {
            double q = data.var / (1 - data.cdump * data.cdump);
            p.diagonal().set(q);
        }

    }

    static class Dynamics implements ISsfDynamics {

        final Data data;
        private final double ccos, csin, e;

        Dynamics(Data data) {
            this.data = data;
            e = Math.sqrt(data.var);
            double q = Math.PI * 2 / data.cperiod;
            ccos = data.cdump * Math.cos(q);
            csin = data.cdump * Math.sin(q);
        }

        @Override
        public int getInnovationsDim() {
            return data.var == 0 ? 0 : 2;
        }

        @Override
        public void V(int pos, FastMatrix v) {
            v.diagonal().set(data.var);
        }

        @Override
        public void S(int pos, FastMatrix s) {
            s.diagonal().set(e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.var != 0;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.set(0, 0, ccos);
            tr.set(0, 1, csin);
            tr.set(1, 0, -csin);
            tr.set(1, 1, ccos);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            double a = x.get(0), b = x.get(1);
            x.set(0, a * ccos + b * csin);
            x.set(1, -a * csin + b * ccos);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addAY(e, u);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.diagonal().add(data.var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            double a = x.get(0), b = x.get(1);
            x.set(0, a * ccos - b * csin);
            x.set(1, a * csin + b * ccos);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.setAY(e, x);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }
}
