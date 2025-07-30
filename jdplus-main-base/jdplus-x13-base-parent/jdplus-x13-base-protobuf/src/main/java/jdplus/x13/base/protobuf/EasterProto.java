/*
 * Copyright 2021 National Bank of Belgium
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
package jdplus.x13.base.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.x13.base.api.regarima.EasterSpec;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterProto {

    public void fill(EasterSpec spec, RegArimaSpec.EasterSpec.Builder builder) {
    }

    public RegArimaSpec.EasterSpec convert(EasterSpec spec) {
        return RegArimaSpec.EasterSpec.newBuilder()
                .setType(X13ProtosUtility.convert(spec.getType()))
                .setDuration(spec.getDuration())
                .setTest(X13ProtosUtility.convert(spec.getTest()))
                .setCoefficient(ToolkitProtosUtility.convert(spec.getCoefficient()))
                .build();
    }

    public byte[] toBuffer(EasterSpec spec) {
        return convert(spec).toByteArray();
    }

    public EasterSpec convert(RegArimaSpec.EasterSpec spec) {

        return EasterSpec.builder()
                .duration(spec.getDuration())
                .type(X13ProtosUtility.convert(spec.getType()))
                .test(X13ProtosUtility.convert(spec.getTest()))
                .coefficient(ToolkitProtosUtility.convert(spec.getCoefficient()))
                .build();
    }

    public EasterSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        RegArimaSpec.EasterSpec spec = RegArimaSpec.EasterSpec.parseFrom(bytes);
        return convert(spec);
    }

}
