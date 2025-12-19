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

package uk.ac.ox.poseidon.agents.tasks.travel;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import lombok.*;
import uk.ac.ox.poseidon.agents.tasks.VesselTaskFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundTripFactory extends VesselTaskFactory<Sequence<Vessel>> {

    private Factory<? super VesselScope, ? extends Task<Vessel>> startTripTask;
    private Factory<? super VesselScope, ? extends Task<Vessel>> travelTask;
    private Factory<? super VesselScope, ? extends Task<Vessel>> fishingTask;
    private Factory<? super VesselScope, ? extends Task<Vessel>> landingTask;

    @SuppressWarnings("unchecked")
    @Override
    protected Sequence<Vessel> newTask(final VesselScope scope) {
        final Task<Vessel> startTripTask = this.startTripTask.get(scope);
        final Task<Vessel> travelTask = this.travelTask.get(scope);
        final Task<Vessel> fishingTask = this.fishingTask.get(scope);
        final Task<Vessel> landingTask = this.landingTask.get(scope);
        return new Sequence<>(
            startTripTask,
            travelTask,
            fishingTask,
            new SetDestinationToOrigin(),
            travelTask,
            landingTask,
            new EndTrip()
        );
    }

}
