/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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
import uk.ac.ox.oxfish.utility.operators.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static java.lang.Double.MAX_VALUE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class LogisticFunctionTest {

    @Test
    public void test() {
        final DoubleUnaryOperator f = new LogisticFunction(0, 1);
        Assertions.assertEquals(0.0, f.applyAsDouble(-MAX_VALUE), EPSILON);
        Assertions.assertEquals(0.5, f.applyAsDouble(0), EPSILON);
        Assertions.assertEquals(1.0, f.applyAsDouble(MAX_VALUE), EPSILON);
    }

}
