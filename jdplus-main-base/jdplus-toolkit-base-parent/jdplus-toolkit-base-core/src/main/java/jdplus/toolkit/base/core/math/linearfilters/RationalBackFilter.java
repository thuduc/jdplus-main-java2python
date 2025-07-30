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
package jdplus.toolkit.base.core.math.linearfilters;

import nbbrd.design.Development;
import nbbrd.design.Immutable;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.RationalFunction;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class RationalBackFilter implements IRationalFilter {

    public static final RationalBackFilter ZERO = new RationalBackFilter();

    private final RationalFunction rationalFunction;
    private final int bshift;

    /**
     *
     */
    public RationalBackFilter() {
        rationalFunction = RationalFunction.zero();
        bshift = 0;
    }

    /**
     *
     * @param num
     * @param denom
     * @param bshift
     */
    public RationalBackFilter(final BackFilter num, final BackFilter denom, final int bshift) {
        rationalFunction = RationalFunction.of(num.asPolynomial(), denom.asPolynomial());
        this.bshift = bshift;
    }

    public RationalBackFilter(final RationalFunction rfe, final int bshift) {
        rationalFunction = rfe;
        this.bshift = bshift;
    }

    /**
     *
     * @param nterms
     * @return
     */
    public RationalBackFilter drop(final int nterms) {
        RationalFunction rfe = rationalFunction.drop(nterms);
        return new RationalBackFilter(rfe, bshift + nterms);
    }

    /**
     *
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(double freq) {
        Polynomial pn = rationalFunction.getNumerator(), pd = rationalFunction.getDenominator();
        Complex n = FilterUtility.frequencyResponse(i -> pn.get(i - bshift), bshift, bshift + pn.degree(), -freq);
        Complex d = FilterUtility.frequencyResponse(i->pd.get(i), 0, rationalFunction.getDenominator().degree(), -freq);
        return n.div(d);
    }

    @Override
    public BackFilter getDenominator() {
        Polynomial p = rationalFunction.getDenominator();
        return new BackFilter(p);
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        if (rationalFunction.isFinite()) {
            return -rationalFunction.getNumerator().degree();
        } else {
            return Integer.MIN_VALUE;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public BackFilter getNumerator() {
        Polynomial p = rationalFunction.getNumerator();
        return new BackFilter(p);
    }

    /**
     *
     * @return
     */
    public RationalFunction getRationalFunction() {
        return rationalFunction;
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        return -bshift;
    }

    /**
     *
     * @param pos
     * @return
     */
    public double weight(int pos) {
        return rationalFunction.get(-bshift - pos);
    }

    public IntToDoubleFunction asFunction() {
        return pos -> weight(pos);
    }

    /**
     *
     * @param n
     * @return
     */
    public double[] getWeights(final int n) {
        return rationalFunction.coefficients(n);
    }

    @Override
    public boolean hasLowerBound() {
        return rationalFunction.isFinite();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasUpperBound() {
        return true;
    }

    /**
     *
     * @return
     */
    public RationalForeFilter mirror() {
        return new RationalForeFilter(rationalFunction, bshift);
    }

    /**
     *
     * @param n
     */
    public void prepare(final int n) {
        rationalFunction.prepare(n);
    }

    /**
     *
     * @param r
     * @return
     */
    public RationalBackFilter times(final RationalBackFilter r) {
        Polynomial ln = rationalFunction.getNumerator(), rn = r.rationalFunction.getNumerator();
        Polynomial ld = rationalFunction.getDenominator(), rd = r.rationalFunction.getDenominator();
        Polynomial.SimplifyingTool psmp = new Polynomial.SimplifyingTool();
        if (psmp.simplify(ln, rd)) {
            ln = psmp.getLeft();
            rd = psmp.getRight();
        }
        if (psmp.simplify(rn, ld)) {
            rn = psmp.getLeft();
            ld = psmp.getRight();
        }

        Polynomial n = ln.times(rn), d = ld.times(rd);

        RationalFunction rfe = RationalFunction.of(n, d);
        return new RationalBackFilter(rfe, bshift + r.bshift);
    }

    @Override
    public RationalBackFilter getRationalBackFilter() {
        return this;
    }

    @Override
    public RationalForeFilter getRationalForeFilter() {
        return RationalForeFilter.ZERO;
    }
}
