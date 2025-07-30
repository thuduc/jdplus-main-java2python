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
package jdplus.toolkit.base.core.regarima.diagnostics;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import lombok.NonNull;
import jdplus.toolkit.base.api.processing.DiagnosticsFactory;
import jdplus.toolkit.base.api.processing.Diagnostics;

/**
 *
 * @author Kristof Bayens
 */
public class OutliersDiagnosticsFactory<R> implements DiagnosticsFactory<OutliersDiagnosticsConfiguration, R> {
    
    public static final String NUMBER = "number of outliers";
    public static final String NAME = "Outliers";
    public static final List<String> ALL = Collections.singletonList(NUMBER);
    private final OutliersDiagnosticsConfiguration config;
    protected final Function<R, RegSarimaModel> extractor;
    
    public OutliersDiagnosticsFactory(@NonNull OutliersDiagnosticsConfiguration config, @NonNull Function<R, RegSarimaModel> extractor) {
        this.config = config;
        this.extractor = extractor;
    }
    
    @Override
    public OutliersDiagnosticsConfiguration getConfiguration() {
        return config;
    }
    
    @Override
    public OutliersDiagnosticsFactory<R> with(OutliersDiagnosticsConfiguration config){
        return new OutliersDiagnosticsFactory(config, extractor);
    }

    @Override
    public String getName() {
        return NAME;        
    }
    
    @Override
    public List<String> getTestDictionary() {
        return ALL.stream().map(s -> s + ":2").collect(Collectors.toList());
    }
    
    @Override
    public Diagnostics of(R rslts) {
        return OutliersDiagnostics.create(extractor.apply(rslts), config);
    }
}
