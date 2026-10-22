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
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A voyage of any kind: the span between a vessel undocking from a port, its
 * origin, with a destination in mind and {@link #endTrip} being called, with
 * the vessel docked again. A trip of a particular kind, such as a
 * {@link FishingTrip}, is a subclass adding what that kind records. Only a
 * {@link TripBlock}, through the vessel, starts and ends a trip. Broadcasts a
 * {@link TripStartEvent} as it starts and a {@link TripEndEvent} on
 * {@link #endTrip}, each of its kind's class, through an {@link EventManager}
 * that forwards to the vessel's own, so trip-scoped listeners can be added
 * without affecting vessel-level ones. {@code destination} can be redirected
 * mid-trip via {@link #setDestination}, e.g. to head towards a new destination
 * or, via {@link #setDestinationToTripOrigin()}, to head back to the origin
 * port; every change is validated against the vessel's grid, the way the
 * initial destination is at construction. {@code origin} and
 * {@code startDateTime} are fixed once the trip begins.
 * <p>
 * A trip holds no clock: it is given the dates it starts and ends, and
 * records the vessel's dockings and undockings from their events. A docking
 * stays open until the vessel undocks, which makes it one of the trip's
 * {@linkplain #getPortCalls() port calls}, or the trip ends, which makes it
 * the trip's arrival in port. Its time at sea is the time it was not docked:
 * {@linkplain #getTimeAtSeaUntil so far}, given the date, and in
 * {@linkplain #getTotalTimeAtSea() total} once it has ended, which is from its
 * start to its arrival less its port calls. Its
 * {@linkplain #getTotalDuration() total duration} runs from its start to its
 * end, so it also counts the time spent in port at the end, e.g. landing the
 * catch.
 */
@Getter
public class Trip {

  private final @NonNull Vessel vessel;
  private final @NonNull EventManager eventManager;
  private final @NonNull Port origin;
  private final @NonNull LocalDateTime startDateTime;
  private final Account account = new Account();

  private @NonNull Destination destination;

  /** The stops of this trip before its end, in the order they happened. */
  @Getter(NONE)
  private final List<PortCall> portCalls = new ArrayList<>();

  /**
   * The vessel's docking during this trip that is not followed by an
   * undocking, if it is docked; the arrival once the trip has ended.
   */
  @Getter(NONE)
  private DockingEvent openDocking;

  /** When the trip arrived in port, set as it ends. */
  private LocalDateTime portArrivalDateTime;
  private LocalDateTime endDateTime;

  /**
   * @param vessel        the vessel taking this trip
   * @param origin        the port the vessel undocked from to start the trip
   * @param destination   where the trip is initially headed; must be on a cell
   *                      in the vessel's grid
   * @param startDateTime when the trip starts
   */
  Trip(
    final @NonNull Vessel vessel,
    final @NonNull Port origin,
    final @NonNull Destination destination,
    final @NonNull LocalDateTime startDateTime
  ) {
    this.vessel = vessel;
    this.destination = checkInGrid(destination);
    this.eventManager = new ForwardingEventManager(vessel.getEventManager());
    // Registered on the trip's own event manager, which delivers to its
    // listeners before forwarding to the vessel's, so the record is complete
    // before vessel-level listeners react.
    this.eventManager.addListener(
      new SimpleListener<>(DockingEvent.class, event -> openDocking = event)
    );
    this.eventManager.addListener(
      new SimpleListener<>(UndockingEvent.class, this::closeDocking)
    );
    this.origin = origin;
    this.startDateTime = startDateTime;
  }

  /**
   * Broadcasts the trip's start event. Called by the vessel once the trip is
   * constructed, so that a subclass is complete before its start is heard.
   */
  void start() {
    eventManager.broadcast(newStartEvent());
  }

  /** @return the event announcing this trip's start, of its kind's class */
  TripStartEvent newStartEvent() {
    return new TripStartEvent(this);
  }

  /** @return the event announcing this trip's end, of its kind's class */
  TripEndEvent newEndEvent() {
    return new TripEndEvent(this);
  }

  /**
   * @param destination the new destination; must be on a cell in the vessel's
   *                    grid
   * @throws IllegalArgumentException if {@code destination} is outside the
   *                                  vessel's grid
   */
  public void setDestination(final @NonNull Destination destination) {
    this.destination = checkInGrid(destination);
  }

  private Destination checkInGrid(final Destination destination) {
    vessel
      .getVesselField()
      .getModelGrid()
      .checkIsInGrid(destination.getCell(vessel.getPortGrid()));
    return destination;
  }

  /** Records the open docking, which {@code undocking} ends, as a port call. */
  private void closeDocking(final UndockingEvent undocking) {
    portCalls.add(
      new PortCall(
        openDocking.getPort(),
        openDocking.getDateTime(),
        undocking.getDateTime()
      )
    );
    openDocking = null;
  }

  /**
   * @return an unmodifiable view of this trip's port calls, its stops before
   *         its end, in the order they happened
   */
  public List<PortCall> getPortCalls() {
    return Collections.unmodifiableList(portCalls);
  }

  /**
   * @param dateTime the date to count to, no earlier than the trip's last
   *                 docking or undocking
   * @return the time from the trip's start to {@code dateTime} during which the
   *         vessel was not docked
   */
  public Duration getTimeAtSeaUntil(final @NonNull LocalDateTime dateTime) {
    final Duration timeDocked = portCalls
      .stream()
      .map(PortCall::duration)
      .reduce(
        openDocking == null
          ? Duration.ZERO
          : Duration.between(openDocking.getDateTime(), dateTime),
        Duration::plus
      );
    return Duration.between(startDateTime, dateTime).minus(timeDocked);
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
   * @return the time from the trip's start to its arrival in port, less its
   *         port calls
   * @throws IllegalStateException if the trip has not ended
   */
  public Duration getTotalTimeAtSea() {
    checkState(
      endDateTime != null,
      "The trip of vessel %s has not ended.",
      vessel.getId()
    );
    return getTimeAtSeaUntil(endDateTime);
  }

  /**
   * Ends the trip, making the vessel's open docking its arrival in port,
   * charges each of {@code tripCosts} to its account, then broadcasts a
   * {@link TripEndEvent}, so that the event's readers see the final account.
   *
   * @param endDateTime when the trip ends
   * @param tripCosts   the costs of the finished trip, possibly none
   * @throws IllegalStateException if the vessel is not docked
   */
  void endTrip(
    final @NonNull LocalDateTime endDateTime,
    final List<? extends Function<? super Trip, ? extends Money>> tripCosts
  ) {
    checkState(
      vessel.isDocked(),
      "The trip of vessel %s cannot end at sea.",
      vessel.getId()
    );
    this.endDateTime = endDateTime;
    this.portArrivalDateTime = openDocking.getDateTime();
    tripCosts.forEach(tripCost -> account.subtract(tripCost.apply(this)));
    this.eventManager.broadcast(newEndEvent());
  }

  /** Redirects the trip back towards the port it started from. */
  public void setDestinationToTripOrigin() {
    setDestination(origin);
  }
}
