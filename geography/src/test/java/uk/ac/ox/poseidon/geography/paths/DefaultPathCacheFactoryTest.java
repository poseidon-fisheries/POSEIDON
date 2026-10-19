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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathCache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class DefaultPathCacheFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final Simulation otherSimulation = mock(Simulation.class);

  @BeforeEach
  void giveTheMocksStores() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(otherSimulation.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void sharesCacheWhenRoutingInputsAreGlobal() {
    final DefaultPathCacheFactory<SimulationScope> factory = pathCache(
      object(bathymetricGrid()),
      object(mock(PortGrid.class)),
      object(mock(DistanceCalculator.class))
    );

    final PathCache<Int2D> first = factory.get(new SimulationScope(simulation));
    final PathCache<Int2D> second = factory.get(
      new SimulationScope(otherSimulation)
    );

    assertThat(first).isSameAs(second);
  }

  @Test
  void separatesCacheWhenRoutingInputsAreSimulationScoped() {
    final DefaultPathCacheFactory<SimulationScope> factory = pathCache(
      new SimulationBathymetricGridFactory(),
      object(mock(PortGrid.class)),
      object(mock(DistanceCalculator.class))
    );

    final PathCache<Int2D> first = factory.get(new SimulationScope(simulation));
    final PathCache<Int2D> second = factory.get(
      new SimulationScope(otherSimulation)
    );

    assertThat(first).isNotSameAs(second);
  }

  private static BathymetricGrid bathymetricGrid() {
    final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
    when(bathymetricGrid.getModelGrid()).thenReturn(
      ModelGrid.create(1, 1, new Envelope(0, 1, 0, 1))
    );
    return bathymetricGrid;
  }

  private static final class SimulationBathymetricGridFactory
    extends SimulationScopeFactory<BathymetricGrid>
  {

    @Override
    protected BathymetricGrid newInstance(final SimulationScope scope) {
      return bathymetricGrid();
    }
  }
}
