/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.api.modelling.highfreq;

import jdplus.toolkit.base.api.modelling.TransformationType;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class TransformSpec {

    public static final TransformSpec DEFAULT = builder().build(),
            DEF_AUTO = TransformSpec.builder()
                    .function(TransformationType.Auto)
                    .build();

    @lombok.NonNull
    private TransformationType function;
    private double aicDiff;

    public static Builder builder() {
        return new Builder().function(TransformationType.None);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

}
