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
package jdplus.sa.base.csv;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class CsvMatrixOutputConfiguration implements Cloneable {

    public static final String NAME = "demetra_m";

    private String[] items;
    private File folder;
    private String name = NAME;
    private boolean fullName;
    private boolean shortColumnName;

    public CsvMatrixOutputConfiguration() {
        fullName = true;
        items = new String[0];
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File value) {
        folder = value;
    }

    public String getFileName() {
        return name;
    }

    public void setFileName(String value) {
        name = value;
    }

    public List<String> getItems() {
        return Arrays.asList(items);
    }

    public void setItems(List<String> value) {
        items = value.toArray(String[]::new);
    }

    public boolean isFullName() {
        return fullName;
    }

    public void setFullName(boolean fullName) {
        this.fullName = fullName;
    }

    public boolean isShortColumnName() {
        return shortColumnName;
    }

    public void setShortColumnName(boolean shortColumnName) {
        this.shortColumnName = shortColumnName;
    }

    @Override
    public CsvMatrixOutputConfiguration clone() {
        try {
            CsvMatrixOutputConfiguration clone = (CsvMatrixOutputConfiguration) super.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
