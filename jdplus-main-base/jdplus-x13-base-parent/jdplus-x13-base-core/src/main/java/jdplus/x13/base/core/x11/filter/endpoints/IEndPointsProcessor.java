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
package jdplus.x13.base.core.x11.filter.endpoints;

import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * Computes the endpoints of a smoothing algorithm.
 * The end points processor is called after the regular (symmetric) smoothing, so
 * that the processor can use values already available in the output buffer.
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IEndPointsProcessor {

    /**
     * The method computes the endpoints of a smoothing algorithm.
     *
     * @param in Input (the series being smoothed)
     * @param out Output. On entry, contains regular smoothing results,
     * which have to be completed.
     * The input and the output buffers should have the same size.
     */
    void process(DoubleSeq in, DataBlock out);
}
