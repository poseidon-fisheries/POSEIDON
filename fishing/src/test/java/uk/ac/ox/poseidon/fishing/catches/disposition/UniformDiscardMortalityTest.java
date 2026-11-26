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

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;

class UniformDiscardMortalityTest {

  private final Bucket discards = Bucket.of(
    Map.of(DummySpecies.A, Biomass.ofKg(50), DummySpecies.B, Biomass.ofKg(100))
  );

  @Test
  void appliesOneRateToEverySpecies() {
    final Bucket deaths = new UniformDiscardMortality(0.5).deaths(
      discards,
      null
    );

    assertThat(deaths.getKg(DummySpecies.A)).isEqualTo(25);
    assertThat(deaths.getKg(DummySpecies.B)).isEqualTo(50);
  }

  @Test
  void fullMortalityKillsEveryDiscard() {
    assertThat(new UniformDiscardMortality(1).deaths(discards, null)).isEqualTo(
      discards
    );
  }

  @Test
  void zeroMortalityKillsNothing() {
    assertThat(
      new UniformDiscardMortality(0).deaths(discards, null).isEmpty()
    ).isTrue();
  }

  @Test
  void emptyDiscardsHaveNoDeaths() {
    assertThat(
      new UniformDiscardMortality(0.5).deaths(Bucket.empty(), null).isEmpty()
    ).isTrue();
  }

  @ParameterizedTest
  @ValueSource(doubles = { -0.1, 1.1, Double.NaN })
  void rejectsInvalidRates(final double rate) {
    assertThatThrownBy(() -> new UniformDiscardMortality(rate)).isInstanceOf(
      IllegalArgumentException.class
    );
  }
}
