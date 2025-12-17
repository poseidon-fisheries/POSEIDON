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

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Task;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BranchTaskFactory<T extends BranchTask<Vessel>> extends VesselTaskFactory<T> {

    @Singular
    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    private List<? extends Factory<? super VesselScope, ? extends Task<Vessel>>> children;

    public BranchTaskFactory(
        final Factory<? super VesselScope, ? extends Task<Vessel>> guard,
        final List<? extends Factory<? super VesselScope, ? extends Task<Vessel>>> children
    ) {
        super(guard);
        this.children = children;
    }

    @Override
    protected T newTask(final VesselScope scope) {
        final T task = newTask();
        if (children != null) {
            this.children
                .stream()
                .map(t -> t.get(scope))
                .forEach(task::addChild);
        }
        return task;
    }

    protected abstract T newTask();
}
