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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.geography.paths.Factories.pathFinder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

class DefaultPathFinderFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final Simulation otherSimulation = mock(Simulation.class);

  @BeforeEach
  void giveTheMocksStores() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(otherSimulation.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void createsOnePathFinderPerSimulation() {
    final DefaultPathFinderFactory factory = pathFinder(
      object(bathymetricGrid()),
      object(mock(PortGrid.class)),
      object(mock(DistanceCalculator.class))
    );

    final GridPathFinder first = factory.get(new SimulationScope(simulation));
    final GridPathFinder second = factory.get(
      new SimulationScope(otherSimulation)
    );

    assertThat(first).isNotSameAs(second);
  }

  @Test
  void reusesPathFinderWithinSimulation() {
    final DefaultPathFinderFactory factory = pathFinder(
      object(bathymetricGrid()),
      object(mock(PortGrid.class)),
      object(mock(DistanceCalculator.class))
    );
    final SimulationScope scope = new SimulationScope(simulation);

    final GridPathFinder first = factory.get(scope);
    final GridPathFinder second = factory.get(scope);

    assertThat(first).isSameAs(second);
  }

  @Test
  void sharesAccessibleWaterNeighboursBetweenSimulations() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
    when(bathymetricGrid.getModelGrid()).thenReturn(modelGrid);
    final DefaultPathFinderFactory factory = pathFinder(
      object(bathymetricGrid),
      object(mock(PortGrid.class)),
      object(mock(DistanceCalculator.class))
    );
    final Int2D cell = new Int2D(1, 1);

    factory
      .get(new SimulationScope(simulation))
      .getAccessibleWaterNeighbours(cell, 1);
    factory
      .get(new SimulationScope(otherSimulation))
      .getAccessibleWaterNeighbours(cell, 1);

    verify(modelGrid, times(1)).getActiveNeighbours(cell, 1);
  }

  private static BathymetricGrid bathymetricGrid() {
    final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
    when(bathymetricGrid.getModelGrid()).thenReturn(mock(ModelGrid.class));
    return bathymetricGrid;
  }
}
