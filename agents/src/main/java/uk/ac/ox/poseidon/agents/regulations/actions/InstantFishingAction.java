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

package uk.ac.ox.poseidon.agents.regulations.actions;

import lombok.NonNull;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.regulations.ExtendedAction;

import java.time.LocalDateTime;

public class InstantFishingAction extends ExtendedAction<Vessel>
    implements TemporalFishingAction, SpatialFishingAction {

    @NonNull private final Gear gear;

    public InstantFishingAction(
        @NonNull final Vessel vessel,
        @NonNull final LocalDateTime dateTime,
        @NonNull final Coordinate coordinate,
        @NonNull final Gear gear
    ) {
        super(vessel, dateTime, dateTime, coordinate);
        this.gear = gear;
    }

    public InstantFishingAction(final Vessel vessel) {
        this(
            vessel,
            vessel.getSchedule().getDateTime(),
            vessel.getVesselField().getModelGrid().toCoordinate(vessel.getCell()),
            vessel.getGear()
        );
    }
}
