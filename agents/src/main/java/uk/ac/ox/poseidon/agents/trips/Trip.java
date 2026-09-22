/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.trips;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;

import java.time.LocalDateTime;

/**
 * A single fishing trip: the span between a vessel leaving its current cell with a destination in
 * mind and {@link #endTrip()} being called. Broadcasts a {@link TripStartEvent} at construction and
 * a {@link TripEndEvent} on {@link #endTrip()}, through an {@link EventManager} that forwards to
 * the vessel's own, so trip-scoped listeners can be added without affecting vessel-level ones.
 * {@code destination} can be redirected mid-trip via {@link #setDestination}, e.g. to head towards
 * a new destination or, via {@link #setDestinationToTripOrigin()}, to abort and return home; every
 * change is validated against the vessel's grid, the way the initial destination is at
 * construction. {@code origin} and {@code startDateTime} are fixed once the trip begins.
 */
@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public class Trip {

    private final @NonNull Vessel vessel;
    private final @NonNull EventManager eventManager;
    private final @NonNull Int2D origin;
    private final @NonNull LocalDateTime startDateTime;
    private final Account account = new Account();

    private @NonNull Int2D destination;
    private LocalDateTime endDateTime;

    /**
     * @param vessel      the vessel taking this trip
     * @param destination where the trip is initially headed; must be a cell in the vessel's grid
     */
    public Trip(
        final @NonNull Vessel vessel,
        final @NonNull Int2D destination
    ) {
        this.vessel = vessel;
        setDestination(destination);
        this.eventManager = new ForwardingEventManager(vessel.getEventManager());
        this.origin = vessel.getCell();
        this.startDateTime = vessel.getSchedule().getDateTime();
        this.eventManager.broadcast(new TripStartEvent(this));
    }

    /**
     * @param destination the new destination; must be a cell in the vessel's grid
     * @throws IllegalArgumentException if {@code destination} is outside the vessel's grid
     */
    public void setDestination(final @NonNull Int2D destination) {
        vessel.getVesselField().getModelGrid().checkIsInGrid(destination);
        this.destination = destination;
    }

    /** Marks the trip as ended at the vessel's current simulation time and broadcasts a {@link TripEndEvent}. */
    public void endTrip() {
        this.endDateTime = vessel.getSchedule().getDateTime();
        this.eventManager.broadcast(new TripEndEvent(this));
    }

    /** Redirects the trip back towards where it started. */
    public void setDestinationToTripOrigin() {
        setDestination(origin);
    }
}
