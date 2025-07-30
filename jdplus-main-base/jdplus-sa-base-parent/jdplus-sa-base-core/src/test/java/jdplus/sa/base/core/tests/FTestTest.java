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
package jdplus.sa.base.core.tests;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class FTestTest {
    
    public FTestTest() {
    }

    @Test
    public void testWN() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(SarimaOrders.Prespecified.WN).build();
        assertFalse(f.isSignificant(0.01));
//        System.out.println(test.build());
        s=TsDataToolkit.delta(Data.TS_PROD, 1);
        test=new FTest(s.getValues(),12);
        f = test.model(SarimaOrders.Prespecified.WN).build();
        assertTrue(f.isSignificant(0.01));
//        System.out.println(test.build());
    }
    
    @Test
    public void testAR() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(SarimaOrders.Prespecified.AR).build();
        assertFalse(f.isSignificant(0.01));
//        System.out.println(test.build());
        s=TsDataToolkit.delta(Data.TS_PROD, 1);
        test=new FTest(s.getValues(),12);
        f = test.model(SarimaOrders.Prespecified.AR).build();
        assertTrue(f.isSignificant(0.01));
//        System.out.println(test.build());
    }
    
    @Test
    public void testD1() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(SarimaOrders.Prespecified.D1).build();
        assertFalse(f.isSignificant(0.01));
//        System.out.println(test.build());
        s=Data.TS_PROD;
        test=new FTest(s.getValues(),12);
        f = test.model(SarimaOrders.Prespecified.D1).build();
        assertTrue(f.isSignificant(0.01));
//        System.out.println(test.build());
    }
}
