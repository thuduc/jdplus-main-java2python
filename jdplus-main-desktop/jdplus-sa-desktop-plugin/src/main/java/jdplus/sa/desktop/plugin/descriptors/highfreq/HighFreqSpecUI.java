/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sa.desktop.plugin.descriptors.highfreq;

import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;
import jdplus.toolkit.base.api.modelling.highfreq.EstimateSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;

/**
 *
 * @author palatej
 */
public interface HighFreqSpecUI {
    
    boolean isRo();
    
    boolean hasFixedCoefficients();
    
    TransformSpec transform();
    
    OutlierSpec outlier();
    
    void update(EstimateSpec spec);

    void update(OutlierSpec spec);

    void update(RegressionSpec spec);

    void update(TransformSpec spec);

    void update(SeriesSpec spec);

    void update(EasterSpec spec);

    void update(HolidaysSpec spec);
}
