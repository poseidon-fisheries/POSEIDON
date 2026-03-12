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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import static java.time.Period.*;

public class Factories {

    public static final PeriodFactory DAILY = period(ofDays(1));
    public static final PeriodFactory MONTHLY = period(ofMonths(1));
    public static final PeriodFactory YEARLY = period(ofYears(1));

    public static final DurationFactory ONE_SECOND = seconds(1);
    public static final DurationFactory ONE_MINUTE = minutes(1);
    public static final DurationFactory ONE_HOUR = hours(1);
    public static final DurationFactory ONE_DAY = days(1);
    public static final PeriodFactory ONE_MONTH = MONTHLY;
    public static final PeriodFactory ONE_YEAR = YEARLY;

    public static DurationFactory duration(
        final long days,
        final long hours,
        final long minutes,
        final long seconds
    ) {
        return new DurationFactory(days, hours, minutes, seconds);
    }

    public static DurationFactory duration(final String iso8601Duration) {
        return new DurationFactory(iso8601Duration);
    }

    public static DurationFactory days(final long days) {
        return new DurationFactory(days, 0, 0, 0);
    }

    public static DurationFactory hours(final long hours) {
        return new DurationFactory(0, hours, 0, 0);
    }

    public static DurationFactory minutes(final long minutes) {
        return new DurationFactory(0, 0, minutes, 0);
    }

    public static DurationFactory seconds(final long seconds) {
        return new DurationFactory(0, 0, 0, seconds);
    }

    public static PeriodFactory period(final String iso8601Period) {
        return new PeriodFactory(iso8601Period);
    }

    public static PeriodFactory period(final Period period) {
        return new PeriodFactory(period);
    }

    public static DateTimeFactory startOfToday() {
        return startOf(LocalDate.now());
    }

    public static DateTimeFactory now() {
        return dateTime(LocalDateTime.now());
    }

    public static DateTimeFactory startOf(final LocalDate date) {
        return dateTime(date.atStartOfDay());
    }

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

    public static TimeFactory time(
        final int hour,
        final int minute,
        final int second
    ) {
        return new TimeFactory(hour, minute, second);
    }

    public static <S extends Scope> DateTimeBeforeFactory<S> dateTimeBefore(
        final Factory<? super S, ? extends LocalDateTime> referenceDateTime,
        final Factory<? super S, ? extends TemporalAmount> temporalAmount
    ) {
        return new DateTimeBeforeFactory<>(referenceDateTime, temporalAmount);
    }

    public static <S extends Scope> DateTimeAfterFactory<S> dateTimeAfter(
        final Factory<? super S, ? extends LocalDateTime> referenceDateTime,
        final Factory<? super S, ? extends TemporalAmount> amountToAdd
    ) {
        return new DateTimeAfterFactory<>(referenceDateTime, amountToAdd);
    }

    public static DateTimeAfterStartingFactory dateTimeAfterStarting(
        final Factory<? super SimulationScope, ? extends TemporalAmount> amountToAdd
    ) {
        return new DateTimeAfterStartingFactory(amountToAdd);
    }

}
