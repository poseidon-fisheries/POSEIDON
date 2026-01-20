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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Streams;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static java.lang.Math.toIntExact;
import static java.util.Map.entry;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;

@Data
@RequiredArgsConstructor(access = PRIVATE)
public class SpeciesIndex {

    private static final Interner<SpeciesIndex> interner = Interners.newWeakInterner();

    private final ImmutableBiMap<Species, Integer> map;

    private static String speciesKey(final Species species) {
        return String.join(
            ";",
            species.getCode(),
            species.getLifeStage() == null ? "" : species.getLifeStage()
        );
    }

    public static SpeciesIndex of(final Set<Species> species) {
        return interner.intern(new SpeciesIndex(makeIndices(species)));
    }

    private static ImmutableBiMap<Species, Integer> makeIndices(final Set<Species> species) {
        final Set<String> speciesKeys =
            species.stream().map(SpeciesIndex::speciesKey).collect(Collectors.toSet());
        if (speciesKeys.size() != species.size()) throw new IllegalArgumentException(
            "Duplicate species codes %s found in species set: %s".formatted(
                species.stream()
                    .map(SpeciesIndex::speciesKey)
                    .collect(groupingBy(Function.identity(), counting()))
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()),
                species
            )
        );
        return Streams
            .mapWithIndex(
                species.stream().sorted(Comparator.comparing(SpeciesIndex::speciesKey)),
                (s, index) -> entry(s, toIntExact(index))
            )
            .collect(toImmutableBiMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public double[] newBiomassArray() {
        return new double[map.size()];
    }

    public Optional<Integer> indexOf(final Species species) {
        return Optional.ofNullable(map.get(species));
    }

    public Optional<Species> speciesAt(final int index) {
        return Optional.ofNullable(map.inverse().get(index));
    }

    public int size() {
        return getMap().size();
    }
}
