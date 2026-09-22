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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartesianDistanceCalculatorTest {

    private final CartesianDistanceCalculator calculator =
        new CartesianDistanceCalculator(null, 2.0);

    @Test
    void threeFourFiveTriangle() {
        // 3-4-5 triangle scaled by cellSizeInKm=2 should give a 6-8-10 result.
        assertEquals(
            10.0,
            calculator.distanceInKm(new Int2D(0, 0), new Int2D(3, 4)),
            1e-9
        );
    }

    @Test
    void samePointIsZero() {
        final Int2D cell = new Int2D(5, 5);
        assertEquals(0.0, calculator.distanceInKm(cell, cell), 1e-9);
    }

    @Test
    void isSymmetric() {
        final Int2D a = new Int2D(1, 2);
        final Int2D b = new Int2D(7, -3);
        assertTrue(
            Math.abs(calculator.distanceInKm(a, b) - calculator.distanceInKm(b, a)) < 1e-9
        );
    }
}
