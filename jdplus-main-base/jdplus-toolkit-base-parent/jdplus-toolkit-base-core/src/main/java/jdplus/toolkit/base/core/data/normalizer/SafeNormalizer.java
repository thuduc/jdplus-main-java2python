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

package jdplus.toolkit.base.core.data.normalizer;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import nbbrd.design.Development;

/**
 * Normalization based on the mean of the absolute values. 
 * The scaling factor is the inverse of the mean of the absolute values of the data.
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@AlgorithmImplementation(algorithm=DataNormalizer.class)
public class SafeNormalizer implements DataNormalizer {

    @Override
    public double normalize(DataBlock data) {

        double s = 0;
	final int n = data.length();
        int m=0;
        
        
	for (int i = 0; i < n; ++i) {
	    double d = data.get(i);
	    if (Double.isFinite(d)) {
		s += Math.abs(d);
		++m;
	    }
	}
	if (s == 0 )
	    return 1;
        double am=s/m;
        if (am >= 1 && am <=2)
            return 1;
        double c=1;
        if (am >2){
            do{
                c/=2;
                am/=2;
            }while (am>2);
        }else{
            // am<1
            do{
                c*=2;
                am*=2;
            } while (am<1);
        }
        data.mul(c);
        return c;
    }
}
