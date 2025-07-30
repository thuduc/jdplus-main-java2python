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
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.tramoseats.base.api.tramo.TransformSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TransformSpecMapping {

    final String SPAN = "span",
            FN = "function",
            ADJUST = "adjust",
            FCT = "fct",
            OUTLIERS="outliers",
            PRELIMINARYCHECK = "preliminarycheck";

    InformationSet write(TransformSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, spec.getSpan());
        }
        if (verbose || spec.getFunction() != TransformationType.None) {
            info.add(FN, spec.getFunction().name());
        }
        if (verbose || spec.getFct() != TransformSpec.DEF_FCT) {
            info.add(FCT, spec.getFct());
        }
        if (verbose || spec.getAdjust() != TransformSpec.DEF_ADJUST) {
            info.add(ADJUST, spec.getAdjust().name());
        }
        if (verbose || spec.isPreliminaryCheck() != TransformSpec.DEF_CHECK) {
            info.add(PRELIMINARYCHECK, spec.isPreliminaryCheck());
        }
        if (verbose || spec.isOutliersCorrection() != TransformSpec.DEF_OUTLIERS) {
            info.add(OUTLIERS, spec.isOutliersCorrection());
        }
        return info;
    }

    TransformSpec read(InformationSet info) {
        if (info == null) {
            return TransformSpec.DEFAULT_UNUSED;
        }
        TransformSpec.Builder builder = TransformSpec.builder();
        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder = builder.span(span);
        }
        String fn = info.get(FN, String.class);
        if (fn != null) {
            builder = builder.function(TransformationType.valueOf(fn));
        }
        Double fct = info.get(FCT, Double.class);
        if (fct != null) {
            builder = builder.fct(fct);
        }
        String adjust = info.get(ADJUST, String.class);
        if (adjust != null) {
            builder.adjust(LengthOfPeriodType.valueOf(adjust));
        }
        Boolean preliminaryChecks = info.get(PRELIMINARYCHECK, Boolean.class);
        if (preliminaryChecks != null) {
            builder = builder.preliminaryCheck(preliminaryChecks);
        }
        Boolean outliers = info.get(OUTLIERS, Boolean.class);
        if (outliers != null) {
            builder = builder.outliersCorrection(outliers);
        }
        return builder.build();
    }

}
