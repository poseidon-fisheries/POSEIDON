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

package uk.ac.ox.poseidon.fishing.fuel;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A grid of {@link FuelStation}s, keyed by each station's {@code getCode()}.
 */
public class FuelStationGrid extends ObjectGrid<FuelStation> {

  /** @param modelGrid the grid this is defined over; starts empty */
  public FuelStationGrid(final ModelGrid modelGrid) {
    super(modelGrid);
  }

  @Override
  protected String getObjectId(final FuelStation object) {
    return object.getCode();
  }

  /**
   * Finds a fuel station by the port it belongs to, not by its cell: ports can
   * share a cell.
   *
   * @param port the port whose fuel station to find
   * @return the one fuel station of {@code port}
   * @throws IllegalStateException if {@code port} does not have exactly one
   *                               fuel station
   */
  public FuelStation getFuelStation(final Port port) {
    final List<FuelStation> fuelStations = fuelStationsOf(port);
    checkState(
      fuelStations.size() == 1,
      "Expected one fuel station at port %s, found %s.",
      port,
      fuelStations.size()
    );
    return fuelStations.getFirst();
  }

  /**
   * @param port the port whose fuel stations to find
   * @return the fuel stations of {@code port}, by scanning every station; a
   *         grid whose stations never change can index them instead
   */
  protected List<FuelStation> fuelStationsOf(final Port port) {
    return stream()
      .filter(fuelStation -> fuelStation.getPort().equals(port))
      .toList();
  }
}
