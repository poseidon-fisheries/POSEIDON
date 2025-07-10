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

import lombok.*;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.LocalDate;
import java.time.Period;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PeriodFactory extends GlobalScopeFactory<Period> {

    public static final PeriodFactory DAILY = new PeriodFactory("P1D");
    public static final PeriodFactory MONTHLY = new PeriodFactory("P1M");
    public static final PeriodFactory YEARLY = new PeriodFactory("P1Y");

    private int year = LocalDate.now().getYear();
    private int month = LocalDate.now().getMonthValue();
    private int day = LocalDate.now().getDayOfMonth();

    public PeriodFactory(final String iso8601Period) {
        final Period period = Period.parse(iso8601Period);
        this.year = period.getYears();
        this.month = period.getMonths();
        this.day = period.getDays();
    }

    @Override
    protected Period newInstance(final @NonNull Simulation simulation) {
        return Period.of(year, month, day);
    }
}
