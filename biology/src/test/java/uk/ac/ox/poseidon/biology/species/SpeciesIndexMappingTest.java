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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static uk.ac.ox.poseidon.biology.species.Factories.speciesIndex;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;

final class SpeciesIndexMappingTest {

  private static final Species COD = new Species("COD", null, "Cod");
  private static final Species HAD = new Species("HAD", null, "Haddock");

  @Test
  void mappingAssignsConcreteValuesToSpeciesInSortedIndexOrder() {
    final SpeciesIndex index = speciesIndex(s -> List.of(HAD, COD)).get(
      Scope.GLOBAL_SCOPE
    );
    final Map<Species, Double> values = Map.of(COD, 3.0, HAD, 8.0);

    final SpeciesIndexedDoubleArray mapped = index.mapToDoubleArray(
      values::get
    );

    assertThat(mapped.getSpeciesIndex()).isSameAs(index);
    assertThat(mapped.getDouble(0)).isEqualTo(3);
    assertThat(mapped.getDouble(1)).isEqualTo(8);
    assertThat(mapped.getDouble(COD)).isEqualTo(3);
    assertThat(mapped.getDouble(HAD)).isEqualTo(8);
  }

  @Test
  void collectionAndSetInputsShareTheSameInternedIndex() {
    final SpeciesIndex fromCollection = SpeciesIndex.of(List.of(HAD, COD, COD));
    final SpeciesIndex fromSet = SpeciesIndex.of(Set.of(COD, HAD));

    assertThat(fromCollection).isSameAs(fromSet);
    assertThat(fromCollection.size()).isEqualTo(2);
    assertThat(fromCollection.speciesAt(0)).isEqualTo(COD);
    assertThat(fromCollection.speciesAt(1)).isEqualTo(HAD);
  }

  @Test
  void mappingRejectsAMissingFunction() {
    final SpeciesIndex index = SpeciesIndex.of(COD);

    assertThatNullPointerException().isThrownBy(() ->
      index.mapToDoubleArray(null)
    );
  }

  @Test
  void mappingRejectsAMissingSpeciesValue() {
    final SpeciesIndex index = SpeciesIndex.of(COD, HAD);
    final Map<Species, Double> values = Map.of(COD, 3.0);

    assertThatNullPointerException().isThrownBy(() ->
      index.mapToDoubleArray(values::get)
    );
  }

  @Test
  void sharedIndexRecognitionDistinguishesEqualAndDifferentSpeciesSets() {
    final SpeciesIndexedDoubleArray first = SpeciesIndexedDoubleArray.of(
      new double[] { 3, 8 },
      SpeciesIndex.of(COD, HAD)
    );
    final SpeciesIndexedDoubleArray same = SpeciesIndexedDoubleArray.of(
      new double[] { 1, 2 },
      SpeciesIndex.of(HAD, COD)
    );
    final SpeciesIndexedDoubleArray different = SpeciesIndexedDoubleArray.of(
      new double[] { 3 },
      SpeciesIndex.of(COD)
    );

    assertThat(first.sameIndex(same)).isTrue();
    assertThat(first.sameIndex(different)).isFalse();
  }
}
