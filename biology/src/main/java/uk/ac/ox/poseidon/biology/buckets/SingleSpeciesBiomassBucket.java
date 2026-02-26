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

import lombok.AccessLevel;
import lombok.Getter;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.utils.ObjDoubleToDoubleFunction;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkNonNegative;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;

public class SingleSpeciesBiomassBucket implements Bucket {

    private final Species species;
    private final double biomassInKg;

    public SingleSpeciesBiomassBucket(
        final Species species,
        final double biomassInKg
    ) {
        this.species = checkNotNull(species, "species");
        this.biomassInKg = checkPositive(biomassInKg, "biomass");
    }

    @Getter(lazy = true)
    private final Biomass content = new Biomass(biomassInKg);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Optional<Biomass> optionalContent = Optional.of(getContent());

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Map<Species, Content> cachedMap = Map.of(species, getContent());

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Set<Species> cachedSpecies = Set.of(species);

    @Override
    public Optional<? extends Content> getContent(final Species species) {
        return species.equals(this.species) ?
            getOptionalContent() :
            Optional.empty();
    }

    @Override
    public double getKg(final Species species) {
        return species.equals(this.species) ? biomassInKg : 0.0;
    }

    @Override
    public Bucket add(final Bucket other) {
        return switch (other) {
            //@formatter:off
            case final SingleSpeciesBiomassBucket otherBucket
                when otherBucket.species.equals(species) ->
                    new SingleSpeciesBiomassBucket(
                        species, biomassInKg + otherBucket.biomassInKg
                    );
            //@formatter:on
            default -> Bucket.super.add(other);
        };
    }

    @Override
    public Bucket subtract(final Bucket other) {
        return switch (other) {
            //@formatter:off
            case final SingleSpeciesBiomassBucket otherBucket
                when otherBucket.species.equals(species) -> {
                    final double newBiomass = checkNonNegative(
                        biomassInKg - otherBucket.biomassInKg,
                        "biomass"
                    );
                    yield newBiomass == 0
                        ? Bucket.empty()
                        : new SingleSpeciesBiomassBucket(species, newBiomass);
                }
            //@formatter:on
            default -> Bucket.super.subtract(other);
        };
    }

    @Override
    public Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        if (species.equals(this.species) && newContent instanceof Biomass) {
            final double newBiomass = newContent.asKg();
            return newBiomass == 0
                ? Bucket.empty()
                : new SingleSpeciesBiomassBucket(species, newBiomass);
        } else {
            return Bucket.super.replaceContent(species, newContent);
        }
    }

    @Override
    public Bucket mapContent(final BiFunction<Species, Content, Content> mapper) {
        final Content mapped = mapper.apply(species, getContent());
        if (mapped instanceof Biomass) {
            final double mappedKg = mapped.asKg();
            return mappedKg == 0
                ? Bucket.empty()
                : new SingleSpeciesBiomassBucket(species, mappedKg);
        }
        return Bucket.super.mapContent(mapper);
    }

    @Override
    public Bucket mapBiomassValue(final ObjDoubleToDoubleFunction<Species> mapper) {
        final double mappedKg = mapper.applyAsDouble(species, biomassInKg);
        return mappedKg == 0
            ? Bucket.empty()
            : new SingleSpeciesBiomassBucket(species, mappedKg);
    }

    @Override
    public Map<Boolean, Bucket> partitionBy(final BiPredicate<Species, Content> predicate) {
        return (predicate.test(species, getContent()))
            ? Map.of(true, this, false, Bucket.empty())
            : Map.of(true, Bucket.empty(), false, this);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Biomass getTotalBiomass() {
        return getContent();
    }

    @Override
    public double getTotalBiomassInKg() {
        return biomassInKg;
    }

    @Override
    public Map<Species, Content> getMap() {
        return getCachedMap();
    }

    @Override
    public Set<Species> getSpecies() {
        return getCachedSpecies();
    }

    @Override
    public void forEach(final BiConsumer<Species, Content> action) {
        action.accept(species, getContent());
    }

    @Override
    public void forEachBiomassValue(final ObjDoubleConsumer<Species> action) {
        action.accept(species, biomassInKg);
    }
}
