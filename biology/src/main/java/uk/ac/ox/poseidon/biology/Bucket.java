/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@Data
public final class Bucket {

    private final ImmutableMap<Species, Content> map;

    private Bucket(final ImmutableMap<Species, Content> map) {
        this.map = map;
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static Bucket empty() {
        return new Bucket(ImmutableMap.<Species, Content>of());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Bucket copyOf(final Bucket other) {
        return new Bucket(other.map);
    }

    public static Bucket of(
        final Species species,
        final Content content
    ) {
        // we call `of` instead of the constructor to filter empty content
        return Bucket.of(ImmutableMap.of(species, content));
    }

    public static Bucket of(
        final Map<Species, Content> map
    ) {
        // we use a builder instead of the constructor to filter empty content
        // and potentially add together multiple entries for the same species
        return new Builder().add(ImmutableMap.copyOf(map)).build();
    }

    public Builder toBuilder() {
        return Bucket.newBuilder().put(this);
    }

    public Optional<Content> getContent(final Species species) {
        return Optional.ofNullable(getMap().get(species));
    }

    public Bucket add(final Bucket other) {
        return new Bucket(
            Stream
                .concat(getMap().entrySet().stream(), other.getMap().entrySet().stream())
                .collect(toImmutableMap(
                    Entry::getKey,
                    Entry::getValue,
                    Content::add
                ))
        );
    }

    public Bucket subtract(final Bucket other) {
        return toBuilder().subtract(other).build();
    }

    public Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        return toBuilder()
            .put(species, newContent)
            .build();
    }

    public Bucket mapContent(final UnaryOperator<Content> mapper) {
        final Builder builder = toBuilder();
        getMap().forEach((species, c) -> builder.put(species, mapper.apply(c)));
        return builder.build();
    }

    public Map<Boolean, Bucket> partitionBy(
        final BiPredicate<Species, Content> predicate
    ) {
        final Bucket.Builder b1 = Bucket.newBuilder();
        final Bucket.Builder b2 = Bucket.newBuilder();
        getMap().forEach((species, content) ->
            (predicate.test(species, content) ? b1 : b2).put(species, content)
        );
        return Map.of(true, b1.build(), false, b2.build());
    }

    public boolean isEmpty() {
        return getMap().values().stream().allMatch(Content::isEmpty);
    }

    public Biomass getTotalBiomass() {
        return getMap()
            .values()
            .stream()
            .map(Content::asBiomass)
            .reduce(Biomass::add)
            .orElse(Biomass.ZERO);
    }

    public static class Builder {
        private final Map<Species, Content> map = new HashMap<>();

        private Builder() {
        }

        public Builder put(
            final Bucket bucket
        ) {
            return put(bucket.getMap());
        }

        public Builder put(
            final Map<Species, Content> map
        ) {
            this.map.putAll(map);
            return this;
        }

        public Builder put(
            final Species species,
            final Content newContent
        ) {
            map.put(species, newContent);
            return this;
        }

        public Builder add(final Bucket bucket) {
            return add(bucket.getMap());
        }

        public Builder add(final Map<Species, Content> map) {
            map.forEach(this::add);
            return this;
        }

        public Builder add(
            final Species species,
            final Content content
        ) {
            map.merge(species, content, Content::add);
            return this;
        }

        public Builder subtract(final Bucket bucket) {
            return subtract(bucket.getMap());
        }

        public Builder subtract(final Map<Species, Content> map) {
            map.forEach(this::subtract);
            return this;
        }

        public Builder subtract(
            final Species species,
            final Content content
        ) {
            map.merge(species, content, Content::subtract);
            return this;
        }

        public Bucket build() {

            if (map.isEmpty())
                return Bucket.empty();

            final ImmutableMap<Species, Content> newMap = this.map
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toImmutableMap(Entry::getKey, Entry::getValue));
            return new Bucket(newMap);
        }

    }

}
