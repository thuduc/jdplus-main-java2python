/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.tramoseats.base.api.tramo.MeanSpec;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import java.util.Arrays;
import jdplus.sa.base.core.SaBenchmarkingResults;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TramoSeatsKernelTest {

    public TramoSeatsKernelTest() {
    }

    @Test
    public void testProd() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSA5, null);
        ProcessingLog log = ProcessingLog.dummy();
        TsData s = TsData.ofInternal(TsPeriod.monthly(2001, 1), Data.RETAIL_ALLHOME);
        TramoSeatsResults rslt = ts.process(s, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsDiagnostics diags = rslt.getDiagnostics();
        assertTrue(diags != null);
//        System.out.println(rslt.getDecomposition().getInitialComponents());
//        System.out.println(rslt.getFinals());
//        Map<String, Class> dictionary = rslt.getDictionary();
//        dictionary.forEach((k, c)->{System.out.print(k);System.out.print('\t');System.out.println(c.getCanonicalName());});
    }

    @Test
    public void testProd0() {
        RegressionSpec.Builder rspec = TramoSeatsSpec.RSA0.getTramo().getRegression().toBuilder();
        rspec.mean(MeanSpec.DEFAULT_UNUSED);

        TramoSeatsSpec nspec = TramoSeatsSpec.RSA0.toBuilder()
                .tramo(TramoSeatsSpec.RSA0.getTramo().toBuilder()
                        .regression(rspec.build()).build()).build();

        TramoSeatsKernel ts = TramoSeatsKernel.of(nspec, null);
        ProcessingLog log = ProcessingLog.dummy();
        TsData s = TsData.ofInternal(TsPeriod.monthly(2001, 1), Data.RETAIL_ALLHOME);
        TramoSeatsResults rslt = ts.process(s, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsDiagnostics diags = rslt.getDiagnostics();
        assertTrue(diags != null);
    }

    @Test
    public void testProdLegacyMissing() {
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1992, 0, Data.RETAIL_FUELDEALERS, true);
        ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSAfull);
    }

    @Test
    public void testBenchmarking() {
        TramoSeatsSpec spec = TramoSeatsSpec.RSAfull.toBuilder()
                .benchmarking(
                        TramoSeatsSpec.RSAfull.getBenchmarking()
                                .toBuilder()
                                .enabled(true)
                                .build()
                ).build();
        TramoSeatsKernel ts = TramoSeatsKernel.of(spec, null);
        ProcessingLog log = ProcessingLog.dummy();
        TsData s = TsData.ofInternal(TsPeriod.monthly(2001, 1), Data.RETAIL_ALLHOME);
        TramoSeatsResults rslt = ts.process(s, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsDiagnostics diags = rslt.getDiagnostics();
        assertTrue(diags != null);
        SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
        assertTrue(benchmarking != null);
        TsDataTable table=TsDataTable.of(Arrays.asList(benchmarking.getSa(), benchmarking.getTarget(), benchmarking.getBenchmarkedSa()));
//        System.out.println(table);
    }

    @Test
    public void tesIPI() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI, log);
    }

    @Test
    public void tesIPI10() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI_10, log);
    }

    @Test
    public void tesIPI72() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.SP_IPI_72, log);
    }
}
