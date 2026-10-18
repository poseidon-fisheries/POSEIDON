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

import com.google.common.collect.ImmutableList;
import org.joda.money.Money;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.functions.ValueAsMappingFactory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * Factories for the routes vessels would sail and what travelling them costs.
 */
public class Factories {

  private Factories() {}

  /**
   * @param engine     the engine whose speed gives the route's duration
   * @param pathFinder finds the paths of the route's two legs
   * @param distance   measures the paths
   * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
   *         {@link RouteViaDestination}
   * @see RouteViaDestination
   */
  public static RouteViaDestinationFactory routeViaDestination(
    final VesselComponent<? extends Engine> engine,
    final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
    final Factory<? super VesselScope, ? extends DistanceCalculator> distance
  ) {
    return new RouteViaDestinationFactory(engine, pathFinder, distance);
  }

  /**
   * @param hourlyCost gives the vessel's cost per hour at sea
   * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
   *         {@link TimeCost}
   * @see TimeCost
   */
  public static TimeCostFactory timeCost(
    final ValueFactory<
      ? super VesselScope,
      ? super Vessel,
      ? extends Money
    > hourlyCost
  ) {
    return new TimeCostFactory(hourlyCost);
  }

  /**
   * @param routeFunction gives the route a vessel would sail via a cell
   * @param routeCosts    what is charged to a vessel for a route, each in
   *                      money
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TravelCost}
   * @see TravelCost
   */
  @SafeVarargs
  public static <S extends Scope> TravelCostFactory<S> travelCost(
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super Int2D,
      ? extends Route
    > routeFunction,
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super Route,
      ? extends Money
    >... routeCosts
  ) {
    return new TravelCostFactory<>(
      routeFunction,
      ImmutableList.copyOf(routeCosts)
    );
  }

  /**
   * @param route        the mapping that reads the route from the thing mapped
   * @param propertyPath the dotted path of properties to follow from the route,
   *                     e.g. {@code "duration"} for how long sailing it takes
   * @return a {@link MappingFactory} for a {@link RouteProperty} of the route
   * @see RouteProperty
   */
  public static <S extends Scope, C, T, R> RoutePropertyFactory<
    S,
    C,
    T,
    R
  > routeProperty(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends Route
    > route,
    final String propertyPath
  ) {
    return new RoutePropertyFactory<S, C, T, R>(route, propertyPath);
  }

  /**
   * @param route        the value that is the route
   * @param propertyPath the dotted path of properties to follow from the route,
   *                     e.g. {@code "duration"} for how long sailing it takes
   * @return a {@link ValueFactory} for a {@link RouteProperty} of the route,
   *         made a value by {@link uk.ac.ox.poseidon.core.functions.MappingAsValue}
   * @see RouteProperty
   */
  public static <S extends Scope, C, R> MappingAsValueFactory<
    S,
    C,
    R
  > routeProperty(
    final ValueFactory<? super S, ? super C, ? extends Route> route,
    final String propertyPath
  ) {
    return new MappingAsValueFactory<S, C, R>(
      new RoutePropertyFactory<S, C, Void, R>(
        new ValueAsMappingFactory<S, C, Route>(route),
        propertyPath
      )
    );
  }
}
