/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.trips;

import java.util.function.BiFunction;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * Holds for a cell where the vessel has fished during its current fishing
 * trip: the cell of the start coordinate of one of the trip's
 * {@link FishingTrip#getFishingEvents() fishing events}, in the vessel's model
 * grid. Asked of a vessel not on a fishing trip, it throws, as reading it there
 * is a model error.
 */
public class CellWasFishedThisTrip
  implements BiFunction<Vessel, Cell, Boolean>
{

  @Override
  public Boolean apply(final Vessel vessel, final Cell cell) {
    if (!(vessel.getCurrentTrip() instanceof final FishingTrip fishingTrip)) {
      throw new IllegalStateException(
        "Vessel " + vessel.getId() + " is not on a fishing trip."
      );
    }
    final ModelGrid modelGrid = vessel.getVesselField().getModelGrid();
    return fishingTrip
      .getFishingEvents()
      .stream()
      .anyMatch(event ->
        modelGrid.toCell(event.getAction().getStartCoordinate()).equals(cell)
      );
  }
}
