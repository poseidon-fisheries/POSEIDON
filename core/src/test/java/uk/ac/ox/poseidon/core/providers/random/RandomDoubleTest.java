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

package uk.ac.ox.poseidon.core.providers.random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;

/**
 * A random double is uniform on {@code [minimum, maximum)}, which must be a
 * non-empty interval.
 */
class RandomDoubleTest {

  @Test
  void refusesAMaximumNotAboveTheMinimum() {
    assertThatThrownBy(() ->
      new RandomDouble(new MersenneTwisterFast(42), 5, 5)
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("less than maximum");
  }

  @Test
  void drawsUniformlyBetweenTheBounds() {
    final RandomDouble random = new RandomDouble(
      new MersenneTwisterFast(42),
      2,
      5
    );
    final MersenneTwisterFast expectedRng = new MersenneTwisterFast(42);

    assertThat(random.apply(null)).isEqualTo(2 + expectedRng.nextDouble() * 3);
  }
}
