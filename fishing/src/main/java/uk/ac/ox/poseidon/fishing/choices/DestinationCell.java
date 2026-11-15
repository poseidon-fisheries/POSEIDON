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

package uk.ac.ox.poseidon.fishing.choices;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import java.util.function.Function;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * The distinct cell a fishing trip was headed for. Port destinations are
 * ignored by type; repeated visits to the same cell give one key. No cell
 * gives no key, and several distinct cells are rejected because one trip's
 * outcome cannot be credited to several destinations.
 */
public class DestinationCell
  implements Function<FishingTripEndEvent, Optional<Cell>>
{

  @Override
  public Optional<Cell> apply(final FishingTripEndEvent event) {
    final var destinations = event
      .getTrip()
      .getDestinations()
      .stream()
      .filter(Cell.class::isInstance)
      .map(Cell.class::cast)
      .distinct()
      .toList();
    checkState(
      destinations.size() <= 1,
      "A trip outcome requires at most one distinct destination cell."
    );
    return destinations.stream().findFirst();
  }
}
