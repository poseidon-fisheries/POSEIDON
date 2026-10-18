/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Getter;
import uk.ac.ox.poseidon.fishing.vessels.Trip;

/**
 * An {@link ExtendedTask} run as part of the vessel's current {@link Trip},
 * capturing that trip when the task starts.
 */
@Getter
public abstract class ExtendedTripTask extends ExtendedTask {

  private Trip trip;

  /** Captures the vessel's current trip (must not be {@code null}). */
  @Override
  public void start() {
    trip = checkNotNull(getVessel().getCurrentTrip());
  }
}
