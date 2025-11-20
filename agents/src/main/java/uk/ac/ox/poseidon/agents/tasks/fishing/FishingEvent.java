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

import lombok.AllArgsConstructor;
import lombok.Value;
import uk.ac.ox.poseidon.agents.catches.disposition.Disposition;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.time.LocalDateTime;

@Value
@AllArgsConstructor
public class FishingEvent {

    LocalDateTime dateTime;
    Vessel vessel;
    Gear gear;
    Bucket grossCatch;
    Disposition disposition;
    Coordinate coordinate;

    public FishingEvent(
        final Vessel vessel,
        final Bucket grossCatch,
        final Disposition disposition
    ) {
        this(
            vessel.getSchedule().getDateTime(),
            vessel,
            vessel.getGear(),
            grossCatch,
            disposition,
            vessel.getVesselField().getModelGrid().toCoordinate(vessel.getCell())
        );
    }
}
