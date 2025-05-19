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

import lombok.*;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.time.MonthDayFactory;

import java.time.MonthDay;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BetweenYearlyDatesFactory extends GlobalScopeFactory<BetweenYearlyDates> {

    private Factory<? extends MonthDay> start;
    private Factory<? extends MonthDay> end;

    public static BetweenYearlyDatesFactory parse(
        final CharSequence start,
        final CharSequence end
    ) {
        return new BetweenYearlyDatesFactory(
            MonthDayFactory.parse(start),
            MonthDayFactory.parse(end)
        );
    }

    @Override
    protected BetweenYearlyDates newInstance(final @NonNull Simulation simulation) {
        return new BetweenYearlyDates(
            start.get(simulation),
            end.get(simulation)
        );
    }
}
