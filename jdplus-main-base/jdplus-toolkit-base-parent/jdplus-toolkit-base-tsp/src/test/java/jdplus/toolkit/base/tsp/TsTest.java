/*
 * Copyright 2018 National Bank of Belgium
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

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsTest {

    @Test
    public void testValue() {
        for (Ts o : new Ts[]{empty, sample}) {
            assertThat(o.toBuilder().build())
                    .isNotSameAs(o)
                    .isEqualTo(o)
                    .hasSameHashCodeAs(o)
                    .hasFieldOrPropertyWithValue("data", o.getData())
                    .hasFieldOrPropertyWithValue("meta", o.getMeta())
                    .hasFieldOrPropertyWithValue("moniker", o.getMoniker())
                    .hasFieldOrPropertyWithValue("name", o.getName())
                    .hasFieldOrPropertyWithValue("type", o.getType());

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> o.getMeta().put("x", "y"));
        }
    }

    @Test
    public void testBuilder() {
        assertThatNullPointerException().isThrownBy(() -> Ts.builder().data(null).build());
        assertThatNullPointerException().isThrownBy(() -> Ts.builder().meta(null));
        assertThatNullPointerException().isThrownBy(() -> Ts.builder().moniker(null).build());
        assertThatNullPointerException().isThrownBy(() -> Ts.builder().name(null).build());
        assertThatNullPointerException().isThrownBy(() -> Ts.builder().type(null).build());
    }

    private final Ts empty = Ts.builder().build();
    private final Ts sample = Ts.builder()
            .data(TsData.random(TsUnit.P7D, 0))
            .meta("hello", "world")
            .moniker(TsMoniker.of("provider", "id"))
            .name("abc")
            .type(TsInformationType.All)
            .build();
}
