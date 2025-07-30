/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.time;

import jdplus.toolkit.base.api.data.Range;
import lombok.NonNull;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

/**
 * Framework-level interface defining the intervening time between two time
 * points.
 *
 * @param <T>
 * @param <D>
 * @author Philippe Charles
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Time_intervals">Time
 * intervals in ISO_8601</a>
 */
@ISO_8601
public interface TimeInterval<T extends Temporal & Comparable<? super T>, D extends TemporalAmount> extends TimeIntervalAccessor, Range<T> {

    @NonNull
    D getDuration();
}
