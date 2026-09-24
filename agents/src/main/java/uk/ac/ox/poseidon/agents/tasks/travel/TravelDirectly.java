/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

import java.time.Duration;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.geography.distance.DistanceCalculator.travelDuration;

/**
 * An {@link ExtendedTripTask} that travels straight (as the crow flies) from the vessel's
 * current cell to its trip's destination, in one step, taking the direct distance's travel
 * duration; broadcasts a {@link TravelEvent} on arrival.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class TravelDirectly extends ExtendedTripTask {

    private final DistanceCalculator distanceCalculator;
    private Int2D origin;
    private Int2D destination;
    private double distanceInKm;

    /** Resolves the origin, the trip's destination, and the direct distance between them. */
    @Override
    public void start() {
        super.start();
        origin = getAgent().getCell();
        destination = getAgent().getCurrentTrip().getDestination();
        distanceInKm = distanceCalculator.distanceInKm(origin, destination);
    }

    /** @return the travel duration for {@link #distanceInKm} at the vessel's cruising speed */
    @Override
    protected Duration getDuration() {
        return travelDuration(
            distanceInKm,
            getAgent().getEngine().getCruisingSpeedInKph()
        );
    }

    /**
     * Consumes the fuel for {@link #distanceInKm}, moves the vessel to the destination, and
     * broadcasts a {@link TravelEvent}.
     *
     * @return always {@code SUCCEEDED}
     */
    @Override
    protected Status complete() {
        getAgent().getEngine().consumeFuelForDistance(distanceInKm);
        getAgent().setCurrentCell(getAgent().getCurrentTrip().getDestination());
        getTrip().getEventManager().broadcast(new TravelEvent(
            getAgent(),
            getStartDateTime(),
            getAgent().getSchedule().getDateTime(),
            origin,
            destination
        ));
        return SUCCEEDED;
    }

}
