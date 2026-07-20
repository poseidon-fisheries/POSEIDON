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

package uk.ac.ox.poseidon.core.providers.temporal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.ac.ox.poseidon.core.providers.Provider;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public class NextTimeAtProvider implements Provider<LocalDateTime> {

    @SuppressFBWarnings("EI2")
    private final TemporalSchedule schedule;
    private final LocalTime[] times;

    public NextTimeAtProvider(final TemporalSchedule schedule, final LocalTime[] times) {
        this.schedule = schedule;
        this.times = times.clone();
        Arrays.sort(this.times);
    }

    @Override
    public LocalDateTime get() {
        final LocalDateTime currentDateTime = schedule.getDateTime();
        final LocalDate today = currentDateTime.toLocalDate();
        final LocalTime now = currentDateTime.toLocalTime();
        final int nowSeconds = now.toSecondOfDay();

        for (final LocalTime time : times) {
            if (time.toSecondOfDay() > nowSeconds) {
                return today.atTime(time);
            }
        }

        return today.plusDays(1).atTime(times[0]);
    }
}
