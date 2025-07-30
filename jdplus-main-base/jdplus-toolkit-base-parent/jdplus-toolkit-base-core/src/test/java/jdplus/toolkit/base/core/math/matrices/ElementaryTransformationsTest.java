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
package jdplus.toolkit.base.core.math.matrices;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixUtility;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.random.JdkRNG;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class ElementaryTransformationsTest {

    public ElementaryTransformationsTest() {
    }

    @Test
    public void testQRGivens() {
        JdkRNG rng = JdkRNG.newRandom(0);
        FastMatrix M = FastMatrix.make(20, 5);
        MatrixUtility.randomize(M, rng);
        FastMatrix cur = M;
        for (int i = 0; i < M.getColumnsCount() - 1; ++i) {
            ElementaryTransformations.columnGivens(cur);
            cur = cur.extract(1, cur.getRowsCount() - 1, 1, cur.getColumnsCount() - 1);
        }
        DataBlock b = M.column(4).range(0, 4);
        UpperTriangularMatrix.solveUx(M.extract(0, 4, 0, 4), b);
        System.out.println(b);

        M = FastMatrix.make(20, 5);
        MatrixUtility.randomize(M, rng);

        QRLeastSquaresSolution ls = QRLeastSquaresSolver.fastLeastSquares(M.column(4), M.extract(0, 20, 0, 4));
        DataBlock b2 = DataBlock.of(ls.getB());
//        System.out.println(b2);
    }

}
