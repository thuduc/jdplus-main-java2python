/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.tramoseats.base.core.seats;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.tramoseats.base.api.seats.DecompositionSpec;
import jdplus.tramoseats.base.api.seats.DecompositionSpec.ComponentsEstimationMethod;
import jdplus.tramoseats.base.api.seats.SeatsModelSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class SeatsKernelTest {

    public SeatsKernelTest() {
    }

    @Test
    public void testProdBurman() {
        SeatsModelSpec model = SeatsModelSpec.builder()
                .series(Data.TS_PROD)
                .log(true)
                .sarimaSpec(SarimaSpec.airline())
                .build();
        DecompositionSpec cmps = DecompositionSpec.builder()
                .backcastCount(0)
                .forecastCount(0)
                .build();


        SeatsToolkit toolkit = SeatsToolkit.of(cmps);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = ProcessingLog.dummy();
        SeatsResults rslt = kernel.process(model, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getInitialComponents());
    }

    @Test
    public void testProdKF() {
        SeatsModelSpec model = SeatsModelSpec.builder()
                .series(Data.TS_PROD)
                .log(true)
                .sarimaSpec(SarimaSpec.airline())
                .build();
        DecompositionSpec cmps = DecompositionSpec.builder()
                .backcastCount(0)
                .forecastCount(0)
                .method(ComponentsEstimationMethod.KalmanSmoother)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(cmps);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = ProcessingLog.dummy();
        SeatsResults rslt = kernel.process(model, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getInitialComponents());
    }

    @Test
    public void testProdKF3() {
        SarimaSpec mspec = SarimaSpec.builder()
                .p(3).d(1).q(1).bp(0).bd(1).bq(1).build();
                
        SeatsModelSpec model = SeatsModelSpec.builder()
                .series(Data.TS_PROD)
                .meanCorrection(true)
                .log(true)
                .sarimaSpec(mspec)
                .build();
        DecompositionSpec cmps = DecompositionSpec.builder()
                .backcastCount(-1)
                .forecastCount(-2)
//               .method(ComponentsEstimationMethod.KalmanSmoother)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(cmps);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = ProcessingLog.dummy();
        SeatsResults rslt = kernel.process(model, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getInitialComponents());
//        System.out.println(rslt.getFinalComponents());
//        System.out.println(rslt.getUcarimaModel());
    }
}
