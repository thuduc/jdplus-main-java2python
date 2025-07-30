/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.sts;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.utility.DynamicsCoherence;
import jdplus.toolkit.base.core.ssf.utility.LoadingCoherence;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class PeriodicComponentTest {

    public PeriodicComponentTest() {
    }

    @Test
    public void testDynamics() {
        StateComponent lt = PeriodicComponent.stateComponent(52, new int[]{1,2,3,4,7,8}, 2);
        DynamicsCoherence.check(lt.dynamics(), lt.dim());
    }

    @Test
    public void testMeasurement() {
        ISsfLoading l = PeriodicComponent.defaultLoading(3);
        LoadingCoherence.check(l, PeriodicComponent.dim(6));
    }
}
