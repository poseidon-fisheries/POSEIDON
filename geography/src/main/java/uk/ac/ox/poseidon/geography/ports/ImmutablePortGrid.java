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

package uk.ac.ox.poseidon.geography.ports;

import lombok.NonNull;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

/**
 * A {@link PortGrid} whose ports are fixed at construction time, defensively copying the given
 * field so later mutation of the source doesn't affect this grid. Built via
 * {@link Factories#portGrid(uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory,
 * uk.ac.ox.poseidon.core.Factory)} in this package.
 */
public class ImmutablePortGrid extends AbstractPortGrid {

  /**
   * @param field           the field of ports to copy
   * @param bathymetricGrid the bathymetric grid ports are placed relative to
   */
  public ImmutablePortGrid(
    final SparseGrid2D field,
    @NonNull final BathymetricGrid bathymetricGrid
  ) {
    super(
      bathymetricGrid.getModelGrid(),
      new SparseGrid2D(field),
      bathymetricGrid
    );
  }
}
