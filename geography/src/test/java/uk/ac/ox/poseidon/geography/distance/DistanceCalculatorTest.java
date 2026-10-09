/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.distance;

import org.junit.jupiter.api.Test;
import sim.util.Int2D;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DistanceCalculatorTest {

    @Test
    void travelDurationIsDistanceOverSpeed() {
        assertThat(DistanceCalculator.travelDuration(5.0, 10.0))
            .isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void travelDurationAlongAPathIsItsLengthOverSpeed() {
        final DistanceCalculator calculator = new CartesianDistanceCalculator(null, 1.0);
        assertThat(calculator.travelDuration(List.of(new Int2D(0, 0), new Int2D(3, 4)), 10.0))
            .isEqualTo(Duration.ofMinutes(30));
    }
}
