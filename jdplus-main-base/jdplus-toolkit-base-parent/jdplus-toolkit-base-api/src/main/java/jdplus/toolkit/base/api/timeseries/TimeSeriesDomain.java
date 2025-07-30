/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.data.Range;
import java.time.LocalDateTime;
import jdplus.toolkit.base.api.data.Seq;

/**
 *
 * @param <P>
 *
 * @author Philippe Charles
 */
public interface TimeSeriesDomain<P extends TimeSeriesInterval<?>> extends Range<LocalDateTime>, Seq<P> {

    /**
     *
     * @param period
     * @return
     */
    boolean contains(P period);

    /**
     *
     * @param date
     * @return Position of the period that contains the date. -1 if this date is
     * before the domain, -length() if it is after the domain.
     */
    int indexOf(LocalDateTime date);

    /**
     *
     * @param period
     * @return Position of the period. -1 if this period is before the domain,
     * -length() if it is after the domain.
     */
    int indexOf(P period);

    TimeSeriesDomain<P> select(TimeSelector selector);
}
