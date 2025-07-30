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
package jdplus.toolkit.base.core.modelling.extractors;

import nbbrd.design.Development;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.dictionaries.ArimaDictionaries;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class UcarimaExtractor extends InformationMapping<UcarimaModel> {


    public UcarimaExtractor() {
        set(ArimaDictionaries.SIZE, Integer.class, source -> source.getComponentsCount());
        delegate(ArimaDictionaries.MODEL, IArimaModel.class, source -> source.getModel());
        delegateArray(ArimaDictionaries.COMPONENT, 1, 20, IArimaModel.class, (source, i)
                -> (i <= 0 || i > source.getComponentsCount()) ? null : source.getComponent(i - 1));
        delegateArray(ArimaDictionaries.COMPONENTC, 1, 20, IArimaModel.class, (source, i)
                -> (i <= 0 || i > source.getComponentsCount()) ? null : source.getComplement(i - 1));
        delegate(ArimaDictionaries.SUM, IArimaModel.class, source -> source.sum());
    }

    @Override
    public Class<UcarimaModel> getSourceClass() {
        return UcarimaModel.class;
    }

}
