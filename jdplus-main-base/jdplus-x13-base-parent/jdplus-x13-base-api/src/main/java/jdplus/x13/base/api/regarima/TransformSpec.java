/*
* Copyright 2019 National Bank of Belgium
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

package jdplus.x13.base.api.regarima;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class TransformSpec implements Validatable<TransformSpec> {

    public static final boolean DEF_OUTLIERS=false;
    public static final LengthOfPeriodType DEF_ADJUST=LengthOfPeriodType.None;
    public static final TransformSpec DEFAULT_NONE = TransformSpec.builder().build();
    
    private TransformationType function;
    private boolean outliersCorrection;
    private LengthOfPeriodType adjust;
    private double aicDiff;
    private double constant;
    public static final double DEF_AICDIFF = -2;
    
    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .function(TransformationType.None)
                .outliersCorrection(DEF_OUTLIERS)
                .adjust(DEF_ADJUST)
                .aicDiff(DEF_AICDIFF);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_NONE);
    }

    @Override
    public TransformSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<TransformSpec> {
    }

}
