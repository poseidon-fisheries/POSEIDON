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

package uk.ac.ox.poseidon.regulations.predicates.temporal;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.time.MonthDayFactory;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Factories for predicates over a {@link uk.ac.ox.poseidon.regulations.TemporalAction}'s
 * date/time span: a fixed date range, a range repeating every calendar year, and a single year.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link BetweenDates}
     * @see BetweenDatesFactory
     */
    public static <S extends Scope> BetweenDatesFactory<S> betweenDates(
        final Factory<? super S, ? extends LocalDate> startDate,
        final Factory<? super S, ? extends LocalDate> endDate
    ) {
        return new BetweenDatesFactory<>(startDate, endDate);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link BetweenYearlyDates}
     * @see BetweenYearlyDatesFactory
     */
    public static <S extends Scope> BetweenYearlyDatesFactory<S> betweenYearlyDates(
        final Factory<? super S, ? extends MonthDay> start,
        final Factory<? super S, ? extends MonthDay> end
    ) {
        return new BetweenYearlyDatesFactory<>(start, end);
    }

    /**
     * @param start the inclusive start of the range, parsed via {@link MonthDayFactory#parse}
     * @param end   the inclusive end of the range, parsed via {@link MonthDayFactory#parse}
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link BetweenYearlyDates}, resolvable against any {@link Scope}
     * @see BetweenYearlyDatesFactory
     */
    public static BetweenYearlyDatesFactory<Scope> betweenYearlyDates(
        final CharSequence start,
        final CharSequence end
    ) {
        return new BetweenYearlyDatesFactory<>(
            MonthDayFactory.parse(start),
            MonthDayFactory.parse(end)
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an {@link InYear}
     * @see InYearFactory
     */
    public static InYearFactory inYear(final int year) {
        return new InYearFactory(year);
    }

}
