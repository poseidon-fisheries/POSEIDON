/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.ac.ox.oxfish.utility.BinarySearch.highestWhere;

class BinarySearchTest {

    private final int min = 0;
    private final int max = 1000;

    @Test
    void highestWhereLessThanOne() {
        Assertions.assertEquals(
            0,
            highestWhere(min, max, n -> n < 1)
        );
    }

    @Test
    void highestWhereTrue() {
        Assertions.assertEquals(
            max,
            highestWhere(min, max, n -> true)
        );
    }

    @Test
    void highestWhereFalse() {
        Assertions.assertEquals(
            min,
            highestWhere(min, max, n -> false)
        );
    }

    @Test
    void highestWhereLessThan500() {
        Assertions.assertEquals(
            499,
            highestWhere(min, max, n -> n < 500)
        );
    }
}
