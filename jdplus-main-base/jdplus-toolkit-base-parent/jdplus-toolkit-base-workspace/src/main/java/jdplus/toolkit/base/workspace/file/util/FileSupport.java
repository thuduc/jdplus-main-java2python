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
package jdplus.toolkit.base.workspace.file.util;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import internal.toolkit.base.workspace.file.util.QuickHandler;
import java.io.IOException;
import java.nio.file.Path;

import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface FileSupport {

    @NonNull
    Path resolveFile(@NonNull Path root, @NonNull String fileName);

    @NonNull
    Object read(@NonNull Path root, @NonNull String fileName) throws IOException;

    void write(@NonNull Path root, @NonNull String fileName, @NonNull Object value) throws IOException;
    
    boolean match(DemetraVersion version);

    @NonNull
    default FamilyHandler asHandler(@NonNull WorkspaceFamily family) {
        return new QuickHandler(family, this);
    }
}
