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
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TsDataSupplier;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.util.DefaultNameValidator;
import jdplus.toolkit.base.api.util.INameValidator;
import jdplus.toolkit.base.api.util.NameManager;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public final class TsDataSuppliers extends NameManager<TsDataSupplier> {

    private static final String DEFAULT_PREFIX = "x_";
    public static final String DEFAULT_INVALID_CHARS = ".+-*/";

    public TsDataSuppliers() {
        super(TsDataSupplier.class, DEFAULT_PREFIX, new DefaultNameValidator(DEFAULT_INVALID_CHARS));
    }

    public TsDataSuppliers(String prefix, INameValidator validator) {
        super(TsDataSupplier.class, prefix, validator);
    }

    public boolean isEmpty() {
        return getCount() < 1;
    }
}
