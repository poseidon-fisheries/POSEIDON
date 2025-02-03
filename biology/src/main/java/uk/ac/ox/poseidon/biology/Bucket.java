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

package uk.ac.ox.poseidon.biology;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public interface Bucket<C extends Content<C>> {

    static <C extends Content<C>> Bucket<C> empty() {
        return new EmptyBucket<>();
    }

    static <C extends Content<C>> Builder<C> newBuilder() {
        return new Builder<>();
    }

    static <C extends Content<C>> Bucket<C> copyOf(final Bucket<C> other) {
        return Bucket.from(other.getMap());
    }

    static <C extends Content<C>> Bucket<C> of(
        final Species species,
        final C content
    ) {
        return Bucket.<C>newBuilder().put(species, content).build();
    }

    static <C extends Content<C>> Bucket<C> from(
        final Map<Species, C> map
    ) {
        return Bucket.<C>newBuilder().put(map).build();
    }

    default Builder<C> toBuilder() {
        return Bucket.<C>newBuilder().put(this);
    }

    default C getContent(final Species species) {
        return checkNotNull(
            getMap().get(species),
            "Species %s not present in bucket",
            species
        );
    }

    default Optional<C> maybeGetContent(final Species species) {
        return Optional.ofNullable(getMap().get(species));
    }

    default Bucket<C> add(final Bucket<C> other) {
        return toBuilder().add(other).build();
    }

    default Bucket<C> subtract(final Bucket<C> other) {
        return toBuilder().subtract(other).build();
    }

    default Bucket<C> replaceContent(
        final Species species,
        final C newContent
    ) {
        return toBuilder()
            .put(species, newContent)
            .build();
    }

    Map<Species, C> getMap();

    default Bucket<C> mapContent(final UnaryOperator<C> mapper) {
        final Builder<C> builder = toBuilder();
        getMap().forEach((species, c) -> builder.put(species, mapper.apply(c)));
        return builder.build();
    }

    default boolean isEmpty() {
        return getMap().values().stream().allMatch(C::isEmpty);
    }

    default Biomass getTotalBiomass() {
        return getMap()
            .values()
            .stream()
            .map(Content::asBiomass)
            .reduce(Biomass::add)
            .orElseGet(() -> Biomass.ofKg(0));
    }

    class Builder<C extends Content<C>> {
        private final Map<Species, C> map = new HashMap<>();

        private Builder() {
        }

        public Builder<C> put(
            final Bucket<C> bucket
        ) {
            return put(bucket.getMap());
        }

        public Builder<C> put(
            final Map<Species, C> map
        ) {
            this.map.putAll(map);
            return this;
        }

        public Builder<C> put(
            final Species species,
            final C newContent
        ) {
            map.put(species, newContent);
            return this;
        }

        public Builder<C> add(final Bucket<C> bucket) {
            return add(bucket.getMap());
        }

        public Builder<C> add(final Map<Species, C> map) {
            map.forEach(this::add);
            return this;
        }

        public Builder<C> add(
            final Species species,
            final C content
        ) {
            map.merge(species, content, Content::add);
            return this;
        }

        public Builder<C> subtract(final Bucket<C> bucket) {
            return subtract(bucket.getMap());
        }

        public Builder<C> subtract(final Map<Species, C> map) {
            map.forEach(this::subtract);
            return this;
        }

        public Builder<C> subtract(
            final Species species,
            final C content
        ) {
            map.merge(species, content, Content::subtract);
            return this;
        }

        public Bucket<C> build() {

            if (map.isEmpty())
                return Bucket.empty();

            final ImmutableMap<Species, C> newMap = this.map
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
            return new ImmutableBucket<>(newMap);
        }

    }

}
