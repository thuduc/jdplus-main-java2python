/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.core.discrete;

import tck.demetra.data.Data;
import tck.demetra.data.MatrixSerializer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Random;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class DiscreteModelKernelTest {

    public DiscreteModelKernelTest() {
    }

    public static void main(String[] args) {
        int n = 10000;
        int[] y = new int[n];
        for (int i = 0; i < y.length; ++i) {
            y[i] = i < n / 2 ? 0 : 1;
        }

        FastMatrix M = FastMatrix.make(n, 10);
        Random rnd = new Random(0);
        M.set((i, j) -> rnd.nextDouble());
        M.add(-.5);
        M.column(0).set(1);

        DiscreteModel model = new DiscreteModel(y, M, new Probit());
        DiscreteModelEvaluation rslt = DiscreteModelKernel.process(model, null);
    }

 //   @Test
    public void logit() throws URISyntaxException, IOException {
        URI uri = Data.class.getResource("/smarket.txt").toURI();
        Matrix smarket = MatrixSerializer.read(Path.of(uri).toFile());
        assertTrue(smarket != null);
        FastMatrix M = FastMatrix.make(smarket.getRowsCount(), 7);
        for (int i = 0; i < 6; ++i) {
            M.column(i + 1).copy(smarket.column(i + 1));
        }
        M.column(0).set(1);
        int[] y = new int[M.getRowsCount()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = smarket.get(i, 7) < 0 ? 0 : 1;
        }

        DiscreteModel model = new DiscreteModel(y, M, new Logit());
        DiscreteModelEvaluation rslt = DiscreteModelKernel.process(model, null);
        assertEquals(rslt.getCoefficients().get(0), -0.126 , 1e-5);
        assertTrue(rslt.getGradient().norm2()<1e-4);
        
    }
}
