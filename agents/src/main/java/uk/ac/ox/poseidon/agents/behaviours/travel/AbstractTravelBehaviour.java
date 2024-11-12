/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.travel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.Distance;

import java.time.Duration;

@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public abstract class TravelBehaviour implements Behaviour {
    private static final long MINUTES_PER_HOUR = 60;
    protected final Distance distance;
    protected final Behaviour behaviourOnArrival;
    @Getter
    @Setter
    protected Int2D currentDestination;

    protected Duration travelDuration(
        final Vessel vessel,
        final Int2D destination
    ) {
        return Duration.ofMinutes(
            Math.round(MINUTES_PER_HOUR *
                distance.distanceBetween(
                    vessel.getCurrentPoint(),
                    vessel.getVesselField().getGridExtent().toPoint(destination)
                ) / vessel.getCruisingSpeed()
            ));
    }
}
