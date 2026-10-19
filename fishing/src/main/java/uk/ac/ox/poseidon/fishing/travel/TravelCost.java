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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.joda.money.Money;
import sim.util.Int2D;
import uk.ac.ox.poseidon.fishing.choices.ContextualisedValuation;
import uk.ac.ox.poseidon.fishing.choices.Valuation;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Values the cost of a vessel travelling to fish at a cell: computes the
 * {@link Route} via the cell once, then sums what each route cost charges the
 * vessel for it. Each route cost is given the vessel once, when the valuation
 * is put in its context, and then costs every route valued for it. The
 * recollection the cell is valued for is ignored, so that this can be combined
 * with other valuations of the same shape. Route costs in different currencies
 * cannot be summed.
 */
public class TravelCost implements Valuation<Int2D, Object> {

  private final BiFunction<
    ? super Vessel,
    ? super Int2D,
    ? extends Route
  > routeFunction;
  private final List<
    Function<? super Vessel, ? extends Function<? super Route, ? extends Money>>
  > routeCosts;

  /**
   * @param routeFunction gives the route a vessel would travel via a cell
   * @param routeCosts    each gives what is charged to a vessel for a route,
   *                      in money
   */
  TravelCost(
    final BiFunction<
      ? super Vessel,
      ? super Int2D,
      ? extends Route
    > routeFunction,
    final List<
      ? extends Function<
        ? super Vessel,
        ? extends Function<? super Route, ? extends Money>
      >
    > routeCosts
  ) {
    this.routeFunction = checkNotNull(routeFunction);
    this.routeCosts = ImmutableList.copyOf(routeCosts);
  }

  /**
   * @param vessel the vessel that would travel
   * @return the sum of the route costs of the route via a cell, as an amount
   *         in their currency; zero without route costs. It throws a
   *         {@link org.joda.money.CurrencyMismatchException} if the route costs
   *         are in different currencies.
   */
  @Override
  public ContextualisedValuation<Int2D, Object> inContextOf(
    final Vessel vessel
  ) {
    final List<Function<? super Route, ? extends Money>> vesselRouteCosts =
      new ArrayList<>(routeCosts.size());
    for (final var routeCost : routeCosts) {
      vesselRouteCosts.add(routeCost.apply(vessel));
    }
    return (cell, recollection) -> {
      final Route route = routeFunction.apply(vessel, cell);
      Money sum = null;
      for (final var routeCost : vesselRouteCosts) {
        final Money cost = routeCost.apply(route);
        sum = sum == null ? cost : sum.plus(cost);
      }
      return sum == null ? 0.0 : sum.getAmount().doubleValue();
    };
  }
}
