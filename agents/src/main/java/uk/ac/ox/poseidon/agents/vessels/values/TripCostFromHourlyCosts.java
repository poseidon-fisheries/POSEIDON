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

package uk.ac.ox.poseidon.agents.vessels.values;

import java.math.RoundingMode;
import java.time.Duration;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Extracts the cost of a vessel's current trip so far, as its time at sea
 * ({@link Trip#getTimeAtSea()}) times an hourly rate from
 * {@code hourlyCosts}.
 */
@RequiredArgsConstructor
public class TripCostFromHourlyCosts implements Function<Vessel, Money> {

  @NonNull
  private final Function<? super Vessel, ? extends Money> hourlyCosts;

  /**
   * @return {@code hourlyCosts}'s rate for {@code vessel}, times its
   *         current trip's hours at sea
   */
  @Override
  public Money apply(final Vessel vessel) {
    final Duration timeAtSea = vessel.getCurrentTrip().getTimeAtSea();
    return hourlyCosts
      .apply(vessel)
      .multipliedBy(timeAtSea.toSeconds() / 3600.0, RoundingMode.HALF_EVEN);
  }
}
