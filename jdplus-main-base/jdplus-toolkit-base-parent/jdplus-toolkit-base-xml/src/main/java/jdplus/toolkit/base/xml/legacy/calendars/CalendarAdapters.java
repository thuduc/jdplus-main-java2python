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

import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class CalendarAdapters {

    private static final AtomicReference<CalendarAdapters> defadapters = new AtomicReference<>();

    public static final CalendarAdapters getDefault() {
        defadapters.compareAndSet(null, make());
        return defadapters.get();
    }

    public CalendarAdapters() {
        load();
    }

    public static final void setDefault(CalendarAdapters adapters) {
        defadapters.set(adapters);
    }

    private static CalendarAdapters make() {
        CalendarAdapters adapters = new CalendarAdapters();
        return adapters;
    }

    private final List<CalendarAdapter> adapters = new ArrayList<>();

    private void load() {
        adapters.addAll(new CalendarAdapterLoader().get());
    }

    public List<Class> getXmlClasses() {
        return adapters.stream().map(adapter -> adapter.getXmlType()).collect(Collectors.toList());
    }

    public CalendarDefinition unmarshal(XmlCalendar xvar) {
        for (CalendarAdapter adapter : adapters) {
            if (adapter.getXmlType().isInstance(xvar)) {
                try {
                    return (CalendarDefinition) adapter.unmarshal(xvar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }

    public XmlCalendar marshal(CalendarDefinition ivar) {
        for (CalendarAdapter adapter : adapters) {
            if (adapter.getValueType().isInstance(ivar)) {
                try {
                    return (XmlCalendar) adapter.marshal(ivar);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
