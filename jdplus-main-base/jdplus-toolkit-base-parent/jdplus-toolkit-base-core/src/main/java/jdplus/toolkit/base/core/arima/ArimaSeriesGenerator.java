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
package jdplus.toolkit.base.core.arima;

import jdplus.toolkit.base.api.dstats.ContinuousDistribution;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Immutable;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.RationalBackFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import lombok.NonNull;
import jdplus.toolkit.base.api.dstats.Distribution;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class ArimaSeriesGenerator {

    @BuilderPattern(ArimaSeriesGenerator.class)
    public static class Builder {

        private int ndrop = 0;
        private double startMean = 100;
        private double startStdev = 10;
        private final RandomNumberGenerator rng;
        private ContinuousDistribution dist = new Normal();

        private Builder() {
            rng = XorshiftRNG.fromSystemNanoTime();
        }

        private Builder(RandomNumberGenerator rng) {
            this.rng = rng;
        }

        /**
         * Number of initial random numbers that are dropped
         *
         * @param n
         * @return
         */
        public Builder initialWarmUp(int n) {
            ndrop = n;
            return this;
        }

        /**
         * Distribution used for generating the innovations
         *
         * @param distribution
         * @return
         */
        public Builder distribution(ContinuousDistribution distribution) {
            dist = distribution;
            return this;
        }

        public Builder startMean(double mean) {
            this.startMean = mean;
            return this;
        }

        public Builder startStdev(double stdev) {
            this.startStdev = stdev;
            return this;
        }

        public ArimaSeriesGenerator build() {
            return new ArimaSeriesGenerator(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(@NonNull RandomNumberGenerator rng) {
        return new Builder(rng);
    }

    private final int initialdrop;
    private final double startMean;
    private final double startStdev;
    private final RandomNumberGenerator rng;
    private final ContinuousDistribution distribution;

    public ArimaSeriesGenerator() {
        this(new Builder());
    }

    private ArimaSeriesGenerator(final Builder builder) {
        initialdrop = builder.ndrop;
        startMean = builder.startMean;
        startStdev = builder.startStdev;
        this.rng = builder.rng.synchronize();
        this.distribution = builder.dist;
    }

    /**
     *
     * @param arima
     * @param n
     * @return
     */
    public double[] generate(final IArimaModel arima, final int n) {
        return generate(arima, 0, n);
    }

    /**
     *
     * @param arima
     * @param mean
     * @param n
     * @return
     */
    public double[] generate(final IArimaModel arima, final double mean, final int n) {
        try {
            StationaryTransformation stm = arima.stationaryTransformation();
            double[] tmp = generateStationary((IArimaModel) stm.getStationaryModel(), mean, n + initialdrop);
            double[] w;
            if (initialdrop == 0) {
                w = tmp;
            } else {
                w = new double[n];
                System.arraycopy(tmp, initialdrop, w, 0, n);
            }
            if (stm.getUnitRoots().isIdentity()) {
                return w;
            } else {
                Polynomial P = stm.getUnitRoots().asPolynomial();
                double[] yprev = new double[P.degree()];
                if (startStdev != 0) {
                    Normal normal = new Normal(startMean, startStdev);

                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = normal.random(rng);
                    }
                } else if (startMean != 0) {
                    for (int i = 0; i < yprev.length; ++i) {
                        yprev[i] = startMean;
                    }
                }

                for (int i = 0; i < n; ++i) {
                    double y = w[i];
                    for (int j = 1; j <= P.degree(); ++j) {
                        y -= yprev[j - 1] * P.get(j);
                    }
                    w[i] = y;
                    for (int j = yprev.length - 1; j > 0; --j) {
                        yprev[j] = yprev[j - 1];
                    }
                    if (yprev.length > 0) {
                        yprev[0] = y;
                    }
                }
                return w;
            }
        } catch (ArimaException ex) {
            return null;
        }
    }

    public double[] generateStationary(final IArimaModel starima, final int n) {
        return generateStationary(starima, 0, n);
    }

    public double[] generateStationary(final IArimaModel starima, final double mean, final int n) {

        BackFilter ar = starima.getAr(), ma = starima.getMa();
        int p = ar.getDegree(), q = ma.getDegree();
        double[] y = new double[p], e = new double[q];
        if (p == 0) {
            for (int i = 0; i < q; ++i) {
                e[i] = distribution.random(rng);
            }
        } else {
            // ac = cov(y(-1), y(-2)...e(-1), e(-2)...)
            FastMatrix ac = FastMatrix.square(p + q);
            AutoCovarianceFunction acf = starima.getAutoCovarianceFunction();
            acf.prepare(p);
            // fill the p part
            FastMatrix pm = ac.extract(0, p, 0, p);
            pm.diagonal().set(acf.get(0));
            for (int i = 1; i < p; ++i) {
                pm.subDiagonal(-i).set(acf.get(i));
            }
            if (q > 0) {
                FastMatrix qm = ac.extract(p, q, p, q);
                qm.diagonal().set(starima.getInnovationVariance());
                FastMatrix qp = ac.extract(p, q, 0, p);
                RationalBackFilter psi = starima.getPsiWeights();
                int nw = Math.min(q, p);
                psi.prepare(q);
                DataBlock w = DataBlock.of(psi.getWeights(q));
                for (int i = 0; i < nw; ++i) {
                    qp.column(i).drop(i, 0).copy(w.drop(0, i));
                }
                qp.mul(starima.getInnovationVariance());
            }
            SymmetricMatrix.fromLower(ac);
            SymmetricMatrix.lcholesky(ac, 1e-6);
            double[] x = new double[p + q];
            for (int i = 0; i < x.length; ++i) {
                x[i] = distribution.random(rng);
            }
            LowerTriangularMatrix.Lx(ac, DataBlock.of(x));
            System.arraycopy(x, 0, y, 0, p);
            if (q > 0) {
                System.arraycopy(x, p, e, 0, q);
            }
        }

        double[] z = new double[n];
        double std = Math.sqrt(starima.getInnovationVariance());
        Polynomial theta = ma.asPolynomial(), phi = ar.asPolynomial();
        for (int i = 0; i < n; ++i) {
            double u = distribution.random(rng) * std;
            double t = mean + u * theta.get(0);
            for (int j = 1; j <= q; ++j) {
                t += e[j - 1] * theta.get(j);
            }
            for (int j = 1; j <= p; ++j) {
                t -= y[j - 1] * phi.get(j);
            }

            t /= phi.get(0);
            z[i] = t;

            if (e.length > 0) {
                for (int j = e.length - 1; j > 0; --j) {
                    e[j] = e[j - 1];
                }
                e[0] = u;
            }
            if (y.length > 0) {
                for (int j = y.length - 1; j > 0; --j) {
                    y[j] = y[j - 1];
                }
                y[0] = t;
            }
        }
        return z;
    }

    public static double[] generate(IArimaModel model, int n, double[] initial, ContinuousDistribution distribution, int warmup) {
        Polynomial phi = model.getAr().asPolynomial();
        int p = phi.degree();
        if (initial.length < p) {
            throw new IllegalArgumentException();
        }
        Polynomial theta = model.getMa().asPolynomial();
        int q = theta.degree();
        RandomNumberGenerator rng = XorshiftRNG.fromSystemNanoTime();
        double[] z = new double[n];
        double[] e = new double[q];
        double[] y = new double[p];
        
        for (int i=0, j=initial.length-1; i<p; ++i, --j){
            y[i]=initial[j];
        }
        
        for (int i = 0; i < n + warmup; ++i) {
            double u = distribution.random(rng);
            double t = u * theta.get(0);
            for (int j = 1; j <= q; ++j) {
                t += e[j - 1] * theta.get(j);
            }
            for (int j = 1; j <= p; ++j) {
                t -= y[j - 1] * phi.get(j);
            }

            t /= phi.get(0);
            if (q > 0) {
                for (int j = q - 1; j > 0; --j) {
                    e[j] = e[j - 1];
                }
                e[0] = u;
            }
            if (p > 0) {
                for (int j = p - 1; j > 0; --j) {
                    y[j] = y[j - 1];
                }
                y[0] = t;
            }
            if (i >= warmup) {
                z[i - warmup] = t;
            }
        }
        return z;

    }

}
