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

package uk.ac.ox.poseidon.regulations.core.conditions;

import java.time.LocalDate;
import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkNotNull;

public class BetweenYearlyDates extends AbstractDateCondition {

    private final MonthDay beginning;
    private final MonthDay end;
    private final boolean yearSpanning;

    public BetweenYearlyDates(
        final MonthDay beginning,
        final MonthDay end
    ) {
        this.beginning = checkNotNull(beginning);
        this.end = checkNotNull(end);
        this.yearSpanning = end.isBefore(beginning);
    }

    public MonthDay getBeginning() {
        return beginning;
    }

    public MonthDay getEnd() {
        return end;
    }

    public boolean isYearSpanning() {
        return yearSpanning;
    }

    @Override
    boolean test(final LocalDate date) {
        return test(MonthDay.from(date));
    }

    boolean test(final MonthDay monthDay) {
        final boolean outsideRange = yearSpanning
            ? monthDay.isAfter(end) && monthDay.isBefore(beginning)
            : monthDay.isBefore(beginning) || monthDay.isAfter(end);
        return !outsideRange;
    }

    @Override
    public String toString() {
        return "BetweenYearlyDates{" +
            "beginning=" + beginning +
            ", end=" + end +
            ", yearSpanning=" + yearSpanning +
            '}';
    }
}
