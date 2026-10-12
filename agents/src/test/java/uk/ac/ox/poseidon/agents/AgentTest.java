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

package uk.ac.ox.poseidon.agents;

import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AgentTest {

    private final TemporalSchedule schedule =
        new TemporalSchedule(LocalDateTime.of(2013, 1, 1, 0, 0));
    private final SimState simState = new SimState(0, schedule) {};

    /** A behaviour that is never running and counts its steps; active while {@link #active}. */
    private static class CountingBehaviour implements Behaviour {
        private boolean active;
        private int steps;

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void step() {
            steps++;
        }
    }

    @Test
    void reactivatingWhileAStepIsPendingDoesNotScheduleASecondStep() {
        final CountingBehaviour behaviour = new CountingBehaviour();
        final Agent agent = new Agent(schedule, mock(EventManager.class), behaviour);
        agent.mutate(() -> behaviour.active = true);
        agent.mutate(() -> behaviour.active = false);
        agent.mutate(() -> behaviour.active = true);
        schedule.step(simState);
        assertThat(behaviour.steps).isEqualTo(1);
    }
}
