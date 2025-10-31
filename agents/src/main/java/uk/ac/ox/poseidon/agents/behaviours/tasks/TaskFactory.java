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

package uk.ac.ox.poseidon.agents.behaviours.tasks;

import com.badlogic.gdx.ai.btree.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class TaskFactory<T extends Task<Vessel>> extends VesselScopeFactory<T> {

    /*
        TODO: there is no reason that task factories have to be _vessel_ scope instead of
         some other object scope, besides the fact that ObjectScopeFactory is currently
         abstract. If I was to fix this, we could make the whole "task factory" class
         hierarchy more generic, but there is no immediate benefit since vessels are currently
         the only agents in POSEIDON. Still, it would be a lot cleaner and open up nice
         new possibilities if I could get around to it... -- NP 2025-10-31.
     */

    private VesselScopeFactory<? extends Task<Vessel>> guard;

    protected abstract T newTask();

    @Override
    protected T newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        final T task = newTask();
        if (guard != null) task.setGuard(guard.get(simulation, vessel));
        return task;
    }
}
