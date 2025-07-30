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
package jdplus.toolkit.base.xml.legacy.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlProperty.NAME)
public class XmlProperty {

    static final String NAME = "PropertyType";

    public XmlProperty() {
    }

    public XmlProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public XmlProperty(String name, Object value) {
        this.name = name;
        this.value = value.toString();
    }
    /**
     *
     */
    @XmlElement(name = "Name")
    public String name;
    /**
     *
     */
    @XmlElement(name = "Value")
    public String value;
}
