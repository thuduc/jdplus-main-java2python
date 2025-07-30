/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixUtility;
import jdplus.toolkit.base.core.math.matrices.decomposition.EigenRoutines;
import jdplus.toolkit.base.core.math.matrices.decomposition.UpperHessenberg;
import jdplus.toolkit.base.core.random.JdkRNG;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class UpperHessenbergTest {

    public UpperHessenbergTest() {
    }

    @Test
    public void testRandom() {
        FastMatrix A = FastMatrix.square(100);
        JdkRNG rng = JdkRNG.newRandom(0);
        MatrixUtility.randomize(A, rng);
        long t0 = System.currentTimeMillis();
        UpperHessenberg uh;
        for (int j = 0; j < 100; ++j) {
            uh = UpperHessenberg.of(A);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(uh.getH());

        t0 = System.currentTimeMillis();
        FastMatrix B;
        for (int j = 0; j < 100; ++j) {
            B = A.deepClone();
            EigenRoutines.hessenberg(B.getStorage(), B.getColumnsCount());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(B);
    }

}
