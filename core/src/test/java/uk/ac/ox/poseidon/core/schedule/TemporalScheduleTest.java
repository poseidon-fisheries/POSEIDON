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

package uk.ac.ox.poseidon.core.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalScheduleTest {

    private final LocalDateTime start = LocalDateTime.of(2013, 1, 1, 0, 0);
    private final TemporalSchedule schedule = new TemporalSchedule(start);

    @Test
    void givesTheDateTimeOfEachTimeConverted() {
        assertThat(schedule.toDateTime(3600)).isEqualTo(start.plusHours(1));
        assertThat(schedule.toDateTime(7200)).isEqualTo(start.plusHours(2));
        assertThat(schedule.toDateTime(3600)).isEqualTo(start.plusHours(1));
    }
}
