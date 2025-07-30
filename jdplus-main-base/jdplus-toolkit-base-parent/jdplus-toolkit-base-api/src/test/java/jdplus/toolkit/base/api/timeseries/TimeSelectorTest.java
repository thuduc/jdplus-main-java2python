package jdplus.toolkit.base.api.timeseries;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeSelectorTest {

    @Test
    public void testBetween() {
        LocalDateTime start = LocalDate.of(2010, 1, 1).atStartOfDay();
        LocalDateTime end = start.plusYears(2);
        assertThat(TimeSelector.between(start, end))
                .isEqualTo(TimeSelector.between(TsDomain.of(TsPeriod.of(TsUnit.P1Y, start), 2)));
    }
}
