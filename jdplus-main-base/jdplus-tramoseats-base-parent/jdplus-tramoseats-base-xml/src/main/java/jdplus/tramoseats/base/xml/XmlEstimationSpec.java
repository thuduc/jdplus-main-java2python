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
package jdplus.tramoseats.base.xml;

import jdplus.toolkit.base.xml.legacy.core.XmlPeriodSelection;
import jdplus.tramoseats.base.api.tramo.EstimateSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EstimationSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EstimationSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}EstimationSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Span" type="{ec/eurostat/jdemetra/core}PeriodSelectionType" minOccurs="0"/&gt;
 *         &lt;element name="Precision" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="EML" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="UBP" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.5"/&gt;
 *               &lt;maxInclusive value="1.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "EstimationSpec")
@XmlType(name = "EstimationSpecType", propOrder = {
    "precision",
    "eml",
    "ubp"
})
public class XmlEstimationSpec
        extends jdplus.toolkit.base.xml.legacy.modelling.XmlEstimationSpec {

    @XmlElement(name = "Precision", defaultValue = "0.0000001")
    protected Double precision;
    @XmlElement(name = "EML", defaultValue = "true")
    protected Boolean eml;
    @XmlElement(name = "UBP", defaultValue = "0.96")
    protected Double ubp;

    /**
     * Gets the value of the precision property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getPrecision() {
        return precision == null ? EstimateSpec.DEF_TOL : precision;
    }

    /**
     * Sets the value of the precision property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setPrecision(Double value) {
        if (value != null && value == EstimateSpec.DEF_TOL) {
            precision = null;
        } else {
            precision = value;
        }
    }

    /**
     * Gets the value of the eml property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public boolean isEML() {
        return eml == null ? true : eml;
    }

    /**
     * Sets the value of the eml property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setEML(Boolean value) {
        if (value != null && value) {
            eml = null;
        } else {
            eml = value;
        }
    }

    /**
     * Gets the value of the ubp property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getUBP() {
        return ubp == null ? EstimateSpec.DEF_UBP : ubp;
    }

    /**
     * Sets the value of the ubp property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setUBP(Double value) {
        if (value != null && value == EstimateSpec.DEF_UBP) {
            ubp = null;
        } else {
            ubp = value;
        }
    }
    
    public boolean isDefault(){
        return precision==null || eml == null || ubp == null;
    }

    public static EstimateSpec unmarshal(XmlEstimationSpec xml){
        if (xml == null || xml.isDefault())
            return EstimateSpec.DEFAULT;
        
        EstimateSpec.Builder builder = EstimateSpec.builder();
        if (xml.span != null) {
            builder=builder.span(XmlPeriodSelection.getAdapter().unmarshal(xml.span));
        } 
        if (xml.eml != null) {
            builder=builder.maximumLikelihood(xml.eml);
        }
        if (xml.precision != null) {
            builder=builder.tol(xml.precision);
        }
        if (xml.ubp != null) {
            builder=builder.ubp(xml.ubp);
        }
        return builder.build();
    }

    public static XmlEstimationSpec marshal(EstimateSpec v){
        if (v.isDefault()) {
            return null;
        }
        XmlEstimationSpec xml=new XmlEstimationSpec();
        marshal(v, xml);
        return xml;
    }
    
    public static boolean marshal(EstimateSpec v, XmlEstimationSpec xml){
        if (!v.getSpan().isAll()) {
            xml.span = new XmlPeriodSelection();
            XmlPeriodSelection.marshal(v.getSpan(), xml.span);
        }
        xml.setEML(v.isMaximumLikelihood());
        xml.setPrecision(v.getTol());
        xml.setUBP(v.getUbp());
        return true;
    };
}
