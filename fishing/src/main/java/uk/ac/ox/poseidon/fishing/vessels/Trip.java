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

package uk.ac.ox.poseidon.fishing.vessels;

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.NONE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;

/**
 * A single fishing trip: the span between a vessel leaving its current cell
 * with a destination in mind and {@link #endTrip} being called. Only a
 * {@link TripBlock}, through the vessel, starts and ends a trip. Broadcasts a
 * {@link TripStartEvent} at construction and a {@link TripEndEvent} on
 * {@link #endTrip}, through an {@link EventManager} that forwards to the
 * vessel's own, so trip-scoped listeners can be added without affecting
 * vessel-level ones. {@code destination} can be redirected mid-trip via
 * {@link #setDestination}, e.g. to head towards a new destination or, via
 * {@link #setDestinationToTripOrigin()}, to abort and return home; every change
 * is validated against the vessel's grid, the way the initial destination is at
 * construction. {@code origin} and {@code startDateTime} are fixed once the
 * trip begins.
 * <p>
 * A trip holds no clock: it is given the dates it starts, arrives in port and
 * ends. It has two totals, each known once its leg is over: its
 * {@linkplain #getTotalTimeAtSea() total time at sea}, from its start to its
 * arrival in port ({@link #arriveInPort}), and its
 * {@linkplain #getTotalDuration() total duration}, from its start to its end.
 * The difference is the time spent in port before the trip ends, e.g. landing
 * the catch. The time at sea or duration so far is its reader's to work out,
 * from the trip's start and the clock.
 */
@Getter
public class Trip {

  private final @NonNull Vessel vessel;
  private final @NonNull EventManager eventManager;
  private final @NonNull Int2D origin;
  private final @NonNull LocalDateTime startDateTime;
  private final Account account = new Account();

  /** The fishing events of this trip, in the order they happened. */
  @Getter(NONE)
  private final List<FishingEvent> fishingEvents = new ArrayList<>();

  private @NonNull Int2D destination;
  private LocalDateTime portArrivalDateTime;
  private LocalDateTime endDateTime;

  /**
   * @param vessel        the vessel taking this trip
   * @param destination   where the trip is initially headed; must be a cell in
   *                      the vessel's grid
   * @param startDateTime when the trip starts
   */
  Trip(
    final @NonNull Vessel vessel,
    final @NonNull Int2D destination,
    final @NonNull LocalDateTime startDateTime
  ) {
    this.vessel = vessel;
    this.destination = checkInGrid(destination);
    this.eventManager = new ForwardingEventManager(vessel.getEventManager());
    // Registered on the trip's own event manager, which delivers to its
    // listeners before forwarding to the vessel's, so the record is complete
    // before vessel-level listeners react.
    this.eventManager.addListener(
      new SimpleListener<>(FishingEvent.class, fishingEvents::add)
    );
    this.origin = vessel.getCell();
    this.startDateTime = startDateTime;
    this.eventManager.broadcast(new TripStartEvent(this));
  }

  /**
   * @param destination the new destination; must be a cell in the vessel's grid
   * @throws IllegalArgumentException if {@code destination} is outside the
   *                                  vessel's grid
   */
  public void setDestination(final @NonNull Int2D destination) {
    this.destination = checkInGrid(destination);
  }

  private Int2D checkInGrid(final Int2D cell) {
    vessel.getVesselField().getModelGrid().checkIsInGrid(cell);
    return cell;
  }

  /**
   * Records the vessel's arrival in port, which ends its time at sea.
   *
   * @param arrivalDateTime when the vessel arrives
   * @throws IllegalStateException if the vessel is not at a port, or the trip
   *                               has already arrived in port
   */
  public void arriveInPort(final @NonNull LocalDateTime arrivalDateTime) {
    checkState(
      !hasArrivedInPort(),
      "The trip of vessel %s has already arrived in port.",
      vessel.getId()
    );
    checkState(
      vessel.isAtPort(),
      "Vessel %s is not at a port.",
      vessel.getId()
    );
    this.portArrivalDateTime = arrivalDateTime;
  }

  /**
   * @return whether the trip has arrived in port; see {@link #arriveInPort}
   */
  public boolean hasArrivedInPort() {
    return portArrivalDateTime != null;
  }

  /**
   * @return the time from the trip's start to its end
   * @throws IllegalStateException if the trip has not ended
   */
  public Duration getTotalDuration() {
    checkState(
      endDateTime != null,
      "The trip of vessel %s has not ended.",
      vessel.getId()
    );
    return Duration.between(startDateTime, endDateTime);
  }

  /**
   * @return the time from the trip's start to its arrival in port
   * @throws IllegalStateException if the trip has not arrived in port
   */
  public Duration getTotalTimeAtSea() {
    checkState(
      hasArrivedInPort(),
      "The trip of vessel %s has not arrived in port.",
      vessel.getId()
    );
    return Duration.between(startDateTime, portArrivalDateTime);
  }

  /**
   * Marks the trip as ended and broadcasts a {@link TripEndEvent}.
   *
   * @param endDateTime when the trip ends
   * @throws IllegalStateException if the trip has not arrived in port
   */
  void endTrip(final @NonNull LocalDateTime endDateTime) {
    checkState(
      hasArrivedInPort(),
      "The trip of vessel %s cannot end before arriving in port.",
      vessel.getId()
    );
    this.endDateTime = endDateTime;
    this.eventManager.broadcast(new TripEndEvent(this));
  }

  /**
   * @return an unmodifiable view of the fishing events of this trip, in the
   *         order they happened
   */
  public List<FishingEvent> getFishingEvents() {
    return Collections.unmodifiableList(fishingEvents);
  }

  /** Redirects the trip back towards where it started. */
  public void setDestinationToTripOrigin() {
    setDestination(origin);
  }
}
