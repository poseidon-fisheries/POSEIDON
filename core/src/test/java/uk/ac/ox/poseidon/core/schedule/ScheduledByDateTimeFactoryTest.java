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
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledByDateTimeFactoryTest {

    @Test
    void schedulesAndReturnsTheResolvedSteppables() {
        final List<Entry<LocalDateTime, Steppable>> steppables = List.of(
            entry(LocalDate.of(2020, 1, 1).atStartOfDay(), mock(Steppable.class)),
            entry(LocalDate.of(2021, 1, 1).atStartOfDay(), mock(Steppable.class))
        );
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getTemporalSchedule()).thenReturn(schedule);

        final List<Entry<LocalDateTime, Steppable>> result =
            Factories.scheduledByDateTime(_ -> steppables).get(new SimulationScope(simulation));

        verify(schedule).scheduleByDateTime(steppables);
        assertThat(result).isSameAs(steppables);
    }

}
