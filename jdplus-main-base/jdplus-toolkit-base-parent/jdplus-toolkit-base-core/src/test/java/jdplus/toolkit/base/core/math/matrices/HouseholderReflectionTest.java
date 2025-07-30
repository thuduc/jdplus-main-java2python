/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices;

import jdplus.toolkit.base.core.math.matrices.decomposition.HouseholderReflection;
import jdplus.toolkit.base.core.data.DataBlock;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class HouseholderReflectionTest {
    
    public HouseholderReflectionTest() {
    }

    @Test
    public void testHouseholder() {
        DataBlock x=DataBlock.make(10);
        Random rnd=new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        double nx=x.norm2();
        // Creates the Householder reflection
        HouseholderReflection hr = HouseholderReflection.of(x, true);
        // x is now (|| x || 0 ... 0)
        assertTrue(x.drop(1, 0).allMatch(z->z==0));
        assertEquals(nx, Math.abs(x.get(0)), 1e-9);
        // apply the transformation on another vector
        DataBlock y=DataBlock.make(10);
        y.set((DoubleSupplier)rnd::nextDouble);
        hr.transform(y);
    }
    
}
