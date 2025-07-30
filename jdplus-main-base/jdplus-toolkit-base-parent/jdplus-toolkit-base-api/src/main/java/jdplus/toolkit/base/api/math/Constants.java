/*
 * Copyright 2021 National Bank of Belgium.
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
package jdplus.toolkit.base.api.math;

import nbbrd.design.Development;

/**
 * Some useful constants. The classes "Double" and "Math" provide other
 * important constants. For compatibility issues, this class continues to
 * provide (deprecated) wrappers around some constants of Double.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class Constants {

    // other useful constants
    public final double MAXLOG = 7.09782712893383996732e2;
    public final double MINLOG = -7.451332191019412076235e2;
    /**
     * 2*PI
     */
    public final double TWOPI = 2 * Math.PI;
    /**
     * PI/2
     */
    public final double PIO2 = Math.PI / 2;
    /**
     * sqrt(2*pi)
     */
    public final double SQTPI = 2.50662827463100050242e0;
    public final double SQRTH = 7.07106781186547524401e-1;
    public final double LOGPI = 1.14472988584940017414;
    public final double LOGTWOPI = Math.log(TWOPI);

    public final int EMIN=-1079;
    
    public final double BIG = 4.503599627370496e15;
    public final double BIGINV = 2.22044604925031308085e-16;
    public final double MACHEP = 1.11022302462515654042e-16;
    

    /**
     * DLAMCH('E')
     * Relative machine precision
     *
     * @return the value
     */
    public double getEpsilon() {
        return MACHEP;
    }
    
    /**
     * DLAMCH('P')
     *
     * @return the value
     */
    public double getPrecision() {
        return MACHEP*2;
    }
    
    /**
     * DLAMCH('S')
     * Safe minimun (such that 1/sfmin doesn't overflow)
     * Same as Double.MIN_NORMAL
     * @return 
     */
    public double getSafeMin(){
        return Double.MIN_NORMAL;
    }
    
    /**
     * DLAMCH('S')
     * Inverse of Double.MAX_VALUE
     * @return 
     */
    public double getSmall(){
        return 1/Double.MAX_VALUE;
    }
    
    /**
     * DLAMCH('O')
     * @return 
     */
    public double getOverflowThreshold(){
        return Double.MAX_VALUE;
    }

    /**
     * DLAMCH('U')
     * @return 
     */
    public double getUnderflowThreshold(){
        return Double.MIN_VALUE;
    }

    /**
     * Checks if Math#exp(double) has been intrinsified. For your information:
     * StrictMath insures portability by returning the same results on every
     * platform while Math might be optimized by the VM to improve performance.
     * In some edge cases (and if intrinsified), Math results are slightly
     * different.
     *
     * @return true if Math is currently intrinsified, false otherwise
     */
    public boolean isMathExpIntrinsifiedByVM() {
        double edgeCase = 0.12585918361184556;
        return Math.exp(edgeCase) != StrictMath.exp(edgeCase);
    }
}
