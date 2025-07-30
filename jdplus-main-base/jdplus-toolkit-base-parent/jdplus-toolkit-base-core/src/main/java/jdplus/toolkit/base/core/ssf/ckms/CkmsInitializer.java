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
package jdplus.toolkit.base.core.ssf.ckms;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CkmsInitializer implements CkmsFilter.IFastFilterInitializer {

    /**
     * K = TPZ', L=K, F=ZPZ'+H
     *
     * @param ssf
     * @param upd
     * @param fstate
     * @param data
     * @return
     */
    @Override
    public int initializeFilter(final CkmsState fstate, final UpdateInformation upd, final ISsf ssf, ISsfData data) {
        if (!ssf.isTimeInvariant()) {
            return -1;
        }
        if (ssf.initialization().isDiffuse()) {
            return initializeDiffuse(fstate, upd, ssf, data);
        } else {
            return initializeStationary(fstate, upd, ssf, data);
        }
    }

    public int initializeStationary(final CkmsState fstate, final UpdateInformation upd, final ISsf ssf, ISsfData data) {
        ISsfDynamics dynamics = ssf.dynamics();
        ISsfLoading loading = ssf.loading();
        ISsfError error = ssf.measurementError();
        ISsfInitialization initialization = ssf.initialization();
        FastMatrix P0 = FastMatrix.square(initialization.getStateDim());
        initialization.Pf0(P0);
        DataBlock m = upd.M();
        loading.ZM(0, P0, m);
        fstate.l.copy(m);
        dynamics.TX(0, fstate.l());
        double f = loading.ZX(0, fstate.l);
        if (error != null) {
            f += error.at(0);
        }
        upd.setVariance(f);
        return 0;
    }

    public int initializeDiffuse(final CkmsState fstate, final UpdateInformation upd, final ISsf ssf, ISsfData data) {
        CkmsDiffuseInitializer initializer = new CkmsDiffuseInitializer();
        return initializer.initializeFilter(fstate, upd, ssf, data);
    }
}
