/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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
import lombok.Data;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@Data
public final class ContentBucket implements Bucket {

    private final ImmutableMap<Species, Content> map;

    public static ContentBucket copyOf(final Bucket bucket) {
        return switch (bucket) {
            case final ContentBucket contentBucket -> contentBucket;
            default -> ofContentMap(bucket.getMap());
        };
    }

    private ContentBucket(final ImmutableMap<Species, Content> map) {
        this.map = map;
    }

    public static ContentBucket ofContentMap(final Map<Species, Content> map) {
        return new ContentBucket(
            map
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().asKg() > 0.0)
                .collect(toImmutableMap(Entry::getKey, Entry::getValue))
        );
    }

    public static Bucket ofBiomassMap(final Map<Species, Double> map) {
        return new ContentBucket(
            map
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0.0)
                .collect(toImmutableMap(
                        Entry::getKey,
                        e -> Biomass.ofKg(e.getValue())
                    )
                )
        );
    }

    @Override
    public Optional<Content> getContent(final Species species) {
        return Optional.ofNullable(getMap().get(species));
    }

    @Override
    public ContentBucket add(final Bucket other) {
        return new ContentBucket(
            Stream
                .concat(getMap().entrySet().stream(), other.getMap().entrySet().stream())
                .collect(toImmutableMap(
                    Entry::getKey,
                    Entry::getValue,
                    Content::add
                ))
        );
    }

    @Override
    public Bucket subtract(final Bucket other) {
        return toBuilder().subtract(other).build();
    }

    @Override
    public Bucket replaceContent(
        final Species species,
        final Content newContent
    ) {
        return toBuilder()
            .put(species, newContent)
            .build();
    }

    @Override
    public Map<Boolean, Bucket> partitionBy(
        final BiPredicate<Species, Content> predicate
    ) {
        final BucketBuilder b1 = Bucket.newBuilder();
        final BucketBuilder b2 = Bucket.newBuilder();
        getMap().forEach((species, content) ->
            (predicate.test(species, content) ? b1 : b2).put(species, content)
        );
        return Map.of(true, b1.build(), false, b2.build());
    }

    @Override
    public boolean isEmpty() {
        return getMap().values().stream().allMatch(Content::isEmpty);
    }

    @Override
    public Biomass getTotalBiomass() {
        return getMap()
            .values()
            .stream()
            .map(Content::asBiomass)
            .reduce(Biomass::add)
            .orElse(Biomass.ZERO);
    }

}
