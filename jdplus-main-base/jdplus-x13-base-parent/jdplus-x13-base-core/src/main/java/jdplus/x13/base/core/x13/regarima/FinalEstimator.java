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
package jdplus.x13.base.core.x13.regarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import static jdplus.toolkit.base.core.math.linearfilters.FilterUtility.checkRoots;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.regsarima.regular.IModelEstimator;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FinalEstimator implements IModelEstimator {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(FinalEstimator.class)
    public static class Builder {

        private double epsilon = .0001, tsig = 1;
        private boolean ami = false;

        public Builder precision(double precision) {
            this.epsilon = precision;
            return this;
        }

        public Builder ami(boolean ami) {
            this.ami = ami;
            return this;
        }

        public Builder tsig(double tsig) {
            this.tsig = tsig;
            return this;
        }

        public FinalEstimator build() {
            return new FinalEstimator(epsilon, tsig, ami);
        }

    }

    private final double tsig;
    private final double eps;
    private final boolean ami;

    private FinalEstimator(double eps, double tsig, boolean ami) {
        this.eps = eps;
        this.tsig = tsig;
        this.ami = ami;
    }

    @Override
    public boolean estimate(RegSarimaModelling context) {

        int niter = 0;
        do {
            try {
                IParametricMapping<SarimaModel> mapping = context.getDescription().mapping();
                int ndim = mapping.getDim();
                RegSarimaComputer processor = RegSarimaComputer.builder()
                        .minimizer(LevenbergMarquardtMinimizer.builder())
                        .precision(eps)
                        .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                        .computeExactFinalDerivatives(true)
                        .build();
                context.getDescription().freeArimaParameters();
                context.estimate(processor);
                if (ndim == 0) {
                    return true;
                }
                if (!ami) {
                    return true;
                }
                int itest = test(context);
                if (itest == 0) {
                    return true;
                } else if (itest > 1) {
                    return false;
                }
            } catch (RuntimeException err) {
                return false;
            }
        } while (niter++ < 5);
        return false;
    }

    private int test(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        double cval = tsig;
        int nz = desc.getEstimationDomain().getLength();
        double cmin = nz <= 150 ? .15 : .1;
        double cmod = .95;
        double bmin = 999;

        SarimaModel m = desc.arima();
        SarimaOrders spec = m.orders();
        DoubleSeq pm = m.parameters();
        int start = 0, len = spec.getP();
        boolean dpr = len > 0 && checkRoots(pm.extract(start, len), 1 / cmod);// (m.RegularAR.Roots,
        start += len;
        len = spec.getBp();
        boolean dps = len > 0 && checkRoots(pm.extract(start, len), 1 / cmod);// SeasonalAR.Roots,
        start += len;
        len = spec.getQ();
        boolean dqr = len > 0 && checkRoots(pm.extract(start, len), 1 / cmod);// RegularMA.Roots,
        start += len;
        len = spec.getBq();
        boolean dqs = len > 0 && checkRoots(pm.extract(start, len), 1 / cmod);// SeasonalMA.Roots,
        if (!dpr && !dps && !dqr && !dqs) {
            return 0; // nothing to do
        }
        int cpr = 0, cps = 0, cqr = 0, cqs = 0;
        double tmin = cval;
        DataBlock diag = context.getEstimation().getMax().asymptoticCovariance().diagonal();

        int k = -1;
        if (dpr) {
            k += spec.getP();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    ++cpr;
                    bmin = t;
                }
            }
        }
        if (dps) {
            k += spec.getBp();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cps;
                        bmin = t;
                        cpr = 0;
                    }
                }
            }
        }
        if (dqr) {
            k += spec.getQ();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqr;
                        bmin = t;
                        cpr = 0;
                        cps = 0;
                    }
                }
            }
        }
        if (dqs) {
            k += spec.getBq();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqs;
                        cpr = 0;
                        cps = 0;
                        cqr = 0;
                    }
                }
            }
        }

        int nnsig = cpr + cps + cqr + cqs;
        if (nnsig == 0) {
            return 0;
        }

        // reduce the orders
        if (cpr > 0) {
            spec.setP(spec.getP() - cpr);
        } else if (cps > 0) {
            spec.setBp(spec.getBp() - cps);
        } else if (cqr > 0) {
            spec.setQ(spec.getQ() - cqr);
        } else if (cqs > 0) {
            spec.setBq(spec.getBq() - cqs);
        }

        context.setSpecification(spec);
        return nnsig;
    }

}
