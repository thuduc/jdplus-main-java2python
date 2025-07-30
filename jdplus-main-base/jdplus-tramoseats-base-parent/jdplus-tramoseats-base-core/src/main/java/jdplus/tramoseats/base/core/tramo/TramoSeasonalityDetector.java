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

import jdplus.sa.base.core.tests.SeasonalityTests;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.regsarima.regular.SeasonalityDetector;

/**
 *
 * @author Jean Palate
 */
public class TramoSeasonalityDetector implements SeasonalityDetector{


    private SeasonalityTests tests;
    private int ost;

    public SeasonalityTests getTests() {
        return tests;
    }

    @Override
    public Seasonality hasSeasonality(DoubleSeq y, int period) {
        ost = 0;
        if (period <= 1) {
            return Seasonality.NotApplicable;
        }
        tests = new SeasonalityTests();
        tests.test(y, period, 1, true);
        int ost95 = 0;
        int cqs = 0, cnp = 0;
        StatisticalTest qs = tests.getQs();
        StatisticalTest np = tests.getNonParametricTest();
        if (qs.getPvalue() < .01) {
            cqs = 2;
            ++ost;
            ++ost95;
        } else if (qs.getPvalue() < .05) {
            cqs = 1;
            ++ost95;
        }
        if (np.getPvalue() < .01) {
            cnp = 2;
            ++ost;
            ++ost95;
        } else if (np.getPvalue() < .05) {
            cnp = 1;
            ++ost95;
        }
        if (cqs == 2)
            return Seasonality.Strong;
        else if (ost95 == 2)
            return Seasonality.Moderate;
        else if (ost == 1 || ost95 == 1)
            return Seasonality.Weak;
        else
            return Seasonality.NotObservable;
    }
}
