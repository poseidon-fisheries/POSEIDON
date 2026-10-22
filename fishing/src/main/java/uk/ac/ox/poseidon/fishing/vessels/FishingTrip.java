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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.FishingTripStartEvent;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A {@link Trip} taken to fish: it records its fishing events, and broadcasts
 * a {@link FishingTripStartEvent} and a {@link FishingTripEndEvent}, so that
 * readers of fishing trips only, such as learning, never hear of other trips.
 * Only a {@link FishingTripBlock}, through the vessel, starts one.
 */
public class FishingTrip extends Trip {

  /** The fishing events of this trip, in the order they happened. */
  private final List<FishingEvent> fishingEvents = new ArrayList<>();

  /**
   * @param vessel        the vessel taking this trip
   * @param origin        the port the vessel undocked from to start the trip
   * @param destination   where the trip is initially headed; must be on a cell
   *                      in the vessel's grid
   * @param startDateTime when the trip starts
   */
  FishingTrip(
    final @NonNull Vessel vessel,
    final @NonNull Port origin,
    final @NonNull Destination destination,
    final @NonNull LocalDateTime startDateTime
  ) {
    super(vessel, origin, destination, startDateTime);
    // Registered on the trip's own event manager, which delivers to its
    // listeners before forwarding to the vessel's, so the record is complete
    // before vessel-level listeners react.
    getEventManager().addListener(
      new SimpleListener<>(FishingEvent.class, fishingEvents::add)
    );
  }

  @Override
  FishingTripStartEvent newStartEvent() {
    return new FishingTripStartEvent(this);
  }

  @Override
  FishingTripEndEvent newEndEvent() {
    return new FishingTripEndEvent(this);
  }

  /**
   * @return an unmodifiable view of the fishing events of this trip, in the
   *         order they happened
   */
  public List<FishingEvent> getFishingEvents() {
    return Collections.unmodifiableList(fishingEvents);
  }
}
