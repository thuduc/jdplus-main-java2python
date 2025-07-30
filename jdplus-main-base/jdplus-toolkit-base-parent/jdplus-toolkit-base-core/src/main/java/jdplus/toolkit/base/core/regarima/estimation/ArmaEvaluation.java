/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.toolkit.base.core.regarima.estimation;

import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunction;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunctionPoint;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * @author Jean Palate
 * @param <S> Specific arima model type
 */
@Development(status = Development.Status.Alpha)
class ArmaEvaluation<S extends IArimaModel> implements ISsqFunctionPoint,
        IFunctionPoint {

    final ArmaFunction<S> fn;
    final DoubleSeq p;
    final S arma;
    final ConcentratedLikelihoodWithMissing ll;

    public ArmaEvaluation(ArmaFunction<S> fn, DoubleSeq p) {
        this.fn = fn;
        this.p = p;
        this.arma = fn.mapping.map(p);
        RegArmaModel<S> regarma = new RegArmaModel<>(fn.dy, arma, fn.nmissing, fn.x);
        ll = fn.cll.compute(regarma);
    }

    @Override
    public DoubleSeq getE() {
        return fn.errors.apply(ll);
    }

//    public ConcentratedLikelihoodWithMissing getLikelihood() {
//        return ll;
//    }

    @Override
    public DoubleSeq getParameters() {
        return p;
    }

    @Override
    public double getSsqE() {
        return fn.ssqll.applyAsDouble(ll);
    }

    @Override
    public double getValue() {
        return fn.ll.applyAsDouble(ll);
    }

    @Override
    public ISsqFunction getSsqFunction() {
        return fn;
    }

    @Override
    public IFunction getFunction() {
        return fn;
    }
}
