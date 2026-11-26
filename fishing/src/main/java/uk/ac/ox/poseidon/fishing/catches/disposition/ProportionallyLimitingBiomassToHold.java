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

import static com.google.common.base.Preconditions.checkArgument;
import static lombok.AccessLevel.PACKAGE;

import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

/**
 * Keeps the catch that fits the free capacity, scaling every species' content
 * by the same factor when the catch is too large.
 */
@NoArgsConstructor(access = PACKAGE)
public class ProportionallyLimitingBiomassToHold implements CatchSorting {

  /**
   * @throws IllegalArgumentException if the free capacity is negative or NaN
   */
  @Override
  public Bucket keep(final Bucket catches, final double freeCapacityInKg) {
    checkArgument(
      !Double.isNaN(freeCapacityInKg) && freeCapacityInKg >= 0,
      "freeCapacityInKg must be a non-negative value, got %s",
      freeCapacityInKg
    );
    final double catchInKg = catches.getTotalBiomassInKg();
    final double proportionToKeep =
      catchInKg <= freeCapacityInKg ? 1 : freeCapacityInKg / catchInKg;
    return catchInKg <= freeCapacityInKg
      ? catches
      : catches.mapContent((species, content) ->
          content.multiply(proportionToKeep)
        );
  }
}
