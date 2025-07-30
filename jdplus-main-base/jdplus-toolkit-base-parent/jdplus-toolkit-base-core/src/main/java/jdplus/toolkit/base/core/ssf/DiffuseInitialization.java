/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 * Fully diffuse initialization
 * @author Jean Palate
 */
public class DiffuseInitialization implements ISsfInitialization {
    
    private final int dim;
    
    public DiffuseInitialization(int dim) {
        this.dim = dim;
    }
    
    @Override
    public int getStateDim() {
        return dim;
    }
    
    @Override
    public boolean isDiffuse() {
        return true;
    }
    
    @Override
    public int getDiffuseDim() {
        return dim;
    }
    
    @Override
    public void diffuseConstraints(FastMatrix b) {
        b.diagonal().set(1);
    }
    
    @Override
    public void a0(DataBlock a) {
    }
    
    @Override
    public void Pf0(FastMatrix pf) {
    }
    
    @Override
    public void Pi0(FastMatrix pi) {
        pi.diagonal().set(1);
    }
    
}
