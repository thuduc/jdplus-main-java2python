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
package jdplus.toolkit.base.core.ucarima;

import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AllSelector implements IRootSelector {

    private Polynomial m_p;

    /**
     *
     */
    public AllSelector() {
    }

    @Override
    public Polynomial getOutofSelection() {
	return null;
    }

    @Override
    public Polynomial getSelection() {
	return m_p;
    }

    @Override
    public boolean select(final Polynomial p) {
	m_p = p;
	return p.degree() > 0;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
       return select(p);
    }
}
