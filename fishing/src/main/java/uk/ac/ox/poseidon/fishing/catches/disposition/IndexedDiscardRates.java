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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

import lombok.NonNull;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

/**
 * Applies per-species discard rates to retained catch, using a pre-indexed rate
 * array for efficient array-based operations. The factory evaluates the
 * per-species mapping once at construction; later changes to its inputs
 * are not seen.
 */
public class IndexedDiscardRates implements CatchSorting {

  private final @NonNull SpeciesIndexedDoubleArray discardRates;

  /**
   * @param discardRates per-species proportion of retained catch to discard,
   *                     each in {@code [0, 1]}
   */
  public IndexedDiscardRates(
    @NonNull final SpeciesIndexedDoubleArray discardRates
  ) {
    discardRates.forEachValue(rate -> checkUnitRange(rate, "discard rate"));
    this.discardRates = discardRates;
  }

  @Override
  public Bucket keep(final Bucket catches, final double freeCapacityInKg) {
    final Bucket discards = SpeciesSpecificRateBuckets.applyRates(
      catches,
      discardRates
    );
    return catches.subtract(discards);
  }
}
