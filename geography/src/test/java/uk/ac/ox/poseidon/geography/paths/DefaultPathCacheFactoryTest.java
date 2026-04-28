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
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathCache;

class DefaultPathCacheFactoryTest {

    @Test
    void sharesCacheWhenRoutingInputsAreGlobal() {
        final DefaultPathCacheFactory<SimulationScope> factory =
            pathCache(
                object(mock(BathymetricGrid.class)),
                object(mock(PortGrid.class)),
                object(mock(DistanceCalculator.class))
            );

        final PathCache<Int2D> first = factory.get(simulationScope());
        final PathCache<Int2D> second = factory.get(simulationScope());

        assertThat(first).isSameAs(second);
    }

    @Test
    void separatesCacheWhenRoutingInputsAreSimulationScoped() {
        final DefaultPathCacheFactory<SimulationScope> factory =
            pathCache(
                new SimulationBathymetricGridFactory(),
                object(mock(PortGrid.class)),
                object(mock(DistanceCalculator.class))
            );

        final PathCache<Int2D> first = factory.get(simulationScope());
        final PathCache<Int2D> second = factory.get(simulationScope());

        assertThat(first).isNotSameAs(second);
    }

    private static SimulationScope simulationScope() {
        return new SimulationScope(mock(Simulation.class));
    }

    private static final class SimulationBathymetricGridFactory
        extends SimulationScopeFactory<BathymetricGrid> {

        @Override
        protected BathymetricGrid newInstance(final SimulationScope scope) {
            return mock(BathymetricGrid.class);
        }
    }
}
