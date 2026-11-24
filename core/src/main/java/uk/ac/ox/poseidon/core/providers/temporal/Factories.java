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

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import uk.ac.ox.poseidon.core.Clock;
import uk.ac.ox.poseidon.core.ClockFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.NowFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.StartingDateTimeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MappingAsValueFactory;
import uk.ac.ox.poseidon.core.functions.ValueAsMappingFactory;
import uk.ac.ox.poseidon.core.time.DurationFactory;
import uk.ac.ox.poseidon.core.time.PeriodFactory;

/**
 * Factories for values that read the simulation's clock, and for the temporal
 * arithmetic over date-time values.
 */
@SuppressWarnings({ "varargs", "overloads" })
public final class Factories {

  private Factories() {}

  /**
   * @return a {@link ClockFactory} for the simulation's {@link Clock}, which
   *         components hold to read the present
   * @see ClockFactory
   */
  public static ClockFactory clock() {
    return new ClockFactory();
  }

  /**
   * @return a {@link ValueFactory} for a {@link Now}: the present date-time
   * @see Now
   */
  public static NowFactory now() {
    return new NowFactory();
  }

  /**
   * @param dateTime the date-time to take the date of
   * @return a {@link ValueFactory} for a {@link DateOf}
   * @see DateOf
   */
  public static <S extends Scope, C> DateOfFactory<S, C> dateOf(
    final ValueFactory<? super S, ? super C, LocalDateTime> dateTime
  ) {
    return new DateOfFactory<>(dateTime);
  }

  /**
   * @param dateTime the date-time to take the year of
   * @return a {@link ValueFactory} for a {@link YearOf}
   * @see YearOf
   */
  public static <S extends Scope, C> YearOfFactory<S, C> yearOf(
    final ValueFactory<? super S, ? super C, LocalDateTime> dateTime
  ) {
    return new YearOfFactory<>(dateTime);
  }

  /**
   * @param dateTime the date-time to take the time of day of
   * @return a {@link ValueFactory} for a {@link TimeOfDayOf}
   * @see TimeOfDayOf
   */
  public static <S extends Scope, C> TimeOfDayOfFactory<S, C> timeOfDayOf(
    final ValueFactory<? super S, ? super C, LocalDateTime> dateTime
  ) {
    return new TimeOfDayOfFactory<>(dateTime);
  }

  /**
   * @param dateTime the date-time to take the day of the week of
   * @return a {@link ValueFactory} for a {@link DayOfWeekOf}
   * @see DayOfWeekOf
   */
  public static <S extends Scope, C> DayOfWeekOfFactory<S, C> dayOfWeekOf(
    final ValueFactory<? super S, ? super C, LocalDateTime> dateTime
  ) {
    return new DayOfWeekOfFactory<>(dateTime);
  }

  /** @return the date of {@link #now()} */
  public static <C> DateOfFactory<SimulationScope, C> today() {
    return dateOf(now());
  }

  /** @return the year of {@link #now()} */
  public static <C> YearOfFactory<SimulationScope, C> thisYear() {
    return yearOf(now());
  }

  /** @return the time of day of {@link #now()} */
  public static <C> TimeOfDayOfFactory<SimulationScope, C> timeOfDay() {
    return timeOfDayOf(now());
  }

