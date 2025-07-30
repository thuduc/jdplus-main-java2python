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

import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LevelShiftType")
public class XmlLevelShift extends XmlOutlier {

    @XmlAttribute(name = "zeroEnded")
    protected Boolean zeroEnded;

    public boolean isZeroEnded() {
        if (zeroEnded == null) {
            return true;
        } else {
            return zeroEnded;
        }
    }

    public void setZeroEnded(Boolean value) {
        this.zeroEnded = value;
    }

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public LevelShift unmarshal(XmlRegressionVariable var) {
            if (!(var instanceof XmlLevelShift)) {
                return null;
            }
            XmlLevelShift v = (XmlLevelShift) var;
            LevelShift o = new LevelShift(v.getPosition().atStartOfDay(), v.zeroEnded == null ? true : v.zeroEnded);
            return o;
        }

        @Override
        public XmlLevelShift marshal(ITsVariable var)  {
            if (!(var instanceof LevelShift)) {
                return null;
            }
            LevelShift v = (LevelShift) var;
            XmlLevelShift xml = new XmlLevelShift();
            xml.position = v.getPosition().toLocalDate();
            xml.zeroEnded = v.isZeroEnded();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlLevelShift.class);
        }

    }
    
    public static final Adapter ADAPTER=new Adapter();
}
