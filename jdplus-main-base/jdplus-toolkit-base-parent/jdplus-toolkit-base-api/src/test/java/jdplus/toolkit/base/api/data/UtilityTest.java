/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.data;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Iterables;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class UtilityTest {
    
    public UtilityTest() {
    }

    @Test
    public void testIterable() {
        DoubleSeq seq=DoubleSeq.of(1,2,3,4,5,6,7,8,9,10);
        Iterable<Double> iter = Iterables.of(seq);
        int n=0;
        for (double s : iter){
            ++n;
        }
        assertTrue(n == 10);
    }
    
}
