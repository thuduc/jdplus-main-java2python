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
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.tramoseats.base.api.tramo.OutlierSpec;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class OutlierSpecMapping {

    final String SPAN = "span",
            TYPES = "types",
            VA = "va",
            EML = "eml",
            DELTATC = "deltatc";

//    static void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, SPAN), TimeSelector.class);
//        dic.put(InformationSet.item(prefix, EML), Boolean.class);
//        dic.put(InformationSet.item(prefix, TYPES), String[].class);
//        dic.put(InformationSet.item(prefix, VA), Double.class);
//        dic.put(InformationSet.item(prefix, DELTATC), Double.class);
//    }
//
    
    InformationSet write(OutlierSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        TimeSelector span = spec.getSpan();
        if (verbose || span.getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, span);
        }
        if (spec.isUsed()) {
            List<String> types = new ArrayList<>();
            if (spec.isAo()) {
                types.add(AdditiveOutlier.CODE);
            }
            if (spec.isLs()) {
                types.add(LevelShift.CODE);
            }
            if (spec.isTc()) {
                types.add(TransitoryChange.CODE);
            }
            if (spec.isSo()) {
                types.add(PeriodicOutlier.CODE);
            }
            info.add(TYPES, types.toArray(String[]::new));
        }
        double cv = spec.getCriticalValue();
        if (verbose || cv != 0) {
            info.add(VA, cv);
        }
        boolean eml = spec.isMaximumLikelihood();
        if (verbose || eml != OutlierSpec.DEF_EML) {
            info.add(EML, eml);
        }
        double tc = spec.getDeltaTC();
        if (verbose || tc != OutlierSpec.DEF_DELTATC) {
            info.add(DELTATC, tc);
        }
        return info;
    }

    OutlierSpec read(InformationSet info) {
        if (info == null) {
            return OutlierSpec.DEFAULT_DISABLED;
        }

        OutlierSpec.Builder builder = OutlierSpec.builder();

        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder = builder.span(span);
        }
        String[] types = info.get(TYPES, String[].class);
        if (types != null) {
            for (int i = 0; i < types.length; ++i) {
                switch (types[i]) {
                    case AdditiveOutlier.CODE:
                        builder = builder.ao(true);
                        break;
                    case LevelShift.CODE:
                        builder = builder.ls(true);
                        break;
                    case TransitoryChange.CODE:
                        builder = builder.tc(true);
                        break;
                    case PeriodicOutlier.CODE:
                        builder = builder.so(true);
                        break;
                }
            }
        }
        Double cv = info.get(VA, Double.class);
        if (cv != null) {
            builder = builder.criticalValue(cv);
        }
        Double tc = info.get(DELTATC, Double.class);
        if (tc != null) {
            builder = builder.deltaTC(tc);
        }
        Boolean eml = info.get(EML, Boolean.class);
        if (eml != null) {
            builder = builder.maximumLikelihood(eml);
        }

        return builder.build();
    }

}
