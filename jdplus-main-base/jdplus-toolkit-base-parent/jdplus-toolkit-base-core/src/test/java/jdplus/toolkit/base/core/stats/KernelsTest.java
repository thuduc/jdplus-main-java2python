/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.stats;

import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.functions.NumericalIntegration;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.stats.Kernel;
import jdplus.toolkit.base.core.stats.Kernels;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class KernelsTest {

    public KernelsTest() {
    }

    @Test
    public void testHenderson() {
        Polynomial p0 = Kernels.hendersonAsPolynomial(11);
        assertEquals(p0.integrate(-1, 1), 1, 1e-9);
    }

    @Test
    public void testMoments() {
        testMoments(Kernels.UNIFORM, 10);
        testMoments(Kernels.TRIANGULAR, 10);
        testMoments(Kernels.BIWEIGHT, 10);
        testMoments(Kernels.EPANECHNIKOV, 10);
        testMoments(Kernels.TRIWEIGHT, 10);
    }

    public void testMoments(Kernel k, int max) {
        DoubleUnaryOperator density = k.asFunction();
        for (int i = 0; i <= 10; ++i) {
            int m = i;
            double q = NumericalIntegration.integrate(x -> Math.pow(x, m) * density.applyAsDouble(x), -1, 1);
            assertEquals(q, k.moment(i), 1e-9);
        }
    }
}
