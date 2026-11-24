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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class DailyTimeWindowTest {

  private static DailyTimeWindow<Void> window(
    final LocalTime startTime,
    final LocalTime endTime
  ) {
    return new DailyTimeWindow<>(context -> startTime, context -> endTime);
  }

  private static LocalDateTime at(final int day, final int hour) {
    return LocalDateTime.of(2026, 3, day, hour, 0);
  }

  @Test
  void isOpenAtItsStartAndClosedAtItsEnd() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );

    assertThat(daytime.isOpenAt(null, at(1, 6))).isTrue();
    assertThat(daytime.isOpenAt(null, at(1, 17))).isTrue();
    assertThat(daytime.isOpenAt(null, at(1, 18))).isFalse();
    assertThat(daytime.isOpenAt(null, at(1, 5))).isFalse();
  }

  @Test
  void crossesMidnightWhenItsStartIsAfterItsEnd() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.isOpenAt(null, at(1, 20))).isTrue();
    assertThat(night.isOpenAt(null, at(2, 0))).isTrue();
    assertThat(night.isOpenAt(null, at(2, 5))).isTrue();
    assertThat(night.isOpenAt(null, at(2, 6))).isFalse();
    assertThat(night.isOpenAt(null, at(2, 19))).isFalse();
  }

  @Test
  void isOpenAllDayWhenItsEndsAreEqual() {
    final DailyTimeWindow<Void> allDay = window(
      LocalTime.of(6, 0),
      LocalTime.of(6, 0)
    );

    assertThat(allDay.isOpenAt(null, at(1, 6))).isTrue();
    assertThat(allDay.isOpenAt(null, at(1, 12))).isTrue();
    assertThat(allDay.isOpenAt(null, at(1, 3))).isTrue();
  }

  @Test
  void opensAtTheSameDateTimeWhenOpen() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.earliestOpenDateTime(null, at(1, 20))).contains(at(1, 20));
    assertThat(night.earliestOpenDateTime(null, at(2, 3))).contains(at(2, 3));
  }

  @Test
  void opensAtItsStartWhenClosedBeforeItToday() {
    final DailyTimeWindow<Void> night = window(
      LocalTime.of(20, 0),
      LocalTime.of(6, 0)
    );

    assertThat(night.earliestOpenDateTime(null, at(2, 7))).isEqualTo(
      Optional.of(at(2, 20))
    );
  }

  @Test
  void opensAtItsStartTomorrowWhenClosedAfterItsEnd() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );

    assertThat(daytime.earliestOpenDateTime(null, at(1, 18))).contains(
      at(2, 6)
    );
    assertThat(daytime.earliestOpenDateTime(null, at(1, 5))).contains(at(1, 6));
  }

  @Test
  void itsTwoAnswersAgreeAtEveryHalfHourOfTheDay() {
    final LocalTime[][] ends = {
      { LocalTime.of(6, 0), LocalTime.of(18, 0) },
      { LocalTime.of(20, 0), LocalTime.of(6, 0) },
      { LocalTime.of(6, 0), LocalTime.of(6, 0) }
    };
    for (final LocalTime[] startAndEnd : ends) {
      final DailyTimeWindow<Void> window = window(
        startAndEnd[0],
        startAndEnd[1]
      );
      IntStream.range(0, 96).forEach(halfHour -> {
        final LocalDateTime dateTime = at(1, 0).plusMinutes(30L * halfHour);
        final LocalDateTime earliest = window
          .earliestOpenDateTime(null, dateTime)
          .orElseThrow();

        if (window.isOpenAt(null, dateTime)) {
          assertThat(earliest).isEqualTo(dateTime);
        } else {
          assertThat(earliest).isAfter(dateTime);
          assertThat(window.isOpenAt(null, earliest)).isTrue();
        }
      });
    }
  }

  @Test
  void answersEachContextFromItsOwnEnds() {
    final DailyTimeWindow<Integer> window = new DailyTimeWindow<>(
      startHour -> LocalTime.of(startHour, 0),
      startHour -> LocalTime.of(startHour + 2, 0)
    );

    assertThat(window.isOpenAt(8, at(1, 9))).isTrue();
    assertThat(window.isOpenAt(10, at(1, 9))).isFalse();
    assertThat(window.earliestOpenDateTime(10, at(1, 9))).contains(at(1, 10));
  }

  @Test
  void rejectsAFractionalReferenceDateTime() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0),
      LocalTime.of(18, 0)
    );
    final LocalDateTime fractional = at(1, 7).withNano(1);

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
      window(fractional, LocalTime.of(18, 0)).isOpenAt(null, at(1, 7))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("start time");
    assertThatThrownBy(() ->
      window(LocalTime.of(6, 0), fractional).isOpenAt(null, at(1, 7))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("end time");
  }

  @Test
  void rejectsAnEndThatBecomesFractionalAfterAValidQuery() {
    final AtomicReference<LocalTime> endTime = new AtomicReference<>(
      LocalTime.of(18, 0)
    );
    final DailyTimeWindow<Void> window = new DailyTimeWindow<>(
      context -> LocalTime.of(6, 0),
      context -> endTime.get()
    );
    assertThat(window.isOpenAt(null, at(1, 7))).isTrue();

    endTime.set(LocalTime.of(18, 0, 0, 1));

    assertThatThrownBy(() -> window.isOpenAt(null, at(1, 7)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("end time");
  }

  @Test
  void keepsWholeSecondEdgeBehaviour() {
    final DailyTimeWindow<Void> daytime = window(
      LocalTime.of(6, 0, 0),
      LocalTime.of(18, 0, 0)
    );

    assertThat(daytime.isOpenAt(null, at(1, 6).minusSeconds(1))).isFalse();
    assertThat(daytime.isOpenAt(null, at(1, 18).minusSeconds(1))).isTrue();
  }
}
