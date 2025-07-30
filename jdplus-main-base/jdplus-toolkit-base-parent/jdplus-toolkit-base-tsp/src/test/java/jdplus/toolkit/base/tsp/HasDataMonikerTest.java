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

import jdplus.toolkit.base.api.timeseries.TsMoniker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class HasDataMonikerTest {

    private final String providerName = "myprovider";
    private final DataSource goodDataSource = DataSource.of("myprovider", "1234");
    private final DataSource badDataSource = DataSource.of("xxx", "1234");
    private final DataSet goodDataSet = DataSet.of(goodDataSource, DataSet.Kind.SERIES);
    private final DataSet badDataSet = DataSet.of(badDataSource, DataSet.Kind.SERIES);
    private final TsMoniker goodDataSourceMoniker = TsMoniker.of(providerName, goodDataSource.toString());
    private final TsMoniker badDataSourceMoniker = TsMoniker.of("xxx", badDataSource.toString());
    private final TsMoniker goodDataSetMoniker = TsMoniker.of(providerName, goodDataSet.toString());
    private final TsMoniker badDataSetMoniker = TsMoniker.of("xxx", badDataSet.toString());

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(HasDataMoniker.usingUri(providerName)).isNotNull();
        assertThatThrownBy(() -> HasDataMoniker.usingUri(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testToDataSource() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toDataSource(goodDataSourceMoniker)).hasValue(goodDataSource);
        assertThat(support.toDataSource(goodDataSetMoniker)).isEmpty();
        assertThatThrownBy(() -> support.toDataSource(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toDataSource(badDataSourceMoniker)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> support.toDataSource(badDataSetMoniker)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testToDataSet() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toDataSet(goodDataSetMoniker)).hasValue(goodDataSet);
        assertThat(support.toDataSet(goodDataSourceMoniker)).isEmpty();
        assertThatThrownBy(() -> support.toDataSet(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toDataSet(badDataSourceMoniker)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> support.toDataSet(badDataSetMoniker)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFromDataSource() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toMoniker(goodDataSource)).isEqualTo(goodDataSourceMoniker);
        assertThatThrownBy(() -> support.toMoniker((DataSource) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toMoniker(badDataSource)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFromDataSet() {
        HasDataMoniker support = HasDataMoniker.usingUri(providerName);
        assertThat(support.toMoniker(goodDataSet)).isEqualTo(goodDataSetMoniker);
        assertThatThrownBy(() -> support.toMoniker((DataSet) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.toMoniker(badDataSet)).isInstanceOf(IllegalArgumentException.class);
    }
}
