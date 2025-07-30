/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.core.linearmodel;

import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.data.DataBlock;
import java.util.Random;
import java.util.function.DoubleSupplier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jdplus.toolkit.base.api.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class LinearModelTest {

    public LinearModelTest() {
    }

    @Test
    public void testEmpty() {
        Random rnd = new Random();

        DataBlock y = DataBlock.make(10);
        y.set((DoubleSupplier)rnd::nextDouble);
        LinearModel lm = LinearModel.builder()
                .y(y)
                .meanCorrection(true)
                .build();
        assertTrue(lm != null);
        assertTrue(lm.calcResiduals(Doubles.of(.5)) != null);
    }

    @Test
    public void testNormal() {
        Random rnd = new Random();

        DataBlock y = DataBlock.make(10);
        y.set((DoubleSupplier)rnd::nextDouble);
        DataBlock x1 = DataBlock.make(10);
        x1.set((DoubleSupplier)rnd::nextDouble);
        DataBlock x2 = DataBlock.make(10);
        x2.set((DoubleSupplier)rnd::nextDouble);
        LinearModel lm = LinearModel.builder()
                .y(y)
                .meanCorrection(true)
                .addX(x1, x2)
                .build();
        assertTrue(lm != null);
        assertTrue(lm.calcResiduals(Doubles.of(new double[]{.5, -.2, .3})) != null);
    }

    @Test
    public void testTransformation() {
        Random rnd = new Random();

        DataBlock y = DataBlock.make(10);
        y.set((DoubleSupplier)rnd::nextDouble);
        DataBlock x1 = DataBlock.make(10);
        x1.set((DoubleSupplier)rnd::nextDouble);
        DataBlock x2 = DataBlock.make(10);
        x2.set((DoubleSupplier)rnd::nextDouble);
        LinearModel lm = LinearModel.builder()
                .y(y.map(Math::log))
                .addX(x1, x2)
                .build();
        assertTrue(lm != null);
        assertTrue(lm.calcResiduals(Doubles.of(new double[]{-.002, .003})) != null);
    }

}
