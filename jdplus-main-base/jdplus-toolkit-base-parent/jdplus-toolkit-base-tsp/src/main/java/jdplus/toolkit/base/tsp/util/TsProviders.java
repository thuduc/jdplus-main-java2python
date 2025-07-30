/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import jdplus.toolkit.base.api.util.TreeTraverser;
import nbbrd.io.function.IOFunction;
import nbbrd.design.NonNegative;
import lombok.NonNull;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.function.Function;

/**
 * Utility class that simplify the use of Ts providers.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@lombok.experimental.UtilityClass
public class TsProviders {

    public @NonNull TreeTraverser<?> getTreeTraverser(@NonNull DataSourceProvider provider, @NonNull DataSource dataSource) {
        IOFunction<Object, Iterable<?>> children = o ->
                o instanceof DataSource
                        ? provider.children((DataSource) o)
                        : ((DataSet) o).getKind() == DataSet.Kind.COLLECTION ? provider.children((DataSet) o) : Collections.emptyList();

        return TreeTraverser.of(dataSource, children.asUnchecked());
    }

    public void prettyPrintTree(
            @NonNull DataSourceProvider provider,
            @NonNull DataSource dataSource,
            @NonNegative int maxLevel,
            @NonNull PrintStream printStream,
            boolean displayName) throws IOException {

        Function<Object, String> formatter = displayName
                ? o -> o instanceof DataSource ? provider.getDisplayName((DataSource) o) : " " + provider.getDisplayNodeName((DataSet) o)
                : o -> o instanceof DataSource ? provider.toMoniker((DataSource) o).getId() : " " + provider.toMoniker((DataSet) o).getId();

        try {
            getTreeTraverser(provider, dataSource).prettyPrintTo(printStream, maxLevel, formatter);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
}
