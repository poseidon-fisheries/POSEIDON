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
import lombok.Setter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;

import java.time.LocalDateTime;

@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public class Trip {

    private final Vessel vessel;
    private final EventManager eventManager;
    private final Int2D origin;
    private final LocalDateTime startDateTime;
    @Setter private Int2D destination;
    private LocalDateTime endDateTime;

    public Trip(
        final Vessel vessel,
        final Int2D destination
    ) {
        this.vessel = vessel;
        this.eventManager = new ForwardingEventManager(vessel.getEventManager());
        this.origin = vessel.getCell();
        this.startDateTime = vessel.getSchedule().getDateTime();
        this.eventManager.broadcast(new TripStartEvent(this));
    }

    public void endTrip() {
        this.endDateTime = vessel.getSchedule().getDateTime();
        this.eventManager.broadcast(new TripEndEvent(this));
    }

    public void setDestinationToTripOrigin() {
        destination = origin;
    }
}
