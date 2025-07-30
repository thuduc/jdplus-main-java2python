/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.data.accumulator;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.accumulator.DoubleAccumulator;
import jdplus.toolkit.base.core.data.accumulator.KahanAccumulator;
import jdplus.toolkit.base.core.data.accumulator.NeumaierAccumulator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kahan summation
 * @author Jean Palate <jean.palate@nbb.be>
 */
public  class DoubleAccumulatorTest {
    
    public DoubleAccumulatorTest() {
    }

    @Test
    public void testSomeMethod() {
        int N=10000;
        double s=0;
        DataBlock block=DataBlock.make(N);
        block.set(i-> (i+1)/7999.0);
        DoubleAccumulator acc=new NeumaierAccumulator();
        DoubleAccumulator acc2=new KahanAccumulator();
        for (int i=0; i<N; ++i){
            double t=block.get(i);
            s+=t;
            acc.add(t);
            acc2.add(t);
        }
        double r=10001*5000.0/7999.0;
//        System.out.println(acc.sum()-r);
//        System.out.println(acc2.sum()-r);
        assertTrue(Math.abs(acc.sum()-r)<Math.abs(s-r));
        assertTrue(Math.abs(acc2.sum()-r)<Math.abs(s-r));
    }
    
}
