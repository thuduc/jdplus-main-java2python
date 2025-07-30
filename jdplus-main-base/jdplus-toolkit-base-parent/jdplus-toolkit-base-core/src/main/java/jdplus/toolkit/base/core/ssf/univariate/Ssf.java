/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.basic.TimeInvariantSsf;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.MeasurementError;

/**
 *
 * @author Jean Palate
 */
public class Ssf implements ISsf {

    private final ISsfInitialization initializer;
    private final ISsfMeasurement measurement;
    private final ISsfDynamics dynamics;

    public static Ssf of(StateComponent cmp, ISsfLoading loading, double measurementError) {
        return new Ssf(cmp.initialization(), cmp.dynamics(),
                new Measurement(loading, MeasurementError.of(measurementError)));
    }

    public static Ssf of(StateComponent cmp, ISsfLoading loading) {
        return new Ssf(cmp.initialization(), cmp.dynamics(),
                new Measurement(loading, null));
    }

    public static Ssf of(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfLoading loading) {
        return new Ssf(initializer, dynamics, new Measurement(loading, null));
    }

    public static Ssf of(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfLoading loading, double measurementError) {
        return new Ssf(initializer, dynamics, new Measurement(loading, MeasurementError.of(measurementError)));
    }

    public static Ssf of(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfLoading loading, ISsfError measurementError) {
        return new Ssf(initializer, dynamics, new Measurement(loading, measurementError));
    }

    /**
     *
     * @param initializer
     * @param dynamics
     * @param measurement
     */
    public Ssf(final ISsfInitialization initializer, final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        this.initializer = initializer;
        this.dynamics = dynamics;
        this.measurement = measurement;
    }

    @Override
    public ISsfInitialization initialization() {
        return initializer;
    }

    @Override
    public ISsfMeasurement measurement() {
        return measurement;
    }

    @Override
    public ISsfDynamics dynamics() {
        return dynamics;
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant() && measurement.isTimeInvariant();
    }

    @Override
    public String toString() {
        if (isTimeInvariant()) {
            return TimeInvariantSsf.toString(this);
        } else {
            return super.toString();
        }
    }

}
