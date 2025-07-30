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
package jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.impl;

import jdplus.sa.desktop.plugin.diagnostics.SaResidualsDiagnosticsBuddy;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddy;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.toolkit.base.core.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = TramoSeatsDiagnosticsFactoryBuddy.class, position = 1100)
public final class TramoSeatsResidualsDiagnosticsBuddy extends SaResidualsDiagnosticsBuddy implements TramoSeatsDiagnosticsFactoryBuddy<ResidualsDiagnosticsConfiguration> {
    
    public TramoSeatsResidualsDiagnosticsBuddy() {
        this.setActiveDiagnosticsConfiguration(ResidualsDiagnosticsConfiguration.getDefault());
    }
    
    @Override
    public SaDiagnosticsFactory createFactory() {
        return new SaResidualsDiagnosticsFactory<>(this.getActiveDiagnosticsConfiguration(),
                (TramoSeatsResults r) -> r.getPreprocessing());
    }
    
}
