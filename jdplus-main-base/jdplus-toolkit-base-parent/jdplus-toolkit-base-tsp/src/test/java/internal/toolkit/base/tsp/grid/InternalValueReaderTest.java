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
package internal.toolkit.base.tsp.grid;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import test.tsprovider.grid.ArrayGridInput;
import static test.tsprovider.grid.Data.*;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class InternalValueReaderTest {

    @Test
    public void testOnNull() throws IOException {
        InternalValueReader x = InternalValueReader.onNull();

        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(i); j++) {
                assertThat(x.read(grid.getValue(i, j))).isNull();
            }
        }
    }

    @Test
    public void testOnDateTime() throws IOException {
        InternalValueReader x = InternalValueReader.onDateTime();

        assertThat(x.read(grid.getValue(0, 0))).isNull();
        assertThat(x.read(grid.getValue(1, 0))).isNull();
        assertThat(x.read(grid.getValue(0, 1))).isEqualTo(JAN_);
        assertThat(x.read(grid.getValue(0, 2))).isEqualTo(FEB_);
    }

    private final Object[][] data = {
        {null, JAN_, FEB_, MAR_},
        {"S1", 3.14, 4.56, 7.89}
    };
    private final ArrayGridInput grid = ArrayGridInput.of(data);
}
