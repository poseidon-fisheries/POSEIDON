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

package uk.ac.ox.poseidon.geography.distance;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquirectangularDistanceCalculatorFactoryTest {

    /**
     * This class tests the `newInstance` method in the `EquirectangularDistanceCalculatorFactory`
     * class.
     * <p>
     * The method is responsible for creating a new instance of `EquirectangularDistanceCalculator`
     * using a `GridExtent` provided by the factory and the `Simulation` passed as a parameter.
     */

    @Test
    void testNewInstanceCreatesEquirectangularDistanceCalculator() {
        // Arrange
        final Factory<GridExtent> mockedGridExtentFactory = mock(Factory.class);
        final Simulation simulation = mock(Simulation.class);
        final GridExtent mockedGridExtent = mock(GridExtent.class);
        when(mockedGridExtentFactory.get(simulation)).thenReturn(mockedGridExtent);

        final EquirectangularDistanceCalculatorFactory factory =
            new EquirectangularDistanceCalculatorFactory(mockedGridExtentFactory);

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
