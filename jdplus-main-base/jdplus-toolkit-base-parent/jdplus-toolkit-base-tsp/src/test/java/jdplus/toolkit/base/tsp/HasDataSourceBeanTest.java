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

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataSourceBean;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author Philippe Charles
 */
public class HasDataSourceBeanTest {

    private final String providerName = "myprovider";
    private final String version = "1234";

    private static final class CustomBean {

        static CustomBean of(File file, String details) {
            CustomBean result = new CustomBean();
            result.file = file;
            result.details = details;
            return result;
        }

        File file;
        String details;

        @Override
        public int hashCode() {
            return Objects.hash(file, details);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CustomBean
                    && ((CustomBean) obj).file.equals(file)
                    && ((CustomBean) obj).details.equals(details);
        }
    }

    private final DataSource.Converter<CustomBean> param = new DataSource.Converter<CustomBean>() {

        private final Property<File> fileParam = Property.of("f", Path.of("defaultFile").toFile(), Parser.onFile(), Formatter.onFile());
        private final Property<String> detailsParam = Property.of("d", "defaultValue", Parser.onString(), Formatter.onString());

        @Override
        public @NonNull CustomBean getDefaultValue() {
            CustomBean result = new CustomBean();
            result.file = fileParam.getDefaultValue();
            result.details = detailsParam.getDefaultValue();
            return result;
        }

        @Override
        public @NonNull CustomBean get(@NonNull DataSource config) {
            CustomBean result = new CustomBean();
            result.file = fileParam.get(config::getParameter);
            result.details = detailsParam.get(config::getParameter);
            return result;
        }

        @Override
        public void set(DataSource.@NonNull Builder builder, CustomBean value) {
            fileParam.set(builder::parameter, value.file);
            detailsParam.set(builder::parameter, value.details);
        }
    };

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatThrownBy(() -> HasDataSourceBean.of(null, param, version)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> HasDataSourceBean.of(providerName, null, version)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testNewBean() {
        HasDataSourceBean support = HasDataSourceBean.of(providerName, param, version);
        assertThat(support.newBean())
                .isNotNull()
                .isNotSameAs(support.newBean())
                .usingRecursiveComparison()
                .isEqualTo(support.newBean());
        assertThat(support.newBean())
                .extracting("file", "details")
                .containsExactly(Path.of("defaultFile").toFile(), "defaultValue");
    }

    @Test
    @SuppressWarnings("null")
    public void testEncodeBean() {
        HasDataSourceBean support = HasDataSourceBean.of(providerName, param, version);
        DataSource.Builder b = DataSource.builder(providerName, version);
        assertThat(support.encodeBean(support.newBean()))
                .isEqualTo(b.clearParameters().build());
        assertThat(support.encodeBean(CustomBean.of(Path.of("hello").toFile(), "world")))
                .isEqualTo(b.clearParameters().parameter("f", "hello").parameter("d", "world").build());
        assertThatThrownBy(() -> support.encodeBean(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.encodeBean("string")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testDecodeBean() {
        HasDataSourceBean support = HasDataSourceBean.of(providerName, param, version);
        DataSource.Builder b = DataSource.builder(providerName, version);
        assertThat(support.decodeBean(b.clearParameters().build()))
                .isEqualTo(support.newBean());
        assertThat(support.decodeBean(b.clearParameters().parameter("f", "hello").parameter("d", "world").build()))
                .isEqualTo(CustomBean.of(Path.of("hello").toFile(), "world"));
        assertThatThrownBy(() -> support.decodeBean(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.decodeBean(DataSource.builder("xxx", version).build())).isInstanceOf(IllegalArgumentException.class);
    }
}
