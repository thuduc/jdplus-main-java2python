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
package jdplus.tramoseats.base.information;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.tramoseats.base.api.tramo.EstimateSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class EstimateSpecMapping {

    final String SPAN = "span",
            EML = "eml",
            TOL = "tol",
            UBP = "ubp";

//    void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, EML), Boolean.class);
//        dic.put(InformationSet.item(prefix, TOL), Double.class);
//        dic.put(InformationSet.item(prefix, UBP), Double.class);
//        dic.put(InformationSet.item(prefix, SPAN), TimeSelector.class);
//    }
//

    InformationSet write(EstimateSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.set(SPAN, spec.getSpan());
        }
        if (verbose || spec.isMaximumLikelihood() != EstimateSpec.DEF_EML) {
            info.set(EML, spec.isMaximumLikelihood());
        }
        if (verbose || spec.getTol() != EstimateSpec.DEF_TOL) {
            info.set(TOL, spec.getTol());
        }
        if (verbose || spec.getUbp() != EstimateSpec.DEF_UBP) {
            info.set(UBP, spec.getUbp());
        }
        return info;
    }

    EstimateSpec read(InformationSet info) {
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
            builder.tol(tol);
        }
        Double ubp = info.get(UBP, Double.class);
        if (ubp != null) {
            builder.ubp(ubp);
        }
        Boolean eml = info.get(EML, Boolean.class);
        if (eml != null) {
            builder.maximumLikelihood(eml);
        }

        return builder.build();
    }
}
