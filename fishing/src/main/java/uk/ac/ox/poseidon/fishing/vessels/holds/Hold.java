/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.vessels.holds;

import static com.google.common.base.Preconditions.checkArgument;

import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;

/** A vessel's storage for caught biomass, with a fixed total capacity. */
public interface Hold {
  /**
   * Adds the specified bucket of content to the current hold.
   *
   * @param categorisedCatch the content to add
   */
  void addContent(CategorisedCatch categorisedCatch);

  /**
   * Adds the specified bucket of content to the current hold.
   *
   * @param uncategorisedCatch the content to add
   */
  void addContent(Bucket uncategorisedCatch);

  /**
   * Retrieves the content held in the container without removing it.
   *
   * @return the current content.
   */
  CategorisedCatch getContent();

  /**
   * The relative margin of the requested biomass threshold within which a hold
   * counts as filled to a proportion: a set fitted to the free capacity by
   * scaling each species separately can add up to a few ulps either side of it.
   * The same margin allows those rounding errors when adding content.
   */
  double FULLNESS_TOLERANCE = 1e-9;

  /**
   * @param proportion the proportion of the capacity, in (0, 1]
   * @return {@code true} if the content has reached {@code proportion} of
   *         {@link #getTotalCapacityInKg()}, within {@link #FULLNESS_TOLERANCE}
   *         of that biomass threshold; content over capacity has reached
   *         any proportion, and a hold of infinite capacity none
   * @throws IllegalArgumentException if {@code proportion} is not in (0, 1]
   */
  default boolean isFilledTo(final double proportion) {
    checkArgument(
      proportion > 0 && proportion <= 1,
      "proportion must be in (0, 1], got %s",
      proportion
    );
    return (
      getContent().getTotalBiomassInKg() >=
      proportion * getTotalCapacityInKg() * (1 - FULLNESS_TOLERANCE)
    );
  }

  /**
   * @return {@code true} if the hold is filled to its capacity
   * @see #isFilledTo(double)
   */
  default boolean isFull() {
    return isFilledTo(1);
  }

  /**
   * Removes all content currently held in the container and returns it.
   *
   * @return the removed content. If the container was empty, returns an empty
   *         {@link CategorisedCatch}.
   */
  CategorisedCatch extractContent();

  /** @return {@code true} if the current content is empty */
  default boolean isEmpty() {
    return getContent().isEmpty();
  }

  /** @return this hold's fixed total capacity, in kilograms */
  double getTotalCapacityInKg();

  /**
   * @return {@link #getTotalCapacityInKg()} minus the current content's total
   *         biomass, clamped to zero when rounding puts content just over
   *         capacity
   */
  default double getAvailableCapacityInKg() {
    return Math.max(
      0,
      getTotalCapacityInKg() - getContent().getTotalBiomassInKg()
    );
  }
}
