/*
 * Copyright 2013 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.Constants;

/**
 * Transformation represented by 
 * |c  s|
 * |s -c|
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class GivensReflection implements IVectorTransformation {

    private final int lentry, rentry;
    private final double c, s;

    public static GivensReflection of(DataBlock vector, int lentry, int rentry) {
        return of(vector, lentry, rentry, true);
    }

    public static GivensReflection of(DataBlock vector, int lentry, int rentry, boolean apply) {
        double a = vector.get(lentry), b = vector.get(rentry);
        double absa = Math.abs(a), absb = Math.abs(b);
        if (absb < Constants.getEpsilon()) {
            if (apply) {
                vector.set(lentry, absa);
                vector.set(rentry, 0);
            }
            return new GivensReflection(lentry, rentry, a < 0 ? 1 : -1, 0);
        }
        double s, c, r;
        if (absa >= absb) {
            s = b / a;
            r = Math.sqrt(1 + s * s);
            if (a < 0) {
                c = -1 / r;
                s *= c;
                r *= -a;
            } else {
                c = 1 / r;
                s *= c;
                r *= a;
            }
        } else {
            c = a / b;
            r = Math.sqrt(1 + c * c);
            if (b < 0) {
                s = -1 / r;
                c *= s;
                r *= -b;
            } else {
                s = 1 / r;
                c *= s;
                r *= b;
            }
        }
        if (apply) {
            vector.set(lentry, r);
            vector.set(rentry, 0);
        }
        return new GivensReflection(lentry, rentry, c, s);
    }

    public static GivensReflection of(DataBlock vector, int entry) {
        return of(vector, 0, entry);
    }

    @Override
    public void transform(DataBlock vector) {
        // |c  s|
        // |s -c|
        double a = vector.get(lentry);
        double b = vector.get(rentry);
        vector.set(lentry, c * a + s * b);
        vector.set(rentry, s * a - c * b);
    }
}
