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

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Factories {

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

}
