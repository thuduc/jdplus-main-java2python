/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;

/**
 * QR decomposition of a matrix. The matrix Q is represented as a product of
 * elementary reflectors Q = H(1) H(2) . . . H(k), where k = min(m,n).
 *
 * Each H(i) has the form
 *
 * H(i) = I - beta * v * v'
 *
 * where beta is a real scalar, and v is a real vector with v(0:i-1) = 0
 * v(i) = 1/beta; v(i+1:m) is stored in qr(i+1:m,i), and beta in beta(i).
 *
 * v is defined by e1-+x/||x|| (- if x0 lt 0, + otherwise
 * we write x/||x||=y
 *
 * beta = 2/(v*v') = 2/( e1-+y)(e1-+y)' = 2/(2-+2y0)) = 1/(1-+y0)=1/v0
 *
 *
 * H*x = H*y*||x|| = +- e1*||x||
 *
 * In the case of Singular matrix, householder with pivoting should be used.
 * All the betas equal to 0 should be placed at the end of the beta-vector
 *
 * When pivoting is not used, pivot should be null.
 *
 * @author palatej
 */
public class QRDecomposition {

    private final FastMatrix qr;
    private final double[] beta;
    private final int[] pivot;

    @FunctionalInterface
    public static interface Decomposer {

        QRDecomposition decompose(FastMatrix A);
    }

    public QRDecomposition(FastMatrix qr, double[] beta, int[] pivot) {
        this.qr = qr;
        this.beta = beta;
        this.pivot = pivot;
    }

    /**
     * The method multiplies an array of double by Q.
     *
     * @param b The array of double. It contains the product at the return of
     * the method
     */
    public void applyQ(double[] b) {
        double[] p = qr.getStorage();
        int m = qr.getRowsCount(), n = qr.getColumnsCount();
        if (m != b.length) {
            throw new MatrixException(MatrixException.DIM);
        }
        for (int k = n - 1, ik = (m + 1) * (n - 1); k >= 0; --k, ik -= m + 1) {
            if (beta[k] != 0) {
                double s = b[k] / beta[k];
                for (int i = k + 1, j = ik + 1; i < m; ++i, ++j) {
                    s += p[j] * b[i];
                }
                if (s != 0) {
                    b[k] -= s;
                    s *= -beta[k];
                    for (int i = k + 1, j = ik + 1; i < m; ++i, ++j) {
                        b[i] += s * p[j];
                    }
                }
            }
        }
    }

    /**
     * The method multiplies an array of double by the transpose of Q (also
     * equal to its inverse).
     *
     * @param b
     */
    public void applyQt(double[] b) {
        double[] p = qr.getStorage();
        int m = qr.getRowsCount(), n = qr.getColumnsCount();
        if (m != b.length) {
            throw new MatrixException(MatrixException.DIM);
        }
        for (int k = 0, ik = 0; k < n; ++k, ik += m + 1) {
            if (beta[k] != 0) {
                double s = b[k] / beta[k];
                for (int i = k + 1, j = ik + 1; i < m; ++i, ++j) {
                    s += b[i] * p[j];
                }
                if (s != 0) {
                    b[k] -= s;
                    s *= -beta[k];
                    for (int i = k + 1, j = ik + 1; i < m; ++i, ++j) {
                        b[i] += s * p[j];
                    }
                }
            }
        }
    }

    public int m() {
        return qr.getRowsCount();
    }

    public int n() {
        return qr.getColumnsCount();
    }

    public FastMatrix rawR() {
        int n = qr.getColumnsCount();
        FastMatrix R = qr.extract(0, n, 0, n).deepClone();
        UpperTriangularMatrix.toUpper(R);
        return R;
    }

    /**
     * Contains the order in which the columns of X have be handled.
     * pivot[i] indicates which column is in position i after pivoting
     * @return 
     */
    public int[] pivot() {
        return pivot == null ? null : pivot.clone();
    }

    public DoubleSeq rawRdiagonal() {
        return qr.diagonal();
    }

    public int pivotSign() {
        if (pivot == null) {
            return 1;
        }
        int n = 0;
        for (int i = 0; i < pivot.length; ++i) {
            for (int j = 0; j < i; ++j) {
                if (pivot[j] > pivot[i]) {
                    ++n;
                }
            }
        }
        return n%2 == 0 ? 1 : -1;
    }
}
