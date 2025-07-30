/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.toolkit.base.tsp;

import jdplus.toolkit.base.api.timeseries.TsMoniker;
import internal.toolkit.base.tsp.InternalTsProvider;
import nbbrd.design.ThreadSafe;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import lombok.NonNull;

import java.util.Optional;

/**
 * Defines the ability to convert a moniker from/to a DataSource or a DataSet.
 * Note that the implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataMoniker {

    /**
     * Creates a moniker from a DataSource. The resulting moniker can be used to
     * retrieve data.
     *
     * @param dataSource
     * @return a non-null moniker.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     */
    @NonNull
    TsMoniker toMoniker(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Creates a moniker from a DataSet. The resulting moniker can be used to
     * retrieve data.
     *
     * @param dataSet
     * @return a non-null moniker.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     */
    @NonNull
    TsMoniker toMoniker(@NonNull DataSet dataSet) throws IllegalArgumentException;

    /**
     * Creates a DataSource from a moniker.
     *
     * @param moniker a non-null moniker
     * @return an optional data source
     * @throws IllegalArgumentException if the moniker doesn't belong to this
     *                                  provider.
     */
    @NonNull Optional<DataSource> toDataSource(@NonNull TsMoniker moniker) throws IllegalArgumentException;

    /**
     * Creates a DataSet from a moniker.
     *
     * @param moniker a non-null moniker
     * @return an optional dataset
     * @throws IllegalArgumentException if the moniker doesn't belong to this
     *                                  provider.
     */
    @NonNull Optional<DataSet> toDataSet(@NonNull TsMoniker moniker) throws IllegalArgumentException;

    /**
     * Creates a new instance of HasDataMoniker using uri parser/formatter.
     *
     * @param providerName a non-null provider name
     * @return a non-null instance
     */
    @NonNull
    static HasDataMoniker usingUri(@NonNull String providerName) {
        return new InternalTsProvider.DataMonikerSupport(providerName,
                Formatter.of(DataSource::toString), Formatter.of(DataSet::toString),
                Parser.of(DataSource::parse), Parser.of(DataSet::parse)
        );
    }
}
