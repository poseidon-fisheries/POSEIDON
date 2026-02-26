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

import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

final class AdaptiveBucketBuilder implements BucketBuilder {
    private final Map<Species, Content> map = new HashMap<>();

    @Override
    public AdaptiveBucketBuilder put(final Bucket bucket) {
        checkNotNull(bucket, "bucket");
        return put(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder put(final Map<Species, Content> map) {
        checkNotNull(map, "map");
        map.forEach((species, content) -> {
            checkNotNull(species, "species");
            checkNotNull(content, "content");
            this.map.put(species, content);
        });
        return this;
    }

    @Override
    public AdaptiveBucketBuilder put(
        final Species species,
        final Content newContent
    ) {
        checkNotNull(species, "species");
        checkNotNull(newContent, "newContent");
        map.put(species, newContent);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder add(final Bucket bucket) {
        checkNotNull(bucket, "bucket");
        return add(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder add(final Map<Species, Content> map) {
        checkNotNull(map, "map");
        map.forEach(this::add);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder add(
        final Species species,
        final Content content
    ) {
        checkNotNull(species, "species");
        checkNotNull(content, "content");
        map.merge(species, content, Content::add);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder subtract(final Bucket bucket) {
        checkNotNull(bucket, "bucket");
        return subtract(bucket.getMap());
    }

    @Override
    public AdaptiveBucketBuilder subtract(final Map<Species, Content> map) {
        checkNotNull(map, "map");
        map.forEach(this::subtract);
        return this;
    }

    @Override
    public AdaptiveBucketBuilder subtract(
        final Species species,
        final Content content
    ) {
        checkNotNull(species, "species");
        checkNotNull(content, "content");
        map.merge(species, content, Content::subtract);
        return this;
    }

    @Override
    public Bucket build() {
        // Performance note:
        // This method is intentionally implemented as a single-pass loop (instead of
        // stream/filter/collect) because it sits on a profiled hot path. The loop avoids
        // transient stream/collector allocations while still preserving builder semantics:
        // 1) drop empty content,
        // 2) return Bucket.empty() if everything is empty,
        // 3) specialize one-species biomass into SingleSpeciesBiomassBucket,
        // 4) otherwise choose BiomassBucket vs ContentBucket based on filtered entries.
        Species firstSpecies = null;
        Content firstContent = null;
        boolean allBiomass = true;
        Map<Species, Content> filteredMap = null;

        for (final var entry : map.entrySet()) {
            final Species species = entry.getKey();
            final Content content = entry.getValue();
            if (content.isEmpty()) continue;

            if (firstContent == null) {
                firstSpecies = species;
                firstContent = content;
                allBiomass = content instanceof Biomass;
                continue;
            }

            if (filteredMap == null) {
                filteredMap = new HashMap<>(map.size());
                filteredMap.put(firstSpecies, firstContent);
            }

            filteredMap.put(species, content);
            allBiomass &= content instanceof Biomass;
        }

        if (firstContent == null) {
            return Bucket.empty();
        }

        if (filteredMap == null) {
            if (!(firstContent instanceof Biomass)) {
                return ContentBucket.ofContentMap(Map.of(firstSpecies, firstContent));
            }
            return new SingleSpeciesBiomassBucket(
                firstSpecies,
                firstContent.asKg()
            );
        }

        return allBiomass
            ? BiomassBucket.ofContentMap(filteredMap)
            : ContentBucket.ofContentMap(filteredMap);
    }
}
