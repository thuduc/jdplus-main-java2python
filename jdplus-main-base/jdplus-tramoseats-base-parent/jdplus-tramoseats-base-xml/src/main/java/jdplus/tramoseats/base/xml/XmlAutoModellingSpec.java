/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.xml;

import jdplus.tramoseats.base.api.tramo.AutoModelSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import lombok.NonNull;

/**
 * <p>
 * Java class for AutoModellingSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="AutoModellingSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PCR" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="0.8"/&gt;
 *               &lt;maxExclusive value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="UB1" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="0.8"/&gt;
 *               &lt;maxExclusive value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="UB2" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="0.7"/&gt;
 *               &lt;maxExclusive value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Cancel" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.0"/&gt;
 *               &lt;maxInclusive value="0.2"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Tsig" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.0"/&gt;
 *               &lt;maxInclusive value="2.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PC" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="1.1"/&gt;
 *               &lt;maxInclusive value="1.5"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AmiCompare" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="AcceptDefault" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoModellingSpecType", propOrder = {
    "pcr",
    "ub1",
    "ub2",
    "cancel",
    "tsig",
    "pc",
    "amiCompare",
    "acceptDefault"
})
public class XmlAutoModellingSpec {

    @XmlElement(name = "PCR")
    protected Double pcr;
    @XmlElement(name = "UB1")
    protected Double ub1;
    @XmlElement(name = "UB2")
    protected Double ub2;
    @XmlElement(name = "Cancel", defaultValue = "0.1")
    protected Double cancel;
    @XmlElement(name = "Tsig")
    protected Double tsig;
    @XmlElement(name = "PC")
    protected Double pc;
    @XmlElement(name = "AmiCompare")
    protected Boolean amiCompare;
    @XmlElement(name = "AcceptDefault")
    protected Boolean acceptDefault;

    /**
     * Gets the value of the pcr property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getPCR() {
        return pcr;
    }

    /**
     * Sets the value of the pcr property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setPCR(Double value) {
        if (value != null && value == AutoModelSpec.DEF_PCR) {
            pcr = null;
        } else {
            pcr = value;
        }
    }

    /**
     * Gets the value of the ub1 property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getUB1() {
        return ub1;
    }

    /**
     * Sets the value of the ub1 property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setUB1(Double value) {
        if (value != null && value == AutoModelSpec.DEF_UB1) {
            ub1 = null;
        } else {
            ub1 = value;
        }
    }

    /**
     * Gets the value of the ub2 property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getUB2() {
        return ub2;
    }

    /**
     * Sets the value of the ub2 property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setUB2(Double value) {
        if (value != null && value == AutoModelSpec.DEF_UB2) {
            ub2 = null;
        } else {
            ub2 = value;
        }
    }

    /**
     * Gets the value of the cancel property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getCancel() {
        return cancel;
    }

    /**
     * Sets the value of the cancel property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setCancel(Double value) {
        if (value != null && value == AutoModelSpec.DEF_CANCEL) {
            cancel = null;
        } else {
            cancel = value;
        }
    }

    /**
     * Gets the value of the tsig property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getTsig() {
        return tsig;
    }

    /**
     * Sets the value of the tsig property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setTsig(Double value) {
        if (value != null && value == AutoModelSpec.DEF_TSIG) {
            tsig = null;
        } else {
            tsig = value;
        }
    }

    /**
     * Gets the value of the pc property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getPC() {
        return pc;
    }

    /**
     * Sets the value of the pc property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setPC(Double value) {
        if (value != null && value == AutoModelSpec.DEF_PC) {
            pc = null;
        } else {
            this.pc = value;
        }
    }

    /**
     * Gets the value of the amiCompare property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isAmiCompare() {
        return amiCompare;
    }

    /**
     * Sets the value of the amiCompare property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setAmiCompare(Boolean value) {
        if (value != null && value == AutoModelSpec.DEF_AMICOMPARE) {
            amiCompare = null;
        } else {
            this.amiCompare = value;
        }
    }

    /**
     * Gets the value of the acceptDefault property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isAcceptDefault() {
        return acceptDefault;
    }

    /**
     * Sets the value of the acceptDefault property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setAcceptDefault(Boolean value) {
        if (value != null && value == AutoModelSpec.DEF_FAL) {
            acceptDefault = null;
        } else {
            this.acceptDefault = value;
        }
    }

    public boolean isDefault() {
        return pcr == null && ub1 == null && ub2 == null && cancel == null && tsig == null && pc == null && amiCompare == null && acceptDefault == null;
    }

    public static XmlAutoModellingSpec marshal(@NonNull AutoModelSpec v) {
        if (!v.isEnabled()) {
            return null;
        }
        XmlAutoModellingSpec xml = new XmlAutoModellingSpec();
        if (!v.isDefault()) {
            marshal(v, xml);
        }
        return xml;
    }

    public static boolean marshal(@NonNull AutoModelSpec v, @NonNull XmlAutoModellingSpec xml) {
        xml.setAcceptDefault(v.isAcceptDefault());
        xml.setAmiCompare(v.isAmiCompare());
        xml.setCancel(v.getCancel());
        xml.setPC(v.getPc());
        xml.setPCR(v.getPcr());
        xml.setTsig(v.getTsig());
        xml.setUB1(v.getUb1());
        xml.setUB2(v.getUb2());
        return true;
    }

    public static AutoModelSpec unmarshal(XmlAutoModellingSpec xml) {
        if (xml == null) {
            return AutoModelSpec.DEFAULT_DISABLED;
        }
        if (xml.isDefault()) {
            return AutoModelSpec.DEFAULT_ENABLED;
        }
        AutoModelSpec.Builder builder = AutoModelSpec.builder()
                .enabled(true);
        if (xml.acceptDefault != null) {
            builder = builder.acceptDefault(xml.acceptDefault);
        }
        if (xml.amiCompare != null) {
            builder = builder.amiCompare(xml.amiCompare);
        }
        if (xml.cancel != null) {
            builder = builder.cancel(xml.cancel);
        }
        if (xml.pc != null) {
            builder = builder.pc(xml.pc);
        }
        if (xml.pcr != null) {
            builder = builder.pcr(xml.pcr);
        }
        if (xml.tsig != null) {
            builder = builder.tsig(xml.tsig);
        }
        if (xml.ub1 != null) {
            builder = builder.ub1(xml.ub1);
        }
        if (xml.ub2 != null) {
            builder = builder.ub2(xml.ub2);
        }
        return builder.build();
    }
}
