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

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.fishing.choices.Cost;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * The cost of the time a vessel would spend travelling to take an option: its
 * hourly cost times the hours of the option's travel time. The hourly cost is
 * read when the vessel is given, and serves every option costed for it then,
 * for one view of its option values. The cost is computed in floating point
 * and rounded half-even to the currency's minor unit, which is much faster than
 * exact decimal arithmetic. The returned function must not be retained; model
 * state must not change during its visit.
 *
 * @param <T> the type of option costed
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TimeCost<T> implements Cost<T> {

  private final @NonNull BiFunction<
    ? super Vessel,
    ? super T,
    ? extends Duration
  > travelTime;
  private final @NonNull Function<? super Vessel, ? extends Money> hourlyCost;

  /**
   * @param vessel the vessel that would travel
   * @return the vessel's current hourly cost times the hours it would travel to
   *         take an option, rounded half-even to the currency's minor unit
   * @throws NullPointerException if {@code hourlyCost} gives no cost for the
   *                              vessel
   */
  @Override
  public ToDoubleFunction<T> forVessel(final Vessel vessel) {
    final Money costPerHour = checkNotNull(
      hourlyCost.apply(vessel),
      "Unable to extract hourly cost for Vessel %s.",
      vessel
    );
    final CurrencyUnit currencyUnit = costPerHour.getCurrencyUnit();
    final long minorUnitsPerHour = costPerHour.getAmountMinorLong();
    return option -> {
      final double hours =
        travelTime.apply(vessel, option).toSeconds() / 3600.0;
      return Money.ofMinor(
        currencyUnit,
        (long) Math.rint(minorUnitsPerHour * hours)
      )
        .getAmount()
        .doubleValue();
    };
  }
}
