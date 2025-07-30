/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.models;

import jdplus.toolkit.base.core.ssf.arima.SsfAr;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import java.util.Random;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.ssf.StateComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ARTest {
    
    public ARTest() {
    }

    @Test
    public void testTVT() {
        StateComponent cmp = SsfAr.of(new double[]{.3, -.4, .2}, 0.7, 10);
        FastMatrix z=FastMatrix.square(cmp.initialization().getStateDim());
        Random rnd=new Random();
        z.set((i,j)->rnd.nextDouble());
        FastMatrix V=SymmetricMatrix.XXt(z);
        FastMatrix W=V.deepClone();
        cmp.dynamics().TVT(0, V);
        cmp.dynamics().TM(0, W);
        cmp.dynamics().MTt(0, W);
        assertTrue(MatrixNorms.frobeniusNorm(V.minus(W))<1e-9);
    }
    
}
