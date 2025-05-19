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

package uk.ac.ox.poseidon.core.predicates;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Simulation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AlwaysFalseFactoryTest {

    /**
     * The AlwaysFalseFactory class is responsible for creating
     * instances of the AlwaysFalse class in the context of a simulation.
     *
     * The newInstance method is a protected method that overrides the behavior
     * from the GlobalScopeFactory superclass to instantiate and return
     * an AlwaysFalse object using a given Simulation input.
     */

    /**
     * Test to verify that the `newInstance` method correctly creates a new instance of the
     * AlwaysFalse class.
     */
    @Test
    void testNewInstanceCreatesAlwaysFalse() {
        // Arrange
        final AlwaysFalseFactory factory = new AlwaysFalseFactory();
        final Simulation simulation = new Simulation();

        // Act
        final AlwaysFalse result = factory.newInstance(simulation);

        // Assert
        assertNotNull(result, "newInstance should not return null");
    }
}
