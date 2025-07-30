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
package jdplus.toolkit.base.core.math.linearfilters;

import nbbrd.design.Development;
import java.util.HashMap;
import java.util.Map;
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public final class HendersonFilters {

    private final Map<Integer, SymmetricFilter> FILTERSTORE=new HashMap<>();

    /**
     * 
     * @param length
     * @return
     */
    public synchronized SymmetricFilter ofLength(int length) {
        SymmetricFilter filter=FILTERSTORE.get(length);
        if (filter != null)
            return filter;
	if (length % 2 == 0)
	    throw new LinearFilterException(
		    "Invalid length for Henderson filter. Should be odd");
	int m = length / 2;
	double[] c = new double[m + 1];
	int n = m + 2;

	double n2 = n * n;
	for (int i = 0; i < m + 1; i++) {
	    double ii = (i - m) * (i - m);
	    double up = 315 * (n2 - n * 2 + 1 - (ii));
	    up *= n2 - ii;
	    up *= n2 + n * 2 + 1 - ii;
	    up *= n2 * 3 - 16 - ii * 11;
	    double down = n * 8;
	    down *= n2 - 1;
	    down *= n2 * 4 - 1;
	    down *= n2 * 4 - 9;
	    down *= n2 * 4 - 25;
	    c[m - i] = up / down;
	}
	filter=SymmetricFilter.ofInternal(c);
        FILTERSTORE.put(length, filter);
        return filter;
    }
    
    public IQuasiSymmetricFiltering of(HendersonSpec spec){

        SymmetricFilter sf=ofLength(1+2*spec.getFilterHorizon());
        IFiniteFilter[] rf = AsymmetricFiltersFactory.musgraveFilters(sf, spec.getRightIcRatio());
        IFiniteFilter[] lf = ISymmetricFiltering.mirror(spec.isSymmetric() ? rf :AsymmetricFiltersFactory.musgraveFilters(sf, spec.getLeftIcRatio()));
        return new QuasiSymmetricFiltering(sf, lf, rf);
    }
}
