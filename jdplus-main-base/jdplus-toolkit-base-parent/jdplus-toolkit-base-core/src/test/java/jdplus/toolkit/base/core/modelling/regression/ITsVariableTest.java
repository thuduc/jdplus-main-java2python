/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ITsVariableTest {
    
    public ITsVariableTest() {
    }

    @Test
    public void testName() {
        String name="var";
        for (int i=0; i<20; ++i){
            name=ITsVariable.nextName(name);
        }
        assertTrue(name.equals("var(20)"));
    }
    
}
