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
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.core.utils.ObjDoubleToDoubleFunction;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;

import static com.google.common.base.Preconditions.checkArgument;

public interface Bucket {

    static Bucket empty() {
        return EmptyBucket.INSTANCE;
    }

    static BucketBuilder newBuilder() {
        return new AdaptiveBucketBuilder();
    }

    static Bucket of(
        final Species species,
        final double biomassInKg
    ) {
        if (Double.isNaN(biomassInKg) || biomassInKg == 0.0) return Bucket.empty();
        checkArgument(biomassInKg > 0, "biomassInKg must be positive");
        return new SingleSpeciesBiomassBucket(species, biomassInKg);
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
        return newBuilder().add(map).build();
    }

    static Bucket of(
        final double[] biomasses,
        final SpeciesIndex speciesIndex
    ) {
        final BiomassBucket bucket = BiomassBucket.create(biomasses, speciesIndex);
        return bucket.isEmpty() ? Bucket.empty() : bucket;
    }

    Optional<? extends Content> getContent(Species species);

    default double getKg(final Species species) {
        return getContent(species).map(Content::asKg).orElse(0.0);
    }

    default Bucket add(final Bucket other) {
        return toBuilder().add(other).build();
    }

    default Bucket subtract(final Bucket other) {
        return toBuilder().subtract(other).build();
    }

    default Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        return toBuilder().put(species, newContent).build();
    }

    default Bucket mapContent(final BiFunction<Species, Content, Content> mapper) {
        final BucketBuilder bucketBuilder = Bucket.newBuilder();
        forEach((species, content) ->
            bucketBuilder.put(species, mapper.apply(species, content))
        );
        return bucketBuilder.build();
    }

    default Bucket mapBiomassValue(final ObjDoubleToDoubleFunction<Species> mapper) {
        final BucketBuilder bucketBuilder = newBuilder();
        getMap().forEach((species, content) -> {
            final double biomassInKg = mapper.applyAsDouble(species, content.asKg());
            if (!Double.isNaN(biomassInKg)) {
                bucketBuilder.put(species, Biomass.ofKg(biomassInKg));
            }
        });
        return bucketBuilder.build();
    }

    default Map<Boolean, Bucket> partitionBy(
        final BiPredicate<Species, Content> predicate
    ) {
        final BucketBuilder b1 = Bucket.newBuilder();
        final BucketBuilder b2 = Bucket.newBuilder();
        getMap().forEach((species, content) ->
            (predicate.test(species, content) ? b1 : b2).put(species, content)
        );
        return Map.of(true, b1.build(), false, b2.build());
    }

    boolean isEmpty();

    Biomass getTotalBiomass();

    default double getTotalBiomassInKg() {
        return getTotalBiomass().asKg();
    }

    Map<Species, Content> getMap();

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
