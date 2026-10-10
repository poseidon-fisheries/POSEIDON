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

package uk.ac.ox.poseidon.agents.travel;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Map.entry;

/**
 * The {@link Route} a vessel would sail to fish at a destination cell: from its current cell to
 * the destination, then back to its home port (from port, the round trip). The vessel's cell, home
 * port and cruising speed are read at each call. The length of the path between two cells is
 * remembered after it is first summed: it only depends on the two cells, and the path finder gives
 * the same path for them throughout a run.
 */
public class RouteViaDestination implements Function<Int2D, Route> {

    private final Vessel vessel;
    private final GridPathFinder pathFinder;
    private final DistanceCalculator distanceCalculator;
    private final Map<Entry<Int2D, Int2D>, Double> pathLengthsInKm = new HashMap<>();

    /**
     * @param vessel             the vessel that would sail the route
     * @param pathFinder         finds the paths of the route's two legs
     * @param distanceCalculator measures the paths
     */
    RouteViaDestination(
        final Vessel vessel,
        final GridPathFinder pathFinder,
        final DistanceCalculator distanceCalculator
    ) {
        this.vessel = checkNotNull(vessel);
        this.pathFinder = checkNotNull(pathFinder);
        this.distanceCalculator = checkNotNull(distanceCalculator);
    }

    /**
     * @param destination the cell to fish at
     * @return the route from the vessel's current cell to {@code destination}, then back to its
     * home port
     * @throws IllegalStateException if either leg has no path
     */
    @Override
    public Route apply(final Int2D destination) {
        final double distanceInKm =
            distanceInKm(vessel.getCell(), destination) +
                distanceInKm(destination, vessel.getHomePortLocation());
        return new Route(
            distanceInKm,
            DistanceCalculator.travelDuration(
                distanceInKm,
                vessel.getEngine().getCruisingSpeedInKph()
            )
        );
    }

    private double distanceInKm(
        final Int2D start,
        final Int2D end
    ) {
        return pathLengthsInKm.computeIfAbsent(entry(start, end), _ ->
            distanceCalculator.distanceInKm(
                pathFinder.getPath(start, end).orElseThrow(() ->
                    new IllegalStateException("No path from " + start + " to " + end)
                )
            )
        );
    }
}
