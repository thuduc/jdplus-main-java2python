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
package internal.toolkit.base.core.ar;

import java.util.Random;

import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.core.ar.AutoRegressiveEstimation;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class OlsAlgorithmTest {

    public static final DoubleSeq X;

    static {
        double[] x = new double[120];
        Random rnd = new Random(0);
        x[0] = rnd.nextGaussian();
        x[1] = rnd.nextGaussian();
        for (int i = 2; i < 120; ++i) {
            x[i] = x[i - 1] * .8 - x[i - 2] * .4 + rnd.nextGaussian();
        }
        X = DoubleSeq.of(x);
    }

    public OlsAlgorithmTest() {
    }

    @Test
    public void testSomeMethod() {

        AutoRegressiveEstimation ar = new OlsAlgorithm();
        ar.estimate(X, 30);
//        System.out.println(ar.coefficients());
//        System.out.println(ar.residuals());
    }

}
