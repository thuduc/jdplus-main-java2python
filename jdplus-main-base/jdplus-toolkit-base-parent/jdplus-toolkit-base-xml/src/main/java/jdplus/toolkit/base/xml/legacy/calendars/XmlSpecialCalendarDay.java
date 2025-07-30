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

import jdplus.toolkit.base.api.timeseries.ValidityPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.DayEvent;
import jdplus.toolkit.base.api.timeseries.calendars.PrespecifiedHoliday;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecialCalendarDayType", propOrder = {
    "event",
    "offset"
})
public class XmlSpecialCalendarDay extends XmlDay {

    @XmlElement(name = "Event", required = true)
    @XmlSchemaType(name = "string")
    protected DayEvent event;
    @XmlElement(name = "Offset", defaultValue = "0")
    protected Short offset;

    public DayEvent getEvent() {
        return event;
    }

    public void setEvent(DayEvent value) {
        this.event = value;
    }

    public short getOffset() {
        return offset == null ? 0 : offset;
    }

    public void setOffset(Short value) {
        if (value != null && value == 0) {
            offset = null;
        } else {
            this.offset = value;
        }
    }

    @ServiceProvider(HolidayAdapter.class)
    public static class Adapter extends HolidayAdapter<XmlSpecialCalendarDay, PrespecifiedHoliday> {

        @Override
        public Class<PrespecifiedHoliday> getValueType() {
            return PrespecifiedHoliday.class;
        }

        @Override
        public Class<XmlSpecialCalendarDay> getXmlType() {
            return XmlSpecialCalendarDay.class;
        }

        @Override
        public PrespecifiedHoliday unmarshal(XmlSpecialCalendarDay v) throws Exception {
            return PrespecifiedHoliday.builder()
                    .event(v.getEvent())
                    .offset(v.getOffset())
                    .weight(v.getWeight())
                    .validityPeriod(ValidityPeriod.ALWAYS)
                    .build();
         }

        @Override
        public XmlSpecialCalendarDay marshal(PrespecifiedHoliday v) throws Exception {
            XmlSpecialCalendarDay xml = new XmlSpecialCalendarDay();
            xml.setEvent(v.getEvent());
            xml.setOffset((short) v.getOffset());
            xml.setWeight(v.getWeight());
            return xml;
        }

    }

}
