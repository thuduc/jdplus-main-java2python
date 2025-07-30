/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.timeseries.simplets;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

import java.time.Clock;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class TsDataViewTest {

    public TsDataViewTest() {
    }

    @Test
    public void testFullYears() {
        TsPeriod p = TsPeriod.of(TsUnit.P1M, LocalDate.now(Clock.systemDefaultZone()));
        for (int i = 0; i < 12; ++i) {
            for (int j = 0; j < 12; ++j) {
                DataBlock d = DataBlock.make(i + j + 36);
                final int beg = p.start().getMonthValue() - 1 - i;
                d.set(k -> beg + k);
                TsData s = TsData.of(p.plus(-i), d);
                TsDataView fy = TsDataView.fullYears(s);
                Assertions.assertTrue(fy.getData().length() % 12 == 0);
//                Assert.assertTrue(((int) fy.getData().get(0)) % 12 == 0);
            }
        }

        TsData d1 = TsData.of(TsPeriod.monthly(2010, 1), DoubleSeq.onMapping(2 * 12, i -> 1 + i));
        assertThat(TsDataView.fullYears(d1))
                .extracting(o -> o.getStart(), o -> o.getData().toArray())
                .containsExactly(d1.getStart(), d1.getValues().toArray());

        TsData d2 = TsData.of(TsPeriod.monthly(2010, 2), DoubleSeq.onMapping(2 * 12, i -> 1 + i));
        assertThat(TsDataView.fullYears(d2))
                .extracting(o -> o.getStart(), o -> o.getData().toArray())
                .containsExactly(d2.getStart().plus(11), d2.getValues().drop(11, 1).toArray());
    }
}
