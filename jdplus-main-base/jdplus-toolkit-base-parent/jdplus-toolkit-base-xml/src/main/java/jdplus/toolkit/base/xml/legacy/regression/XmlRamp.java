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
package jdplus.toolkit.base.xml.legacy.regression;

import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nbbrd.service.ServiceProvider;

/**
 * 
 *                 Ramp variable.
 *                 A ramp is defined by two days: the first day is the starting point of the ramp and the last day is the end point of the ramp.
 *                 The variable is -1 before the starting day (included), 0 after the ending day (included) and increases linearly between the two days.
 *             
 * 
 * <p>Java class for RampType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RampType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}RegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}TimeSpan"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RampType", propOrder = {
    "from",
    "to"
})
public class XmlRamp
    extends XmlRegressionVariable
{

    @XmlElement(name = "From")
    @XmlSchemaType(name = "date")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    private LocalDate from;
    @XmlElement(name = "To")
    @XmlSchemaType(name = "date")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    private LocalDate to;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFrom(LocalDate value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public LocalDate getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTo(LocalDate value) {
        this.to = value;
    }



    public XmlRamp() {
    }

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public Ramp unmarshal(XmlRegressionVariable var) {
            if (!(var instanceof XmlRamp))
                return null;
            XmlRamp v=(XmlRamp) var;
            Ramp o = new Ramp(v.from.atStartOfDay(), v.to.atStartOfDay());
            return o;
        }

        @Override
        public XmlRamp marshal(ITsVariable var) {
            if (! (var instanceof Ramp))
                return null;
            Ramp v=(Ramp) var;
            XmlRamp xml = new XmlRamp();
            xml.from = v.getStart().toLocalDate();
            xml.to = v.getEnd().toLocalDate();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlRamp.class);
        }
    }
    
  
    private static final Adapter ADAPTER=new Adapter();
    public static final Adapter getAdapter(){
        return ADAPTER;
    }

}
