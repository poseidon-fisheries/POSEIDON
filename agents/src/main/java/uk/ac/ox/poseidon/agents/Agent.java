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

import com.badlogic.gdx.ai.btree.BehaviorTree;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@RequiredArgsConstructor
public class Agent<A extends Agent<A>> implements Steppable {

    private static final int AGENT_BEHAVIOUR_ORDERING = 1;

    private final @NonNull TemporalSchedule schedule;
    private final @NonNull EventManager eventManager;

    private BehaviorTree<A> behaviour;

    private BehaviorTree<A> currentBehaviour;

    @Setter private Duration taskDuration;

    @Override
    public void step(final SimState simState) {
        if (currentBehaviour == null || currentBehaviour.getStatus() != RUNNING) {
            if (isActive())
                currentBehaviour = behaviour;
            else
                currentBehaviour = null;
        }
        if (currentBehaviour != null) {
            currentBehaviour.step();
            if (currentBehaviour.getStatus() == RUNNING) {
                checkNotNull(taskDuration);
                schedule.scheduleOnceIn(taskDuration, this, AGENT_BEHAVIOUR_ORDERING);
            } else {
                schedule.scheduleOnce(this, AGENT_BEHAVIOUR_ORDERING);
            }
        }
    }

    public boolean isActive() {
        return behaviour != null;
    }

    protected void mutate(final Runnable mutation) {
        final boolean previouslyActive = isActive();
        mutation.run();
        if (!previouslyActive && isActive() && currentBehaviour == null) {
            schedule.scheduleOnce(this, AGENT_BEHAVIOUR_ORDERING);
        }
    }

    public void setBehaviour(final BehaviorTree<A> behaviour) {
        mutate(() -> this.behaviour = behaviour);
    }
}
