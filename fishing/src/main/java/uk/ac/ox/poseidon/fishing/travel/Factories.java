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

package uk.ac.ox.poseidon.fishing.travel;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.functions.ValueAsMappingFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;

/**
 * Factories for the routes vessels would travel and what travelling them
 * costs.
 */
@SuppressWarnings({ "overloads", "varargs" })
public final class Factories {

  private Factories() {}

  /**
   * @param engine               the engine whose speed gives the route's
   *                             duration
   * @param pathLengthCalculator measures the route's two legs
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link RouteViaDestination}
   * @see RouteViaDestination
   */
  public static <S extends Scope> RouteViaDestinationFactory<S> routeViaDestination(
    final VesselComponent<? extends Engine> engine,
    final Factory<
      ? super S,
      ? extends PathLengthCalculator
    > pathLengthCalculator
  ) {
    return new RouteViaDestinationFactory<>(engine, pathLengthCalculator);
  }

  /**
   * @param hourlyCost gives the vessel's cost per hour at sea
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TimeCost}
   * @see TimeCost
   */
  public static <S extends Scope> TimeCostFactory<S> timeCost(
    final ValueFactory<? super S, ? super Vessel, ? extends Money> hourlyCost
  ) {
    return new TimeCostFactory<>(hourlyCost);
  }

  /**
   * @param hourlyCost the cost of an hour, the same for every vessel
   * @return the same as {@link #timeCost(ValueFactory)}, over a constant
   *         {@code hourlyCost}
   */
  public static <S extends Scope> TimeCostFactory<S> timeCost(
    final Factory<? super S, ? extends Money> hourlyCost
  ) {
    return timeCost(constant(hourlyCost));
  }

  /**
   * @param routeFunction gives the route a vessel would travel via a cell
   * @param routeCosts    each gives what is charged to a vessel for a route,
   *                      in money
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TravelCost}
   * @see TravelCost
   */
  @SafeVarargs
  public static <S extends Scope> TravelCostFactory<S> travelCost(
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super Cell,
      ? extends Route
    > routeFunction,
    final ValueFactory<
      ? super S,
      ? super Vessel,
      ? extends Function<? super Route, ? extends Money>
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
   *                     e.g. {@code "duration"} for how long travelling it
   *                     takes
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
   *                     e.g. {@code "duration"} for how long travelling it
   *                     takes
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
