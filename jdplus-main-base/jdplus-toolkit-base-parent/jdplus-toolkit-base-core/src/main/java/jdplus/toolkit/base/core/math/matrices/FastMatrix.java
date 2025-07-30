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
package jdplus.toolkit.base.core.math.matrices;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.math.matrices.decomposition.Gauss;
import jdplus.toolkit.base.core.math.matrices.decomposition.LUDecomposition;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;
import nbbrd.design.Development;
import nbbrd.design.Unsafe;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.data.DataWindow;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public final class FastMatrix implements Matrix.Mutable {

    public static Builder builder(final double[] storage) {
        return new Builder(storage);
    }

    public static class Builder {

        private final double[] storage;
        private int start = 0, lda;
        private int nrows, ncols;

        public Builder(double[] s) {
            this.storage = s;
        }

        public Builder start(/*@Positive*/ final int start) {
            this.start = start;
            return this;
        }

        public Builder nrows(/*@Positive*/ final int nrows) {
            this.nrows = nrows;
            return this;
        }

        public Builder ncolumns(/*@Positive*/ final int ncols) {
            this.ncols = ncols;
            return this;
        }

        public Builder columnIncrement(/*@Positive*/ final int lda) {
            this.lda = lda;
            return this;
        }

        public FastMatrix build() {
            if (lda != 0 && lda < nrows) {
                throw new MatrixException(MatrixException.DIM);
            }
            return new FastMatrix(storage, lda == 0 ? nrows : lda, start, nrows, ncols);
        }
    }

    private final double[] storage;
    private final int lda;
    int start, nrows, ncols;

    //<editor-fold defaultstate="collapsed" desc="matrix constructors">
    public static final FastMatrix EMPTY = new FastMatrix(Doubles.EMPTYARRAY, 0, 0);

    public static FastMatrix square(int n) {
        if (n == 0) {
            return FastMatrix.EMPTY;
        }
        double[] data = new double[n * n];
        return new FastMatrix(data, n, n);
    }

    public static FastMatrix make(int nrows, int ncols) {
        if (nrows == 0 || ncols == 0) {
            return FastMatrix.EMPTY;
        }
        double[] data = new double[nrows * ncols];
        return new FastMatrix(data, nrows, ncols);
    }

    public static FastMatrix of(Matrix matrix) {
        if (matrix == null) {
            return null;
        }
        return new FastMatrix(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
    }

    public static FastMatrix identity(int n) {
        FastMatrix i = square(n);
        i.diagonal().set(1);
        return i;
    }

    public static FastMatrix diagonal(DoubleSeq d) {
        FastMatrix i = square(d.length());
        i.diagonal().copy(d);
        return i;
    }

    public static FastMatrix rowOf(DataBlock x) {
        return new FastMatrix(x.toArray(), 1, x.length());
    }

    public static FastMatrix columnOf(DataBlock x) {
        return new FastMatrix(x.toArray(), x.length(), 1);
    }

    public static FastMatrix internalRowOf(DataBlock x) {
        return new FastMatrix(x.getStorage(), x.getIncrement(), x.getStartPosition(), 1, x.length());
    }

    public static FastMatrix internalColumnOf(DataBlock x) {
        if (x.getIncrement() != 1) {
            throw new MatrixException(MatrixException.DIM);
        }
        int start = x.getStartPosition(), n = x.length();
        return new FastMatrix(x.getStorage(), n, start, n, 1);
    }

    /**
     * Creates a new instance of SubMatrix
     *
     * @param data
     * @param nrows
     * @param ncols
     */
    public FastMatrix(final double[] data, final int nrows, final int ncols) {
        this.storage = data;
        this.nrows = nrows;
        this.ncols = ncols;
        this.start = 0;
        this.lda = nrows;
    }

    /**
     * Creates a new instance of SubMatrix
     *
     * @param nrows
     * @param ncols
     */
    FastMatrix(final int nrows, final int ncols) {
        this.storage = new double[nrows * ncols];
        this.lda = nrows;
        this.start = 0;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    FastMatrix(double[] x, int lda, int start, int nrows, int ncols) {
        this.storage = x;
        this.lda = lda;
        this.start = start;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    //</editor-fold>
    public FastMatrix deepClone() {
        return new FastMatrix(toArray(), nrows, ncols);
    }

//    public FastMatrix shallowClone() {
//        return new FastMatrix(storage, lda, start, nrows, ncols);
//    }
    /**
     * Creates a sub-matrix
     *
     * @param r0 starting row
     * @param nr number of rows
     * @param c0 starting column
     * @param nc number of columns
     * @return
     */
    @Override
    public FastMatrix extract(int r0, int nr, int c0, int nc) {
        return new FastMatrix(storage, lda, start + r0 + c0 * lda, nr, nc);
    }

    public FastMatrix dropTopLeft(int nr, int nc) {
        return new FastMatrix(storage, lda, start + nr + nc * lda, nrows - nr, ncols - nc);
    }

    public FastMatrix dropBottomRight(int nr, int nc) {
        return new FastMatrix(storage, lda, start, nrows - nr, ncols - nc);
    }

    //<editor-fold defaultstate="collapsed" desc="diagnostics">
    public boolean isFull() {
        return storage.length == nrows * ncols;
    }

    public boolean isDiagonal(double zero) {
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }

        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(1).isZero(zero)) {
                return false;
            }
            if (!udiag.slideAndShrink(lda).isZero(zero)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIdentity() {
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return get(0, 0) == 1;
        }
        if (diagonal().anyMatch(x -> x != 1)) {
            return false;
        }

        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (ldiag.slideAndShrink(1).anyMatch(x -> x != 0)) {
                return false;
            }
            if (udiag.slideAndShrink(lda).anyMatch(x -> x != 0)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDiagonal() {
        return isDiagonal(0);
    }

    public boolean isZero(double eps) {
        return test(x -> Math.abs(x) <= eps);
    }

    public boolean test(final DoublePredicate pred) {
        if (isEmpty()) {
            return true;
        }
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            if (!cols.next().allMatch(pred)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSymmetric(double eps) {
        if (nrows != ncols) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }
        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(1).allMatch(udiag.slideAndShrink(lda),
                    (x, y) -> Math.abs(x - y) <= eps)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSymmetric() {
        return isSymmetric(0);
    }

    /**
     * Gets the index of the last row with values different from zero.
     *
     * @return Index of the row or -1 if the matrix is zero
     */
    public int lastSignificantRow() {
        if (isEmpty()) {
            return -1;
        }
        int r0 = start + nrows - 1, r1 = r0 + lda * (ncols - 1);
        if (storage[r0] != 0 || storage[r1] != 0) {
            return nrows - 1;
        }
        // test by column
        int ncur = nrows - 1, c = start;
        for (int j = 0; j < ncols; ++j) {
            while (ncur >= c && storage[ncur] == 0) {
                --ncur;
            }
            if (ncur < c) {
                return -1;
            }
            ncur += lda;
            c += lda;
        }
        return ncur - c;
    }

    /**
     * Gets the index of the last column with values different from zero.
     *
     * @return Index of the column or -1 if the matrix is zero
     */
    public int lastSignificantColumn() {
        if (isEmpty()) {
            return -1;
        }
        for (int c = ncols - 1, j = start + ncols * lda; c >= 0; --c) {
            j -= lda;
            int kmax = j + nrows;
            for (int k = j; k < kmax; ++k) {
                if (storage[k] != 0) {
                    return c;
                }
            }

        }
        return -1;
    }

    //</editor-fold>
    @Override
    public void copyTo(double[] buffer, int pos) {
        if (isFull()) {
            System.arraycopy(storage, 0, buffer, pos, storage.length);
        }
        int lmax = pos + nrows * ncols;
        for (int l = pos, i0 = start, i1 = start + nrows; l < lmax; i0 += lda, i1 += lda) {
            for (int k = i0; k < i1; ++k, ++l) {
                buffer[l] = storage[k];
            }
        }
    }

    @Override
    public double[] toArray() {
        if (isFull()) {
            return storage.clone();
        }

        double[] z = new double[nrows * ncols];
        for (int l = 0, i0 = start, i1 = start + nrows; l < z.length; i0 += lda, i1 += lda) {
            for (int k = i0; k < i1; ++k, ++l) {
                z[l] = storage[k];
            }
        }
        return z;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public double get(final int row, final int col) {
        return storage[start + row + col * lda];
    }

    /**
     *
     * @return
     */
    @Override
    public int getColumnsCount() {
        return ncols;
    }

    /**
     *
     * @return
     */
    @Override
    public int getRowsCount() {

        return nrows;
    }

    @Unsafe
    public double[] getStorage() {
        return storage;
    }

    /**
     * Position of the top-left cell
     *
     * @return
     */
    public int getStartPosition() {
        return start;
    }

    /**
     * Position of the bottom-right cell
     *
     * @return
     */
    public final int getLastPosition() {
        return start + (nrows - 1) + (ncols - 1) * lda;
    }

    /**
     * Increment between two adjacent cells on the same row
     *
     * @return
     */
    public int getColumnIncrement() {
        return lda;
    }

    /**
     * Gets a given skew-diagonal of the matrix (as opposed to the main
     * diagonal)
     *
     * @param pos The index of the skew-diagonal (in [0, max(rowsCount,
     * columnsCount)[).
     * @return The src blocks representing the skew-diagonal. Refers to the
     * actual src (changing the src block modifies the underlying matrix).
     */
    public DataBlock skewDiagonal(int pos) {
        if (pos < 0) {
            return null;
        }
        int nmax = Math.max(nrows, ncols);
        if (pos >= nmax) {
            return null;
        }

        int beg, inc = lda - 1;
        int n;
        if (pos < nrows) {
            beg = start + pos;
            n = Math.min(pos + 1, ncols);
        } else {
            int rlast = nrows - 1;
            int col = pos - rlast;
            beg = start + rlast + lda * (col); // cell (nrows-1, pos-(nrows-1)) 
            n = Math.min(nrows, ncols - col);
        }
        return DataBlock.of(storage, beg, beg + inc * n, inc);
    }

    public void set(MatrixFunction fn) {
        for (int c = 0, k = start; c < ncols; ++c, k += lda) {
            for (int r = 0, l = k; r < nrows; ++r, ++l) {
                storage[l] = fn.apply(r, c);
            }
        }
    }

    public void set(double value) {
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = value;
            }
        } else {
            for (int c = 0, k = start; c < ncols; ++c, k += lda) {
                for (int r = 0, l = k; r < nrows; ++r, ++l) {
                    storage[l] = value;
                }
            }
        }
    }

    /**
     *
     * @param row
     * @param col
     * @param value
     */
    @Override
    public void set(final int row, final int col, final double value) {
        storage[start + row + col * lda] = value;
    }

    public void set(final int row, final int col, DoubleSupplier fn) {
        storage[start + row + col * lda] = fn.getAsDouble();
    }

    public void add(final int row, final int col, final double value) {
        storage[start + row + col * lda] += value;
    }

    public void mul(final int row, final int col, final double value) {
        storage[start + row + col * lda] *= value;
    }

    public void copy(FastMatrix B) {
        if (nrows != B.nrows || ncols != B.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && B.isFull()) {
            System.arraycopy(B.storage, 0, storage, 0, storage.length);
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = B.start; i0 < end; i0 += lda, i1 += lda, j0 += B.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] = B.storage[l];
                }
            }
        }
    }

    public void setAY(double a, FastMatrix Y) {
        if (nrows != Y.nrows || ncols != Y.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (a == 0) {
            set(0);
        } else if (a == 1) {
            copy(Y);
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = Y.start; i0 < end; i0 += lda, i1 += lda, j0 += Y.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] = a * Y.storage[l];
                }
            }
        }
    }

    public void copyTranspose(FastMatrix B) {
        if (nrows != B.ncols || ncols != B.nrows) {
            throw new MatrixException(MatrixException.DIM);
        }
        int end = start + ncols * lda;
        for (int i0 = start, i1 = start + nrows, j0 = B.start; i0 < end; i0 += lda, i1 += lda, ++j0) {
            for (int k = i0, l = j0; k < i1; ++k, l += B.lda) {
                storage[k] = B.storage[l];
            }
        }
    }

    public FastMatrix transpose() {
        FastMatrix T = FastMatrix.make(ncols, nrows);
        T.copyTranspose(this);
        return T;
    }

    /**
     * Return the sum of all the cells of the matrix
     *
     * @return
     */
    public double sum() {
        if (isEmpty()) {
            return 0;
        }
        double s = 0;
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                s += storage[i];
            }
        } else {
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                s += cols.next().sum();
            }
        }
        return s;
    }

    /**
     * Return the sum of the squares of all the cells of the matrix
     *
     * @return
     */
    public double ssq() {
        if (isEmpty()) {
            return 0;
        }
        double s = 0;
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                s += storage[i] * storage[i];
            }
        } else {
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                s += cols.next().ssq();
            }
        }
        return s;
    }

    /**
     * Computes sum(this(i,j**m(i,j))
     *
     * @param m The right operand of the dot product
     * @return
     */
    public double dot(FastMatrix m) {
        double p = 0;
        DataBlockIterator cols = columnsIterator(), mcols = m.columnsIterator();
        while (cols.hasNext()) {
            p += cols.next().dot(mcols.next());
        }
        return p;
    }

    /**
     *
     * @param row
     * @param col
     * @param fn
     */
    @Override
    public void apply(final int row, final int col, final DoubleUnaryOperator fn) {
        int idx = start + row + col * lda;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

    public void apply(final DoubleUnaryOperator fn) {
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = fn.applyAsDouble(storage[i]);
            }
        } else {
            applyByColumns(col -> col.apply(fn));
        }
    }

    public void applyByRows(final Consumer<DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next());
        }
    }

    public void applyByColumns(final Consumer<DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next());
        }
    }

    public void applyByRows(final FastMatrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        DataBlockIterator mrows = M.rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next(), mrows.next());
        }
    }

    public void applyByColumns(final FastMatrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next(), mcols.next());
        }
    }

    public Iterable<DataBlock> rows() {
        return () -> new Rows(this);
    }

    public Iterable<DataBlock> columns() {
        return () -> new Columns(this);
    }

    /**
     *
     * @param M
     */
    public void add(FastMatrix M) {
        if (nrows != M.nrows || ncols != M.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && M.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] += M.storage[i];
            }
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, j0 += M.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] += M.storage[l];
                }
            }
        }
    }

    public void addTranspose(FastMatrix M) {
        if (nrows != M.ncols || ncols != M.nrows) {
            throw new MatrixException(MatrixException.DIM);
        }
        int end = start + ncols * lda;
        for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, ++j0) {
            for (int k = i0, l = j0; k < i1; ++k, l += M.lda) {
                storage[k] += M.storage[l];
            }
        }
    }

    public void add(double d) {
        if (d != 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] += d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] += d;
                    }
                }
            }
        }
    }

    public void chs() {
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = -storage[i];
            }
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                for (int k = i0; k < i1; ++k) {
                    storage[k] = -storage[k];
                }
            }
        }
    }

    public void sub(FastMatrix M) {
        if (nrows != M.nrows || ncols != M.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && M.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] -= M.storage[i];
            }
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, j0 += M.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] -= M.storage[l];
                }
            }
        }
    }

    public void subTranspose(FastMatrix M) {
        if (nrows != M.ncols || ncols != M.nrows) {
            throw new MatrixException(MatrixException.DIM);
        }
        int end = start + ncols * lda;
        for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, ++j0) {
            for (int k = i0, l = j0; k < i1; ++k, l += M.lda) {
                storage[k] -= M.storage[l];
            }
        }
    }

    public void sub(double d) {
        if (d != 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] -= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int c = 0; c < ncols; ++c, i0 += lda, i1 += lda) {
                        for (int k = i0; k < i1; ++k) {
                            storage[k] += d;
                        }
                    }
                }
            }
        }
    }

    public void mul(double d) {
        if (d == 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] = 0;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] = 0;
                    }
                }
            }
        } else if (d != 1) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] *= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] *= d;
                    }
                }
            }
        }
    }

    public void div(double d) {
        if (d == 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] = Double.NaN;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] = Double.NaN;
                    }
                }
            }
        } else if (d != 1) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] /= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] /= d;
                    }
                }
            }
        }
    }

    public void addAY(double alpha, FastMatrix Y) {
        if (alpha == 0) {
            return;
        }

        DataBlockIterator cols = this.columnsIterator();
        DataBlockIterator ycols = Y.columnsIterator();
        while (cols.hasNext()) {
            cols.next().addAY(alpha, ycols.next());
        }
    }

    public void addAYt(double alpha, FastMatrix Y) {
        if (alpha == 0) {
            return;
        }

        DataBlockIterator cols = this.columnsIterator();
        DataBlockIterator ycols = Y.rowsIterator();
        while (cols.hasNext()) {
            cols.next().addAY(alpha, ycols.next());
        }
    }

    public void addXaYt(final double a, final DataBlock x, final DataBlock y) {
        if (a == 0) {
            return;
        }
        double[] px = x.getStorage(), py = y.getStorage();
        int xinc = x.getIncrement(), yinc = y.getIncrement();
        int x0 = x.getStartPosition(), x1 = x.getEndPosition(), y0 = y.getStartPosition(), y1 = y.getEndPosition();

        // Raw gaxpy implementation
        for (int pos = start, ypos = y0; ypos != y1; ypos += yinc, pos += lda) {
            double yc = py[ypos];
            if (yc != 0) {
                yc *= a;
                if (xinc == 1) {
                    for (int xpos = x0, cpos = pos; xpos < x1; ++cpos, ++xpos) {
                        storage[cpos] += yc * px[xpos];
                    }
                } else {
                    for (int xpos = x0, cpos = pos; xpos != x1; ++cpos, xpos += xinc) {
                        storage[cpos] += yc * px[xpos];
                    }
                }
            }
        }
    }

    /**
     * This = This + a X*X'. This matrix must be a square matrix
     *
     * @param a Scalar
     * @param x Array. Length equal to the number of rows of this matrix
     */
    public void addXaXt(final double a, final DataBlock x) {
        addXaYt(a, x, x);
    }

    public FastMatrix inv() {
        if (!isSquare()) {
            throw new IllegalArgumentException();
        }
        FastMatrix I = FastMatrix.identity(nrows);
        LUDecomposition lu = Gauss.decompose(this);
        lu.solve(I);
        return I;
    }

    public static LogSign logDeterminant(FastMatrix X) {
        if (!X.isSquare()) {
            throw new IllegalArgumentException();
        }
        if (X.nrows == 1) {
            double x00 = X.get(0, 0);
            return new LogSign(Math.log(Math.abs(x00)), x00 < 0);
        }
//        LUDecomposition lu = Gauss.decompose(X);
//        return lu.logDeterminant();
        HouseholderWithPivoting hous = new HouseholderWithPivoting();
        QRDecomposition qr = hous.decompose(X, 0);
        int rank = UpperTriangularMatrix.rank(qr.rawR(), 1e-13);
        if (rank < X.nrows) {
            return null;
        }
        return LogSign.of(qr.rawRdiagonal(), (qr.pivotSign() == -1) != (X.nrows % 2 == 1));
    }

    public static double determinant(FastMatrix X) {
        try {
            if (X.nrows == 1) {
                return X.get(0, 0);
            }
            LogSign ls = logDeterminant(X);
            if (ls == null) {
                return 0;
            }
            double val = Math.exp(ls.getValue());
            return ls.isPositive() ? val : -val;
        } catch (MatrixException err) {
            return 0; // singular matrix
        }
    }

    @Override
    public DataBlock diagonal() {
        int n = Math.min(nrows, ncols), inc = 1 + lda;
        return DataBlock.of(storage, start, start + inc * n, inc);
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public DataBlock subDiagonal(int pos) {
        if (pos >= ncols) {
            return DataBlock.EMPTY;
        }
        if (-pos >= nrows) {
            return DataBlock.EMPTY;
        }
        int beg = start, inc = 1 + lda;
        int n;
        if (pos > 0) {
            beg += pos * lda;
            n = Math.min(nrows, ncols - pos);
        } else if (pos < 0) {
            beg -= pos;
            n = Math.min(nrows + pos, ncols);
        } else {
            n = Math.min(nrows, ncols);
        }
        return DataBlock.of(storage, beg, beg + inc * n, inc);
    }

    @Override
    public DataBlock row(int r) {
        int inc = lda;
        int beg = start + r, end = beg + ncols * inc;
        return DataBlock.of(storage, beg, end, inc);
    }

    @Override
    public DataBlock column(int c) {
        int beg = start + c * lda, end = beg + nrows;
        return DataBlock.of(storage, beg, end, 1);
    }

    /**
     *
     * @param r0 Row index of the first cell
     * @param c0 Column index of the first cell
     * @param n Number of cells
     * @return
     */
    public DataBlock diagonal(int r0, int c0, int n) {
        int inc = lda + 1;
        int beg = start + c0 * lda + r0, end = beg + n * inc;
        return DataBlock.of(storage, beg, end, inc);
    }

    /**
     *
     * @param r0 Row index of the first cell
     * @param c0 Column index of the first cell
     * @param n Number of cells
     * @return
     */
    public DataBlock skewDiagonal(int r0, int c0, int n) {
        int inc = 1 - lda;
        int beg = start + c0 * lda + r0, end = beg + n * inc;
        return DataBlock.of(storage, beg, end, inc);
    }

    /**
     *
     * @param r0 Row index of the first cell
     * @param c0 Column index of the first cell
     * @param n Number of cells
     * @return
     */
    public DataBlock row(int r0, int c0, int n) {
        int beg = start + c0 * lda + r0, end = beg + n * lda;
        return DataBlock.of(storage, beg, end, lda);
    }

    /**
     *
     * @param r0 Row index of the first cell
     * @param c0 Column index of the first cell
     * @param n Number of cells
     * @return
     */
    public DataBlock column(int r0, int c0, int n) {
        int beg = start + c0 * lda + r0, end = beg + n;
        return DataBlock.of(storage, beg, end, 1);
    }

    /**
     * 
     * @return An array containing the sum of each rows (size=number of rows)
     */
    public DataBlock rowSums() {
        if (isEmpty()) {
            return DataBlock.EMPTY;
        }
        DataBlockIterator cols = columnsIterator();
        DataBlock x = cols.next().deepClone();
        while (cols.hasNext()) {
            x.add(cols.next());
        }
        return x;
    }

    /**
     * 
     * @return An array containing the sum of each columns (size=number of columns)
     */
    public DataBlock columnSums() {
        if (isEmpty()) {
            return DataBlock.EMPTY;
        }
        DataBlockIterator rows = rowsIterator();
        DataBlock x = rows.next().deepClone();
        while (rows.hasNext()) {
            x.add(rows.next());
        }
        return x;
    }

    public DataBlockIterator rowsIterator() {
        return new RCIterator(topOutside(), nrows, 1);
    }

    public DataBlockIterator reverseRowsIterator() {
        return new RCIterator(bottomOutside(), nrows, -1);
    }

    public DataBlockIterator columnsIterator() {
        return new RCIterator(leftOutside(), ncols, lda);
    }

    public DataBlockIterator reverseColumnsIterator() {
        return new RCIterator(rightOutside(), ncols, -lda);
    }

    public DataBlock topOutside() {
        int beg = start - 1,
                inc = lda;
        return DataBlock.of(storage, beg, beg + ncols * inc, inc);
    }

    public DataBlock leftOutside() {
        int beg = start - lda;
        return DataBlock.of(storage, beg, beg + nrows, 1);
    }

    public DataBlock bottomOutside() {
        int beg = start + nrows, inc = lda;

        return DataBlock.of(storage, beg, beg + ncols * inc, inc);
    }

    public DataBlock rightOutside() {
        int beg = start + lda * ncols;
        return DataBlock.of(storage, beg, beg + nrows, 1);
    }

    //</editor-fold>
    //</editor-fold>
    public FastMatrix plus(FastMatrix B) {
        FastMatrix AB = deepClone();
        AB.add(B);
        return AB;
    }

    public FastMatrix minus(FastMatrix B) {
        FastMatrix AB = deepClone();
        AB.sub(B);
        return AB;
    }

    public FastMatrix plus(double d) {
        FastMatrix AB = deepClone();
        AB.add(d);
        return AB;
    }

    public FastMatrix minus(double d) {
        FastMatrix AB = deepClone();
        AB.sub(d);
        return AB;
    }

    public FastMatrix times(double d) {
        FastMatrix AB = deepClone();
        AB.mul(d);
        return AB;
    }

    public FastMatrix dividedBy(double d) {
        FastMatrix AB = deepClone();
        AB.div(d);
        return AB;
    }

    //</editor-fold>
    /**
     * Shifts the matrix to the top-left corner. a(i,j) = a(i+n, j+n) for i in
     * [0, nrows-n[ and j in [0, ncols-n[ The cells that are not moved are not
     * modified
     *
     * @param n The displacement (n cells left and n cells up)
     */
    public void upLeftShift(final int n) {
        int del = (1 + lda) * n;
        for (int c = 0, i = start; c < ncols - n; ++c, i += lda) {
            for (int r = 0, j = i; r < nrows - n; ++r, j++) {
                storage[j] = storage[j + del];
            }
        }
    }

    /**
     * Shifts the matrix to the bottom-right corner a(i,j) = a(i-n, j-n) for i
     * in [n, nrows[ and j in [n, ncols[ The cells that are not moved are not
     * modified.
     *
     * @param n The displacement (n cells right and n cells down)
     */
    public void downRightShift(final int n) {
        int del = (1 + lda) * n;
        for (int c = n, i = start + (nrows - 1)
                + (ncols - 1) * lda; c < ncols; ++c, i -= lda) {
            for (int r = n, j = i; r < nrows; ++r, j--) {
                storage[j] = storage[j - del];
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="matrix windows">
    public MatrixWindow all() {
        return new MatrixWindow(storage, lda, start, nrows, ncols);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow topLeft(int nr, int nc) {
        return new MatrixWindow(storage, lda, start, nr, nc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow top(int nr) {
        return new MatrixWindow(storage, lda, start, nr, ncols);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow left(int nc) {
        return new MatrixWindow(storage, lda, start, nrows, nc);
    }

    /**
     * bottom-right sub-matrix.
     *
     * @return An empty sub-matrix
     */
    public MatrixWindow bottomRight() {
        int nstart = start + nrows + ncols * lda;
        return new MatrixWindow(storage, lda, nstart, 0, 0);
    }

    /**
     * Bottom-right sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow bottomRight(int nr, int nc) {
        int nstart = start + (nrows - nr) + (ncols - nc) * lda;
        return new MatrixWindow(storage, lda, nstart, nr, nc);
    }

    /**
     * Bottom sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return The last n rows
     */
    public MatrixWindow bottom(int nr) {
        return new MatrixWindow(storage, lda, start + nrows - nr, nr, ncols);
    }

    /**
     * right sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return The nc right columns
     */
    public MatrixWindow right(int nc) {
        return new MatrixWindow(storage, lda, start + (ncols - nc) * lda, nrows, nc);
    }

    public DataWindow top() {
        return DataWindow.windowOf(storage, start, start + ncols * lda, lda);
    }

    public DataWindow left() {
        return DataWindow.windowOf(storage, start, start + nrows, 1);
    }

    public DataWindow bottom() {
        int beg = start + (nrows - 1);
        return DataWindow.windowOf(storage, beg, beg + ncols * lda, lda);
    }

    public DataWindow right() {
        int beg = start + (ncols - 1) * lda;
        return DataWindow.windowOf(storage, beg, beg + nrows, 1);
    }

    //</editor-fold>  
    public String toString(String fmt) {
        return Matrix.format(this, fmt);
    }

    @Override
    public String toString() {
        return Matrix.format(this);
    }

    private static class Rows implements Iterator<DataBlock> {

        private int pos;
        private final FastMatrix M;

        Rows(FastMatrix M) {
            pos = 0;
            this.M = M;
        }

        @Override
        public boolean hasNext() {
            return pos < M.nrows;
        }

        @Override
        public DataBlock next() {
            return M.row(pos++);
        }
    }

    private static class Columns implements Iterator<DataBlock> {

        private int pos;
        private final FastMatrix M;

        Columns(FastMatrix M) {
            pos = 0;
            this.M = M;
        }

        @Override
        public boolean hasNext() {
            return pos < M.ncols;
        }

        @Override
        public DataBlock next() {
            return M.column(pos++);
        }
    }

    private static class RCIterator extends DataBlockIterator {

        private RCIterator(DataBlock block, int n, int inc) {
            super(block, n, inc);
        }
    }
}
