/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.modelling.highfreq;

import jdplus.toolkit.base.api.data.Parameter;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class EasterSpec implements Validatable<EasterSpec> {

    public static enum Type {
        UNUSED, EASTER, JULIANEASTER;
    };

    public static final int DEF_IDUR = 6;
    public static final Type DEF_TYPE = Type.UNUSED;

    boolean test;
    int duration;
    Type type;

    // optional coefficient.
    Parameter coefficient;

    public static final EasterSpec DEFAULT_UNUSED = EasterSpec.builder().build();
    public static final EasterSpec DEFAULT_USED = new Builder()
            .test(true)
            .type(Type.EASTER)
            .duration(DEF_IDUR)
            .build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .test(false)
                .type(Type.UNUSED)
                .duration(DEF_IDUR);
    }

    @Override
    public EasterSpec validate() throws IllegalArgumentException {
        if (duration <= 0 || duration > 15) {
            throw new IllegalArgumentException("Duration should be inside [1, 15]");
        }
        if (test && Parameter.isFixed(coefficient)) {
            throw new IllegalArgumentException("Fixed coefficient should not be used with testing");
        }
        return this;
    }

    public boolean isUsed() {
        return type != Type.UNUSED;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }

    public boolean isDefined() {
        return type != Type.UNUSED && !test;
    }

    public boolean isJulian() {
        return type == Type.JULIANEASTER;
    }

    public static EasterSpec none() {
        return DEFAULT_UNUSED;
    }

    public boolean hasFixedCoefficient() {
        return coefficient != null && coefficient.isFixed();
    }

    public static class Builder implements Validatable.Builder<EasterSpec> {
    }
}
