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

package uk.ac.ox.poseidon.agents.tasks.travel;

import com.badlogic.gdx.ai.btree.Task;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

public class Factories {

    private Factories() {}

    public static RefuelFactory refuel(
        final Factory<? super VesselScope, ? extends FuelStationGrid> fuelStationGrid
    ) {
        return new RefuelFactory(fuelStationGrid);
    }

    public static RoundTripFactory roundTrip(
        final Factory<? super VesselScope, ? extends Task<Vessel>> startTripTask,
        final Factory<? super VesselScope, ? extends Task<Vessel>> travelTask,
        final Factory<? super VesselScope, ? extends Task<Vessel>> fishingTask,
        final Factory<? super VesselScope, ? extends Task<Vessel>> landingTask
    ) {
        return new RoundTripFactory(startTripTask, travelTask, fishingTask, landingTask);
    }

    public static TravelAlongPathFactory travelAlongPath(
        final Factory<? super VesselScope, ? extends PathFinder<Int2D>> pathFinder,
        final Factory<? super VesselScope, ? extends DistanceCalculator> distance
    ) {
        return new TravelAlongPathFactory(pathFinder, distance);
    }

    public static SetDestinationToOriginFactory setDestinationToOrigin() {
        return new SetDestinationToOriginFactory();
    }

    public static EndTripFactory endTrip() {
        return new EndTripFactory();
    }

}
