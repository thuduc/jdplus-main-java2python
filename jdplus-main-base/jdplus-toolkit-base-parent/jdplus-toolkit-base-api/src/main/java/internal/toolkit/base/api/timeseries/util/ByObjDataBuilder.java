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
package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import lombok.AccessLevel;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @param <T>
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByObjDataBuilder<T> implements TsDataBuilder<T> {

    public static TsDataBuilder<LocalDateTime> fromDateTime(ObsGathering gathering, ObsCharacteristics[] characteristics) {
        return TsDataBuilderUtil.isValid(gathering)
                ? new ByObjDataBuilder<>(getDateTimeObsList(TsDataBuilderUtil.isOrdered(characteristics)), !gathering.isIncludeMissingValues(), TsDataBuilderUtil.getMaker(gathering))
                : new NoOpDataBuilder<>(TsDataBuilderUtil.INVALID_AGGREGATION);
    }

    private final ByObjObsList<T> obsList;
    private final boolean skipMissingValues;
    private final Function<ObsList, TsData> maker;

    @Override
    public TsDataBuilder clear() {
        obsList.clear();
        return this;
    }

    @Override
    public TsDataBuilder add(T date, Number value) {
        if (date != null) {
            if (value != null) {
                obsList.add(date, value.doubleValue());
            } else if (!skipMissingValues) {
                obsList.add(date, Double.NaN);
            }
        }
        return this;
    }

    @Override
    public TsData build() {
        return maker.apply(obsList);
    }

    private static ByObjObsList<LocalDateTime> getDateTimeObsList(boolean preSorted) {
        return preSorted
                ? new ByObjObsList.PreSorted<>(ByObjDataBuilder::getPeriodIdFunc, 32)
                : new ByObjObsList.Sortable<>(ByObjDataBuilder::getPeriodIdFunc, LocalDateTime::compareTo);
    }

    private static int getPeriodIdFunc(TsUnit unit, LocalDateTime reference, LocalDateTime date) {
        return (int) TsPeriod.idAt(reference, unit, date);
    }
}
