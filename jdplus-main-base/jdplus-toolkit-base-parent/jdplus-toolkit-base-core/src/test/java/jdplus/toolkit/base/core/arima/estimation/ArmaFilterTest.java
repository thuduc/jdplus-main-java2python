/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.arima.estimation;

import tck.demetra.data.Data;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.arima.SarimaOrders;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import jdplus.toolkit.base.api.data.DoubleSeq;
import internal.toolkit.base.core.arima.KalmanFilter;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaUtility;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArmaFilterTest {

    private static final SarimaModel airline, arima, ar1, ma12;
    private static final DoubleSeq data;

    static {
        SarimaOrders spec=SarimaOrders.airline(12);
        airline = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.006).build();
        spec.setP(3);
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).phi(-.2, -.5, -.2).build();
        DataBlock s = DataBlock.copyOf(Data.PROD);
        DataBlock ds = DataBlock.make(s.length() - spec.getDifferenceOrder());
        SarimaUtility.getDifferencingFilter(spec).apply(s, ds);
        data = ds;
        SarimaOrders sar1=new SarimaOrders(12);
        sar1.setP(1);
        ar1=SarimaModel.builder(sar1).phi(-.95).build();
         SarimaOrders orders = SarimaOrders.airline(12);
        orders.setQ(0);
        SarimaModel m = SarimaModel.builder(orders)
                .btheta(-.9)
                .build();
        ma12 = m.stationaryTransformation().getStationaryModel();
   }

    public ArmaFilterTest() {

    }

    @Test
    public void testAirline() {
        
        ArmaFilter filter = ArmaFilter.ljungBox(true);
        int m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = ArmaFilter.kalman();
        m = filter.prepare((IArimaModel) airline.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-12);
        assertEquals(ssq, s.ssq(), 1e-12);
    }

    @Test
    public void testAr1() {
        
        ArmaFilter filter = ArmaFilter.ansley();
        int m = filter.prepare(ar1, data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = new KalmanFilter(true);
        m = filter.prepare(ar1, data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-12);
        assertEquals(ssq, s.ssq(), 1e-8);
    }

    @Test
    public void testMa12() {
        
        ArmaFilter filter = ArmaFilter.ansley();
        int m = filter.prepare(ma12, data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = new KalmanFilter(true);
        m = filter.prepare(ma12, data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-12);
        assertEquals(ssq, s.ssq(), 1e-8);
    }

    @Test
    public void testArima() {
        ArmaFilter filter = ArmaFilter.ljungBox(true);
        int m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        DataBlock s = DataBlock.make(m);
        filter.apply(data, s);
        double ldet = filter.getLogDeterminant(), ssq = s.ssq();
        filter = ArmaFilter.kalman();
        m = filter.prepare((IArimaModel) arima.stationaryTransformation().getStationaryModel(), data.length());
        s = DataBlock.make(m);
        filter.apply(data, s);
        assertEquals(ldet, filter.getLogDeterminant(), 1e-8);
        assertEquals(ssq, s.ssq(), 1e-6);
    }

    public static void stressTest() {
        System.out.println("Stress Test 1");
        int K = 100000;
        IArimaModel[] models = new IArimaModel[]{airline, arima};
        for (IArimaModel model : models) {
            long t0 = System.currentTimeMillis();
            ArmaFilter filter = ArmaFilter.ansley();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.kalman();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                DataBlock s = DataBlock.make(m);
                filter.apply(data, s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();

            ec.tstoolkit.arima.IArimaModel lmodel = toLegacy(model);

            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.AnsleyFilter a = new ec.tstoolkit.arima.estimation.AnsleyFilter();
            for (int i = 0; i < K; ++i) {
                int m = a.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                a.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(a.getClass().getName());
            System.out.println(t1 - t0);

            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.KalmanFilter kf = new ec.tstoolkit.arima.estimation.KalmanFilter();
            for (int i = 0; i < K; ++i) {
                int m = kf.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                kf.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(kf.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.LjungBoxFilter lb = new ec.tstoolkit.arima.estimation.LjungBoxFilter();
            for (int i = 0; i < K; ++i) {
                int m = lb.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                lb.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(lb.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter mlb = new ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter();
            for (int i = 0; i < K; ++i) {
                int m = mlb.initialize((ec.tstoolkit.arima.IArimaModel) lmodel.stationaryTransformation().stationaryModel, data.length());
                ec.tstoolkit.data.DataBlock s = new ec.tstoolkit.data.DataBlock(m);
                mlb.filter(new ec.tstoolkit.data.ReadDataBlock(data.toArray()), s);
            }
            t1 = System.currentTimeMillis();
            System.out.println(mlb.getClass().getName());
            System.out.println(t1 - t0);
        }
    }

    public static void stressTest2() {
        System.out.println("Stress Test 2");
        int K = 10000, L = 10;
        long t0 = System.currentTimeMillis();
        IArimaModel[] models = new IArimaModel[]{airline, arima};
        for (IArimaModel model : models) {
            ArmaFilter filter = ArmaFilter.ansley();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            long t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            filter = ArmaFilter.kalman();
            for (int i = 0; i < K; ++i) {
                int m = filter.prepare((IArimaModel) model.stationaryTransformation().getStationaryModel(), data.length());
                for (int j = 0; j < L; ++j) {
                    DataBlock s = DataBlock.make(m);
                    filter.apply(data, s);
                }
            }
            t1 = System.currentTimeMillis();
            System.out.println(filter.getClass().getName());
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();

        }
    }

    private static ec.tstoolkit.arima.IArimaModel toLegacy(IArimaModel model) {
        if (model instanceof SarimaModel) {
            SarimaModel sarima = (SarimaModel) model;
            SarimaOrders spec = sarima.orders();
            ec.tstoolkit.sarima.SarimaSpecification lspec = new ec.tstoolkit.sarima.SarimaSpecification(spec.getPeriod());
            lspec.setP(spec.getP());
            lspec.setD(spec.getD());
            lspec.setQ(spec.getQ());
            lspec.setBP(spec.getBp());
            lspec.setBD(spec.getBd());
            lspec.setBQ(spec.getBq());
            ec.tstoolkit.sarima.SarimaModel lmodel = new ec.tstoolkit.sarima.SarimaModel(lspec);
            lmodel.setParameters(new ec.tstoolkit.data.ReadDataBlock(sarima.parameters().toArray()));
            return lmodel;
        } else {
            return null;
        }
    }

    @Test
    @Disabled
    public void testAO() {
        int POS=35;
        double[] ao = new double[120];
        ao[POS] = 1;
        ArmaFilter filter = ArmaFilter.ansley();
        StationaryTransformation<SarimaModel> st = airline.stationaryTransformation();
        double[] dao = new double[ao.length - st.getUnitRoots().getDegree()];
        st.getUnitRoots().apply(DataBlock.of(ao), DataBlock.of(dao));
        int m = filter.prepare((IArimaModel) st.getStationaryModel(), dao.length);
        DataBlock s = DataBlock.make(m);
        filter.apply(DoubleSeq.of(dao), s);
        System.out.println(s);
        double[] coefficients = st.getStationaryModel().getPiWeights().getRationalFunction().coefficients(120);
        double[] q=coefficients.clone();
        int start=POS-st.getUnitRoots().getDegree();
        for (int j=start+1; j<dao.length; ++j){
            double a=dao[j];
            if (a != 0){
                for (int k=j; k<q.length; ++k){
                    q[k-start]+=a*coefficients[k-j];
                }
            }
        }
        System.out.println(DoubleSeq.of(q));
    }

    @Test
    @Disabled
    public void testLS() {
        int POS=35;
        double[] ls = new double[120];
        for (int i = 0; i < POS; ++i) {
            ls[i] = -1;
        }
        ArmaFilter filter = ArmaFilter.ansley();
        StationaryTransformation<SarimaModel> st = airline.stationaryTransformation();
        double[] dls = new double[ls.length - st.getUnitRoots().getDegree()];
        st.getUnitRoots().apply(DataBlock.of(ls), DataBlock.of(dls));
        int m = filter.prepare((IArimaModel) st.getStationaryModel(), dls.length);
        DataBlock s = DataBlock.make(m);
        filter.apply(DoubleSeq.of(dls), s);
        System.out.println(s);
        double[] coefficients = st.getStationaryModel().getPiWeights().getRationalFunction().coefficients(120);
        double[] q=coefficients.clone();
        int start=POS-st.getUnitRoots().getDegree();
        for (int j=start+1; j<dls.length; ++j){
            double a=dls[j];
            if (a != 0){
                for (int k=j; k<q.length; ++k){
                    q[k-start]+=a*coefficients[k-j];
                }
            }
        }
        System.out.println(DoubleSeq.of(q));
    }
    
    public static void main(String[] arg){
        stressTest();
        stressTest2();
    }
}
