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

import java.util.function.BiFunction;
import java.util.function.Function;
import org.joda.money.Money;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * The cost of the time a vessel would spend travelling a {@link Route}: its
 * hourly cost times the route's hours. The hourly cost is read at each call,
 * for the vessel given. The cost is computed in floating point and rounded
 * half-even to the currency's minor unit, which is much faster than exact
 * decimal arithmetic.
 */
public class TimeCost implements BiFunction<Vessel, Route, Money> {

  private final Function<? super Vessel, ? extends Money> hourlyCost;

  /**
   * @param hourlyCost gives a vessel's cost per hour at sea
   */
  TimeCost(final Function<? super Vessel, ? extends Money> hourlyCost) {
    this.hourlyCost = checkNotNull(hourlyCost);
  }

  /**
   * @param vessel the vessel that would travel the route
   * @param route  the route the vessel would travel
   * @return the vessel's hourly cost times the hours {@code route} takes to
   *         travel, rounded half-even to the currency's minor unit
   * @throws NullPointerException if {@code hourlyCost} gives no cost for the
   *                              vessel
   */
  @Override
  public Money apply(final Vessel vessel, final Route route) {
    final Money costPerHour = checkNotNull(
      hourlyCost.apply(vessel),
      "Unable to extract hourly cost for Vessel %s.",
      vessel
    );
    final double hours = route.getDuration().toSeconds() / 3600.0;
    return Money.ofMinor(
      costPerHour.getCurrencyUnit(),
      (long) Math.rint(costPerHour.getAmountMinorLong() * hours)
    );
  }
}
