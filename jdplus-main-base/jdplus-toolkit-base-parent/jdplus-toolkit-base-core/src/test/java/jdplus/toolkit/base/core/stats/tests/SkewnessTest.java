/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.base.core.stats.tests;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.data.DataBlock;
import java.util.Random;
import java.util.function.DoubleSupplier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SkewnessTest {
    
    public SkewnessTest() {
    }

    @Test
    public void testLegacy() {
        int N=100;
        double[] data=new double[N];
        DataBlock X=DataBlock.of(data);
        Random rnd=new Random();
        X.set((DoubleSupplier)rnd::nextDouble);
        
        StatisticalTest sk = new Skewness(X).build();
        
        ec.tstoolkit.stats.SkewnessTest sk2=new ec.tstoolkit.stats.SkewnessTest();
        sk2.test(new ec.tstoolkit.data.DescriptiveStatistics(data));
        
        assertEquals(sk.getPvalue(), sk2.getPValue(), 1e-9);
    }
    
}
