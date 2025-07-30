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

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.information.InformationSetSerializerEx;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSpecMapping {

    public static final InformationSetSerializerEx<TramoSpec, TsDomain> SERIALIZER_V3 = new InformationSetSerializerEx<TramoSpec, TsDomain>() {
        @Override
        public InformationSet write(TramoSpec object, TsDomain context, boolean verbose) {
            return TramoSpecMapping.write(object, context, verbose);
        }

        @Override
        public TramoSpec read(InformationSet info, TsDomain context) {
            return TramoSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }

    };

    public static final InformationSetSerializerEx<TramoSpec, TsDomain> SERIALIZER_LEGACY = new InformationSetSerializerEx<TramoSpec, TsDomain>() {
        @Override
        public InformationSet write(TramoSpec object, TsDomain context, boolean verbose) {
            return TramoSpecMapping.writeLegacy(object, context, verbose);
        }

        @Override
        public TramoSpec read(InformationSet info, TsDomain context) {
            return TramoSpecMapping.readLegacy(info, context);
        }

        @Override
        public boolean match(DemetraVersion version){
            return version == DemetraVersion.JD2;
        }
    };

    public static final String TRANSFORM = "transform",
            AUTOMDL = "automdl", ARIMA = "arima",
            REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE_OLD = "esimate", ESTIMATE = "estimate";

//    public static void fillDictionary(String prefix, Map<String, Class> dic) {
//        EstimateSpecMapping.fillDictionary(InformationSet.item(prefix, ESTIMATE), dic);
//        TransformSpecMapping.fillDictionary(InformationSet.item(prefix, TRANSFORM), dic);
//        AutoModelSpecMapping.fillDictionary(InformationSet.item(prefix, AUTOMDL), dic);
//        ArimaSpecMapping.fillDictionary(InformationSet.item(prefix, ARIMA), dic);
//        OutlierSpecMapping.fillDictionary(InformationSet.item(prefix, OUTLIER), dic);
//        RegressionSpecMapping.fillDictionary(InformationSet.item(prefix, REGRESSION), dic);
//    }
//
    public TramoSpec read(InformationSet info, TsDomain context) {
        if (info == null) {
            return TramoSpec.DEFAULT;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc != null && desc.equals(TramoSpec.DESCRIPTOR_LEGACY)) {
            return readLegacy(info, context);
        } else {
            return readV3(info, context);
        }
    }

    public TramoSpec readV3(InformationSet info, TsDomain context) {
        if (info == null) {
            return TramoSpec.DEFAULT;
        }
        InformationSet estimate = info.getSubSet(ESTIMATE);
        if (estimate == null)
            estimate = info.getSubSet(ESTIMATE_OLD);
        return TramoSpec.builder()
                .transform(TransformSpecMapping.read(info.getSubSet(TRANSFORM)))
                .arima(ArimaSpecMapping.read(info.getSubSet(ARIMA)))
                .autoModel(AutoModelSpecMapping.read(info.getSubSet(AUTOMDL)))
                .outliers(OutlierSpecMapping.read(info.getSubSet(OUTLIER)))
                .regression(RegressionSpecMapping.read(info.getSubSet(REGRESSION)))
                .estimate(EstimateSpecMapping.read(estimate))
                .build();
    }

    public InformationSet write(TramoSpec spec, TsDomain context, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.set(ProcSpecification.ALGORITHM, TramoSpec.DESCRIPTOR_V3);
        InformationSet tinfo = TransformSpecMapping.write(spec.getTransform(), verbose);
        if (tinfo != null) {
            specInfo.set(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = ArimaSpecMapping.write(spec.getArima(), verbose);
        if (arimainfo != null) {
            specInfo.set(ARIMA, arimainfo);
        }
        InformationSet amiinfo = AutoModelSpecMapping.write(spec.getAutoModel(), verbose);
        if (amiinfo != null) {
            specInfo.set(AUTOMDL, amiinfo);
        }
        InformationSet outlierinfo = OutlierSpecMapping.write(spec.getOutliers(), verbose);
        if (outlierinfo != null) {
            specInfo.set(OUTLIER, outlierinfo);
        }
        InformationSet reginfo = RegressionSpecMapping.write(spec.getRegression(), context, verbose);
        if (reginfo != null) {
            specInfo.set(REGRESSION, reginfo);
        }
        InformationSet estimateinfo = EstimateSpecMapping.write(spec.getEstimate(), verbose);
        if (estimateinfo != null) {
            specInfo.set(ESTIMATE, estimateinfo);
        }
        return specInfo;
    }

    public TramoSpec readLegacy(InformationSet info, TsDomain context) {
        TramoSpec.Builder builder = TramoSpec.builder();
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        InformationSet oinfo = info.getSubSet(OUTLIER);
        InformationSet ainfo = info.getSubSet(ARIMA);
        InformationSet amiinfo = info.getSubSet(AUTOMDL);
        InformationSet einfo = info.getSubSet(ESTIMATE_OLD);
        if (einfo == null)
            einfo = info.getSubSet(ESTIMATE);
        InformationSet rinfo = info.getSubSet(REGRESSION);
        if (tinfo != null) {
            builder.transform(TransformSpecMapping.read(tinfo));
        }
        if (oinfo != null) {
            builder.outliers(OutlierSpecMapping.read(oinfo));
        }
        SarimaSpec.Builder ab = SarimaSpec.builder() //                .validator(SarimaValidator.VALIDATOR)
                ;
        RegressionSpec.Builder rb = RegressionSpec.builder();
        if (ainfo != null) {
            ArimaSpecMapping.readLegacy(ainfo, ab, rb);
            builder.arima(ab.build());
        }
        if (amiinfo != null) {
            builder.autoModel(AutoModelSpecMapping.read(amiinfo));
        }
        if (einfo != null) {
            builder.estimate(EstimateSpecMapping.read(einfo));
        }
        if (rinfo != null) {
            RegressionSpecMapping.readLegacy(rinfo, context, rb);
        }
        builder.regression(rb.build());
        return builder.build();
    }

    public InformationSet writeLegacy(TramoSpec spec, TsDomain context,boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.set(ProcSpecification.ALGORITHM, TramoSpec.DESCRIPTOR_LEGACY);
        InformationSet tinfo = TransformSpecMapping.write(spec.getTransform(), verbose);
        if (tinfo != null) {
            specInfo.set(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = ArimaSpecMapping.writeLegacy(spec.getArima(), spec.getRegression(), verbose);
        if (arimainfo != null) {
            specInfo.set(ARIMA, arimainfo);
        }
        InformationSet amiinfo = AutoModelSpecMapping.write(spec.getAutoModel(), verbose);
        if (amiinfo != null) {
            specInfo.set(AUTOMDL, amiinfo);
        }
        InformationSet outlierinfo = OutlierSpecMapping.write(spec.getOutliers(), verbose);
        if (outlierinfo != null) {
            specInfo.set(OUTLIER, outlierinfo);
        }
        InformationSet reginfo = RegressionSpecMapping.writeLegacy(spec.getRegression(), context, verbose);
        if (reginfo != null) {
            specInfo.set(REGRESSION, reginfo);
        }
        InformationSet estimateinfo = EstimateSpecMapping.write(spec.getEstimate(), verbose);
        if (estimateinfo != null) {
            specInfo.set(ESTIMATE_OLD, estimateinfo);
        }
        return specInfo;
    }

}
