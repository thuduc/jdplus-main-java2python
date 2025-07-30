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
package jdplus.toolkit.base.xml.legacy.processing;

import jdplus.toolkit.base.api.timeseries.DynamicTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.StaticTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.TsDataSupplier;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TsVariablesType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TsVariablesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variable" type="{ec/eurostat/jdemetra/core}TsVariableType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TsVariablesType", propOrder = {
    "variable"
})
public class XmlTsVariables {

    @XmlElement(name = "Variable")
    protected List<XmlTsVariable> variable;
    @XmlAttribute(name = "name", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected String name;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the variable property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variable
     * property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariable().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TsVariableType }
     *
     *
     * @return 
     */
    public List<XmlTsVariable> getVariables() {
        if (variable == null) {
            variable = new ArrayList<>();
        }
        return this.variable;
    }

    public static XmlTsVariables marshal(TsDataSuppliers v) {
        XmlTsVariables xml = new XmlTsVariables();
        String[] names = v.getNames();
        List<XmlTsVariable> variables = xml.getVariables();
        for (int i = 0; i < names.length; ++i) {
            TsDataSupplier var = v.get(names[i]);
            XmlTsVariable xvar = null;
            if (var instanceof DynamicTsDataSupplier) {
                xvar = XmlDynamicTsVariable.getAdapter().marshal((DynamicTsDataSupplier) var);
            } else if (var instanceof StaticTsDataSupplier) {
                xvar = XmlStaticTsVariable.getAdapter().marshal((StaticTsDataSupplier) var);
            }
            if (xvar != null) {
                xvar.setName(names[i]);
                variables.add(xvar);
            }
        }
        return xml;
    }

    public static final TsDataSuppliers unmarshal(XmlTsVariables xml) {
        TsDataSuppliers v=new TsDataSuppliers();
        for (XmlTsVariable xvar : xml.getVariables()) {
            TsDataSupplier var = null;
            if (xvar instanceof XmlStaticTsVariable) {
                var = XmlStaticTsVariable.getAdapter().unmarshal((XmlStaticTsVariable) xvar);
            } else if (xvar instanceof XmlDynamicTsVariable) {
                var = XmlDynamicTsVariable.getAdapter().unmarshal((XmlDynamicTsVariable) xvar);
            }
            if (var != null) {
                v.set(xvar.getName(), var);
            }
        }
        return v;
    }

}
