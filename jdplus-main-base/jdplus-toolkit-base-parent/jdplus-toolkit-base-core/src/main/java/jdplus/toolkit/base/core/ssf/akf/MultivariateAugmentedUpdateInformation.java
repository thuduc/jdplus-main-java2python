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

/**
 * The augmented state only contains information on non missing values. So, it
 * has to be considered with the corresponding data.
 *
 * @author Jean Palate
 */
//public class MultivariateAugmentedUpdateInformation extends MultivariateUpdateInformation {
//
//    /**
//     * E is the "prediction error" on the diffuse constraints (=(0-Z(t)A(t)) E ~
//     * ndiffuse x nvars
//     */
//    private final FastMatrix E;
//
//    /**
//     *
//     * @param ndiffuse
//     * @param nvars
//     * @param dim
//     */
//    public MultivariateAugmentedUpdateInformation(final int dim, final int nvars, final int ndiffuse) {
//        super(dim, nvars);
//        E = FastMatrix.make(ndiffuse, nvars);
//    }
//
//    public FastMatrix E() {
//        return E;
//    }
//
//    public boolean isDiffuse() {
//        return E.isZero(State.ZERO);
//    }
//
//    public void compute(IMultivariateSsf ssf, int t, AugmentedState state, DoubleSeq x, int[] equations) {
//
//        super.compute(ssf, t, state, x, equations);
//        // E is ndiffuse x nobs. Each column contains the diffuse effects
//        // on the corresponding variable
//        MZt(t, ssf.measurements(), state.B(), E);
//        E.chs();
//        DataBlockIterator erows = E.rowsIterator();
//        while (erows.hasNext()) {
//            LowerTriangularMatrix.solveLx(this.getCholeskyFactor(), erows.next(), State.ZERO);
//        }
//    }
//}
