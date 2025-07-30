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

/**
 *
 * @author Jean Palate
 */
//@Development(status = Development.Status.Alpha)
//public class MultivariateCkmsInitializer implements MultivariateCkmsArrayFilter.IFastFilterInitializer {
//
//
//    public int initializeStationary(LState state, MultivariateUpdateInformation upd, IMultivariateSsf ssf, IMultivariateSsfData data) {
////        ISsfDynamics dynamics = ssf.dynamics();
////        ISsfLoading loading = ssf.loading();
////        ISsfError error = ssf.measurementError();
////        ISsfInitialization initialization = ssf.initialization();
////        FastMatrix P0 = FastMatrix.square(initialization.getStateDim());
////        initialization.Pf0(P0);
////        DataBlock m = upd.M();
////        loading.ZM(0, P0, m);
////        fstate.l.copy(m);
////        dynamics.TX(0, fstate.l());
////        double f = loading.ZX(0, fstate.l);
////        if (error != null) {
////            f += error.at(0);
////        }
////        upd.
//        return 0;
//    }
//
//    public int initializeDiffuse(LState state, MultivariateUpdateInformation upd, IMultivariateSsf ssf, IMultivariateSsfData data) {
////        CkmsDiffuseInitializer initializer = new CkmsDiffuseInitializer();
////        return initializer.initializeFilter(fstate, upd, ssf, data);
//        return 0;
//    }
//
//    @Override
//    public int initializeFilter(LState state, MultivariateUpdateInformation upd, IMultivariateSsf ssf, IMultivariateSsfData data) {
//        if (!ssf.isTimeInvariant()) {
//            return -1;
//        }
//        if (ssf.initialization().isDiffuse()) {
//            return initializeDiffuse(state, upd, ssf, data);
//        } else {
//            return initializeStationary(state, upd, ssf, data);
//        }
//    }
//}
