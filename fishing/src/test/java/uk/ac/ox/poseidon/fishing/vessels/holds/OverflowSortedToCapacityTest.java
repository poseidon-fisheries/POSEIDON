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

package uk.ac.ox.poseidon.fishing.vessels.holds;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.fishing.catches.disposition.Factories.proportionallyLimitingBiomassToHold;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.DummySpecies;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.catches.disposition.CatchSorting;

/**
 * A set bigger than the hold's free capacity, as {@code Fishing} handles it:
 * the limiter fits it to the free capacity, and the hold then counts as full.
 */
class OverflowSortedToCapacityTest {

  private static final CatchCategory CATEGORY = new CatchCategory("C");

  private final CatchSorting limiter =
    proportionallyLimitingBiomassToHold().get(Scope.GLOBAL_SCOPE);

  private final StandardBiomassHold hold = new StandardBiomassHold(
    bucket -> new CategorisedCatch(Map.of(CATEGORY, bucket)),
    60_000.0
  );

  @Test
  void aSetBeyondTheFreeCapacityFillsTheHoldAndDiscardsTheRest() {
    final Bucket set = Bucket.of(
      Map.of(
        DummySpecies.A,
        Biomass.ofKg(21_000.0 / 3),
        DummySpecies.B,
        Biomass.ofKg(70_000.0 / 7)
      )
    );
    hold.addContent(Bucket.of(DummySpecies.A, Biomass.ofKg(45_000.0)));

    final Bucket keptCatch = limiter.keep(set, hold.getAvailableCapacityInKg());
    hold.addContent(keptCatch);

    assertThat(hold.isFilledTo(1)).isTrue();
    assertThat(hold.getContent().getTotalBiomassInKg()).isCloseTo(
      60_000.0,
      org.assertj.core.data.Offset.offset(1e-6)
    );
    assertThat(set.subtract(keptCatch).getTotalBiomassInKg()).isCloseTo(
      set.getTotalBiomassInKg() - 15_000.0,
      org.assertj.core.data.Offset.offset(1e-6)
    );
  }

  @Test
  void aSetThatFitsLeavesTheHoldBelowTheThresholdAndReturnsNothing() {
    final Bucket set = Bucket.of(DummySpecies.A, Biomass.ofKg(10_000.0));

    final Bucket keptCatch = limiter.keep(set, hold.getAvailableCapacityInKg());
    hold.addContent(keptCatch);

    assertThat(hold.isFilledTo(0.5)).isFalse();
    assertThat(set.subtract(keptCatch).isEmpty()).isTrue();
  }
}
