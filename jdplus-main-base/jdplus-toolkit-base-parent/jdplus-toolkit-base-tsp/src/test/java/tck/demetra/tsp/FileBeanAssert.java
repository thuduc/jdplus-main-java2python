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
package tck.demetra.tsp;

import jdplus.toolkit.base.tsp.FileBean;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;
import lombok.NonNull;

/**
 * {@link FileBean} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class FileBeanAssert extends AbstractAssert<FileBeanAssert, FileBean> {

    /**
     * Creates a new <code>{@link FileBeanAssert}</code> to make assertions on
     * actual IFileBean.
     *
     * @param actual the IFileBean we want to make assertions on.
     */
    public FileBeanAssert(@NonNull FileBean actual) {
        super(actual, FileBeanAssert.class);
    }

    /**
     * An entry point for IFileBeanAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myIFileBean)</code> and get specific assertion with code
     * completion.
     *
     * @param actual the IFileBean we want to make assertions on.
     * @return a new <code>{@link FileBeanAssert}</code>
     */
    @NonNull
    public static FileBeanAssert assertThat(@NonNull FileBean actual) {
        return new FileBeanAssert(actual);
    }

    /**
     * Verifies that the actual IFileBean's file is equal to the given one.
     *
     * @param file the given file to compare the actual IFileBean's file to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileBean's file is not equal to
     *                        the given one.
     */
    @NonNull
    public FileBeanAssert hasFile(java.io.File file) {
        // check that actual IFileBean we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting file of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.io.File actualFile = actual.getFile();
        if (!Objects.areEqual(actualFile, file)) {
            failWithMessage(assertjErrorMessage, actual, file, actualFile);
        }

        // return the current assertion for method chaining
        return this;
    }
}
