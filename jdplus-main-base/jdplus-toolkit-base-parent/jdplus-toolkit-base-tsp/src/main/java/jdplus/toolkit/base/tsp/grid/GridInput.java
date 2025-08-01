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
package jdplus.toolkit.base.tsp.grid;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface GridInput {

    @NonNull
    Set<GridDataType> getDataTypes();

    @NonNull
    String getName();

    @NonNull
    Stream open() throws IOException;

    interface Stream extends Closeable {

        boolean readCell() throws IOException;

        boolean readRow() throws IOException;

        @Nullable
        Object getCell() throws IOException;
    }
}
