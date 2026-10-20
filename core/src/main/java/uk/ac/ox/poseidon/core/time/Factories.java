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

package uk.ac.ox.poseidon.core.time;

import static uk.ac.ox.poseidon.core.providers.random.Factories.randomNumberGenerator;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;

/**
 * Factories for fixed dates, times, durations, and periods, and for a
 * {@link java.time.LocalDateTime} computed relative to the simulation's start.
 * Date-times that move with the simulation are in
 * {@link uk.ac.ox.poseidon.core.providers.temporal.Factories}.
 */
public final class Factories {

  private Factories() {}

  /**
   * @return a {@link DurationFactory} for the given days/hours/minutes/seconds
   * @see DurationFactory
   */
  public static DurationFactory duration(
    final long days,
    final long hours,
    final long minutes,
    final long seconds
  ) {
    return new DurationFactory(days, hours, minutes, seconds);
  }

  /**
   * @param iso8601Duration an ISO-8601 duration string (e.g. {@code "PT1H30M"})
   * @return a {@link DurationFactory} for the parsed duration
   * @see DurationFactory
   */
  public static DurationFactory duration(final String iso8601Duration) {
    return new DurationFactory(iso8601Duration);
  }

  /**
   * @return a {@link DurationFactory} for the given number of days
   * @see DurationFactory
   */
  public static DurationFactory days(final long days) {
    return new DurationFactory(days, 0, 0, 0);
  }

  /**
   * @return a {@link DurationFactory} for the given number of hours
   * @see DurationFactory
   */
  public static DurationFactory hours(final long hours) {
    return new DurationFactory(0, hours, 0, 0);
  }

  /**
   * @return a {@link DurationFactory} for the given number of minutes
   * @see DurationFactory
   */
  public static DurationFactory minutes(final long minutes) {
    return new DurationFactory(0, 0, minutes, 0);
  }

  /**
   * @return a {@link DurationFactory} for the given number of seconds
   * @see DurationFactory
   */
  public static DurationFactory seconds(final long seconds) {
    return new DurationFactory(0, 0, 0, seconds);
  }

  /**
   * @param iso8601Period an ISO-8601 period string (e.g. {@code "P1Y2M"})
   * @return a {@link PeriodFactory} for the parsed period
   * @see PeriodFactory
   */
  public static PeriodFactory period(final String iso8601Period) {
    return new PeriodFactory(iso8601Period);
  }

  /**
   * @return a {@link PeriodFactory} for the given period
   * @see PeriodFactory
   */
  public static PeriodFactory period(final Period period) {
    return new PeriodFactory(period);
  }

  /**
   * @return a {@link DateTimeFactory} for the start (midnight) of today,
   *         evaluated when this method is called
   * @see DateTimeFactory
   */
  public static DateTimeFactory startOfToday() {
    return startOf(LocalDate.now());
  }

  /**
   * @return a {@link DateTimeFactory} for the current date-time, evaluated when
   *         this method is called
   * @see DateTimeFactory
   */
  public static DateTimeFactory now() {
    return dateTime(LocalDateTime.now());
  }

  /**
   * @param date the date whose start (midnight) the resulting factory produces
   * @return a {@link DateTimeFactory} for the start of the given date
   * @see DateTimeFactory
   */
  public static DateTimeFactory startOf(final LocalDate date) {
    return dateTime(date.atStartOfDay());
  }

  /**
   * @return a {@link DateTimeFactory} for the given date-time
   * @see DateTimeFactory
   */
  public static DateTimeFactory dateTime(final LocalDateTime dateTime) {
    return new DateTimeFactory(
      dateTime.getYear(),
      dateTime.getMonthValue(),
      dateTime.getDayOfMonth(),
      dateTime.getHour(),
      dateTime.getMinute(),
      dateTime.getSecond()
    );
  }

  /**
   * @return a {@link TimeFactory} for the given time of day
   * @see TimeFactory
   */
  public static TimeFactory time(
    final int hour,
    final int minute,
    final int second
  ) {
    return new TimeFactory(hour, minute, second);
  }

  /**
   * @return a {@link DateFactory} for the given date
   * @see DateFactory
   */
  public static DateFactory date(
    final int year,
    final int month,
    final int day
  ) {
    return new DateFactory(year, month, day);
  }

  /**
   * @return a {@link DateFactory} for the given date
   * @see DateFactory
   */
  public static DateFactory date(final LocalDate date) {
    return date(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  /**
   * @param amountToAdd factory for the amount to add to the simulation's
   *                    starting date-time
   * @return a {@link SimulationScope}-scoped factory for a
   *         {@link LocalDateTime} relative to the simulation's start
   * @see DateTimeAfterStartingFactory
   */
  public static DateTimeAfterStartingFactory dateTimeAfterStarting(
    final Factory<? super SimulationScope, ? extends TemporalAmount> amountToAdd
  ) {
    return new DateTimeAfterStartingFactory(amountToAdd, startingDateTime());
  }

  /**
   * @param meanDuration factory for the mean of the exponential distribution
   * @return a {@link SimulationScope}-scoped factory for an
   *         {@link ExponentiallyDistributedDuration} with the resolved mean
   * @see ExponentiallyDistributedDuration
   */
  public static ExponentiallyDistributedDurationSupplierFactory exponentiallyDistributedDuration(
    final Factory<? super SimulationScope, ? extends Duration> meanDuration
  ) {
    return new ExponentiallyDistributedDurationSupplierFactory(
      meanDuration,
      randomNumberGenerator()
    );
  }
}
