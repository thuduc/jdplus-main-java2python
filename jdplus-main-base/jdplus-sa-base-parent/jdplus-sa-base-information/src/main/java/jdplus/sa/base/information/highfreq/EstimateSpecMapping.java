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
package jdplus.sa.base.information.highfreq;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.highfreq.EstimateSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EstimateSpecMapping {

    final String SPAN = "span",
            TOL = "tol", HESSIAN ="hessian";

    public InformationSet write(EstimateSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.set(SPAN, spec.getSpan());
        }
        if (verbose || spec.getPrecision() != EstimateSpec.EPS) {
            info.set(TOL, spec.getPrecision());
        }
        if (verbose || !spec.isApproximateHessian()){
            info.set(HESSIAN, spec.isApproximateHessian());
        }
        return info;
    }

    public EstimateSpec read(InformationSet info) {
        if (info == null) {
            return EstimateSpec.DEFAULT;
        }

        EstimateSpec.Builder builder = EstimateSpec.builder();

        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder.span(span);
        }
        Double tol = info.get(TOL, Double.class);
        if (tol != null) {
            builder.precision(tol);
        }
        Boolean hessian = info.get(HESSIAN, Boolean.class);
        if (hessian != null) {
            builder.approximateHessian(hessian);
        }

        return builder.build();
    }
}
