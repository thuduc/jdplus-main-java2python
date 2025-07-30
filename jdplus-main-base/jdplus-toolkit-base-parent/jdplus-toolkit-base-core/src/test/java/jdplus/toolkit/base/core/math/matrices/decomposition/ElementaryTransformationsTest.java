/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class ElementaryTransformationsTest {
    
    public ElementaryTransformationsTest() {
    }

    @Test
    public void testJHypothenuse() {
        
        double small=1, big=1e15;
        double z = ElementaryTransformations.jhypotenuse(big+small, big);
        z*=z;
        assertEquals(2*big+1, z, 1);
    }
    
}
