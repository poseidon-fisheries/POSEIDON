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

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class NextOccurrenceOfTimeOfDayTest {

  private static final LocalTime[] SIX_AND_TWENTY_TWO = {
    LocalTime.of(22, 0),
    LocalTime.of(6, 0)
  };

  private static LocalDateTime nextOccurrenceAfter(
    final LocalDateTime referenceDateTime,
    final LocalTime... times
  ) {
    return new NextOccurrenceOfTimeOfDay<>(
      context -> referenceDateTime,
      times
    ).apply(null);
  }

  @Test
  void picksTheNextTimeLaterTheSameDay() {
    assertThat(
      nextOccurrenceAfter(
        LocalDateTime.of(2025, 6, 1, 2, 0),
        SIX_AND_TWENTY_TWO
      )
    ).isEqualTo(LocalDateTime.of(2025, 6, 1, 6, 0));
    assertThat(
      nextOccurrenceAfter(
        LocalDateTime.of(2025, 6, 1, 7, 0),
        SIX_AND_TWENTY_TWO
      )
    ).isEqualTo(LocalDateTime.of(2025, 6, 1, 22, 0));
  }

  @Test
  void wrapsToTheNextDayWhenEveryTimeHasPassed() {
    assertThat(
      nextOccurrenceAfter(
        LocalDateTime.of(2025, 6, 1, 23, 0),
        SIX_AND_TWENTY_TWO
      )
    ).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
  }

  @Test
  void isStrictlyAfterTheReference() {
    assertThat(
      nextOccurrenceAfter(
        LocalDateTime.of(2025, 6, 1, 6, 0),
        SIX_AND_TWENTY_TWO
      )
    ).isEqualTo(LocalDateTime.of(2025, 6, 1, 22, 0));
    assertThat(
      nextOccurrenceAfter(
        LocalDateTime.of(2025, 6, 1, 22, 0),
        SIX_AND_TWENTY_TWO
      )
    ).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
  }

  @Test
  void readsTheReferenceAnewOnEachCall() {
    final LocalDateTime[] reference = { LocalDateTime.of(2025, 6, 1, 22, 30) };
    final NextOccurrenceOfTimeOfDay<Object> nextOccurrence =
      new NextOccurrenceOfTimeOfDay<>(
        context -> reference[0],
        new LocalTime[] { LocalTime.of(6, 30) }
      );
    assertThat(nextOccurrence.apply(null)).isEqualTo(
      LocalDateTime.of(2025, 6, 2, 6, 30)
    );

    reference[0] = LocalDateTime.of(2025, 6, 2, 22, 30);

    assertThat(nextOccurrence.apply(null)).isEqualTo(
      LocalDateTime.of(2025, 6, 3, 6, 30)
    );
  }
}
