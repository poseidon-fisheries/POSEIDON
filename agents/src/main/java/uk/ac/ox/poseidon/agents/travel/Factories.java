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

import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/** Factories for the routes vessels would sail and what travelling them costs. */
public class Factories {

    private Factories() {}

    /**
     * @param pathFinder finds the paths of the route's two legs
     * @param distance   measures the paths
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link RouteViaDestination}
     * @see RouteViaDestination
     */
    public static RouteViaDestinationFactory routeViaDestination(
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
        final Factory<? super VesselScope, ? extends DistanceCalculator> distance
    ) {
        return new RouteViaDestinationFactory(pathFinder, distance);
    }

    /**
     * @param propertyPath the dotted path of properties to follow from the route, e.g.
     *                     {@code "duration"} for how long sailing it takes
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an
     * {@link uk.ac.ox.poseidon.core.functions.ObjectProperty} of a route
     * @see RoutePropertyFactory
     */
    public static <R> RoutePropertyFactory<R> routeProperty(final String propertyPath) {
        return new RoutePropertyFactory<>(propertyPath);
    }
}
