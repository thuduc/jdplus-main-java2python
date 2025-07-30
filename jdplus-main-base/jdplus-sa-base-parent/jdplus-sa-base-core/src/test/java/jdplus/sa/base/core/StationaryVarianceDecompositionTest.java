/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.base.core;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StationaryVarianceDecompositionTest {

    public StationaryVarianceDecompositionTest() {
    }

    @Test
    public void testLongTerm() {
        TsData t1 = StationaryVarianceComputer.LINEARTREND
                .calcLongTermTrend(TsData.ofInternal(TsPeriod.yearly(1900),
                        Data.NILE));
//        System.out.println(t1);
        TsData t2 = new StationaryVarianceComputer.HPTrendComputer(20)
                .calcLongTermTrend(TsData.ofInternal(TsPeriod.yearly(1900),
                        Data.NILE));
//        System.out.println(t2);

        assertTrue(t1 != null);
        assertTrue(t2 != null);
    }

}
