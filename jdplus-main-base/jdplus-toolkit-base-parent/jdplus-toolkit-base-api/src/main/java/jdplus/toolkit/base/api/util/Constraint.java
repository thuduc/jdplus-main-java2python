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
package jdplus.toolkit.base.api.util;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Defines a constraint on an class.
 *
 * @author Philippe Charles
 */
public interface Constraint<T> {

    /**
     * Checks the constraint against an instance of the class.
     *
     * @param t the instance to be checked
     * @return a description of the violated constraint, null otherwise.
     */
    @Nullable
    String check(@NonNull T t);
}
