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
 * A random int is uniform on its inclusive range, which must be valid and fit
 * within integer bounds.
 */
class RandomIntTest {

  private final MersenneTwisterFast rng = new MersenneTwisterFast(42);

  @Test
  void refusesAMaximumBelowTheMinimum() {
    assertThatThrownBy(() -> new RandomInt(rng, 5, 4))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("greater or equal to minimum");
  }

  @Test
  void acceptsAMaximumEqualToTheMinimum() {
    assertThat(new RandomInt(rng, 3, 3).apply(null)).isEqualTo(3);
  }

  @Test
  void acceptsTheWidestValidRange() {
    final RandomInt randomInt = new RandomInt(
      rng,
      Integer.MIN_VALUE,
      Integer.MIN_VALUE + Integer.MAX_VALUE - 1
    );

    for (int i = 0; i < 100; i++) {
      final int value = randomInt.apply(null);
      assertThat(value)
        .isGreaterThanOrEqualTo(Integer.MIN_VALUE)
        .isLessThan(Integer.MIN_VALUE + Integer.MAX_VALUE);
    }
  }

  @Test
  void refusesARangeBeyondIntegerBounds() {
    assertThatThrownBy(() ->
      new RandomInt(rng, Integer.MIN_VALUE, Integer.MAX_VALUE)
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("within integer bounds");
  }
}
