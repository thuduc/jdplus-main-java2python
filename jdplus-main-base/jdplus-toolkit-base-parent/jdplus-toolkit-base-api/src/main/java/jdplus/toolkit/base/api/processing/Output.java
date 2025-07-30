/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jdplus.toolkit.base.api.processing;

import nbbrd.design.Development;
import nbbrd.design.NotThreadSafe;


/**
 * Generic interface that describes the making of output from a processing.
 *
 * @param <D>
 * @author Jean Palate
 */
@NotThreadSafe
@Development(status = Development.Status.Alpha)
public interface Output<D> {

    /**
     * Name of the output generating tool
     *
     * @return
     */
    String getName();

    /**
     * Controls the availability of the tool
     *
     * @return
     */
    boolean isAvailable();

    /**
     * Creates the actual output for a given document
     *
     * @param document The considered document
     */
    void process(D document) throws Exception;

    /**
     * Starts the processing of the item identified by the given id;
     *
     * @param context
     */
    void start(Object context) throws Exception;

    /**
     * Finishes the processing of the item identified by the given id;
     *
     * @param context
     * @throws java.lang.Exception
     */
    void end(Object context) throws Exception;
}
