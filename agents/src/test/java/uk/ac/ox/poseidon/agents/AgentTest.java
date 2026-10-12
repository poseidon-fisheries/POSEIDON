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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AgentTest {

    private static final LocalDateTime START = LocalDateTime.of(2013, 1, 1, 0, 0);

    private final TemporalSchedule schedule = new TemporalSchedule(START);
    private final SimState simState = new SimState(0, schedule) {};
    private final RecordingBehaviour behaviour = new RecordingBehaviour();
    private final Agent agent = new Agent(schedule, mock(EventManager.class), behaviour);

    /**
     * A behaviour that records when it is stepped; active while {@link #active}. With a
     * {@link #taskDuration}, each step alternates between starting a task of that duration and
     * finishing it; without one, it is never running.
     */
    private class RecordingBehaviour implements Behaviour {
        private final List<LocalDateTime> stepDateTimes = new ArrayList<>();
        private boolean active;
        private boolean running;
        private Duration taskDuration;

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void step() {
            stepDateTimes.add(schedule.getDateTime());
            if (taskDuration != null) {
                running = !running;
                if (running) {
                    agent.setTaskDuration(taskDuration);
                }
            }
        }
    }

    /** Keeps the schedule from running out of events, which would stop it. */
    @BeforeEach
    void keepTheScheduleGoing() {
        schedule.scheduleRepeating(_ -> { }, 0, 1.0);
    }

    private void activate() {
        agent.mutate(() -> behaviour.active = true);
    }

    private void deactivate() {
        agent.mutate(() -> behaviour.active = false);
    }

    private void stepFor(final Duration duration) {
        schedule.stepFor(simState, duration);
    }

    @Test
    void anAgentThatIsNeverActivatedIsNeverStepped() {
        stepFor(ofSeconds(3));
        assertThat(behaviour.stepDateTimes).isEmpty();
    }

    @Test
    void activatingAnAgentStepsItEveryTickWhileItsBehaviourIsNotRunning() {
        activate();
        stepFor(ofSeconds(4));
        assertThat(behaviour.stepDateTimes).containsExactly(
            START.plusSeconds(1),
            START.plusSeconds(2),
            START.plusSeconds(3)
        );
    }

    @Test
    void mutatingAnInactiveAgentThatStaysInactiveSchedulesNothing() {
        agent.mutate(() -> { });
        stepFor(ofSeconds(3));
        assertThat(behaviour.stepDateTimes).isEmpty();
    }

    @Test
    void mutatingAnActiveAgentKeepsASingleStepChain() {
        activate();
        stepFor(ofSeconds(2));
        agent.mutate(() -> { });
        activate();
        stepFor(ofSeconds(2));
        assertThat(behaviour.stepDateTimes).containsExactly(
            START.plusSeconds(1),
            START.plusSeconds(2),
            START.plusSeconds(3)
        );
    }

    @Test
    void reactivatingWhileAStepIsPendingDoesNotScheduleASecondStep() {
        activate();
        deactivate();
        activate();
        stepFor(ofSeconds(3));
        assertThat(behaviour.stepDateTimes).containsExactly(
            START.plusSeconds(1),
            START.plusSeconds(2)
        );
    }

    @Test
    void aDeactivatedAgentStopsAfterItsPendingStepAndCanBeReactivated() {
        activate();
        stepFor(ofSeconds(2));
        deactivate();
        stepFor(ofSeconds(3));
        activate();
        stepFor(ofSeconds(2));
        assertThat(behaviour.stepDateTimes).containsExactly(
            START.plusSeconds(1),
            START.plusSeconds(6)
        );
    }

    @Test
    void aRunningBehaviourIsSteppedAgainAfterItsTaskDuration() {
        behaviour.taskDuration = ofHours(1);
        activate();
        stepFor(ofHours(2));
        assertThat(behaviour.stepDateTimes).startsWith(
            START.plusSeconds(1),
            START.plusHours(1).plusSeconds(1),
            START.plusHours(1).plusSeconds(2)
        );
    }

    @Test
    void mutationsWhileTheBehaviourIsRunningWaitUntilItStopsRunning() {
        behaviour.taskDuration = ofHours(1);
        activate();
        stepFor(ofSeconds(2));
        deactivate();
        assertThat(behaviour.active).isTrue();
        stepFor(ofHours(2));
        assertThat(behaviour.active).isFalse();
        assertThat(behaviour.stepDateTimes).containsExactly(
            START.plusSeconds(1),
            START.plusHours(1).plusSeconds(1)
        );
    }
}
