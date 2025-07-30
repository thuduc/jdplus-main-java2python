/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.filter;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.x13.base.core.x11.X11Utility;
import jdplus.x13.base.core.x11.X11Context;
import static jdplus.x13.base.core.x11.X11Kernel.table;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Thomas Witthohn
 */
public class AutomaticHenderson {

    public static double calcICR(X11Context context, DoubleSeq s) {
        int freq = context.getPeriod();
        int filterLength = freq + 1;
        SymmetricFilter trendFilter = context.trendFilter(filterLength);

        int ndrop = filterLength / 2;
        double[] x = table(s.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        trendFilter.apply(s, out);

        DoubleSeq sc = out;
        DoubleSeq si = context.remove(s.extract(ndrop, sc.length()), sc);
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        sc = sc.drop(nb, nf);
        si = si.drop(nb, nf);
        double gc = X11Utility.calcAbsMeanVariation(sc, 1, context.isMultiplicative());
        double gi = X11Utility.calcAbsMeanVariation(si, 1, context.isMultiplicative());
        double icr = gi / gc;
        if (freq == 4) {
            icr *= 3.0;
        } else if (freq == 2) {
            icr *= 6.0;
        }
        return icr;

    }

    public static int selectFilter(double icr, final int freq) {
        if (freq == 2) {
            return 5;
        }
        if (icr >= 1 && icr < 3.5) {
            return freq + 1;
        }
        if (icr < 1) {
            if (freq == 12) {
                return 9;
            } else {
                return 5;
            }
        } else {
            return freq == 12 ? 23 : 7;
        }
    }
}
