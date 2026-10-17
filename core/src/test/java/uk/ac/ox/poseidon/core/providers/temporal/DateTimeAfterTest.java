/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DateTimeAfterTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2025, 6, 1, 7, 0);

  @Test
  void addsTheAmountToTheReference() {
    assertThat(
      new DateTimeAfter<>(context -> NOW, context -> Duration.ofHours(3)).apply(
        null
      )
    ).isEqualTo(NOW.plusHours(3));
  }

  @Test
  void readsTheReferenceAnewOnEveryCall() {
    final AtomicReference<LocalDateTime> referenceDateTime =
      new AtomicReference<>(NOW);
    final DateTimeAfter<Object> dateTimeAfter = new DateTimeAfter<>(
      context -> referenceDateTime.get(),
      context -> Duration.ofHours(3)
    );
    dateTimeAfter.apply(null);
    referenceDateTime.set(NOW.plusHours(1));
    assertThat(dateTimeAfter.apply(null)).isEqualTo(NOW.plusHours(4));
  }
}
