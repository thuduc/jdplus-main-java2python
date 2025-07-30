/*
 * Copyright 2015 National Bank of Belgium
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
package internal.spreadsheet.base.api;

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.tsp.grid.GridLayout;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * @author Philippe Charles
 */
final class SpreadSheetCollectionAssert extends AbstractAssert<SpreadSheetCollectionAssert, TsCollection> {

    public static SpreadSheetCollectionAssert assertThat(TsCollection actual) {
        return new SpreadSheetCollectionAssert(actual);
    }

    public SpreadSheetCollectionAssert(TsCollection actual) {
        super(actual, SpreadSheetCollectionAssert.class);
    }

    public SpreadSheetCollectionAssert hasSheetName(String name) {
        isNotNull();
        if (!actual.getName().equals(name)) {
            failWithMessage("Expected sheet's name to be <%s> but was <%s>", name, actual.getName());
        }
        return this;
    }

    public SpreadSheetCollectionAssert hasLayout(GridLayout layout) {
        isNotNull();
        if (!actual.getMeta().getOrDefault(GridLayout.PROPERTY, GridLayout.UNDEFINED.name()).equals(layout.name())) {
            failWithMessage("Expected alignType to be <%s> but was <%s>", layout, actual.getMeta().get(GridLayout.PROPERTY));
        }
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(TsData... data) {
        Assertions.assertThat(actual)
                .extracting(Ts::getData)
                .containsExactly(data);
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(String... names) {
        Assertions.assertThat(actual)
                .extracting(Ts::getName)
                .containsExactly(names);
        return this;
    }
}
