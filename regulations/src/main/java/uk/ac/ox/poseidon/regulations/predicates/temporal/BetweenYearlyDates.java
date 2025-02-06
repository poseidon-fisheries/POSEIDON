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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.MonthDay;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@ToString
@EqualsAndHashCode
public class BetweenYearlyDates implements Predicate<Action> {

    private final MonthDay start;
    private final MonthDay end;
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
        return test(MonthDay.from(action.getStart())) || test(MonthDay.from(action.getEnd()));
    }

    private boolean test(final MonthDay monthDay) {
        final boolean outsideRange = yearSpanning
            ? monthDay.isAfter(end) && monthDay.isBefore(start)
            : monthDay.isBefore(start) || monthDay.isAfter(end);
        return !outsideRange;
    }
}
