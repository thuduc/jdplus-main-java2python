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
package jdplus.tramoseats.base.core.tramo;

import tck.demetra.data.Data;
import ec.tstoolkit.modelling.DefaultTransformationType;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class LogLeveModuleTest {

    public LogLeveModuleTest() {
    }

    @Test
    public void testProd() {

//        long t0 = System.currentTimeMillis();
//        System.out.println("New");
//        for (int i = 0; i < 1000; ++i) {
        LogLevelModule ll = LogLevelModule.builder()
                .estimationPrecision(1e-7)
                .build();
        ll.process(DoubleSeq.of(Data.PROD), 12, FastMatrix.EMPTY, true, null);
        assertTrue(ll.isChoosingLog());
//        System.out.println(ll.getLevelLL());
//        System.out.println(ll.getLogCorrection());
//        System.out.println(ll.getLogLL());
//        System.out.println(ll.isChoosingLog());
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

//    @Test
    public void testProdLegacy() {

//        long t0 = System.currentTimeMillis();
//        System.out.println("Legacy");
//        for (int i = 0; i < 1000; ++i) {
        ec.tstoolkit.modelling.arima.tramo.LogLevelTest ll = new ec.tstoolkit.modelling.arima.tramo.LogLevelTest();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
        desc.setTransformation(DefaultTransformationType.Auto);
        context.description = desc;
        context.hasseas = true;
        ll.process(context);
        System.out.println(ll.getLevelLL());
        System.out.println(ll.getLogCorrection());
        System.out.println(ll.getLogLL());
        System.out.println(ll.isChoosingLog());
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    public static void testInseeRecursive() {
        TsData[] all = Data.insee();
        for (int i = 0; i < all.length; ++i) {
            for (int j = 0; j < 36; ++j) {
                LogLevelModule ll = LogLevelModule.builder()
                        .logPreference(0)
                        .estimationPrecision(1e-7)
                        .build();
                ll.process(all[i].drop(0, j).getValues(), 12, FastMatrix.EMPTY, true, null);
                System.out.print(ll.isChoosingLog() ? 1 : 0);
                System.out.print('\t');
            }
            System.out.println();
        }
    }

    public static void testInsee() {
        TsData[] all = Data.insee();
        for (int i = 0; i < all.length; ++i) {
            LogLevelModule ll = LogLevelModule.builder()
                    .logPreference(0)
                    .estimationPrecision(1e-7)
                    .build();
            ll.process(all[i].getValues(), 12, FastMatrix.EMPTY, true, null);
            System.out.print(ll.getLogLL());
            System.out.print('\t');
            System.out.println(ll.getLevelLL());
        }
    }
    
    public static void main(String[] args){
        testInsee();
        testInseeRecursive();
    }
}
