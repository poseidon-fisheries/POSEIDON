/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.utility.operators.CompressedExponentialFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class CompressedExponentialFunctionTest {

    @Test
    public void testApplyAsDouble() {
        final DoubleUnaryOperator f1 = new CompressedExponentialFunction(0, 2);
        Assertions.assertEquals(0, f1.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0, f1.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0, f1.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f2 = new CompressedExponentialFunction(0, 3);
        Assertions.assertEquals(0, f2.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0, f2.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0, f2.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f3 = new CompressedExponentialFunction(0, 4);
        Assertions.assertEquals(0, f3.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0, f3.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0, f3.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f4 = new CompressedExponentialFunction(0.5, 2);
        Assertions.assertEquals(0, f4.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.2211, f4.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0.6321, f4.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f5 = new CompressedExponentialFunction(0.5, 3);
        Assertions.assertEquals(0, f5.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.1175, f5.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0.6321, f5.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f6 = new CompressedExponentialFunction(0.5, 4);
        Assertions.assertEquals(0, f6.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.0605, f6.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0.6321, f6.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f7 = new CompressedExponentialFunction(1, 2);
        Assertions.assertEquals(0, f7.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.6321, f7.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(0.9816844, f7.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f8 = new CompressedExponentialFunction(1, 3);
        Assertions.assertEquals(0, f8.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.6321, f8.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(1, f8.applyAsDouble(2), EPSILON);

        final DoubleUnaryOperator f9 = new CompressedExponentialFunction(1, 4);
        Assertions.assertEquals(0, f9.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(0.6321, f9.applyAsDouble(1), EPSILON);
        Assertions.assertEquals(1, f9.applyAsDouble(2), EPSILON);
    }
}
