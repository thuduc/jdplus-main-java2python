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
package jdplus.tramoseats.base.api.tramo;

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.util.Validatable;
import lombok.NonNull;

/**
 * Defines the specifications for the Tramo pre-processing, which is
 * an(automatic)regarima modelling
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true,  buildMethodName = "buildWithoutValidation")
public final class TramoSpec implements Validatable<TramoSpec>, ProcSpecification {

    public static final String METHOD = "tramo";
    public static final String FAMILY = "Modelling";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final TramoSpec DEFAULT = TramoSpec.builder().build();

    /**
     * Gets the predefined Arima specification. The AutoModel and the Arima
     * properties are mutually exclusive specifications: Related TRAMO options:
     * P, D, Q, BP, BD, BQ, IMEAN, JPR, JPS, JQR, JQS, INIT.
     *
     * @return the Arima specifications
     */
    @lombok.NonNull
    private SarimaSpec arima;

    /**
     * Gets the specifications related to the transformation of the original
     * series. Related TRAMO options: LAM, FCT, UNITS.
     * -- SETTER --
     * Sets the specifications related to the transformation of the original
     * series.
     *
     * @return The transform specifications
     * @param transform The new transform specifications. Should not be null
     */
    @lombok.NonNull
    private TransformSpec transform;

    /**
     * Gets the specifications related to the automatic ARIMA modelling (AMI).
     * The AutoModelSpec and the ArimaSpec are mutually exclusive
     * specifications: Related TRAMO options: IDIF, INIC, UB1, UB2, CANCEL,
     * TSIG, PR, PCR.
     *
     * @return The AMI specifications
     */
    @lombok.NonNull
    private AutoModelSpec autoModel;

    /**
     * Gets options related to the estimation routine Related TRAMO options:
     * TOL, TYPE.
     * -- SETTER --
     * Sets new options for the estimation routine.
     *
     * @param estimate The new options
     * @return
     */
    @lombok.NonNull
    private EstimateSpec estimate;

    /**
     * Gets the options for the automatic outliers detection Related TRAMO
     * options: IATIP, AIO, TC, VA
     * -- SETTER --
     * Sets the options for the automatic outliers detection.
     *
     * @return the options for automatic outliers detection.
     * @param outliers The new specifications.
     */
    @lombok.NonNull
    private OutlierSpec outliers;

    /**
     * Gets the specifications for the regression model (including calendar
     * effects).
     * -- SETTER --
     * Sets the specifications for the regression model.
     *
     * @param regression The new regression specifications.
     * @return The specifications for the regression model.
     */
    @lombok.NonNull
    private RegressionSpec regression;

    /**
     * Creates a new default specification builder. No transformation, no
     * regression
     * variables, no outliers detection, default airline model (without mean).
     *
     * @return the builder initialized with default parameters
     */
    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .transform(TransformSpec.DEFAULT_UNUSED)
                .estimate(EstimateSpec.DEFAULT)
                .autoModel(AutoModelSpec.DEFAULT_DISABLED)
                .outliers(OutlierSpec.DEFAULT_DISABLED)
                .arima(SarimaSpec.airline())
                .regression(RegressionSpec.DEFAULT_CONST);
    }

    @Override
    public TramoSpec validate() throws IllegalArgumentException {
        autoModel.validate();
        estimate.validate();
        outliers.validate();
        regression.validate();
        transform.validate();
        return this;
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor(){
        return DESCRIPTOR_V3;
    }
    /**
     * Checks that the AMI is enabled
     *
     * @return true if the AMI spec is enabled, false otherwise.
     */
    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<TramoSpec> {

        /**
         * Enables/disables the AMI.
         *
         * @param enableAutoModel
         * @return 
         */
        public Builder usingAutoModel(boolean enableAutoModel) {
            if (autoModel.isEnabled() != enableAutoModel) {
                if (enableAutoModel) {
                    autoModel = AutoModelSpec.DEFAULT_ENABLED;
                } else {
                    autoModel = AutoModelSpec.DEFAULT_DISABLED;
                }
            }
            return this;
        }

        /**
         * Sets a predefined Sarima specification. The AMI is disabled.
         *
         * @param sarima new Sarima spec
         * @return
         */
        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            this.autoModel = AutoModelSpec.DEFAULT_DISABLED;

            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Default Specifications">
    public static final TramoSpec TR0, TR1, TR2, TR3, TR4, TR5, TRfull;

    public static final TramoSpec[] allSpecifications() {
        return new TramoSpec[]{TR0, TR1, TR2, TR3, TR4, TR5, TRfull};
    }

    static {
        TR0 = TramoSpec.DEFAULT;

        EasterSpec e = EasterSpec.builder()
                .type(EasterSpec.Type.Standard)
                .test(true)
                .build();

        TradingDaysSpec wd = TradingDaysSpec.td(TradingDaysType.TD2,
                LengthOfPeriodType.LeapYear, RegressionTestType.Separate_T, false);

        TradingDaysSpec td = TradingDaysSpec.td(TradingDaysType.TD7,
                LengthOfPeriodType.LeapYear, RegressionTestType.Separate_T, false);

        TradingDaysSpec dc = TradingDaysSpec.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.FTEST,
                TradingDaysSpec.DEF_PFTD, false);

        EasterSpec ec = e.toBuilder()
                .type(EasterSpec.Type.IncludeEaster)
                .test(true)
                .build();

        CalendarSpec cwd = CalendarSpec.builder()
                .easter(e)
                .tradingDays(wd)
                .build();
        CalendarSpec ctd = CalendarSpec.builder()
                .easter(e)
                .tradingDays(td)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .ao(true)
                .tc(true)
                .ls(true).build();

        TR1 = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .build();

        TR2 = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder()
                        .mean(MeanSpec.DEFAULT_USED)
                        .calendar(cwd)
                        .build())
                .build();

        TR3 = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .usingAutoModel(true)
                .build();

        TR4 = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder()
                        .calendar(cwd)
                        .build())
                .usingAutoModel(true)
                .build();


        TR5 = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder()
                        .calendar(ctd)
                        .build())
                .usingAutoModel(true)
                .build();

        CalendarSpec cc = CalendarSpec.builder()
                .easter(ec)
                .tradingDays(dc)
                .build();
        RegressionSpec rc = RegressionSpec.builder()
                .calendar(cc)
                .build();
        TRfull = TramoSpec.builder()
                .transform(TransformSpec.DEFAULT_AUTO)
                .outliers(o)
                .regression(rc)
                .usingAutoModel(true)
                .build();
    }

    public static TramoSpec fromString(String name) {
        switch (name) {
            case "TR0", "tr0" -> {
                return TR0;
            }
            case "TR1", "tr1" -> {
                return TR1;
            }
            case "TR2", "tr2" -> {
                return TR2;
            }
            case "TR3", "tr3" -> {
                return TR3;
            }
            case "TR4", "tr4" -> {
                return TR4;
            }
            case "TR5", "tr5" -> {
                return TR5;
            }
            case "TRfull", "trfull" -> {
                return TRfull;
            }
            default -> throw new TramoException();
        }
    }
    //</editor-fold>

    @Override
    public String display() {
        if (this == TR0) {
            return "TR0";
        }
        if (this == TR1) {
            return "TR1";
        }
        if (this == TR2) {
            return "TR2";
        }
        if (this == TR3) {
            return "TR3";
        }
        if (this == TR4) {
            return "TR4";
        }
        if (this == TR5) {
            return "TR5";
        }
        if (this == TRfull) {
            return "TRfull";
        }
        if (equals(TR0)) {
            return "TR0";
        }
        if (equals(TR1)) {
            return "TR1";
        }
        if (equals(TR2)) {
            return "TR2";
        }
        if (equals(TR3)) {
            return "TR3";
        }
        if (equals(TR4)) {
            return "TR4";
        }
        if (equals(TR5)) {
            return "TR5";
        }
        if (equals(TRfull)) {
            return "TRfull";
        }
        return SMETHOD;
    }

    private static final String SMETHOD = "TR";

}
