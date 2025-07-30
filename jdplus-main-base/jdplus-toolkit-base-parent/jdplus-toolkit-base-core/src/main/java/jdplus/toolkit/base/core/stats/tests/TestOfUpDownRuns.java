/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.stats.tests;

import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.dstats.Normal;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.stats.StatException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TestOfUpDownRuns {

    private static double dfact(double x, final int k) {
        for (int i = 2; i <= k; ++i) {
            if (x == 0) {
                return 0;
            } else {
                x /= i;
            }
        }
        return x;
    }

    public TestOfUpDownRuns(DoubleSeq data) {
        obs = data.select(x -> Double.isFinite(x));
    }

    private int nruns;
    private int[] runLengths;
    private DoubleSeq obs;

    private void races() {
        if (runLengths != null) {
            return;
        }
        int n = obs.length();
        runLengths = new int[n - 1];
        nruns = 1;
        if (n < 2) {
            throw new StatException(StatException.NOT_ENOUGH_DATA);
        }
        DoubleSeqCursor reader = obs.cursor();
        double o0 = reader.getAndNext(), o1 = reader.getAndNext();
        boolean up = o1 >= o0;
        int curlength = 1;
        for (int i = 2; i < n; ++i) {
            o0 = o1;
            o1 = reader.getAndNext();
            boolean curup = o1 >= o0;
            if (up != curup) {
                ++nruns;
                up = curup;
                ++runLengths[curlength - 1];
                curlength = 1;
            } else {
                ++curlength;
            }
        }
        ++runLengths[curlength - 1];
    }

    /**
     *
     * @param length
     * @return
     */
    public int runsCount(final int length) {
        return (length <= 0) ? nruns : runLengths[length - 1];
    }

    public StatisticalTest testLength() {
        races();
        int n = obs.length();
        double x = 0;
        for (int i = 1; i < n; ++i) {
            double ei = 0;
            if (i != n - 1) {
                ei = 2 * (n * (i * i + 3 * i + 1) - (i * i * i + 3 * i * i - i - 4));
                ei = dfact(ei, i + 3);
            } else {
                ei = dfact(2.0, n);
            }

            double oi = runLengths[i - 1];
            if (oi == 0) {
                x += ei;
            } else if (ei != 0) {
                x += (oi - ei) / ei * (oi - ei);
            } else {
                x += 999999;
            }
        }

        Chi2 dist = new Chi2(n - 1);
        return TestsUtility.testOf(x, dist, TestType.Upper);
    }

    public StatisticalTest testNumber() {
        races();
        double n = obs.length();
        double E = (2 * n - 1) / 3;
        double V = (16 * n - 29) / 90;

        Normal dist = new Normal();
        return TestsUtility.testOf((nruns - E) / Math.sqrt(V), dist, TestType.TwoSided);
    }

}
