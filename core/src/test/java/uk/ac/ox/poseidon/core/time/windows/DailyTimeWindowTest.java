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

package uk.ac.ox.poseidon.core.time.windows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DailyTimeWindowTest {

  private static final Duration STEP = Duration.ofMinutes(30);
  // Two days of half hours
  private static final int STEPS = 96;

  private static DailyTimeWindow<Void> window(
    final LocalTime startTime,
    final LocalTime endTime
  ) {
    return new DailyTimeWindow<>(context -> startTime, context -> endTime);
  }

  private static LocalDateTime dateTimeAt(final int day, final int hour) {
    return LocalDateTime.of(2026, 3, day, hour, 0);
  }

  @Test
  void isOpenAtItsStartAndClosedAtItsEnd() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );

    assertThat(daytime.isOpenAt(null, dateTimeAt(1, 6))).isTrue();
    assertThat(daytime.isOpenAt(null, dateTimeAt(1, 17))).isTrue();
    assertThat(daytime.isOpenAt(null, dateTimeAt(1, 18))).isFalse();
    assertThat(daytime.isOpenAt(null, dateTimeAt(1, 5))).isFalse();
  }

  @Test
  void crossesMidnightWhenItsStartIsAfterItsEnd() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.isOpenAt(null, dateTimeAt(1, 20))).isTrue();
    assertThat(night.isOpenAt(null, dateTimeAt(2, 0))).isTrue();
    assertThat(night.isOpenAt(null, dateTimeAt(2, 5))).isTrue();
    assertThat(night.isOpenAt(null, dateTimeAt(2, 6))).isFalse();
    assertThat(night.isOpenAt(null, dateTimeAt(2, 19))).isFalse();
  }

  @Test
  void isOpenAllDayWhenItsEndsAreEqual() {
    final DailyTimeWindow<Void> allDay = window(
      LocalTime.of(6, 0),
      LocalTime.of(6, 0)
    );

    assertThat(allDay.isOpenAt(null, dateTimeAt(1, 6))).isTrue();
    assertThat(allDay.isOpenAt(null, dateTimeAt(1, 12))).isTrue();
    assertThat(allDay.isOpenAt(null, dateTimeAt(1, 3))).isTrue();
  }

  @Test
  void opensAtTheSameDateTimeWhenOpen() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.earliestOpenDateTime(null, dateTimeAt(1, 20))).contains(
      dateTimeAt(1, 20)
    );
    assertThat(night.earliestOpenDateTime(null, dateTimeAt(2, 3))).contains(
      dateTimeAt(2, 3)
    );
  }

  @Test
  void opensAtItsStartWhenClosedBeforeItToday() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.earliestOpenDateTime(null, dateTimeAt(2, 7))).isEqualTo(
      Optional.of(dateTimeAt(2, 20))
    );
  }

  @Test
  void opensAtItsStartTomorrowWhenClosedAfterItsEnd() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );

    assertThat(daytime.earliestOpenDateTime(null, dateTimeAt(1, 18))).contains(
      dateTimeAt(2, 6)
    );
    assertThat(daytime.earliestOpenDateTime(null, dateTimeAt(1, 5))).contains(
      dateTimeAt(1, 6)
    );
  }

  @Test
  void itsTwoAnswersAgreeAtEveryHalfHourOfTwoDays() {
    final LocalTime[][] startAndEndTimes = {
      { LocalTime.of(6, 0), LocalTime.of(18, 0) },
      { LocalTime.of(20, 0), LocalTime.of(6, 0) },
      { LocalTime.of(6, 0), LocalTime.of(6, 0) }
    };
    for (final LocalTime[] startAndEnd : startAndEndTimes) {
      final DailyTimeWindow<Void> timeWindow = window(
        startAndEnd[0],
        startAndEnd[1]
      );
      for (int step = 0; step < STEPS; step++) {
        final LocalDateTime dateTime = dateTimeAt(1, 0).plus(
          STEP.multipliedBy(step)
        );
        final LocalDateTime earliest = timeWindow
          .earliestOpenDateTime(null, dateTime)
          .orElseThrow();

        if (timeWindow.isOpenAt(null, dateTime)) {
          assertThat(earliest).isEqualTo(dateTime);
        } else {
          assertThat(earliest).isAfter(dateTime);
          assertThat(timeWindow.isOpenAt(null, earliest)).isTrue();
        }
      }
    }
  }

  @Test
  void answersEachContextFromItsOwnEnds() {
    final DailyTimeWindow<Integer> timeWindow = new DailyTimeWindow<>(
      startHour -> LocalTime.of(startHour, 0),
      startHour -> LocalTime.of(startHour + 2, 0)
    );

    assertThat(timeWindow.isOpenAt(8, dateTimeAt(1, 9))).isTrue();
    assertThat(timeWindow.isOpenAt(10, dateTimeAt(1, 9))).isFalse();
    assertThat(timeWindow.earliestOpenDateTime(10, dateTimeAt(1, 9))).contains(
      dateTimeAt(1, 10)
    );
  }

  @Test
  void rejectsAFractionalReferenceDateTime() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );
    final LocalDateTime fractional = dateTimeAt(1, 7).withNano(1);

    assertThatThrownBy(() -> daytime.isOpenAt(null, fractional))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("fractional");
    assertThatThrownBy(() -> daytime.earliestOpenDateTime(null, fractional))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("fractional");
  }

  @Test
  void rejectsAFractionalStartOrEnd() {
    final LocalTime fractional = LocalTime.of(6, 0, 0, 500_000_000);

    assertThatThrownBy(() ->
      window(fractional, LocalTime.of(18, 0)).isOpenAt(null, dateTimeAt(1, 7))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("start time");
    assertThatThrownBy(() ->
      window(LocalTime.of(6, 0), fractional).isOpenAt(null, dateTimeAt(1, 7))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("end time");
  }

  @Test
  void rejectsAnEndThatBecomesFractionalAfterAValidQuery() {
    final AtomicReference<LocalTime> endTime = new AtomicReference<>(
      LocalTime.of(18, 0)
    );
    final DailyTimeWindow<Void> timeWindow = new DailyTimeWindow<>(
      context -> LocalTime.of(6, 0),
      context -> endTime.get()
    );
    assertThat(timeWindow.isOpenAt(null, dateTimeAt(1, 7))).isTrue();

    endTime.set(LocalTime.of(18, 0, 0, 1));

    assertThatThrownBy(() -> timeWindow.isOpenAt(null, dateTimeAt(1, 7)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("end time");
  }

  @Test
  void keepsWholeSecondEdgeBehaviour() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0, 0),
      LocalTime.of(18, 0, 0)
    );

    assertThat(
      daytime.isOpenAt(null, dateTimeAt(1, 6).minusSeconds(1))
    ).isFalse();
    assertThat(
      daytime.isOpenAt(null, dateTimeAt(1, 18).minusSeconds(1))
    ).isTrue();
  }
}
