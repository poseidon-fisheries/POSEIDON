/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.regulations.predicates;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Simulation;

import static org.junit.jupiter.api.Assertions.*;

class AlwaysTrueFactoryTest {

    /**
     * Test class for AlwaysTrueFactory. The AlwaysTrueFactory is responsible for creating new
     * instances of the AlwaysTrue class in the context of a given Simulation.
     * <p>
     * The newInstance method should always return a non-null instance of the AlwaysTrue class.
     */

    @Test
    void newInstance_shouldReturnNonNullAlwaysTrueInstance() {
        // Arrange
        final AlwaysTrueFactory factory = new AlwaysTrueFactory();
        final Simulation simulation = new Simulation();

        // Act
        final AlwaysTrue result = factory.newInstance(simulation);

        // Assert
        assertNotNull(result, "newInstance should return a non-null AlwaysTrue instance");
        assertInstanceOf(
            AlwaysTrue.class,
            result,
            "newInstance should return an instance of AlwaysTrue"
        );
    }
}
