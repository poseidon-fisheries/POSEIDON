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

package uk.ac.ox.poseidon.agents.tasks;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.Agent;

import java.time.Duration;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;

/**
 * An {@link AgentTask} that runs over more than one step: on first execution it sets the agent's
 * task duration to {@link #getDuration()} and reports {@code RUNNING}; once that duration has
 * elapsed and the task is executed again, it reports the outcome of {@link #complete()}.
 *
 * @param <G> the type of agent this task runs against
 */
@RequiredArgsConstructor
public abstract class ExtendedTask<G extends Agent> extends AgentTask<G> {

    /**
     * @return {@link #complete()}'s outcome if already {@code RUNNING} (the task's duration has
     * elapsed), otherwise sets the agent's task duration to {@link #getDuration()} and returns
     * {@code RUNNING}
     */
    @Override
    public Status execute() {
        if (getStatus() == RUNNING)
            return complete();
        else {
            getAgent().setTaskDuration(getDuration());
            return RUNNING;
        }
    }

    /** @return how long this task should run for, computed once, when it starts */
    protected abstract Duration getDuration();

    /** @return this task's outcome once {@link #getDuration()} has elapsed */
    protected abstract Status complete();

}
