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

package uk.ac.ox.poseidon.core.providers.temporal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DurationUntilTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2025, 6, 1, 7, 0);

  private static Duration durationUntil(final LocalDateTime referenceDateTime) {
    return new DurationUntil<>(
      context -> NOW,
      context -> referenceDateTime
    ).apply(null);
  }

  @Test
  void isTheTimeLeftUntilALaterReference() {
    assertThat(durationUntil(NOW.plusHours(3))).isEqualTo(Duration.ofHours(3));
  }

  @Test
  void isZeroAtTheReference() {
    assertThat(durationUntil(NOW)).isEqualTo(Duration.ZERO);
  }

  @Test
  void isNegativeOnceTheReferenceHasPassed() {
    assertThat(durationUntil(NOW.minusMinutes(30))).isEqualTo(
      Duration.ofMinutes(-30)
    );
  }
}
