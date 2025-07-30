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

import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.util.Validatable;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value

@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class RegArimaSpec implements Validatable<RegArimaSpec>, ProcSpecification {

    public static final String METHOD = "regarima";
    public static final String FAMILY = "Modelling";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final RegArimaSpec DEFAULT_ENABLED = RegArimaSpec.builder().build();
    public static final RegArimaSpec DEFAULT_DISABLED = RegArimaSpec.builder().basic(BasicSpec.DEFAULT_ENABLED).build();

    private BasicSpec basic;
    private TransformSpec transform;
    private RegressionSpec regression;
    private OutlierSpec outliers;
    private AutoModelSpec autoModel;
    private SarimaSpec arima;
    private EstimateSpec estimate;
    
    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor(){
        return DESCRIPTOR_V3;
    }

    @LombokWorkaround
    public static Builder builder() {
        SarimaSpec arima = SarimaSpec.airline();
        return new Builder()
                .basic(BasicSpec.builder().build())
                .transform(TransformSpec.builder().build())
                .estimate(EstimateSpec.builder().build())
                .autoModel(AutoModelSpec.builder().build())
                .outliers(OutlierSpec.builder().build())
                .arima(arima)
                .regression(RegressionSpec.builder().build());
    }

    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    @Override
    public RegArimaSpec validate() throws IllegalArgumentException {
        basic.validate();
        transform.validate();
        regression.validate();
        outliers.validate();
        autoModel.validate();
        estimate.validate();
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_ENABLED);
    }

    public static class Builder implements Validatable.Builder<RegArimaSpec> {

        public Builder usingAutoModel(boolean enableAutoModel) {
            this.autoModel = autoModel.toBuilder().enabled(enableAutoModel).build();
            return this;
        }

        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            if (this.autoModel == null) {
                this.autoModel = AutoModelSpec.builder().build();
            }
            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Default specifications">
    public static final RegArimaSpec RGDISABLED, RG0, RG1, RG2, RG3, RG4, RG5;

    public static final RegArimaSpec[] allSpecifications() {
        return new RegArimaSpec[]{RG0, RG1, RG2, RG3, RG4, RG5};
    }

    static {
        RGDISABLED = RegArimaSpec.builder()
                .basic(BasicSpec.builder().preprocessing(false).build())
                .build();

        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec easter = EasterSpec.builder()
                .easterSpec(true)
                .build();

        TradingDaysSpec wd = TradingDaysSpec.td(TradingDaysType.TD2, LengthOfPeriodType.LeapYear, RegressionTestSpec.Remove, true);

        TradingDaysSpec td = TradingDaysSpec.td(TradingDaysType.TD7, LengthOfPeriodType.LeapYear, RegressionTestSpec.Remove, true);

        RegressionSpec rwd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(wd)
                .build();

        RegressionSpec rtd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(td)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .type(new SingleOutlierSpec("AO", 0))
                .type(new SingleOutlierSpec("LS", 0))
                .type(new SingleOutlierSpec("TC",0))
                .build();

        RG0 = RegArimaSpec.DEFAULT_ENABLED;

        RG1 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .build();

        RG2 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .build();
        RG3 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .usingAutoModel(true)
                .build();
        RG4 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .usingAutoModel(true)
                .build();

        RG5 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rtd)
                .usingAutoModel(true)
                .build();
    }

    public static RegArimaSpec fromString(String name) {
        switch (name) {
            case "RG0":
            case "rg0":
                return RG0;
            case "RG1":
            case "rg1":
                return RG1;
            case "RG2c":
            case "rg2c":
            case "RG2":
            case "rg2":
                return RG2;
            case "RG3":
            case "rg3":
                return RG3;
            case "RG4c":
            case "rg4c":
            case "RG4":
            case "rg4":
                return RG4;
            case "RG5c":
            case "rg5c":
            case "RG5":
            case "rg5":
                return RG5;
            default:
                throw new RegArimaException();
        }
    }
    //</editor-fold>
    
    @Override
    public String display() {
        if (this == RG0) {
            return "RG0";
        }
        if (this == RG1) {
            return "RG1";
        }
        if (this == RG2) {
            return "RG2";
        }
        if (this == RG3) {
            return "RG3";
        }
        if (this == RG4) {
            return "RG4";
        }
        if (this == RG5) {
            return "RG5";
        }
        if (equals(RG0)) {
            return "RG0";
        }
        if (equals(RG1)) {
            return "RG1";
        }
        if (equals(RG2)) {
            return "RG2";
        }
        if (equals(RG3)) {
            return "RG3";
        }
        if (equals(RG4)) {
            return "RG4";
        }
        if (equals(RG5)) {
            return "RG5";
        }
        return SMETHOD;
    }

    private static final String SMETHOD = "RG";

     
}
