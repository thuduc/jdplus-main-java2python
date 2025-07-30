/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.timeseries.calendars;

import jdplus.toolkit.base.api.timeseries.calendars.FixedWeekDay;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class FixedWeekDayTest {

    public FixedWeekDayTest() {
    }

    @Test
    public void testDate() {
        for (int i = 2010; i < 2030; ++i) {
            LocalDate date = FixedWeekDay.BLACKFRIDAY.calcDate(i);
//            System.out.println(date);
            assertTrue(date.getDayOfMonth() <= 28 && date.getDayOfMonth() > 21);
        }
    }

    @Test
    public void testLastDate() {
        FixedWeekDay fwd=new FixedWeekDay(1, -1, DayOfWeek.SATURDAY);
        for (int i = 2010; i < 2030; ++i) {
            LocalDate date = fwd.calcDate(i);
            System.out.println(date);
            assertTrue(date.getDayOfMonth() > 24);
        }
    }
}
