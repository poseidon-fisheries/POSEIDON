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

package uk.ac.ox.poseidon.biology.biomass;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.biology.biomass.Factories.smoothBiomassDiffusionRule;

import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import uk.ac.ox.poseidon.core.Scope;

final class SmoothBiomassDiffusionRuleTest {

  @Test
  void movesAFractionOfTheBiomassDifference() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.25,
      0.5
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(100, 200, 20, 200);

    assertThat(result).isEqualTo(new Double2D(80, 40));
  }

  @Test
  void limitsMovementByDestinationSpareCapacity() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.8
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(100, 200, 20, 30);

    assertThat(result).isEqualTo(new Double2D(95, 25));
  }

  @Test
  void capsMovementBySourceBiomass() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.1
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(100, 200, 20, 200);

    assertThat(result).isEqualTo(new Double2D(90, 30));
  }

  @Test
  void doesNotMoveFromTheLessPopulatedCell() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.5
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(20, 200, 100, 200);

    assertThat(result).isEqualTo(new Double2D(20, 100));
  }

  @Test
  void doesNotMoveIntoAFullCell() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.5
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(100, 200, 20, 20);

    assertThat(result).isEqualTo(new Double2D(100, 20));
  }

  @Test
  void doesNotMoveIntoAnOverCapacityCell() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.5
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(100, 200, 30, 20);

    assertThat(result).isEqualTo(new Double2D(100, 30));
  }

  @Test
  void leavesEqualBiomassesUnchanged() {
    final SmoothBiomassDiffusionRule rule = smoothBiomassDiffusionRule(
      0.5,
      0.5
    ).get(Scope.GLOBAL_SCOPE);

    final Double2D result = rule.updatedBiomasses(40, 200, 40, 200);

    assertThat(result).isEqualTo(new Double2D(40, 40));
  }
}
