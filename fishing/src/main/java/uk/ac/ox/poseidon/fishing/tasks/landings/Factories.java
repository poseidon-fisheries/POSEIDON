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

package uk.ac.ox.poseidon.fishing.tasks.landings;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import java.time.Duration;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;

/**
 * Factories for tasks that land a vessel's catch at market.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @param hold     the hold whose catch is landed
   * @param duration how long landing takes, read for the vessel
   * @return a {@link uk.ac.ox.poseidon.fishing.tasks.TaskFactory} for a
   *         {@link LandCatches}
   * @see LandCatchesFactory
   */
  public static LandCatchesFactory landCatches(
    final VesselComponent<? extends Hold> hold,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Duration
    > duration
  ) {
    return new LandCatchesFactory(hold, duration, currentDateTime());
  }

  /**
   * @param hold     the hold whose catch is landed
   * @param duration how long landing takes
   * @return the same as {@link #landCatches(VesselComponent, ValueFactory)},
   *         over a constant {@code duration}
   */
  public static LandCatchesFactory landCatches(
    final VesselComponent<? extends Hold> hold,
    final Factory<? super VesselComponentScope, ? extends Duration> duration
  ) {
    return landCatches(hold, constant(duration));
  }
}
