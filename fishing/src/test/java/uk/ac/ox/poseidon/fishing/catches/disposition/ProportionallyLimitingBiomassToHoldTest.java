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

class ProportionallyLimitingBiomassToHoldTest {

  private final Disposition disposition = new Disposition(
    Bucket.of(
      Map.of(
        DummySpecies.A,
        Biomass.ofKg(60.0),
        DummySpecies.B,
        Biomass.ofKg(40.0)
      )
    ),
    Bucket.of(DummySpecies.A, Biomass.ofKg(5.0)),
    Bucket.of(DummySpecies.B, Biomass.ofKg(7.0))
  );

  private final ProportionallyLimitingBiomassToHold process =
    new ProportionallyLimitingBiomassToHold();

  @Test
  void partition_returnsDispositionUnchangedWhenRetainedFits() {
    assertThat(process.partition(disposition, 100.0)).isSameAs(disposition);
  }

  @Test
  void partition_scalesEverySpeciesBySameFactorWhenRetainedDoesNotFit() {
    final Disposition result = process.partition(disposition, 50.0);

    assertThat(result.getRetained().getTotalBiomassInKg()).isEqualTo(50.0);
    assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(30.0);
    assertThat(result.getRetained().getKg(DummySpecies.B)).isEqualTo(20.0);
  }

  @Test
  void partition_movesExcessToDiscardedAliveAndConservesBiomass() {
    final Disposition result = process.partition(disposition, 50.0);

    assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(
      35.0
    );
    assertThat(result.getDiscardedAlive().getKg(DummySpecies.B)).isEqualTo(
      20.0
    );
    assertThat(result.getDiscardedDead()).isEqualTo(
      disposition.getDiscardedDead()
    );
    assertThat(totalKg(result)).isEqualTo(totalKg(disposition));
  }

  @Test
  void partition_discardsAllRetainedAliveWhenCapacityIsZero() {
    final Disposition result = process.partition(disposition, 0.0);

    assertThat(result.getRetained().getTotalBiomassInKg()).isZero();
    assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(
      65.0
    );
    assertThat(result.getDiscardedAlive().getKg(DummySpecies.B)).isEqualTo(
      40.0
    );
  }

  @ParameterizedTest
  @ValueSource(doubles = { -1.0, Double.NaN })
  void partition_throwsOnNegativeOrNaNCapacity(final double capacity) {
    assertThatThrownBy(() -> process.partition(disposition, capacity))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be a non-negative value");
  }

  private static double totalKg(final Disposition disposition) {
    return (
      disposition.getRetained().getTotalBiomassInKg() +
      disposition.getDiscardedAlive().getTotalBiomassInKg() +
      disposition.getDiscardedDead().getTotalBiomassInKg()
    );
  }
}
