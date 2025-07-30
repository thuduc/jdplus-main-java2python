/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.toolkit.base.core.math.highprecision;

import jdplus.toolkit.base.api.math.Complex;
import java.util.Random;
import java.util.function.DoubleSupplier;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.PolynomialComputer;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublePolynomialComputerTest {
    
    public DoublePolynomialComputerTest() {
    }

    @Test
    public void testEvaluation() {
        DataBlock P = DataBlock.make(300);
        Random rnd = new Random(0);
        P.set((DoubleSupplier)rnd::nextDouble);

        Polynomial sp = Polynomial.of(P.toArray());
        PolynomialComputer cp=new PolynomialComputer(sp);
        cp.computeAll(Complex.cart(.3, .5));
//        System.out.println(cp.f());
//        System.out.println(cp.df());
        cp.computeAll(0.9923);
//        System.out.println(cp.f());
//        System.out.println(cp.df());

        DoublePolynomial dp = DoublePolynomial.of(P);
        DoublePolynomialComputer dcp=new DoublePolynomialComputer(dp);
        dcp.computeAll(DoubleComplex.cart(.3, .5));
//        System.out.println(dcp.f());
//        System.out.println(dcp.df());
        dcp.computeAll(new DoubleDouble(0.9923, 0));
//        System.out.println(dcp.f());
//        System.out.println(dcp.df());
    }
    
}
