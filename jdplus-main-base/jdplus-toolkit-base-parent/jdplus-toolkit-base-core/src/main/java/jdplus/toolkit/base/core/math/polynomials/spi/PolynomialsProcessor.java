/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.core.math.polynomials.spi;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.api.advanced.math.Polynomials;
import jdplus.toolkit.base.api.advanced.math.Polynomials.Solver;
import jdplus.toolkit.base.core.math.polynomials.EigenValuesSolver;
import jdplus.toolkit.base.core.math.polynomials.FastEigenValuesSolver;
import jdplus.toolkit.base.core.math.polynomials.MullerNewtonSolver;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.RobustMullerNewtonSolver;
import jdplus.toolkit.base.core.math.polynomials.RootsSolver;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(Polynomials.Processor.class)
public class PolynomialsProcessor implements Polynomials.Processor {

    @Override
    public Complex[] rootsOf(DoubleSeq polynomial, Solver solver) {
        RootsSolver alg;
        switch (solver) {
            case MullerNewton:
                alg = new MullerNewtonSolver();
                break;
            case EigenValues:
                alg = new EigenValuesSolver();
                break;
            case FastEigenValues:
                alg = new FastEigenValuesSolver();
                break;
            case Robust:
                alg = new RobustMullerNewtonSolver();
                break;
            default:
                alg = new MullerNewtonSolver();
        }
        return Polynomial.ofInternal(polynomial.toArray()).roots(alg);

    }

    @Override
    public DoubleSeq zero() {
        return DoubleSeq.of(0);
    }

    @Override
    public DoubleSeq plus(DoubleSeq a, DoubleSeq b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleSeq opposite(DoubleSeq x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleSeq minus(DoubleSeq a, DoubleSeq b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleSeq one() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleSeq times(DoubleSeq a, DoubleSeq b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double evaluate(DoubleSeq polynomial, double x) {
        int n=polynomial.length()-1;
        DoubleSeqCursor cursor = polynomial.reverse().cursor();
        double f = cursor.getAndNext();
        for (int i=n-1; i >= 0; --i) {
            f = cursor.getAndNext() + (f * x);
        }
        return f;
    }

    @Override
    public Complex evaluate(DoubleSeq polynomial, Complex x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
