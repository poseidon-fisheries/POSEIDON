/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import org.junit.Test;

import java.util.function.DoubleUnaryOperator;
import uk.ac.ox.oxfish.utility.operators.LogisticFunction;

import static java.lang.Double.MAX_VALUE;
import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class LogisticFunctionTest {

    @Test
    public void test() {
        final DoubleUnaryOperator f = new LogisticFunction(0, 1);
        assertEquals(0.0, f.applyAsDouble(-MAX_VALUE), EPSILON);
        assertEquals(0.5, f.applyAsDouble(0), EPSILON);
        assertEquals(1.0, f.applyAsDouble(MAX_VALUE), EPSILON);
    }

}