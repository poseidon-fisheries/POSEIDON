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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BiFunction;
import org.joda.money.Money;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.Valuation;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Values the cost of a vessel travelling to fish at a cell: computes the
 * {@link Route} via the cell once, then sums what each route cost charges the
 * vessel for it. The
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
    BiFunction<? super Vessel, ? super Route, ? extends Money>
  > routeCosts;

  /**
   * @param routeFunction gives the route a vessel would sail via a cell
   * @param routeCosts    what is charged to a vessel for a route, each in
   *                      money
   */
  TravelCost(
    final BiFunction<
      ? super Vessel,
      ? super Int2D,
      ? extends Route
    > routeFunction,
    final List<
      ? extends BiFunction<? super Vessel, ? super Route, ? extends Money>
    > routeCosts
  ) {
    this.routeFunction = checkNotNull(routeFunction);
    this.routeCosts = ImmutableList.copyOf(routeCosts);
  }

  /**
   * @param vessel       the vessel that would travel
   * @param cell         the cell to fish at
   * @param recollection ignored
   * @return the sum of the route costs of the route via {@code cell}, as an
   *         amount in their currency; zero without route costs
   * @throws org.joda.money.CurrencyMismatchException if the route costs are in
   *                                                  different currencies
   */
  @Override
  public double valueOf(
    final Vessel vessel,
    final Int2D cell,
    final Object recollection
  ) {
    final Route route = routeFunction.apply(vessel, cell);
    return routeCosts
      .stream()
      .<Money>map(routeCost -> routeCost.apply(vessel, route))
      .reduce(Money::plus)
      .map(money -> money.getAmount().doubleValue())
      .orElse(0.0);
  }
}
