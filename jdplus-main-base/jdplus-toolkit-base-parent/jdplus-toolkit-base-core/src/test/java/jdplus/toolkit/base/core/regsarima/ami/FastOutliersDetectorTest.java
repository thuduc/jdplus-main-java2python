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
package jdplus.toolkit.base.core.regsarima.ami;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import java.util.List;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastOutliersDetectorTest {
    
    public FastOutliersDetectorTest() {
    }

    @Test
    public void testProd() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 500; ++i) {
        FastOutliersDetector od = FastOutliersDetector.builder()
                .singleOutlierDetector(FastOutliersDetector.defaultOutlierDetector())
                .criticalValue(3)
                .processor(RegArimaUtility.processor(true, 1e-7))
                .maximumLikelihood(true)
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] outliers = od.getOutliers();
        for (int i = 0; i < outliers.length; ++i) {
            int[] cur = outliers[i];
//            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
        }
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    @Test
    public void testProdWn() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        SarimaOrders spec = new SarimaOrders(12);
 //       spec.airline(true);
        spec.setBd(1);
        spec.setD(1);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();
//        System.out.println("WN");
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 500; ++i) {
        FastOutliersDetector od = FastOutliersDetector.builder()
                .singleOutlierDetector(FastOutliersDetector.defaultOutlierDetector())
                .criticalValue(3)
                 .processor(RegArimaUtility.processor(true, 1e-7))
               .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder().y(Doubles.of(Data.PROD)).arima(sarima).build();
        od.prepare(regarima.getObservationsCount());
        od.process(regarima, SarimaMapping.of(spec));
        int[][] outliers = od.getOutliers();
//        for (int i = 0; i < outliers.length; ++i) {
//            int[] cur = outliers[i];
//            System.out.println(od.getFactory(cur[1]).getCode() + '-' + start.plus(cur[0]).display());
//        }
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    @Test
    public void testProdLegacy() {

//        System.out.println("Legacy");
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 500; ++i) {
        ec.tstoolkit.modelling.arima.tramo.OutliersDetector od = new ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        od.setDefault();
        od.setCriticalValue(3);
        od.useEML(true);
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();

        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        od.process(context);
        List<IOutlierVariable> outliers = context.description.getOutliers();
        int n = outliers.size();
//        for (IOutlierVariable o : outliers) {
//            System.out.println(o.getName());
//        }
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }
    
}
