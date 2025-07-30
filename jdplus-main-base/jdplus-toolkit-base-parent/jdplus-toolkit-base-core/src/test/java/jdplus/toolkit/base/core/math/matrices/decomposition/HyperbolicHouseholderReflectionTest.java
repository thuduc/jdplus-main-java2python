/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.data.DataBlock;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class HyperbolicHouseholderReflectionTest {

    private static DoubleSeq X = Doubles.of(new double[]{1, 2, 3, 4, 5});

    public HyperbolicHouseholderReflectionTest() {
    }

    @Test
    public void testSomeMethod() {
        DataBlock z = DataBlock.of(X);
        HyperbolicHouseholderReflection hr = HyperbolicHouseholderReflection.of(z, 3);
        assertEquals(hr.getNrm2(), Math.sqrt(-27), 1e-12);
    }

}
