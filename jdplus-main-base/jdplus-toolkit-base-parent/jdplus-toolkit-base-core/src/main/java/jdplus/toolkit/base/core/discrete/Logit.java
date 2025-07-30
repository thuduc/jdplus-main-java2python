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
package jdplus.toolkit.base.core.discrete;

/**
 *
 * @author Jean Palate
 */
public class Logit implements ICumulativeDistributionFunction {

    @Override
    public double f(double x) {
        double e = Math.exp(-x);
        return 1 / (1 + e);
    }

    @Override
    public double df(double x) {
        // p = 1/(1+e(-y)) p'=-1/(1+e(-y))^2*(-e(-y))
        // p' = e(-y)/(1+e(-y))^2
        double e = Math.exp(-x);
        double d = 1 + e;
        return e / (d * d);
    }

    @Override
    public double d2f(double x) {
        // p'' = e(-y)(e(-y)-1)/(1+e(-y))^3
        double e = Math.exp(-x);
        double d = 1 + e;
        return e * (e - 1) / (d * d * d);
    }
}
