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
import static org.mockito.Mockito.*;

class HaversineDistanceCalculatorFactoryTest {

    /**
     * Test class for the HaversineDistanceCalculatorFactory.
     * <p>
     * The `newInstance` method is tested to ensure it creates and returns a valid instance of
     * HaversineDistanceCalculator with the correct dependencies initialized.
     */

    @Test
    void testNewInstance_createsHaversineDistanceCalculatorWithValidGridExtent() {
        // Arrange
        final GridExtent mockedGridExtent = mock(GridExtent.class);
        @SuppressWarnings("unchecked") final Factory<GridExtent> gridExtentFactory =
            mock(Factory.class);
        final Simulation simulation = mock(Simulation.class);

        when(gridExtentFactory.get(simulation)).thenReturn(mockedGridExtent);

        final HaversineDistanceCalculatorFactory factory = new HaversineDistanceCalculatorFactory();
        factory.setGridExtent(gridExtentFactory);

        // Act
        final HaversineDistanceCalculator result = factory.newInstance(simulation);

        // Assert
        assertNotNull(result);
        verify(gridExtentFactory, times(1)).get(simulation);
    }
}
