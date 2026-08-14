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

package uk.ac.ox.poseidon.core.predicates.comparable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BetweenTest {

    @Test
    void inclusiveBoundsIncludeEndpoints() {
        final Between<Double> between =
            new Between<>(() -> 0.0, () -> 10.0, true, true);

        assertTrue(between.test(0.0));
        assertTrue(between.test(5.0));
        assertTrue(between.test(10.0));
        assertFalse(between.test(-0.1));
        assertFalse(between.test(10.1));
    }

    @Test
    void exclusiveMinimumExcludesLowerEndpointOnly() {
        final Between<Double> between =
            new Between<>(() -> 0.0, () -> 10.0, false, true);

        assertFalse(between.test(0.0));
        assertTrue(between.test(5.0));
        assertTrue(between.test(10.0));
    }

    @Test
    void exclusiveMaximumExcludesUpperEndpointOnly() {
        final Between<Double> between =
            new Between<>(() -> 0.0, () -> 10.0, true, false);

        assertTrue(between.test(0.0));
        assertTrue(between.test(5.0));
        assertFalse(between.test(10.0));
    }

    @Test
    void exclusiveBothBoundsExcludeBothEndpoints() {
        final Between<Double> between =
            new Between<>(() -> 0.0, () -> 10.0, false, false);

        assertFalse(between.test(0.0));
        assertTrue(between.test(5.0));
        assertFalse(between.test(10.0));
    }
}
