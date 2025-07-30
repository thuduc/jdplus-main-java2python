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
package jdplus.x13.base.api.regarima;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.*;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class RegressionSpec implements Validatable<RegressionSpec> {

    public static final double DEF_AICCDIFF = 0;

    public static final RegressionSpec DEFAULT = RegressionSpec.builder().build();

    private double aicDiff;

    @lombok.NonNull
    private MeanSpec mean;
    @lombok.NonNull
    private TradingDaysSpec tradingDays;
    @lombok.NonNull
    private EasterSpec easter;
    @lombok.Singular
    private List<Variable<IOutlier>> outliers;
    @lombok.Singular
    private List<Variable<TsContextVariable>> userDefinedVariables;
    @lombok.Singular
    private List<Variable<InterventionVariable>> interventionVariables;
    @lombok.Singular
    private List<Variable<Ramp>> ramps;

    public static final RegressionSpec DEFAULT_UNUSED = RegressionSpec.builder().build(),
            DEFAULT_CONST = RegressionSpec.builder().mean(MeanSpec.DEFAULT_USED).build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .mean(MeanSpec.DEFAULT_UNUSED)
                .aicDiff(DEF_AICCDIFF)
                .easter(EasterSpec.DEFAULT_UNUSED)
                .tradingDays(TradingDaysSpec.none());
    }

    public boolean isUsed() {
        return mean.isUsed() || tradingDays.isUsed() || easter.isUsed()
                || !outliers.isEmpty() || !userDefinedVariables.isEmpty()
                || !ramps.isEmpty() || !interventionVariables.isEmpty();
    }

    public int getOutliersCount() {
        return outliers.size();
    }

    public int getRampsCount() {
        return ramps.size();
    }

    public int getInterventionVariablesCount() {
        return interventionVariables.size();
    }

    public int getUserDefinedVariablesCount() {
        return userDefinedVariables.size();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public RegressionSpec validate() throws IllegalArgumentException {
        tradingDays.validate();
        return this;
    }

    public static class Builder implements Validatable.Builder<RegressionSpec> {

    }

    public boolean hasFixedCoefficients() {
        if (!isUsed()) {
            return false;
        }
        return mean.hasFixedCoefficient() || tradingDays.hasFixedCoefficients()
                || easter.hasFixedCoefficient()
                || outliers.stream().anyMatch(var -> !var.isFree())
                || ramps.stream().anyMatch(var -> !var.isFree())
                || interventionVariables.stream().anyMatch(var -> !var.isFree())
                || userDefinedVariables.stream().anyMatch(var -> !var.isFree());
    }
}
