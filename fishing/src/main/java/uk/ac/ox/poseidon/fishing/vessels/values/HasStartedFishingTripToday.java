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

package uk.ac.ox.poseidon.fishing.vessels.values;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Holds for a vessel that has started a fishing trip on the current calendar
 * date. It listens to the vessel's fishing trip starts, keeping the date of the
 * last one, so it must be registered on the event manager of the vessel it is
 * asked about, from before that vessel's first trip. Never holds for a vessel
 * that has started no fishing trip.
 */
@RequiredArgsConstructor
public class HasStartedFishingTripToday
  implements Function<Vessel, Boolean>, Listener<FishingTripStartEvent>
{

  /** The clock, which gives the current date. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /**
   * The date the vessel's last fishing trip started, or {@code null} if it
   * has started none.
   */
  private LocalDate lastFishingTripStartDate;

  @Override
  public Class<FishingTripStartEvent> getEventClass() {
    return FishingTripStartEvent.class;
  }

  @Override
  public void receive(final FishingTripStartEvent event) {
    lastFishingTripStartDate = event.getTrip().getStartDateTime().toLocalDate();
  }

  @Override
  public Boolean apply(final Vessel vessel) {
    return (
      lastFishingTripStartDate != null &&
      lastFishingTripStartDate.equals(
        currentDateTime.apply(vessel).toLocalDate()
      )
    );
  }
}
