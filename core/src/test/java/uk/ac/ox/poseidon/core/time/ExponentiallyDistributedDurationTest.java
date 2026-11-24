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

package uk.ac.ox.poseidon.core.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.util.MersenneTwisterFast;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sim.util.distribution.Exponential;

/**
 * An exponentially distributed duration draws its rate from the mean and its
 * seconds from the distribution, adding a second so a draw is never zero.
 */
class ExponentiallyDistributedDurationTest {

  @Test
  void drawsExponentiallyDistributedSecondsAroundTheMean() {
    final ExponentiallyDistributedDuration supplier =
      new ExponentiallyDistributedDuration(
        Duration.ofSeconds(2),
        new MersenneTwisterFast(42)
      );
    final Exponential expected = new Exponential(
      1.0 / 2,
      new MersenneTwisterFast(42)
    );

    assertThat(supplier.get())
      .isPositive()
      .isEqualTo(Duration.ofSeconds(1 + expected.nextInt()));
  }

  @Test
  void refusesAMeanWithAFractionalSecond() {
    assertThatThrownBy(() ->
      new ExponentiallyDistributedDuration(
        Duration.ofMillis(500),
        new MersenneTwisterFast(42)
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("fractional");
  }
}
