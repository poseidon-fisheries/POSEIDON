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

package uk.ac.ox.poseidon.agents.vessels.hold;

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;

public interface Hold<C extends Content<C>> {

    /**
     * Adds the specified bucket of content to the current hold.
     *
     * @param fishToStore the content to add
     */
    void addContent(Bucket<C> fishToStore);

    /**
     * Retrieves the content held in the container without removing it.
     *
     * @return the current content.
     */
    Bucket<C> getContent();

    default boolean isFull() {
        return getAvailableCapacityInKg() <= 0;
    }

    /**
     * Removes all content currently held in the container and returns it.
     *
     * @return the removed content. If the container was empty, returns an empty Bucket.
     */
    Bucket<C> extractContent();

    default boolean isEmpty() {
        return getContent().isEmpty();
    }

    double getTotalCapacityInKg();

    default double getAvailableCapacityInKg() {
        return getTotalCapacityInKg() - getContent().getTotalBiomass().asKg();
    }

}
