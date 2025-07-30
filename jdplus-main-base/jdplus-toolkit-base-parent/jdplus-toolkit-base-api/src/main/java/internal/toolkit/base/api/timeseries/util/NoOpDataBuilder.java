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
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class NoOpDataBuilder<T> implements TsDataBuilder<T> {

    private final TsData data;

    @Override
    public TsDataBuilder<T> clear() {
        return this;
    }

    @Override
    public TsDataBuilder<T> add(T date, Number value) {
        return this;
    }

    @Override
    public TsData build() {
        return data;
    }
}
