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
package jdplus.x13.base.xml;

import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.x13.base.api.regarima.TransformSpec;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.xml.legacy.XmlEmptyElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TransformationSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TransformationSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}TransformationSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/sa/x13}TransformationSpecGroup" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "TransformationSpec")
@XmlType(name = "TransformationSpecType", propOrder = {
    "log",
    "auto",
    "adjust"
})
public class XmlTransformationSpec {

    @XmlElement(name = "Log")
    protected XmlEmptyElement log;
    @XmlElement(name = "Auto")
    protected XmlAutoTransformationSpec auto;
    @XmlElement(name = "Adjust")
    @XmlSchemaType(name = "NMTOKEN")
    protected LengthOfPeriodType adjust;

    /**
     * Gets the value of the log property.
     *
     * @return possible object is {@link Object }
     *
     */
    public XmlEmptyElement getLog() {
        return log;
    }

    /**
     * Sets the value of the log property.
     *
     * @param value allowed object is {@link Object }
     *
     */
    public void setLog(XmlEmptyElement value) {
        this.log = value;
    }

    /**
     * Gets the value of the auto property.
     *
     * @return possible object is {@link AutoTransformationSpecType }
     *
     */
    public XmlAutoTransformationSpec getAuto() {
        return auto;
    }

    /**
     * Sets the value of the auto property.
     *
     * @param value allowed object is {@link AutoTransformationSpecType }
     *
     */
    public void setAuto(XmlAutoTransformationSpec value) {
        this.auto = value;
    }

    /**
     * Gets the value of the adjust property.
     *
     * @return possible object is {@link LengthOfPeriodEnum }
     *
     */
    public LengthOfPeriodType getAdjust() {
        return adjust;
    }

    /**
     * Sets the value of the adjust property.
     *
     * @param value allowed object is {@link LengthOfPeriodEnum }
     *
     */
    public void setAdjust(LengthOfPeriodType value) {
        if (value == LengthOfPeriodType.None) {
            this.adjust = null;
        } else {
            this.adjust = value;
        }
    }

    public static TransformSpec unmarshal(XmlTransformationSpec xml) {
        if (xml == null) {
            return TransformSpec.DEFAULT_NONE;
        }
        TransformSpec.Builder builder = TransformSpec.builder();
        if (xml.log != null) {
            builder = builder.function(TransformationType.Log);
        } else if (xml.auto != null) {
            builder = builder.function(TransformationType.Auto).aicDiff(xml.auto.aicDiff);
        }
        if (xml.adjust != null) {
            builder = builder.adjust(xml.adjust);
        }
        return builder.build();
    }

    public static XmlTransformationSpec marshal(TransformSpec v) {
        if (v.getFunction() == TransformationType.None) {
            return null;
        }
        XmlTransformationSpec xml = new XmlTransformationSpec();
        marshal(v, xml);
        return xml;
    }

    public static boolean marshal(TransformSpec v, XmlTransformationSpec xml) {
        if (v.getFunction() == TransformationType.None) {
            return true;
        }

        xml.setAdjust(v.getAdjust());
        switch (v.getFunction()) {
            case Log:
                xml.setLog(new XmlEmptyElement());
                break;
            case Auto:
                XmlAutoTransformationSpec xauto = XmlAutoTransformationSpec.marshal(v);
                xml.setAuto(xauto);
                break;

        }
        return true;
    }
}
