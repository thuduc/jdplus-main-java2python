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

@XmlSchema(namespace = "ec/eurostat/jdemetra/sa/x13", elementFormDefault = XmlNsForm.QUALIFIED, attributeFormDefault = XmlNsForm.UNQUALIFIED, 
        xmlns = { @XmlNs(prefix = "sa", namespaceURI = "ec/eurostat/jdemetra/sa"), 
            @XmlNs(prefix = "x13", namespaceURI = "ec/eurostat/jdemetra/sa/x13"),
            @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema") 
})
package jdplus.x13.base.xml;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
