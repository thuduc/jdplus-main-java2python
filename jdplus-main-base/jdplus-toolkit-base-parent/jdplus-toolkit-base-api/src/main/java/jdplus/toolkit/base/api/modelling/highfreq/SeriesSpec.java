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
package jdplus.toolkit.base.api.modelling.highfreq;

import jdplus.toolkit.base.api.timeseries.TimeSelector;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName="Builder")
public class SeriesSpec {
    
    public static final SeriesSpec DEFAULT=builder().build();

    @lombok.NonNull
    private TimeSelector span;
    @lombok.NonNull
    private DataCleaning cleaning;
    
    public static Builder builder(){
        return new Builder().cleaning(DataCleaning.NONE).span(TimeSelector.all());
    }
    
    public boolean isDefault(){
        return this.equals(DEFAULT);
    }
    
}
