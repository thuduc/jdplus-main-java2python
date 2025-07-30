/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.linearsystem;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;

/**
 * Solution to the least squares problem: min || y - X*b ||
 * by means of the QR algorithm:
 *
 * X = Q*R
 * || y - X*b || = || Q'*y - R*b ||
 *
 * Q'*y = z = (z0', z1')'
 * z0 = R*b <=> R^-1*z0 = b
 *
 * || y - X*b || = || z1 ||
 *
 * z1 = e (residuals)
 * z1'z1 = ssqerr
 *
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class QRLeastSquaresSolution {

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private QRDecomposition qr;
    private int rank;
    private DoubleSeq b;
    private DoubleSeq e;
    private double ssqErr;

    public int rank() {
        return rank;
    }

    public FastMatrix rawR() {
        return qr.rawR();
    }

    public DoubleSeq rawRDiagonal() {
        return qr.rawRdiagonal();
    }

    /**
     * Contains the order in which the columns of X have be handled.
     * pivot[i] indicates which column is in position i after pivoting
     *
     * @return
     */
    public int[] pivot() {
        return qr.pivot();
    }

    public FastMatrix unscaledCovariance() {
        int[] pivot = qr.pivot();
        FastMatrix rawR = qr.rawR().extract(0, rank, 0, rank);
        FastMatrix v = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(rawR));
        int n = qr.n();
        if (pivot == null) {
            if (rank == n) {
                return v;
            } else {
                FastMatrix V = FastMatrix.square(n);
                V.extract(0, rank, 0, rank).copy(v);
                return V;
            }
        } else {
            FastMatrix V = FastMatrix.square(n);
            for (int i = 0; i < rank; ++i) {
                double sii = v.get(i, i);
                V.set(pivot[i], pivot[i], sii);
                for (int j = 0; j < i; ++j) {
                    double sij = v.get(i, j);
                    V.set(pivot[i], pivot[j], sij);
                    V.set(pivot[j], pivot[i], sij);
                }
            }
            return V;
        }
    }

    /**
     * Inverse of the unscaled covariance matrix
     * =re-ordered rawR'*rawR
     *
     * @return
     */
    public FastMatrix RtR() {
        int[] pivot = qr.pivot();
        FastMatrix rawR = qr.rawR();
        FastMatrix v = SymmetricMatrix.UtU(rawR);
        if (pivot == null) {
            return v;
        } else {
            int n = pivot.length;
            FastMatrix V = FastMatrix.square(n);
            for (int i = 0; i < rank; ++i) {
                double sii = v.get(i, i);
                V.set(pivot[i], pivot[i], sii);
                for (int j = 0; j < i; ++j) {
                    double sij = v.get(i, j);
                    V.set(pivot[i], pivot[j], sij);
                    V.set(pivot[j], pivot[i], sij);
                }
            }
            return V;
        }
    }
}
