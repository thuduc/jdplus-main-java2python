/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.core.data.transformation.LogJacobian;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataTransformation;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.core.timeseries.simplets.Transformations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LengthOfPeriodTransformationTest {
    
    public LengthOfPeriodTransformationTest() {
    }

    @Test
    public void testLengthOfPeriod() {
        DoubleSeq data=DoubleSeq.onMapping(300, i->1);
        TsPeriod start=TsPeriod.monthly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length(), null);
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
    
    @Test
    public void testLengthOfPeriodQ() {
        DoubleSeq data=DoubleSeq.onMapping(300, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 2);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length(), null);
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }

    @Test
    public void testLeapYear() {
        DoubleSeq data=DoubleSeq.onMapping(80, i->1);
        TsPeriod start=TsPeriod.quarterly(1980, 3);
        TsDataTransformation lp=Transformations.lengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
        LogJacobian lj=new LogJacobian(0, data.length(), null);
        TsData s=TsData.of(start, data);
        TsData s1=lp.transform(s, lj);
        TsData s2=lp.converse().transform(s1, lj);
        assertTrue(s2.getValues().allMatch(x->Math.abs(x-1)<1e-12));
        assertEquals(lj.value, 0, 1e-12);
    }
}
