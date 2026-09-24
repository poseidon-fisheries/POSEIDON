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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import lombok.Value;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.core.events.ExtendedEvent;

import java.time.LocalDateTime;

/** An {@link ExtendedEvent} recording one completed fishing action and what it caught. */
@Value
public class FishingEvent implements ExtendedEvent {

    /** The fishing action taken. */
    ExtendedFishingAction action;
    /** What the action caught, and how it was disposed of. */
    FishingOutcome outcome;

    /** @return {@link #action}'s start time */
    @Override
    public LocalDateTime getStartDateTime() {
        return action.getStartDateTime();
    }

    /** @return {@link #action}'s end time */
    @Override
    public LocalDateTime getEndDateTime() {
        return action.getEndDateTime();
    }
}
