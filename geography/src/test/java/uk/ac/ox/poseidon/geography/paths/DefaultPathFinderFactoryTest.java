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

package uk.ac.ox.poseidon.geography.paths;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathFinder;

class DefaultPathFinderFactoryTest {

    @Test
    void createsOnePathFinderPerSimulation() {
        final DefaultPathFinderFactory factory =
            pathFinder(
                object(bathymetricGrid()),
                object(mock(PortGrid.class)),
                object(mock(DistanceCalculator.class))
            );

        final GridPathFinder first = factory.get(simulationScope());
        final GridPathFinder second = factory.get(simulationScope());

        assertThat(first).isNotSameAs(second);
    }

    @Test
    void reusesPathFinderWithinSimulation() {
        final DefaultPathFinderFactory factory =
            pathFinder(
                object(bathymetricGrid()),
                object(mock(PortGrid.class)),
                object(mock(DistanceCalculator.class))
            );
        final SimulationScope scope = simulationScope();

        final GridPathFinder first = factory.get(scope);
        final GridPathFinder second = factory.get(scope);

        assertThat(first).isSameAs(second);
    }

    private static BathymetricGrid bathymetricGrid() {
        final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
        when(bathymetricGrid.getModelGrid()).thenReturn(mock(ModelGrid.class));
        return bathymetricGrid;
    }

    private static SimulationScope simulationScope() {
        return new SimulationScope(mock(Simulation.class));
    }
}
