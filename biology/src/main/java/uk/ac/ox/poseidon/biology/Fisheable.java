/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology;

import uk.ac.ox.poseidon.biology.buckets.Bucket;

/** Something that fish can be caught from and released back to, at a single grid cell. */
public interface Fisheable {

    /** @return the fish currently available to be caught */
    Bucket availableFish();

    /** @param fishToRelease fish to add back, e.g. live discards */
    void release(Bucket fishToRelease);

    /**
     * Mutates the fisheable by removing the content of the provided bucket and returns another
     * bucket containing the fish that were actually removed. The returned bucket may contain less
     * than requested if the fisheable does not have enough available.
     *
     * @param bucket the fish to remove from the fisheable
     * @return the fish that were removed from the fisheable
     */
    Bucket extract(Bucket bucket);

}
