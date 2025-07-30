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

import nbbrd.design.ThreadSafe;
import internal.toolkit.base.tsp.InternalTsProvider;
import java.util.LinkedHashSet;
import java.util.function.Consumer;
import lombok.NonNull;

/**
 * Defines the ability to modify a list of data sources. Note that the
 * implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataSourceMutableList extends HasDataSourceList {

    /**
     * Adds a new DataSource to the provider.
     *
     * @param dataSource
     * @return true if the DataSource has been added to the provider, false
     * otherwise.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     */
    boolean open(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Removes a DataSource from the provider.
     *
     * @param dataSource
     * @return true if the DataSource has been removed from the provider, false
     * otherwise.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     */
    boolean close(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Removes all the DataSources from this provider.
     */
    default void closeAll() {
        getDataSources().forEach(this::close);
    }

    @NonNull
    static HasDataSourceMutableList of(@NonNull String providerName, @NonNull Consumer<? super DataSource> cacheCleaner) {
        return new InternalTsProvider.DataSourceMutableListSupport(providerName, new LinkedHashSet<>(), cacheCleaner);
    }

    @NonNull
    static HasDataSourceMutableList of(@NonNull String providerName) {
        return of(providerName, InternalTsProvider.DO_NOTHING);
    }
}
