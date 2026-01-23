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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class BiomassBucket implements Bucket {

    private final double[] biomasses;
    @Getter private final SpeciesIndex speciesIndex;

    @Getter(lazy = true)
    private final ImmutableMap<Species, Content> map =
        speciesIndex
            .asMap()
            .entrySet()
            .stream()
            .filter(entry -> biomasses[entry.getValue()] > 0)
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> Biomass.ofKg(biomasses[entry.getValue()])
            ));

    public static BiomassBucket ofContentMap(final Map<Species, Content> map) {
        return ofBiomassMap(Maps.transformValues(map, Content::asKg));
    }

    public static BiomassBucket ofBiomassMap(final Map<Species, Double> map) {
        final SpeciesIndex speciesIndex = SpeciesIndex.of(map.keySet());
        final double[] biomasses = speciesIndex.newBiomassArray();
        final Map<Species, Integer> speciesIndexMap = speciesIndex.asMap();
        for (final Entry<Species, Double> entry : map.entrySet()) {
            final int index = speciesIndexMap.get(entry.getKey());
            biomasses[index] = entry.getValue();
        }
        return new BiomassBucket(biomasses, speciesIndex);
    }

    public static BiomassBucket of(
        final double[] biomasses,
        final SpeciesIndex speciesIndex
    ) {
        checkArgument(
            biomasses.length == speciesIndex.size(),
            "Biomass array length must match species index size"
        );
        for (int i = 0; i < biomasses.length; i++) {
            if (biomasses[i] < 0) {
                throw new IllegalArgumentException(
                    "Negative biomass (" + biomasses[i] + ") at index " + i + " in array: " +
                        Arrays.toString(biomasses)
                );
            }
        }
        return new BiomassBucket(biomasses.clone(), speciesIndex);
    }

    @Override
    public Optional<Content> getContent(final Species species) {
        final int i = speciesIndex.indexOf(species);
        if (i == -1) return Optional.empty();
        final double biomass = biomasses[i];
        if (biomass == 0) return Optional.empty();
        return Optional.of(Biomass.ofKg(biomass));
    }

    @Override
    public Bucket add(final Bucket other) {
        return switch (other) {
            case final BiomassBucket otherBucket when sameIndex(otherBucket) ->
                addBiomassArrayBucket(otherBucket);
            default -> addOtherBucket(other);
        };
    }

    private boolean sameIndex(final BiomassBucket other) {
        return speciesIndex.equals(other.speciesIndex);
    }

    private Bucket addBiomassArrayBucket(final BiomassBucket other) {
        final double[] newBiomasses = new double[biomasses.length];
        for (int i = 0; i < biomasses.length; i++) {
            newBiomasses[i] = biomasses[i] + other.biomasses[i];
        }
        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    private Bucket addOtherBucket(final Bucket other) {
        final SpeciesIndex speciesIndex = commonIndex(other);
        final double[] newBiomasses = speciesIndex.newBiomassArray();

        for (final var entry : speciesIndex.asMap().entrySet()) {
            final var species = entry.getKey();
            final int index = entry.getValue();
            newBiomasses[index] = this.getKg(species) + other.getKg(species);
        }

        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    private SpeciesIndex commonIndex(final Bucket other) {
        if (containsAll(speciesIndex, other.getSpecies())) return speciesIndex;
        if (other instanceof final BiomassBucket otherBucket &&
            containsAll(otherBucket.speciesIndex, this.getSpecies())) {
            return otherBucket.speciesIndex;
        }
        return SpeciesIndex.of(Sets.union(this.getSpecies(), other.getSpecies()));
    }

    private static boolean containsAll(
        final SpeciesIndex index,
        final Set<Species> species
    ) {
        if (species.size() > index.size()) return false;
        for (final Species s : species) {
            if (index.indexOf(s) == -1) return false;
        }
        return true;
    }

    @Override
    public Bucket subtract(final Bucket other) {
        return switch (other) {
            case final BiomassBucket otherBucket when sameIndex(otherBucket) ->
                subtractBiomassArrayBucket(otherBucket);
            default -> subtractOtherBucket(other);
        };
    }

    private Bucket subtractBiomassArrayBucket(final BiomassBucket other) {
        final double[] newBiomasses = new double[biomasses.length];
        for (int i = 0; i < biomasses.length; i++) {
            final double otherBiomass = other.biomasses[i];
            newBiomasses[i] = biomasses[i] - other.biomasses[i];
            checkBiomassNonNegativeWhenSubtracting(newBiomasses[i], otherBiomass, i);
        }
        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    private Bucket subtractOtherBucket(final Bucket other) {
        final double[] newBiomasses = speciesIndex.newBiomassArray();
        for (int i = 0; i < speciesIndex.size(); i++) {
            final Species species = speciesIndex.speciesAt(i);
            final double otherBiomass = other.getContent(species).map(Content::asKg).orElse(0.0);
            newBiomasses[i] = biomasses[i] - otherBiomass;
            checkBiomassNonNegativeWhenSubtracting(newBiomasses[i], otherBiomass, i);
        }
        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    private void checkBiomassNonNegativeWhenSubtracting(
        final double newBiomass,
        final double otherBiomass,
        final int i
    ) {
        if (newBiomass < 0) throw new IllegalStateException(
            "Subtracting " + otherBiomass + " kg from " + biomasses[i] +
                " kg for species " + speciesIndex.speciesAt(i) +
                " results in negative biomass " + newBiomass
        );
    }

    @Override
    public Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        final int i = speciesIndex.indexOf(species);
        if (i == -1) return toBuilder().put(species, newContent).build();
        final double[] newBiomasses = biomasses.clone();
        newBiomasses[i] = newContent.asKg();
        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    @Override
    public Bucket mapContent(final UnaryOperator<Content> mapper) {
        final double[] newBiomasses = biomasses.clone();
        for (int i = 0; i < biomasses.length; i++) {
            newBiomasses[i] = mapper.apply(Biomass.ofKg(biomasses[i])).asKg();
        }
        return new BiomassBucket(newBiomasses, speciesIndex);
    }

    @Override
    public Map<Boolean, Bucket> partitionBy(final BiPredicate<Species, Content> predicate) {

        final double[] t = speciesIndex.newBiomassArray();
        final double[] f = speciesIndex.newBiomassArray();

        for (int i = 0; i < speciesIndex.size(); i++) {
            final boolean b = predicate.test(speciesIndex.speciesAt(i), Biomass.ofKg(biomasses[i]));
            (b ? t : f)[i] = biomasses[i];
        }
        return Map.of(
            true, BiomassBucket.of(t, speciesIndex),
            false, BiomassBucket.of(f, speciesIndex)
        );

    }

    @Override
    public void forEach(final BiConsumer<Species, Content> action) {
        for (int i = 0; i < biomasses.length; i++) {
            action.accept(speciesIndex.speciesAt(i), Biomass.ofKg(biomasses[i]));
        }
    }

    @Override
    public void forEachBiomassValue(final ObjDoubleConsumer<Species> action) {
        for (int i = 0; i < biomasses.length; i++) {
            action.accept(speciesIndex.speciesAt(i), biomasses[i]);
        }
    }

    @Override
    public boolean isEmpty() {
        return Arrays.stream(biomasses).sum() == 0;
    }

    public double getDouble(final int index) {
        return biomasses[index];
    }

    @Override
    public Biomass getTotalBiomass() {
        return Biomass.ofKg(Arrays.stream(biomasses).sum());
    }

    @Override
    public Set<Species> getSpecies() {
        final HashSet<Species> species = new HashSet<>();
        for (int i = 0; i < biomasses.length; i++) {
            if (biomasses[i] > 0) species.add(speciesIndex.speciesAt(i));
        }
        return species;
    }

}
