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

import lombok.Getter;
import lombok.NonNull;
import org.threeten.extra.Interval;
import uk.ac.ox.poseidon.regulations.TemporalAction;

import java.time.LocalDate;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.ZoneOffset.UTC;

/**
 * Predicate matching temporal actions that overlap a given inclusive date range.
 * <p>
 * The provided dates are converted to a UTC interval
 * {@code [start at 00:00, day after end at 00:00)}. An action matches when its
 * interval overlaps this range. Touching only at an endpoint does not match.
 * Zero-duration actions match when their instant falls inside the range.
 * <p>
 * Preconditions: the start date must not be after the end date.
 */
@Getter
public final class BetweenDates implements Predicate<TemporalAction<?>> {

    @NonNull private final Interval interval;

    public BetweenDates(
        @NonNull final LocalDate start,
        @NonNull final LocalDate end
    ) {
        checkArgument(
            !start.isAfter(end),
            "Start date (%s) must not be after end date (%s).",
            start,
            end
        );
        this.interval =
            Interval.of(
                start.atStartOfDay().toInstant(UTC),
                end.plusDays(1).atStartOfDay().toInstant(UTC)
            );
    }

    @Override
    public boolean test(final TemporalAction<?> action) {
        return action.getInterval().overlaps(this.interval);
    }

}
