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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.time.Factories.time;
import static uk.ac.ox.poseidon.core.time.windows.Factories.dailyTimeWindow;
import static uk.ac.ox.poseidon.core.time.windows.Factories.durationUntilTimeWindowOpens;
import static uk.ac.ox.poseidon.core.time.windows.Factories.withinTimeWindow;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * The helpers test a date-time against a time window, and ask how long until it
 * opens, keeping the shape (value or mapping) of the date-time they are given.
 */
class FactoriesTest {

  private static final LocalDateTime NOON = LocalDateTime.of(2026, 3, 1, 12, 0);

  /** A window that is open until the given date-time, and never again. */
  private static final TimeWindow<Object> ONE_OFF = new TimeWindow<>() {
    @Override
    public boolean isOpenAt(
      final Object context,
      final LocalDateTime dateTime
    ) {
      return dateTime.isBefore(NOON);
    }

    @Override
    public Optional<LocalDateTime> earliestOpenDateTime(
      final Object context,
      final LocalDateTime dateTime
    ) {
      return isOpenAt(context, dateTime)
        ? Optional.of(dateTime)
        : Optional.empty();
    }
  };

  private final ValueFactory<Scope, Object, LocalDateTime> dateTime = scope ->
    context ->
      NOON;

  private final MappingFactory<Scope, Object, Integer, LocalDateTime> arrival =
    scope -> (context, hours) -> NOON.plusHours(hours);

  @Test
  void withinTimeWindowTestsADateTimeUsedAsAPredicateOfACondition() {
    final Function<Object, Boolean> noonIsInTheDaytime = condition(
      dateTime,
      withinTimeWindow(dailyTimeWindow(time(6, 0, 0), time(18, 0, 0)))
    ).get(GLOBAL_SCOPE);
    final Function<Object, Boolean> noonIsInTheNight = condition(
      dateTime,
      withinTimeWindow(dailyTimeWindow(time(20, 0, 0), time(6, 0, 0)))
    ).get(GLOBAL_SCOPE);

    assertThat(noonIsInTheDaytime.apply(null)).isTrue();
    assertThat(noonIsInTheNight.apply(null)).isFalse();
  }

  @Test
  void durationUntilTimeWindowOpensIsZeroWhenOpenAndTheWaitWhenClosed() {
    final var night = dailyTimeWindow(time(20, 0, 0), time(6, 0, 0));

    final Function<Object, Duration> fromNoon = durationUntilTimeWindowOpens(
      dateTime,
      night
    ).get(GLOBAL_SCOPE);

    assertThat(fromNoon.apply(null)).isEqualTo(Duration.ofHours(8));
  }

  @Test
  void durationUntilTimeWindowOpensTakesAnArrivalTimeMapping() {
    final var night = dailyTimeWindow(time(20, 0, 0), time(6, 0, 0));

    final BiFunction<Object, Integer, Duration> fromArrival =
      durationUntilTimeWindowOpens(arrival, night).get(GLOBAL_SCOPE);

    assertThat(fromArrival.apply(null, 0)).isEqualTo(Duration.ofHours(8));
    assertThat(fromArrival.apply(null, 8)).isEqualTo(Duration.ZERO);
    assertThat(fromArrival.apply(null, 17)).isEqualTo(Duration.ZERO);
    assertThat(fromArrival.apply(null, 18)).isEqualTo(Duration.ofHours(14));
  }

  @Test
  void durationUntilTimeWindowOpensNamesTheWindowAndDateTimeWhenItNeverOpens() {
    final Function<Object, Duration> fromNoon = durationUntilTimeWindowOpens(
      dateTime,
      scope -> ONE_OFF
    ).get(GLOBAL_SCOPE);

    assertThatThrownBy(() -> fromNoon.apply(null))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(NOON.toString())
      .hasMessageContaining(ONE_OFF.getClass().getName());
  }
}
