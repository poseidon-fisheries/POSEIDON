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

package uk.ac.ox.poseidon.fishing.vessels.values;

import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;

/**
 * Whether a vessel's hold is filled to a proportion of its capacity, read on
 * every call: the proportion is a live parameter of the vessel.
 */
@RequiredArgsConstructor
public class VesselHoldIsFilledTo implements Function<Vessel, Boolean> {

  @NonNull
  private final VesselComponent<? extends Hold> hold;

  @NonNull
  private final Function<? super Vessel, Double> proportion;

  /**
   * @throws IllegalStateException    if the vessel has no such hold
   * @throws IllegalArgumentException if the proportion is not in (0, 1]
   * @see Hold#isFilledTo(double)
   */
  @Override
  public Boolean apply(final Vessel vessel) {
    return hold.of(vessel).isFilledTo(proportion.apply(vessel));
  }
}
