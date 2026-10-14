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

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.function.Supplier;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.time.DurationFactory;
import uk.ac.ox.poseidon.core.time.PeriodFactory;

/**
 * Factories for {@link uk.ac.ox.poseidon.core.providers.Provider}s that read
 * the simulation's current or upcoming date/time from its
 * {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule}.
 */
public class Factories {

  private Factories() {}

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link CurrentDate}
   * @see CurrentDate
   */
  public static CurrentDateFactory currentDate() {
    return new CurrentDateFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link CurrentYear}
   * @see CurrentYear
   */
  public static CurrentYearFactory currentYear() {
    return new CurrentYearFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link CurrentDateTime}
   * @see CurrentDateTime
   */
  public static CurrentDateTimeFactory currentDateTime() {
    return new CurrentDateTimeFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link CurrentDayOfWeek}
   * @see CurrentDayOfWeek
   */
  public static CurrentDayOfWeekFactory currentDayOfWeek() {
    return new CurrentDayOfWeekFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link CurrentTime}
   * @see CurrentTime
   */
  public static CurrentTimeFactory currentTime() {
    return new CurrentTimeFactory();
  }

  /**
   * @param times the times of day
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link NextOccurrenceOfTimeOfDay} of the given times strictly after
   *         the current date-time
   * @see NextOccurrenceOfTimeOfDay
   */
  @SafeVarargs
  public static NextOccurrenceOfTimeOfDayFactory<SimulationScope> nextOccurrenceOf(
    final Factory<? super SimulationScope, ? extends LocalTime>... times
  ) {
    return new NextOccurrenceOfTimeOfDayFactory<>(
      currentDateTime(),
      List.of(times)
    );
  }

  /**
   * @param referenceDateTime supplies the date-time the occurrence must be
   *                          strictly after
   * @param times             the times of day
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link NextOccurrenceOfTimeOfDay} of the given times strictly after
   *         the reference
   * @see NextOccurrenceOfTimeOfDay
   */
  @SafeVarargs
  public static <S extends Scope> NextOccurrenceOfTimeOfDayFactory<S> nextOccurrenceAfter(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final Factory<? super S, ? extends LocalTime>... times
  ) {
    return new NextOccurrenceOfTimeOfDayFactory<>(
      referenceDateTime,
      List.of(times)
    );
  }

  /**
   * @param referenceDateTime supplies the date-time to compute the time left
   *                          until; the result is negative once it has passed
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DurationUntil} from the current date-time
   * @see DurationUntil
   */
  public static <S extends SimulationScope> DurationUntilFactory<S> durationUntil(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime
  ) {
    return new DurationUntilFactory<>(currentDateTime(), referenceDateTime);
  }

  /**
   * @param referenceDateTime supplies the date-time to add to
   * @param amountToAdd       supplies the amount to add
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeAfter}
   * @see DateTimeAfter
   */
  public static <S extends Scope> DateTimeAfterFactory<S> dateTimeAfter(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final Factory<
      ? super S,
      ? extends Supplier<? extends TemporalAmount>
    > amountToAdd
  ) {
    return new DateTimeAfterFactory<>(referenceDateTime, amountToAdd);
  }

  /**
   * @param referenceDateTime supplies the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeAfter} over a constant amount
   * @see DateTimeAfter
   */
  public static <S extends Scope> DateTimeAfterFactory<S> dateTimeAfter(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final DurationFactory amountToAdd
  ) {
    return dateTimeAfter(referenceDateTime, constant(amountToAdd));
  }

  /**
   * @param referenceDateTime supplies the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeAfter} over a constant amount
   * @see DateTimeAfter
   */
  public static <S extends Scope> DateTimeAfterFactory<S> dateTimeAfter(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final PeriodFactory amountToAdd
  ) {
    return dateTimeAfter(referenceDateTime, constant(amountToAdd));
  }

  /**
   * @param referenceDateTime supplies the date-time to subtract from
   * @param amountToSubtract  supplies the amount to subtract
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeBefore}
   * @see DateTimeBefore
   */
  public static <S extends Scope> DateTimeBeforeFactory<S> dateTimeBefore(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final Factory<
      ? super S,
      ? extends Supplier<? extends TemporalAmount>
    > amountToSubtract
  ) {
    return new DateTimeBeforeFactory<>(referenceDateTime, amountToSubtract);
  }

  /**
   * @param referenceDateTime supplies the date-time to subtract from
   * @param amountToSubtract  the amount to subtract
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeBefore} over a constant amount
   * @see DateTimeBefore
   */
  public static <S extends Scope> DateTimeBeforeFactory<S> dateTimeBefore(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final DurationFactory amountToSubtract
  ) {
    return dateTimeBefore(referenceDateTime, constant(amountToSubtract));
  }

  /**
   * @param referenceDateTime supplies the date-time to subtract from
   * @param amountToSubtract  the amount to subtract
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link DateTimeBefore} over a constant amount
   * @see DateTimeBefore
   */
  public static <S extends Scope> DateTimeBeforeFactory<S> dateTimeBefore(
    final Factory<
      ? super S,
      ? extends Supplier<LocalDateTime>
    > referenceDateTime,
    final PeriodFactory amountToSubtract
  ) {
    return dateTimeBefore(referenceDateTime, constant(amountToSubtract));
  }
}
