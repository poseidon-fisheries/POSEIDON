/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels.hold;

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;

public interface Hold<C extends Content<C>> {

    /**
     * Adds the specified bucket of content to the current hold and returns the content that could
     * not be stored (usually because lack of space).
     *
     * @param fishToStore the content to add
     * @return the fish that could not be added, if any
     */
    Bucket<C> addContent(Bucket<C> fishToStore);

    /**
     * Retrieves the content held in the container without removing it.
     *
     * @return the current content.
     */
    Bucket<C> getContent();

    /**
     * Removes all content currently held in the container and returns it.
     *
     * @return the removed content. If the container was empty, returns an empty Bucket.
     */
    Bucket<C> removeContent();

    default boolean isEmpty() {
        return getContent().isEmpty();
    }
}
