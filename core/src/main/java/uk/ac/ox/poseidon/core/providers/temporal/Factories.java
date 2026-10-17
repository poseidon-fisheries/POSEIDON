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
import uk.ac.ox.poseidon.core.CurrentDateTimeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.HasDateTimeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.StartingDateTimeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.time.DurationFactory;
import uk.ac.ox.poseidon.core.time.PeriodFactory;

/**
 * Factories for values that read the simulation's current or upcoming date/time
 * from its {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule}, and for
 * the temporal arithmetic over such values.
 */
public class Factories {

  private Factories() {}

  /**
   * @return a {@link ValueFactory} for a
   *         {@link CurrentDate}
   * @see CurrentDate
   */
  public static <C> CurrentDateFactory<C> currentDate() {
    return new CurrentDateFactory<>(currentDateTime());
  }

  /**
   * @return a {@link ValueFactory} for a
   *         {@link CurrentYear}
   * @see CurrentYear
   */
  public static <C> CurrentYearFactory<C> currentYear() {
    return new CurrentYearFactory<>(currentDateTime());
  }

  /**
   * @return a {@link ValueFactory} for a
   *         {@link CurrentDateTime}
   * @see CurrentDateTime
   */
  public static CurrentDateTimeFactory currentDateTime() {
    return new CurrentDateTimeFactory();
  }

  /**
   * @return a {@link ValueFactory} for a {@link HasDateTime}: whether the
   *         simulation's clock has a date-time
   * @see HasDateTime
   */
  public static HasDateTimeFactory hasDateTime() {
    return new HasDateTimeFactory();
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
   * @return a {@link ValueFactory} for a
   *         {@link CurrentDayOfWeek}
   * @see CurrentDayOfWeek
   */
  public static <C> CurrentDayOfWeekFactory<C> currentDayOfWeek() {
    return new CurrentDayOfWeekFactory<>(currentDateTime());
  }

  /**
   * @return a {@link ValueFactory} for a
   *         {@link CurrentTime}
   * @see CurrentTime
   */
  public static <C> CurrentTimeFactory<C> currentTime() {
    return new CurrentTimeFactory<>(currentDateTime());
  }

  /**
   * @param times the times of day
   * @return a {@link ValueFactory} for a
   *         {@link NextOccurrenceOfTimeOfDay} of the given times strictly after
   *         the current date-time
   * @see NextOccurrenceOfTimeOfDay
   */
  @SafeVarargs
  public static <C> NextOccurrenceOfTimeOfDayFactory<
    SimulationScope,
    C
  > nextOccurrenceOf(
    final Factory<? super SimulationScope, ? extends LocalTime>... times
  ) {
    return new NextOccurrenceOfTimeOfDayFactory<>(
      currentDateTime(),
      ImmutableList.copyOf(times)
    );
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
   * @param referenceDateTime the date-time to compute the time left
   *                          until; the result is negative once it has passed
   * @return a {@link ValueFactory} for a
   *         {@link DurationUntil} from the current date-time
   * @see DurationUntil
   */
  public static <S extends SimulationScope, C> DurationUntilFactory<
    S,
    C
  > durationUntil(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime
  ) {
    return new DurationUntilFactory<>(currentDateTime(), referenceDateTime);
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeAfter}
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> DateTimeAfterFactory<S, C> dateTimeAfter(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends TemporalAmount
    > amountToAdd
  ) {
    return new DateTimeAfterFactory<>(referenceDateTime, amountToAdd);
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeAfter} over a constant amount
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> DateTimeAfterFactory<S, C> dateTimeAfter(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
    final DurationFactory amountToAdd
  ) {
    return dateTimeAfter(referenceDateTime, constant(amountToAdd));
  }

  /**
   * @param referenceDateTime the date-time to add to
   * @param amountToAdd       the amount to add
   * @return a {@link ValueFactory} for a
   *         {@link DateTimeAfter} over a constant amount
   * @see DateTimeAfter
   */
  public static <S extends Scope, C> DateTimeAfterFactory<S, C> dateTimeAfter(
    final ValueFactory<? super S, ? super C, LocalDateTime> referenceDateTime,
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
