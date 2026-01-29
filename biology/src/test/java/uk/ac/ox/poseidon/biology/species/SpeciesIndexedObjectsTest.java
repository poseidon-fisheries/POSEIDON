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

package uk.ac.ox.poseidon.biology.species;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpeciesIndexedObjectsTest {

    private static final Species SPECIES_A = new Species("A", null, "Alpha");
    private static final Species SPECIES_B = new Species("B", null, "Beta");
    private static final Species SPECIES_C = new Species("C", null, "Gamma");

    @Test
    void default_methods_follow_index_ordering_and_return_same_type() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A, SPECIES_B));
        final String[] values = new String[index.size()];
        values[index.indexOf(SPECIES_A)] = "alpha";
        values[index.indexOf(SPECIES_B)] = "beta";
        final TestIndexedObjects objects = new TestIndexedObjects(values, index);

        assertThat(objects.get(SPECIES_A)).isEqualTo("alpha");
        assertThat(objects.getOrDefault(SPECIES_C, "missing")).isEqualTo("missing");

        final List<String> seenValues = new ArrayList<>();
        objects.forEachValue(seenValues::add);
        assertThat(seenValues).containsExactly(values[0], values[1]);

        final List<Integer> indices = new ArrayList<>();
        objects.forEachWithIndex((value, idx) -> indices.add(idx));
        assertThat(indices).containsExactly(0, 1);

        final List<String> speciesKeys = new ArrayList<>();
        objects.forEachEntry((species, value) -> speciesKeys.add(species.getKey() + ":" + value));
        assertThat(speciesKeys).containsExactly(
            index.speciesAt(0).getKey() + ":" + values[0],
            index.speciesAt(1).getKey() + ":" + values[1]
        );

        final TestIndexedObjects upper = objects.mapValue(String::toUpperCase);
        assertThat(upper.getSpeciesIndex()).isSameAs(index);
        assertThat(upper.get(index.indexOf(SPECIES_A))).isEqualTo("ALPHA");

        final TestIndexedObjects withIndex = objects.mapWithIndex((value, idx) -> value + idx);
        assertThat(withIndex.get(index.indexOf(SPECIES_B)))
            .isEqualTo("beta" + index.indexOf(SPECIES_B));

        final TestIndexedObjects withSpecies = objects.mapEntry(
            (species, value) -> species.getKey() + "-" + value
        );
        assertThat(withSpecies.get(index.indexOf(SPECIES_A))).isEqualTo("A-alpha");
    }

    private static final class TestIndexedObjects
        implements SpeciesIndexedObjects<String, TestIndexedObjects> {

        private final String[] values;
        private final SpeciesIndex speciesIndex;

        private TestIndexedObjects(final String[] values, final SpeciesIndex speciesIndex) {
            this.values = values;
            this.speciesIndex = speciesIndex;
        }

        @Override
        public String get(final int index) {
            return values[index];
        }

        @Override
        public TestIndexedObjects newInstance(final String[] values) {
            return new TestIndexedObjects(values, speciesIndex);
        }

        @Override
        public String[] newArray(final int size) {
            return new String[size];
        }

        @Override
        public SpeciesIndex getSpeciesIndex() {
            return speciesIndex;
        }
    }
}
