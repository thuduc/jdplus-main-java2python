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
package jdplus.toolkit.base.core.regarima;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionTest;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.linearmodel.JointTest;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FRegressionTest implements IRegressionTest {

    private final double eps;

    public FRegressionTest(final double eps) {
        this.eps = eps;
    }

    /**
     * Tests that all the specified variables are jointly 0.
     *
     * @param ll
     * @param nhp
     * @param ireg
     * @param nregs
     * @param info
     * @return
     */
    @Override
    public boolean accept(ConcentratedLikelihoodWithMissing ll, int nhp, int ireg, int nregs) {
        StatisticalTest stat = new JointTest(ll)
                .variableSelection(ireg, nregs)
                .hyperParametersCount(nhp)
                .build();
        return Math.abs(stat.getPvalue())< eps;
    }
}
