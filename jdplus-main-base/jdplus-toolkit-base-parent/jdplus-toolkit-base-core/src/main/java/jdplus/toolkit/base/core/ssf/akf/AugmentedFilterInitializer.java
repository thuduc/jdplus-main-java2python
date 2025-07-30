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

import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;

/**
 *
 * @author Jean Palate
 */
public class AugmentedFilterInitializer implements OrdinaryFilter.Initializer {

    private final IQFilteringResults results;

    public AugmentedFilterInitializer(IQFilteringResults results) {
        this.results = results;
    }

    @Override
    public int initializeFilter(State state, ISsf ssf, ISsfData data) {
        AugmentedFilter akf = new AugmentedFilter(true);
        boolean ok = akf.process(ssf, data, results);
        if (!ok) {
            return -1;
        }
        AugmentedState astate = akf.getState();
        state.copy(astate);
        int nd = akf.getCollapsingPosition();
//        if (nd < 0) {
//            throw new SsfException("Initialization by the augmented filter failed ");
//        }

        return nd;
    }

}
