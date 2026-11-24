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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.functions.ValueAsMappingFactory;
import uk.ac.ox.poseidon.core.time.TimeFactory;

/**
 * Factories for time windows, and for what can be asked of them: whether one is
 * open at a date-time, and how long until it is open. A time window is asked
 * under a context, such as the vessel asking; a window or question that needs
 * none has {@code Void} or {@code Object} as its context.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @param startTime the time of day the window opens, inclusive
   * @param endTime   the time of day the window closes, exclusive
   * @return a factory for a {@link DailyTimeWindow} over fixed times of day
   * @see DailyTimeWindow
   */
  public static <S extends Scope, C> DailyTimeWindowFactory<
    S,
    C
  > dailyTimeWindow(final TimeFactory startTime, final TimeFactory endTime) {
    return dailyTimeWindow(constant(startTime), constant(endTime));
  }

  /**
   * @param startTime the time of day the window opens, inclusive, read on every
   *                  query
   * @param endTime   the time of day the window closes, exclusive, read on
   *                  every query
   * @return a factory for a {@link DailyTimeWindow}
   * @see DailyTimeWindow
   */
  public static <S extends Scope, C> DailyTimeWindowFactory<
    S,
    C
  > dailyTimeWindow(
    final ValueFactory<? super S, ? super C, LocalTime> startTime,
    final ValueFactory<? super S, ? super C, LocalTime> endTime
  ) {
    return new DailyTimeWindowFactory<>(startTime, endTime);
  }

  /**
   * @param timeWindow the window tested against
   * @return a {@link MappingFactory} for a {@link WithinTimeWindow}: a
   *         predicate of a date-time, for use as the predicate of a condition
   * @see WithinTimeWindow
   */
  public static <S extends Scope, C> WithinTimeWindowFactory<
    S,
    C
  > withinTimeWindow(
    final Factory<? super S, ? extends TimeWindow<? super C>> timeWindow
  ) {
    return new WithinTimeWindowFactory<>(timeWindow);
  }

  /**
   * @param dateTime   the date-time the duration is counted from, such as an
   *                   arrival time
   * @param timeWindow the window waited for
   * @return a {@link MappingFactory} for a {@link DurationUntilTimeWindowOpens}
   * @see DurationUntilTimeWindowOpens
   */
  public static <S extends Scope, C, T> DurationUntilTimeWindowOpensFactory<
    S,
    C,
    T
  > durationUntilTimeWindowOpens(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends LocalDateTime
    > dateTime,
    final Factory<? super S, ? extends TimeWindow<? super C>> timeWindow
  ) {
    return new DurationUntilTimeWindowOpensFactory<>(dateTime, timeWindow);
  }

  /**
   * @param dateTime   the date-time the duration is counted from, such as the
   *                   current date-time
   * @param timeWindow the window waited for
   * @return a {@link ValueFactory} for a {@link DurationUntilTimeWindowOpens}
   * @see DurationUntilTimeWindowOpens
   */
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    Duration
  > durationUntilTimeWindowOpens(
    final ValueFactory<? super S, ? super C, ? extends LocalDateTime> dateTime,
    final Factory<? super S, ? extends TimeWindow<? super C>> timeWindow
  ) {
    return new MappingAsValueFactory<S, C, Duration>(
      new DurationUntilTimeWindowOpensFactory<S, C, Void>(
        new ValueAsMappingFactory<S, C, LocalDateTime>(dateTime),
        timeWindow
      )
    );
  }
}
