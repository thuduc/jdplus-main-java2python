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
package internal.text.base.api;

import jdplus.toolkit.base.tsp.fixme.Files2;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

/**
 * @author Philippe Charles
 */
public final class TxtFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        switch (Files2.getFileExtension(pathname).toLowerCase(Locale.ENGLISH)) {
            case "txt":
            case "csv":
            case "tsv":
                return true;
            default:
                return false;
        }
    }

    public String getFileDescription() {
        return "Text file (.txt, .csv, .tsv)";
    }
}
