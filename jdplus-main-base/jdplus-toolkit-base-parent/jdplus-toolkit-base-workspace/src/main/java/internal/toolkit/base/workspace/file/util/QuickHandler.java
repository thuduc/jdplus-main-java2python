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
package internal.toolkit.base.workspace.file.util;

import jdplus.toolkit.base.workspace.WorkspaceFamily;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.toolkit.base.workspace.file.util.FileSupport;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public final class QuickHandler implements FamilyHandler {

    private final WorkspaceFamily family;

    @lombok.experimental.Delegate
    private final FileSupport fileSupport;

    public QuickHandler(WorkspaceFamily family, FileSupport fileSupport) {
        this.family = Objects.requireNonNull(family, "family");
        this.fileSupport = Objects.requireNonNull(fileSupport, "fileSupport");
    }

    @Override
    public WorkspaceFamily getFamily() {
        return family;
    }

}
