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

package uk.ac.ox.poseidon.core.providers.temporal;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.providers.Provider;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A {@link Provider} that returns the given time of day on the simulation's next calendar day
 * (i.e. always one day after the current simulated date), recomputed on every call. Built via
 * {@link Factories Factories.nextDayAtTime(...)} in this package.
 */
@RequiredArgsConstructor
public class NextDayAtTimeProvider implements Provider<LocalDateTime> {

    private final TemporalSchedule schedule;
    private final LocalTime time;

    @Override
    public LocalDateTime get() {
        return schedule.getDate().plusDays(1).atTime(time);
    }
}
