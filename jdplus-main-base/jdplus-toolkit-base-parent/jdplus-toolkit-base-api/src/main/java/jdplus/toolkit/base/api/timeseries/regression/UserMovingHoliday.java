/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class UserMovingHoliday extends TsVariable implements IMovingHolidayVariable {

    public static UserMovingHoliday of(String id, String desc, ModellingContext context) {
        TsData data = data(id, context);
        if (data == null) {
            return null;
        } else {
            return new UserMovingHoliday(id, data, desc);
        }
    }


    private UserMovingHoliday(String id, TsData data, String desc) {
        super(id, data, desc);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserMovingHoliday) {
            return equals((TsVariable) obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash();
    }
}
