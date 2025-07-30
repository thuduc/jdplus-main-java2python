/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.core.sarima.estimation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.ArimaSeriesGenerator;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
public class HannanRissanenTest {

    static final DoubleSeq airlineData, data;

    static {
        XorshiftRNG rnd = new XorshiftRNG(0);
        SarmaOrders spec = new SarmaOrders(12);
        spec.setQ(1);
        spec.setBq(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        ArimaSeriesGenerator generator = ArimaSeriesGenerator.builder(rnd)
                .build() ;
        airlineData = DoubleSeq.of(generator.generate(arima, 120));
        spec.setP(3);
        arima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .phi(-.2, -.3, .4)
                .build();
        data = DoubleSeq.of(generator.generate(arima, 120));
    }

    public HannanRissanenTest() {
    }

    @Test
    @Disabled
    public void testAirline() {
        HannanRissanen hr = HannanRissanen.builder().build();
        SarmaOrders spec = new SarmaOrders(12);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(airlineData, spec);
        System.out.println("New airline");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void test3101() {
        HannanRissanen hr = HannanRissanen.builder().initialization(HannanRissanen.Initialization.Ols).build();
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(data, spec);
        System.out.println("New 3101");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void test3101_burg() {
        HannanRissanen hr = HannanRissanen.builder().initialization(HannanRissanen.Initialization.Burg).build();
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(data, spec);
        System.out.println("New 3101, Burg");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void test3101_levinson() {
        HannanRissanen hr = HannanRissanen.builder().initialization(HannanRissanen.Initialization.Levinson).build();
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBq(1);
        hr.process(data, spec);
        System.out.println("New 3101, Levinson");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void testLegacyAirline() {
        ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
        ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
        spec.setQ(1);
        spec.setBQ(1);
        hr.process(new ec.tstoolkit.data.ReadDataBlock(airlineData.toArray()), spec);
        System.out.println("Legacy airline");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void testLegacy3101() {
        ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
        ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBQ(1);
        hr.process(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), spec);
        System.out.println("Legacy 3101");
        System.out.println(hr.getModel());
    }

    @Test
    @Disabled
    public void stressTest() {
        int K = 100000;
        for (int q = 0; q < 2; ++q) {
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < (q == 0 ? 100 : K); ++i) {
                ec.tstoolkit.sarima.estimation.HannanRissanen hr = new ec.tstoolkit.sarima.estimation.HannanRissanen();
                ec.tstoolkit.sarima.SarmaSpecification spec = new ec.tstoolkit.sarima.SarmaSpecification(12);
                spec.setQ(1);
                spec.setBQ(1);
                hr.process(new ec.tstoolkit.data.ReadDataBlock(airlineData.toArray()), spec);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < (q == 0 ? 100 : K); ++i) {
                HannanRissanen hr = HannanRissanen.builder().build();
                SarmaOrders spec = new SarmaOrders(12);
                spec.setQ(1);
                spec.setBq(1);
                hr.process(airlineData, spec);
            }
            t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
    }
}
