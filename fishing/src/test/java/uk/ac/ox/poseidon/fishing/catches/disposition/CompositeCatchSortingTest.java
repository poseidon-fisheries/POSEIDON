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

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;

class CompositeCatchSortingTest {

  @Test
  void rejectsAnIncreaseEvenWhenALaterStepWouldHideIt() {
    final CatchSorting faultyStep = (catches, freeCapacityInKg) ->
      Bucket.of(DummySpecies.A, 120);
    final CatchSorting limiter = new ProportionallyLimitingBiomassToHold();
    final CatchSorting sorting = new CompositeCatchSorting(
      List.of(faultyStep, limiter)
    );

    assertThatThrownBy(() -> sorting.keep(Bucket.of(DummySpecies.A, 100), 40))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(faultyStep.toString())
      .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void eachStepReceivesOnlyWhatItsPredecessorKept() {
    final CatchSorting sorting = new CompositeCatchSorting(
      List.of(
        (catches, freeCapacityInKg) ->
          catches.mapContent((species, content) -> content.multiply(0.5)),
        new ProportionallyLimitingBiomassToHold()
      )
    );

    assertThat(
      sorting.keep(Bucket.of(DummySpecies.A, 100), 75).getKg(DummySpecies.A)
    ).isEqualTo(50);
  }

  @Test
  void anEmptyChainReturnsItsInput() {
    final Bucket catches = Bucket.of(DummySpecies.A, 100);

    assertThat(new CompositeCatchSorting(List.of()).keep(catches, 40)).isSameAs(
      catches
    );
  }

  @Test
  void everyStepReceivesTheSameFreeCapacity() {
    final java.util.List<Double> capacitiesInKg = new java.util.ArrayList<>();
    final CatchSorting recordingStep = (catches, freeCapacityInKg) -> {
      capacitiesInKg.add(freeCapacityInKg);
      return catches;
    };

    new CompositeCatchSorting(List.of(recordingStep, recordingStep)).keep(
      Bucket.of(DummySpecies.A, 100),
      40
    );

    assertThat(capacitiesInKg).containsExactly(40.0, 40.0);
  }
}
