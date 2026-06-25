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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@Data
final class ContentBucket implements Bucket {

    private final ImmutableMap<Species, Content> map;

    private ContentBucket(final ImmutableMap<Species, Content> map) {
        this.map = map;
    }

    static ContentBucket ofContentMap(final Map<Species, Content> map) {
        return new ContentBucket(
            map
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().asKg() > 0.0)
                .collect(toImmutableMap(Entry::getKey, Entry::getValue))
        );
    }

    static Bucket ofBiomassMap(final Map<Species, Double> map) {
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
    public Bucket add(final Bucket other) {
        final Map<Species, Content> results = new HashMap<>(getMap());
        for (final Entry<Species, Content> entry : other.getMap().entrySet()) {
            results.merge(
                entry.getKey(),
                entry.getValue(),
                Content::add
            );
        }
        results.values().removeIf(Content::isEmpty);
        if (results.isEmpty()) {
            return Bucket.empty();
        }
        if (results.values().stream().allMatch(Biomass.class::isInstance)) {
            return BiomassBucket.ofContentMap(results);
        }
        return ContentBucket.ofContentMap(results);
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
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
