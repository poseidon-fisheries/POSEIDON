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
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The BetweenYearlyDates class implements a predicate to determine whether an {@code Action}'s
 * start and end dates overlap with a specified range of dates, repeating every calendar year.
 * <p>
 * The range is defined using two {@code MonthDay} instances representing the inclusive start and
 * end of the range. The class accounts for whether the range crosses the boundary of a calendar
 * year (year spanning).
 * <p>
 * Key behavior: - If the date range does not span a year (start is before or equal to end within
 * the same year), the predicate checks if the action's dates fall within this range. - If the date
 * range spans a year (start is after end crossing into the next calendar year), actions are tested
 * with respect to the wrapped year-spanning interval. - The predicate will return {@code true} if
 * the action's start or end date lies inside the range, or if the range fully encompasses the
 * action's entire duration.
 * <p>
 * This class is immutable, with precondition checks performed to ensure valid input parameters.
 */
@Getter
@ToString
@EqualsAndHashCode
public class BetweenYearlyDates implements Predicate<Action> {

    @NonNull private final MonthDay start;
    @NonNull private final MonthDay end;
    private final boolean yearSpanning;

    public BetweenYearlyDates(
        final MonthDay start,
        final MonthDay end
    ) {
        this.start = checkNotNull(start);
        this.end = checkNotNull(end);
        this.yearSpanning = end.isBefore(start);
    }

    @Override
    public boolean test(final Action action) {
        checkArgument(action.getStartDateTime().isBefore(action.getEndDateTime()));
        return insideRange(action.getStartDateTime()) || insideRange(action.getEndDateTime()) ||
            coversRange(action.getStartDateTime(), action.getEndDateTime());
    }

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
