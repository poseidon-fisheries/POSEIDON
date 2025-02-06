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

package uk.ac.ox.poseidon.core.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.Month;
import java.time.MonthDay;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthDayFactory extends GlobalScopeFactory<MonthDay> {

    private int month;
    private int dayOfMonth;

    public MonthDayFactory(
        final Month month,
        final int dayOfMonth
    ) {
        this(month.getValue(), dayOfMonth);
    }

    public static MonthDayFactory parse(final CharSequence text) {
        final var monthDay = MonthDay.parse(text);
        return new MonthDayFactory(monthDay.getMonthValue(), monthDay.getDayOfMonth());
    }

    @Override
    protected MonthDay newInstance(final Simulation simulation) {
        return MonthDay.of(month, dayOfMonth);
    }
}
