/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.protobuf;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;
import jdplus.tramoseats.base.api.tramo.EasterSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EasterProto {

    public void fill(EasterSpec spec, TramoSpec.EasterSpec.Builder builder) {
        builder.setType(TramoSeatsProtosUtility.convert(spec.getType()))
                .setDuration(spec.getDuration())
                .setJulian(spec.isJulian())
                .setTest(spec.isTest());
    }

    public TramoSpec.EasterSpec convert(EasterSpec spec) {
        TramoSpec.EasterSpec.Builder builder = TramoSpec.EasterSpec.newBuilder();
        fill(spec, builder);
        Parameter c = spec.getCoefficient();
        if (c != null)
            builder.setCoefficient(ToolkitProtosUtility.convert(c));
        return builder.build();
    }

    public EasterSpec convert(TramoSpec.EasterSpec spec) {
        EasterSpec.Builder builder=EasterSpec.builder()
                .duration(spec.getDuration())
                .type(TramoSeatsProtosUtility.convert(spec.getType()))
                .test(spec.getTest())
                .julian(spec.getJulian());
        if (spec.hasCoefficient())
            builder.coefficient(ToolkitProtosUtility.convert(spec.getCoefficient()));
        return builder.build();
    }

}
