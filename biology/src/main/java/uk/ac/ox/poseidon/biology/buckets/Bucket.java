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
import uk.ac.ox.poseidon.biology.species.SpeciesIndexed;
import uk.ac.ox.poseidon.core.functions.DoubleIntToDoubleFunction;
import uk.ac.ox.poseidon.core.utils.DoubleIntConsumer;
import uk.ac.ox.poseidon.core.utils.ObjDoubleToDoubleFunction;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An immutable, per-species quantity of {@link Content} — the unit in which fish are caught,
 * carried, landed and sold throughout the model. Several implementations exist, each specialized
 * for a different shape of content ({@link EmptyBucket}, {@link SingleSpeciesBiomassBucket},
 * {@code BiomassBucket}, {@code ContentBucket}); which one you get is an implementation detail
 * chosen by {@link #newBuilder()} based on the content, so always program against this interface.
 * Every operation returns a new bucket rather than mutating this one, and species whose content
 * becomes empty are dropped, so a bucket never carries a zero-valued species in its
 * {@link #getMap()}.
 *
 * <p>Two buckets are {@code equals} exactly when their {@link #getMap()}s are equal — which
 * implementation produced them, any underlying species-index width, and whether an absent species
 * is stored as a zero slot or not present at all are pure representation and don't affect equality
 * or {@code hashCode}. Content equality bottoms out in exact {@code double} comparison (see
 * {@link Biomass#equals}), so two buckets that are conceptually "the same" may still compare
 * unequal if they were built through different arithmetic paths and accumulated floating-point
 * error differently.
 */
public interface Bucket {

    /** @return the shared empty bucket */
    static Bucket empty() {
        return EmptyBucket.INSTANCE;
    }

    /**
     * The {@code equals} implementation shared by every {@link Bucket}: two buckets are equal iff
     * their {@link #getMap()}s are equal, regardless of implementation. Implementations should
     * define {@code equals} as {@code return Bucket.contentEquals(this, obj);}
     *
     * @param bucket the bucket being compared
     * @param other  the object to compare it against
     * @return whether {@code other} is a {@link Bucket} holding the same content as {@code bucket}
     */
    static boolean contentEquals(
        final Bucket bucket,
        final Object other
    ) {
        if (bucket == other) return true;
        if (!(other instanceof final Bucket otherBucket)) return false;
        return bucket.getMap().equals(otherBucket.getMap());
    }

    /**
     * The {@code hashCode} implementation shared by every {@link Bucket}, consistent with
     * {@link #contentEquals}. Implementations should define {@code hashCode} as
     * {@code return Bucket.contentHashCode(this);}
     *
     * @param bucket the bucket to hash
     * @return {@code bucket.getMap().hashCode()}
     */
    static int contentHashCode(final Bucket bucket) {
        return bucket.getMap().hashCode();
    }

    /** @return a new builder, which picks the best-suited implementation at {@code build()} time */
    static BucketBuilder newBuilder() {
        return new AdaptiveBucketBuilder();
    }

    /**
     * @param species     the species the biomass belongs to
     * @param biomassInKg the amount of biomass, in kilograms
     * @return a bucket holding {@code biomassInKg} of {@code species}, or {@link #empty()} if that
     * amount is zero or {@link Double#NaN}
     * @throws IllegalArgumentException if {@code biomassInKg} is negative
     */
    static Bucket of(
        final Species species,
        final double biomassInKg
    ) {
        if (Double.isNaN(biomassInKg) || biomassInKg == 0.0) return Bucket.empty();
        checkArgument(biomassInKg > 0, "biomassInKg must be positive");
        return new SingleSpeciesBiomassBucket(species, biomassInKg);
    }

    /**
     * @param species the species the content belongs to
     * @param content the content
     * @return a bucket holding {@code content} for {@code species}, or {@link #empty()} if the
     * content is empty
     */
    static Bucket of(
        final Species species,
        final Content content
    ) {
        // we call `of` instead of the constructor to filter empty content
        return of(ImmutableMap.of(species, content));
    }

    /**
     * @param map the content to hold, per species
     * @return a bucket holding {@code map}'s non-empty entries, or {@link #empty()} if there are
     * none
     */
    static Bucket of(
        final Map<Species, Content> map
    ) {
        // we use a builder instead of the constructor to filter empty content
        // and potentially add together multiple entries for the same species
        return newBuilder().add(map).build();
    }

    /**
     * @param biomasses    the biomass of each species, in kilograms, indexed by
     *                     {@code speciesIndex}; {@link Double#NaN} entries count as zero
     * @param speciesIndex the index giving each array position's species
     * @return a bucket holding the given biomasses, or {@link #empty()} if they're all zero
     * @throws IllegalArgumentException if the array's length doesn't match the index's size, or
     *                                   any entry is negative
     */
    static Bucket of(
        final double[] biomasses,
        final SpeciesIndex speciesIndex
    ) {
        final BiomassBucket bucket = BiomassBucket.create(biomasses, speciesIndex);
        return bucket.isEmpty() ? Bucket.empty() : bucket;
    }

    /** @return {@code species}' content, or empty if this bucket holds none of it */
    Optional<? extends Content> getContent(Species species);

    /**
     * @return {@code species}' biomass in kilograms, or {@code 0} if this bucket holds none of it
     */
    default double getKg(final Species species) {
        return getContent(species).map(Content::asKg).orElse(0.0);
    }

    /** @return a bucket holding this bucket's content plus {@code other}'s, species by species */
    default Bucket add(final Bucket other) {
        return toBuilder().add(other).build();
    }

    /**
     * @param other the content to remove, species by species
     * @return a bucket holding this bucket's content minus {@code other}'s
     * @throws IllegalArgumentException if {@code other} holds more of any species than this bucket
     *                                   does
     */
    default Bucket subtract(final Bucket other) {
        return toBuilder().subtract(other).build();
    }

    /**
     * @param species    the species whose content to replace
     * @param newContent the content to set, replacing (not adding to) whatever was there
     * @return a bucket with {@code species}' content replaced
     */
    default Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        return toBuilder().put(species, newContent).build();
    }

    /**
     * @param mapper applied to each species' content to produce its new content
     * @return a bucket holding the mapped content, dropping species whose mapped content is empty
     */
    default Bucket mapContent(final BiFunction<Species, Content, Content> mapper) {
        final BucketBuilder bucketBuilder = Bucket.newBuilder();
        forEach((species, content) ->
            bucketBuilder.put(species, mapper.apply(species, content))
        );
        return bucketBuilder.build();
    }

    /**
     * @param mapper applied to each species' biomass in kilograms to produce its new biomass;
     *               returning {@link Double#NaN} drops that species
     * @return a bucket holding the mapped biomasses
     */
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

    /**
     * @param other  supplies the index whose positions are passed to {@code mapper}
     * @param mapper applied to each species' biomass in kilograms and its position in
     *               {@code other}'s index
     * @return a bucket holding the mapped biomasses, dropping species absent from {@code other}'s
     * index
     */
    default Bucket mapWithIndex(
        final SpeciesIndexed other,
        final DoubleIntToDoubleFunction mapper
    ) {
        return mapBiomassValue((species, biomass) -> {
            final int i = other.getSpeciesIndex().indexOf(species);
            return i == -1 ? 0.0 : mapper.applyAsDouble(biomass, i);
        });
    }

    /**
     * @param predicate tested against each species' content
     * @return a map holding, under {@code true}, a bucket of the content matching
     * {@code predicate}, and under {@code false} a bucket of the rest
     */
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

    /** @return whether this bucket holds no content at all */
    boolean isEmpty();

    /** @return the total biomass across every species */
    Biomass getTotalBiomass();

    /** @return the total biomass across every species, in kilograms */
    default double getTotalBiomassInKg() {
        return getTotalBiomass().asKg();
    }

    /** @return this bucket's content, per species; never holds an empty entry */
    Map<Species, Content> getMap();

    /** @return the species this bucket holds content for */
    default Set<Species> getSpecies() {
        return getMap().keySet();
    }

    /** @return a builder pre-populated with this bucket's content */
    default BucketBuilder toBuilder() {
        return newBuilder().put(this);
    }

    /** @param action called once per species holding content */
    default void forEach(final BiConsumer<Species, Content> action) {
        getMap().forEach(action);
    }

    /** @param action called once per species holding content, with its biomass in kilograms */
    default void forEachBiomassValue(final ObjDoubleConsumer<Species> action) {
        forEach((species, content) -> action.accept(species, content.asKg()));
    }

    /**
     * Like {@link #forEachWithIndex(SpeciesIndexed, DoubleIntConsumer, ObjDoubleConsumer)}, but
     * silently skipping species absent from {@code other}'s index.
     *
     * @param other  supplies the index whose positions are passed to {@code action}
     * @param action called with each species' biomass in kilograms and its position in
     *               {@code other}'s index
     */
    default void forEachWithIndex(
        final SpeciesIndexed other,
        final DoubleIntConsumer action
    ) {
        forEachWithIndex(other, action, (species, biomass) -> {
        });
    }

    /**
     * @param other                 supplies the index whose positions are passed to
     *                              {@code action}
     * @param action                called with each species' biomass in kilograms and its
     *                              position in {@code other}'s index
     * @param missingSpeciesAction  called instead, with the species and its biomass, for species
     *                              absent from {@code other}'s index
     */
    default void forEachWithIndex(
        final SpeciesIndexed other,
        final DoubleIntConsumer action,
        final ObjDoubleConsumer<Species> missingSpeciesAction
    ) {
        forEachBiomassValue((species, biomass) -> {
            final int i = other.getSpeciesIndex().indexOf(species);
            if (i == -1) {
                missingSpeciesAction.accept(species, biomass);
            } else {
                action.accept(biomass, i);
            }
        });
    }

}
