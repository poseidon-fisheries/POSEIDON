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

import uk.ac.ox.poseidon.biology.buckets.Bucket;

/**
 * A crew's sorting of a catch into what is kept and what is discarded.
 * Each step must keep only part of its input, at the content's own resolution.
 * Steps can be chained with {@link CompositeCatchSorting}.
 */
@FunctionalInterface
public interface CatchSorting {
  /**
   * @param catches          the catch to sort
   * @param freeCapacityInKg the hold's free capacity in kilograms
   * @return the part of the catch kept for the hold
   */
  Bucket keep(Bucket catches, double freeCapacityInKg);
}
