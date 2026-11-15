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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;

class CompositeDispositionProcessTest {

  private final Disposition disposition = new Disposition(
    Bucket.of(DummySpecies.A, Biomass.ofKg(100.0)),
    Bucket.empty(),
    Bucket.empty()
  );

  @Test
  void partition_limitThenMortalityLeavesExcessDead() {
    final Disposition result = new CompositeDispositionProcess(
      List.of(
        new ProportionallyLimitingBiomassToHold(),
        new FullDiscardMortality()
      )
    ).partition(disposition, 40.0);

    assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(40.0);
    assertThat(result.getDiscardedAlive().isEmpty()).isTrue();
    assertThat(result.getDiscardedDead().getKg(DummySpecies.A)).isEqualTo(60.0);
  }

  @Test
  void partition_mortalityThenLimitLeavesExcessAlive() {
    final Disposition result = new CompositeDispositionProcess(
      List.of(
        new FullDiscardMortality(),
        new ProportionallyLimitingBiomassToHold()
      )
    ).partition(disposition, 40.0);

    assertThat(result.getRetained().getKg(DummySpecies.A)).isEqualTo(40.0);
    assertThat(result.getDiscardedAlive().getKg(DummySpecies.A)).isEqualTo(
      60.0
    );
    assertThat(result.getDiscardedDead().isEmpty()).isTrue();
  }

  @Test
  void partition_returnsDispositionUnchangedWhenEmpty() {
    assertThat(
      new CompositeDispositionProcess(List.of()).partition(disposition, 40.0)
    ).isSameAs(disposition);
  }

  @Test
  void partition_passesSameCapacityToEveryProcess() {
    final List<Double> capacities = new ArrayList<>();
    final DispositionProcess recording = (currentDisposition, capacity) -> {
      capacities.add(capacity);
      return currentDisposition;
    };

    new CompositeDispositionProcess(
      List.of(recording, recording, recording)
    ).partition(disposition, 40.0);

    assertThat(capacities).containsExactly(40.0, 40.0, 40.0);
  }
}
