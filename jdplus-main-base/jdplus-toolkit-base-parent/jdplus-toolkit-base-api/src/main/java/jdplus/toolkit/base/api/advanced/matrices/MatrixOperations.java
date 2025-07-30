/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.advanced.matrices;

import jdplus.toolkit.base.api.advanced.algebra.Ring;
import jdplus.toolkit.base.api.design.InterchangeableProcessor;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class MatrixOperations {

    private final MatrixOperationsLoader.Processor PROCESSOR = new MatrixOperationsLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public Matrix plus(Matrix left, Matrix right) {
        return PROCESSOR.get().plus(left, right);
    }

    public Matrix plus(Matrix M, double d) {
        return PROCESSOR.get().plus(M, d);
    }

    public Matrix minus(Matrix left, Matrix right) {
        return PROCESSOR.get().minus(left, right);
    }

    public Matrix minus(Matrix M, double d) {
        return PROCESSOR.get().minus(M, d);
    }

    public Matrix times(Matrix left, Matrix right) {
        return PROCESSOR.get().times(left, right);
    }

    public Matrix times(Matrix M, double d) {
        return PROCESSOR.get().times(M, d);
    }

    public Matrix opposite(Matrix M) {
        return PROCESSOR.get().opposite(M);
    }

    public Matrix inv(Matrix M) {
        return PROCESSOR.get().inv(M);
    }

    public Matrix transpose(Matrix M) {
        return PROCESSOR.get().transpose(M);
    }

    public Matrix XXt(Matrix X) {
        return PROCESSOR.get().XXt(X);
    }

    public Matrix XtX(Matrix X) {
        return PROCESSOR.get().XtX(X);
    }


    @InterchangeableProcessor
    @SuppressWarnings(ServiceDefinition.SINGLE_FALLBACK_NOT_EXPECTED)
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Processor extends Ring<Matrix> {
        // Use the Ring definition to normalize the names. To get an actual ring, we should consider square matrices 
        // of size n.

        Matrix plus(Matrix M, double d);

        Matrix minus(Matrix M, double d);

        Matrix times(Matrix M, double d);

        Matrix inv(Matrix M);

        Matrix transpose(Matrix M);

        Matrix XXt(Matrix X);

        Matrix XtX(Matrix X);
    }

}
