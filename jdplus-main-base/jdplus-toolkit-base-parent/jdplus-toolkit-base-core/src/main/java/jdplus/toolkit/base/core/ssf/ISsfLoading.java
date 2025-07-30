/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfLoading extends ISsfRoot {

//<editor-fold defaultstate="collapsed" desc="description">
    /**
     * Gets a given measurement equation at a given position
     *
     * @param pos Position copyOf the measurement. Must be greater or equal than 0
     * @param z The buffer that will contain the measurement coefficients. Its
     * size must be equal to the state dimension
     */
    void Z(int pos, DataBlock z);

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="forward operations">
    /**
     *
     * @param pos
     * @param m
     * @return
     */
    double ZX(int pos, DataBlock m);

    /**
     * Computes Z*M
     * @param pos
     * @param m
     * @param zm 
     */
    default void ZM(int pos, FastMatrix m, DataBlock zm) {
        zm.set(m.columnsIterator(), x->ZX(pos, x));
    }

    /**
     * Computes M*Z' (or ZM')
     * @param pos
     * @param m
     * @param zm 
     */
    default void MZt(int pos, FastMatrix m, DataBlock zm) {
        zm.set(m.rowsIterator(), x->ZX(pos, x));
    }
    /**
     * Computes Z(pos) * V * Z'(pos)
     *
     * @param pos
     * @param V FastMatrix (statedim x statedim)
     * @return
     */
    double ZVZ(int pos, FastMatrix V);

//</editor-fold>    
//<editor-fold defaultstate="collapsed" desc="backward operations">
    /**
     *
     * @param pos
     * @param V
     * @param d
     */
    void VpZdZ(int pos, FastMatrix V, double d);

    /**
     * Computes x = x + Z * D
     *
     * @param pos
     * @param x DataBlock copyOf size statedim
     * @param d
     */
    void XpZd(int pos, DataBlock x, double d);

//</editor-fold>
}
