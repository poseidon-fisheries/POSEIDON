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

package uk.ac.ox.poseidon.agents.tasks.landings;

import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import java.time.Duration;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for tasks that land a vessel's catch at market.
 */
public class Factories {

  private Factories() {}

  /**
   * @param hold     the hold whose catch is landed
   * @param duration how long landing takes, read for the vessel
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for a
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
}
