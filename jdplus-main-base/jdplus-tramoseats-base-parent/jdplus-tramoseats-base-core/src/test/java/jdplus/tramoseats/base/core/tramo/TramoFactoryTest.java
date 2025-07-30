/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import tck.demetra.data.Data;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TramoFactoryTest {

    public TramoFactoryTest() {
    }

    @Test
    public void testRefreshPolicy() {
        TramoKernel kernel = TramoKernel.of(TramoSpec.TRfull, null);
        RegSarimaModel rslt = kernel.process(Data.TS_PROD, null);
        TramoSpec pspec = TramoFactory.getInstance().generateSpec(TramoSpec.TRfull, rslt.getDescription());
        TramoSpec nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.Current, null);
        assertTrue(nspec != null);
        RegSarimaModel tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
        nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.Fixed, null);
        assertTrue(nspec != null);
        tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
        nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.FixedAutoRegressiveParameters, null);
        assertTrue(nspec != null);
        tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
        nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.FreeParameters, null);
        assertTrue(nspec != null);
        tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
        nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.Outliers, null);
        assertTrue(nspec != null);
        tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
        nspec = TramoFactory.getInstance().refreshSpec(pspec, TramoSpec.TRfull, EstimationPolicyType.Outliers_StochasticComponent, null);
        assertTrue(nspec != null);
        tmp = TramoKernel.of(nspec, null).process(Data.TS_PROD, null);
        assertTrue(tmp != null);
    }

}
