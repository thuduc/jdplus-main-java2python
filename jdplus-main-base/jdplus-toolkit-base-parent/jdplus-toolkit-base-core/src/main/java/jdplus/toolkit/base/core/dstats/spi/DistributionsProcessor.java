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
package jdplus.toolkit.base.core.dstats.spi;

import jdplus.toolkit.base.api.advanced.dstats.Distributions;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.dstats.Gamma;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.dstats.T;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(Distributions.Processor.class)
public class DistributionsProcessor implements Distributions.Processor {

    @Override
    public Distribution normal(double mean, double stdev) {
        return new DProcessor(new Normal(mean, stdev));
    }

    @Override
    public Distribution chi2(double degreesOfFreedom) {
        return new DProcessor(new Chi2(degreesOfFreedom));
    }

    @Override
    public Distribution t(double degreesOfFreedom) {
        return new DProcessor(new T(degreesOfFreedom));
    }

    @Override
    public Distribution f(double degreesOfFreedomOnNum, double degreesOfFreedomOnDenom) {
        return new DProcessor(new F(degreesOfFreedomOnNum, degreesOfFreedomOnDenom));
    }

    @Override
    public Distribution gamma(double alpha, double beta) {
        return new DProcessor(new Gamma(alpha, beta));
    }

}
