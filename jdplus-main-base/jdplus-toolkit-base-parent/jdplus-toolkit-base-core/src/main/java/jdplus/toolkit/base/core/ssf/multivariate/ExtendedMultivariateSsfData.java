/*
* Copyright 2013 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ExtendedMultivariateSsfData implements IMultivariateSsfData {

    private final int nbcasts;
    private final int nfcasts;
    private final IMultivariateSsfData data;

    /**
     *
     * @param data
     * @param bcasts
     * @param fcasts
     */
    public ExtendedMultivariateSsfData(final IMultivariateSsfData data, final int bcasts, final int fcasts) {
        this.data = data;
        nfcasts = fcasts;
        nbcasts = bcasts;
    }

    /**
     *
     * @param n
     * @return
     */
    @Override
    public double get(int pos, int v) {
        if (pos < nbcasts) {
            return Double.NaN;
        } else {
            return data.get(pos - nbcasts, v);
        }
    }

    /**
     *
     * @return
     */
    public int getBackcastsCount() {
        return nbcasts;
    }

    /**
     *
     * @return
     */
    @Override
    public int getObsCount() {
        return nbcasts + nfcasts + data.getObsCount();
    }

    /**
     *
     * @return
     */
    public int getForecastsCount() {
        return nfcasts;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(int pos, int v) {
        if (pos < nbcasts) {
            return true;
        }
        return data.isMissing(pos - nbcasts, v);
    }

    @Override
    public DoubleSeq get(int pos) {
        if (pos < nbcasts) {
            return DoubleSeq.empty();
        } else {
            return data.get(pos - nbcasts);
        }
    }

    @Override
    public boolean isConstraint(int pos, int v) {
        if (pos < nbcasts) {
            return false;
        }
        return data.isConstraint(pos - nbcasts, v);
    }

    @Override
    public int getVarsCount() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
