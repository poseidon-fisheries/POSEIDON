/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.apache.commons.math3.util.Precision.EPSILON;

public class OneAtATimeSensitivityTest {

    @Test
    public void testValueRange() {
        Assertions.assertArrayEquals(
            new double[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
            OneAtATimeSensitivity.valueRange(0, 100, 11).toArray(),
            EPSILON
        );
        Assertions.assertArrayEquals(
            new double[]{25, 35, 45, 55, 65, 75},
            OneAtATimeSensitivity.valueRange(25, 75, 6).toArray(),
            EPSILON
        );
    }
}
