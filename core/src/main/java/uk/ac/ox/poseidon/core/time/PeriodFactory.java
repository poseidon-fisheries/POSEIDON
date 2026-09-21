/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Period;

/**
 * A {@link GlobalScopeFactory} for a fixed {@link Period}. No separate plain component class
 * here: the produced value is a bare JDK type, with no wrapper to carry documentation. Built via
 * {@link Factories Factories.period(...)}, or the {@code DAILY}/{@code MONTHLY}/{@code YEARLY}/
 * {@code ONE_MONTH}/{@code ONE_YEAR} constants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PeriodFactory extends GlobalScopeFactory<Period> {

    private int years;
    private int months;
    private int days;

    /**
     * @param period the period to decompose into years/months/days
     */
    public PeriodFactory(final Period period) {
        this.years = period.getYears();
        this.months = period.getMonths();
        this.days = period.getDays();
    }

    /**
     * @param iso8601Period an ISO-8601 period string (e.g. {@code "P1Y2M"}), as accepted by
     *                      {@link Period#parse}
     */
    public PeriodFactory(final String iso8601Period) {
        this(Period.parse(iso8601Period));
    }

    @Override
    protected Period newInstance(final Scope scope) {
        return Period.of(years, months, days);
    }
}
