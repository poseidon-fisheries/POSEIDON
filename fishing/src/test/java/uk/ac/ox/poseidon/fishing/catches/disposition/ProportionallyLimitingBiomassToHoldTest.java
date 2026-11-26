/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;

class ProportionallyLimitingBiomassToHoldTest {

  private final Bucket catches = Bucket.of(
    new double[] { 60, 40 },
    SpeciesIndex.of(DummySpecies.A, DummySpecies.B)
  );
  private final CatchSorting catchSorting =
    new ProportionallyLimitingBiomassToHold();

  @Test
  void returnsTheInputWhenItFits() {
    assertThat(catchSorting.keep(catches, 100)).isSameAs(catches);
  }

  @Test
  void scalesEverySpeciesByTheSameFactor() {
    final Bucket keptCatch = catchSorting.keep(catches, 50);

    assertThat(keptCatch.getKg(DummySpecies.A)).isEqualTo(30);
    assertThat(keptCatch.getKg(DummySpecies.B)).isEqualTo(20);
  }

  @Test
  void zeroCapacityKeepsNothing() {
    assertThat(catchSorting.keep(catches, 0).isEmpty()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(doubles = { -1, Double.NaN })
  void rejectsInvalidCapacity(final double freeCapacityInKg) {
    assertThatThrownBy(() ->
      catchSorting.keep(catches, freeCapacityInKg)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
