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

import com.badlogic.gdx.ai.btree.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link TaskFactory} base for tasks run against a {@link Vessel}.
 *
 * @param <T> the concrete task type built
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class VesselTaskFactory<T extends Task<Vessel>>
    extends TaskFactory<Vessel, VesselScope, T> {
    /** @param guard optional task guarding whether the built task runs at all */
    public VesselTaskFactory(final Factory<? super VesselScope, ? extends Task<Vessel>> guard) {
        super(guard);
    }

    /** @return {@link VesselScope}{@code .class} */
    @Override
    protected Class<VesselScope> scopeClass() {
        return VesselScope.class;
    }
}
