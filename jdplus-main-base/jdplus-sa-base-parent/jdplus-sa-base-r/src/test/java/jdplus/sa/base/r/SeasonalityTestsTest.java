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
package jdplus.sa.base.r;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTestsTest {

    public SeasonalityTestsTest() {
    }

    @Test
    public void testFTest() {
        DoubleSeq x=DoubleSeq.of(Data.ABS_RETAIL).delta(1).removeMean();
        StatisticalTest test = SeasonalityTests.fTest(x.toArray(), 12, "AR", 0);
//        System.out.println(test);
        assertTrue(test.getPvalue() < .01);
    }

    @Test
    public void testQsTest() {
        DoubleSeq x=DoubleSeq.of(Data.ABS_RETAIL).delta(1).removeMean();
        StatisticalTest test = SeasonalityTests.qsTest(x.toArray(), 12, 0);
//        System.out.println(test);
        assertTrue(test.getPvalue() < .01);
    }

    @Test
    public void testPeriodicQsTest() {
        DoubleSeq x=DoubleSeq.of(Data.ABS_RETAIL).delta(1).removeMean();
        StatisticalTest test = SeasonalityTests.periodicQsTest(x.toArray(), new double[]{17, 1});
//        System.out.println(test);
        assertTrue(test.getPvalue() > .01);
    }

    @Test
    public void testCombinedTest() {
        DoubleSeq x=DoubleSeq.of(Data.EXPORTS).delta(1).removeMean();
        CombinedSeasonality test = SeasonalityTests.combinedTest(x.toArray(), 12, 1, false);

        ec.satoolkit.diagnostics.CombinedSeasonalityTest otest = new ec.satoolkit.diagnostics.CombinedSeasonalityTest(
                new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 1,
                        x.toArray(), false), false);
        ec.satoolkit.diagnostics.CombinedSeasonalityTest.IdentifiableSeasonality osummary = otest.getSummary();
        //        System.out.println(test);
        assertEquals(test.mvalue(), otest.mvalue(), 1e-9);
    }
    
    @Test
    public void testCH() {
        DoubleSeq x=DoubleSeq.of(Data.RETAIL_BOOKSTORES).log().delta(1);
        double[] q=SeasonalityTests.canovaHansen(x.toArray(), 12, "Trigonometric", false, "Bartlett", -1, 3);
 //       System.out.println(DoubleSeq.of(q));
        q=SeasonalityTests.canovaHansen(x.toArray(), 12, "Dummy", false, "Bartlett", -1, 3);
//        System.out.println(DoubleSeq.of(q));
        q=SeasonalityTests.canovaHansen(x.toArray(), 12, "Contrast", false, "Bartlett", -1, 3);
//        System.out.println(DoubleSeq.of(q));
    }
}
