/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.extremevaluecorrector;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.x13.base.api.x11.SigmaVecOption;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Christiane Hofer
 */
public class GroupSpecificExtremeValuesCorrector extends PeriodSpecificExtremeValuesCorrector {

    private final SigmaVecOption[] sigmavecOption_;

    public GroupSpecificExtremeValuesCorrector(SigmaVecOption[] sigmavecOption) {
        super();
        sigmavecOption_ = sigmavecOption;
    }

    @Override
    protected double[] calcStdev(DoubleSeq s) {
        DataBlock db = DataBlock.of(s);
        if (excludeFcast) {
            db = db.drop(0, forecastHorizon);
        }

        Integer np = period;
        double[] stdev = new double[np];
        double stdvGroup1 = 0;
        double stdvGroup2 = 0;
        int nGroup1 = 0; //Count of numbers in group one
        int nGroup2 = 0;//
        double eGroup1 = 0;
        double eGroup2 = 0;
        for (int i = 0; i < period; i++) {
            int j = ((period - start) % period + i) % period;
            DataBlock dbPeriod = db.extract(j, -1, period);
            if (sigmavecOption_[i].equals(SigmaVecOption.Group1)) {
                for (int k = 0; k < dbPeriod.length(); k++) {
                    double x = dbPeriod.get(k);
                    if (!Double.isNaN(x)) {
                        nGroup1 = nGroup1 + 1;
                        if (mul) {
                            x -= 1;
                        }
                        eGroup1 += x * x;
                    }
                }

            } else if (sigmavecOption_[i].equals(SigmaVecOption.Group2)) {
                for (int k = 0; k < dbPeriod.length(); k++) {
                    double x = dbPeriod.get(k);
                    if (!Double.isNaN(x)) {
                        nGroup2 = nGroup2 + 1;
                        if (mul) {
                            x -= 1;
                        }
                        eGroup2 += x * x;
                    }
                }

            }

        }

        stdvGroup1 = Math.sqrt(eGroup1 / nGroup1);
        stdvGroup2 = Math.sqrt(eGroup2 / nGroup2);

        for (int i = 0; i < np; i++) {
            if (sigmavecOption_[i].equals(SigmaVecOption.Group1)) {
                stdev[i] = stdvGroup1;
            } else if (sigmavecOption_[i].equals(SigmaVecOption.Group2)) {
                stdev[i] = stdvGroup2;
            }

        }

        return stdev;
    }
}
