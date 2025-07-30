/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.stats.tests;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.data.DataBlock;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class BowmanShentonTest {
    
    public BowmanShentonTest() {
    }

    @Test
    public void testLegacy() {
        int N = 100;
        DataBlock X = DataBlock.make(N);
        Random rnd = new Random();
        X.set((DoubleSupplier)rnd::nextDouble);

        BowmanShenton bs = new BowmanShenton(X);

        StatisticalTest test = bs.build();

        ec.tstoolkit.stats.BowmanShentonTest bs2 = new ec.tstoolkit.stats.BowmanShentonTest();
        bs2.test(new ec.tstoolkit.data.ReadDataBlock(X.getStorage()));

        assertEquals(test.getPvalue(), bs2.getPValue(), 1e-9);
    }
}
