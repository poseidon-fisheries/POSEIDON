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

package uk.ac.ox.poseidon.fishing.vessels.engines;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;

/** Factories for a vessel's {@link Engine}. */
public final class Factories {

  private Factories() {}

  /**
   * @param cruisingSpeed     this engine's cruising speed
   * @param fuelConsumedPerKm this engine's fuel consumption rate, per kilometre
   *                          travelled
   * @return a {@link VesselScope}-relative factory for a {@link SimpleEngine}
   * @see SimpleEngine
   */
  public static SimpleEngineFactory<VesselScope> simpleEngine(
    final Factory<? super VesselScope, ? extends Quantity<Speed>> cruisingSpeed,
    final Factory<
      ? super VesselScope,
      ? extends Quantity<Volume>
    > fuelConsumedPerKm
  ) {
    return new SimpleEngineFactory<>(cruisingSpeed, fuelConsumedPerKm);
  }
}
