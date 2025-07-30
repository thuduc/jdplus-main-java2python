/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.tramoseats.base.api.tramo;

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
public final class CalendarSpec implements Validatable<CalendarSpec> {
    
    public final static CalendarSpec DEFAULT_UNUSED = CalendarSpec.builder().build();
    
    public static final String TD = "td", EASTER = "easter";
    
    @lombok.NonNull
    private TradingDaysSpec tradingDays;
    
    @lombok.NonNull
    private EasterSpec easter;
    
    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .tradingDays(TradingDaysSpec.none())
                .easter(EasterSpec.DEFAULT_UNUSED);
    }
    
    public boolean isUsed() {
        return easter.isUsed() || tradingDays.isUsed();
    }
    
    public boolean isDefault() {
        return this.equals(DEFAULT_UNUSED);
    }
    
    @Override
    public CalendarSpec validate() throws IllegalArgumentException {
        easter.validate();
        return this;
    }
    
    boolean hasFixedCoefficients() {
        return easter.hasFixedCoefficient() || tradingDays.hasFixedCoefficients();
    }
    
    public static class Builder implements Validatable.Builder<CalendarSpec> {
    }
}