  /** @return the day of the week of {@link #now()} */
  public static <C> DayOfWeekOfFactory<SimulationScope, C> dayOfWeek() {
    return dayOfWeekOf(now());
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for the
   *         date-time the simulation's clock starts at
   * @see StartingDateTimeFactory
   */
  public static StartingDateTimeFactory startingDateTime() {
    return new StartingDateTimeFactory();
  }

  /**
   * @param referenceDateTime the date-time the occurrence must be
   *                          strictly after
   * @param times             the times of day
   * @return a {@link ValueFactory} for a
   *         {@link NextOccurrenceOfTimeOfDay} of the given times strictly after
   *         the reference
   * @see NextOccurrenceOfTimeOfDay
   */
  @SafeVarargs
  public static <S extends Scope, C> NextOccurrenceOfTimeOfDayFactory<
    S,
    C
  > nextOccurrenceAfter(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final Factory<? super S, ? extends LocalTime>... times
  ) {
    return new NextOccurrenceOfTimeOfDayFactory<>(
      referenceDateTime,
      ImmutableList.copyOf(times)
    );
  }

  /**
   * @param startDateTime the date-time to count from
   * @param endDateTime   the date-time to count to; the result is negative once
   *                      it is before the start
   * @return a {@link ValueFactory} for a
   *         {@link DurationBetween}
   * @see DurationBetween
   */
  public static <S extends Scope, C> DurationBetweenFactory<
    S,
    C
  > durationBetween(
    final ValueFactory<? super S, ? super C, LocalDateTime> startDateTime,
    final ValueFactory<? super S, ? super C, LocalDateTime> endDateTime
  ) {
    return new DurationBetweenFactory<>(startDateTime, endDateTime);
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link MappingFactory} for a {@link DateTimeAfter}
   * @see DateTimeAfter
   */
  public static <S extends Scope, C, T> DateTimeAfterFactory<
    S,
    C,
    T
  > dateTimeAfter(
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends LocalDateTime
    > referenceDateTime,
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends TemporalAmount
    > amountToAdd
  ) {
    return new DateTimeAfterFactory<>(referenceDateTime, amountToAdd);
  }

  /**
   * @param referenceDateTime the date-time to add to, such as the current
   *                          date-time
   * @param amountToAdd       the amount to add, which depends on the thing
   *                          mapped
   * @return a {@link MappingFactory} for a {@link DateTimeAfter}
   * @see DateTimeAfter
   */
  public static <S extends Scope, C, T> DateTimeAfterFactory<
    S,
    C,
    T
  > dateTimeAfter(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime,
    final MappingFactory<
      ? super S,
      ? super C,
      ? super T,
      ? extends TemporalAmount
    > amountToAdd
  ) {
    return dateTimeAfter(
      new ValueAsMappingFactory<S, C, LocalDateTime>(referenceDateTime),
      amountToAdd
    );
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a {@link DateTimeAfter}
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    LocalDateTime
  > dateTimeAfter(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime,
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends TemporalAmount
    > amountToAdd
  ) {
    return new MappingAsValueFactory<S, C, LocalDateTime>(
      new DateTimeAfterFactory<S, C, Void>(
        new ValueAsMappingFactory<S, C, LocalDateTime>(referenceDateTime),
        new ValueAsMappingFactory<S, C, TemporalAmount>(amountToAdd)
      )
    );
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a {@link DateTimeAfter} over a constant
   *         amount
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    LocalDateTime
  > dateTimeAfter(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime,
    final DurationFactory amountToAdd
  ) {
    return dateTimeAfter(referenceDateTime, constant(amountToAdd));
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a {@link DateTimeAfter} over a constant
   *         amount
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> MappingAsValueFactory<
    S,
    C,
    LocalDateTime
  > dateTimeAfter(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime,
    final PeriodFactory amountToAdd
  ) {
    return dateTimeAfter(referenceDateTime, constant(amountToAdd));
  }

  /**
   * @param referenceDateTime the date-time to subtract from
   * @param amountToSubtract  the amount to subtract
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeBefore}
   * @see DateTimeBefore
   */
  public static <S extends Scope, C> DateTimeBeforeFactory<S, C> dateTimeBefore(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends TemporalAmount
    > amountToSubtract
  ) {
    return new DateTimeBeforeFactory<>(referenceDateTime, amountToSubtract);
  }

  /**
   * @param referenceDateTime the date-time to subtract from
   * @param amountToSubtract  the amount to subtract
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeBefore} over a constant amount
   * @see DateTimeBefore
   */
  public static <S extends Scope, C> DateTimeBeforeFactory<S, C> dateTimeBefore(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final DurationFactory amountToSubtract
  ) {
    return dateTimeBefore(referenceDateTime, constant(amountToSubtract));
  }

  /**
   * @param referenceDateTime the date-time to subtract from
   * @param amountToSubtract  the amount to subtract
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeBefore} over a constant amount
   * @see DateTimeBefore
   */
  public static <S extends Scope, C> DateTimeBeforeFactory<S, C> dateTimeBefore(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final PeriodFactory amountToSubtract
  ) {
    return dateTimeBefore(referenceDateTime, constant(amountToSubtract));
  }
}
