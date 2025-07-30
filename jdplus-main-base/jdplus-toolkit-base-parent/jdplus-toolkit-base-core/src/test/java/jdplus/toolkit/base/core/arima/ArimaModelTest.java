/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.arima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArimaModelTest {

    public ArimaModelTest() {
    }

    @Test
    public void testConstants() {
        assertTrue(ArimaModel.ONE.isWhiteNoise());
        assertFalse(ArimaModel.ONE.isNull());
        assertTrue(ArimaModel.NULL.isNull());
    }

    @Test
    public void testSumDiff() {
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();

        ArimaModel wn = ArimaModel.whiteNoise();
        ArimaModel sum = ArimaModel.add(sarima, wn);
//        System.out.println(sum);
        ArimaModel m = ArimaModel.subtract(sum, ArimaModel.of(sarima));
        m=m.simplifyUr();
//        System.out.println();
        assertTrue(m.isWhiteNoise());
    }
}
