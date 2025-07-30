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
package jdplus.toolkit.base.core.regsarima.ami;

import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.regarima.estimation.ConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.regarima.outlier.SingleOutlierDetector;
import jdplus.toolkit.base.core.regarima.outlier.FastOutlierDetector;
import jdplus.toolkit.base.core.sarima.estimation.HannanRissanen;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.TransitoryChangeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.arima.estimation.IArimaMapping;
import jdplus.toolkit.base.core.regarima.ami.GenericOutliersDetection;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FastOutliersDetector implements GenericOutliersDetection<SarimaModel> {

    public static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(){
        FastOutlierDetector detector=new FastOutlierDetector(null);
        detector.setOutlierFactories(AdditiveOutlierFactory.FACTORY,
                LevelShiftFactory.FACTORY_ZEROSTARTED,
                new TransitoryChangeFactory(.7));
        return detector;
    }

    static int DEF_MAXROUND = 100;
    static int DEF_MAXOUTLIERS = 50;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(FastOutliersDetector.class)
    public static class Builder {

        private double cv = 0;
        private boolean mvx;
        private IRegArimaComputer<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private SingleOutlierDetector<SarimaModel> sod;

        private Builder() {
        }

        public Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        public Builder maximumLikelihood(boolean mvx) {
            this.mvx = mvx;
            return this;
        }

        public Builder processor(IRegArimaComputer<SarimaModel> processor) {
            this.processor = processor;
            return this;
        }

        public Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        public Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public Builder singleOutlierDetector(SingleOutlierDetector<SarimaModel> sod) {
            this.sod=sod;
            return this;
        }

        public FastOutliersDetector build() {
            return new FastOutliersDetector(sod, processor, maxRound, maxOutliers, cv, mvx);
        }
    }

    private final int maxRound, maxOutliers;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final SingleOutlierDetector sod;
    private final IRegArimaComputer<SarimaModel> processor;
    private final double cv;
    private final boolean mvx;

    private RegArimaModel<SarimaModel> regarima;
    private double[] tstats;
    private int round;
    // festim = true if the model has to be re-estimated
    private boolean rflag, backwardStep, exit, estimationStep;
    private int[] lastremoved;
    private DoubleSeq coeff, res;
    //

    private FastOutliersDetector(final SingleOutlierDetector sod, final IRegArimaComputer<SarimaModel> processor,
            final int maxOutliers, final int maxRound, final double cv, final boolean mvx) {
        this.sod = sod;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
        this.mvx = mvx;
        this.cv = cv;
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
    }

    @Override
    public void prepare(int n) {
        sod.prepare(n);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    /**
     * @return the outliers (position/type) entries
     */
    @Override
    public int[][] getOutliers() {
        return outliers.toArray(int[][]::new);
    }

    public IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

    public String[] outlierTypes() {
        IOutlierFactory[] factories = sod.getOutliersFactories();
        String[] types = new String[factories.length];
        for (int i = 0; i < types.length; ++i) {
            types[i] = factories[i].getCode();
        }
        return types;
    }
    
    public RegArimaModel<SarimaModel> getRegArima(){
        return regarima;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel, IArimaMapping<SarimaModel> mapping) {
        clear();
        int n = initialModel.getY().length();
        regarima = initialModel;
        double max;
        try {
            do {
                if (!estimateModel(mapping)) {
                    return false;
                }
                boolean search = true;
                if (backwardStep) {
                    search = verifyModel();
                    if (exit) {
                        break;
                    }
                }
                if (search) {
                    if (!sod.process(regarima)) {
                        break;
                    }
                    round++;
                    max = sod.getMaxTStat();
                    if (Math.abs(max) < cv) {
                        break;
                    }
                    int type = sod.getMaxOutlierType();
                    int pos = sod.getMaxOutlierPosition();
                    addOutlier(pos, type, sod.coefficient(pos, type));
                    if (outliers.size() == maxOutliers) {
                        break;
                    }
                }
            } while (round < maxRound);

            // we should remove non signigicant outlier (witouht re-estimation)
            if (exit || round == maxRound || outliers.size() == maxOutliers) {
                estimateModel(mapping);
            }
            estimationStep = false;

            while (!verifyModel()) {
                estimateModel(mapping);
            }

            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }

    private boolean estimateModel(IArimaMapping<SarimaModel> mapping) {
        // step 1 Initial values by OLS
        SarimaModel sarima = regarima.arima();
        SarimaOrders spec = sarima.orders();
        RegArmaModel<SarimaModel> dm = regarima.differencedModel();
        LinearModel lm = dm.asLinearModel();
        if (rflag) {
            if (lm.getVariablesCount() > 0) {
                LeastSquaresResults lsr = Ols.compute(lm);
                if (lsr == null) {
                    return false;
                }
                res = lm.calcResiduals(lsr.getCoefficients());
            } else {
                res = lm.getY();
            }

        } else if (coeff != null) {
            res = lm.calcResiduals(coeff);
        } else {
            res = lm.getY();
        }
        boolean stable = true;
        rflag = false;
        if (estimationStep) {
            SarmaOrders dspec = spec.doStationary();
            if (spec.getParametersCount() != 0) {
                HannanRissanen hr = HannanRissanen.builder().build();
                if (hr.process(res, dspec)) {
                    SarimaModel hrmodel = hr.getModel();
                    SarimaModel stmodel = SarimaMapping.stabilize(hrmodel);
                    stable = stmodel == hrmodel;
                    if (stable || mvx || round == 0) {
                        regarima = RegArimaModel.of(regarima,
                                SarimaModel.builder(spec)
                                        .parameters(stmodel.parameters())
                                        .build());
                    } else {
                        rflag = true;
                        stable = true;
                    }
                }
            }
            if (mvx || !stable) {
                return optimizeModel(mapping);
            }
        }
        if (lm.getVariablesCount() > 0) {
            updateLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima), spec.getParametersCount());
        }

        return true;
    }

    private boolean optimizeModel(IArimaMapping<SarimaModel> mapping) {
        RegArimaEstimation<SarimaModel> estimation = processor.optimize(regarima, mapping);
        regarima = estimation.getModel();
        updateLikelihood(estimation.getConcentratedLikelihood(), estimation.parametersCount());
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihoodWithMissing likelihood, int nhp) {
        coeff = likelihood.allCoefficients();
        tstats = likelihood.tstats(nhp, true);
        res=regarima.differencedModel().asLinearModel().calcResiduals(coeff);
    }

    private void clear() {
        rflag = true;
        outliers.clear();
        round = 0;
        lastremoved = null;
        coeff = null;
        tstats = null;
        estimationStep = true;
        backwardStep = false;
        exit = false;
        res = null;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel() {
        estimationStep = true;
        if (outliers.isEmpty()) {
            return true;
        }
        /*double[] t = m_model.computeLikelihood().getTStats(true,
         m_model.getArma().getParametersCount());*/
        int nx0 = tstats.length - outliers.size();
        int imin = 0;
        for (int i = 1; i < outliers.size(); ++i) {
            if (Math.abs(tstats[i + nx0]) < Math.abs(tstats[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats[nx0 + imin]) >= cv) {
            return true;
        }
        backwardStep = false;
        estimationStep = false;
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        if (lastremoved != null) {
            if (Arrays.equals(toremove, lastremoved)) {
                exit = true;
            }
        }
        lastremoved = toremove;
        return false;
    }

    private void addOutlier(int pos, int type, double c) {
        addOutlier(pos, type);
        double[] tmp;
        if (coeff == null) {
            coeff = Doubles.of(c);
        } else {
            tmp = new double[coeff.length() + 1];
            coeff.copyTo(tmp, 0);
            tmp[coeff.length()] = c;
            coeff = DoubleSeq.of(tmp);
        }
        backwardStep = true;
    }

    private void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.of(xo);
        sod.getOutlierFactory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        sod.exclude(pos, type);
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        //
        int opos = regarima.getX().size() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
        double[] tmp;
        if (coeff.length() == 1) {
            coeff = null;
        } else {
            if (regarima.isMean()) {
                ++opos;
            }
            tmp = new double[coeff.length() - 1];
            for (int i = 0; i < opos; ++i) {
                tmp[i] = coeff.get(i);
            }
            for (int i = opos + 1; i < coeff.length(); ++i) {
                tmp[i - 1] = coeff.get(i);
            }
            coeff = DoubleSeq.of(tmp);
        }
    }
 }
