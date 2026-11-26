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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;

/** Applies one discard mortality rate to every species, independently of haul. */
public final class UniformDiscardMortality implements DiscardMortality {

  private final double mortalityRate;

  /** @param mortalityRate the fraction of discards that dies, in [0, 1] */
  public UniformDiscardMortality(final double mortalityRate) {
    this.mortalityRate = checkUnitRange(mortalityRate, "mortality rate");
  }

  @Override
  public Bucket deaths(
    final Bucket discards,
    final ExtendedFishingAction action
  ) {
    return discards.mapContent((species, content) ->
      content.multiply(mortalityRate)
    );
  }
}
