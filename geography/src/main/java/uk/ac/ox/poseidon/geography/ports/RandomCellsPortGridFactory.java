/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.ports;

import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.functions.WithoutContext;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A {@link SimulationScopeFactory} for a {@link MutablePortGrid} with a fixed
 * number of ports placed on randomly-chosen suitable land cells (land cells
 * with at least {@code minimumAdjacentWaterTiles} active water neighbours),
 * using the simulation's own random stream. There's no separate plain component
 * class here: the produced {@link PortGrid} is returned as-is, with no wrapper
 * type to carry documentation, so this factory carries the behavior doc
 * directly. Built via
 * {@link Factories#randomCellsPortGrid(Factory, MappingFactory, int, int)}
 * in this package.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class RandomCellsPortGridFactory
  extends SimulationScopeFactory<PortGrid>
{

  private final Factory<
    ? super SimulationScope,
    ? extends BathymetricGrid
  > bathymetricGrid;
  private final MappingFactory<
    ? super SimulationScope,
    ? super Void,
    ? super Cell,
    ? extends String
  > code;
  private final int numberOfPorts;
  private final int minimumAdjacentWaterTiles;
  private final Factory<
    ? super SimulationScope,
    ? extends MersenneTwisterFast
  > randomNumberGenerator;

  @Override
  protected PortGrid newInstance(final SimulationScope scope) {
    final BathymetricGrid bathymetricGrid = this.bathymetricGrid.get(scope);
    final ModelGrid modelGrid = bathymetricGrid.getModelGrid();
    final List<Cell> suitableTiles = bathymetricGrid
      .getLandCells()
      .filter(
        cell ->
          modelGrid
            .getActiveNeighbours(cell)
            .stream()
            .filter(bathymetricGrid::isWater)
            .count() >= minimumAdjacentWaterTiles
      )
      .toList();
    checkState(
      suitableTiles.size() >= numberOfPorts,
      "Only %s suitable land tiles for %s ports.",
      suitableTiles.size(),
      numberOfPorts
    );
    final SparseGrid2D sparseGrid2D = new SparseGrid2D(
      modelGrid.getGridWidth(),
      modelGrid.getGridHeight()
    );
    final MutablePortGrid portGrid = new MutablePortGrid(
      bathymetricGrid,
      sparseGrid2D
    );
    final Function<Cell, String> code = WithoutContext.function(
      this.code.get(scope)
    );
    shuffledStream(suitableTiles, randomNumberGenerator.get(scope))
      .limit(numberOfPorts)
      .forEach(cell -> {
        final String portCode = code.apply(cell);
        portGrid.createPort(portCode, "Port " + portCode, cell);
      });
    return portGrid;
  }
}
