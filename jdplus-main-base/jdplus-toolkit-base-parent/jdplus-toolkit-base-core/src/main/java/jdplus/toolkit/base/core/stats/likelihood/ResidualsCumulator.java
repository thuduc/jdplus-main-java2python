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
package jdplus.toolkit.base.core.stats.likelihood;

import jdplus.toolkit.base.api.DemetraException;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import nbbrd.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ResidualsCumulator {
    
    private static final double ZERO=1e-9; 

    DeterminantalTerm m_det = new DeterminantalTerm();
    double m_ssqerr;
    int m_n;

    /**
     * Creates a new instance of PredictionErrorDecomposition
     */
    public ResidualsCumulator() {
    }

    public void add(final DataBlock e, final FastMatrix var) {
        if (e.length() == 1) {
            add(e.get(0), var.get(0, 0));
        } else {
            FastMatrix l = var.deepClone();
            SymmetricMatrix.lcholesky(l);
            DataBlock el = DataBlock.of(e);
            // L^-1*e=el <-> e=L*el
            LowerTriangularMatrix.solveLx(l, el);
            DataBlock diag = l.diagonal();
            for (int i = 0; i < el.length(); ++i) {
                double r = diag.get(i);
                if (r != 0) {
                    addStd(el.get(i), r);
                }
            }
        }
    }

    /**
     *
     * @param e
     * @param var
     */
    public void add(final double e, final double var) {
        if (Math.abs(var) < ZERO) {
            if (Math.abs(e) < ZERO) {
                return;
            }else{
                throw new DemetraException();
            }
        }

        m_det.add(var);
        m_ssqerr += e * e / var;
        ++m_n;
    }

    /**
     *
     * @param e
     * @param stde
     */
    public void addStd(final double e, final double stde) {
        if (Math.abs(stde) < ZERO) {
            if (Math.abs(e) < ZERO) {
                return;
            }
        }

        m_det.add(stde * stde);
        m_ssqerr += e * e;
        ++m_n;
    }

    /**
     *
     */
    public void clear() {
        m_ssqerr = 0;
        m_det.clear();
        m_n = 0;
    }

    /**
     *
     * @return
     */
    public double getLogDeterminant() {
        return m_det.getLogDeterminant();
    }

    /**
     *
     * @return
     */
    public int getObsCount() {
        return m_n;
    }

    /**
     *
     * @return
     */
    public double getSsqErr() {
        return m_ssqerr;
    }

    /**
     *
     * @return 
     */
    public Likelihood evaluate() {
        return Likelihood.builder(m_n)
                .logDeterminant(m_det.getLogDeterminant())
                .ssqErr(m_ssqerr).build();
    }
}
