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
package jdplus.toolkit.desktop.plugin.datatransfer;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.NamedService;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.util.Table;
import nbbrd.design.swing.OnAnyThread;
import nbbrd.design.swing.OnEDT;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import lombok.NonNull;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 * SPI that allows to import/export specific data structures from/to the
 * clipboard.
 *
 * @author Philippe Charles
 * @since 1.3.0
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface DataTransferSpi extends NamedService {

    @ServiceSorter
    int getPosition();

    @NonNull
    DataFlavor getDataFlavor();

    @OnEDT
    boolean canExportTsCollection(@NonNull TsCollection col);

    @OnAnyThread
    @NonNull
    Object exportTsCollection(@NonNull TsCollection col) throws IOException;

    @OnEDT
    boolean canImportTsCollection(@NonNull Object obj);

    @OnEDT
    @NonNull
    TsCollection importTsCollection(@NonNull Object obj) throws IOException, ClassCastException;

    @OnEDT
    boolean canExportMatrix(@NonNull Matrix matrix);

    @OnAnyThread
    @NonNull
    Object exportMatrix(@NonNull Matrix matrix) throws IOException;

    @OnEDT
    boolean canImportMatrix(@NonNull Object obj);

    @OnEDT
    @NonNull
    Matrix importMatrix(@NonNull Object obj) throws IOException, ClassCastException;

    @OnEDT
    boolean canExportTable(@NonNull Table<?> table);

    @OnAnyThread
    @NonNull
    Object exportTable(@NonNull Table<?> table) throws IOException;

    @OnEDT
    boolean canImportTable(@NonNull Object obj);

    @OnEDT
    @NonNull
    Table<?> importTable(@NonNull Object obj) throws IOException, ClassCastException;
}
