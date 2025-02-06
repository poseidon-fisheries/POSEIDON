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

package uk.ac.ox.poseidon.regulations.predicates.temporal;

import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.LocalDate;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The BetweenDates class implements a predicate to determine if a given {@code Action} occurs
 * within a specified date range, defined by a start and end date.
 * <p>
 * Features and Behavior: - The date range is inclusive of both the start and end dates. - The
 * {@code Action} is considered to meet the predicate if any of the following conditions hold: 1.
 * The {@code Action}'s start date falls within the specified range. 2. The {@code Action}'s end
 * date falls within the specified range. 3. The range fully encompasses the {@code Action}'s
 * duration.
 * <p>
 * Immutability: - This class is immutable. Both the start and end dates are required to be
 * non-null.
 * <p>
 * Preconditions: - The start date must not be after the end date.
 * <p>
 * Thread Safety: - Instances of this class are thread-safe as long as the {@code Action} instances
 * provided are used in a thread-safe manner.
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
        final LocalDate actionStart = action.getStart().toLocalDate();
        final LocalDate actionEnd = action.getEnd().toLocalDate();
        return betweenDates(actionStart) || betweenDates(actionEnd) ||
            (actionStart.isBefore(start) && actionEnd.isAfter(end));
    }

    private boolean betweenDates(final LocalDate date) {
        return !(date.isBefore(start) || date.isAfter(end));
    }

}
