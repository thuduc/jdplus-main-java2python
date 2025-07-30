/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.sa.base.core.tests.SeasonalityTests;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SeasonalityTestsTest {

    public SeasonalityTestsTest() {
    }

    //@Test
    public void testProd() {
        SeasonalityTests tests = SeasonalityTests.seasonalityTest(DoubleSeq.of(Data.PROD), 12, -1, false, true);
        ec.tstoolkit.timeseries.simplets.TsData os = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.tramo.SeasonalityTests otests = ec.tstoolkit.modelling.arima.tramo.SeasonalityTests.seasonalityTest(os, -1, false, true);
        assertEquals(tests.getQs().getValue(), otests.getQs().getValue(), 1e-6);
        assertEquals(tests.getNonParametricTest().getValue(), otests.getNonParametricTest().getValue(), 1e-6);
        assertEquals(tests.getPeriodogramTest().getValue(), otests.getPeriodogramTest().getValue(), 1e-6);
        assertEquals(tests.getTukeyPeaks().getTdProb(), otests.getTukeyPeaks().getTdProb(), 1e-6);
    }

}
