/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;

/**
 *
 * @author Jean Palate
 */
public interface ISsfData extends DoubleSeq {

    /**
     *
     * @param pos
     * @return
     */
    boolean isMissing(int pos);

    /**
     *
     * @param pos
     * @return
     */
    default boolean isConstraint(int pos){
        return false;
    }

    default int getObsCount() {
        int nm = 0, n = length();
        for (int i = 0; i < n; ++i) {
            if (isMissing(i)) {
                ++nm;
            }
        }
        return n - nm;
    }

    default boolean hasMissingValues() {
        int n = length();
        for (int i = 0; i < n; ++i) {
            if (isMissing(i)) {
                return true;
            }
        }
        return false;
    }

    default double scale() {
        int n = this.length();
        int nd = 0;
        double sabs = 0;
        DoubleSeqCursor cursor = cursor();
        for (int i = 0; i < n; ++i) {
            double c = cursor.getAndNext();
            if (Double.isFinite(c)) {
                sabs += Math.abs(c);
            } else {
                ++nd;
            }
        }
        if (nd == n) {
            return 0;
        } else {
            return sabs / (n - nd);
        }
    }
}
