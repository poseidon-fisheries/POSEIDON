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

package uk.ac.ox.poseidon.agents.tasks.travel;

import lombok.Value;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.events.ExtendedEvent;

import java.time.LocalDateTime;

/** An {@link ExtendedEvent} recording one completed leg of travel between two cells. */
@Value
public class TravelEvent implements ExtendedEvent {
    /** The vessel that travelled. */
    Vessel vessel;
    /** When travel started. */
    LocalDateTime startDateTime;
    /** When travel ended. */
    LocalDateTime endDateTime;
    /** The cell travel started from. */
    Int2D origin;
    /** The cell travel ended at. */
    Int2D destination;
}
