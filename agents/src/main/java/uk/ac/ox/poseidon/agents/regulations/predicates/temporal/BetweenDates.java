/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.regulations.predicates.temporal;

import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The BetweenDates class implements a predicate to determine whether an {@code Action}'s start or
 * end date falls within a specified range of dates. It uses {@code LocalDate} for comparison of
 * date ranges.
 * <p>
 * This class is immutable and requires two {@code LocalDate} objects, representing the inclusive
 * start and end dates for the range.
 * <p>
 * The predicate evaluates {@code true} if either the start or the end date of the {@code Action}
 * lies within the specified date range. Otherwise, it evaluates to {@code false}.
 */
@Getter
public final class BetweenDates implements Predicate<Action> {

    @NonNull private final LocalDate start;
    @NonNull private final LocalDate end;

    public BetweenDates(
        @NonNull final LocalDate start,
        @NonNull final LocalDate end
    ) {
        this.start = start;
        this.end = end;
        checkArgument(
            !start.isAfter(end),
            "Start date (%s) must not be after end date (%s).",
            start,
            end
        );
    }

    @Override
    public boolean test(final Action action) {
        return betweenDates(action.getStart()) || betweenDates(action.getEnd());
    }

    private boolean betweenDates(final LocalDateTime dateTime) {
        final var date = dateTime.toLocalDate();
        return !(date.isBefore(start) || date.isAfter(end));
    }

}
