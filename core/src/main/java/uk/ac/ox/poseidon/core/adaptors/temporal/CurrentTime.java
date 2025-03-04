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

package uk.ac.ox.poseidon.core.adaptors.temporal;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalTime;
import java.util.function.Function;

@RequiredArgsConstructor
public class CurrentTime implements Function<Object, LocalTime> {

    private final TemporalSchedule schedule;

    @Override
    public LocalTime apply(final Object o) {
        return schedule.getDateTime().toLocalTime();
    }

}
