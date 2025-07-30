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
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
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
@XmlType(name = "SeasonalOutlierType")
public class XmlSeasonalOutlier extends XmlOutlier {

    @XmlAttribute(name = "zeroEnded")
    protected Boolean zeroEnded;

    @XmlAttribute(name = "period")
    protected Integer period;

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

    public int getPeriod() {
        if (period == null) {
            return 0;
        } else {
            return period;
        }
    }

    public void setPeriod(Integer value) {
        if (value != null && value != 0) {
            this.period = value;
        }else
            this.period=null;
    }

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public PeriodicOutlier unmarshal(XmlRegressionVariable var){
            if (!(var instanceof XmlSeasonalOutlier)) {
                return null;
            }
            XmlSeasonalOutlier v = (XmlSeasonalOutlier) var;
            PeriodicOutlier o = new PeriodicOutlier(v.position.atStartOfDay(), v.getPeriod(), v.zeroEnded != null ? v.zeroEnded : true);
            return o;
        }

        @Override
        public XmlSeasonalOutlier marshal(ITsVariable var){
            if (!(var instanceof PeriodicOutlier)) {
                return null;
            }
            PeriodicOutlier v = (PeriodicOutlier) var;
            XmlSeasonalOutlier xml = new XmlSeasonalOutlier();
            xml.position = v.getPosition().toLocalDate();
            xml.setPeriod(v.getPeriod());
            xml.zeroEnded = v.isZeroEnded();
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlSeasonalOutlier.class);
        }

    }

    public static final Adapter ADAPTER=new Adapter();
}
