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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

final class AdaptiveBucketBuilder implements BucketBuilder {
    private final Map<Species, Content> map = new HashMap<>();

    @Override
    public AdaptiveBucketBuilder put(final Bucket bucket) {
        return put(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder put(final Map<Species, Content> map) {
        this.map.putAll(map);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder put(
        final Species species,
        final Content newContent
    ) {
        map.put(species, newContent);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder add(final Bucket bucket) {
        return add(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder add(final Map<Species, Content> map) {
        map.forEach(this::add);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder add(
        final Species species,
        final Content content
    ) {
        map.merge(species, content, Content::add);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder subtract(final Bucket bucket) {
        return subtract(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder subtract(final Map<Species, Content> map) {
        map.forEach(this::subtract);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder subtract(
        final Species species,
        final Content content
    ) {
        map.merge(species, content, Content::subtract);
        return this;
    }

    @Override
    public Bucket build() {

        if (map.isEmpty()) {
            return Bucket.empty();
        }

        final ImmutableMap<Species, Content> newMap = this.map
            .entrySet()
            .stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));

        if (newMap.isEmpty()) {
            return Bucket.empty();
        }

        final boolean allBiomass = newMap.values().stream().allMatch(Biomass.class::isInstance);

        return allBiomass
            ? BiomassBucket.ofContentMap(newMap)
            : ContentBucket.ofContentMap(newMap);
    }
}
