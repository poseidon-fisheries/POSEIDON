/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 * The InfiniteHold class is an implementation of the Hold interface designed to store an unlimited
 * amount of content of a specific type. It uses a Bucket to manage its contents and does not impose
 * any constraints on the amount of content it can hold.
 *
 * @param <C> the type of content the hold can store, which must extend the Content interface.
 */
public class InfiniteHold<C extends Content<C>> implements Hold<C> {

    private Bucket<C> content = Bucket.empty();

    @Override
    public Bucket<C> addContent(final Bucket<C> fishToStore) {
        content = content.add(fishToStore);
        return Bucket.empty();
    }

    @Override
    public Bucket<C> getContent() {
        return content;
    }

    /**
     * An infinite hold is never full (though there might be technical limits depending on content
     * type, e.g. Double.MAX_VALUE).
     *
     * @return false
     */
    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public Bucket<C> removeContent() {
        final Bucket<C> result = content;
        content = Bucket.empty();
        return result;
    }
}
