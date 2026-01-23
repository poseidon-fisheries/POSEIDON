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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;
import java.util.function.UnaryOperator;

public interface Bucket {

    static Bucket empty() {
        return EmptyBucket.INSTANCE;
    }

    static BucketBuilder newBuilder() {
        return new AdaptiveBucketBuilder();
    }

    static Bucket of(
        final Species species,
        final Content content
    ) {
        // we call `of` instead of the constructor to filter empty content
        return of(ImmutableMap.of(species, content));
    }

    static Bucket of(
        final Map<Species, Content> map
    ) {
        // we use a builder instead of the constructor to filter empty content
        // and potentially add together multiple entries for the same species
        return newBuilder().add(ImmutableMap.copyOf(map)).build();
    }

    Optional<Content> getContent(Species species);

    default double getKg(final Species species) {
        return getContent(species).map(Content::asKg).orElse(0.0);
    }

    Bucket add(Bucket other);

    Bucket subtract(Bucket other);

    Bucket replaceContent(
        Species species,
        Content newContent
    );

    Bucket mapContent(UnaryOperator<Content> mapper);

    Map<Boolean, Bucket> partitionBy(
        BiPredicate<Species, Content> predicate
    );

    boolean isEmpty();

    Biomass getTotalBiomass();

    ImmutableMap<Species, Content> getMap();

    default Set<Species> getSpecies() {
        return getMap().keySet();
    }

    default BucketBuilder toBuilder() {
        return newBuilder().put(this);
    }

    default void forEach(final BiConsumer<Species, Content> action) {
        getMap().forEach(action);
    }

    default void forEachBiomassValue(final ObjDoubleConsumer<Species> action) {
        forEach((species, content) -> action.accept(species, content.asKg()));
    }

}
