package jdplus.toolkit.base.core.regsarima.regular;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.LengthOfPeriod;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author palatej
 */
public class TradingDaysRegressionComparatorTest {

    public TradingDaysRegressionComparatorTest() {
    }

    @Test
    public void testRetail() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .test(model, TradingDaysRegressionComparator.ALL, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
//        for (int i=0; i<test.length; ++i){
//            System.out.println(test[i].statistics().getAIC());
//        }
        int bestModel = TradingDaysRegressionComparator.bestModel(test, TradingDaysRegressionComparator.bicComparator());
        assertTrue(bestModel == 4);
    }

   @Test
    public void testRetailNoLp() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .test(model, TradingDaysRegressionComparator.ALL, 
                        null, 1e-5);
//        for (int i=0; i<test.length; ++i){
//            System.out.println(test[i].statistics().getAIC());
//        }
        int bestModel = TradingDaysRegressionComparator.bestModel(test, TradingDaysRegressionComparator.bicComparator());
        assertTrue(bestModel == 4);
    }

    @Test
    public void testWaldRetail() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 4);
    }
    
    @Test
    public void testWaldRetailNoLp() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        null, 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 4);
    }

    @Test
    public void testWaldProd() {
        TsData s = Data.TS_PROD;
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 5);
    }
    
   @Test
    public void testWaldABS() {
        TsData s = Data.TS_ABS_RETAIL;
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 5);
    }
}
