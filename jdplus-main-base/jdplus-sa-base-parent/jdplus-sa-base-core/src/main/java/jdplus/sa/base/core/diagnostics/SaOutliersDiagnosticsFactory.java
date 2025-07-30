/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.base.core.diagnostics;

import jdplus.sa.base.api.SaDiagnosticsFactory;
import java.util.function.Function;
import jdplus.toolkit.base.core.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.OutliersDiagnosticsFactory;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author PALATEJ
 * @param <R>
 */
public class SaOutliersDiagnosticsFactory<R> extends OutliersDiagnosticsFactory<R> implements SaDiagnosticsFactory<OutliersDiagnosticsConfiguration, R> {

    public SaOutliersDiagnosticsFactory(OutliersDiagnosticsConfiguration config, Function<R, RegSarimaModel> extractor) {
        super(config, extractor);
    }

    @Override
    public Scope getScope() {
        return Scope.Modelling;
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public SaOutliersDiagnosticsFactory<R> with(OutliersDiagnosticsConfiguration config){
        return new SaOutliersDiagnosticsFactory(config, extractor);
    }
}
