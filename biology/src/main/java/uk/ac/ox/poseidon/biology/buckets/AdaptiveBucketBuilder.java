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
        Species onlySpecies = null;
        Content onlyContent = null;
        int nonEmptyEntries = 0;
        boolean allBiomass = true;
        Map<Species, Content> filteredMap = null;

        for (final var entry : map.entrySet()) {
            final Species species = entry.getKey();
            final Content content = entry.getValue();
            if (content.isEmpty()) continue;

            if (nonEmptyEntries == 0) {
                onlySpecies = species;
                onlyContent = content;
                nonEmptyEntries = 1;
                allBiomass = content instanceof Biomass;
                continue;
            }

            if (nonEmptyEntries == 1) {
                filteredMap = new HashMap<>();
                filteredMap.put(onlySpecies, onlyContent);
            }

            filteredMap.put(species, content);
            nonEmptyEntries++;
            if (!(content instanceof Biomass)) allBiomass = false;
        }

        if (nonEmptyEntries == 0) {
            return Bucket.empty();
        }

        if (nonEmptyEntries == 1) {
            if (!(onlyContent instanceof Biomass)) {
                return ContentBucket.ofContentMap(Map.of(onlySpecies, onlyContent));
            }
            return new SingleSpeciesBiomassBucket(
                onlySpecies,
                onlyContent.asKg()
            );
        }

        return allBiomass
            ? BiomassBucket.ofContentMap(filteredMap)
            : ContentBucket.ofContentMap(filteredMap);
    }
}
