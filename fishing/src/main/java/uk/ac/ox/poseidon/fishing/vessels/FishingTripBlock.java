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

package uk.ac.ox.poseidon.fishing.vessels;

import com.badlogic.gdx.ai.btree.Task;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.joda.money.Money;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A {@link TripBlock} that opens a {@link FishingTrip}, so that the trip
 * records its fishing events and announces its start and end to readers of
 * fishing trips.
 */
public final class FishingTripBlock extends TripBlock {

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave
   * @param currentDateTime    the clock, which dates the trip's start and end
   * @param tripCosts          the costs of the finished trip, charged in
   *                           order; possibly none
   * @param children           the tasks run in sequence during the trip
   * @throws IllegalArgumentException if {@code children} is empty
   */
  public FishingTripBlock(
    final Function<? super Vessel, Optional<Cell>> initialDestination,
    final Function<? super Vessel, LocalDateTime> currentDateTime,
    final List<? extends Function<? super Trip, ? extends Money>> tripCosts,
    final List<? extends Task<Vessel>> children
  ) {
    super(initialDestination, currentDateTime, tripCosts, children);
  }

  @Override
  FishingTrip newTrip(
    final Vessel vessel,
    final Port origin,
    final Destination destination,
    final LocalDateTime startDateTime
  ) {
    return new FishingTrip(vessel, origin, destination, startDateTime);
  }
}
