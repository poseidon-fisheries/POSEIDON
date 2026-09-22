/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.predicates.temporal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import uk.ac.ox.poseidon.regulations.TemporalAction;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Predicate matching temporal actions whose start or end date falls within an inclusive
 * {@code [start, end]} range of {@link MonthDay}s that repeats every calendar year, or whose
 * duration fully covers the range. A range where {@code end} is before {@code start} (e.g.
 * November 1 to February 28) is treated as spanning the year boundary rather than empty.
 */
@Getter
@ToString
@EqualsAndHashCode
public class BetweenYearlyDates implements Predicate<TemporalAction<?>> {

    @NonNull private final MonthDay start;
    @NonNull private final MonthDay end;
    private final boolean yearSpanning;

    /**
     * @param start the inclusive start of the range; {@code end} before {@code start} makes the
     *              range year-spanning rather than invalid
     * @param end   the inclusive end of the range
     */
    public BetweenYearlyDates(
        final MonthDay start,
        final MonthDay end
    ) {
        this.start = checkNotNull(start);
        this.end = checkNotNull(end);
        this.yearSpanning = end.isBefore(start);
    }

    @Override
    public boolean test(final TemporalAction<?> action) {
        checkArgument(!action.getStartDateTime().isAfter(action.getEndDateTime()));
        return insideRange(action.getStartDateTime()) ||
            insideRange(action.getEndDateTime()) ||
            coversRange(action.getStartDateTime(), action.getEndDateTime());
    }

    /**
     * Whether the action's duration fully covers the range, e.g. a multi-month trip encompassing
     * a whole month-long closure.
     */
    private boolean coversRange(
        final LocalDateTime actionStart,
        final LocalDateTime actionEnd
    ) {
        final MonthDay startDay = MonthDay.from(actionStart);
        final MonthDay endDay = MonthDay.from(actionEnd);
        return switch (actionEnd.getYear() - actionStart.getYear()) {
            // If the action is within a single year, it can only cover a range that isn't year
            // spanning
            case 0 -> !yearSpanning && startDay.isBefore(start) && endDay.isAfter(end);
            // action spans year boundary, it can only cover a range that is year spanning
            case 1 -> yearSpanning && startDay.isBefore(start) && endDay.isAfter(end);
            // if action spans more than a year, it necessarily covers any range
            default -> true;
        };
    }

    private boolean insideRange(final LocalDateTime dateTime) {
        final MonthDay monthDay = MonthDay.from(dateTime);
        return yearSpanning
            ? !monthDay.isAfter(end) || !monthDay.isBefore(start)
            : !monthDay.isBefore(start) && !monthDay.isAfter(end);
    }
}
