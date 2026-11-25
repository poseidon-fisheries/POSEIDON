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
import static org.assertj.core.api.Assertions.within;

import ec.util.MersenneTwisterFast;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sim.util.distribution.Exponential;

/**
 * An exponentially distributed duration draws its rate from the mean and its
 * seconds from the distribution, rounded up so a draw is never zero.
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
      .isEqualTo(
        Duration.ofSeconds(Math.max(1, (long) Math.ceil(expected.nextDouble())))
      );
  }

  @Test
  void drawsArePositiveWithASampleMeanNearTheRequestedMean() {
    final int draws = 100_000;
    final ExponentiallyDistributedDuration supplier =
      new ExponentiallyDistributedDuration(
        Duration.ofSeconds(10),
        new MersenneTwisterFast(42)
      );

    long totalSeconds = 0;
    for (int i = 0; i < draws; i++) {
      final Duration draw = supplier.get();
      assertThat(draw).isPositive();
      totalSeconds += draw.getSeconds();
    }

    // Rounding up adds half a second on average, so the expected mean is the
    // requested one plus 0.5 s. The standard error of the mean is about
    // 10 / sqrt(draws) = 0.03 s, so a tolerance of 0.2 s is generous, yet it
    // still rejects the old bias of a full second.
    assertThat((double) totalSeconds / draws).isCloseTo(10.5, within(0.2));
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
