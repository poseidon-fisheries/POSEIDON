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

package uk.ac.ox.poseidon.agents.tasks.branches;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;

import java.util.List;

@NoArgsConstructor
@SuperBuilder
public class SequenceTaskFactory extends BranchTaskFactory<Sequence<Vessel>> {

    public SequenceTaskFactory(
        final List<? extends Factory<? super VesselScope, ? extends Task<Vessel>>> children
    ) {
        super(children);
    }

    public SequenceTaskFactory(
        final VesselScopeFactory<? extends Task<Vessel>> guard,
        final List<? extends Factory<? super VesselScope, ? extends Task<Vessel>>> children
    ) {
        super(guard, children);
    }

    @Override
    protected Sequence<Vessel> newTask(final VesselScope scope) {
        return new Sequence<>();
    }
}
