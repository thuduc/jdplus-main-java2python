/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.lapack;

import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import ec.tstoolkit.random.JdkRNG;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.lapack.Cholesky;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class CholeskyTest {

    public CholeskyTest() {
    }

    @Test
    public void testUCholesky() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.ucholesky(T);
        UpperTriangularMatrix.toUpper(T);
        FastMatrix del = SymmetricMatrix.UtU(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testLCholesky() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testSingularLCholesky() {
        FastMatrix X = FastMatrix.make(3, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        boolean ok = true;
        try {
            SymmetricMatrix.lcholesky(T);
        } catch (MatrixException err) {
            ok = false;
        }
        assertTrue(!ok);

        T = S.deepClone();
        SymmetricMatrix.lcholesky(T, 1e-9);
        FastMatrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testSingularUCholesky() {
        FastMatrix X = FastMatrix.make(3, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        boolean ok = true;
        try {
            SymmetricMatrix.ucholesky(T);

        } catch (MatrixException err) {
            ok = false;
        }
        assertTrue(!ok);

        T = S.deepClone();
        SymmetricMatrix.ucholesky(T, 1e-9);
        UpperTriangularMatrix.toUpper(T);
        FastMatrix del = SymmetricMatrix.UtU(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    public static void main(String[] args) {
        int N = 15, K = 1000000;
        FastMatrix X = FastMatrix.make(2 * N, N);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);

         Cholesky C=new Cholesky();
      long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FastMatrix T = S.deepClone();
             C.lcholesky(T);
       }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
         t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FastMatrix T = S.deepClone();
            SymmetricMatrix.lcholesky(T);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FastMatrix T = S.deepClone();
            C.ucholesky(T);
        } 
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            FastMatrix T = S.deepClone();
            SymmetricMatrix.ucholesky(T);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
