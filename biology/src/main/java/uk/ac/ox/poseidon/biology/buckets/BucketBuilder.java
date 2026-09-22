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

package uk.ac.ox.poseidon.biology.buckets;

import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;

/**
 * Accumulates per-species {@link Content} into a {@link Bucket}. {@code put} replaces a species'
 * content outright, while {@code add}/{@code subtract} combine with whatever is already there.
 * Obtained from {@link Bucket#newBuilder()} or {@link Bucket#toBuilder()}; builders are mutable
 * and not thread-safe, unlike the buckets they produce.
 */
public interface BucketBuilder {

    /** @return this builder, with every species in {@code bucket} replaced */
    BucketBuilder put(Bucket bucket);

    /** @return this builder, with every species in {@code map} replaced */
    BucketBuilder put(Map<Species, Content> map);

    /** @return this builder, with {@code species}' content replaced by {@code newContent} */
    BucketBuilder put(
        Species species,
        Content newContent
    );

    /** @return this builder, with {@code bucket}'s content added species by species */
    BucketBuilder add(Bucket bucket);

    /** @return this builder, with {@code map}'s content added species by species */
    BucketBuilder add(Map<Species, Content> map);

    /** @return this builder, with {@code content} added to {@code species}' */
    BucketBuilder add(
        Species species,
        Content content
    );

    /** @return this builder, with {@code bucket}'s content removed species by species */
    BucketBuilder subtract(Bucket bucket);

    /** @return this builder, with {@code map}'s content removed species by species */
    BucketBuilder subtract(Map<Species, Content> map);

    /**
     * @return this builder, with {@code content} removed from {@code species}'; a species not
     * already present is left absent rather than going negative
     */
    BucketBuilder subtract(
        Species species,
        Content content
    );

    /**
     * @return a bucket holding the accumulated content, dropping empty entries; the concrete
     * implementation is chosen to suit that content
     */
    Bucket build();
}
