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

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.DefaultLikelihoodEvaluation;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;

/**
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Alpha)
public class ArmaFunction<S extends IArimaModel> implements ISsqFunction, IFunction {

    @BuilderPattern(ArmaFunction.class)
    public static class Builder<S extends IArimaModel> {

        public static final ToDoubleFunction<Likelihood> LL = DefaultLikelihoodEvaluation.ml();
        public static final Function<Likelihood, DoubleSeq> ERRORS = DefaultLikelihoodEvaluation.v();
        public static final ToDoubleFunction<Likelihood> SSQLL = DefaultLikelihoodEvaluation.deviance();

        // algorithms
        private ToDoubleFunction<Likelihood> ll = LL;
        private ToDoubleFunction<Likelihood> ssqll = SSQLL;
        private Function<Likelihood, DoubleSeq> errors = ERRORS;
        private ConcentratedLikelihoodComputer cll = ConcentratedLikelihoodComputer.DEFAULT_FULL_COMPUTER;
        private boolean mt = false;
        // model
        private final DoubleSeq dy;
        private FastMatrix x;
        private int nmissing;
        // mapping
        private IParametricMapping<S> mapping;

        private Builder(final DoubleSeq dy) {
            this.dy = dy;
        }

        public Builder variables(FastMatrix x) {
            this.x = x;
            return this;
        }

        public Builder nmissing(int nm) {
            this.nmissing = nm;
            return this;
        }

        public Builder parallelProcessing(boolean parallel) {
            this.mt = parallel;
            return this;
        }

        public Builder evaluation(ToDoubleFunction<Likelihood> ll) {
            this.ll = ll;
            return this;
        }

     public Builder ssqEvaluation(ToDoubleFunction<Likelihood> ssqll) {
            this.ssqll = ssqll;
            return this;
        }

     public Builder evaluation(Function<Likelihood, DoubleSeq> errors) {
            this.errors = errors;
            return this;
        }

        public Builder computer(ConcentratedLikelihoodComputer computer) {
            this.cll = computer;
            return this;
        }

        public ArmaFunction<S> build() {
            return new ArmaFunction<>(dy, x, nmissing, mapping, cll, ll, ssqll, errors, mt);
        }

    }

    public static <S extends IArimaModel> Builder<S> builder(DoubleSeq dy) {
        return new Builder<>(dy);
    }

    // model
    final DoubleSeq dy;
    final FastMatrix x;
    final int nmissing;
    // mapping
    final IParametricMapping<S> mapping;
    // algorithms
    final ConcentratedLikelihoodComputer cll;
    final ToDoubleFunction<Likelihood> ll, ssqll;
    final Function<Likelihood, DoubleSeq> errors;
    final boolean mt;

    private ArmaFunction(final DoubleSeq dy,
            final FastMatrix x,
            final int nm,
            final IParametricMapping<S> mapping,
            final ConcentratedLikelihoodComputer cll,
            final ToDoubleFunction<Likelihood> ll,
            final ToDoubleFunction<Likelihood> ssqll,
            final Function<Likelihood, DoubleSeq> errors,
            final boolean mt) {
        this.dy = dy;
        this.x = x;
        this.nmissing = nm;
        this.mapping = mapping;
        this.cll = cll;
        this.ll = ll;
        this.ssqll=ssqll;
        this.errors = errors;
        this.mt=mt;
    }

    @Override
    public ArmaEvaluation<S> evaluate(DoubleSeq parameters) {
         return new ArmaEvaluation<>(this, parameters);
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public ArmaEvaluation<S> ssqEvaluate(DoubleSeq parameters) {
        return new ArmaEvaluation<>(this, parameters);
    }

}
