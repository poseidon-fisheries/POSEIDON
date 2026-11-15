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

package uk.ac.ox.poseidon.fishing.choices;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Function;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;

/**
 * Realised profit per hour for any ended trip, from its final account and
 * time at sea, excluding time docked. An empty account gives zero; multiple
 * currencies or no positive time at sea are rejected.
 */
public class ProfitPerHour implements Function<TripEndEvent, Double> {

  @Override
  public Double apply(final TripEndEvent event) {
    final var trip = event.getTrip();
    final var balances = trip.getAccount().getBalances();
    checkState(
      balances.size() <= 1,
      "Profit per hour requires at most one currency."
    );
    final double hours =
      trip.getTotalTimeAtSea().toNanos() / 3_600_000_000_000.0;
    checkState(hours > 0, "Time at sea must be positive.");
    return (
      balances
        .values()
        .stream()
        .mapToDouble(balance -> balance.getAmount().doubleValue())
        .sum() / hours
    );
  }
}
