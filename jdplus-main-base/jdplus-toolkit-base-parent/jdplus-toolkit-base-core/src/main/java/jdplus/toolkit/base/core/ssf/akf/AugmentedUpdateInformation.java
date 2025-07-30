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
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class AugmentedUpdateInformation extends UpdateInformation {

    /**
     * E is the "prediction error" on the diffuse constraints (=(0-Z(t)A(t))
     */
    private final DataBlock E;

     /**
     *
     * @param ndiffuse
     * @param dim
     */
    public AugmentedUpdateInformation(final int dim, final int ndiffuse) {
        super(dim);
        E = DataBlock.make(ndiffuse);
    }

    public DataBlock E() {
        return E;
    }

    public boolean isDiffuse() {
         return !E.isZero(State.ZERO);
    }


}
