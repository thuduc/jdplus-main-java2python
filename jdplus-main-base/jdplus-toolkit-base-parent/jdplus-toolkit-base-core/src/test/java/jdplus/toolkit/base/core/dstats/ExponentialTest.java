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
package jdplus.toolkit.base.core.dstats;

import jdplus.toolkit.base.api.stats.ProbabilityType;
import java.util.Arrays;

import jdplus.toolkit.base.core.random.MersenneTwister;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class ExponentialTest {
    
    public ExponentialTest() {
    }

    @Test
    public void testProb() {
        Exponential E=new Exponential(.2);
        double x=.123;
        double p =E.getProbability(x, ProbabilityType.Upper);
        double y=E.getProbabilityInverse(p, ProbabilityType.Upper);
        assertEquals(x,y,1e-9);
        p =E.getProbability(x, ProbabilityType.Lower);
        y=E.getProbabilityInverse(p, ProbabilityType.Lower);
        assertEquals(x,y,1e-9);
    }

    public static void main(String[] args){
        Exponential E=new Exponential(2);
        MersenneTwister rng=MersenneTwister.fromSystemNanoTime();
        double[] z=new double[10000001];
        long t0=System.currentTimeMillis();
        for (int i=0; i<z.length; ++i){
            z[i]=E.random(rng);
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        Arrays.sort(z);
        double step=5.0/1000;
        for (int i=0, j=0; i<=1000; ++i){
            int k=j;
            while (z[k] < step*(i+1) && k<z.length) {++k;}
            System.out.print((k-j)/(z.length*step));
            j=k;            
            System.out.print('\t');
            System.out.println(E.getDensity(i*step));
        }
        
    }
    
}
