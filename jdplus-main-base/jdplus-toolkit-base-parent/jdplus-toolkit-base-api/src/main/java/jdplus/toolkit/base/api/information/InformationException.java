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
package jdplus.toolkit.base.api.information;

import jdplus.toolkit.base.api.DemetraException;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class InformationException extends DemetraException {

    /**
     *
     */
    public InformationException() {
    }

    /**
     *
     * @param msg
     */
    public InformationException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param innerException
     */
    public InformationException(final String message, final Exception innerException) {
        super(message, innerException);
    }

}
