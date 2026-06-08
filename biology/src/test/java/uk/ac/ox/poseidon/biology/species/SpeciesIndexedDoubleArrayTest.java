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

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeciesIndexedDoubleArrayTest {

    private static final Species SPECIES_A = new Species("A", null, "Alpha");
    private static final Species SPECIES_B = new Species("B", null, "Beta");
    private static final Species SPECIES_C = new Species("C", null, "Gamma");

    @Test
    void constructor_rejectsMismatchedArrayLength() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A, SPECIES_B));

        assertThatThrownBy(() -> SpeciesIndexedDoubleArray.of(
            new double[]{1.0},
            index
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void get_Double_returnsValueForSpeciesInIndex() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A, SPECIES_B));
        final double[] values = new double[index.size()];
        values[index.indexOf(SPECIES_A)] = 1.5;
        values[index.indexOf(SPECIES_B)] = 2.5;
        final SpeciesIndexedDoubleArray array = SpeciesIndexedDoubleArray.of(values, index);

        assertThat(array.getDouble(SPECIES_A)).isEqualTo(1.5);
        assertThat(array.getDouble(SPECIES_B)).isEqualTo(2.5);
    }

    @Test
    void getDoubleOrDefault_returnsDefaultForMissingSpecies() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A));
        final double[] values = new double[index.size()];
        values[index.indexOf(SPECIES_A)] = 3.0;
        final SpeciesIndexedDoubleArray array = SpeciesIndexedDoubleArray.of(values, index);

        assertThat(array.getDoubleOrDefault(SPECIES_A, 1.0)).isEqualTo(3.0);
        assertThat(array.getDoubleOrDefault(SPECIES_C, 1.0)).isEqualTo(1.0);
    }

    @Test
    void get_Double_returnsValueForIndex() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A, SPECIES_B));
        final double[] values = new double[index.size()];
        values[0] = 4.0;
        values[1] = 6.0;
        final SpeciesIndexedDoubleArray array = SpeciesIndexedDoubleArray.of(values, index);

        assertThat(array.getDouble(0)).isEqualTo(4.0);
        assertThat(array.getDouble(1)).isEqualTo(6.0);
    }

    @Test
    void forEach_and_map_methods_followIndexOrdering() {
        final SpeciesIndex index = SpeciesIndex.of(Set.of(SPECIES_A, SPECIES_B));
        final double[] values = new double[index.size()];
        values[index.indexOf(SPECIES_A)] = 2.0;
        values[index.indexOf(SPECIES_B)] = 5.0;
        final SpeciesIndexedDoubleArray array = SpeciesIndexedDoubleArray.of(values, index);

        final List<MultiKey<Object>> speciesKeys = new ArrayList<>();
        final List<Double> observedValues = new ArrayList<>();
        array.forEachEntry((species, value) -> {
            speciesKeys.add(species.getKey());
            observedValues.add(value);
        });

        assertThat(speciesKeys).containsExactly(
            index.speciesAt(0).getKey(),
            index.speciesAt(1).getKey()
        );
        assertThat(observedValues).containsExactly(values[0], values[1]);

        final double[] sum = new double[]{0.0};
        array.forEachValue(value -> sum[0] += value);
        assertThat(sum[0]).isEqualTo(7.0);

        final List<Integer> indices = new ArrayList<>();
        array.forEachWithIndex((value, idx) -> indices.add(idx));
        assertThat(indices).containsExactly(0, 1);

        assertThat(array.mapValue(value -> value + 1).getDouble(0)).isEqualTo(3.0);
        assertThat(array
            .mapWithIndex((value, idx) -> value + idx)
            .getDouble(1)).isEqualTo(6.0);
        assertThat(array
            .mapEntry((species, value) -> species.equals(SPECIES_A) ? 9.0 : value)
            .getDouble(0))
            .isEqualTo(9.0);
    }
}
