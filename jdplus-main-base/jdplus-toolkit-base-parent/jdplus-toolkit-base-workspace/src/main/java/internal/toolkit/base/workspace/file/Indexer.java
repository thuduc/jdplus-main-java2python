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
package internal.toolkit.base.workspace.file;

import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor.Key;
import java.io.Closeable;
import java.io.IOException;
import nbbrd.io.Resource;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
interface Indexer extends Closeable {

    void checkId(@NonNull Key id) throws IOException;

    @NonNull
    Index loadIndex() throws IOException;

    void storeIndex(@NonNull Index index) throws IOException;

    @NonNull
    default Indexer memoize() {
        Indexer delegate = this;
        return new Indexer() {
            private Index latest;
            private boolean storeRequired;

            @Override
            public void checkId(Key id) throws IOException {
                delegate.checkId(id);
            }

            @Override
            public Index loadIndex() throws IOException {
                if (latest == null) {
                    latest = delegate.loadIndex();
                    storeRequired = false;
                }
                return latest;
            }

            @Override
            public void storeIndex(Index index) throws IOException {
                latest = index;
                storeRequired = true;
            }

            @Override
            public void close() throws IOException {
                Resource.closeBoth(this::flushIndex, delegate::close);
            }

            private void flushIndex() throws IOException {
                if (latest != null && storeRequired) {
                    delegate.storeIndex(latest);
                }
            }
        };
    }
}
