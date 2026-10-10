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

import org.joda.money.Money;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.List;
import java.util.function.Function;

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
     * @param hourlyCost gives the vessel's cost per hour at sea
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a {@link TimeCost}
     * @see TimeCost
     */
    public static TimeCostFactory timeCost(
        final Factory<? super VesselScope, ? extends Function<? super Vessel, ? extends Money>>
            hourlyCost
    ) {
        return new TimeCostFactory(hourlyCost);
    }

    /**
     * @param routeFunction gives the route via a cell
     * @param routeCosts    what is charged for a route, each in money
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link TravelCost}
     * @see TravelCost
     */
    @SafeVarargs
    public static TravelCostFactory travelCost(
        final Factory<? super VesselScope, ? extends Function<? super Int2D, ? extends Route>>
            routeFunction,
        final Factory<? super VesselScope, ? extends Function<? super Route, ? extends Money>>...
            routeCosts
    ) {
        return new TravelCostFactory(routeFunction, List.of(routeCosts));
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
