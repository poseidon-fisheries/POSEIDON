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

/**
 * A container implementation where all content added to it is essentially discarded. This
 * represents a "void" behavior, simulating a hold that does not store any content.
 *
 * @param <C> the type of content that this hold can handle, constrained by {@link Content}.
 */
public class VoidHold<C extends Content<C>> implements Hold<C> {
    /**
     * Adds the specified bucket of content to this void hold. Since this is a void hold, the
     * provided content is discarded, and no actual storage occurs.
     *
     * @param fishToStore the content to attempt to add
     * @return an empty bucket, indicating that the content has effectively been discarded
     */
    @Override
    public Bucket<C> addContent(final Bucket<C> fishToStore) {
        // Nothing happens here: the fish vanishes into the void.
        // We return an empty bucket as if everything had been stored.
        return Bucket.empty();
    }

    /**
     * Retrieves the current content held in the void hold. As this is a void hold, it does not
     * store any content and will always return an empty bucket.
     *
     * @return an empty bucket representing the absence of any stored content.
     */
    @Override
    public Bucket<C> getContent() {
        return Bucket.empty();
    }

    /**
     * A void hold is never full. There is always more room in the void.
     *
     * @return false
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * Removes all content from the hold and returns it. In the context of a {@code VoidHold}, no
     * content is actually stored, so this method will always return an empty bucket.
     *
     * @return an empty bucket, representing the absence of any stored content.
     */
    @Override
    public Bucket<C> extractContent() {
        return Bucket.empty();
    }
}
