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
package jdplus.toolkit.base.core.regsarima.regular;

import nbbrd.design.Development;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TRegressionTest implements IRegressionTest {

    private final double tlow, thigh;
    private final int nsig;

    public TRegressionTest(double tlow, double thigh) {
        this.tlow = tlow;
        this.thigh = thigh;
        nsig = 2;
    }

    public TRegressionTest(double tsig) {
        tlow = tsig;
        thigh = tsig;
        nsig = 1;
    }

    @Override
    public boolean accept(ConcentratedLikelihoodWithMissing ll, int nhp, int ireg, int nregs) {
        double[] t = ll.tstats(nhp < 0 ? 0 : nhp, nhp >= 0);
        int nlow = 0, nhigh = 0;
        for (int i = 0; i < nregs; ++i) {
            double ct = Math.abs(t[ireg + i]);
            if (ct >= thigh) {
                ++nhigh;
            } else if (ct >= tlow) {
                ++nlow;
            }
        }
        return nhigh > 0 || nlow >= Math.min(nsig, nregs);
    }
}
