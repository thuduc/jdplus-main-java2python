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
package jdplus.sa.base.core.tests;

import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.core.stats.tests.LjungBox;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@BuilderPattern(StatisticalTest.class)
public class Qs {

    private final LjungBox lb;
    
    public Qs(DoubleSeq sample, int seasLag){
        lb=new LjungBox(sample)
                .lag(seasLag)
                .autoCorrelationsCount(2)
                .usePositiveAutoCorrelations();
     }

    /**
     *
     * @param nhp
     * @return
     */
    public Qs hyperParametersCount(int nhp) {
        lb.hyperParametersCount(nhp);
        return this;
    }


     /**
     *
     * @param k
     * @return
     */
    public Qs autoCorrelationsCount(int k) {
        lb.autoCorrelationsCount(k);
        return this;
    }
    
    public Qs useNegativeAutocorrelations(){
        lb.useNegativeAutoCorrelations();
        return this;
    }

    public Qs usePositiveAutocorrelations(){
        lb.usePositiveAutoCorrelations();
        return this;
    }

    public Qs useAllAutocorrelations(){
        lb.useAllAutoCorrelations();
        return this;
    }

    public StatisticalTest build() {
        return lb.build();            
    }
 }
