/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package jdplus.toolkit.base.api.timeseries;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

/**
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class TimePointObs implements TimeSeriesObs<TimePoint> {

    @lombok.NonNull
    TimePoint period;

    double value;

    @StaticFactoryMethod
    public static @NonNull TimePointObs parse(@NonNull CharSequence text) {
        int index = text.toString().indexOf("=");
        if (index < 0) {
            throw new IllegalArgumentException("Invalid TimePointObs text: " + text);
        }
        return of(
                TimePoint.parse(text.subSequence(0, index)),
                Double.parseDouble(text.subSequence(index + 1, text.length()).toString())
        );
    }

    @Override
    public String toString() {
        return period + "=" + value;
    }
}
