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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;
import static lombok.AccessLevel.NONE;

@Getter
public class Agent implements Steppable {

    private static final int AGENT_BEHAVIOUR_ORDERING = 1;

    private final @NonNull TemporalSchedule schedule;
    private final @NonNull EventManager eventManager;

    private @NonNull Behaviour behaviour;

    @Getter(NONE)
    private final Queue<Runnable> mutationQueue = new LinkedList<>();

    @Setter private Duration taskDuration;

    @SuppressFBWarnings(
        value = "EI2",
        justification = "Agent keeps references to mutable schedule/event manager by design."
    )
    public Agent(
        @NonNull final TemporalSchedule schedule,
        @NonNull final EventManager eventManager,
        @NonNull final Behaviour behaviour
    ) {
        this.schedule = schedule;
        this.eventManager = eventManager;
        this.behaviour = behaviour;
    }

    @Override
    public void step(final SimState simState) {
        if (!behaviour.isRunning()) {
            while (!mutationQueue.isEmpty()) {
                mutationQueue.poll().run();
            }
        }
        if (isActive()) {
            behaviour.step();
            if (behaviour.isRunning()) {
                checkNotNull(taskDuration);
                schedule.scheduleOnceIn(taskDuration, this, AGENT_BEHAVIOUR_ORDERING);
            } else {
                schedule.scheduleOnce(this, AGENT_BEHAVIOUR_ORDERING);
            }
        }
    }

    public boolean isActive() {
        return behaviour.isActive();
    }

    /**
     * This method is meant for mutating the agent in ways that might change its "active" status.
     * Mutations can't be applied while an agent behaviour is running, so if the agent is currently
     * active, we queue the mutation, and it will be applied the next time the agent is stepped and
     * the current behaviour is not running.
     * <p>
     * If the agent is inactive, we apply the mutation immediately and check if its effect activated
     * the agent, in which case we schedule it to be stepped again.
     */
    protected void mutate(final Runnable mutation) {
        if (behaviour.isRunning()) {
            mutationQueue.add(mutation);
        } else {
            final boolean wasActive = isActive();
            mutation.run();
            if (!wasActive && isActive()) {
                schedule.scheduleOnce(this, AGENT_BEHAVIOUR_ORDERING);
            }
        }
    }

    public void setBehaviour(final Behaviour behaviour) {
        mutate(() -> this.behaviour = behaviour);
    }
}
