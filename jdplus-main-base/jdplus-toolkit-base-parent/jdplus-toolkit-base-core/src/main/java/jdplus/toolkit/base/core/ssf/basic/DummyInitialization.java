/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
package jdplus.toolkit.base.core.ssf.basic;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DummyInitialization implements ISsfInitialization {
    
    private final int dim;
    
    public DummyInitialization(int dim){
        this.dim=dim;
    }

    @Override
    public int getStateDim() {
        return dim;
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
    }

}
