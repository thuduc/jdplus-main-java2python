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
package jdplus.toolkit.base.xml.legacy.regression;

import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Jean Palate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutlierType", propOrder = {
    "position"
})
@XmlSeeAlso({
    XmlAdditiveOutlier.class, XmlLevelShift.class, XmlTransitoryChange.class, XmlSeasonalOutlier.class
})
public abstract class XmlOutlier extends XmlRegressionVariable {

    @XmlElement(name = "Position", required = true)
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate position;

    @XmlAttribute(name = "preSpecified")
    protected Boolean preSpecified;

    /**
     * @return the position
     */
    public LocalDate getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(LocalDate position) {
        this.position = position;
    }

    public boolean isPreSpecified() {
        if (preSpecified == null) {
            return false;
        } else {
            return preSpecified;
        }
    }

    /**
     * @param preSpecified the preSpecified to set
     */
    public void setPreSpecified(Boolean preSpecified) {
        this.preSpecified = preSpecified;
    }

    public static final IOutlier unmarshal(XmlOutlier xml) {
        IOutlier rslt = XmlAdditiveOutlier.ADAPTER.unmarshal(xml);
        if (rslt != null) {
            return rslt;
        }
        rslt = XmlLevelShift.ADAPTER.unmarshal(xml);
        if (rslt != null) {
            return rslt;
        }
        rslt = XmlTransitoryChange.ADAPTER.unmarshal(xml);
        if (rslt != null) {
            return rslt;
        }
        return XmlSeasonalOutlier.ADAPTER.unmarshal(xml);
    }

    public static final XmlOutlier marshal(IOutlier v) {
        switch (v.getCode()) {
            case "AO":
                return XmlAdditiveOutlier.ADAPTER.marshal(v);
            case "LS":
                return XmlLevelShift.ADAPTER.marshal(v);
            case "TC":
                return XmlTransitoryChange.ADAPTER.marshal(v);
            case "SO":
                return XmlSeasonalOutlier.ADAPTER.marshal(v);
            default:
                return null;
        }
    }
}
