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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ObjDoubleConsumer;

public enum EmptyBucket implements Bucket {

    INSTANCE;

    @Override
    public Optional<Content> getContent(final Species species) {
        return Optional.empty();
    }

    @Override
    public double getKg(final Species species) {
        return 0.0;
    }

    @Override
    public Bucket add(final Bucket other) {
        return other;
    }

    @Override
    public Bucket subtract(final Bucket other) {
        if (other.isEmpty()) return this;
        throw new IllegalArgumentException("Cannot subtract from empty bucket");
    }

    @Override
    public Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        return Bucket.newBuilder().put(species, newContent).build();
    }

    @Override
    public Bucket map(final BiFunction<Species, Content, Content> mapper) {
        return this;
    }

    @Override
    public Map<Boolean, Bucket> partitionBy(final BiPredicate<Species, Content> predicate) {
        return Map.of(true, this, false, this);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Biomass getTotalBiomass() {
        return Biomass.ofKg(0.0);
    }

    @Override
    public ImmutableMap<Species, Content> getMap() {
        return ImmutableMap.of();
    }

    @Override
    public Set<Species> getSpecies() {
        return Set.of();
    }

    @Override
    public BucketBuilder toBuilder() {
        return Bucket.newBuilder();
    }

    @Override
    public void forEach(final BiConsumer<Species, Content> action) {
        // noop
    }

    @Override
    public void forEachBiomassValue(final ObjDoubleConsumer<Species> action) {
        // noop
    }
}
