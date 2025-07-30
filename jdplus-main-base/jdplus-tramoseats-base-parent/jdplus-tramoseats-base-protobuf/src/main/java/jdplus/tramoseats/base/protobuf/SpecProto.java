/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.protobuf;

import jdplus.sa.base.protobuf.SaProtosUtility;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SpecProto {

    public Spec convert(TramoSeatsSpec spec) {
        return Spec.newBuilder()
                .setTramo(TramoProto.convert(spec.getTramo()))
                .setSeats(DecompositionProto.convert(spec.getSeats()))
                .setBenchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }

    public TramoSeatsSpec convert(Spec spec) {
        return TramoSeatsSpec.builder()
                .tramo(TramoProto.convert(spec.getTramo()))
                .seats(DecompositionProto.convert(spec.getSeats()))
                .benchmarking(SaProtosUtility.convert(spec.getBenchmarking()))
                .build();
    }
}
