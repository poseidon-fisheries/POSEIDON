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

package uk.ac.ox.poseidon.core.predicates.comparable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BetweenTest {

  @Test
  void inclusiveBoundsIncludeEndpoints() {
    final Between<Object, Double> between = new Between<>(
      context -> 0.0,
      context -> 10.0,
      true,
      true
    );

    assertThat(between.apply(null, 0.0)).isTrue();
    assertThat(between.apply(null, 5.0)).isTrue();
    assertThat(between.apply(null, 10.0)).isTrue();
    assertThat(between.apply(null, -0.1)).isFalse();
    assertThat(between.apply(null, 10.1)).isFalse();
  }

  @Test
  void exclusiveMinimumExcludesLowerEndpointOnly() {
    final Between<Object, Double> between = new Between<>(
      context -> 0.0,
      context -> 10.0,
      false,
      true
    );

    assertThat(between.apply(null, 0.0)).isFalse();
    assertThat(between.apply(null, 5.0)).isTrue();
    assertThat(between.apply(null, 10.0)).isTrue();
  }

  @Test
  void exclusiveMaximumExcludesUpperEndpointOnly() {
    final Between<Object, Double> between = new Between<>(
      context -> 0.0,
      context -> 10.0,
      true,
      false
    );

    assertThat(between.apply(null, 0.0)).isTrue();
    assertThat(between.apply(null, 5.0)).isTrue();
    assertThat(between.apply(null, 10.0)).isFalse();
  }

  @Test
  void exclusiveBothBoundsExcludeBothEndpoints() {
    final Between<Object, Double> between = new Between<>(
      context -> 0.0,
      context -> 10.0,
      false,
      false
    );

    assertThat(between.apply(null, 0.0)).isFalse();
    assertThat(between.apply(null, 5.0)).isTrue();
    assertThat(between.apply(null, 10.0)).isFalse();
  }
}
