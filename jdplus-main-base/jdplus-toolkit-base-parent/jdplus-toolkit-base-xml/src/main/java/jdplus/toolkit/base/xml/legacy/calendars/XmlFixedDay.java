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
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import java.time.Month;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FixedDayType", propOrder = {
    "month",
    "day"
})
public class XmlFixedDay
    extends XmlDay
{
    @XmlElement(name = "Month", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected Month month;
    @XmlElement(name = "Day")
    protected int day;

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month value) {
        this.month = value;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int value) {
        this.day = value;
    }

    @ServiceProvider(HolidayAdapter.class)
    public static class Adapter extends HolidayAdapter<XmlFixedDay, FixedDay> {

        @Override
        public Class<FixedDay> getValueType() {
            return FixedDay.class;
        }

        @Override
        public Class<XmlFixedDay> getXmlType() {
            return XmlFixedDay.class;
        }

         @Override
        public FixedDay unmarshal(XmlFixedDay v) throws Exception {
            FixedDay o = new FixedDay(v.getMonth().ordinal(), v.getDay(), v.getWeight(), ValidityPeriod.ALWAYS);
            return o;
        }

        @Override
        public XmlFixedDay marshal(FixedDay v) throws Exception {
            XmlFixedDay xml = new XmlFixedDay();
            xml.setDay(v.getDay());
            xml.setMonth(Month.of(v.getMonth()));
            xml.setWeight(v.getWeight());
            return xml;
        }

    }
}
