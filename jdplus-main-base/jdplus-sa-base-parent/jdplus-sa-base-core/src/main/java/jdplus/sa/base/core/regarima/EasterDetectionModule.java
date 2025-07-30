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
package jdplus.sa.base.core.regarima;

import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.regarima.AICcComparator;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regsarima.regular.IModelComparator;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EasterDetectionModule implements IRegressionModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(EasterDetectionModule.class)
    public static class Builder {

        private IEasterVariable[] easters;
        private IModelComparator comparator = new AICcComparator(0);
        private double eps = 1e-5;

        public Builder easters(IEasterVariable[] easters) {
            this.easters = easters;
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder modelComparator(IModelComparator comparator) {
            this.comparator = comparator;
            return this;
        }

        public EasterDetectionModule build() {
            return new EasterDetectionModule(this);
        }
    }

    private final IModelComparator comparator;
    private final IEasterVariable[] easters;
    private final double eps;

    public EasterDetectionModule(Builder builder) {
        this.comparator = builder.comparator;
        this.easters = builder.easters;
        this.eps = builder.eps;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {
        ModelDescription description = context.getDescription();
        if (description.getAnnualFrequency() <= 2)
            return ProcessingResult.Unprocessed;
        int n = easters.length;
        int icur = -1;
        ModelDescription[] desc = new ModelDescription[n];
        RegArimaEstimation[] est = new RegArimaEstimation[n];
        IRegArimaComputer<SarimaModel> processor = RegArimaUtility.processor(true, eps);

        ModelDescription refdesc = ModelDescription.copyOf(description);
        refdesc.remove("easter");
        RegArimaEstimation<SarimaModel> refest = refdesc.estimate(processor);

        for (int i = 0; i < n; ++i) {
            ModelDescription curDesc = ModelDescription.copyOf(refdesc);
            curDesc.addVariable(Variable.variable("easter", easters[i], ModelBuilder.calendarAMI));
            desc[i] = curDesc;
            est[i] = curDesc.estimate(processor);
        }

        // choose best model
        int imodel = comparator.compare(refest, est);
        if (imodel < 0) {
            context.set(refdesc, refest);
        } else {
            context.set(desc[imodel], est[imodel]);
        }

        return icur == imodel ? ProcessingResult.Unchanged : ProcessingResult.Changed;
    }

}
