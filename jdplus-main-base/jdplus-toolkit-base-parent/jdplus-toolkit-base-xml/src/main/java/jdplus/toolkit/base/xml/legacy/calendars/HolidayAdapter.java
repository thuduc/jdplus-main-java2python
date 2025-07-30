/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.toolkit.base.xml.legacy.calendars;

import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 * @param <V>
 * @param <X>
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.xml.calendars.DayAdapterLoader"
)
public abstract class HolidayAdapter<X extends XmlDay, V extends Holiday> extends XmlAdapter<X, V> {

    public abstract Class<V> getValueType();

    public abstract Class<X> getXmlType();
}
