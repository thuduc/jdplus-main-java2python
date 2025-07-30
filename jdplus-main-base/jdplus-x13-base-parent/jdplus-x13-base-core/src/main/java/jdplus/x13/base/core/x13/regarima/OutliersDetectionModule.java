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
package jdplus.x13.base.core.x13.regarima;

import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.core.regsarima.ami.ExactOutliersDetector;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.PeriodicOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.TransitoryChangeFactory;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.toolkit.base.core.regarima.outlier.SingleOutlierDetector;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.regsarima.regular.IOutliersDetectionModule;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectionModule implements IOutliersDetectionModule {

    public static int DEF_MAXROUND = 50;
    public static int DEF_MAXOUTLIERS = 30;
    public static final double EPS = 1e-7, TCRATE=.7;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(OutliersDetectionModule.class)
    public static class Builder {

        private double eps = EPS;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private boolean ao, ls, tc, so;
        private double tcrate=TCRATE;
        private TimeSelector span = TimeSelector.all();

        private Builder() {
        }

        public Builder span(TimeSelector span) {
            this.span = span;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder tcrate(double tcrate) {
            this.tcrate = tcrate;
            return this;
        }

        public Builder ao(boolean ao) {
            this.ao = ao;
            return this;
        }

        public Builder ls(boolean ls) {
            this.ls = ls;
            return this;
        }

        public Builder tc(boolean tc) {
            this.tc = tc;
            return this;
        }

        public Builder so(boolean so) {
            this.so = so;
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

        public OutliersDetectionModule build() {
            return new OutliersDetectionModule(this);
        }
    }

    private final double eps;
    private final int maxOutliers;
    private final int maxRound;
    private final boolean ao, ls, tc, so;
    private final double tcrate;
    private final TimeSelector span;

    private OutliersDetectionModule(Builder builder) {
        this.eps = builder.eps;
        this.maxOutliers = builder.maxOutliers;
        this.maxRound = builder.maxRound;
        this.ao = builder.ao;
        this.ls = builder.ls;
        this.tc = builder.tc;
        this.so = builder.so;
        this.tcrate = builder.tcrate;
        this.span = builder.span;
    }

    private SingleOutlierDetector<SarimaModel> factories(int freq) {
        SingleOutlierDetector sod = new ExactSingleOutlierDetector(RobustStandardDeviationComputer.mad(false),
                null, X13Utility.mlComputer());
        
        List<IOutlierFactory> factory=new ArrayList<>();
        if (ao) {
            factory.add(AdditiveOutlierFactory.FACTORY);
        }
        if (ls) {
            factory.add(LevelShiftFactory.FACTORY_ZEROENDED);
        }
        if (tc) {
            double c = TransitoryChangeFactory.rate(freq, tcrate);
            factory.add(new TransitoryChangeFactory(c));
        }
        if (freq > 1 && so) {
            factory.add(new PeriodicOutlierFactory(freq, true));
        }
        sod.setOutlierFactories(factory.toArray(IOutlierFactory[]::new));
        return sod;
    }

    private ExactOutliersDetector make(ModelDescription desc, double cv) {
        TsDomain domain = desc.getEstimationDomain();

        ExactOutliersDetector impl = ExactOutliersDetector.builder()
                .singleOutlierDetector(factories(domain.getAnnualFrequency()))
                .criticalValue(cv)
                .maxOutliers(maxOutliers)
                .maxRound(maxRound)
                .processor(RegArimaUtility.processor(true, eps))
                .build();
        TsDomain odom = domain.select(span);
        int start = domain.indexOf(odom.getStartPeriod()), end = start + odom.getLength();
        impl.prepare(domain.getLength());
        impl.setBounds(start, end);
        String[] types = impl.outlierTypes();
        // remove missing values
        int[] missing = desc.getMissingInEstimationDomain();
        if (missing != null) {
            for (int i = 0; i < missing.length; ++i) {
                for (int j = 0; j < types.length; ++j) {
                    impl.exclude(missing[i], j);
                }
            }
        }
        // exclude pre-specified outliers
        desc.variables()
                .filter(var ->ModellingUtility.isOutlier(var, false))
                .map(var -> (IOutlier) var.getCore()).forEach(
                o -> impl.exclude(domain.indexOf(o.getPosition()), outlierType(types, o.getCode())));
        // add current outliers
        desc.variables()
                .filter(var -> ModellingUtility.isOutlier(var, true))
                .map(var -> (IOutlier) var.getCore()).forEach(
                o -> impl.addOutlier(domain.indexOf(o.getPosition()), outlierType(types, o.getCode())));
        return impl;
    }

    @Override
    public ProcessingResult process(RegSarimaModelling context, double criticalValue) {
        try {
            ModelDescription model = context.getDescription();
            TsDomain domain = model.getEstimationDomain();
            ExactOutliersDetector impl = make(model, criticalValue);
            boolean changed = impl.process(model.regarima(), model.mapping());
            if (!changed) {
                return ProcessingResult.Unchanged;
            }
            // clear current outliers and add the new ones (that could be partly the same)
            model.removeVariable(var -> ModellingUtility.isOutlier(var, true));
            // add new outliers
            int[][] outliers = impl.getOutliers();
            for (int i = 0; i < outliers.length; ++i) {
                int[] cur = outliers[i];
                TsPeriod pos = domain.get(cur[0]);
                IOutlier o = impl.getFactory(cur[1]).make(pos.start());
            model.addVariable(Variable.variable(IOutlier.defaultName(o.getCode(), pos), o, attributes(o)));
            }
            context.clearEstimation();
            return ProcessingResult.Changed;
        } catch (RuntimeException err) {
            return ProcessingResult.Failed;
        }
    }

    static Map<String, String> attributes(IOutlier o){
        HashMap<String, String> attributes=new HashMap<>();
        attributes.put(ModellingUtility.AMI, "x13");
        attributes.put(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(o).name());
        return attributes;
    }

    private static int outlierType(String[] all, String cur) {
        for (int i = 0; i < all.length; ++i) {
            if (cur.equals(all[i])) {
                return i;
            }
        }
        return -1;
    }


}
