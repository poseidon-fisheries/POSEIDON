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

package uk.ac.ox.poseidon.agents.vessels.providers;

import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.function.Supplier;

/** Factories for values and services derived from a vessel's current state. */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link VesselScope}-relative factory for a {@link CurrentCell}
     * @see CurrentCell
     */
    public static CurrentCellFactory currentCell() {
        return new CurrentCellFactory();
    }

    /**
     * @param pathFinder finds which cells are reachable, and by what path
     * @return a {@link VesselScope}-relative factory for an {@link AccessibleWaterCells}
     * @see AccessibleWaterCells
     */
    public static AccessibleWaterCellsFactory accessibleWaterCells(
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder
    ) {
        return new AccessibleWaterCellsFactory(pathFinder);
    }

    /**
     * @return a {@link VesselScope}-relative factory for a {@link HomePortCell}
     * @see HomePortCell
     */
    public static HomePortCellFactory homePortCell() {
        return new HomePortCellFactory();
    }

    /**
     * @return a {@link VesselScope}-relative factory for a {@link CurrentTripDestinationCell}
     * @see CurrentTripDestinationCell
     */
    public static CurrentTripDestinationCellFactory currentTripDestinationCell() {
        return new CurrentTripDestinationCellFactory();
    }

    /**
     * @return a {@link VesselScope}-relative factory for a {@link CurrentTripDuration}
     * @see CurrentTripDuration
     */
    public static CurrentTripDurationFactory currentTripDuration() {
        return new CurrentTripDurationFactory();
    }

    /**
     * @return a {@link VesselScope}-relative factory for a {@link VesselEventManager}
     * @see VesselEventManager
     */
    public static VesselEventManagerFactory vesselEventManager() {
        return new VesselEventManagerFactory();
    }

    /**
     * @return a {@link VesselScope}-relative factory for a {@link CurrentTripEventManager}
     * @see CurrentTripEventManager
     */
    public static CurrentTripEventManagerFactory currentTripEventManager() {
        return new CurrentTripEventManagerFactory();
    }

    /**
     * @param pathFinder finds a path between two cells
     * @param distance   computes the travel duration of a path at a given speed
     * @return a {@link VesselScope}-relative factory for a
     * {@link TravelTimeToPortViaDestination}
     * @see TravelTimeToPortViaDestination
     */
    public static TravelTimeToPortViaDestinationFactory travelTimeToPortViaDestination(
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
        final Factory<? super VesselScope, ? extends DistanceCalculator> distance
    ) {
        return new TravelTimeToPortViaDestinationFactory(pathFinder, distance);
    }
}
