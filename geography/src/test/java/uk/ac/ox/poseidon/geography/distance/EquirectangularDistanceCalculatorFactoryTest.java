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

package uk.ac.ox.poseidon.geography.distance;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquirectangularDistanceCalculatorFactoryTest {

    /**
     * This class tests the `newInstance` method in the `EquirectangularDistanceCalculatorFactory`
     * class.
     * <p>
     * The method is responsible for creating a new instance of `EquirectangularDistanceCalculator`
     * using a `ModelGrid` provided by the factory and the `Simulation` passed as a parameter.
     */

    @Test
    void testNewInstanceCreatesEquirectangularDistanceCalculator() {
        // Arrange
        final Factory<ModelGrid> mockedModelGridFactory = mock(Factory.class);
        final Simulation simulation = mock(Simulation.class);
        final ModelGrid mockedModelGrid = mock(ModelGrid.class);
        when(mockedModelGridFactory.get(simulation)).thenReturn(mockedModelGrid);

        final EquirectangularDistanceCalculatorFactory factory =
            new EquirectangularDistanceCalculatorFactory(mockedModelGridFactory);

        // Act
        final EquirectangularDistanceCalculator result = factory.newInstance(simulation);

        // Assert
        assertNotNull(
            result,
            "The newInstance method should return a non-null EquirectangularDistanceCalculator " +
                "instance."
        );
    }
}
