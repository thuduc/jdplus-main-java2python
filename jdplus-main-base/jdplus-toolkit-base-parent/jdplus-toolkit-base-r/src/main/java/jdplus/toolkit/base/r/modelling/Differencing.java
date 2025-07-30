/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.modelling.DifferencingResult;
import jdplus.toolkit.base.core.modelling.StationaryTransformation;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtosUtility;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.modelling.FastDifferencingModule;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Differencing {
    
    public double[] differences(double[] data, int[] dlags, boolean mean) {
        DataBlock z = DataBlock.of(data.clone());
        for (int i = 0; i < dlags.length; ++i) {
            z.autoApply(-dlags[i], (a, b) -> a - b);
            z = z.drop(dlags[i], 0);
        }
        if (mean) {
            z.sub(z.average());
        }
        return z.toArray();
    }
    
    public StationaryTransformation doStationary(double[] data, int period) {
        DifferencingResult dr = DifferencingResult.of(DoubleSeq.of(data), period, -1, true);
        return StationaryTransformation.builder()
                .meanCorrection(dr.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, dr.getDifferencingOrder()))
                .stationarySeries(dr.getDifferenced())
                .build();
    }
    
    public StationaryTransformation fastDifferencing(double[] data, int period, boolean mad, double centile, double k) {
        FastDifferencingModule diff = FastDifferencingModule.builder()
                .mad(mad)
                .centile(centile)
                .k(k)
                .build();
        DoubleSeq x = DoubleSeq.of(data);
        int[] D = diff.process(x, new int[]{1, period}, null);
        
        if (D[0] != 0) {
            x = x.delta(1, D[0]);
        }
        if (D[1] != 0) {
            x = x.delta(period, D[1]);
        }
        if (diff.isMeanCorrection()) {
            x = x.removeMean();
        }
        
        return StationaryTransformation.builder()
                .meanCorrection(diff.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, D[0]))
                .difference(new StationaryTransformation.Differencing(period, D[1]))
                .stationarySeries(x)
                .build();
    }
    
    public byte[] toBuffer(StationaryTransformation st) {
        return ModellingProtosUtility.convert(st).toByteArray();
    }
    
}
